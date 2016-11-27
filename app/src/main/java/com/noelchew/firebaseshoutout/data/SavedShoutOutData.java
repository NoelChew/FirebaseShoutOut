package com.noelchew.firebaseshoutout.data;

import android.content.Context;

import com.noelchew.ncutils.DateUtil;
import com.noelchew.ncutils.SharedPreferencesUtil;

import java.util.Date;



/**
 * Created by noelchew on 27/11/2016.
 */

public class SavedShoutOutData {
    private static final String DATA_KEY = "savedShoutOutData";
    private static final String DATE_FORMAT = "h:mm:ss a d MMM yyyy";
    public static String getSavedShoutOuts(Context context) {
        return SharedPreferencesUtil.getString(context, DATA_KEY).trim();
    }

    public static void clearSavedShoutOuts(Context context) {
        SharedPreferencesUtil.setString(context, DATA_KEY, "");
    }

    public static void appendSavedShoutOuts(Context context, Date date, String topic, String message) {
        String existingMessages = getSavedShoutOuts(context);
        existingMessages += "\n\n" + DateUtil.dateToString(date, DATE_FORMAT) + "\n" +
                topic + ": " + message;
        SharedPreferencesUtil.setString(context, DATA_KEY, existingMessages);
    }
}
