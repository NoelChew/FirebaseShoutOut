package com.noelchew.firebaseshoutout.util;

import com.noelchew.ncutils.DateUtil;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

/**
 * Created by noelchew on 29/11/2016.
 */

public class PrettyTimeUtil {
    private static final long ONE_DAY = 86400000;
    private static final long ONE_DAY_AND_HALF = 129600000;
    private static final String DATE_FORMAT = "h:mm:ss a d MMM yyyy";

    public static String getRelativeDateTime(long timestamp) {
        Date date = new Date(timestamp);
        if ((System.currentTimeMillis() - timestamp) > ONE_DAY_AND_HALF) {
            return DateUtil.dateToString(date, DATE_FORMAT);
        } else {
            PrettyTime prettyTime = new PrettyTime();
            return prettyTime.format(date);
        }
    }
}
