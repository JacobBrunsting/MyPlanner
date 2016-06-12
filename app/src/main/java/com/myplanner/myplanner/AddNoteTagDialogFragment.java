package com.myplanner.myplanner;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AddNoteTagDialogFragment extends DialogFragment {

    List<String> possibleTags = new ArrayList<>();
    View dialogView;

    // ---------------------------------------------------------------------------------------------
    // ----------------------------------------- Interface -----------------------------------------
    // ---------------------------------------------------------------------------------------------

    addNoteTagDialogInterface mCallback;
    public interface addNoteTagDialogInterface {
        List<String> getPossibleTags();
        void addNewTag(String tag);
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------------ Override Functions -------------------------------------
    // ---------------------------------------------------------------------------------------------

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (addNoteTagDialogInterface) context;
        possibleTags = mCallback.getPossibleTags();

        // if dialogView is not null, meaning the view has been created, set up the activity
        if (dialogView != null) {
            setUpDialog();
        }
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(getActivity().getCurrentFocus(), InputMethodManager.SHOW_FORCED);
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
        // google's soft keyboard functions are terrible, hence, we have this mess
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(getActivity().getCurrentFocus(), InputMethodManager.SHOW_FORCED);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------------- Private Functions -------------------------------------
    // ---------------------------------------------------------------------------------------------

    private void setUpDialog() {
        final AutoCompleteTextView tagSelect = (AutoCompleteTextView) dialogView.findViewById(R.id.tag_edit_text);
        final TagSelectAdapter adapter = new TagSelectAdapter(getContext(), R.layout.auto_complete_list_item, possibleTags);
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

        final Button acceptTag = (Button) dialogView.findViewById(R.id.accept_tag_button);
        acceptTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String tag = tagSelect.getText().toString().toUpperCase();
                if (!tag.equals("")) {
                    mCallback.addNewTag(tagSelect.getText().toString().toUpperCase());
                }
                dismiss();
            }
        });
    }

    private class TagSelectAdapter extends ArrayAdapter<String> {
        private List<String> possibleTags;
        private int resourceID;

        private TagSelectAdapter(final Context context, final int listResourceID, final List<String> possibleTagsList) {
            super(context, listResourceID, possibleTagsList);
            resourceID = listResourceID;
            possibleTags = possibleTagsList;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(resourceID, null);
            }
            if (possibleTags != null && !possibleTags.isEmpty()) {
                ((TextView) convertView.findViewById(R.id.text)).setText(possibleTags.get(position));
            }
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    final List<String> filterResultsList = new ArrayList<>();
                    for (String tag : possibleTags) {
                        if (tag.toUpperCase().startsWith(constraint.toString().toUpperCase())) {
                            filterResultsList.add(tag);
                        }
                    }
                    final FilterResults filterResults = new FilterResults();
                    if (!filterResultsList.isEmpty()) {
                        filterResults.values = filterResultsList;
                        filterResults.count = filterResultsList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    final List<String> resultList = (List<String>) results.values;
                    clear();
                    if (resultList != null) {
                        for (String result : resultList) {
                            add(result);
                        }
                        notifyDataSetChanged();
                    }
                }
            };
        }
    }
}
