package com.bae.dialogflowbot.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.util.List;

public class Consultant {

    @SerializedName("c_consultant_num")
    @Expose
    private Long c_consultant_num;

    @SerializedName("c_name")
    @Expose
    private String c_name;

    @SerializedName("c_personal_num")
    @Expose
    private Long c_personal_num;


    @SerializedName("consultant_content")
    @Expose
    private List<String> consultant_content;

    @SerializedName("consultant_type")
    @Expose
    private Integer consultant_type;

    @SerializedName("consultant_date")
    @Expose
    private LocalDate consultant_date;

    @SerializedName("negative")
    @Expose
    private String negative;

    public Consultant(Long c_consultant_num,String c_name, Long c_personal_num, List<String> consultant_content, Integer consultant_type, LocalDate consultant_date, String negative) {
        this.c_consultant_num = c_consultant_num;
        this.c_personal_num = c_personal_num;
        this.c_name = c_name;
        this.consultant_content = consultant_content;
        this.consultant_type = consultant_type;
        this.consultant_date = consultant_date;
        this.negative = negative;
    }

    public Consultant(String c_name, Long c_personal_num,List<String> consultant_content,String negative) {
        this.c_consultant_num = c_consultant_num;
        this.c_name = c_name;
        this.c_personal_num = c_personal_num;
        this.consultant_content = consultant_content;
        this.negative = negative;
        //this.consultant_type = consultant_type;
        // this.consultant_date = consultant_date;

    }

    public Long getC_consultant_num() {
        return c_consultant_num;
    }

    public void setC_consultant_num(Long c_consultant_num) {
        this.c_consultant_num = c_consultant_num;
    }

    public Long getC_personal_num() {
        return c_personal_num;
    }

    public void setC_personal_num(Long c_personal_num) {
        this.c_personal_num = c_personal_num;
    }

    public String getC_name() {
        return c_name;
    }

    public void setC_name(String c_name) {
        this.c_name = c_name;
    }

    public List<String> getConsultant_content() {
        return consultant_content;
    }

    public void setConsultant_content(List<String> consultant_content) {
        this.consultant_content = consultant_content;
    }

    public Integer getConsultant_type() {
        return consultant_type;
    }

    public void setConsultant_type(Integer consultant_type) {
        this.consultant_type = consultant_type;
    }

    public LocalDate getConsultant_date() {
        return consultant_date;
    }

    public void setConsultant_date(LocalDate consultant_date) {
        this.consultant_date = consultant_date;
    }

    public String getNegative() {
        return negative;
    }

    public void setNegative(String negative) {
        this.negative = negative;
    }
}
