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

package org.apache.shardingsphere.scaling.core.metadata;

import com.google.common.base.Strings;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Jdbc uri.
 */
public final class JdbcUri {
    
    private final URI jdbcUri;
    
    public JdbcUri(final String jdbcUrl) {
        jdbcUri = URI.create(jdbcUrl.substring(5));
    }
    
    /**
     * Get scheme.
     *
     * @return scheme name
     */
    public String getScheme() {
        return jdbcUri.getScheme();
    }
    
    /**
     * Get hostname.
     *
     * @return hostname
     */
    public String getHostname() {
        return jdbcUri.getHost();
    }
    
    /**
     * Get port.
     *
     * @return port
     */
    public int getPort() {
        return -1 == jdbcUri.getPort() ? 3306 : jdbcUri.getPort();
    }
    
    /**
     * Get host.
     *
     * @return host
     */
    public String getHost() {
        return String.format("%s:%d", getHostname(), getPort());
    }
    
    /**
     * Get database name.
     * @return database name
     */
    public String getDatabase() {
        return null == jdbcUri.getPath() ? "" : jdbcUri.getPath().replaceFirst("/", "");
    }
    
    /**
     * Get parameters.
     *
     * @return parameters.
     */
    public Map<String, String> getParameters() {
        Map<String, String> result = new HashMap<>();
        if (Strings.isNullOrEmpty(jdbcUri.getQuery())) {
            return result;
        }
        String[] parameters = jdbcUri.getQuery().split("&");
        for (String each : parameters) {
            String[] args = each.split("=");
            result.put(args[0], 1 == args.length ? null : args[1]);
        }
        return result;
    }
}
