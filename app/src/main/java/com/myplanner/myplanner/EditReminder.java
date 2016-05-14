package com.myplanner.myplanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.myplanner.myplanner.UserData.DataRetriever;
import com.myplanner.myplanner.UserData.PlannerReminder;

import java.util.Calendar;

public class EditReminder extends AppCompatActivity {
    //----------------------------------------------------------------------------------------------
    //------------------------------------------ Constants -----------------------------------------
    //----------------------------------------------------------------------------------------------

    final int millsPerHour = 3600000;
    final int millsPerMinute = 60000;
    final String[] months = {"January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"};

    final DataRetriever userData = DataRetriever.getInstance();

    //----------------------------------------------------------------------------------------------
    //------------------------------------ Class-Wide Variables ------------------------------------
    //----------------------------------------------------------------------------------------------

    long timeMills;
    String title;
    String body;
    int id;

    TimePicker startTime;
    EditText titleEditTxt;
    EditText bodyEditTxt;

    int oldDayTimeMills;

    //----------------------------------------------------------------------------------------------
    //------------------------------------- Override Functions -------------------------------------
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reminder);

        // save the editable elements
        startTime = (TimePicker) findViewById(R.id.edit_reminder_start_time);
        titleEditTxt = (EditText) findViewById(R.id.edit_reminder_title_input);
        bodyEditTxt = (EditText) findViewById(R.id.edit_reminder_body_input);

        // get the id of the reminder being edited
        Bundle passedData = getIntent().getExtras();
        id = passedData.getInt("id");

        // get the information about the reminder being edited from the provided id
        final PlannerReminder oldReminder = userData.getReminderByID(id);
        timeMills = oldReminder.getMills();
        title = oldReminder.getTitle();
        body = oldReminder.getMessage();

        // create a calendar object to more easily deal with dates
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeMills);

        // determine the number of milliseconds that have passed in the day before the reminder
        oldDayTimeMills = cal.get(Calendar.HOUR_OF_DAY) * millsPerHour + cal.get(Calendar.MINUTE) * millsPerMinute;

        // set up the toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.edit_reminder_toolbar);
        toolbar.setTitle("Edit Reminder");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set up the start time selector
        startTime.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
        startTime.setCurrentMinute(cal.get(Calendar.MINUTE));

        // set up the text inputs
        titleEditTxt.setText(title);
        bodyEditTxt.setText(body);

        // set up the text that tells the user what date the reminder starts
        final TextView reminderDate = (TextView) findViewById(R.id.edit_reminder_date_txt);
        final String dateText = months[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.DATE);
        reminderDate.setText(dateText);

        // configure the bottom buttons
        final Button cancelBtn = (Button) findViewById(R.id.edit_reminder_cancel_btn);
        final Button saveBtn = (Button) findViewById(R.id.edit_reminder_save_btn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeOldReminder();
                insertReminder();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_reminder_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.edit_reminder_delete_menu_button:
                removeOldReminder();
                returnToHome();
                break;
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //----------------------------------------------------------------------------------------------
    //-------------------------------------- Local Functions ---------------------------------------
    //----------------------------------------------------------------------------------------------

    // insert an reminder into the userData
    public void insertReminder() {
        final int newDayTimeMills = startTime.getCurrentHour() * millsPerHour + startTime.getCurrentMinute() * millsPerMinute;
        final int dayTimeChangeMills = newDayTimeMills - oldDayTimeMills;

        // take the original reminder start time, and increment it by the difference between the old
        //   start time and the new start time (this method is easier than trying to get the time in
        //   milliseconds from the date and time and stuff)
        timeMills += dayTimeChangeMills;
        title = titleEditTxt.getText().toString();
        body = bodyEditTxt.getText().toString();

        final PlannerReminder newReminder = new PlannerReminder(timeMills, title, body, id);
        userData.addReminder(newReminder);
    }

    // delete the reminder being edited so it can be replaced or perminantely deleted
    public void removeOldReminder() {
        userData.removeReminder(id);
    }

    // go back to the home screen
    public void returnToHome() {
        Intent intentBundle = new Intent(EditReminder.this, Main.class);
        startActivity(intentBundle);
    }
}
