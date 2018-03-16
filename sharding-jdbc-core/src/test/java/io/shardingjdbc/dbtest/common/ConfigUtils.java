/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.dbtest.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigUtils {

    private static Properties config = new Properties();

    static {
        try {
            config.load(ConfigUtils.class.getClassLoader().getResourceAsStream("integrate/env.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * query value.
     * @param key key
     * @param defaultValue default Value
     * @return value
     */
    public static String getString(final String key, final String defaultValue) {
        return config.getProperty(key, defaultValue);
    }

    /**
     * Get the data group based on the string prefix.
     * @param startKey start Key
     * @return datas
     */
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
