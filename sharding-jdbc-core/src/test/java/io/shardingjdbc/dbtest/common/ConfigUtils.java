package io.shardingjdbc.dbtest.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigUtils {

    private static Properties config = null;

    static {
        try {
            config = new Properties();
            config.load(ConfigUtils.class.getClassLoader().getResourceAsStream("integrate/env.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getString(final String key, final String defaultValue) {
        return config.getProperty(key, defaultValue);
    }

    public static Map<String, String> getDatas(final String startKey) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<Object, Object> eachEntry : config.entrySet()) {
            String key = (String) eachEntry.getKey();
            if (key.startsWith(startKey)) {
                result.put(key, (String) eachEntry.getValue());
            }
        }
        return result;
    }

}
