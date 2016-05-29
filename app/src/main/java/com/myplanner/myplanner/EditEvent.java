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
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.myplanner.myplanner.UserData.DataRetriever;
import com.myplanner.myplanner.UserData.PlannerEvent;

import java.util.Calendar;

public class EditEvent extends AppCompatActivity {
    private final int millsPerHour = 3600000;
    private final int millsPerMinute = 60000;
    private final String[] months = {"January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"};

    private final DataRetriever userData = DataRetriever.getInstance();
    private long startMills;
    private long endMills;
    private String title;
    private String body;
    private int id;
    private TimePicker startTime;
    private NumberPicker durationHours;
    private NumberPicker durationMinutes;
    private EditText titleEditTxt;
    private EditText bodyEditTxt;
    private Switch eventTimedSwitch;
    private int oldDayTimeMills;

    //----------------------------------------------------------------------------------------------
    //------------------------------------- Override Functions -------------------------------------
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // get the elements of the activity
        startTime = (TimePicker) findViewById(R.id.edit_event_start_time);
        durationHours = (NumberPicker) findViewById(R.id.edit_event_duration_hours);
        durationMinutes = (NumberPicker) findViewById(R.id.edit_event_duration_minutes);
        titleEditTxt = (EditText) findViewById(R.id.edit_event_title_input);
        bodyEditTxt = (EditText) findViewById(R.id.edit_event_body_input);
        eventTimedSwitch = (Switch) findViewById(R.id.edit_event_timed_switch);
        final RelativeLayout durationLayout = (RelativeLayout) findViewById(R.id.edit_event_duration_layout);

        // get the id of the event being edited from the previous activity
        Bundle passedData = getIntent().getExtras();
        id = passedData.getInt("id");

        // get the information about the event being edited from the provided id
        final PlannerEvent oldEvent = userData.getEventByID(id);
        startMills = oldEvent.getStartMills();
        endMills = oldEvent.getEndMills();
        title = oldEvent.getTitle();
        body = oldEvent.getMessage();

        // create two calendars at the start and end time to more easily deal with dates
        final Calendar startCal = Calendar.getInstance();
        final Calendar endCal = Calendar.getInstance();
        startCal.setTimeInMillis(startMills);
        endCal.setTimeInMillis(endMills);

        // determine the number of milliseconds that have passed in the day before the event starts
        oldDayTimeMills = startCal.get(Calendar.HOUR_OF_DAY) * millsPerHour + startCal.get(Calendar.MINUTE) * millsPerMinute;

        // set up the toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.edit_event_toolbar);
        toolbar.setTitle("Edit Event");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set up the start time selector
        startTime.setCurrentHour(startCal.get(Calendar.HOUR_OF_DAY));
        startTime.setCurrentMinute(startCal.get(Calendar.MINUTE));

