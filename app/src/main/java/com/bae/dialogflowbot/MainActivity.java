package com.bae.dialogflowbot;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bae.dialogflowbot.adapters.ChatAdapter;
import com.bae.dialogflowbot.helpers.SendMessageInBg;
import com.bae.dialogflowbot.interfaces.BotReply;
import com.bae.dialogflowbot.interfaces.RetrofitInterface;
import com.bae.dialogflowbot.models.Consultant;
import com.bae.dialogflowbot.models.DataManager;
import com.bae.dialogflowbot.models.Message;
import com.bae.dialogflowbot.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.common.collect.Lists;
import com.google.protobuf.ListValue;
import com.google.protobuf.Value;

import java.io.InputStream;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import android.speech.tts.TextToSpeech;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements BotReply {

  RecyclerView chatView;
  ChatAdapter chatAdapter;
  List<Message> messageList = new ArrayList<>();
  EditText editMessage;
  ImageButton btnSend;

  //dialogFlow
  private SessionsClient sessionsClient;
  private SessionName sessionName;
  private String uuid = UUID.randomUUID().toString();
  private String TAG = "mainactivity";

  private TextToSpeech mTTS;
  private EditText mEditText;

  private AlarmManager alarmManager;
  private GregorianCalendar mCalender;

  private NotificationManager notificationManager;
  NotificationCompat.Builder builder;

  String from;

  int no;
  int after;
  int alarm = 0;

  long number;
  List<String> content= new ArrayList<>();
  User user;
  String neg_val;
  String appointment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    chatView = findViewById(R.id.chatView);
    editMessage = findViewById(R.id.editMessage);
    btnSend = findViewById(R.id.btnSend);

    chatAdapter = new ChatAdapter(messageList, this);
    chatView.setAdapter(chatAdapter);

    SoundPlayer.initSounds(getApplicationContext());

    Intent intent = getIntent();
    no = intent.getIntExtra("daily", 0);
    after = ((LoginActivity)LoginActivity.context_login).after;
    System.out.println("after: " + after);

    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        VoiceTask voiceTask = new VoiceTask();
        voiceTask.execute();
      }
    });

    mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
      @Override
      public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
          int result = mTTS.setLanguage(Locale.KOREAN);
          if (result == TextToSpeech.LANG_MISSING_DATA
                  || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("TTS", "Language not supported");
          }
        } else {
          Log.e("TTS", "Initialization failed");
        }
      }
    });



    setUpBot();

    if (no == 1){
      String str = "daily";
      sendMessageToBot(str);
    }
    if (after == 1){
      String str = "after";
      sendMessageToBot(str);
    }

    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

    mCalender = new GregorianCalendar();

    Log.v("HelloAlarmActivity", mCalender.getTime().toString());
  }

  private void setAlarm() {
    //AlarmReceiver에 값 전달
    Intent receiverIntent = new Intent(MainActivity.this, AlarmReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, receiverIntent, 0);

    //String from = "2021-06-01 17:20:00"; //임의로 날짜와 시간을 지정

    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
    SimpleDateFormat dt = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);

    System.out.println("current: " + df.format(cal.getTime()));

    cal.add(Calendar.DATE, 7);
    System.out.println("after: " + df.format(cal.getTime()));

    String getDate = df.format(cal.getTime());
    from = getDate + " 10:00:00";
    System.out.println("from: " + from);


    //날짜 포맷을 바꿔주는 소스코드
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date datetime = null;
    try {
      datetime = dateFormat.parse(from);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(datetime);

    alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(),pendingIntent);
  }

  private void setUpBot() {
    try {
      InputStream stream = this.getResources().openRawResource(R.raw.chatbot2_credential);
      GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
          .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
      String projectId = ((ServiceAccountCredentials) credentials).getProjectId();

      SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
      SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(
          FixedCredentialsProvider.create(credentials)).build();
      sessionsClient = SessionsClient.create(sessionsSettings);
      sessionName = SessionName.of(projectId, uuid);

      Log.d(TAG, "projectId : " + projectId);
    } catch (Exception e) {
      Log.d(TAG, "setUpBot: " + e.getMessage());
    }
  }

  private void sendMessageToBot(String message) {
    QueryInput input = QueryInput.newBuilder()
        .setText(TextInput.newBuilder().setText(message).setLanguageCode("ko-kr")).build();
    new SendMessageInBg(this, sessionName, sessionsClient, input).execute();
  }


  String botReply;

  private void speak(String str) {
    mTTS.speak(str, TextToSpeech.QUEUE_ADD, null);
  }


  Dialog emergencydialog;
  public void showDialog01(){
    emergencydialog.show(); // 다이얼로그 띄우기

    TextView txt = emergencydialog.findViewById(R.id.txtText);
    txt.setText("전문적인 치료가 필요할 것 같아요. 전화를 연결하시겠어요?");
    /* 이 함수 안에 원하는 디자인과 기능을 구현하면 된다. */

    // 위젯 연결 방식은 각자 취향대로~
    // '아래 아니오 버튼'처럼 일반적인 방법대로 연결하면 재사용에 용이하고,
    // '아래 네 버튼'처럼 바로 연결하면 일회성으로 사용하기 편함.
    // *주의할 점: findViewById()를 쓸 때는 -> 앞에 반드시 다이얼로그 이름을 붙여야 한다.

    // 아니오 버튼
    Button noBtn = emergencydialog.findViewById(R.id.Button2);
    noBtn.setText("종료하기");
    noBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // 원하는 기능 구현
        emergencydialog.dismiss();
        finish(); // 앱 종료
      }
    });
    // 네 버튼
    Button yesBtn = emergencydialog.findViewById(R.id.Button1);
    yesBtn.setText("전화하기");
    emergencydialog.findViewById(R.id.Button1).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // 원하는 기능 구현
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel: 119"));
        startActivity(intent);
        emergencydialog.dismiss();
        finish();
      }
    });
  }

  @Override
  public void callback(DetectIntentResponse returnResponse) {
     if(returnResponse!=null) {
       System.out.println(returnResponse.getQueryResult().getAllFields());
       Map<String, Value> neg = returnResponse.getQueryResult().getParameters().getFields();

       if (returnResponse.getQueryResult().getParameters().getFieldsMap().keySet().contains("negative")) {
         System.out.println("parameters fields: " + neg.get("negative"));
         System.out.println(neg.get("negative").getListValue().getValuesList());
       }
       botReply = returnResponse.getQueryResult().getFulfillmentText();
       if(!botReply.isEmpty()) {
         ArrayList<String> colors = new ArrayList<>(Arrays.asList("positive", "negative", "no emotion"));
         ArrayList<String> emos = new ArrayList<>(Arrays.asList("positive", "negative", "no emotion"));

         if (no == 1) {
           if (botReply.equals("start daily") || botReply.equals("노래 추천") || botReply.equals("after")) {
             botReply = "";
           } else if (!emos.contains(botReply) && botReply.contains("http")) {
             Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(botReply));
             startActivity(intent);
             botReply = "";
           } else if (botReply.equals("사진 출력")) {
             String pic = "이건 어떠세요?";
             messageList.add(new Message(botReply, true));
             speak(pic);
           }
           else {
             if (botReply.equals("positive")) {
               SoundPlayer.play(SoundPlayer.POSITIVE);
               botReply = "다행이네요, 좋아보이셔서 저도 행복해지네요!";
             }
             if (botReply.equals("negative")) {
               SoundPlayer.play(SoundPlayer.NEGATIVE);
               botReply = "아이고, 괜찮아요. 곧 모든 게 나아질 거에요! 이 사운드를 듣고 기분이 풀렸으면 좋겠어요.";
             }
             if (botReply.equals("no emotion")) {
               SoundPlayer.play(SoundPlayer.NO_EMOTION);
               botReply = "그렇군요! 제가 내담자님에게 조금이라도 도움이 되었으면 좋겠어요!";
             }
             messageList.add(new Message(botReply, true));

           }
           content.add(botReply);
           chatAdapter.notifyDataSetChanged();
           Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
         } else {

           if (botReply.equals("Warning!")) {
             emergencydialog = new Dialog(MainActivity.this);       // Dialog 초기화
             emergencydialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
             emergencydialog.setContentView(R.layout.activity_popup);

             showDialog01();
           }
           else if (botReply.equals("therapy")){
             alarm = 1;
             String str = "restart";
             Intent intent = new Intent(this, FrontPageActivity.class);
             sendMessageToBot(str);
             ((LoginActivity)LoginActivity.context_login).after = 0;
             startActivity(intent);
           }
           else if(botReply.equals("again")){
             setAlarm();
             content.add(botReply);
             messageList.add(new Message(from, true));
             chatAdapter.notifyDataSetChanged();
             Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
           }
           else if(botReply.equals("finish")){
             alarm = 1;
             Intent intent = new Intent(this, FinishActivity.class);
             Date mdate = Calendar.getInstance().getTime();
             SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
             String date = df.format(mdate);
             intent.putExtra("date", date);
             startActivity(intent);
           }
           else {
             content.add(botReply);
             messageList.add(new Message(botReply, true));
             chatAdapter.notifyDataSetChanged();
             Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
           }
         }
         String[] arr = new String[]{"그런 상황이시군요,, 많이 고통스러우셨겠어요. ",
                 "그러시군요, 상황이 많이 고통스러우시겠어요. ",
                 "그런 상황이시군요,, 많이 힘드시겠어요. ",
                 "그러시군요, 많이 힘드셨겠어요. ",
                 "그런 상황이시군요, 많이 힘드셨겠어요. ",
                 "심적으로 정말 힘드셨겠네요. ", "다행이네요,", "아이고, 괜찮아요.", "그러시군요"};
         String result = "";

         for (String item : arr) {
           if (botReply.contains(item)) {
             int target_num = botReply.indexOf(item);
             result = botReply.substring((item.length() + target_num), botReply.length() - 1);

             mTTS.setPitch(0.9f);        // 음성 톤을 09배 내려준다.
             mTTS.setSpeechRate(0.7f);    // 읽는 속도는 0.7 빠르기로 설정
             speak(item);

             mTTS.setPitch(1.0f);        // 기본  음성 톤.
             mTTS.setSpeechRate(1.0f);    // 기본  읽는 속도.
             speak(result);
             break;
           }
         }

         if (result.equals("") && !botReply.equals("사진 출력")) {
           speak(botReply);
         }

       }else {
         Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
       }
     } else {
       Toast.makeText(this, "failed to connect!", Toast.LENGTH_SHORT).show();
     }
  }

  @Override
  protected void onDestroy() {
    if (mTTS != null) {
      mTTS.stop();
      mTTS.shutdown();
    }
    super.onDestroy();

    if (alarm == 0) {
      setAlarm();
    }
  }


  public class VoiceTask extends AsyncTask<String, Integer, String> {
    String str = null;

    @Override
    protected String doInBackground(String... params) {
      // TODO Auto-generated method stub
      try {
        getVoice();
      } catch (Exception e) {
        // TODO: handle exception
      }
      return str;
    }

    @Override
    protected void onPostExecute(String result) {
      try {

      } catch (Exception e) {
        Log.d("onActivityResult", "getImageURL exception");
      }
    }
  }

  private void getVoice() {

    Intent intent = new Intent();
    intent.setAction(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

    String language = "ko-KR";

    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
    startActivityForResult(intent, 2);

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // TODO Auto-generated method stub
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == RESULT_OK) {

      ArrayList<String> results = data
              .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

      String str = results.get(0);

      messageList.add(new Message(str, false));
      sendMessageToBot(str);
      content.add(str);
      Objects.requireNonNull(chatView.getAdapter()).notifyDataSetChanged();
      Objects.requireNonNull(chatView.getLayoutManager())
              .scrollToPosition(messageList.size() - 1);
      //TextView tv = findViewById(R.id.editMessage);
      //tv.setText(str);
    }
  }


  protected void sendToDB(List<String> cont,String negative){
//        user = ((LoginActivity)LoginActivity.context_login).user;
//        System.out.println("after: " + after);

    System.out.println(cont);

    System.out.println("after: "+after);
    DataManager dataManager = DataManager.getInstance();
    user = dataManager.getUser();
    Long personal_num = user.getPersonal_num();
    String name= user.getName();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://ec2-user@ec2-3-35-45-32.ap-northeast-2.compute.amazonaws.com:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    RetrofitInterface retrofitInterface=retrofit.create(RetrofitInterface.class);
    // System.out.println(1);
    System.out.println(name + " " + personal_num);
    Consultant consultant_content = new Consultant(name,personal_num,cont,negative);


    Call<Consultant> createcontent = retrofitInterface.createContent(consultant_content);
    createcontent.enqueue(new Callback<Consultant>() {
      @Override
      public void onResponse(Call<Consultant> call, Response<Consultant> response) {
        if (response.isSuccessful()) {
          //  dataManager.setConsultant(consultant_content);
          Log.d(TAG, "onResponse1: Something Happen");
          number=response.body().getC_consultant_num();

          User updateuser=new User(user.getPersonal_num(),user.getId(),user.getPw(),user.getName(),number);

          update(updateuser);


        } else {
          Log.d(TAG, "onResponse1: Something Wrong");
        }
      }
      @Override
      public void onFailure(Call<Consultant> call, Throwable t) {
        Log.d(TAG, "onResponse2: Something Wrong");
      }


    });




  }

  @Override
  protected void onStop() {
    super.onStop();
    if (alarm == 0) {
      setAlarm();
      //content = content.replace("\n", "@");
      if(no != 1) {
        content.remove("null");
        sendToDB(content, neg_val);
      }
    }
  }
  public void update(User user2){
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://ec2-user@ec2-3-35-45-32.ap-northeast-2.compute.amazonaws.com:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();



    RetrofitInterface retrofitInterface=retrofit.create(RetrofitInterface.class);
    Call<User> user1 = retrofitInterface.updateuser(user.getPersonal_num(),user2);
    user1.enqueue(new Callback<User>() {
      @Override
      public void onResponse(Call<User> call, Response<User> response) {
        Log.d(TAG, "onResponse1: Something Happen");
      }

      @Override
      public void onFailure(Call<User> call, Throwable t) {
        Log.d(TAG, "onResponse2: Something Wrong");

      }
    });
  }
}
