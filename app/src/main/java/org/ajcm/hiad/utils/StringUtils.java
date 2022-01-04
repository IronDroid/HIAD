package org.ajcm.hiad.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StringUtils {

    private static final String TAG = "StringUtils";
    public static String DATE_TIME_SERVER = "yyyy/MM/dd";
    public static String HUMAN_DATE_FORMAT = "dd, MMM yyyy";

    public static String getCurrentDateString() {
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDate = new SimpleDateFormat(DATE_TIME_SERVER, Locale.getDefault());
        return simpleDate.format(currentTime);
    }

    public static String getHumanDate(String inputDate) {
        return formateDateFromstring(DATE_TIME_SERVER, HUMAN_DATE_FORMAT, inputDate);
    }

    public static String formateDateFromstring(String inputFormat, String outputFormat, String inputDate) {

        Date parsed = null;
        String outputDate = "";

        SimpleDateFormat df_input = new SimpleDateFormat(inputFormat, java.util.Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat(outputFormat, java.util.Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
            outputDate = df_output.format(parsed);

        } catch (ParseException e) {
            Log.e(TAG, "ParseException - dateFormat");
        }

        return outputDate;
    }
}
