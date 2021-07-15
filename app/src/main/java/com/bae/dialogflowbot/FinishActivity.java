package com.bae.dialogflowbot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bae.dialogflowbot.interfaces.RetrofitInterface;
import com.bae.dialogflowbot.models.Consultant;
import com.bae.dialogflowbot.models.DataManager;
import com.bae.dialogflowbot.models.Message;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FinishActivity extends AppCompatActivity {

    String negative;
    DataManager dataManager = DataManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        check(dataManager.getUser().getConsult_num());

        Intent intent = getIntent();
        String date = intent.getStringExtra("date");



        TextView datetext = (TextView) findViewById(R.id.textView5);
        datetext.setText(date);


        String name = ((LoginActivity)LoginActivity.context_login).u_ID;
        TextView nametext = (TextView) findViewById(R.id.textView3);
        nametext.setText(name);



    }

    public void clickBtn(View view){
        Intent intent = new Intent(this, FrontPageActivity.class);
        startActivity(intent);
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
                    System.out.println(consultant.getNegative());
                    negative = consultant.getNegative();
                    System.out.println(negative);

                    TextView cert = (TextView) findViewById(R.id.textView4);
                    String certificate= "위 사람은 어려움을 극복하고\n본인의 이야기를 풀어냄으로써\n본인의 감정을 되짚으며\n본인에 대해 더 이해하고\n이를 통해 꾸준한 노력으로\n" + negative +"을/를 이겨낼 수 있었기에\n이 증서를 드립니다.";
                    cert.setText(certificate);

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