package com.myplanner.myplanner.MainScreenFragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.myplanner.myplanner.R;

import java.util.ArrayList;

public class Notes extends Fragment {
    private ArrayList<String> titles = new ArrayList<>();
    private ArrayList<ArrayList<String>> tags = new ArrayList<>();
    private ArrayList<String> bodies = new ArrayList<>();
    private ArrayList<Integer> ids = new ArrayList<>();
    private ArrayList<String> possibleTags = new ArrayList<>();
    private ArrayList<Boolean> hasCorrectTag = new ArrayList<>();
    private int numNotesPassedFilter = 0;
    private String currentTag = ""; // an empty tag means any tag is valid
    String[] spinnerItems;
    int numSpinnerItems;

    NoteRecycleViewAdapter adapter;

    // ---------------------------------------------------------------------------------------------
    // ----------------------------------------- Interface -----------------------------------------
    // ---------------------------------------------------------------------------------------------

    NotesInterface mCallback;
    public interface NotesInterface {
        void noteClickedAction(int id);
        void tagClickedAction(String tag);
    }

    // ---------------------------------------------------------------------------------------------
    // -------------------------------- Fragment Override Functions --------------------------------
    // ---------------------------------------------------------------------------------------------

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (NotesInterface) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView rv = (RecyclerView) inflater.inflate(R.layout.basic_recyclerview, container, false);
        adapter = new NoteRecycleViewAdapter();
        rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));
        rv.setAdapter(adapter);
        changeFilterTag(currentTag);
        return rv;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    // ---------------------------------------------------------------------------------------------
    // --------------------------------- Functions Called by Main ----------------------------------
    // ---------------------------------------------------------------------------------------------

    public void reloadData() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void clearNoteArrays() {
        titles.clear();
        tags.clear();
        bodies.clear();
        ids.clear();
        hasCorrectTag.clear();
        numNotesPassedFilter = 0;
        possibleTags.clear();
    }

    public void addNoteInfo(String title, ArrayList<String> tagList, String body, int id) {
        titles.add(title);
        tags.add(tagList);
        bodies.add(body);
        ids.add(id);
        if (tagList.contains(currentTag) || currentTag.equals("")) {
            hasCorrectTag.add(true);
            numNotesPassedFilter++;
        } else {
            hasCorrectTag.add(false);
        }
        for (int i = 0; i < tagList.size(); ++i) {
            if (!possibleTags.contains(tagList.get(i))) {
                possibleTags.add(tagList.get(i));
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // --------------------------------- Private Helper Functions ----------------------------------
    // ---------------------------------------------------------------------------------------------

    private void onTagClicked(String tag) {
        changeFilterTag(tag);
    }

    private void changeFilterTag(String newTag) {
        numNotesPassedFilter = 0;
        currentTag = newTag;
        for (int i = 0; i < hasCorrectTag.size(); ++i) {
            if (containsFilterTag(tags.get(i))) {
                hasCorrectTag.set(i, true);
                numNotesPassedFilter++;
            } else {
                hasCorrectTag.set(i, false);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private boolean containsFilterTag(ArrayList<String> tagList) {
        return tagList.contains(currentTag) || currentTag.equals("");
    }

    // ---------------------------------------------------------------------------------------------
    // -------------------------- Local Adapter Required for RecycleView ---------------------------
    // ---------------------------------------------------------------------------------------------

    class NoteRecycleViewAdapter extends RecyclerView.Adapter<NoteRecycleViewAdapter.ViewHolder> {

        // class to store the information for one element in the RecycleView
        class ViewHolder extends RecyclerView.ViewHolder {
            private View view;
            private TextView title;
            private LinearLayout tag_holder;
            private TextView body;
            private Spinner noteFilterTagSelector;
            private int id;

            private ViewHolder(View nview) {
                super(nview);
                view = nview;
                if (getItemViewType() == 0) {
                    noteFilterTagSelector = (Spinner) view.findViewById(R.id.note_filter_tag_selector);
                } else {
                    title = (TextView) view.findViewById(R.id.create_note_title_edit_txt);
                    tag_holder = (LinearLayout) view.findViewById(R.id.note_button_holder_layout);
                    body = (TextView) view.findViewById(R.id.note_body_txt);
                    id = -1;

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCallback.noteClickedAction(id);
                        }
                    });
                }
            }
        }

        private NoteRecycleViewAdapter() {
            this.notifyDataSetChanged();
        }

        // this allows the first item to have a different layout. viewType 0 is the filter, where
        //   you select what tags you want to show, and viewType 1 is the regular note
        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if (viewType == 0) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_note_list_filter_item, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_note_list_item, parent, false);
            }
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            if (position == 0) {// create the first filter menu
                /*Spinner spinner = holder.noteFilterTagSelector;
                // generate the items in the spinner
                int numItems = 1 + possibleTags.size();
                final String[] listItems = new String[numItems];
                listItems[0] = getResources().getString(R.string.notes_no_filter_item);
                for (int i = 1; i < numItems; ++i) {
                    listItems[i] = possibleTags.get(i - 1);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, listItems);
                holder.noteFilterTagSelector.setAdapter(adapter);
                holder.noteFilterTagSelector.setPrompt(listItems[0]);
                holder.noteFilterTagSelector.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            changeFilterTag("");
                        } else {
                            changeFilterTag(listItems[position - 1]);
                        }
                        holder.noteFilterTagSelector.setPrompt(listItems[position]);
                    }
                });*/
            } else {
                // decriment the position to account for the first item being a non-note item
                int itemNumber = position - 1;
                // increment the number once for every note that does not pass the filter because
                //   only items that pass the filter should be shown, and the number of positions
                //   is set by only counting items passing the filter
                for (int i = 0; i < itemNumber; ++i) {
                    if (!containsFilterTag(tags.get(i))) {
                        itemNumber++;
                    }
                }
                holder.title.setText(titles.get(itemNumber));
                holder.body.setText(bodies.get(itemNumber));
                holder.id = ids.get(itemNumber);
                holder.tag_holder.removeAllViews();

                for (int i = 0; i < tags.get(itemNumber).size(); ++i) {
                    Button newButton = new Button(getContext());
                    newButton.setText(tags.get(itemNumber).get(i));
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    newButton.setLayoutParams(params);
                    newButton.setTextSize(getResources().getDimension(R.dimen.notes_tag_btn_text_size));

                    final int index1 = itemNumber;
                    final int index2 = i;
                    newButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onTagClicked(tags.get(index1).get(index2));
                        }
                    });

                    holder.tag_holder.addView(newButton);
                }
            }
        }

        @Override
        public int getItemCount() {
            // the extra 1 is for the filter item which is always in the list
            return numNotesPassedFilter + 1;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------- Local Adapter Required for Spinner -----------------------------
    // ---------------------------------------------------------------------------------------------

    //private class noteSpinnerAdapter extends ArrayAdapter {
// use the possibleTags array to populate spinner
   // }
}
