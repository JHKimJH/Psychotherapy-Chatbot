package com.bae.dialogflowbot.models;

public class DataManager {
    private User user;
    private Consultant consultant;
    private Appointment appointment;
    private DataManager(){

    }
    private static class Sys{
    public static final DataManager INSTANCE = new DataManager();
    }
    public static DataManager getInstance(){return Sys.INSTANCE;}

    public User getUser(){return user;}
    public void setUser(User user){this.user = user;}

    public Consultant getConsultant(){return consultant;}
    public void setConsultant(Consultant consultant){this.consultant=consultant;}

    public Appointment getAppointment(){return appointment;}
    public void setAppointment(Appointment appointment){this.appointment=appointment;}
}