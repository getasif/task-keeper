package com.rishi.codepath.taskkeeper.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Utility to work with dates as per requirement
 * Created by rishi on 09/25/16.
 */
public class DateUtil {

    private static final String TAG = "DateUtil";
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("dd MMM yyy");
    private static final SimpleDateFormat SQL_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Returns the current display date in provided format
     * @return
     */
    public static String getCurrentSQLDate(){
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();

        return SQL_FORMATTER.format(date);
    }

    /**
     * Returns display date based on the year, month and day provided.
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static String getDisplayDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(year, month, day);
        Date date = cal.getTime();

        return FORMATTER.format(date);
    }

    /**
     * Returns display date to show
     * @param displayDate
     * @return
     */
    public static String getDisplayDate(String displayDate) {
        if("".equalsIgnoreCase(displayDate)){
            return "";
        }
        try {
            Date date = SQL_FORMATTER.parse(displayDate);
            return FORMATTER.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Returns date for storing in SQL for easy sorting
     * @param displayDate
     * @return
     */
    public static String getSQLDate(String displayDate) {
        if("".equalsIgnoreCase(displayDate)){
            return "";
        }
        try {
            Date date = FORMATTER.parse(displayDate);
            return SQL_FORMATTER.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}
