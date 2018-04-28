package com.saaavsaaa.client.utility;

/**
 * Created by aaa
 */
public class StringUtil {
    public static boolean isNullOrBlank(String string) {
        return string == null || string.trim().length() == 0;
    }
}
