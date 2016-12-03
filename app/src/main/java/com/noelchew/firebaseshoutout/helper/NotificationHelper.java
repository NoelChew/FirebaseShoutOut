package com.noelchew.firebaseshoutout.helper;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.noelchew.firebaseshoutout.R;
import com.noelchew.firebaseshoutout.ui.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.goncalves.pugnotification.notification.PugNotification;

/**
 * Created by noelchew on 03/12/2016.
 */

public class NotificationHelper {
    public static void showNotification(Context context, String title, String message) {
        try {
            long[] pattern = {10, 150, 50, 50};
            PugNotification.with(context)
                    .load()
                    .identifier(createNotificationId())
                    .title(title)
                    .message(message)
                    .bigTextStyle(message)
                    .smallIcon(R.drawable.ic_bullhorn)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_LIGHTS) //not using default sound and default vibration
                    .vibrate(pattern)
                    .click(createPendingIntent(context))
                    .autoCancel(true)
                    .simple()
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static PendingIntent createPendingIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static int createNotificationId() {
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("MMddHHmmss").format(now));
        return id;
    }
}
