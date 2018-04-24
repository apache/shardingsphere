package com.saaavsaaa.client.untils;

import java.nio.charset.Charset;

/**
 * Created by aaa on 18-4-18.
 */
public class StringUtil {
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static boolean isNullOrWhite(String string) {
        return string == null || string.trim().length() == 0;
    }
}
