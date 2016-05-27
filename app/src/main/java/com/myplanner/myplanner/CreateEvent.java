package com.myplanner.myplanner;

import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TimePicker;

import com.myplanner.myplanner.UserData.DataRetriever;
import com.myplanner.myplanner.UserData.PlannerEvent;

import java.util.Calendar;

public class CreateEvent extends AppCompatActivity {
    CalendarView dateSelect;
    TimePicker startTime;
    NumberPicker durationHours;
    NumberPicker durationMinutes;
    EditText titleEditTxt;
    EditText bodyEditTxt;
    Switch eventTimedSwitch;

    final int millsPerHour = 3600000;
    final int millsPerMinute = 60000;

    int eventID;
    int startYear;
    int startMonth;
    int startDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // get the date information
        Bundle passedData = getIntent().getExtras();

        eventID = passedData.getInt("ID");
        final long dateInMills = passedData.getLong("dateInMills");

        // save the editable elements
        dateSelect = (CalendarView) findViewById(R.id.create_event_date_selector);
        startTime = (TimePicker) findViewById(R.id.create_event_start_time);
        durationHours = (NumberPicker) findViewById(R.id.create_event_duration_hours);
        durationMinutes = (NumberPicker) findViewById(R.id.create_event_duration_minutes);
        titleEditTxt = (EditText) findViewById(R.id.create_event_title_input);
        bodyEditTxt = (EditText) findViewById(R.id.create_event_body_input);
        eventTimedSwitch = (Switch) findViewById(R.id.create_event_timed_switch);
        final RelativeLayout durationLayout = (RelativeLayout) findViewById(R.id.create_event_duration_layout);

        // set up the toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.create_event_toolbar);
        toolbar.setTitle("Create Event");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get the initial date
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateInMills);
        startYear = cal.get(Calendar.YEAR);
        startMonth = cal.get(Calendar.MONTH);
        startDate = cal.get(Calendar.DATE);

        // set up the date selector, making it skip to the time select when a date is chosen
        dateSelect.setDate(dateInMills);
        dateSelect.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                final NestedScrollView nestedScrollView = (NestedScrollView) findViewById(R.id.create_event_nested_scroll_view);
                final RelativeLayout calendarLayout = (RelativeLayout) findViewById(R.id.create_event_calendar_layout);
                final int margin = ((int)getResources().getDimension(R.dimen.activity_vertical_margin)) * 2;
                int offset = margin + calendarLayout.getHeight();
                nestedScrollView.smoothScrollTo(0, offset);

                startYear = year;
                startMonth = month;
                startDate = dayOfMonth;
            }
        });

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

        // set up the event duration selector with the numbers counting in the opposite direction
        // everything must be flipped to match the inverted values (which is really
        //   confusing and messy), since we set custom display values to flip the direction
        final int minute59 = 0;
        final int minute0 = 59;
        final int minuteIncrement = -1;
        final int hourMax = 0;
        final int hourMin = 99;
        final int hourIncrement = -1;
        final int hourDecrement = 1;

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
        durationHours.setDisplayedValues(validHourValues);
        durationMinutes.setDisplayedValues(validMinuteValues);
        durationHours.setMaxValue(99);
        durationMinutes.setMaxValue(59);
        durationHours.setValue(hourMin);
        durationMinutes.setValue(minute0);

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

        // make the event default to being untimed
        durationLayout.setEnabled(false);
        durationLayout.setVisibility(View.INVISIBLE);
        eventTimedSwitch.setChecked(false);

        // configure the bottom buttons
        final Button cancelBtn = (Button) findViewById(R.id.create_event_cancel_btn);
        final Button saveBtn = (Button) findViewById(R.id.create_event_save_btn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertEvent();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_event_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void insertEvent() {
        final int newDayTimeMills = startTime.getCurrentHour() * millsPerHour + startTime.getCurrentMinute() * millsPerMinute;
        int eventDurationMills = (99 - durationHours.getValue()) * millsPerHour + (59 - durationMinutes.getValue()) * millsPerMinute;

        if (!eventTimedSwitch.isChecked()) {
            eventDurationMills = 0;
        }

        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, startYear);
        cal.set(Calendar.MONTH, startMonth);
        cal.set(Calendar.DATE, startDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);//24 hour time here, so 0 is the first hour of the day
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);

        final long startMills = cal.getTimeInMillis() + newDayTimeMills;
        final long endMills = startMills + eventDurationMills;
        final String title = titleEditTxt.getText().toString();
        final String body = bodyEditTxt.getText().toString();

        final PlannerEvent newEvent = new PlannerEvent(startMills, endMills, title, body, eventID);
        DataRetriever.getInstance().addEvent(newEvent);
    }
}
