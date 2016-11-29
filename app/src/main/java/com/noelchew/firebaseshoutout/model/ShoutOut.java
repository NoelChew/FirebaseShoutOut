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
    private String message;
    private HashMap<String, Object> dateCreated;
    private HashMap<String, User> likes;

    public ShoutOut() {
    }

    public ShoutOut(String id, String message) {
        this.id = id;
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

    public HashMap<String, User> getLikes() {
        return likes;
    }

    public void setLike(HashMap<String, User> likes) {
        this.likes = likes;
    }

    @Exclude
    public ArrayList<User> getLikeArrayList() {
        ArrayList<User> _likes = new ArrayList<>();
        if (likes != null && !likes.isEmpty()) {
            for (Map.Entry<String, User> entry : likes.entrySet()) {
                _likes.add(entry.getValue());
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
