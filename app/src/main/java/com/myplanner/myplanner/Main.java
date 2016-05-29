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
import java.util.List;
import java.util.Calendar;

public class Main extends AppCompatActivity implements Events.EventInterface,
        Reminders.ReminderInterface, Notes.NotesInterface, CalendarDialogFragment.CalendarInterface {
    private final int NUM_FRAGMENTS = 3;
    private final String[] titles = {"Events", "Notes", "Reminders"};
    private final Events eventsFragment = new Events();
    private final Notes notesFragment = new Notes();
    private final Reminders remindersFragment = new Reminders();
    private final ArrayList<String> possibleTags = new ArrayList<>(); // This is an arrayList so it is serializable

    private final Calendar cal = Calendar.getInstance();

    private final String[] months = {"January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"};

    private int curDate = cal.get(Calendar.DATE);
    private int curMonth = cal.get(Calendar.MONTH);
    private int curYear = cal.get(Calendar.YEAR);

    private TabLayout tabs;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private DataRetriever userData;

    private int nextEventID = 0;
    private int nextNoteID = 0;
    private int nextReminderID = 0;

    // ---------------------------------------------------------------------------------------------
    // ---------------------------- Events Fragment Interface Functions ----------------------------
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
    // ---------------------------- Notes Fragment Interface Functions -----------------------------
    // ---------------------------------------------------------------------------------------------

    // this is called whenever one of the elements in the RecyclerView layout in the Notes
    //   fragment is clicked, and it opens an activity to edit the event that was clicked
    public void noteClickedAction(int noteID) {
        if (userData.getNoteByID(noteID) != null) {
            // start the edit note activity to create a new note from the old data
            Intent intentBundle = new Intent(Main.this, EditNote.class);
            Bundle bundle = new Bundle();
            bundle.putInt("id", noteID);
            bundle.putSerializable("possibleTags", possibleTags);
            intentBundle.putExtras(bundle);
            startActivity(intentBundle);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // -------------------------- Reminders Fragment Interface Functions ---------------------------
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
    // ------------------------ Calendar DialogFragment Interface Functions ------------------------
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

    private void addEvent() {
        Intent intentBundle = new Intent(Main.this, CreateEvent.class);
        Bundle bundle = new Bundle();
        bundle.putLong("dateInMills", cal.getTimeInMillis());
        bundle.putInt("ID", nextEventID++);
        intentBundle.putExtras(bundle);
        startActivity(intentBundle);
    }

    private void addNote() {
        Intent intentBundle = new Intent(Main.this, CreateNote.class);
        Bundle bundle = new Bundle();
        bundle.putInt("ID", nextNoteID++);
        bundle.putSerializable("possibleTags", possibleTags);
        intentBundle.putExtras(bundle);
        startActivity(intentBundle);
    }

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

    // increment the current date being looked at and refresh the events fragment to reflect the
    //   change
    private void nextDate() {
        cal.add(Calendar.DATE, 1);
        curDate = cal.get(Calendar.DATE);
        curMonth = cal.get(Calendar.MONTH);
        curYear = cal.get(Calendar.YEAR);

        reloadData(0);
    }

    // decrement the current date being looked at and refresh all the fragments to reflect the
    //   change
    private void previousDate() {
        cal.add(Calendar.DATE, -1);
        curDate = cal.get(Calendar.DATE);
        curMonth = cal.get(Calendar.MONTH);
        curYear = cal.get(Calendar.YEAR);

        reloadData(0);
    }

    // change the date being looked at, and refresh all the fragments to reflect the change
    private void goToDate(final int year, final int month, final int date) {
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DATE, date);
        curDate = date;
        curMonth = month;
        curYear = year;

        reloadData();
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------------ Override Functions -------------------------------------
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
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
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // add the listener here to avoid null pointer issues with toolbar
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                toolbar.setTitle(generateTitle(position));
                if (position == 0) {
                    toolbar.getMenu().findItem(R.id.action_forward).setVisible(true);
                    toolbar.getMenu().findItem(R.id.action_back).setVisible(true);
                } else {
                    toolbar.getMenu().findItem(R.id.action_forward).setVisible(false);
                    toolbar.getMenu().findItem(R.id.action_back).setVisible(false);
                }
                userData.setCurrentTab(position);
                ((FloatingActionButton) findViewById(R.id.main_fab)).show();
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
    public boolean onOptionsItemSelected(final MenuItem item) {
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
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt("nextEventID", nextEventID);
        savedInstanceState.putInt("nextNoteID", nextNoteID);
        savedInstanceState.putInt("nextReminderID", nextReminderID);
    }

    // retrieve the saved data from when the activity was closed
    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        nextEventID = savedInstanceState.getInt("nextEventID");
        nextNoteID = savedInstanceState.getInt("nextNoteID");
        nextReminderID = savedInstanceState.getInt("nextReminderID");
    }

    // ---------------------------------------------------------------------------------------------
    // --------------------------------- User Interface Generation ---------------------------------
    // ---------------------------------------------------------------------------------------------

    // refresh the data and then reload the fragment for all fragments
    private void reloadData() {
        for (int i = 0; i < NUM_FRAGMENTS; ++i) {
            reloadData(i);
        }
    }

    // refresh the data and reload the fragment at the specified tab
    private void reloadData(final int tab) {
        switch(tab) {
            case 0:
                // generate the data
                generateEventScreen();

                // adjust the toolbar accordingly
                if (toolbar == null) {
                    toolbar = (Toolbar) findViewById(R.id.main_toolbar);
                }
                toolbar.setTitle(generateTitle(viewPager.getCurrentItem()));

                // reload the fragment
                eventsFragment.reloadData();
                break;
            case 1:
                // generate the data
                generateNotesScreen();

                // adjust the toolbar accordingly
                if (toolbar == null) {
                    toolbar = (Toolbar) findViewById(R.id.main_toolbar);
                }
                toolbar.setTitle(generateTitle(viewPager.getCurrentItem()));

                // reload the fragment
                notesFragment.reloadData();
                break;
            case 2:
                // generate the data
                generateRemindersScreen();

                // adjust the toolbar accordingly
                if (toolbar == null) {
                    toolbar = (Toolbar) findViewById(R.id.main_toolbar);
                }
                toolbar.setTitle(generateTitle(viewPager.getCurrentItem()));

                // reload the fragment
                remindersFragment.reloadData();
                break;
        }
    }

    // generate the lists that store the data being shown to the user on the event screen
    private void generateEventScreen() {
        // remove the previously generated data
        eventsFragment.clearEventLists();

        // generate the new data
        for (int i = 0; i < userData.getNumEvents(); ++i) {
            // retrieve some of the properties of the event from the DataRetriever
            final PlannerEvent event = userData.getEvent(i);
            final int startHour = event.getStartHour();
            final int endHour = event.getEndHour();
            final int startMinute = event.getStartMinute();
            final int endMinute = event.getEndMinute();
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

            eventsFragment.addEventInfo(event.getTitle(), timeString, event.getMessage(), event.getID());
        }
    }

    // generate the lists that store the data being shown to users on the notes screen
    private void generateNotesScreen() {
        // remove the previously generated data
        notesFragment.clearNoteLists();

        // generate the new data
        for (int i = 0; i < userData.getNumNotes(); ++i) {
            // retrieve the properties of the note from the DataRetriever
            PlannerNote note = userData.getNote(i);
            final String title = note.getTitle();
            final String body = note.getBody();
            final int id = note.getID();

            // for each tag, add it to the list, and add it to the list of possible tags, which
            //   contains all of the tags currently on a note
            final List<String> tags = new ArrayList<>();
            for (int t = 0; t < note.getNumTags(); ++t) {
                final String newTag = note.getTag(t);
                tags.add(newTag);
                possibleTags.add(newTag);
            }
            notesFragment.addNoteInfo(title, tags, body, id);
        }
    }

    // generate the lists that store the data being shown to users on the reminders screen
    private void generateRemindersScreen() {
        // remove the previously generated data
        remindersFragment.clearReminderLists();

        // generate the new data
        for (int i = 0; i < userData.getNumReminders(); ++i) {
            final PlannerReminder reminder = userData.getReminder(i);
            final int hour = reminder.getHour();
            final int minute = reminder.getMinute();
            String timeString;

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
    private String generateTitle(final int position) {
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
    // ---------------------------------------- Tab Adapter ----------------------------------------
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
