package com.noelchew.firebaseshoutout.data;

import android.content.Context;

import com.noelchew.ncutils.SharedPreferencesUtil;

/**
 * Created by noelchew on 31/08/16.
 */
public class FirstTimeRunningData {
    private static final String FIRST_TIME_RUNNING_KEY = "firstTimeRunningKey";

    public static boolean isFirstTimeRunning(Context context) {
        return SharedPreferencesUtil.getBoolean(context, FIRST_TIME_RUNNING_KEY, true);
    }

    public static void setFirstTimeRunning(Context context, boolean isFirstTimeRunning) {
        SharedPreferencesUtil.setBoolean(context, FIRST_TIME_RUNNING_KEY, isFirstTimeRunning);
    }
}
