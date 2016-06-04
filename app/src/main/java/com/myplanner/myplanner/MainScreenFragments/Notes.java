package com.myplanner.myplanner.MainScreenFragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.myplanner.myplanner.R;

import java.util.ArrayList;
import java.util.List;

public class Notes extends Fragment {
    private final String NO_FILTER_TAG = "";

    private List<String> titles = new ArrayList<>();
    private List<List<String>> tags = new ArrayList<>();
    private List<String> bodies = new ArrayList<>();
    private List<Integer> ids = new ArrayList<>();
    private List<String> possibleTags = new ArrayList<>();
    private String currentTag = NO_FILTER_TAG;
    private NoteRecycleViewAdapter adapter;
    private boolean hasItemBeenSelected; // an ugly flag that is necessary

    // ---------------------------------------------------------------------------------------------
    // ----------------------------------------- Interface -----------------------------------------
    // ---------------------------------------------------------------------------------------------

    NotesInterface mCallback;
    public interface NotesInterface {
        void noteClickedAction(int id);
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------------- Public Functions --------------------------------------
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
            // add every tag to the list of possible tags, in alphabetical order, if it is not
            //   already in the list
            for (int i = 0; i < tagList.size(); ++i) {
                if (!possibleTags.contains(tagList.get(i))) {
                    possibleTags = addAlphabetically(possibleTags, tagList.get(i));
                }
            }
        }
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
    // ------------------------------------ Private Functions --------------------------------------
    // ---------------------------------------------------------------------------------------------

    private void onTagClicked(String tag) {
        changeFilterTag(tag);
    }

    private void changeFilterTag(String newTag) {
        if (!newTag.equals(currentTag)) {
            if (newTag.equals(getResources().getString(R.string.notes_no_filter_item))) {
                currentTag = NO_FILTER_TAG;
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
            return currentTag.equals(NO_FILTER_TAG);
        }
        return tagList.contains(currentTag) || currentTag.equals(NO_FILTER_TAG);
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
    // ----------------------------------- RecyclerView Adapter ------------------------------------
    // ---------------------------------------------------------------------------------------------

    class NoteRecycleViewAdapter extends RecyclerView.Adapter<NoteRecycleViewAdapter.ViewHolder> {

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
                    noteFilterTagSelector = (Spinner) view.findViewById(R.id.tag_selector);
                } else {
                    title = (TextView) view.findViewById(R.id.title_edit_text);
                    tag_holder = (LinearLayout) view.findViewById(R.id.tag_holder);
                    button_holder_scroll_view = (HorizontalScrollView) view.findViewById(R.id.tag_layout);
                    body = (TextView) view.findViewById(R.id.body_text);
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
            if (getItemViewType(position) == 0) {// create the tag filter item
                // generate the items in the spinner
                String[] listItems;
                if (possibleTags == null) {
                    // if there are no tags on any of the notes, just put the 'no filter' option
                    //   in the spinner
                    listItems = new String[1];
                    listItems[0] = getResources().getString(R.string.notes_no_filter_item);
                } else if (currentTag.equals(NO_FILTER_TAG)) {
                    // if the current tag is NO_FILTER_TAG, we want the first item in the spinner to
                    //   be the no filter item, and the rest of the items to be the possible tags
                    final int numItems = 1 + possibleTags.size();
                    listItems = new String[numItems];
                    listItems[0] = getResources().getString(R.string.notes_no_filter_item);

                    // add all of the tags to the list used to populate the tag selector
                    for (int i = 1; i < numItems; ++i) {
                        listItems[i] = possibleTags.get(i - 1);
                    }
                } else {
                    // if the current tag is an actual tag, then we want the first option to be the
                    //   current tag, the second to be the 'no filter' option, and the rest to be
                    //   the other tags
                    final int numItems = Math.max(1 + possibleTags.size(), 2);
                    listItems = new String[numItems];

                    // set the first item to the currently selected tag, and the second item to the
                    //   no filter button
                    listItems[0] = currentTag;
                    listItems[1] = getResources().getString(R.string.notes_no_filter_item);

                    // add all of the tags before the currently selected tag to the list used to
                    //   populate the tag selector
                    int i = 2;
                    for (; i < numItems && !possibleTags.get(i - 2).equals(currentTag); ++i) {
                        listItems[i] = possibleTags.get(i - 2);
                    }

                    // add in all of the tags after the currently selected tag to the list used to
                    //   populate the tag selector
                    for (; i < numItems; ++i) {
                        listItems[i] = possibleTags.get(i - 1);
                    }

                }
                final ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, listItems);
                holder.noteFilterTagSelector.setAdapter(tagAdapter);
                hasItemBeenSelected = false;
                holder.noteFilterTagSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // this is required because onItemSelected is called immediately when the
                        //   selector is created, which we don't want, so we have a flag to make
                        //   sure the first onItemSelected call is ignored
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
                // decrement the position to account for the first item being the tag filter
                int itemNumber = position - 1;

                // increment the number once for every note that does not pass the filter, because
                //   only items that pass the filter should be shown, so we skip over all fails
                for (int i = 0; i <= itemNumber; ++i) {
                    if (!containsFilterTag(tags.get(i))) {
                        itemNumber++;
                    }
                }

                holder.title.setText(titles.get(itemNumber));
                holder.body.setText(bodies.get(itemNumber));
                holder.id = ids.get(itemNumber);
                holder.tag_holder.removeAllViews();

                // collapse the tag holder if there are no tags so there isn't an ugly gap
                final ViewGroup.LayoutParams btnHolderParams = holder.button_holder_scroll_view.getLayoutParams();
                final int numTags = tags.get(itemNumber).size();
                if (numTags == 0) {
                    btnHolderParams.height = 0;
                } else {
                    btnHolderParams.height = (int) getActivity().getResources().getDimension(R.dimen.notes_tag_btn_height);
                }
                holder.button_holder_scroll_view.setLayoutParams(btnHolderParams);

                // create a button for every tag attached to the note, and put it in the button
                //   holder
                for (int i = 0; i < numTags; ++i) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    final FrameLayout buttonLayout = (FrameLayout) inflater.inflate(R.layout.button_tag_layout, null);
                    final Button button = ((Button)buttonLayout.findViewById(R.id.button));
                    button.setText(tags.get(itemNumber).get(i));

                    // change the filter to the tag attached to the selected button when clicked
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

            // loop through all of the items, and check if any of their tags pass the filter,
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
