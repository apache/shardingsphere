package com.saaavsaaa.client.utility.constant;

import java.nio.charset.Charset;

/**
 * Created by aaa
 */
public final class Constants {
    public static final int VERSION = -1;
    public static final byte[] NOTHING_DATA = new byte[0];
    public static final String NOTHING_VALUE = "";
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final String PATH_SEPARATOR = "/";
    public static final String GLOBAL_LISTENER_KEY = "globalListener";
    
    public static final byte[] CHANGING_VALUE = new byte[]{'c'};
    public static final byte[] RELEASE_VALUE = new byte[]{'r'};
    public static final String CHANGING_KEY = "CHANGING_KEY";
}
