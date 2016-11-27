package com.noelchew.firebaseshoutout.util.fcm.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Created by noelchew on 26/11/2016.
 */

public class AppInstanceInfo {
// https://developers.google.com/instance-id/reference/server#example_get_request
//    {
//        "application":"com.iid.example",
//            "authorizedEntity":"123456782354",
//            "platform":"Android",
//            "attestStatus":"ROOTED",
//            "appSigner":"1a2bc3d4e5",
//            "connectionType":"WIFI",
//            "connectDate":"2015-05-12"
//        "rel":{
//        "topics":{
//            "topicname1":{"addDate":"2015-07-30"},
//            "topicname2":{"addDate":"2015-07-30"},
//            "topicname3":{"addDate":"2015-07-30"},
//            "topicname4":{"addDate":"2015-07-30"}
//        }
//    }
//    }

    @SerializedName("application")
    private String applicationName;

    @SerializedName("authorizedEntity")
    private String authorizedEntity;

    @SerializedName("platform")
    private String platform;

    @SerializedName("attestStatus")
    private String attestStatus;

    @SerializedName("appSigner")
    private String appSigner;

    @SerializedName("connectionType")
    private String connectionType;

    @SerializedName("connectDate")
    private String connectDate;

    @SerializedName("rel")
    private TopicHashmap relationship;

    public AppInstanceInfo() {
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getAuthorizedEntity() {
        return authorizedEntity;
    }

    public void setAuthorizedEntity(String authorizedEntity) {
        this.authorizedEntity = authorizedEntity;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getAttestStatus() {
        return attestStatus;
    }

    public void setAttestStatus(String attestStatus) {
        this.attestStatus = attestStatus;
    }

    public String getAppSigner() {
        return appSigner;
    }

    public void setAppSigner(String appSigner) {
        this.appSigner = appSigner;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getConnectDate() {
        return connectDate;
    }

    public void setConnectDate(String connectDate) {
        this.connectDate = connectDate;
    }

    public TopicHashmap getRelationship() {
        return relationship;
    }

    public void setRelationship(TopicHashmap relationship) {
        this.relationship = relationship;
    }

    public static AppInstanceInfo fromJson(String json) {
        return new Gson().fromJson(json, AppInstanceInfo.class);
    }
}
