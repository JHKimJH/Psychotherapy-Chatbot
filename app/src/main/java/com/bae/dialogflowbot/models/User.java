package com.bae.dialogflowbot.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;

public class User {
    public User(Long personal_num, String id, String pw, String name,
                Long consultant_num) {
        this.personal_num = personal_num;
        this.id = id;
        this.name = name;
        this.pw = pw;
        this.consultant_num = consultant_num;

    }

    public User(String id) {
        this.id =id;
}
    public User(String id, String pw) {
        this.id = id;
        this.pw = pw;
    }

    public User(String id, String pw, String name) {
        this.id = id;
        this.name = name;
        this.pw = pw;
    }



    @SerializedName("personal_num")
    @Expose
    private Long personal_num;

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("pw")
    @Expose
    private String pw;

    @SerializedName("consultant_num")
    @Expose
    private Long consultant_num;

    public Long getPersonal_num() {
        return personal_num;
    }

    public void setPersonal_num(Long personal_num) {
        this.personal_num = personal_num;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public Long getConsult_num() {
        return consultant_num;
    }

    public void setConsult_num(Long consult_num) {
        this.consultant_num = consultant_num;
    }
}
