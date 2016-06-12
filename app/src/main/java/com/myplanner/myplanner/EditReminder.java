package com.myplanner.myplanner;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
    private final int MILLS_PER_HOUR = 3600000;
    private final int MILLS_PER_MINUTE = 60000;
    private final String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"};

    private final DataRetriever userData = DataRetriever.getInstance();
    private long timeMills;
    private String title;
    private String body;
    private int id;
    private TimePicker startTime;
    private EditText titleEditTxt;
    private EditText bodyEditTxt;
    private int oldDayTimeMills;

    //----------------------------------------------------------------------------------------------
    //------------------------------------- Override Functions -------------------------------------
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reminder);

        // save the editable elements
        startTime = (TimePicker) findViewById(R.id.start_time_picker);
        titleEditTxt = (EditText) findViewById(R.id.title_edit_text);
        bodyEditTxt = (EditText) findViewById(R.id.body_edit_text);

        // get the id of the reminder being edited
        Bundle passedData = getIntent().getExtras();
        id = passedData.getInt(Main.ID_TAG);

        // get the information about the reminder being edited from the provided id
        final PlannerReminder oldReminder = userData.getReminderByID(id);
        timeMills = oldReminder.getMills();
        title = oldReminder.getTitle();
        body = oldReminder.getMessage();

        // create a calendar object to more easily deal with dates
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeMills);

        // determine the number of milliseconds that have passed in the day before the reminder
        oldDayTimeMills = cal.get(Calendar.HOUR_OF_DAY) * MILLS_PER_HOUR + cal.get(Calendar.MINUTE) * MILLS_PER_MINUTE;

        // set up the toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        final TextView reminderDate = (TextView) findViewById(R.id.date_title_text);
        final String dateText = MONTHS[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.DATE);
        reminderDate.setText(dateText);

        // configure the bottom bar buttons
        final Button cancelBtn = (Button) findViewById(R.id.cancel_btn);
        final Button saveBtn = (Button) findViewById(R.id.save_btn);
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
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.edit_reminder_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.delete_button:
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
    //------------------------------------- Private Functions --------------------------------------
    //----------------------------------------------------------------------------------------------

    // insert an reminder into the userData
    private void insertReminder() {
        final int newDayTimeMills = startTime.getCurrentHour() * MILLS_PER_HOUR + startTime.getCurrentMinute() * MILLS_PER_MINUTE;
        final int dayTimeChangeMills = newDayTimeMills - oldDayTimeMills;

        // take the original reminder start time, and increment it by the difference between the old
        //   start time and the new start time (this method is easier than trying to get the time in
        //   milliseconds from the date and time and stuff)
        timeMills += dayTimeChangeMills;
        title = titleEditTxt.getText().toString();
        body = bodyEditTxt.getText().toString();

        final PlannerReminder newReminder = new PlannerReminder(timeMills, title, body, id);
        userData.addReminder(newReminder);

        // update the notification (the ID of each notification is the reminder ID)
        final AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        final Intent actionIntent = new Intent(this, Main.class);
        final PendingIntent actionPendingIntent = PendingIntent.getActivity(this, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        actionIntent.putExtra(Main.TAB_TAG, 2);
        NotificationCreator.addNotification(alarmManager, id, title, body, R.drawable.ic_add_black_24dp, timeMills, getApplicationContext(), actionPendingIntent);


    }

    // delete the reminder being edited
    private void removeOldReminder() {
        userData.removeReminder(id);
    }

    // go back to the home screen
    private void returnToHome() {
        Intent intentBundle = new Intent(EditReminder.this, Main.class);
        startActivity(intentBundle);
    }
}
