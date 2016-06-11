package com.myplanner.myplanner.UserData;

import java.io.Serializable;
import java.util.Calendar;

public class PlannerReminder {
    private Calendar cal = Calendar.getInstance();
    private String title;
    private String message;
    private int id;

    public PlannerReminder (long timeInMills, String ntitle, String nmessage, int nID) {
        cal.setTimeInMillis(timeInMills);
        title = ntitle;
        message = nmessage;
        id = nID;
    }

    //----------------------------------------------------------------------------------------------
    //------------------------------------------ Getters -------------------------------------------
    // ---------------------------------------------------------------------------------------------

    public int getMinute() {return cal.get(Calendar.MINUTE);}

    public int getHour() {return cal.get(Calendar.HOUR_OF_DAY);}

    public int getDate() {return cal.get(Calendar.DAY_OF_MONTH);}

    public int getMonth() {return cal.get(Calendar.MONTH);}

    public int getYear() {return cal.get(Calendar.YEAR);}

    public long getMills() {return cal.getTimeInMillis();}

    public String getTitle() {return title;}

    public String getMessage() {return message;}

    public int getID() {return id;}

    //----------------------------------------------------------------------------------------------
    //------------------------------------------ Setters -------------------------------------------
    // ---------------------------------------------------------------------------------------------

    public void setTime(long timeInMills) {cal.setTimeInMillis(timeInMills);}

    public void setTitle(String ntitle) {title = ntitle;}

    public void setMessage(String nmessage) {message = nmessage;}
}
