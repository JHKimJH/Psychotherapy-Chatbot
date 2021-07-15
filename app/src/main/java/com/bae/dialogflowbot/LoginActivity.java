package com.bae.dialogflowbot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
//import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.bae.dialogflowbot.interfaces.RetrofitInterface;
import com.bae.dialogflowbot.models.DataManager;
import com.bae.dialogflowbot.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class LoginActivity extends AppCompatActivity {
    private EditText et_id, et_pass;
    private Button btn_register;
    public static Context context_login;
    public int after;
    public String u_ID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context_login = this;

        et_id = findViewById(R.id.et_id);
        et_pass = findViewById(R.id.et_pass);
        Button btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);

        Intent intent = getIntent();
        after = intent.getIntExtra("after", 0);


        // 회원가입 버튼을 클릭 시 수행
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // EditText에 현재 입력되어있는 값을 get(가져온다)해온다.
                String userID = et_id.getText().toString();
                String userPass = et_pass.getText().toString();

                u_ID = userID;

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("id", userID);
                hashMap.put("pw", userPass);
                login(hashMap);
            }

        });
    }

    public void login(HashMap<String, Object> parameters) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://ec2-user@ec2-3-35-45-32.ap-northeast-2.compute.amazonaws.com:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

        Call<User> login = retrofitInterface.loginUser(parameters);

        login.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse1: Something Happen");
                    Toast.makeText(getApplicationContext(), "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show();

                            /*response body가 로그인한 User의 정보를 서버에서 받아옴!
                            싱클톤 패턴으로 DataManager를 구성하여 User의 정보를 get, set할 수 있게함
                            만약에 user이름을 받아오고 싶으면 DataManager객체 만든후에 dataManager.getname() 이용하세요*/

                    User user = response.body();
                    DataManager dataManager = DataManager.getInstance();
                    dataManager.setUser(user);

                    Intent intent = new Intent(LoginActivity.this, FrontPageActivity.class);
                    //위에서 사용한 DataManager를 구성해서 넘겨올수있게 되었어요.
                     //그래서 이 부분에서 변수를 안넘겨줘도 될꺼같아요
                         /*   intent.putExtra("userID", parameters.get(userID));
                            intent.putExtra("userPass", userPass); */
                    et_id.setText("");
                    et_pass.setText("");
                    startActivity(intent);


                } else {
                    Log.d(TAG, "onResponse1: Something Wrong");
                    Toast.makeText(getApplicationContext(), "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    return;

                }

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(getBaseContext(), "존재하지 않는 ID이거나 " + "\n" +
                        "비밀번호가 틀렸습니다!", Toast.LENGTH_LONG).show();
                ;
                Log.d(TAG, "onFailure2: Something Wrong");

            }

        });


    }
}