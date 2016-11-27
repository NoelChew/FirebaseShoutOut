package com.noelchew.firebaseshoutout.data;

import android.content.Context;

import com.noelchew.ncutils.SharedPreferencesUtil;

/**
 * Created by noelchew on 17/08/2016.
 */
public class FcmTokenData {
    private static final String FCM_TOKEN_KEY = "fcmTokenKey";

    public static String getFcmToken(Context context) {
        return SharedPreferencesUtil.getString(context, FCM_TOKEN_KEY);
    }

    public static void setFcmToken(Context context, String fcmToken) {
        SharedPreferencesUtil.setString(context, FCM_TOKEN_KEY, fcmToken);
    }
}
