package com.bae.dialogflowbot;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.api.SystemParameterRule;
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
import com.google.protobuf.Value;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MainActivity2 extends AppCompatActivity implements BotReply {

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

    private EditText mEditText;

    private AlarmManager alarmManager;
    private GregorianCalendar mCalender;

    private NotificationManager notificationManager;
    NotificationCompat.Builder builder;



    int no;
    int after;
    int alarm = 0;


    String from;

    long number;
    List<String> content= new ArrayList<>();
    User user;
    String neg_val;
    String appointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
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



        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                String message = editMessage.getText().toString();
                if (!message.isEmpty()) {
                    messageList.add(new Message(message, false));
                    editMessage.setText("");
                    content.add(message);
                    sendMessageToBot(message);
                    Objects.requireNonNull(chatView.getAdapter()).notifyDataSetChanged();
                    Objects.requireNonNull(chatView.getLayoutManager())
                            .scrollToPosition(messageList.size() - 1);
                } else {
                    Toast.makeText(MainActivity2.this, "Please enter text!", Toast.LENGTH_SHORT).show();
                }
            }
        });



        setUpBot();

        System.out.println("after: " + after);
        if (no == 1){
            String str = "daily";
            content.add(str);
            sendMessageToBot(str);
        }
        if (after == 1){
            String str = "after";
            content.add(str);
            sendMessageToBot(str);
        }

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        mCalender = new GregorianCalendar();

        Log.v("HelloAlarmActivity", mCalender.getTime().toString());
    }


    private void setAlarm() {
        //AlarmReceiver에 값 전달
        Intent receiverIntent = new Intent(MainActivity2.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity2.this, 0, receiverIntent, 0);

        //String from = "2021-06-01 17:20:00"; //임의로 날짜와 시간을 지정

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        //SimpleDateFormat dt = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);

        System.out.println("current: " + df.format(cal.getTime()));

        //cal.add(Calendar.DATE, 7);
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
                .setText(TextInput.newBuilder().setText(message).setLanguageCode("UTF-8")).build();
        new SendMessageInBg(this, sessionName, sessionsClient, input).execute();
    }


    String botReply;



    //Emergency Pop-up
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

    private class ImageGetter implements Html.ImageGetter {
        public Drawable getDrawable(String source) {
            source = source.replaceAll(".png", "");  //이때 .png .jpg 등이 붙어 있으면 drawable에서 이미지를 못불러 온다.

            int id = MainActivity2.this.getResources().getIdentifier(source, "drawable", MainActivity2.this.getPackageName());
            Drawable d = MainActivity2.this.getResources().getDrawable(id);
            int w = d.getIntrinsicWidth();
            int h = d.getIntrinsicHeight();
            d.setBounds(0,0, w, h); //이미지 크기 설정
            return d;
        }
    };

    List<String> piclist = new ArrayList<>(Arrays.asList("sea", "river", "tree", "rain", "forest"));
    String pic;


    @Override
    public void callback(DetectIntentResponse returnResponse) {
        if(returnResponse!=null) {
            Map<String, Value> neg = returnResponse.getQueryResult().getParameters().getFields();
            if (returnResponse.getQueryResult().getParameters().getFieldsMap().keySet().contains("neg_ex")) {
                System.out.println("parameters fields: " + neg.get("neg_ex"));
                neg_val = neg.get("neg_ex").getStringValue();
                System.out.println(neg_val);
            }
            if(returnResponse.getQueryResult().getIntent().getDisplayName().contains("todo_week")){
                appointment = returnResponse.getQueryResult().getQueryText();
                System.out.println("intent: " + returnResponse.getQueryResult().getQueryText());
            }

            botReply = returnResponse.getQueryResult().getFulfillmentText();

            if(!botReply.isEmpty()) {
                ArrayList<String> colors = new ArrayList<>(Arrays.asList("positive", "negative", "no emotion"));
                ArrayList<String> emos = new ArrayList<>(Arrays.asList("positive", "negative", "no emotion"));

                if (no == 1) {
                    if (botReply.equals("start daily") || botReply.equals("노래 추천")) {
                    }
                    else if (!emos.contains(botReply) && botReply.contains("http")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(botReply));
                        startActivity(intent);
                    }
                    else if(botReply.contains("사진 출력")){
                        Collections.shuffle(piclist);
                        pic = piclist.get(0);

                        String htmltext = "이건 어떠세요? </br>" +
                                "<p style = \"text-align: center;\">" +  "<img src='" + pic + ".png'/> </p>";

                        botReply = htmltext;

                        messageList.add(new Message(botReply, true));
                        chatAdapter.notifyDataSetChanged();
                        Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
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
                        content.add(botReply);
                        messageList.add(new Message(botReply, true));
                        chatAdapter.notifyDataSetChanged();
                        Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
                    }

                }
                else {
                    if(botReply.equals("after")){
                    }
                    if (botReply.equals("Warning!")) {
                        emergencydialog = new Dialog(MainActivity2.this);       // Dialog 초기화
                        emergencydialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
                        emergencydialog.setContentView(R.layout.activity_popup);

                        showDialog01();
                        content.add(botReply);
                        messageList.add(new Message(botReply, true));
                    }
                    else if (botReply.equals("therapy")){
                        alarm = 1;
                        String str = "restart";
                        Intent intent = new Intent(this, FrontPageActivity.class);
                        content.add(botReply);
                        sendMessageToBot(str);
                        ((LoginActivity)LoginActivity.context_login).after = 0;
                        startActivity(intent);
                    }
                    else if(botReply.equals("again")){
                        setAlarm();
                        String str = "좋아요, 그럼 일주일 동안 다시 실천해보고 " + from + "에 다시 만나요!";
                        content.add(botReply);
                        messageList.add(new Message(str, true));
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
            }else {
                Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "failed to connect!", Toast.LENGTH_SHORT).show();
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
            if(no != 1 && after != 1 && !content.isEmpty()) {
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
