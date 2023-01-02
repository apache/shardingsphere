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

package org.apache.shardingsphere.test.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Properties;

/**
 * Properties builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PropertiesBuilder {
    
    /**
     * Build properties.
     * 
     * @param properties to be built properties
     * @return built properties
     */
    public static Properties build(final Property... properties) {
        Properties result = new Properties();
        for (Property each : properties) {
            result.setProperty(each.key, each.value);
        }
        return result;
    }
    
    /**
     * Property.
     */
    @RequiredArgsConstructor
    public static class Property {
        
        private final String key;
        
        private final String value;
    }
}
