package com.myplanner.myplanner;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AddNoteTagDialogFragment extends DialogFragment {

    ArrayList<String> possibleTags = new ArrayList<>();
    View dialogView;

    addNoteTagDialogInterface mCallback;
    public interface addNoteTagDialogInterface {
        ArrayList<String> getPossibleTags();
        void addNewTag(String tag);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (addNoteTagDialogInterface) context;
        possibleTags = mCallback.getPossibleTags();

        // if dialogView is not null, meaning the view has been created, set up the activity
        if (dialogView != null) {
            setUpDialog();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        dialogView = inflater.inflate(R.layout.fragment_add_note_tag_dialog, container, false);
        // if mCallback is not null, meaning onAttach has been called, and possibleTags is populated,
        //   set up the dialog
        if (mCallback != null) {
            setUpDialog();
        }
        return dialogView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    private void setUpDialog() {
        final AutoCompleteTextView tagSelect = (AutoCompleteTextView) dialogView.findViewById(R.id.add_note_tag_txt_view);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, possibleTags);
        tagSelect.showDropDown();
        tagSelect.setAdapter(adapter);
        tagSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCallback.addNewTag(tagSelect.getText().toString().toUpperCase());
                dismiss();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                dismiss();
            }
        });

        final Button acceptTag = (Button) dialogView.findViewById(R.id.accept_note_tag_btn);
        acceptTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.addNewTag(tagSelect.getText().toString().toUpperCase());
                dismiss();
            }
        });
    }
}
