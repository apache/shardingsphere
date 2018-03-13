package io.shardingjdbc.dbtest.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigUtils {

    private static Properties config = null;

    static {
        try {
            config = new Properties();
            try {
                config.load(ConfigUtils.class.getClassLoader().getResourceAsStream("integrate/env.properties"));
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getString(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }

    public static Map<String, String> getDatas(String startKey) {
        Map<String, String> maps = new HashMap<>();
        for (Map.Entry<Object, Object> eachEntry : config.entrySet()) {
            String key = (String) eachEntry.getKey();
            if (key.startsWith(startKey)) {
                maps.put(key, (String) eachEntry.getValue());
            }
        }
        return maps;
    }

}
