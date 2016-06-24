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
    private final char SPLIT_CHARACTER = (char) 1;
    private final char OPENING_CHARACTER = (char) 2;
    private final char CLOSING_CHARACTER = (char) 3;

    private enum TYPE_CODES {
        INT, LONG, STRING, STR_ARR
    }

    //----------------------------------------------------------------------------------------------
    //--------------------------------- Type Codes Lists for Loading -------------------------------
    //----------------------------------------------------------------------------------------------

    final static List<TYPE_CODES> eventTypeCodes = new ArrayList<>();
    final static List<TYPE_CODES> noteTypeCodes = new ArrayList<>();
    final static List<TYPE_CODES> reminderTypeCodes = new ArrayList<>();

    // these lists are used to decode the save data. The order they are in is the order the
    //   information is stored in inside of the text file
    private static void setupTypeCodes() {
        eventTypeCodes.add(0,TYPE_CODES.LONG);
        eventTypeCodes.add(1, TYPE_CODES.LONG);
        eventTypeCodes.add(2, TYPE_CODES.STRING);
        eventTypeCodes.add(3, TYPE_CODES.STRING);
        eventTypeCodes.add(4, TYPE_CODES.INT);

        noteTypeCodes.add(0, TYPE_CODES.STR_ARR);
        noteTypeCodes.add(1, TYPE_CODES.STRING);
        noteTypeCodes.add(2, TYPE_CODES.STRING);
        noteTypeCodes.add(3, TYPE_CODES.INT);

        reminderTypeCodes.add(0,TYPE_CODES.LONG);
        reminderTypeCodes.add(1, TYPE_CODES.STRING);
        reminderTypeCodes.add(2, TYPE_CODES.STRING);
        reminderTypeCodes.add(3, TYPE_CODES.INT);
    }

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
            setupTypeCodes();
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
        final String split = Character.toString(SPLIT_CHARACTER);
        final String open = Character.toString(OPENING_CHARACTER);
        final String close = Character.toString(CLOSING_CHARACTER);
        // events
        String saveString = open;
        for (PlannerEvent event : events) {
            saveString += open + event.getStartMills() + split + event.getEndMills() + split
                          + event.getTitle() + split + event.getMessage() + split
                          + event.getID() + close;
        }
        // reminders
        saveString +=  close + open;
        for (PlannerNote note : notes) {
            saveString += open + open;
            for (int i = 0; i < note.getNumTags(); ++i) {
                if (i != 0) {
                    saveString += split;
                }
                saveString += note.getTag(i);
            }
            saveString +=  close + note.getTitle() + split + note.getBody() + split + note.getID() + close;
        }
        // notes
        saveString +=  close + open;
        for (PlannerReminder reminder : reminders) {
            saveString += open + reminder.getMills() + split + reminder.getTitle() + split
                          + reminder.getMessage() + split + reminder.getID() +  close;
        }
        saveString += close;
        try {
            FileOutputStream outputStream = context.openFileOutput(SAVE_FILE_NAME, Context.MODE_PRIVATE);
            outputStream.write(saveString.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void LoadData(Context context) {
        events.clear();
        notes.clear();
        reminders.clear();
        String saveString;
        try {
            File file = new File(context.getCacheDir(), SAVE_FILE_NAME);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
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
        Integer i = 0;
        final char[] saveData = saveString.toCharArray();
        // Events
        for (; i < saveData.length; ++i) {
            List eventData = decode(eventTypeCodes, saveData, i);
            if (eventData != null) {
                PlannerEvent event = new PlannerEvent((long) eventData.get(0), (long) eventData.get(1),
                        (String) eventData.get(2), (String) eventData.get(3), (int) eventData.get(4));
                events.add(event);
            }
            if (i >= saveData.length) {
                return;
            } else if (saveData[i] == CLOSING_CHARACTER) {
                ++i;
                break;
            }
        }
        // Notes
        for (; i < saveData.length; ++i) {
            List noteData = decode(noteTypeCodes, saveData, i);
            if (noteData != null) {
                PlannerNote note = new PlannerNote((ArrayList<String>) noteData.get(0),
                        (String) noteData.get(1), (String) noteData.get(2), (int) noteData.get(3));
                notes.add(note);
            }
            if (i >= saveData.length) {
                return;
            } else if (saveData[i] == CLOSING_CHARACTER) {
                ++i;
                break;
            }
        }
        // Reminders
        for (; i < saveData.length; ++i) {
            List reminderData = decode(reminderTypeCodes, saveData, i);
            if (reminderData != null) {
                PlannerReminder reminder = new PlannerReminder((long) reminderData.get(0),
                        (String) reminderData.get(1), (String) reminderData.get(2), (int) reminderData.get(3));
                reminders.add(reminder);
            }
            if (i >= saveData.length || saveData[i] == CLOSING_CHARACTER) {
                return;
            }
        }
    }

    // decodes until all fields are filled, so excess trailing characters are OK. It makes
    //   startingIndex point to the character after the closing character of the last chunk
    private List decode(List<TYPE_CODES> typeCodes,  char[] cArr, Integer startingIndex) {
        int i = startingIndex.intValue();
        List decodedData = new ArrayList();
        int intVal = 0;
        long longVal = 0;
        String stringVal = "";

        for (int t = 0; t < typeCodes.size(); ++t) {
            for (; i < cArr.length && cArr[i] == OPENING_CHARACTER; ++i);
            switch (typeCodes.get(t)) {
                case INT:
                    for (; i < cArr.length && cArr[i] != CLOSING_CHARACTER; ++i) {
                        if (cArr[i] >= '0' && cArr[i] <= '9') {
                            intVal = intVal * 10 + (cArr[i] - '0');
                        } else {
                            return null;
                        }
                    }
                    decodedData.add(t, intVal);
                    intVal = 0;
                    break;
                case LONG:
                    for (; i < cArr.length && cArr[i] != CLOSING_CHARACTER; ++i) {
                        if (cArr[i] >= '0' && cArr[i] <= '9') {
                            longVal = longVal * 10 + (cArr[i] - '0');
                        } else {
                            return null;
                        }
                    }
                    decodedData.add(t, longVal);
                    longVal = 0;
                    break;
                case STRING:
                    for (; i < cArr.length && cArr[i] != CLOSING_CHARACTER; ++i) {
                        stringVal += cArr[i];
                    }
                    decodedData.add(t, stringVal);
                    stringVal = "";
                    break;
                case STR_ARR:
                    List<String> strings = new ArrayList<>();
                    for (; i < cArr.length && cArr[i] != CLOSING_CHARACTER; ++i) {
                        String string = "";
                        for (; i < cArr.length && cArr[i] != CLOSING_CHARACTER; ++i) {
                            if (cArr[i] == SPLIT_CHARACTER) {
                                if (!"".equals(string)) {
                                    strings.add(string);
                                }
                                break;
                            }
                        }
                    }
                    decodedData.add(strings);
                    break;
            }
        }
        if (i + 1 < cArr.length) {
            ++i;
        }
        return decodedData;
    }
}
