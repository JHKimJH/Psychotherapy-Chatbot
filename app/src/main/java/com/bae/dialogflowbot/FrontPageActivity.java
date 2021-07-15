package com.bae.dialogflowbot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bae.dialogflowbot.interfaces.RetrofitInterface;
import com.bae.dialogflowbot.models.Consultant;
import com.bae.dialogflowbot.models.DataManager;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FrontPageActivity extends AppCompatActivity {

    int daily = 0;
    DataManager dataManager=DataManager.getInstance();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_page);
        ImageButton b =findViewById(R.id.imageButton);
        ImageButton b2=findViewById(R.id.imageButton2);
        Button b3=findViewById(R.id.button3);

        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){

                Intent intent =new Intent();
                intent.setAction(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:119"));
                startActivity(intent);

            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){

                Intent intent =new Intent();
                intent.setAction(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:1577-0199"));
                startActivity(intent);
            }
        });


        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               System.out.println(dataManager.getUser().getConsult_num());
              //  check(dataManager.getUser().getConsult_num());
                Intent intent = new Intent(FrontPageActivity.this, MyPageActivity.class);
                // intent.put("content",);
                startActivity(intent);



            }
        });


        if ((dataManager.getUser().getConsult_num())==null) {
            b3.setVisibility(Button.GONE);

        } else {
            b3.setVisibility(Button.VISIBLE);

        }

    }



    public void clickBtn(View view){
        Intent intent = new Intent(this, PopupActivity.class);
        intent.putExtra("data", "음성 기능을 사용하시겠어요?");
        daily = 0;
        startActivityForResult(intent, 1);
    }

    public void clickDailyBtn(View view){
        Intent intent = new Intent(this, PopupActivity.class);
        intent.putExtra("data", "음성 기능을 사용하시겠어요?");
        daily = 1;
        startActivityForResult(intent, 1);
    }

   /* public void clickMyPageBtn(View view){
        Intent intent = new Intent(this, MyPageActivity.class);

        check(dataManager.getUser().getConsult_num());
        System.out.println(dataManager.getConsultant().getC_name());
        startActivity(intent);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int RESULT_Voice = 0;
        if (requestCode == 1) {
            if (resultCode == RESULT_Voice) {
                //데이터 받기
                Intent intent = new Intent(this, MainActivity.class);
                if(daily == 1){
                    intent.putExtra("daily", 1) ;
                }
                else{
                    intent.putExtra("daily", 0) ;
                }
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(this, MainActivity2.class);
                if(daily == 1){
                    intent.putExtra("daily", 1) ;
                }
                else{
                    intent.putExtra("daily", 0) ;
                }
                startActivity(intent);
            }
        }
    }


}