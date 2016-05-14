package com.myplanner.myplanner;

import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import com.myplanner.myplanner.UserData.DataRetriever;
import com.myplanner.myplanner.UserData.PlannerReminder;

import java.util.Calendar;

public class CreateReminder extends AppCompatActivity {
    CalendarView dateSelect;
    TimePicker startTime;
    EditText titleEditTxt;
    EditText bodyEditTxt;

    final int millsPerHour = 3600000;
    final int millsPerMinute = 60000;

    int reminderID;
    int reminderYear;
    int reminderMonth;
    int reminderDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reminder);

        // get the date information
        Bundle passedData = getIntent().getExtras();

        reminderID = passedData.getInt("ID");
        final long dateInMills = passedData.getLong("dateInMills");

        // save the editable elements
        dateSelect = (CalendarView) findViewById(R.id.create_reminder_date_selector);
        startTime = (TimePicker) findViewById(R.id.create_reminder_start_time);
        titleEditTxt = (EditText) findViewById(R.id.create_reminder_title_input);
        bodyEditTxt = (EditText) findViewById(R.id.create_reminder_body_input);

        // set up the toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.create_reminder_toolbar);
        toolbar.setTitle("Create Reminder");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get the initial date
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateInMills);
        reminderYear = cal.get(Calendar.YEAR);
        reminderMonth = cal.get(Calendar.MONTH);
        reminderDate = cal.get(Calendar.DATE);

        // set up the date selector, making it skip to the time select when a date is chosen
        dateSelect.setDate(dateInMills);
        dateSelect.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                final NestedScrollView nestedScrollView = (NestedScrollView) findViewById(R.id.create_reminder_nested_scroll_view);
                final RelativeLayout calendarLayout = (RelativeLayout) findViewById(R.id.create_reminder_calendar_layout);
                final int margin = ((int)getResources().getDimension(R.dimen.activity_vertical_margin)) * 2;
                int offset = margin + calendarLayout.getHeight();
                nestedScrollView.setScrollY(offset);

                reminderYear = year;
                reminderMonth = month;
                reminderDate = dayOfMonth;
            }
        });

        // configure the bottom buttons
        final Button cancelBtn = (Button) findViewById(R.id.create_reminder_cancel_btn);
        final Button saveBtn = (Button) findViewById(R.id.create_reminder_save_btn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertReminder();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_reminder_menu, menu);
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

    public void insertReminder() {
        final int newDayTimeMills = startTime.getCurrentHour() * millsPerHour + startTime.getCurrentMinute() * millsPerMinute;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, reminderYear);
        cal.set(Calendar.MONTH, reminderMonth);
        cal.set(Calendar.DATE, reminderDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);//24 hour time here, so 0 is the first hour of the day
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);

        final long reminderMills = cal.getTimeInMillis() + newDayTimeMills;
        final String title = titleEditTxt.getText().toString();
        final String body = bodyEditTxt.getText().toString();

        final PlannerReminder newReminder = new PlannerReminder(reminderMills, title, body, reminderID);
        DataRetriever.getInstance().addReminder(newReminder);
    }
}
