package com.noelchew.firebaseshoutout.model;

/**
 * Created by noelchew on 27/11/2016.
 */

public class NotificationEvent2 {
    private String topicId;
    private String topic;
    private String message;

    public NotificationEvent2(String topicId, String topic, String message) {
        this.topicId = topicId;
        this.topic = topic;
        this.message = message;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
