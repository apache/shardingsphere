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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Arguments utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArgsUtils {
    
    /**
     * Get configuration subject.
     *
     * @param url URL
     * @param urlPrefix URL prefix
     * @param configurationType configuration type
     * @return configuration subject
     */
    public static String getConfigurationSubject(final String url, final String urlPrefix, final String configurationType) {
        String configuredFile = url.substring(urlPrefix.length(), url.contains("?") ? url.indexOf('?') : url.length());
        String result = configuredFile.substring(configurationType.length());
        Preconditions.checkArgument(!result.isEmpty(), "Configuration subject is required in driver URL.");
        return result;
    }
    
    /**
     * Parse parameters.
     *
     * @param url URL
     * @return parameter map
     */
    public static Map<String, String> parseParameters(final String url) {
        if (!url.contains("?")) {
            return Collections.emptyMap();
        }
        String query = url.substring(url.indexOf('?') + 1);
        if (Strings.isNullOrEmpty(query)) {
            return Collections.emptyMap();
        }
        String[] pairs = query.split("&");
        Map<String, String> result = new HashMap<>(pairs.length, 1L);
        for (String each : pairs) {
            int index = each.indexOf("=");
            if (index > 0) {
                result.put(each.substring(0, index), each.substring(index + 1));
            }
        }
        return result;
    }
}
