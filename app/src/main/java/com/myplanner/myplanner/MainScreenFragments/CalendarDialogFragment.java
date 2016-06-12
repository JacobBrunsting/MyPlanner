package com.myplanner.myplanner.MainScreenFragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import com.myplanner.myplanner.R;

import java.util.Calendar;

public class CalendarDialogFragment extends DialogFragment {
    DatePicker dateSelector = null;

    // ---------------------------------------------------------------------------------------------
    // ----------------------------------------- Interface -----------------------------------------
    // ---------------------------------------------------------------------------------------------

    CalendarInterface mCallback;
    public interface CalendarInterface {
        void onDateSelected(int year, int month, int date);
        long getCurrentSelectedDate();
    }

    // ---------------------------------------------------------------------------------------------
    // ----------------------------- DialogFragment Override Functions -----------------------------
    // ---------------------------------------------------------------------------------------------

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (CalendarInterface) context;

        // setup the date selector if dateSelector is not null, meaning onCreateView has been called
        if (dateSelector != null) {
            setupDateSelector();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_calendar_dialog, container, false);
        dateSelector = (DatePicker) view.findViewById(R.id.date_selector);

        // set up the date selector if mCallback is not null, meaning onAttach has been called
        if (mCallback != null) {
            setupDateSelector();
        }

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------------- Private functions -------------------------------------
    // ---------------------------------------------------------------------------------------------

    // create the on date selected action for the calendar
    private void setupDateSelector() {
        final Calendar currentCal = Calendar.getInstance();
        currentCal.setTimeInMillis(mCallback.getCurrentSelectedDate());
        dateSelector.init(
                currentCal.get(Calendar.YEAR),
                currentCal.get(Calendar.MONTH),
                currentCal.get(Calendar.DATE),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        mCallback.onDateSelected(year, monthOfYear, dayOfMonth);
                        dismiss();
                    }
                });
    }
}
