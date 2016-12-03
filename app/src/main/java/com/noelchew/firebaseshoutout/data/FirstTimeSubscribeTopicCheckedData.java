package com.noelchew.firebaseshoutout.data;

import android.content.Context;

import com.noelchew.ncutils.SharedPreferencesUtil;

/**
 * Created by noelchew on 31/08/16.
 */
public class FirstTimeSubscribeTopicCheckedData {
    private static final String FIRST_TIME_SUBSCRIBE_KEY = "firstTimeSubscribeCheckedKey";

    public static boolean isFirstTimeSubscribeChecked(Context context) {
        return SharedPreferencesUtil.getBoolean(context, FIRST_TIME_SUBSCRIBE_KEY, false);
    }

    public static void setFirstTimeSubscribeChecked(Context context, boolean isFirstTimeSubscribe) {
        SharedPreferencesUtil.setBoolean(context, FIRST_TIME_SUBSCRIBE_KEY, isFirstTimeSubscribe);
    }
}
