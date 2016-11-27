package com.noelchew.firebaseshoutout.model;

import com.google.firebase.database.Exclude;
import com.google.gson.Gson;

/**
 * Created by noelchew on 25/11/2016.
 */

public class User {
    private String id;
    private String name;
    private String fcmUserDeviceId;
    private String email;
    private String profileImageUrl;

    public User() {
    }

    public User(String id, String name, String fcmUserDeviceId, String email, String profileImageUrl) {
        this.id = id;
        this.name = name;
        this.fcmUserDeviceId = fcmUserDeviceId;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
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

    @Exclude
    public String toJson() {
        return new Gson().toJson(this);
    }

    @Exclude
    public static User fromJson(String json) {
        return new Gson().fromJson(json, User.class);
    }
}
