package com.myplanner.myplanner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.myplanner.myplanner.UserData.DataRetriever;
import com.myplanner.myplanner.UserData.PlannerNote;

import java.util.ArrayList;

public class CreateNote extends AppCompatActivity {
    DataRetriever userData;
    ArrayList<String> tags = new ArrayList<>();
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        Bundle passedData = getIntent().getExtras();
        id = passedData.getInt("ID");
        userData = DataRetriever.getInstance();

        // configure the add tag button
        final Button addTagBtn = (Button) findViewById(R.id.create_note_add_tag_btn);

        addTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTag();
            }
        });

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

    }
}
