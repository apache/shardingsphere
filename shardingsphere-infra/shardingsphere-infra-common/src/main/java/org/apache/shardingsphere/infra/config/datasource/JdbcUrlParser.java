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
import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JDBC URL parser.
 */
public final class JdbcUrlParser {
    
    private static final String SCHEMA_PATTERN = "(?<schema>[\\w\\+:%]+)\\s*";
    
    private static final String AUTHORITY_PATTERN = "(?://(?<authority>[^/?#]*))?\\s*";
    
    private static final String PATH_PATTERN = "(?:/(?!\\s*/)(?<path>[^?#]*))?";
    
    private static final String QUERY_PATTERN = "(?:\\?(?!\\s*\\?)(?<query>[^#]*))?";
    
    private static final Pattern CONNECTION_URL_PATTERN = Pattern.compile(SCHEMA_PATTERN + AUTHORITY_PATTERN + PATH_PATTERN + QUERY_PATTERN);
    
    private static final String AUTHORITY_GROUP_KEY = "authority";
    
    private static final String PATH_GROUP_KEY = "path";
    
    private static final String QUERY_GROUP_KEY = "query";
    
    private final String jdbcURL;
    
    private final String authority;
    
    @Getter
    private final String database;
    
    private final String query;
    
    public JdbcUrlParser(final String jdbcURL) {
        this.jdbcURL = jdbcURL;
        Matcher matcher = CONNECTION_URL_PATTERN.matcher(jdbcURL);
        if (matcher.matches()) {
            authority = matcher.group(AUTHORITY_GROUP_KEY);
            database = matcher.group(PATH_GROUP_KEY);
            query = matcher.group(QUERY_GROUP_KEY);
        } else {
            authority = "";
            database = "";
            query = "";
        }
    }
    
    /**
     * Get hostname.
     *
     * @return hostname
     */
    public String getHostname() {
        if (!authority.contains(":")) {
            return authority;
        }
        String[] values = authority.split(":");
        if (2 == values.length) {
            return values[0];
        }
        // TODO process with multiple services, for example: replication, failover etc
        return null;
    }
    
    /**
     * Get port.
     * 
     * @return port
     */
    public int getPort() {
        if (!authority.contains(":")) {
            // TODO adapt other databases
            return 3306;
        }
        String[] values = authority.split(":");
        if (2 == values.length) {
            return Integer.parseInt(values[1]);
        }
        // TODO process with multiple services, for example: replication, failover etc
        return -1;
    }
    
    /**
     * Get query properties from JDBC connection URL.
     *
     * @return query properties
     */
    public Map<String, String> getQueryProperties() {
        return Strings.isNullOrEmpty(query) ? Collections.emptyMap() : Splitter.on("&").withKeyValueSeparator("=").split(query);
    }
    
    /**
     * Append query properties.
     *
     * @param queryProps query properties
     * @return new JDBC URL
     */
    public String appendQueryProperties(final Map<String, String> queryProps) {
        StringBuilder result = new StringBuilder(jdbcURL);
        String delimiter = Strings.isNullOrEmpty(query) ? "?" : "&";
        result.append(delimiter);
        for (Entry<String, String> entry : queryProps.entrySet()) {
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
