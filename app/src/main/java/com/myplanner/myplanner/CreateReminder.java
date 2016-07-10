package com.myplanner.myplanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import com.myplanner.myplanner.UserData.DataRetriever;
import com.myplanner.myplanner.UserData.PlannerReminder;

import java.util.Calendar;

public class CreateReminder extends AppCompatActivity {
    private DatePicker dateSelect;
    private TimePicker startTime;
    private EditText titleEditTxt;
    private EditText bodyEditTxt;
    private int reminderID;

    // ---------------------------------------------------------------------------------------------
    // -------------------------------------- Public Functions -------------------------------------
    // ---------------------------------------------------------------------------------------------

    public void insertReminder() {
        final int MILLS_PER_HOUR = 3600000;
        final int MILLS_PER_MINUTE = 60000;
        final int newDayTimeMills = startTime.getCurrentHour() * MILLS_PER_HOUR + startTime.getCurrentMinute() * MILLS_PER_MINUTE;

        // create a calendar object at the reminder time
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, dateSelect.getYear());
        cal.set(Calendar.MONTH, dateSelect.getMonth());
        cal.set(Calendar.DATE, dateSelect.getDayOfMonth());
        cal.set(Calendar.HOUR_OF_DAY, 0);//24 hour time here, so 0 is the first hour of the day
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // use the calendar object to get the time in mills
        final long reminderMills = cal.getTimeInMillis() + newDayTimeMills;

        final String title = titleEditTxt.getText().toString();
        final String body = bodyEditTxt.getText().toString();
        final PlannerReminder newReminder = new PlannerReminder(reminderMills, title, body, reminderID);
        DataRetriever.getInstance().addReminder(newReminder);

        // create the notification
        final AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        final Intent actionIntent = new Intent(this, Main.class);
        final PendingIntent actionPendingIntent = PendingIntent.getActivity(this, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        actionIntent.putExtra(Main.TAB_TAG, 2);
        NotificationCreator.addNotification(alarmManager, reminderID, title, body, R.drawable.ic_event_note_black_48dp, reminderMills, getApplicationContext(), actionPendingIntent);
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
        reminderID = passedData.getInt(Main.ID_TAG);
        final long dateInMills = passedData.getLong(Main.DATE_IN_MILLS_TAG);

        // get the editable elements of the activity
        dateSelect = (DatePicker) findViewById(R.id.date_selector);
        startTime = (TimePicker) findViewById(R.id.time_selector);
        titleEditTxt = (EditText) findViewById(R.id.title_edit_text);
        bodyEditTxt = (EditText) findViewById(R.id.body_edit_text);

        // set up the toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.create_reminder_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get the initial date
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateInMills);
        final int startYear = cal.get(Calendar.YEAR);
        final int startMonth = cal.get(Calendar.MONTH);
        final int startDate = cal.get(Calendar.DATE);

        // set up the date selector, making it skip to the time selector when a date is chosen
        dateSelect.init(startYear, startMonth, startDate, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                final NestedScrollView nestedScrollView = (NestedScrollView) findViewById(R.id.scroll_view);
                final RelativeLayout calendarLayout = (RelativeLayout) findViewById(R.id.date_selector_layout);
                final int margin = (int) getResources().getDimension(R.dimen.activity_vertical_margin);
                int offset = margin + calendarLayout.getHeight();
                nestedScrollView.smoothScrollTo(0, offset);
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
