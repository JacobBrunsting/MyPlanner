package com.myplanner.myplanner.UserData;

import android.util.Log;

import java.util.Calendar;

public class PlannerEvent {
    Calendar startCal = Calendar.getInstance();
    Calendar endCal = Calendar.getInstance();
    String title;
    String message;
    int id;

    public PlannerEvent (long startTimeInMills, long endTimeInMills, String ntitle, String nmessage, int nID) {
        startCal.setTimeInMillis(startTimeInMills);
        endCal.setTimeInMillis(endTimeInMills);
        title = ntitle;
        message = nmessage;
        id = nID;
    }

    //----------------------------------------------------------------------------------------------
    //------------------------------------------ Getters -------------------------------------------
    // ---------------------------------------------------------------------------------------------

    public int getStartAMOrPM() {return startCal.get(Calendar.AM_PM);}

    public int getStartMinute() {return startCal.get(Calendar.MINUTE);}

    public int getStartHour() {return startCal.get(Calendar.HOUR_OF_DAY);}

    public int getStartDate() {return startCal.get(Calendar.DAY_OF_MONTH);}

    public int getStartMonth() {return startCal.get(Calendar.MONTH);}

    public int getStartYear() {return startCal.get(Calendar.YEAR);}

    public long getStartMills() {return startCal.getTimeInMillis();}

    public int getEndAMOrPM() {return endCal.get(Calendar.AM_PM);}

    public int getEndMinute() {return endCal.get(Calendar.MINUTE);}

    public int getEndHour() {return endCal.get(Calendar.HOUR_OF_DAY);}

    public int getEndDate() {return endCal.get(Calendar.DAY_OF_MONTH);}

    public int getEndMonth() {return endCal.get(Calendar.MONTH);}

    public int getEndYear() {return endCal.get(Calendar.YEAR);}

    public long getEndMills() {return endCal.getTimeInMillis();}

    public String getTitle() {return title;}

    public String getMessage() {return message;}

    public int getID() {return id;}

    //----------------------------------------------------------------------------------------------
    //------------------------------------------ Setters -------------------------------------------
    // ---------------------------------------------------------------------------------------------

    public void setStartTime(long timeInMills) {
        startCal.setTimeInMillis(timeInMills);
        if (timeInMills > endCal.getTimeInMillis()) {
            endCal = startCal;
        }
    }

    public void setEndTime(long timeInMills) {
        if (timeInMills >= startCal.getTimeInMillis()) {
            endCal.setTimeInMillis(timeInMills);
        }
    }

    public void setTitle(String ntitle) {title = ntitle;}

    public void setMessage(String nmessage) {message = nmessage;}
}