        // make the eventTimedSwitch hide/show the duration selector
        eventTimedSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventTimedSwitch.isChecked()) {
                    durationLayout.setEnabled(true);
                    durationLayout.setVisibility(View.VISIBLE);
                } else {
                    durationLayout.setEnabled(false);
                    durationLayout.setVisibility(View.INVISIBLE);
                }
            }
        });

        // set up the event duration selector, hide it if the duration is zero, meaning the event
        //   does not have a length, and finish setting up the event timed switch
        // everything must be flipped to match the inverted values (which is really
        //   confusing and messy), since we set custom display values to flip the direction
        final int minute59 = 0;
        final int minute0 = 59;
        final int minuteIncrement = -1;
        final int hourMax = 0;
        final int hourMin = 99;
        final int hourIncrement = -1;
        final int hourDecrement = 1;

        // determine the original duration of the event
        int differenceHours = (int) ((endMills - startMills) / millsPerHour);
        int differenceMinutes = (int) ((endMills - startMills - (differenceHours * millsPerHour)) / millsPerMinute);

        if (differenceHours > 99) {
            differenceHours = 99;
        }

        // configure the duration selectors to count in the opposite direction by creating custom
        //   display values that are the inverse of the regular ones
        String[] validHourValues = new String[100];
        String[] validMinuteValues = new String[60];
        for (int i = 99; i >= 10; --i) {
            validHourValues[99 - i] = i + "";
        }
        for (int i = 9; i >= 0; --i) {
            validHourValues[99 - i] = "0" + i;
        }
        for (int i = 59; i >= 10; --i) {
            validMinuteValues[59 - i] = i + "";
        }
        for (int i = 9; i >= 0; --i) {
            validMinuteValues[59 - i] = "0" + i;
        }

        // set the duration selectors to follow the preferences we have set
        durationHours.setMaxValue(99);
        durationMinutes.setMaxValue(59);
        durationHours.setValue(hourMin + differenceHours * hourIncrement);
        durationMinutes.setValue(minute0 + differenceMinutes * minuteIncrement);
        durationHours.setDisplayedValues(validHourValues);
        durationMinutes.setDisplayedValues(validMinuteValues);

        // make the hour count increase/decrease when the minute selector loops completely around
        durationMinutes.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            if (newVal == minute0 && oldVal == minute59) {
                if (durationHours.getValue() != hourMax) {
                    durationHours.setValue(durationHours.getValue() + hourIncrement);
                }
            } else if (newVal == minute59 && oldVal == minute0) {
                if (durationHours.getValue() != hourMin) {
                    durationHours.setValue(durationHours.getValue() + hourDecrement);
                }
            }
            }
        });

        // show the option to change the duration of the event only if the event being edited had
        //   some duration
        if (startMills == endMills) {
            durationLayout.setEnabled(false);
            durationLayout.setVisibility(View.GONE);
            eventTimedSwitch.setChecked(false);
        } else {
            durationLayout.setEnabled(true);
            durationLayout.setVisibility(View.VISIBLE);
            eventTimedSwitch.setChecked(true);
        }

        // set up the text inputs
        titleEditTxt.setText(title);
        bodyEditTxt.setText(body);

        // set up the text that tells the user what date the event starts
        final TextView eventDate = (TextView) findViewById(R.id.edit_event_date_txt);
        final String dateText = months[startCal.get(Calendar.MONTH)] + " " + startCal.get(Calendar.DATE);
        eventDate.setText(dateText);

        // configure the bottom bar buttons
        final Button cancelBtn = (Button) findViewById(R.id.edit_event_cancel_btn);
        final Button saveBtn = (Button) findViewById(R.id.edit_event_save_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeOldEvent();
                insertEvent();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.edit_event_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.edit_event_delete_menu_button:
                removeOldEvent();
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

    // insert an event into the userData
    private void insertEvent() {
        final int newDayTimeMills = startTime.getCurrentHour() * millsPerHour + startTime.getCurrentMinute() * millsPerMinute;
        final int dayTimeChangeMills = newDayTimeMills - oldDayTimeMills;
        int eventDurationMills = (99 - durationHours.getValue()) * millsPerHour + (59 - durationMinutes.getValue()) * millsPerMinute;

        if (!eventTimedSwitch.isChecked()) {
            eventDurationMills = 0;
        }

        // take the original event start time, and increment it by the difference between the old
        //   start time and the new start time (this method is easier than trying to get the time in
        //   milliseconds from the date and time and stuff)
        startMills += dayTimeChangeMills;
        endMills = startMills + eventDurationMills;
        title = titleEditTxt.getText().toString();
        body = bodyEditTxt.getText().toString();

        final PlannerEvent newEvent = new PlannerEvent(startMills, endMills, title, body, id);
        userData.addEvent(newEvent);
    }

    // delete the event being edited
    private void removeOldEvent() {
        userData.removeEvent(id);
    }

    // go back to the home screen
    private void returnToHome() {
        final Intent intentBundle = new Intent(EditEvent.this, Main.class);
        startActivity(intentBundle);
    }
}
