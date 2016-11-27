package com.noelchew.firebaseshoutout.util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.noelchew.firebaseshoutout.MyApplication;

/**
 * Created by noelchew on 31/08/16.
 */
public class AnalyticsUtil {
    public static void sendAnalyticsScreenTrack(Context context, String screen) {
        sendGoogleAnalyticsScreenTrack(context, screen);
        sendFirebaseAnalyticsScreenTrack(context, screen);
    }

    public static void sendAnalyticsEventTrack(Context context, String category, String event) {
        sendGoogleAnalyticsEventTrack(context, category, event);
        sendFirebaseAnalyticsEventTrack(context, category, event);
    }

    // --------- Google Analytics - Begin --------- //
    private static void sendGoogleAnalyticsScreenTrack(Context context, String screen) {
        Tracker tracker = null;
        if (context instanceof Activity) {
            tracker = ((MyApplication) ((Activity) context).getApplication()).getDefaultTracker();
        } else if (context instanceof FragmentActivity) {
            tracker = ((MyApplication) ((FragmentActivity) context).getApplication()).getDefaultTracker();
        }
        HitBuilders.ScreenViewBuilder builder = new HitBuilders.ScreenViewBuilder();
        tracker.setScreenName(screen);
        tracker.send(builder.build());
    }

    private static void sendGoogleAnalyticsEventTrack(Context context, String category, String event) {
        Tracker tracker = null;
        if (context instanceof Activity) {
            tracker = ((MyApplication) ((Activity) context).getApplication()).getDefaultTracker();
        } else if (context instanceof FragmentActivity) {
            tracker = ((MyApplication) ((FragmentActivity) context).getApplication()).getDefaultTracker();
        }
        HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(event);
        tracker.send(builder.build());

    }
    // --------- Google Analytics - End --------- //


    // --------- Firebase Analytics - Begin --------- //
    private static void sendFirebaseAnalyticsScreenTrack(Context context, String screen) {
        Bundle payload = new Bundle();
        payload.putString("Category", "Screen");
        payload.putString(FirebaseAnalytics.Param.VALUE, screen);
        FirebaseAnalytics.getInstance(context).logEvent(screen,
                payload);
    }

    private static void sendFirebaseAnalyticsEventTrack(Context context, String category, String event) {
        Bundle payload = new Bundle();
        payload.putString("Category", category);
        payload.putString(FirebaseAnalytics.Param.VALUE, event);
        FirebaseAnalytics.getInstance(context).logEvent(event,
                payload);
    }
    // --------- Firebase Analytics - End --------- //

}