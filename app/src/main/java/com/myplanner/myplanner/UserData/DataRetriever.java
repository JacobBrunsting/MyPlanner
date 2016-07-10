package com.myplanner.myplanner.UserData;

import android.content.Context;

import java.io.BufferedReader;
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
    private int ioCounter = 0;

    private static final int MILLS_PER_HOUR = 3600000;
    private static final int MILLS_PER_MINUTE = 60000;
    private static final String EVENTS_SAVE_FILE = "EventsSaveFile";
    private static final String NOTES_SAVE_FILE = "NotesSaveFile";
    private static final String REMINDERS_SAVE_FILE = "RemindersSaveFile";
    private static final char SPLIT_CHARACTER = (char) 1;
    private static final char OPENING_CHARACTER = (char) 2;
    private static final char CLOSING_CHARACTER = (char) 3;
    private static boolean shouldSaveEvents = true;
    private static boolean shouldSaveNotes = true;
    private static boolean shouldSaveReminders = true;
    private int nextEventID = 0;
    private int nextNoteID = 0;
    private int nextReminderID = 0;

    private enum TYPE_CODES {
        INT, LONG, STRING, STR_ARR
    }

    //----------------------------------------------------------------------------------------------
    //--------------------------------- Type Codes Lists for Loading -------------------------------
    //----------------------------------------------------------------------------------------------

    final static List<TYPE_CODES> eventTypeCodes = new ArrayList<>();
    final static List<TYPE_CODES> noteTypeCodes = new ArrayList<>();
    final static List<TYPE_CODES> reminderTypeCodes = new ArrayList<>();
    final static List<TYPE_CODES> nextIdTypeCodes = new ArrayList<>();

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

        nextIdTypeCodes.add(0, TYPE_CODES.INT);
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

    public int getNextEventID() {
        return nextEventID;
    }

    public int getNextNoteID() {
        return nextNoteID;
    }

    public int getNextReminderID() {
        return nextReminderID;
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
            return;
        }

        int insertPos = 0;
        while (insertPos < events.size() &&
                (events.get(insertPos).getStartMills() < event.getStartMills() ||
                        (events.get(insertPos).getStartMills() == event.getStartMills() &&
                                events.get(insertPos).getEndMills() > event.getEndMills()))) {
            ++insertPos;
        }

        shouldSaveEvents = true;
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
    }

    public void addNote(PlannerNote note) {
        if (notes == null) {
            return;
        }

        shouldSaveNotes = true;
        notes.add(note);
    }

    public void addReminder(PlannerReminder reminder) {
        if (reminders == null) {
            return;
        }

        int insertPos = 0;
        while (insertPos + 1 < reminders.size()
                && reminders.get(insertPos).getMills() < reminder.getMills()) {
            ++insertPos;
        }

        shouldSaveReminders = true;
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

    public void saveData(Context context) {
        final String split = Character.toString(SPLIT_CHARACTER);
        final String open = Character.toString(OPENING_CHARACTER);
        final String close = Character.toString(CLOSING_CHARACTER);

        // events
        if (shouldSaveEvents) {
            String eventsSave = open + open + nextEventID + close;
            for (PlannerEvent event : events) {
                eventsSave += open + event.getStartMills() + split + event.getEndMills() + split
                        + event.getTitle() + split + event.getMessage() + split
                        + event.getID() + close;
            }
            eventsSave += close;

            try {
                OutputStreamWriter outputStream = new OutputStreamWriter(context.openFileOutput(EVENTS_SAVE_FILE, Context.MODE_PRIVATE));
                outputStream.write(eventsSave);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // notes
        if (shouldSaveNotes) {
            String notesSave = open + open + nextNoteID + close;
            for (PlannerNote note : notes) {
                notesSave += open + open;
                for (int i = 0; i < note.getNumTags(); ++i) {
                    if (!note.getTag(i).equals("")) {
                        if (i != 0) {
                            notesSave += split;
                        }
                        notesSave += note.getTag(i);
                    }
                }
                notesSave += close + note.getTitle() + split + note.getBody() + split + note.getID() + close;
            }
            notesSave += close;

            try {
                OutputStreamWriter outputStream = new OutputStreamWriter(context.openFileOutput(NOTES_SAVE_FILE, Context.MODE_PRIVATE));
                outputStream.write(notesSave);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // reminders
        if (shouldSaveReminders) {
            String remindersSave = open + open + nextReminderID + close;
            for (PlannerReminder reminder : reminders) {
                remindersSave += open + reminder.getMills() + split + reminder.getTitle() + split
                        + reminder.getMessage() + split + reminder.getID() + close;
            }
            remindersSave += close;

            try {
                OutputStreamWriter outputStream = new OutputStreamWriter(context.openFileOutput(REMINDERS_SAVE_FILE, Context.MODE_PRIVATE));
                outputStream.write(remindersSave);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        shouldSaveEvents = shouldSaveNotes = shouldSaveReminders = false;
    }

    public boolean LoadData(Context context) {
        events.clear();
        notes.clear();
        reminders.clear();
        String saveString = loadFile(EVENTS_SAVE_FILE, context) + loadFile(NOTES_SAVE_FILE, context) + loadFile(REMINDERS_SAVE_FILE, context);
        final char[] saveData = saveString.toCharArray();
        // the io counter starts at 1 to skip over the opening section bracket for the events
        ioCounter = 1;
        // Events
        if (ioCounter < saveData.length && saveData[ioCounter] != CLOSING_CHARACTER) {
            List nextId = decode(nextIdTypeCodes, saveData);
            if (nextId != null && nextId.size() == 1 && nextId.get(0).getClass().equals(Integer.class)) {
                nextEventID = (int) nextId.get(0);
            } else {
                nextEventID = 0;
            }
            while (ioCounter < saveData.length) {
                if (saveData[ioCounter] == CLOSING_CHARACTER) {
                    ++ioCounter;
                    break;
                }
                List eventData = decode(eventTypeCodes, saveData);
                if (eventData == null) {
                    break;
                } else {
                    PlannerEvent event = new PlannerEvent((long) eventData.get(0), (long) eventData.get(1),
                            (String) eventData.get(2), (String) eventData.get(3), (int) eventData.get(4));
                    events.add(event);
                }
                if (ioCounter >= saveData.length) {
                    return true;
                } else if (saveData[ioCounter] == CLOSING_CHARACTER) {
                    break;
                }
            }
        }
        // we increment the io counter twice; once to skip over the closing bracket of the events
        //   section, and once to skip over the opening section bracket for the notes
        ioCounter += 2;
        // Notes
        if (ioCounter < saveData.length && saveData[ioCounter] != CLOSING_CHARACTER) {
            List nextId = decode(nextIdTypeCodes, saveData);
            if (nextId != null && nextId.size() == 1 && nextId.get(0).getClass().equals(Integer.class)) {
                nextNoteID = (int) nextId.get(0);
            } else {
                nextNoteID = 0;
            }
            while (ioCounter < saveData.length) {
                List noteData = decode(noteTypeCodes, saveData);
                if (noteData == null) {
                    break;
                } else {
                    PlannerNote note = new PlannerNote((ArrayList<String>) noteData.get(0),
                            (String) noteData.get(1), (String) noteData.get(2), (int) noteData.get(3));
                    notes.add(note);
                }
                if (ioCounter >= saveData.length) {
                    return true;
                } else if (saveData[ioCounter] == CLOSING_CHARACTER) {
                    break;
                }
            }
        }
        // we increment the io counter twice; once to skip over the closing bracket of the notes
        //   section, and once to skip over the opening section bracket for the reminders
        ioCounter += 2;
        // Reminders
        if (ioCounter >= saveData.length || saveData[ioCounter] == CLOSING_CHARACTER) {
            // if the section is empty, we are done, since this is the last section
            return true;
        }
        List nextId = decode(nextIdTypeCodes, saveData);
        if (nextId != null && nextId.size() == 1 && nextId.get(0).getClass().equals(Integer.class)) {
            nextReminderID = (int) nextId.get(0);
        } else {
            nextReminderID = 0;
        }
        while (ioCounter < saveData.length) {
            List reminderData = decode(reminderTypeCodes, saveData);
            if (reminderData == null) {
                break;
            } else {
                PlannerReminder reminder = new PlannerReminder((long) reminderData.get(0),
                        (String) reminderData.get(1), (String) reminderData.get(2), (int) reminderData.get(3));
                reminders.add(reminder);
            }
            if (ioCounter >= saveData.length || saveData[ioCounter] == CLOSING_CHARACTER) {
                return true;
            }
        }
        return true;
    }

    // decodes until all fields are filled, so excess trailing characters are OK
    private List decode(List<TYPE_CODES> typeCodes,  char[] cArr) {
        List decodedData = new ArrayList();
        int intVal = 0;
        long longVal = 0;
        String stringVal = "";

        // increment ioCounter to skip over the opening bracket
        ioCounter++;

        // if the body is empty
        if (ioCounter < cArr.length && cArr[ioCounter] == CLOSING_CHARACTER) {
            return null;
        }

        for (int t = 0; t < typeCodes.size(); ++t) {
            switch (typeCodes.get(t)) {
                case INT:
                    for (; ioCounter < cArr.length && cArr[ioCounter] != SPLIT_CHARACTER && cArr[ioCounter] != CLOSING_CHARACTER; ++ioCounter) {
                        if (cArr[ioCounter] >= '0' && cArr[ioCounter] <= '9') {
                            intVal = intVal * 10 + (cArr[ioCounter] - '0');
                        } else {
                            return null;
                        }
                    }
                    decodedData.add(t, intVal);
                    intVal = 0;
                    break;
                case LONG:
                    for (; ioCounter < cArr.length && cArr[ioCounter] != SPLIT_CHARACTER && cArr[ioCounter] != CLOSING_CHARACTER; ++ioCounter) {
                        if (cArr[ioCounter] >= '0' && cArr[ioCounter] <= '9') {
                            longVal = longVal * 10 + (cArr[ioCounter] - '0');
                        } else {
                            return null;
                        }
                    }
                    decodedData.add(t, longVal);
                    longVal = 0;
                    break;
                case STRING:
                    for (; ioCounter < cArr.length && cArr[ioCounter] != SPLIT_CHARACTER && cArr[ioCounter] != CLOSING_CHARACTER; ++ioCounter) {
                        stringVal += cArr[ioCounter];
                    }
                    decodedData.add(t, stringVal);
                    stringVal = "";
                    break;
                case STR_ARR:
                    List<String> strings = new ArrayList<>();
                    // we increment the io counter to skip over the opening character at the start
                    //   of the array
                    ++ioCounter;
                    for (; ioCounter < cArr.length && cArr[ioCounter] != CLOSING_CHARACTER; ++ioCounter) {
                        String string = "";
                        for (; ioCounter < cArr.length; ++ioCounter) {
                            if (cArr[ioCounter] == SPLIT_CHARACTER || cArr[ioCounter] == CLOSING_CHARACTER) {
                                if (!"".equals(string)) {
                                    strings.add(string);
                                }
                                break;
                            }
                            string += cArr[ioCounter];
                        }
                        if (cArr[ioCounter] == CLOSING_CHARACTER) {
                            break;
                        }
                    }
                    decodedData.add(t, strings);
                    // we increment the io counter to skip over the closing character at the end of
                    //   the array
                    ++ioCounter;
                    break;
            }
            // we always increment the io counter to skip over the split/closing character that
            //   separates the data chunks. This also will skip over the closing character, making
            //   ioCounter point to the next chunk of data to be loaded.
            ++ioCounter;
        }
        return decodedData;
    }

    private String loadFile(String file, Context context) {
        try {
            InputStream stream = context.openFileInput(file);
            InputStreamReader reader = new InputStreamReader(stream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = bufferedReader.readLine();
            StringBuilder stringBuffer = new StringBuilder();
            while (line != null) {
                stringBuffer.append(line);
                line = bufferedReader.readLine();
            }
            stream.close();
            return stringBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
