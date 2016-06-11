package com.myplanner.myplanner.UserData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlannerNote {
    private String title;
    private String body;
    private List<String> tags = new ArrayList<>();
    private int id;

    public PlannerNote(List<String> ntags, String ntitle, String nbody, int nID) {
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
        if (tags != null && index < tags.size()) {
            return tags.get(index);
        } else {
            return "";
        }
    }

    public int getNumTags() {
        if (tags == null) {
            return 0;
        }
        return tags.size();
    }

    public int getID() {return id;}

    //----------------------------------------------------------------------------------------------
    //------------------------------------------ Setters -------------------------------------------
    // ---------------------------------------------------------------------------------------------

    public void setTitle(String ntitle) {title = ntitle;}

    public void setBody(String nbody) {body = nbody;}

    public void addTag(String tag) {tags.add(tag);}

    public void removeTag(String tag) {tags.remove(tag);}
}
