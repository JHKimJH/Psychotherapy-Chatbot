package com.bae.dialogflowbot.interfaces;

import com.bae.dialogflowbot.models.Consultant;
import com.bae.dialogflowbot.models.User;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;




public interface RetrofitInterface {
    @POST("jts/join")
        //Call<Member> createMember(@FieldMap HashMap<String, Object> parameters);
    Call<User> createUser(@Body User parameters);

    @POST("jts/signIn")
        //Call<Member> createMember(@FieldMap HashMap<String, Object> parameters);
    Call<User> loginUser(@Body HashMap<String,Object> parameters);

    //id중복 확인
   @GET("jts/join/{id}")
   Call<User>idCheck(@Path("id") String id);
     //SentToDB
    @POST("jts/consultant/content")
    Call<Consultant> createContent(@Body Consultant parameters);

    @PUT("jts/update/{personal_num}")
    Call<User>updateuser(@Path("personal_num") Long personal_num, @Body User parameter);

    @GET("jts/consultant/{c_consultant_num}")
    Call<Consultant> c_numCheck(@Path("c_consultant_num") Long c_consultant_num);


}
