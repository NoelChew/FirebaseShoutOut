package com.noelchew.firebaseshoutout.helper;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.noelchew.firebaseshoutout.R;
import com.noelchew.firebaseshoutout.data.CurrentUserData;
import com.noelchew.firebaseshoutout.model.ShoutOut;
import com.noelchew.firebaseshoutout.model.ShoutOutTopic;
import com.noelchew.firebaseshoutout.model.User;
import com.noelchew.firebaseshoutout.util.fcm.FcmUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by noelchew on 26/11/2016.
 */

public class ActionHelper {
    private static final String TAG = "TopicSubscrptnHelper";

    public static void addShoutOutTopic(final Context context, String topic, final AddShoutOutTopicCallback callback) {
        User user = CurrentUserData.getCurrentUser(context);
        if (user == null) {
            Log.e(TAG, "Unable to add ShoutOutTopic because user is NULL.");
            callback.addFailed(context.getString(R.string.user_not_logged_in));
            return;
        }
        final ShoutOutTopic shoutOutTopic = new ShoutOutTopic(user, topic);

        DatabaseReferenceHelper.getSpecificTopicDatabaseReference(context, shoutOutTopic.getTopicId()).setValue(shoutOutTopic);
        DatabaseReferenceHelper.getSpecificUserDatabaseReference(context, user.getId()).child("topics").child(shoutOutTopic.getTopicId()).setValue(true);
    }

