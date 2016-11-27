package com.noelchew.firebaseshoutout.util.fcm.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by noelchew on 26/11/2016.
 */

public class TopicAddedDate {

    @SerializedName("addDate")
    private String dateAdded;

    public TopicAddedDate(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }
}
