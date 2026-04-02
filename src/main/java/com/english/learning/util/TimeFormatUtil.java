package com.english.learning.util;

public class TimeFormatUtil {

    private TimeFormatUtil() {
        // Prevent instantiation
    }

    public static String formatActiveTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        if (minutes < 1) {
            return "0 phút";
        }
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        if (hours > 0) {
            return hours + "h" + " " + remainingMinutes + "m";
        } else {
            return remainingMinutes + " phút";
        }
    }
}
