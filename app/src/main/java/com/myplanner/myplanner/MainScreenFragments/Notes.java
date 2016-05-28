package com.myplanner.myplanner.MainScreenFragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.myplanner.myplanner.R;

import java.util.ArrayList;
import java.util.List;

public class Notes extends Fragment {
    private List<String> titles = new ArrayList<>();
    private List<List<String>> tags = new ArrayList<>();
    private List<String> bodies = new ArrayList<>();
    private List<Integer> ids = new ArrayList<>();
    private List<String> possibleTags = new ArrayList<>();
    private String currentTag = ""; // an empty tag means any tag is valid
    private NoteRecycleViewAdapter adapter;
    private boolean hasItemBeenSelected; // an ugly flag that is neccesary

    // ---------------------------------------------------------------------------------------------
    // ----------------------------------------- Interface -----------------------------------------
    // ---------------------------------------------------------------------------------------------

    NotesInterface mCallback;
    public interface NotesInterface {
        void noteClickedAction(int id);
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
        changeFilterTag(currentTag);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void clearNoteLists() {
        titles.clear();
        tags.clear();
        bodies.clear();
        ids.clear();
        possibleTags.clear();
    }

    public void addNoteInfo(String title, List<String> tagList, String body, int id) {
        titles.add(title);
        tags.add(tagList);
        bodies.add(body);
        ids.add(id);
        if (tagList != null) {
            for (int i = 0; i < tagList.size(); ++i) {
                if (!possibleTags.contains(tagList.get(i))) {
                    possibleTags = addAlphabetically(possibleTags, tagList.get(i));
                }
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
        if (!newTag.equals(currentTag)) {
            if (newTag.equals(getResources().getString(R.string.notes_no_filter_item))) {
                currentTag = "";
            } else {
                currentTag = newTag;
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private boolean containsFilterTag(List<String> tagList) {
        if (tagList == null || tagList.size() == 0) {
            return currentTag.equals("");
        }
        return tagList.contains(currentTag) || currentTag.equals("");
    }

    private List<String> addAlphabetically(final List<String> list, final String string) {
        int index = list.size();
        if (index == 0) {
            list.add(0, string);
            return list;
        }
        list.add(index, list.get(index - 1));

        for (; index > 0; --index) {
            list.set(index, list.get(index - 1));
            // if the current list item comes before the string alphabetically
            if (list.get(index).compareToIgnoreCase(string) < 0) {
                break;
            }
        }

        list.set(index, string);
        return list;
    }

    private void addButtonLayoutAlphabetically(LinearLayout tagHolder, FrameLayout buttonHolder, String buttonTag) {
        int index = 0;
        for (; index < tagHolder.getChildCount(); ++index) {
            // if the button we are looking at comes before the button we are inserting alphabetically
            final FrameLayout currentButtonHolder = (FrameLayout) tagHolder.getChildAt(index);
            final String holderTag = ((Button) currentButtonHolder.findViewById(R.id.button)).getText().toString();
            if (buttonTag.compareToIgnoreCase(holderTag) < 0) {
                break;
            }
        }
        tagHolder.addView(buttonHolder, index);
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
            private HorizontalScrollView button_holder_scroll_view;
            private TextView body;
            private Spinner noteFilterTagSelector;
            private int id;

            private ViewHolder(View nview, int viewType) {
                super(nview);
                view = nview;
                if (viewType == 0) {
                    noteFilterTagSelector = (Spinner) view.findViewById(R.id.note_filter_tag_selector);
                } else {
                    title = (TextView) view.findViewById(R.id.create_note_title_edit_txt);
                    tag_holder = (LinearLayout) view.findViewById(R.id.note_button_holder_layout);
                    button_holder_scroll_view = (HorizontalScrollView) view.findViewById(R.id.note_button_holder_scroll_view);
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
            return new ViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            if (getItemViewType(position) == 0) {// create the first filter menu
                // generate the items in the spinner
                String[] listItems;
                if (possibleTags == null) {
                    listItems = new String[1];
                    listItems[0] = getResources().getString(R.string.notes_no_filter_item);
                } else if (currentTag.equals("")) {
                    int numItems = 1 + possibleTags.size();
                    listItems = new String[numItems];
                    listItems[0] = getResources().getString(R.string.notes_no_filter_item);
                    // add all of the tags to the list used to populate the tag selector
                    for (int i = 1; i < numItems; ++i) {
                        listItems[i] = possibleTags.get(i - 1);
                    }
                } else {
                    int numItems = 1 + possibleTags.size();
                    listItems = new String[numItems];
                    listItems[0] = currentTag;
                    listItems[1] = getResources().getString(R.string.notes_no_filter_item);

                    int i = 2;
                    // add all of the tags before the currently selected tag to the list used to
                    //   populate the tag selector
                    for (; i < numItems && !possibleTags.get(i - 2).equals(currentTag); ++i) {
                        listItems[i] = possibleTags.get(i - 2);
                    }
                    // add in all of the tags after the currently selected tag to the list used to
                    //   populate the tag selector
                    for (; i < numItems; ++i) {
                        listItems[i] = possibleTags.get(i - 1);
                    }

                }
                ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, listItems);
                hasItemBeenSelected = false;
                holder.noteFilterTagSelector.setAdapter(tagAdapter);
                holder.noteFilterTagSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // this is required because onItemSelected is called immediately when the
                        //   selector is created, which we don't want
                        if (hasItemBeenSelected) {
                            changeFilterTag((String) holder.noteFilterTagSelector.getItemAtPosition(position));
                        } else {
                            hasItemBeenSelected = true;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            } else {
                // decriment the position to account for the first item being a non-note item
                int itemNumber = position - 1;

                // increment the number once for every note that does not pass the filter because
                //   only items that pass the filter should be shown, and the number of positions
                //   is set by only counting items passing the filter
                for (int i = 0; i <= itemNumber; ++i) {
                    if (!containsFilterTag(tags.get(i))) {
                        itemNumber++;
                    }
                }

                holder.title.setText(titles.get(itemNumber));
                holder.body.setText(bodies.get(itemNumber));
                holder.id = ids.get(itemNumber);
                holder.tag_holder.removeAllViews();

                final int numTags = tags.get(itemNumber).size();
                final RelativeLayout.LayoutParams btnHolderParams;
                // hide the tag holder if there are no tags so there isn't an ugly gap
                if (numTags == 0) {
                    holder.button_holder_scroll_view.setVisibility(View.GONE);
                } else {
                    holder.button_holder_scroll_view.setVisibility(View.VISIBLE);
                }

                for (int i = 0; i < numTags; ++i) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    final FrameLayout buttonLayout = (FrameLayout) inflater.inflate(R.layout.button_tag_layout, null);
                    final Button button = ((Button)buttonLayout.findViewById(R.id.button));
                    button.setText(tags.get(itemNumber).get(i));

                    final int index1 = itemNumber;
                    final int index2 = i;
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onTagClicked(tags.get(index1).get(index2));
                        }
                    });

                    addButtonLayoutAlphabetically(holder.tag_holder, buttonLayout, tags.get(itemNumber).get(i));
                }
            }
        }

        @Override
        public int getItemCount() {
            // the extra 1 is for the filter item which is always in the list
            int count = 1;
            // loop through all of the items, and check if any of their tags pass the filter, ]
            //   incrementing count if they do
            for (int i = 0; i < tags.size(); ++i) {
                if (containsFilterTag(tags.get(i))) {
                    count++;
                }
            }
            return count;
        }
    }
}
