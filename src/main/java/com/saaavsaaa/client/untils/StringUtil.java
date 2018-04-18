package com.saaavsaaa.client.untils;

/**
 * Created by aaa on 18-4-18.
 */
public class StringUtil {
    public static boolean isNullOrWhite(String string) {
        return string == null || string.trim().length() == 0;
    }
}
