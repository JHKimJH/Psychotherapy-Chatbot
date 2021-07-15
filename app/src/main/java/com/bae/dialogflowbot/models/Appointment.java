package com.bae.dialogflowbot.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.type.Date;
import com.google.type.DateTime;

import java.time.LocalDate;

public class Appointment {

    @SerializedName("a_personal_num")
    @Expose
    private Long a_personal_num;

    @SerializedName("a_name")
    @Expose
    private String a_name;

    @SerializedName("a_consultant_num")
    @Expose
    private Long a_consultant_num;

    @SerializedName("appointment_content")
    @Expose
    private String appointment_content;

    @SerializedName("consultant_date")
    @Expose
    private DateTime appointment_date;


    public Appointment() {
    }

    public Long getA_personal_num() {
        return a_personal_num;
    }

    public void setA_personal_num(Long a_personal_num) {
        this.a_personal_num = a_personal_num;
    }

    public String getA_name() {
        return a_name;
    }

    public void setA_name(String a_name) {
        this.a_name = a_name;
    }

    public Long getA_consultant_num() {
        return a_consultant_num;
    }

    public void setA_consultant_num(Long a_consultant_num) {
        this.a_consultant_num = a_consultant_num;
    }

    public String getAppointment_content() {
        return appointment_content;
    }

    public void setAppointment_content(String appointment_content) {
        this.appointment_content = appointment_content;
    }

    public DateTime getAppointment_date() {
        return appointment_date;
    }

    public void setAppointment_date(DateTime appointment_date) {
        this.appointment_date = appointment_date;
    }
}