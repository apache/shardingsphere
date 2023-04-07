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

package org.apache.shardingsphere.globalclock.type.tso.provider;

import java.util.Properties;

/**
 * Properties of RedisTSOProvider.
 */
public enum RedisTSOProperties {
    
    HOST("host", "127.0.0.1"),
    
    PORT("port", "6379"),
    
    PASSWORD("password", ""),
    
    TIMEOUT_INTERVAL("timeoutInterval", "40000"),
    
    MAX_IDLE("maxIdle", "8"),
    
    MAX_TOTAL("maxTotal", "18");
    
    private final String name;
    
    private final String defaultValue;
    
    RedisTSOProperties(final String name, final String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }
    
    /**
     * Get name of properties.
     *
     * @return name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get default value of properties.
     *
     * @return default value
     */
    public String getDefaultValue() {
        return defaultValue;
    }
    
    /**
     * Get value of properties.
     *
     * @param properties properties
     * @return value
     */
    public String get(final Properties properties) {
        return properties.getProperty(name, defaultValue);
    }
    
    /**
     * Set value of properties if value != null,
     * remove key of properties if value == null.
     *
     * @param properties properties
     * @param value value
     */
    public void set(final Properties properties, final String value) {
        if (value == null) {
            properties.remove(name);
        } else {
            properties.setProperty(name, value);
        }
    }
}
