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

package org.apache.shardingsphere.infra.database.core.url;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Standard JDBC URL parser.
 */
public final class StandardJdbcUrlParser {
    
    private static final String SCHEMA_PATTERN = "(?<schema>[\\w-.+:%]+)\\s*";
    
    private static final String AUTHORITY_PATTERN = "(?://(?<authority>[^/?#]*))?\\s*";
    
    private static final String PATH_PATTERN = "(?:/(?!\\s*/)(?<path>[^?#]*))?";
    
    private static final String QUERY_PATTERN = "(?:\\?(?!\\s*\\?)(?<query>[^#]*))?";
    
    private static final Pattern CONNECTION_URL_PATTERN = Pattern.compile(SCHEMA_PATTERN + AUTHORITY_PATTERN + PATH_PATTERN + QUERY_PATTERN, Pattern.CASE_INSENSITIVE);
    
    private static final String AUTHORITY_GROUP_KEY = "authority";
    
    private static final String PATH_GROUP_KEY = "path";
    
    private static final String QUERY_GROUP_KEY = "query";
    
    /**
     * Parse JDBC URL.
     * 
     * @param jdbcUrl JDBC URL to be parsed
     * @return parsed JDBC URL
     * @throws UnrecognizedDatabaseURLException unrecognized database URL exception
     */
    public JdbcUrl parse(final String jdbcUrl) {
        Matcher matcher = CONNECTION_URL_PATTERN.matcher(jdbcUrl);
        if (matcher.matches()) {
            String authority = matcher.group(AUTHORITY_GROUP_KEY);
            ShardingSpherePreconditions.checkNotNull(authority, () -> new UnrecognizedDatabaseURLException(jdbcUrl, CONNECTION_URL_PATTERN.pattern().replaceAll("%", "%%")));
            return new JdbcUrl(parseHostname(authority), parsePort(authority), matcher.group(PATH_GROUP_KEY), parseQueryProperties(matcher.group(QUERY_GROUP_KEY)));
        }
        throw new UnrecognizedDatabaseURLException(jdbcUrl, CONNECTION_URL_PATTERN.pattern().replaceAll("%", "%%"));
    }
    
    private String parseHostname(final String authority) {
        if (!authority.contains(":")) {
            return authority;
        }
        return authority.split(":")[0];
    }
    
    private int parsePort(final String authority) {
        if (!authority.contains(":")) {
            return -1;
        }
        String port = authority.split(":")[1];
        if (port.contains(",")) {
            port = port.split(",")[0];
        }
        return Integer.parseInt(port);
    }
    
    /**
     * Parse query properties.
     *
     * @param query query parameter
     * @return query properties
     */
    public Properties parseQueryProperties(final String query) {
        if (Strings.isNullOrEmpty(query)) {
            return new Properties();
        }
        Properties result = new Properties();
        for (String each : Splitter.on("&").split(query)) {
            String[] property = each.split("=", 2);
            result.setProperty(property[0], property[1]);
        }
        return result;
    }
}
