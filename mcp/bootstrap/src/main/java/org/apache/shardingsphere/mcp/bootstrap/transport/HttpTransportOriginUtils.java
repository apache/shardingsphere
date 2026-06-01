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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * HTTP transport origin utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpTransportOriginUtils {
    
    /**
     * Normalize origins.
     *
     * @param origins origins
     * @return normalized origins
     */
    public static List<String> normalizeOrigins(final Collection<String> origins) {
        return null == origins ? Collections.emptyList() : origins.stream().map(HttpTransportOriginUtils::normalizeOrigin).filter(each -> !each.isEmpty()).toList();
    }
    
    /**
     * Check whether the value is a valid HTTP origin.
     *
     * @param value value
     * @return true if the value is a valid HTTP origin, otherwise false
     */
    public static boolean isValidOrigin(final String value) {
        return !normalizeOrigin(value).isEmpty();
    }
    
    /**
     * Normalize origin.
     *
     * @param value value
     * @return normalized origin, or empty text for an invalid origin
     */
    public static String normalizeOrigin(final String value) {
        try {
            URI uri = URI.create(Objects.toString(value, "").trim());
            return isValidOriginUri(uri) ? createOrigin(uri) : "";
        } catch (final IllegalArgumentException ignored) {
            return "";
        }
    }
    
    private static boolean isValidOriginUri(final URI uri) {
        return isHttpScheme(uri) && !Objects.toString(uri.getHost(), "").isEmpty() && null == uri.getUserInfo()
                && Objects.toString(uri.getRawPath(), "").isEmpty() && null == uri.getRawQuery() && null == uri.getRawFragment();
    }
    
    private static boolean isHttpScheme(final URI uri) {
        String scheme = Objects.toString(uri.getScheme(), "").toLowerCase(Locale.ROOT);
        return "http".equals(scheme) || "https".equals(scheme);
    }
    
    private static String createOrigin(final URI uri) {
        String result = Objects.toString(uri.getScheme(), "").toLowerCase(Locale.ROOT) + "://" + normalizeHost(uri.getHost());
        return uri.getPort() < 0 ? result : result + ":" + uri.getPort();
    }
    
    private static String normalizeHost(final String host) {
        String result = Objects.toString(host, "").trim().toLowerCase(Locale.ROOT);
        return result.contains(":") && !result.startsWith("[") ? "[" + result + "]" : result;
    }
}
