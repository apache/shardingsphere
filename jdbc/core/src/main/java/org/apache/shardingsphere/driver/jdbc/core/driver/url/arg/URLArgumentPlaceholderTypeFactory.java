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

package org.apache.shardingsphere.driver.jdbc.core.driver.url.arg;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * URL argument placeholder type factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class URLArgumentPlaceholderTypeFactory {
    
    private static final String KEY = "placeholder-type";
    
    /**
     * Get value of placeholder type.
     *
     * @param params parameters
     * @return placeholder type
     */
    public static URLArgumentPlaceholderType valueOf(final Map<String, String> params) {
        if (!params.containsKey(KEY)) {
            return URLArgumentPlaceholderType.NONE;
        }
        try {
            return URLArgumentPlaceholderType.valueOf(params.get(KEY).toUpperCase());
        } catch (final IllegalArgumentException ex) {
            return URLArgumentPlaceholderType.NONE;
        }
    }
}
