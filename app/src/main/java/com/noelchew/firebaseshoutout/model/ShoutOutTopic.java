package com.noelchew.firebaseshoutout.model;

import android.text.TextUtils;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by noelchew on 26/11/2016.
 */

public class ShoutOutTopic {

    private HashMap<String, Object> dateCreated;
    private User user;
    private String topicId;
    private String topicName;
    private HashMap<String, Object> lastActiveDate;
    private String lastShoutOut; // shout out message
    private HashMap<String, Boolean> subscribers;

    public ShoutOutTopic() {
    }

    public ShoutOutTopic(User user, String topicName) {
        this.dateCreated = new HashMap<String, Object>();
        this.dateCreated.put("date", ServerValue.TIMESTAMP);
        this.lastActiveDate = new HashMap<String, Object>();
        this.lastActiveDate.put("date", ServerValue.TIMESTAMP);
        this.user = user;
        this.topicId = UUID.randomUUID().toString();
        this.topicName = topicName;
        this.subscribers = new HashMap<>();
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public HashMap<String, Object> getLastActiveDate() {
        return lastActiveDate;
    }

    @Exclude
    public long getLastActiveDateInLong() {
        return (long) lastActiveDate.get("date");
    }

    public void setLastActiveDate(HashMap<String, Object> lastActiveDate) {
        this.lastActiveDate = lastActiveDate;
    }

    @Exclude
    public void setLastActiveDateToNow() {
        this.lastActiveDate.put("date", ServerValue.TIMESTAMP);
    }

    public String getLastShoutOut() {
        if (!TextUtils.isEmpty(lastShoutOut)) {
            return lastShoutOut;
        } else {
            return "-";
        }
    }

    public void setLastShoutOut(String lastShoutOut) {
        this.lastShoutOut = lastShoutOut;
    }

    public HashMap<String, Boolean> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(HashMap<String, Boolean> subscribers) {
        this.subscribers = subscribers;
    }

    @Exclude
    public ArrayList<String> getSubscriberArrayList() {
        ArrayList<String> _subscribers = new ArrayList<>();
        if (subscribers != null && !subscribers.isEmpty()) {
            for (Map.Entry<String, Boolean> entry : subscribers.entrySet()) {
                _subscribers.add(entry.getKey());
            }
        }
        return _subscribers;
    }

    @Exclude
    public int getSubscriberCount() {
        if (subscribers != null && !subscribers.isEmpty()) {
            return subscribers.size();
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
