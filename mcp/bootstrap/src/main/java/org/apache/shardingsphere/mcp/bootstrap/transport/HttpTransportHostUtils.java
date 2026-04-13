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

import java.util.Locale;
import java.util.Objects;

/**
 * HTTP transport host utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpTransportHostUtils {
    
    /**
     * Check whether host is loopback.
     *
     * @param host host
     * @return true if host is loopback, otherwise false
     */
    public static boolean isLoopbackHost(final String host) {
        String actualHost = normalizeHost(host);
        return "127.0.0.1".equals(actualHost) || "localhost".equals(actualHost) || "::1".equals(actualHost);
    }
    
    private static String normalizeHost(final String host) {
        String result = Objects.toString(host, "").trim().toLowerCase(Locale.ENGLISH);
        return result.length() > 1 && '[' == result.charAt(0) && ']' == result.charAt(result.length() - 1) ? result.substring(1, result.length() - 1) : result;
    }
}
