package com.noelchew.firebaseshoutout.helper;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.noelchew.firebaseshoutout.R;

/**
 * Created by noelchew on 03/12/2016.
 */

public class DatabaseReferenceHelper {

    public static DatabaseReference getUserDatabaseReference(Context context) {
        return FirebaseDatabase.getInstance().getReference(context.getString(R.string.users_node));
    }

    public static DatabaseReference getSpecificUserDatabaseReference(Context context, String userId) {
        return FirebaseDatabase.getInstance().getReference(context.getString(R.string.users_node)).child(userId);
    }

    public static DatabaseReference getTopicDatabaseReference(Context context) {
        return FirebaseDatabase.getInstance().getReference(context.getString(R.string.shout_out_topic_node));
    }

    public static DatabaseReference getSpecificTopicDatabaseReference(Context context, String topicId) {
        return FirebaseDatabase.getInstance().getReference(context.getString(R.string.shout_out_topic_node)).child(topicId);
    }

    public static DatabaseReference getSubscriptionsDatabaseReference(Context context) {
        return FirebaseDatabase.getInstance().getReference(context.getString(R.string.subscriptions_node));
    }

    public static DatabaseReference getSpecificTopicSubscriptionDatabaseReference(Context context, String topicId) {
        return FirebaseDatabase.getInstance().getReference(context.getString(R.string.subscriptions_node)).child(topicId);
    }

    public static DatabaseReference getShoutOutDatabaseReference(Context context) {
        return FirebaseDatabase.getInstance().getReference(context.getString(R.string.shout_out_node));
    }

    public static DatabaseReference getShoutOutsByTopicDatabaseReference(Context context, String topicId) {
        return FirebaseDatabase.getInstance().getReference(context.getString(R.string.shout_out_node)).child(topicId);
    }

}
