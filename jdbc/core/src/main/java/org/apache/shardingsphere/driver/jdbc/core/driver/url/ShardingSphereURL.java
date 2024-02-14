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

package org.apache.shardingsphere.driver.jdbc.core.driver.url;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ShardingSphere URL.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ShardingSphereURL {
    
    private final String configurationSubject;
    
    private final Map<String, String> parameters;
    
    /**
     * Parse ShardingSphere URL.
     * 
     * @param url ShardingSphere URL
     * @param urlPrefix URL prefix
     * @param configurationType configuration type
     * @return ShardingSphere URL
     */
    public static ShardingSphereURL parse(final String url, final String urlPrefix, final String configurationType) {
        return new ShardingSphereURL(parseConfigurationSubject(url, urlPrefix, configurationType), parseParameters(url));
    }
    
    private static String parseConfigurationSubject(final String url, final String urlPrefix, final String configurationType) {
        String configuredFile = url.substring(urlPrefix.length(), url.contains("?") ? url.indexOf('?') : url.length());
        String result = configuredFile.substring(configurationType.length());
        Preconditions.checkArgument(!result.isEmpty(), "Configuration subject is required in driver URL.");
        return result;
    }
    
    private static Map<String, String> parseParameters(final String url) {
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
