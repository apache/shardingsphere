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

package org.apache.shardingsphere.database.connector.core.jdbcurl.parser;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.database.connector.core.exception.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.Optional;
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
    
    private static final String HOST_PORT_PATTERN = "((?<hostname>[\\w-.+%]+)|(\\[(?<ipv6hostname>[^]]+)]))\\s*(:\\s*(?<port>\\d+))?";
    
    private static final Pattern HOST_PORT_PATTERN_PATTERN = Pattern.compile(HOST_PORT_PATTERN);
    
    private static final String HOSTNAME_GROUP_KEY = "hostname";
    
    private static final String IPV6_HOSTNAME_GROUP_KEY = "ipv6hostname";
    
    private static final String PORT_GROUP_KEY = "port";
    
    private static final String SCHEMA_KEY = "schema";
    
    /**
     * Parse JDBC URL.
     *
     * @param jdbcUrl JDBC URL to be parsed
     * @param defaultPort default port
     * @return parsed JDBC URL
     */
    public ConnectionProperties parse(final String jdbcUrl, final int defaultPort) {
        Matcher matcher = CONNECTION_URL_PATTERN.matcher(jdbcUrl);
        ShardingSpherePreconditions.checkState(matcher.matches(), () -> new UnrecognizedDatabaseURLException(jdbcUrl, CONNECTION_URL_PATTERN.pattern().replaceAll("%", "%%")));
        String authority = matcher.group(AUTHORITY_GROUP_KEY);
        ShardingSpherePreconditions.checkNotNull(authority, () -> new UnrecognizedDatabaseURLException(jdbcUrl, CONNECTION_URL_PATTERN.pattern().replaceAll("%", "%%")));
        Properties queryProperties = parseQueryProperties(matcher.group(QUERY_GROUP_KEY));
        String database = matcher.group(PATH_GROUP_KEY);
        String schema = queryProperties.getProperty(SCHEMA_KEY);
        if (authority.isEmpty()) {
            return new ConnectionProperties("", defaultPort, database, schema, queryProperties);
        }
        Matcher hostMatcher = HOST_PORT_PATTERN_PATTERN.matcher(authority);
        ShardingSpherePreconditions.checkState(hostMatcher.find(), () -> new UnrecognizedDatabaseURLException(jdbcUrl, CONNECTION_URL_PATTERN.pattern().replaceAll("%", "%%")));
        return new ConnectionProperties(parseHostname(hostMatcher), parsePort(hostMatcher, defaultPort), database, schema, queryProperties);
    }
    
    private String parseHostname(final Matcher hostMatcher) {
        return Optional.ofNullable(hostMatcher.group(IPV6_HOSTNAME_GROUP_KEY)).orElse(hostMatcher.group(HOSTNAME_GROUP_KEY));
    }
    
    private int parsePort(final Matcher hostMatcher, final int defaultPort) {
        return Optional.ofNullable(hostMatcher.group(PORT_GROUP_KEY)).map(Integer::parseInt).orElse(defaultPort);
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