    public static void renameShoutOutTopic(final Context context, final ShoutOutTopic shoutOutTopic, String newName, final RenameShoutOutTopicCallback callback) {
        FirebaseDatabase.getInstance().getReference(context.getString(R.string.shout_out_topic_node)).child(shoutOutTopic.getTopicId()).child("topicName").setValue(newName, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, "Error updating topicName of ShoutOutTopic " + shoutOutTopic.getTopicId());
                    callback.renameFailed(context.getString(R.string.error_occurred));
                } else {
                    Log.d(TAG, "topicName of ShoutOutTopic " + shoutOutTopic.getTopicId() + " has been updated successfully.");
                    callback.renameSuccess();
                }
            }
        });
    }

    public static void removeShoutOutTopic(final Context context, final ShoutOutTopic shoutOutTopic, final RemoveShoutOutTopicCallback callback) {
        User user = CurrentUserData.getCurrentUser(context);
        if (user == null) {
            Log.e(TAG, "Unable to remove ShoutOutTopic because user is NULL.");
            callback.removeFailed(context.getString(R.string.error_occurred));
            return;
        } else if (!user.getId().equalsIgnoreCase(shoutOutTopic.getUser().getId())) {
            Log.e(TAG, "Unable to remove ShoutOutTopic because user is not the creator.");
            callback.removeFailed(context.getString(R.string.error_occurred));
            return;
        }

        DatabaseReferenceHelper.getSpecificTopicDatabaseReference(context, shoutOutTopic.getTopicId()).child("subscribers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Boolean> subscribers = (HashMap<String, Boolean>) dataSnapshot.getValue();
                if (subscribers != null && !subscribers.isEmpty()) {
                    for (Map.Entry<String, Boolean> entry : subscribers.entrySet()) {
                        DatabaseReferenceHelper.getSpecificUserDatabaseReference(context, entry.getKey()).child("subscriptions").child(shoutOutTopic.getTopicId()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReferenceHelper.getSpecificTopicDatabaseReference(context, shoutOutTopic.getTopicId()).removeValue();
        DatabaseReferenceHelper.getShoutOutsByTopicDatabaseReference(context, shoutOutTopic.getTopicId()).removeValue();
        DatabaseReferenceHelper.getSpecificUserDatabaseReference(context, shoutOutTopic.getUser().getId()).child("topics").child(shoutOutTopic.getTopicId()).removeValue();
    }

    public static void getSubscribedTopics(Context context) {
        User user = CurrentUserData.getCurrentUser(context);
        if (user == null) {
            Log.e(TAG, "Unable to get subscribed topics because user is NULL.");
            return;
        }

        DatabaseReferenceHelper.getUserDatabaseReference(context).child(user.getId()).child("subscriptions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "user subscriptions dataSnapshot: " + dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static void subscribeTopic(Context context, final ShoutOutTopic shoutOutTopic) {
        User user = CurrentUserData.getCurrentUser(context);
        if (user == null) {
            Log.e(TAG, "Unable to subscribe topic because user is NULL.");
            return;
        }

        subscribeFcmTopic(shoutOutTopic.getTopicId());

        // update Firebase Database
        DatabaseReferenceHelper.getSpecificTopicDatabaseReference(context, shoutOutTopic.getTopicId()).child("subscribers").child(user.getId()).setValue(true);
        DatabaseReferenceHelper.getSpecificUserDatabaseReference(context, user.getId()).child("subscriptions").child(shoutOutTopic.getTopicId()).setValue(true);
    }

    public static void unsubscribeTopic(Context context, final ShoutOutTopic shoutOutTopic) {
        User user = CurrentUserData.getCurrentUser(context);
        if (user == null) {
            Log.e(TAG, "Unable to unsubscribe topic because user is NULL.");
            return;
        }

        unsubscribeFcmTopic(shoutOutTopic.getTopicId());

        // update Firebase Database
        DatabaseReferenceHelper.getSpecificTopicDatabaseReference(context, shoutOutTopic.getTopicId()).child("subscribers").child(user.getId()).removeValue();
        DatabaseReferenceHelper.getSpecificUserDatabaseReference(context, user.getId()).child("subscriptions").child(shoutOutTopic.getTopicId()).removeValue();
    }

    public static void subscribeFcmTopic(String topicId) {
        FirebaseMessaging.getInstance().subscribeToTopic(topicId);
    }

    public static void unsubscribeFcmTopic(String topicId) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicId);
    }

    public static void makeShoutOut(Context context, final ShoutOutTopic shoutOutTopic, final String message, FcmUtils.FcmCloudMessagingCallback callback) {
        User user = CurrentUserData.getCurrentUser(context);
        if (user == null) {
            Log.e(TAG, "Unable to make Shout Out because user is NULL.");
            return;
        }

        // update Firebase Database
        // update shout out
        String key = DatabaseReferenceHelper.getShoutOutsByTopicDatabaseReference(context, shoutOutTopic.getTopicId()).push().getKey();
        ShoutOut shoutOut = new ShoutOut(key, user.getId(), message);
        DatabaseReferenceHelper.getShoutOutsByTopicDatabaseReference(context, shoutOutTopic.getTopicId()).child(key).setValue(shoutOut);

        // update topic - last shout out
        DatabaseReferenceHelper.getSpecificTopicDatabaseReference(context, shoutOutTopic.getTopicId()).child("lastShoutOut").setValue(message);

        // update topic - last active date
        DatabaseReferenceHelper.getSpecificTopicDatabaseReference(context, shoutOutTopic.getTopicId()).child("lastActiveDate").child("date").setValue(ServerValue.TIMESTAMP);

        FcmUtils.sendTopicNotificationDataMessage(context, shoutOutTopic.getTopicId(), shoutOutTopic.getTopicName(), shoutOutTopic.getTopicName(), message, callback);
    }

    public static void likeShoutOut(Context context, final ShoutOutTopic shoutOutTopic, final ShoutOut shoutOut) {
        User user = CurrentUserData.getCurrentUser(context);
        if (user == null) {
            Log.e(TAG, "Unable to like ShoutOut because user is NULL.");
            return;
        }

        // update Firebase Database
        DatabaseReferenceHelper.getShoutOutsByTopicDatabaseReference(context, shoutOutTopic.getTopicId()).child(shoutOut.getId()).child("likes").child(user.getId()).setValue(true);
    }

    public static void unlikeShoutOut(Context context, final ShoutOutTopic shoutOutTopic, final ShoutOut shoutOut) {
        User user = CurrentUserData.getCurrentUser(context);
        if (user == null) {
            Log.e(TAG, "Unable to unlike ShoutOut because user is NULL.");
            return;
        }

        // update Firebase Database
        DatabaseReferenceHelper.getShoutOutsByTopicDatabaseReference(context, shoutOutTopic.getTopicId()).child(shoutOut.getId()).child("likes").child(user.getId()).removeValue();
    }

    public interface AddShoutOutTopicCallback {
        void addSuccess();
        void addFailed(String errorMessage);
    }

    public interface RenameShoutOutTopicCallback {
        void renameSuccess();
        void renameFailed(String errorMessage);
    }

    public interface RemoveShoutOutTopicCallback {
        void removeSuccess();
        void removeFailed(String errorMessage);
    }
}
