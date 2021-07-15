package com.bae.dialogflowbot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bae.dialogflowbot.adapters.ChatAdapter;
import com.bae.dialogflowbot.interfaces.BotReply;
import com.bae.dialogflowbot.interfaces.RetrofitInterface;
import com.bae.dialogflowbot.models.Consultant;
import com.bae.dialogflowbot.models.DataManager;
import com.bae.dialogflowbot.models.Message;
import com.bae.dialogflowbot.models.User;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class MyPageActivity extends AppCompatActivity implements BotReply {

    RecyclerView chatView;
    ChatAdapter chatAdapter;
    List<Message> messageList = new ArrayList<>();
    DataManager dataManager = DataManager.getInstance();
    List<String> list_content=new ArrayList<>();

    Consultant consultant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        chatView = findViewById(R.id.chatView);

        chatAdapter = new ChatAdapter(messageList, this);
        chatView.setAdapter(chatAdapter);



        check(dataManager.getUser().getConsult_num());
       // consultant = dataManager.getConsultant();
        //String content=getIntent().getStringExtra("content");
        //String content = consultant.getConsultant_content();

       // content = content.replace("@", "\n");
       // String[] array = content.split("/");
        System.out.println(list_content+"과연2");

    }

    @Override
    public void callback(DetectIntentResponse returnResponse) {
    }

    public void check(Long c_num){
        System.out.println(c_num);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://ec2-user@ec2-3-35-45-32.ap-northeast-2.compute.amazonaws.com:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

        Call<Consultant> c_num1Check = retrofitInterface.c_numCheck(c_num);
        c_num1Check.enqueue(new Callback<Consultant>()

        {
            @Override
            public void onResponse (Call <Consultant> call, Response<Consultant> response){

                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse1: Something Happen");
                    Consultant consultant = response.body();
                    System.out.println(consultant.getConsultant_content());
                    list_content=consultant.getConsultant_content();

                    System.out.println(list_content+"과연");
                    int i = 0;
                    while (i < list_content.size()) {
                        if (i < list_content.size()) {
                            messageList.add(new Message(list_content.get(i), false));
                            Objects.requireNonNull(chatView.getAdapter()).notifyDataSetChanged();
                            Objects.requireNonNull(chatView.getLayoutManager())
                                    .scrollToPosition(messageList.size() - 1);
                        }
                        if (i + 1 < list_content.size()) {
                            messageList.add(new Message(list_content.get(++i), true));
                            chatAdapter.notifyDataSetChanged();
                            Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
                        }
                        i++;
                    }

                } else {
                    Log.d(TAG, "onResponse1: Something Wrong");
                    Toast.makeText(getApplicationContext(), "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    return;

                }
            }

            @Override
            public void onFailure (Call < Consultant > call, Throwable t){
                Log.d(TAG, "onResponse2: Something Happen");

            }
        });

    }



}