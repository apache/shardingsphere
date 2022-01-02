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

package org.apache.shardingsphere.infra.config.datasource;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/**
 * JDBC URI.
 */
public final class JdbcUri {
    
    private final URI jdbcUri;
    
    public JdbcUri(final String jdbcUrl) {
        jdbcUri = URI.create(jdbcUrl.substring(5));
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
        String query = jdbcUri.getQuery();
        return Strings.isNullOrEmpty(query) ? Collections.emptyMap() : Splitter.on("&").withKeyValueSeparator("=").split(query);
    }
    
    /**
     * Append parameters.
     *
     * @param parameters JDBC parameters
     * @return new JDBC URL
     */
    public String appendParameters(final Map<String, String> parameters) {
        return String.format("jdbc:%s%s", jdbcUri, formatParameters(parameters));
    }
    
    private String formatParameters(final Map<String, String> parameters) {
        String prefix = (Strings.isNullOrEmpty(jdbcUri.getQuery())) ? "?" : "&";
        StringBuilder result = new StringBuilder(prefix);
        for (Entry<String, String> entry : parameters.entrySet()) {
            result.append(entry.getKey());
            if (null != entry.getValue()) {
                result.append("=").append(entry.getValue());
            }
            result.append("&");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }
}
