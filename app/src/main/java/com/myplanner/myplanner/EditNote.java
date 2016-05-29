package com.myplanner.myplanner;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.myplanner.myplanner.UserData.DataRetriever;
import com.myplanner.myplanner.UserData.PlannerNote;

import java.util.ArrayList;
import java.util.List;

public class EditNote extends AppCompatActivity implements AddNoteTagDialogFragment.addNoteTagDialogInterface {
    private Menu menu;
    private DataRetriever userData;
    private EditText titleEditText;
    private EditText bodyEditText;
    private Button addTagBtn;
    private LinearLayout tagHolder;
    private LinearLayout bottomButtonHolder;
    private Toolbar toolbar;
    private int id;
    private ArrayList<String> possibleTags = new ArrayList<>();
    private List<String> tags = new ArrayList<>();

    // ---------------------------------------------------------------------------------------------
    // -------------------------------------- Public Functions -------------------------------------
    // ---------------------------------------------------------------------------------------------

    public void addNewTag(final String tag) {
        tagHolder.setVisibility(View.VISIBLE);
        if (!tags.contains(tag)) {
            tags.add(tag);
            LayoutInflater inflater = getLayoutInflater();
            final FrameLayout buttonLayout = (FrameLayout) inflater.inflate(R.layout.button_create_tag_layout, null);
            final Button button = (Button) buttonLayout.findViewById(R.id.button);
            button.setText(tag);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // only allow tag removal in edit mode (where the add tag button is visible)
                    if (addTagBtn.getVisibility() == View.VISIBLE) {
                        tagHolder.removeView(buttonLayout);
                        tags.remove(tag);
                    }
                }
            });

            tagHolder.addView(buttonLayout);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ----------------------- AddNoteTagDialogFragment Interface Functions ------------------------
    // ---------------------------------------------------------------------------------------------

    public List<String> getPossibleTags() {
        return possibleTags;
    }

    //----------------------------------------------------------------------------------------------
    //------------------------------------- Override Functions -------------------------------------
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        userData = DataRetriever.getInstance();

        // store the elements of the activity
        titleEditText = (EditText) findViewById(R.id.title_edit_text);
        bodyEditText = (EditText) findViewById(R.id.body_edit_text);
        tagHolder = (LinearLayout) findViewById(R.id.button_holder_layout);
        bottomButtonHolder = (LinearLayout) findViewById(R.id.bottom_menu_button_holder);
        addTagBtn = (Button) findViewById(R.id.add_tag_btn);

        // get the information passed from the previous activity
        Bundle passedData = getIntent().getExtras();
        id = passedData.getInt("id");
        possibleTags = (ArrayList<String>) passedData.getSerializable("possibleTags");

        // display the different elements of the note being edited in the activity elements
        final PlannerNote note = userData.getNoteByID(id);
        titleEditText.setText(note.getTitle());
        bodyEditText.setText(note.getBody());
        for (int i = 0; i < note.getNumTags(); ++i) {
            addNewTag(note.getTag(i));
        }
        if (tags.isEmpty()) {
            setTagHolderVisible(false);
        } else {
            setTagHolderVisible(true);
        }

        // set up the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("View Note");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set up the bottom bar
        final Button cancelBtn = (Button) findViewById(R.id.cancel_btn);
        final Button saveBtn = (Button) findViewById(R.id.save_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
                finish();
            }
        });

        addTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTag();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.edit_note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.enter_edit_mode_menu_button:
                // change the icon displayed on the toolbar
                menu.findItem(R.id.exit_edit_mode_menu_button).setVisible(true);
                menu.findItem(R.id.enter_edit_mode_menu_button).setVisible(false);

                // make the text fields editable
                titleEditText.setEnabled(true);
                bodyEditText.setEnabled(true);

                // show the save, cancel, and add tag buttons
                bottomButtonHolder.setVisibility(View.VISIBLE);
                addTagBtn.setVisibility(View.VISIBLE);

                // show the tag holder in case it was hidden
                setTagHolderVisible(true);

                toolbar.setTitle("Edit Note");
                toolbar.invalidate();
                break;
            case R.id.exit_edit_mode_menu_button:
                // change the icon displayed on the toolbar
                menu.findItem(R.id.exit_edit_mode_menu_button).setVisible(false);
                menu.findItem(R.id.enter_edit_mode_menu_button).setVisible(true);

                // make the text fields view-only
                titleEditText.setEnabled(false);
                bodyEditText.setEnabled(false);

                // hide the save, cancel, and add tag buttons
                bottomButtonHolder.setVisibility(View.INVISIBLE);
                addTagBtn.setVisibility(View.GONE);

                // collapse the tag holder if there are no tags to show
                if (tags.isEmpty()) {
                    setTagHolderVisible(false);
                }

                toolbar.setTitle("View Note");
                toolbar.invalidate();
                saveChanges();
                break;
            case R.id.delete_menu_button:
                userData.removeNote(id);
                finish();
                break;
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //----------------------------------------------------------------------------------------------
    //------------------------------------- Private Functions --------------------------------------
    //----------------------------------------------------------------------------------------------

    // delete the note that was being edited, and use the fields to create a new one with the same
    //   id
    private void saveChanges() {
        userData.removeNote(id);
        PlannerNote newNote = new PlannerNote(tags, titleEditText.getText().toString(),
                                              bodyEditText.getText().toString(), id);
        userData.addNote(newNote);
    }

    // start the add tag dialog
    private void addTag() {
        DialogFragment addTagDialog = new AddNoteTagDialogFragment();
        addTagDialog.show(getSupportFragmentManager(), "AddNoteTagDialog");
    }

    // show the tags if visible is true, or collapse the tag holder, shifting the other activity
    //   elements up, if it is false
    private void setTagHolderVisible(final boolean visible) {
        final HorizontalScrollView tagHolderScrollView = (HorizontalScrollView) findViewById(R.id.tag_holder_scroll_view);
        final ViewGroup.LayoutParams params = tagHolderScrollView.getLayoutParams();

        if (visible) {
            params.height = (int) getResources().getDimension(R.dimen.notes_create_tag_btn_height);
        } else {
            params.height = 0;
        }

        tagHolderScrollView.setLayoutParams(params);
    }
}
