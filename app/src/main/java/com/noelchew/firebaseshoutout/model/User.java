package com.noelchew.firebaseshoutout.model;

import com.google.firebase.database.Exclude;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by noelchew on 25/11/2016.
 */

public class User {
    private String id;
    private String name;
    private String fcmUserDeviceId;
    private String email;
    private String profileImageUrl;
    private HashMap<String, Boolean> subscriptions;
    private HashMap<String, Boolean> topics;

    public User() {
    }

    public User(String id, String name, String fcmUserDeviceId, String email, String profileImageUrl) {
        this.id = id;
        this.name = name;
        this.fcmUserDeviceId = fcmUserDeviceId;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.subscriptions = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFcmUserDeviceId() {
        return fcmUserDeviceId;
    }

    public void setFcmUserDeviceId(String fcmUserDeviceId) {
        this.fcmUserDeviceId = fcmUserDeviceId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public HashMap<String, Boolean> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(HashMap<String, Boolean> subscriptions) {
        this.subscriptions = subscriptions;
    }

    @Exclude
    public ArrayList<String> getSubscriptionArrayList() {
        ArrayList<String> subscribedTopicIdArrayList = new ArrayList<>();
        if (subscriptions != null && !subscriptions.isEmpty()) {
            for (Map.Entry<String, Boolean> entry : subscriptions.entrySet()) {
                subscribedTopicIdArrayList.add(entry.getKey());
            }
        }
        return subscribedTopicIdArrayList;
    }

    @Exclude
    public int getSubscriptionCount() {
        if (subscriptions != null && !subscriptions.isEmpty()) {
            return subscriptions.size();
        } else {
            return 0;
        }
    }

    public HashMap<String, Boolean> getTopics() {
        return topics;
    }

    public void setTopics(HashMap<String, Boolean> topics) {
        this.topics = topics;
    }

    @Exclude
    public ArrayList<String> getTopicsArrayList() {
        ArrayList<String> topicsArrayList = new ArrayList<>();
        if (topics != null && !topics.isEmpty()) {
            for (Map.Entry<String, Boolean> entry : topics.entrySet()) {
                topicsArrayList.add(entry.getKey());
            }
        }
        return topicsArrayList;
    }

    @Exclude
    public int getTopicCount() {
        if (topics != null && !topics.isEmpty()) {
            return topics.size();
        } else {
            return 0;
        }
    }

    @Exclude
    public String toJson() {
        return new Gson().toJson(this);
    }

    @Exclude
    public static User fromJson(String json) {
        return new Gson().fromJson(json, User.class);
    }
}
