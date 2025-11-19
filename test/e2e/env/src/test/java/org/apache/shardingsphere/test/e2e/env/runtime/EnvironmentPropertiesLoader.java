/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.e2e.env.runtime;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Environment properties loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnvironmentPropertiesLoader {
    
    /**
     * Load environment properties.
     *
     * @return loaded properties
     */
    public static Properties loadProperties() {
        return loadProperties("env/e2e-env.properties");
    }
    
    /**
     * Load environment properties.
     *
     * @param fileName file name
     * @return loaded properties
     */
    @SneakyThrows(IOException.class)
    public static Properties loadProperties(final String fileName) {
        Properties result = new Properties();
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            if (null != inputStream) {
                result.load(inputStream);
            }
        }
        for (String each : System.getProperties().stringPropertyNames()) {
            result.setProperty(each, System.getProperty(each));
        }
        return result;
    }
    
    /**
     * Get list value.
     *
     * @param props properties
     * @param key key
     * @return list value
     */
    public static List<String> getListValue(final Properties props, final String key) {
        return Arrays.stream(props.getProperty(key, "").split(",")).map(String::trim).filter(each -> !each.isEmpty()).collect(Collectors.toList());
    }
}
