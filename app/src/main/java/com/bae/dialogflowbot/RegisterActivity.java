package com.bae.dialogflowbot;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
//import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bae.dialogflowbot.interfaces.RetrofitInterface;
import com.bae.dialogflowbot.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;

import static androidx.constraintlayout.widget.Constraints.TAG;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;


public class RegisterActivity extends AppCompatActivity {

    EditText et_id, et_pass, et_name;
    Button btn_register;
    Button validateButton;
    private AlertDialog dialog;
    private boolean validate;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // 액티비티 시작시 처음으로 실행되는 생명주기!
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        // 아이디 값 찾아주기
        et_id = findViewById(R.id.et_id);
        et_pass = findViewById(R.id.et_pass);
        et_name = findViewById(R.id.et_name);

        //id 중복 버튼 클릭시 수행
        validateButton = findViewById(R.id.validateButton);
        validateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String userID = et_id.getText().toString();
                if (validate) {
                    return;
                }
                if (userID.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("아이디는 빈칸일 수 없습니다!")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }


                System.out.println(1);

                checkid(userID);
            }
        });


        // 회원가입 버튼 클릭 시 수행
        btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // EditText에 현재 입력되어있는 값을 get(가져온다)해온다.
                String userID = et_id.getText().toString();
                String userPass = et_pass.getText().toString();
                String userName = et_name.getText().toString();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://ec2-user@ec2-3-35-45-32.ap-northeast-2.compute.amazonaws.com:8080/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();


                System.out.println(1);
                User user = new User(userID, userPass, userName);
                RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

                Call<User> signup = retrofitInterface.createUser(user);
                System.out.println(signup);
                signup.enqueue(new Callback<User>() {

                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "onResponse1: Something Happen");
                            Toast.makeText(getApplicationContext(), "회원 등록에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);


                        } else {
                            Log.d(TAG, "onResponse1: Something Wrong");

                        }

                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.d(TAG, "onResponse2: Something Wrong");

                    }
                });
            }
        });
    }

    public void checkid(String userID) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://ec2-user@ec2-3-35-45-32.ap-northeast-2.compute.amazonaws.com:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
        Call<User> idcheck = retrofitInterface.idCheck(userID);
        idcheck.enqueue(new Callback<User>() {

            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "아이디 중복");
                    // Toast.makeText(getApplicationContext(), "사용할 수 없는 아이디입니다..", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("사용할 수 없는 아이디입니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                } else {
                    Log.d(TAG, "사용가능");
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("사용할 수 있는 아이디입니다.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    et_id.setEnabled(false);
                    validate = true;
                    validateButton.setText("확인");

                }

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d(TAG, "사용가능");
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                dialog = builder.setMessage("사용할 수 있는 아이디입니다.")
                        .setPositiveButton("확인", null)
                        .create();
                dialog.show();
                et_id.setEnabled(false);
                validate = true;
                validateButton.setText("확인");

            }

        });


    }
}


