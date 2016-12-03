package com.noelchew.firebaseshoutout.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by noelchew on 29/11/2016.
 */

public class ShoutOut {
    private String id;
    private String userId;
    private String message;
    private HashMap<String, Object> dateCreated;
    private HashMap<String, Boolean> likes;

    public ShoutOut() {
    }

    public ShoutOut(String id, String userId, String message) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.dateCreated = new HashMap<String, Object>();
        this.dateCreated.put("date", ServerValue.TIMESTAMP);
        this.likes = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public HashMap<String, Object> getDateCreated() {
        return dateCreated;
    }

    @Exclude
    public long getDateCreatedInLong() {
        return (long) dateCreated.get("date");
    }

    public void setDateCreated(HashMap<String, Object> dateCreated) {
        this.dateCreated = dateCreated;
    }

    public HashMap<String, Boolean> getLikes() {
        return likes;
    }

    public void setLikes(HashMap<String, Boolean> likes) {
        this.likes = likes;
    }

    @Exclude
    public ArrayList<String> getLikeArrayList() {
        ArrayList<String> _likes = new ArrayList<>();
        if (likes != null && !likes.isEmpty()) {
            for (Map.Entry<String, Boolean> entry : likes.entrySet()) {
                _likes.add(entry.getKey());
            }
        }
        return _likes;
    }

    @Exclude
    public int getLikeCount() {
        if (likes != null && !likes.isEmpty()) {
            return likes.size();
        } else {
            return 0;
        }
    }

    @Exclude
    public String toJson() {
        return new Gson().toJson(this);
    }

    @Exclude
    public static ShoutOutTopic fromJson(String json) {
        return new Gson().fromJson(json, ShoutOutTopic.class);
    }
}
