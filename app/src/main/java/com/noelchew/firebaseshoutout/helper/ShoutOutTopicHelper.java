package com.noelchew.firebaseshoutout.helper;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.messaging.FirebaseMessaging;
import com.noelchew.firebaseshoutout.R;
import com.noelchew.firebaseshoutout.data.CurrentUserData;
import com.noelchew.firebaseshoutout.model.ShoutOut;
import com.noelchew.firebaseshoutout.model.ShoutOutTopic;
import com.noelchew.firebaseshoutout.model.User;
import com.noelchew.firebaseshoutout.util.fcm.FcmUtils;

/**
 * Created by noelchew on 26/11/2016.
 */

public class ShoutOutTopicHelper {
    private static final String TAG = "TopicSubscrptnHelper";

    public static void addShoutOutTopic(final Context context, String topic, final AddShoutOutTopicCallback callback) {
        User user = CurrentUserData.getCurrentUser(context);
        if (user == null) {
            Log.e(TAG, "Unable to add ShoutOutTopic because user is NULL.");
            callback.addFailed(context.getString(R.string.user_not_logged_in));
            return;
        }
        final ShoutOutTopic shoutOutTopic = new ShoutOutTopic(user, topic);
        FirebaseDatabase.getInstance().getReference(context.getString(R.string.shout_out_topic_node)).child(shoutOutTopic.getTopicId()).setValue(shoutOutTopic, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, "Error adding ShoutOutTopic " + shoutOutTopic.getTopicId());
                    callback.addFailed(context.getString(R.string.error_occurred));
                } else {
                    Log.d(TAG, "ShoutOutTopic " + shoutOutTopic.getTopicId() + " has been added successfully.");
                    callback.addSuccess();
                }
            }
        });
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
        FirebaseDatabase.getInstance().getReference(context.getString(R.string.shout_out_topic_node)).child(shoutOutTopic.getTopicId()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, "Error removing ShoutOutTopic " + shoutOutTopic.getTopicId());
                    callback.removeFailed(context.getString(R.string.error_occurred));
                } else {
                    Log.d(TAG, "ShoutOutTopic " + shoutOutTopic.getTopicId() + " has been removed successfully.");
                    callback.removeSuccess();
                }
            }
        });
    }

    public static void subscribeTopic(Context context, final ShoutOutTopic shoutOutTopic) {
        User user = CurrentUserData.getCurrentUser(context);
        if (user == null) {
            Log.e(TAG, "Unable to subscribe topic because user is NULL.");
            return;
        }

        FirebaseMessaging.getInstance().subscribeToTopic(shoutOutTopic.getTopicId());

        // update Firebase Database
        FirebaseDatabase.getInstance().getReference(context.getString(R.string.shout_out_topic_node)).child(shoutOutTopic.getTopicId()).child("subscribers").child(user.getId()).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, "Error adding user into subscribers of ShoutOutTopic " + shoutOutTopic.getTopicId());
                } else {
                    Log.d(TAG, "User is added into subscribers of ShoutOutTopic " + shoutOutTopic.getTopicId() + " successfully.");
                }
            }
        });
    }

    public static void unsubscribeTopic(Context context, final ShoutOutTopic shoutOutTopic) {
        User user = CurrentUserData.getCurrentUser(context);
        if (user == null) {
            Log.e(TAG, "Unable to unsubscribe topic because user is NULL.");
            return;
        }

        FirebaseMessaging.getInstance().unsubscribeFromTopic(shoutOutTopic.getTopicId());

        // update Firebase Database
        FirebaseDatabase.getInstance().getReference(context.getString(R.string.shout_out_topic_node)).child(shoutOutTopic.getTopicId()).child("subscribers").child(user.getId()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, "Error removing user from subscribers of ShoutOutTopic " + shoutOutTopic.getTopicId());
                } else {
                    Log.d(TAG, "User is removed from subscribers of ShoutOutTopic " + shoutOutTopic.getTopicId() + " successfully.");
                }
            }
        });
    }

    public static void makeShoutOut(Context context, final ShoutOutTopic shoutOutTopic, final String message, FcmUtils.FcmCloudMessagingCallback callback) {
        // update Firebase Database
        // update topic - shout out list
        String key = FirebaseDatabase.getInstance().getReference(context.getString(R.string.shout_out_topic_node)).child(shoutOutTopic.getTopicId()).child("shoutOuts").push().getKey();

        ShoutOut shoutOut = new ShoutOut(key, message);
        FirebaseDatabase.getInstance().getReference(context.getString(R.string.shout_out_topic_node)).child(shoutOutTopic.getTopicId()).child("shoutOuts").child(key).setValue(shoutOut, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, "Error adding Shout Out [" + message + "] to ShoutOutTopic " + shoutOutTopic.getTopicId());
                } else {
                    Log.d(TAG, "Shout Out [" + message + "] has been added to shoutOuts of ShoutOutTopic " + shoutOutTopic.getTopicId() + " successfully.");
                }
            }
        });

        // update topic - last shout out
        FirebaseDatabase.getInstance().getReference(context.getString(R.string.shout_out_topic_node)).child(shoutOutTopic.getTopicId()).child("lastShoutOut").setValue(message, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, "Error updating lastShoutOut of ShoutOutTopic " + shoutOutTopic.getTopicId());
                } else {
                    Log.d(TAG, "lastShoutOut of ShoutOutTopic " + shoutOutTopic.getTopicId() + " has been updated successfully.");
                }
            }
        });


        // update topic - last active date
        FirebaseDatabase.getInstance().getReference(context.getString(R.string.shout_out_topic_node)).child(shoutOutTopic.getTopicId()).child("lastActiveDate").child("date").setValue(ServerValue.TIMESTAMP, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, "Error updating lastShoutOutDate of ShoutOutTopic " + shoutOutTopic.getTopicId());
                } else {
                    Log.d(TAG, "lastShoutOutDate of ShoutOutTopic " + shoutOutTopic.getTopicId() + " has been updated successfully.");
                }
            }
        });

        FcmUtils.sendTopicNotificationDataMessage(context, shoutOutTopic.getTopicId(), shoutOutTopic.getTopicName(), shoutOutTopic.getTopicName(), message, callback);
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
