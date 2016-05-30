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
    private CalendarView dateSelect;
    private TimePicker startTime;
    private EditText titleEditTxt;
    private EditText bodyEditTxt;
    private int reminderID;
    private int reminderYear;
    private int reminderMonth;
    private int reminderDate;

    // ---------------------------------------------------------------------------------------------
    // -------------------------------------- Public Functions -------------------------------------
    // ---------------------------------------------------------------------------------------------

    public void insertReminder() {
        final int MILLS_PER_HOUR = 3600000;
        final int MILLS_PER_MINUTE = 60000;
        final int newDayTimeMills = startTime.getCurrentHour() * MILLS_PER_HOUR + startTime.getCurrentMinute() * MILLS_PER_MINUTE;

        // create a calendar object at the reminder time
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, reminderYear);
        cal.set(Calendar.MONTH, reminderMonth);
        cal.set(Calendar.DATE, reminderDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);//24 hour time here, so 0 is the first hour of the day
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // use the calendar object to get the time in mills
        final long reminderMills = cal.getTimeInMillis() + newDayTimeMills;

        final String title = titleEditTxt.getText().toString();
        final String body = bodyEditTxt.getText().toString();
        final PlannerReminder newReminder = new PlannerReminder(reminderMills, title, body, reminderID);
        DataRetriever.getInstance().addReminder(newReminder);
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------------ Override Functions -------------------------------------
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reminder);

        // get the information passed from the previous activity
        Bundle passedData = getIntent().getExtras();
        reminderID = passedData.getInt("ID");
        final long dateInMills = passedData.getLong("dateInMills");

        // get the initial date
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateInMills);
        reminderYear = cal.get(Calendar.YEAR);
        reminderMonth = cal.get(Calendar.MONTH);
        reminderDate = cal.get(Calendar.DATE);

        // get the editable elements of the activity
        dateSelect = (CalendarView) findViewById(R.id.date_selector);
        startTime = (TimePicker) findViewById(R.id.time_selector);
        titleEditTxt = (EditText) findViewById(R.id.title_edit_text);
        bodyEditTxt = (EditText) findViewById(R.id.body_edit_text);

        // set up the toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Create Reminder");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set up the date selector, making it skip to the time selector when a date is chosen
        dateSelect.setDate(dateInMills);
        dateSelect.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                final NestedScrollView nestedScrollView = (NestedScrollView) findViewById(R.id.scroll_view);
                final RelativeLayout calendarLayout = (RelativeLayout) findViewById(R.id.date_selector_layout);
                final int margin = ((int)getResources().getDimension(R.dimen.activity_vertical_margin)) * 2;
                int offset = margin + calendarLayout.getHeight();
                nestedScrollView.smoothScrollTo(0, offset);

                reminderYear = year;
                reminderMonth = month;
                reminderDate = dayOfMonth;
            }
        });

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
                insertReminder();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.create_reminder_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
