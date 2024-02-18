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

package org.apache.shardingsphere.infra.url.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.url.core.exception.URLProviderNotFoundException;

import java.util.Properties;

/**
 * ShardingSphere URL.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ShardingSphereURL {
    
    private final String sourceType;
    
    private final String configurationSubject;
    
    private final Properties queryProps;
    
    /**
     * Parse ShardingSphere URL.
     * 
     * @param url URL
     * @return ShardingSphere URL
     */
    public static ShardingSphereURL parse(final String url) {
        ShardingSpherePreconditions.checkNotNull(url, () -> new URLProviderNotFoundException(url));
        String sourceType = parseSourceType(url);
        return new ShardingSphereURL(sourceType, parseConfigurationSubject(url.substring(sourceType.length())), parseProperties(url));
    }
    
    private static String parseSourceType(final String url) {
        return url.substring(0, url.indexOf(':') + 1);
    }
    
    private static String parseConfigurationSubject(final String url) {
        String result = url.substring(0, url.contains("?") ? url.indexOf('?') : url.length());
        Preconditions.checkArgument(!result.isEmpty(), "Configuration subject is required in URL.");
        return result;
    }
    
    private static Properties parseProperties(final String url) {
        if (!url.contains("?")) {
            return new Properties();
        }
        String queryProps = url.substring(url.indexOf('?') + 1);
        if (Strings.isNullOrEmpty(queryProps)) {
            return new Properties();
        }
        String[] pairs = queryProps.split("&");
        Properties result = new Properties();
        for (String each : pairs) {
            int index = each.indexOf("=");
            if (index > 0) {
                result.put(each.substring(0, index), each.substring(index + 1));
            }
        }
        return result;
    }
}
