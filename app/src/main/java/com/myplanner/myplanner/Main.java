package com.myplanner.myplanner;

import android.app.DialogFragment;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.myplanner.myplanner.MainScreenFragments.CalendarDialogFragment;
import com.myplanner.myplanner.MainScreenFragments.Events;
import com.myplanner.myplanner.MainScreenFragments.Notes;
import com.myplanner.myplanner.MainScreenFragments.Reminders;
import com.myplanner.myplanner.UserData.DataRetriever;
import com.myplanner.myplanner.UserData.PlannerEvent;
import com.myplanner.myplanner.UserData.PlannerNote;
import com.myplanner.myplanner.UserData.PlannerReminder;

import java.util.ArrayList;
import java.util.Calendar;

public class Main extends AppCompatActivity implements Events.EventInterface,
        Reminders.ReminderInterface, Notes.NotesInterface, CalendarDialogFragment.CalendarInterface {
    //----------------------------------------------------------------------------------------------
    //------------------------------------------ Constants -----------------------------------------
    //----------------------------------------------------------------------------------------------

    final int NUM_FRAGMENTS = 3;
    final String[] titles = {"Events", "Notes", "Reminders"};
    final Events eventsFragment = new Events();
    final Notes notesFragment = new Notes();
    final Reminders remindersFragment = new Reminders();

    final Calendar cal = Calendar.getInstance();

    final String[] months = {"January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"};

    //----------------------------------------------------------------------------------------------
    //---------------------------- Variables Reset on Activity Refresh -----------------------------
    //----------------------------------------------------------------------------------------------

    int curDay = cal.get(Calendar.DAY_OF_WEEK);
    int curDate = cal.get(Calendar.DATE);
    int curMonth = cal.get(Calendar.MONTH);
    int curYear = cal.get(Calendar.YEAR);

    TabLayout tabs;
    Toolbar toolbar;
    ViewPager viewPager;
    DataRetriever userData;

    //----------------------------------------------------------------------------------------------
    //-------------------------- Variables Preserved on Activity Refresh ---------------------------
    //----------------------------------------------------------------------------------------------

    int nextEventID = 0;
    int nextNoteID = 0;
    int nextReminderID = 0;

    // ---------------------------------------------------------------------------------------------
    // ------------------------------------ Override Functions -------------------------------------
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up the toolbar
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbarClicked();
            }
        });

        // set up the ViewPager that contains the fragments
        viewPager = (ViewPager) findViewById(R.id.main_viewpager);
        viewPager.setAdapter(new LocalPageAdapter(getSupportFragmentManager()));

        // set up the tabs
        tabs = (TabLayout) findViewById(R.id.main_tabs);
        tabs.setupWithViewPager(viewPager);

        // configure the fab
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.main_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FABOnClicked();
            }
        });
    }

    // set the toolbar to follow the main_menu layout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // add the listener here to avoid null pointer issues with toolbar
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                Log.i("Main", "setting title to position " + position);
                toolbar.setTitle(generateTitle(position));
                if (position == 0) {
                    toolbar.getMenu().findItem(R.id.action_forward).setVisible(true);
                    toolbar.getMenu().findItem(R.id.action_back).setVisible(true);
                } else {
                    toolbar.getMenu().findItem(R.id.action_forward).setVisible(false);
                    toolbar.getMenu().findItem(R.id.action_back).setVisible(false);
                }
                userData.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        int position = userData.getCurrentTab();

        viewPager.setCurrentItem(position);
        toolbar.setTitle(generateTitle(position));
        if (position == 0) {
            toolbar.getMenu().findItem(R.id.action_forward).setVisible(true);
            toolbar.getMenu().findItem(R.id.action_back).setVisible(true);
        } else {
            toolbar.getMenu().findItem(R.id.action_forward).setVisible(false);
            toolbar.getMenu().findItem(R.id.action_back).setVisible(false);
        }
        userData.setCurrentTab(position);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // retrieve the user data from the singleton class DataRetriever
        userData = DataRetriever.getInstance();

        reloadData();
    }

    // override for when a toolbar button is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_forward:
                nextDate();
                return true;
            case R.id.action_back:
                previousDate();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // save the activity information when it is closed
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt("nextEventID", nextEventID);
        savedInstanceState.putInt("nextNoteID", nextNoteID);
        savedInstanceState.putInt("nextReminderID", nextReminderID);
    }

    // retrieve the saved data from when the activity was closed
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        nextEventID = savedInstanceState.getInt("nextEventID");
        nextNoteID = savedInstanceState.getInt("nextNoteID");
        nextReminderID = savedInstanceState.getInt("nextReminderID");
    }

    // ---------------------------------------------------------------------------------------------
    // --------------------------------- User Interface Generation ---------------------------------
    // ---------------------------------------------------------------------------------------------

    // regenerate the lists that store the current data being shown to the user
    private void refreshData() {
        generateEventScreen();
        generateNotesScreen();
        generateRemindersScreen();

        if (toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        }

        toolbar.setTitle(generateTitle(viewPager.getCurrentItem()));
        Log.i("Main", "Refreshed the data");
    }

    // refresh the data, and then reload the fragments to show the changes
    private void reloadData() {
        Log.i("Main", "Reloading the data");
        refreshData();

        eventsFragment.reloadData();
        notesFragment.reloadData();
        remindersFragment.reloadData();
        Log.i("Main", "Done reloading");
    }

    // generate the lists that store the data being shown to the user on the event screen
    private void generateEventScreen() {
        // retrieve all events happening on the current date
        ArrayList<PlannerEvent> targetEvents = userData.getDateEvents(curYear, curMonth, curDate);

        eventsFragment.clearEventArrays();

        // loop through all events in the day, and add them into the lists that hold the data
        //   displayed to the user
        for (int i = 0; i < targetEvents.size(); ++i) {
            final int startHour = targetEvents.get(i).getStartHour();
            final int endHour = targetEvents.get(i).getEndHour();
            final int startMinute = targetEvents.get(i).getStartMinute();
            final int endMinute = targetEvents.get(i).getEndMinute();

            String timeString;

            // convert from 24 hour time to 12 hour time, and attach the hour the event starts to
            //   the time string
            if (startHour > 12) {
                timeString = (startHour - 12) + ":";
            } else if (startHour == 0) {
                timeString = "12:";
            } else {
                timeString = startHour + ":";
            }

            // attach the minute the event starts to the time string, ensuring there are enough 0's
            if (startMinute > 9) {
                timeString += startMinute;
            } else if (startMinute > 0) {
                timeString += "0" + startMinute;
            } else {
                timeString += "00";
            }

            // attach the AM/PM suffix for the start time to the time string
            if (startHour >= 12) {
                timeString += " PM";
            } else {
                timeString += " AM";
            }

            if (endHour != startHour || endMinute != startMinute) {
                // add in the dash between the start time and end time
                timeString += " - ";

                // attach the hour the event ends to the time string
                if (endHour > 12) {
                    timeString += (endHour - 12) + ":";
                } else if (endHour == 0) {
                    timeString += "12:";
                } else {
                    timeString += endHour + ":";
                }

                // attach the minute the event ends to the time string
                if (endMinute > 9) {
                    timeString += endMinute;
                } else if (endMinute > 0) {
                    timeString += "0" + endMinute;
                } else {
                    timeString += "00";
                }

                // attach the AM/PM suffix for the end time to the time string
                if (endHour >= 12) {
                    timeString += " PM";
                } else {
                    timeString += " AM";
                }
            }

            eventsFragment.addEventInfo(targetEvents.get(i).getTitle(), timeString, targetEvents.get(i).getMessage(), targetEvents.get(i).getID());
        }
    }

    // generate the lists that store the data being shown to users on the notes screen
    private void generateNotesScreen() {
        notesFragment.clearNoteArrays();

        for (int i = 0; i < userData.getNumNotes(); ++i) {
            PlannerNote note = userData.getNote(i);

            final String title = note.getTitle();
            final ArrayList<String> tags = new ArrayList<>();
            final String body = note.getBody();
            final int id = note.getID();
            for (int t = 0; t < note.getNumTags(); ++t) {
                final String newTag = note.getTag(t);
                tags.add(newTag);
            }
            notesFragment.addNoteInfo(title, tags, body, id);
        }
    }

    // generate the lists that store the data being shown to users on the reminders screen
    private void generateRemindersScreen() {
        remindersFragment.clearReminderArrays();

        for (int i = 0; i < userData.getNumReminders(); ++i) {
            PlannerReminder reminder = userData.getReminder(i);

            String timeString;
            final int hour = reminder.getHour();
            final int minute = reminder.getMinute();

            // convert from 24 hour time to 12 hour time, and attach the hour the event starts to
            //   the time string
            if (hour > 12) {
                timeString = (hour - 12) + ":";
            } else if (hour == 0) {
                timeString = "12:";
            } else {
                timeString = hour + ":";
            }

            // attach the minute the event starts to the time string, ensuring there are enough 0's
            if (minute > 9) {
                timeString += minute;
            } else if (minute > 0) {
                timeString += "0" + minute;
            } else {
                timeString += "00";
            }

            // attach the AM/PM suffix for the start time to the time string
            if (hour >= 12) {
                timeString += " PM";
            } else {
                timeString += " AM";
            }

            String dateString = months[reminder.getMonth()] + " " + reminder.getDate() + ", " + reminder.getYear();

            remindersFragment.addReminderInfo(reminder.getTitle(), timeString, dateString, reminder.getMessage(), reminder.getID());
        }
    }

    // return the title that should be at the top of the activity
    private String generateTitle(int position) {
        Log.i("Main", "Generating title with tab position " + position);
        switch (position) {
            case 0:
                return months[curMonth] + " " + curDate + " " +  curYear;
            case 1:
                return "Notes";
            case 2:
                return "Reminders";
            default:
                return "";
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ----------------------------------- User Action Functions -----------------------------------
    // ---------------------------------------------------------------------------------------------

    private void FABOnClicked() {
        switch (tabs.getSelectedTabPosition()) {
            case 0:
                addEvent();
                break;
            case 1:
                addNote();
                break;
            case 2:
                addReminder();
                break;
        }
    }

    // add a new event to the userData, and then show the changes
    private void addEvent() {
        Intent intentBundle = new Intent(Main.this, CreateEvent.class);
        Bundle bundle = new Bundle();
        bundle.putLong("dateInMills", cal.getTimeInMillis());
        bundle.putInt("ID", nextEventID++);
        intentBundle.putExtras(bundle);
        startActivity(intentBundle);
    }

    // add a new note to the userData, and then show the changes
    private void addNote() {
        Intent intentBundle = new Intent(Main.this, CreateNote.class);
        Bundle bundle = new Bundle();
        bundle.putInt("ID", nextNoteID++);
        intentBundle.putExtras(bundle);
        startActivity(intentBundle);
    }

    // add a new reminder to the userData, and then show the changes
    private void addReminder() {
        Intent intentBundle = new Intent(Main.this, CreateReminder.class);
        Bundle bundle = new Bundle();
        bundle.putLong("dateInMills", cal.getTimeInMillis());
        bundle.putInt("ID", nextReminderID++);
        intentBundle.putExtras(bundle);
        startActivity(intentBundle);
    }

    // show a calendar to select a new date when the toolbar is clicked, but a button on the toolbar
    //   is not, meaning the toolbar was clicked near where the date is shown
    private void toolbarClicked(){
        if (viewPager.getCurrentItem() == 0) {
            DialogFragment calDialog = new CalendarDialogFragment();
            calDialog.show(getFragmentManager(), "DateSelect");
        }
    }

    // increment the current date being looked at and refresh all the fragments to reflect the
    //   change
    private void nextDate() {
        cal.add(Calendar.DATE, 1);
        curDay = cal.get(Calendar.DAY_OF_WEEK);
        curDate = cal.get(Calendar.DATE);
        curMonth = cal.get(Calendar.MONTH);
        curYear = cal.get(Calendar.YEAR);

        reloadData();
    }

    // decrement the current date being looked at and refresh all the fragments to reflect the
    //   change
    private void previousDate() {
        cal.add(Calendar.DATE, -1);
        curDay = cal.get(Calendar.DAY_OF_WEEK);
        curDate = cal.get(Calendar.DATE);
        curMonth = cal.get(Calendar.MONTH);
        curYear = cal.get(Calendar.YEAR);

        reloadData();
    }

    // change the date being looked at, and refresh all the fragments to reflect the change
    private void goToDate(int year, int month, int date) {
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DATE, date);
        curDay = cal.get(Calendar.DAY_OF_WEEK);
        curDate = date;
        curMonth = month;
        curYear = year;

        reloadData();
    }

    // ---------------------------------------------------------------------------------------------
    // --------------------------------- Events Fragment Interface ---------------------------------
    // ---------------------------------------------------------------------------------------------

    // this is called whenever one of the elements in the RecyclerView layout in the Events fragment
    //   is clicked, and it opens an activity to edit the event that was clicked
    public void eventClickedAction(int eventID) {
        if (userData.getEventByID(eventID) != null) {
            // start the edit event activity to create a new event from the old data
            Intent intentBundle = new Intent(Main.this, EditEvent.class);
            Bundle bundle = new Bundle();
            bundle.putInt("id", eventID);
            intentBundle.putExtras(bundle);
            startActivity(intentBundle);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // --------------------------------- Notes Fragment Interface ----------------------------------
    // ---------------------------------------------------------------------------------------------

    // this is called whenever one of the elements in the RecyclerView layout in the Notes
    //   fragment is clicked, and it opens an activity to edit the event that was clicked
    public void noteClickedAction(int noteID) {
        Log.i("Main", "Note with id " + noteID + "clicked");
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------- Reminders Fragment Interface --------------------------------
    // ---------------------------------------------------------------------------------------------

    // this is called whenever one of the elements in the RecyclerView layout in the Reminders
    //   fragment is clicked, and it opens an activity to edit the event that was clicked
    public void reminderClickedAction(int eventID) {
        if (userData.getReminderByID(eventID) != null) {
            // start the edit reminder activity to create a new reminder from the old data
            Intent intentBundle = new Intent(Main.this, EditReminder.class);
            Bundle bundle = new Bundle();
            bundle.putInt("id", eventID);
            intentBundle.putExtras(bundle);
            startActivity(intentBundle);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ----------------------------- Calendar DialogFragment Interface -----------------------------
    // ---------------------------------------------------------------------------------------------

    // this is called when a date is selected in the Calendar dialog (which opens after the user
    //   taps the toolbar), and changes the date. The dialog automatically closes immediately after
    //   this is called
    public void onDateSelected(int year, int month, int date) {
        goToDate(year, month, date);
    }

    public long getCurrentSelectedDate() {
        return cal.getTimeInMillis();
    }

    // ---------------------------------------------------------------------------------------------
    // --------------------------------------- Local Adapter ---------------------------------------
    // ---------------------------------------------------------------------------------------------

    class LocalPageAdapter extends FragmentPagerAdapter {
        public LocalPageAdapter(FragmentManager fm) {super(fm);}
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return eventsFragment;
                case 1:
                    return notesFragment;
                case 2:
                    return remindersFragment;
                default:
                    return eventsFragment;
            }
        }
        @Override
        public int getCount() {return NUM_FRAGMENTS;}
        @Override
        public CharSequence getPageTitle(int position) {return titles[position];}
    }
}
