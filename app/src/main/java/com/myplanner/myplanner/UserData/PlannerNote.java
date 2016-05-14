package com.myplanner.myplanner.UserData;

import java.io.Serializable;
import java.util.ArrayList;

public class PlannerNote {
    String title;
    String body;
    ArrayList<String> tags;
    int id;

    public PlannerNote(ArrayList<String> ntags, String ntitle, String nbody, int nID) {
        title = ntitle;
        body = nbody;
        tags = ntags;
        id = nID;
    }

    //----------------------------------------------------------------------------------------------
    //------------------------------------------ Getters -------------------------------------------
    // ---------------------------------------------------------------------------------------------

    public String getTitle() {return title;}

    public String getBody() {return body;}

    public String getTag(int index) {
        if (index < tags.size()) {
            return tags.get(index);
        } else {
            return "";
        }
    }

    public int getNumTags() {return tags.size();}

    public int getID() {return id;}

    //----------------------------------------------------------------------------------------------
    //------------------------------------------ Setters -------------------------------------------
    // ---------------------------------------------------------------------------------------------

    public void setTitle(String ntitle) {title = ntitle;}

    public void setBody(String nbody) {body = nbody;}

    public void addTag(String tag) {tags.add(tag);}

    public void removeTag(String tag) {tags.remove(tag);}
}
