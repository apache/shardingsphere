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

package org.apache.shardingsphere.test.integration.env.container.atomic.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Command part util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandPartUtil {
    
    /**
     * Merge command parts.
     *
     * @param defaultCommandParts default command parts
     * @param inputCommandParts input command parts, will override default command parts
     * @return merged command parts
     */
    public static String[] mergeCommandParts(final String[] defaultCommandParts, final String[] inputCommandParts) {
        if (null == defaultCommandParts || defaultCommandParts.length == 0) {
            return inputCommandParts;
        }
        if (null == inputCommandParts || inputCommandParts.length == 0) {
            return defaultCommandParts;
        }
        Map<String, String> defaultPartsMap = new LinkedHashMap<>();
        for (String each : defaultCommandParts) {
            String[] split = each.split("=");
            defaultPartsMap.put(split[0], split[1]);
        }
        for (String each : inputCommandParts) {
            String[] split = each.split("=");
            defaultPartsMap.put(split[0], split[1]);
        }
        return defaultPartsMap.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).toArray(String[]::new);
    }
}
