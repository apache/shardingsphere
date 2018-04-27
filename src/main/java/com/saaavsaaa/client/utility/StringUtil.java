package com.saaavsaaa.client.utility;

/**
 * Created by aaa on 18-4-18.
 */
public class StringUtil {
    public static boolean isNullOrBlank(String string) {
        return string == null || string.trim().length() == 0;
    }
}
