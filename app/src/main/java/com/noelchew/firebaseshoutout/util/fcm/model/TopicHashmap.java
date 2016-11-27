package com.noelchew.firebaseshoutout.util.fcm.model;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by noelchew on 26/11/2016.
 */

public class TopicHashmap {
    @SerializedName("topics")
    private HashMap<String, TopicAddedDate> topics;

    public TopicHashmap(HashMap<String, TopicAddedDate> topics) {
        this.topics = topics;
    }

    public HashMap<String, TopicAddedDate> getTopics() {
        return topics;
    }

    public void setTopics(HashMap<String, TopicAddedDate> topics) {
        this.topics = topics;
    }

    public ArrayList<FcmTopic> getTopicArrayList() {
        ArrayList<FcmTopic> topicArrayList = new ArrayList<>();
        if (topics == null) return topicArrayList;
        Iterator it = topics.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String strTopic;
            long dateAdded;
            try {
                strTopic = (String) pair.getKey();
            } catch (Exception e) {
                e.printStackTrace();
                strTopic = "-";
            }
            try {
                dateAdded = df.parse(((TopicAddedDate) pair.getValue()).getDateAdded()).getTime();
            } catch (Exception e) {
                e.printStackTrace();
                dateAdded = System.currentTimeMillis();
            }
            topicArrayList.add(new FcmTopic(strTopic, dateAdded));
        }

        return topicArrayList;
    }

    public class FcmTopic {
        private String topic;
        private long timestamp;

        public FcmTopic(String topic, long timestamp) {
            this.topic = topic;
            this.timestamp = timestamp;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
