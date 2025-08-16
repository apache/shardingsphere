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

package org.apache.shardingsphere.test.it.sql.parser.external.env;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * External environment context.
 */
public final class ExternalEnvironmentContext {
    
    private static final ExternalEnvironmentContext INSTANCE = new ExternalEnvironmentContext();
    
    private final Properties props;
    
    private ExternalEnvironmentContext() {
        props = loadProperties();
    }
    
    /**
     * Get GitHub environment instance.
     *
     * @return got instance
     */
    public static ExternalEnvironmentContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get value by key.
     *
     * @param key key
     * @return value
     */
    public String getValue(final String key) {
        return props.getProperty(key);
    }
    
    @SneakyThrows(IOException.class)
    private Properties loadProperties() {
        Properties result = new Properties();
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("env/external-env.properties")) {
            result.load(inputStream);
        }
        for (String each : System.getProperties().stringPropertyNames()) {
            result.setProperty(each, System.getProperty(each));
        }
        return result;
    }
}
