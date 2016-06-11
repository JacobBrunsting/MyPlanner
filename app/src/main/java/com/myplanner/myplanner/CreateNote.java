package com.myplanner.myplanner;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.myplanner.myplanner.UserData.DataRetriever;
import com.myplanner.myplanner.UserData.PlannerNote;

import java.util.ArrayList;
import java.util.List;

public class CreateNote extends AppCompatActivity implements AddNoteTagDialogFragment.addNoteTagDialogInterface {
    private DataRetriever userData;
    private ArrayList<String> possibleTags = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private int id;

    // ---------------------------------------------------------------------------------------------
    // -------------------------------------- Public Functions -------------------------------------
    // ---------------------------------------------------------------------------------------------

    public void addNewTag(final String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
            final LinearLayout tagHolder = (LinearLayout) findViewById(R.id.tag_holder);
            LayoutInflater inflater = getLayoutInflater();
            final FrameLayout buttonLayout = (FrameLayout) inflater.inflate(R.layout.button_create_tag_layout, null);
            final Button button = (Button) buttonLayout.findViewById(R.id.button);
            button.setText(tag);

            final Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setDuration(200);
            button.startAnimation(fadeIn);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Animation fadeOut = new AlphaAnimation(1, 0);
                    fadeOut.setDuration(200);
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            tagHolder.removeView(buttonLayout);
                            tags.remove(tag);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                    button.startAnimation(fadeOut);
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

    // ---------------------------------------------------------------------------------------------
    // ------------------------------------ Override Functions -------------------------------------
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        // retrieve the data passed from the previous activity
        Bundle passedData = getIntent().getExtras();
        id = passedData.getInt(Main.ID_TAG);
        possibleTags = (ArrayList<String>) passedData.getSerializable(Main.POSSIBLE_TAGS_TAG);
        userData = DataRetriever.getInstance();

        // configure the add tag button
        final Button addTagBtn = (Button) findViewById(R.id.add_tag_button);
        addTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTag();
            }
        });

        // set up the toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.create_note_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // configure the bottom bar buttons
        final Button cancelBtn = (Button) findViewById(R.id.cancel_button);
        final Button saveBtn = (Button) findViewById(R.id.save_button);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertNote();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.create_note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------------- Private Functions -------------------------------------
    // ---------------------------------------------------------------------------------------------

    private void insertNote() {
        final String title = ((EditText)findViewById(R.id.title_edit_text)).getText().toString();
        final String body = ((EditText)findViewById(R.id.body_edit_text)).getText().toString();
        final PlannerNote note = new PlannerNote(tags, title, body, id);
        userData.addNote(note);
    }

    private void addTag() {
        DialogFragment addTagDialog = new AddNoteTagDialogFragment();
        addTagDialog.show(getSupportFragmentManager(), "AddNoteTagDialog");
    }
}
