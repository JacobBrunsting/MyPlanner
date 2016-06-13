package com.myplanner.myplanner.UserData;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    private static List<PlannerEvent> events;
    private static List<PlannerNote> notes;
    private static List<PlannerReminder> reminders;

    private int currentTab = 0;

    private final int MILLS_PER_HOUR = 3600000;
    private final int MILLS_PER_MINUTE = 60000;
    private final String SAVE_FILE_NAME = "MyPlannerSaveFile";

    //----------------------------------------------------------------------------------------------
    //-------------------------------- Singleton Class Declarations --------------------------------
    //----------------------------------------------------------------------------------------------

    private DataRetriever() {}

    private static DataRetriever instance = null;

    public static DataRetriever getInstance() {
        if (instance == null) {
            events = new ArrayList<>();
            notes = new ArrayList<>();
            reminders = new ArrayList<>();
            instance = new DataRetriever();
            return instance;
        } else {
            return instance;
        }
    }

    //----------------------------------------------------------------------------------------------
    //------------------------------------------ Getters -------------------------------------------
    // ---------------------------------------------------------------------------------------------

    public int getCurrentTab() {return currentTab;}

    public PlannerEvent getEvent(int position) {return events.get(position);}

    public PlannerEvent getEventByID(int id) {
        if (events == null) {
            return null;
        }
        for (int i = 0; i < events.size(); ++i) {
            if (events.get(i).getID() == id) {
                return events.get(i);
            }
        }
        return null;
    }

    public int getNumEvents() {
        if (events == null) {
            return 0;
        }
        return events.size();
    }

    public PlannerNote getNote(int position) {return notes.get(position);}

    public PlannerNote getNoteByID(int id) {
        if (notes == null) {
            return null;
        }
        for (int i = 0; i < notes.size(); ++i) {
            if (notes.get(i).getID() == id) {
                return notes.get(i);
            }
        }
        return null;
    }

    public int getNumNotes() {
        if (notes == null) {
            return 0;
        }
        return notes.size();
    }

    public PlannerReminder getReminder(int position) {return reminders.get(position);}

    public PlannerReminder getReminderByID(int id) {
        if (reminders == null) {
            return null;
        }
        for (int i = 0; i < reminders.size(); ++i) {
            if (reminders.get(i).getID() == id) {
                return reminders.get(i);
            }
        }
        return null;
    }

    public int getNumReminders() {
        if (reminders == null) {
            return 0;
        }
        return reminders.size();
    }

    //----------------------------------------------------------------------------------------------
    //-------------------------------------- Filtered Getters --------------------------------------
    // ---------------------------------------------------------------------------------------------

    // gets a list of the events for some date
    public List<PlannerEvent> getDateEvents(int year, int month, int date) {
        List<PlannerEvent> dayEvents = new ArrayList<>();
        int pos = 0;
        int newPos = 0;

        if (events == null || events.size() == 0) {
            return dayEvents;
        }

        // look at all events that start before the target day to see if they end after the target
        PlannerEvent curEvent = events.get(0);
        while (year > curEvent.getStartYear() ||
                (year == curEvent.getStartYear() && month > curEvent.getStartMonth()) ||
                (year == curEvent.getStartYear() && month == curEvent.getStartMonth() && date > curEvent.getStartDate())) {

            if (curEvent.getEndYear() == year && curEvent.getEndMonth() == month
                    && curEvent.getEndDate() == date) {
                long midnightInMills = curEvent.getStartMills() - (curEvent.getStartHour() * MILLS_PER_HOUR + curEvent.getStartMinute() * MILLS_PER_MINUTE);
                PlannerEvent tempEvent = new PlannerEvent(midnightInMills, curEvent.getEndMills(), curEvent.getTitle(), curEvent.getMessage(), curEvent.getID());
                dayEvents.add(newPos++, tempEvent);
            } else if (curEvent.getEndYear() >= year && curEvent.getEndMonth() >= month
                    && curEvent.getEndDate() >= date) {
                long midnightInMills = curEvent.getStartMills() - (curEvent.getStartHour() * MILLS_PER_HOUR + curEvent.getStartMinute() * MILLS_PER_MINUTE);
                long lastMinuteInMills = midnightInMills + 24 * MILLS_PER_HOUR - MILLS_PER_MINUTE;
                PlannerEvent tempEvent = new PlannerEvent(midnightInMills, lastMinuteInMills, curEvent.getTitle(), curEvent.getMessage(), curEvent.getID());
                dayEvents.add(newPos++, tempEvent);
            }

            if (++pos < events.size()) {
                curEvent = events.get(pos);
            } else {
                return dayEvents;
            }
        }

        // look at all events that start at the target date
        while (pos < events.size() && curEvent.getStartYear() == year
                && curEvent.getStartMonth() == month && curEvent.getStartDate() == date) {
            if (curEvent.getEndYear() == year && curEvent.getEndMonth() == month && curEvent.getEndDate() == date) {
                dayEvents.add(newPos++, curEvent);
            } else {
                long lastMinuteInMills = curEvent.getStartMills() + (23 - curEvent.getStartHour()) * MILLS_PER_HOUR + (59 - curEvent.getStartMinute()) * MILLS_PER_MINUTE;
                PlannerEvent tempEvent = new PlannerEvent(curEvent.getStartMills(), lastMinuteInMills, curEvent.getTitle(), curEvent.getMessage(), curEvent.getID());
                dayEvents.add(newPos++, tempEvent);
            }


            if (++pos < events.size()) {
                curEvent = events.get(pos);
            } else {
                return dayEvents;
            }
        }

        return dayEvents;
    }

    // gets a list of the notes for some tag
    public List<PlannerNote> getTagNotes(int year, List<String> tags) {
        if (notes == null) {
            return null;
        }

        List<PlannerNote> tagNotes = new ArrayList<>();
        int newPos = 0;

        for (int i = 0; i < notes.size(); ++i) {
            for (int t1 = 0; t1 < tags.size(); ++t1) {
                for (int t2 = 0; t2 < notes.get(i).getNumTags(); ++t2) {
                    if (notes.get(i).getTag(t2).equals(tags.get(t1))) {
                        tagNotes.add(newPos++, notes.get(i));
                        t1 = tags.size();
                        break;
                    }
                }
            }
        }

        return tagNotes;
    }

    // gets a list of the reminders for some date
    public List<PlannerReminder> getDateReminders(int year, int month, int date) {
        if (reminders == null) {
            return null;
        }

        List<PlannerReminder> dayReminders = new ArrayList<>();
        int pos = 0;
        int newPos = 0;

        while (pos < reminders.size() && year > reminders.get(pos++).getYear());
        while (pos < reminders.size() && month > reminders.get(pos++).getMonth());
        while (pos < reminders.size() && date > reminders.get(pos++).getDate());
        while (pos < reminders.size() && date == reminders.get(pos).getDate()) {
            dayReminders.add(newPos++, reminders.get(pos++));
        }

        return dayReminders;
    }

    //----------------------------------------------------------------------------------------------
    //------------------------------------- Addition Functions -------------------------------------
    // ---------------------------------------------------------------------------------------------

    public void setCurrentTab(int tab) {currentTab = tab;}

    public void addEvent(PlannerEvent event) {
        if (events == null) {
            events.add(event);
            return;
        }
        for (int k = 0; k < events.size(); ++k) {
            PlannerEvent e = events.get(k);
        }

        int insertPos = 0;
        while (insertPos < events.size() &&
                (events.get(insertPos).getStartMills() < event.getStartMills() ||
                        (events.get(insertPos).getStartMills() == event.getStartMills() &&
                                events.get(insertPos).getEndMills() > event.getEndMills()))) {
            ++insertPos;
        }

        // insert the event into the List. The items must be shifted manually because the add()
        //   function in List may not preserve order.
        if (events.size() == 0) {
            events.add(event);
        } else {
            events.add(events.size(), event);

            for (int i = events.size() - 1; i > insertPos; --i) {
                events.set(i, events.get(i - 1));
            }

            events.set(insertPos, event);
        }

        for (int x = 0; x < events.size(); ++x) {
            PlannerEvent e = events.get(x);
        }
    }

    public void addNote(PlannerNote note) {
        notes.add(note);
    }

    public void addReminder(PlannerReminder reminder) {
        if (reminders == null) {
            reminders.add(reminder);
            return;
        }

        int insertPos = 0;

        while (insertPos + 1 < reminders.size()
                && reminders.get(insertPos).getMills() < reminder.getMills()) {
            ++insertPos;
        }

        // insert the reminder into the List. The items must be shifted manually because the
        //   add() function in List may not preserve order. THIS MIGHT NOT ACTUALLY BE TRUE
        if (reminders.size() == 0) {
            reminders.add(reminder);
        } else {
            reminders.add(reminders.size(), reminder);

            for (int i = reminders.size() - 1; i > insertPos; --i) {
                reminders.set(i, reminders.get(i - 1));
            }

            reminders.set(insertPos, reminder);
        }
    }

    //----------------------------------------------------------------------------------------------
    //------------------------------------- Removal Functions --------------------------------------
    // ---------------------------------------------------------------------------------------------

    public void removeEvent(int id) {
        if (events != null) {
            int i = 0;
            while (i < events.size()) {
                if (events.get(i).getID() == id) {
                    events.remove(i);
                    return;
                }

                ++i;
            }
        }
    }

    public void removeNote(int id) {
        if  (notes != null) {
            int i = 0;
            while (i < notes.size()) {
                if (notes.get(i).getID() == id) {
                    notes.remove(i);
                    return;
                }

                ++i;
            }
        }
    }

    public void removeReminder(int id) {
        if (reminders != null) {
            int i = 0;
            while (i < reminders.size()) {
                if (reminders.get(i).getID() == id) {
                    reminders.remove(i);
                    return;
                }

                ++i;
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    //------------------------------------ Save/Load Functions -------------------------------------
    // ---------------------------------------------------------------------------------------------

    public void SaveData(Context context) {
        String saveString = "E{";
        for (PlannerEvent event : events) {
            saveString += "{" + event.getStartMills() + "," + event.getEndMills() + ","
                          + event.getTitle() + "," + event.getMessage() + ","
                          + event.getID() + "}";
        }
        saveString += "}N{";
        for (PlannerNote note : notes) {
            saveString += "{{";
            for (int i = 0; i < note.getNumTags(); ++i) {
                if (i != 0) {
                    saveString += ",";
                }
                saveString += note.getTag(i);
            }
            saveString += "}" + note.getTitle() + "," + note.getBody() + "," + note.getID() + "}";
        }
        saveString += "}R{";
        for (PlannerReminder reminder : reminders) {
            saveString += "{" + reminder.getMills() + "," + reminder.getTitle() + ","
                          + reminder.getMessage() + "," + reminder.getID() + "}";
        }
        saveString += "}";
        try {
            FileOutputStream outputStream = context.openFileOutput(SAVE_FILE_NAME, Context.MODE_PRIVATE);
            outputStream.write(saveString.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void LoadData(Context context) {
        String saveString;
        try {
            File file = new File(context.getCacheDir(), SAVE_FILE_NAME);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String saveData = "";
            String line = bufferedReader.readLine();
            StringBuffer stringBuffer = new StringBuffer();
            while (line != null) {
                stringBuffer.append(line);
                line = bufferedReader.readLine();
            }
            saveString = stringBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            saveString = "";
        }
        // add code to extract the data from the string
    }
}
