package com.myplanner.myplanner;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
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
    private NestedScrollView scrollView;
    private CalendarView dateSelect;
    private TimePicker startTime;
    private NumberPicker durationHours;
    private NumberPicker durationMinutes;
    private EditText titleEditTxt;
    private EditText bodyEditTxt;
    private Switch eventTimedSwitch;

    private final int millsPerHour = 3600000;
    private final int millsPerMinute = 60000;

    private int eventID;
    private int startYear;
    private int startMonth;
    private int startDate;
    private float durationContainerHeight = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // get the date information
        Bundle passedData = getIntent().getExtras();

        eventID = passedData.getInt("ID");
        final long dateInMills = passedData.getLong("dateInMills");

        // save the editable elements
        scrollView = (NestedScrollView) findViewById(R.id.scroll_view);
        dateSelect = (CalendarView) findViewById(R.id.date_selector);
        startTime = (TimePicker) findViewById(R.id.time_selector);
        durationHours = (NumberPicker) findViewById(R.id.duration_hour_selector);
        durationMinutes = (NumberPicker) findViewById(R.id.duration_minute_selector);
        titleEditTxt = (EditText) findViewById(R.id.title_edit_text);
        bodyEditTxt = (EditText) findViewById(R.id.body_edit_text);
        eventTimedSwitch = (Switch) findViewById(R.id.timed_switch);
        final RelativeLayout durationLayout = (RelativeLayout) findViewById(R.id.duration_layout);

        // set up the toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
                final RelativeLayout calendarLayout = (RelativeLayout) findViewById(R.id.date_selector_layout);
                final int margin = ((int)getResources().getDimension(R.dimen.activity_vertical_margin)) * 2;
                int offset = margin + calendarLayout.getHeight();
                scrollView.smoothScrollTo(0, offset);

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
                    showDurationLayout(durationLayout);
                } else {
                    hideDurationLayout(durationLayout);
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


        // configure the bottom buttons
        final Button cancelBtn = (Button) findViewById(R.id.cancel_button);
        final Button saveBtn = (Button) findViewById(R.id.save_button);

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

        // get the height of the duration layout, and the collapse it (TODO: make this less ugly)
        final ViewTreeObserver onViewCreatedObserver = durationLayout.getViewTreeObserver();
        if(onViewCreatedObserver.isAlive()) {
            onViewCreatedObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (durationContainerHeight == -1) {
                        durationContainerHeight = durationLayout.getHeight();
                        hideDurationLayout(durationLayout);
                        eventTimedSwitch.setChecked(false);
                    }
                    if (onViewCreatedObserver.isAlive()) {
                        onViewCreatedObserver.removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
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

    private void hideDurationLayout(final RelativeLayout durationLayout) {
        final Animation fadeAnimation = CustomAnimation.fadeView(0, 200, durationLayout);
        fadeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                durationLayout.setVisibility(View.INVISIBLE);
                final float shiftAmount = -durationContainerHeight;
                final Animation shiftAnimation = CustomAnimation.adjustHeight(shiftAmount, 500, durationLayout);
                durationLayout.startAnimation(shiftAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        durationLayout.startAnimation(fadeAnimation);
    }

    private void showDurationLayout(final RelativeLayout durationLayout) {
        if (durationContainerHeight == -1) {
            durationContainerHeight = durationLayout.getHeight();
        }
        final float shiftAmount = durationContainerHeight;
        final Animation shiftAnimation = CustomAnimation.adjustHeight(shiftAmount, 500, durationLayout);
        shiftAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                durationLayout.setVisibility(View.VISIBLE);
                scrollView.smoothScrollTo(0, (int) durationLayout.getY() + durationLayout.getHeight());
                final Animation fadeAnimation = CustomAnimation.fadeView(1, 200, durationLayout);
                durationLayout.startAnimation(fadeAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        durationLayout.startAnimation(shiftAnimation);
    }
}
