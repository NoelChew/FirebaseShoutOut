package com.noelchew.firebaseshoutout.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.noelchew.firebaseshoutout.R;
import com.noelchew.firebaseshoutout.data.SavedShoutOutData;
import com.noelchew.firebaseshoutout.model.NotificationEvent;
import com.noelchew.firebaseshoutout.ui.MainActivity;
import com.noelchew.firebaseshoutout.util.BitmapUtils;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import br.com.goncalves.pugnotification.notification.PugNotification;

/**
 * Created by noelchew on 27/08/2016.
 */
public class MyFcmMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFcmMessagingService";

    private long[] pattern = {200, 100, 200};

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived");
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notification Title: " + title);
            Log.d(TAG, "Notification Body: " + body);
//            sendDefaultNotification(title, body);

            // date data not sent over via FCM. use current timestamp
            SavedShoutOutData.appendSavedShoutOuts(MyFcmMessagingService.this, new Date(), title, body);

            EventBus.getDefault().post(new NotificationEvent());
        } else
            // Check if message contains a data payload.
            if (remoteMessage.getData().size() > 0) {
                Log.d(TAG, "Message data payload: " + remoteMessage.getData());
                String title = remoteMessage.getData().get("title");
                String body = remoteMessage.getData().get("body");
//            sendDefaultNotification(title, body);

                // date data not sent over via FCM. use current timestamp
                SavedShoutOutData.appendSavedShoutOuts(MyFcmMessagingService.this, new Date(), title, body);

                EventBus.getDefault().post(new NotificationEvent());

            }
    }

    private void sendDefaultNotification(String messageTitle, String messageBody) {
        try {
            PugNotification.with(this)
                    .load()
                    .identifier(createNotificationId())
                    .title(messageTitle)
                    .message(messageBody)
                    .bigTextStyle(messageBody)
                    .smallIcon(R.drawable.ic_bullhorn)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
                    .click(createPendingIntent())
                    .autoCancel(true)
                    .vibrate(pattern)
                    .simple()
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(String messageTitle, String messageBody, String profileImageUrl) {
//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (messageTitle != null && !messageTitle.isEmpty()) {
            Bitmap bitmap = null;
            if (!TextUtils.isEmpty(profileImageUrl)) {
                try {
                    bitmap = Glide.
                            with(this).
                            load(profileImageUrl).
                            asBitmap().
                            into(100, 100). // Width and height
                            get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

            if (bitmap != null) {
                final Bitmap circleBitmap = BitmapUtils.getCircleBitmap(bitmap);
                PugNotification.with(this)
                        .load()
                        .identifier(createNotificationId())
                        .title(messageTitle)
                        .message(messageBody)
                        .bigTextStyle(messageBody)
                        .smallIcon(R.drawable.ic_bullhorn)
                        .largeIcon(circleBitmap)
                        .flags(Notification.DEFAULT_ALL)
                        .click(createPendingIntent())
                        .autoCancel(true)
                        .vibrate(pattern)
                        .simple()
                        .build();
            } else {
                PugNotification.with(this)
                        .load()
                        .identifier(createNotificationId())
                        .title(messageTitle)
                        .message(messageBody)
                        .bigTextStyle(messageBody)
                        .smallIcon(R.drawable.ic_bullhorn)
                        .largeIcon(R.mipmap.ic_launcher)
                        .flags(Notification.DEFAULT_ALL)
                        .click(createPendingIntent())
                        .autoCancel(true)
                        .vibrate(pattern)
                        .simple()
                        .build();
            }
        }
    }

    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private int createNotificationId() {
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("MMddHHmmss").format(now));
        return id;
    }
}