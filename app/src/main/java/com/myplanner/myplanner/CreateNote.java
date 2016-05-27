package com.myplanner.myplanner;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.myplanner.myplanner.UserData.DataRetriever;
import com.myplanner.myplanner.UserData.PlannerNote;

import java.util.ArrayList;
import java.util.List;

public class CreateNote extends AppCompatActivity implements AddNoteTagDialogFragment.addNoteTagDialogInterface {
    DataRetriever userData;
    ArrayList<String> possibleTags = new ArrayList<>();
    List<String> tags = new ArrayList<>();
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        Bundle passedData = getIntent().getExtras();
        id = passedData.getInt("ID");
        possibleTags = (ArrayList<String>) passedData.getSerializable("possibleTags");
        userData = DataRetriever.getInstance();

        // configure the add tag button
        final Button addTagBtn = (Button) findViewById(R.id.create_note_add_tag_btn);

        addTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTag();
            }
        });

        // set up the toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.create_note_toolbar);
        toolbar.setTitle("Create Note");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // configure the bottom buttons
        final Button cancelBtn = (Button) findViewById(R.id.create_note_cancel_btn);
        final Button saveBtn = (Button) findViewById(R.id.create_note_save_btn);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void insertNote() {
        final String title = ((EditText)findViewById(R.id.create_note_title_edit_txt)).getText().toString();
        final String body = ((EditText)findViewById(R.id.create_note_body_edit_txt)).getText().toString();
        final PlannerNote note = new PlannerNote(tags, title, body, id);
        userData.addNote(note);
    }

    private void addTag() {
        DialogFragment addTagDialog = new AddNoteTagDialogFragment();
        addTagDialog.show(getSupportFragmentManager(), "AddNoteTagDialog");
    }

    // interface for AddNoteTagDialogFragment
    public List<String> getPossibleTags() {
        return possibleTags;
    }

    public void addNewTag(final String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
            final LinearLayout tagHolder = (LinearLayout) findViewById(R.id.note_button_holder_layout);
            LayoutInflater inflater = getLayoutInflater();
            final FrameLayout buttonLayout = (FrameLayout) inflater.inflate(R.layout.button_create_tag_layout, null);
            final Button button = (Button) buttonLayout.findViewById(R.id.button);
            button.setText(tag);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tagHolder.removeView(buttonLayout);
                    tags.remove(tag);
                }
            });

            tagHolder.addView(buttonLayout);
        }
    }
}
