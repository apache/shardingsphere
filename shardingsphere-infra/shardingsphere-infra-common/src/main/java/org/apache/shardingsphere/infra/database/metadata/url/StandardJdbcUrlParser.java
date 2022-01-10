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

package org.apache.shardingsphere.infra.database.metadata.url;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Standard JDBC URL parser.
 */
public final class StandardJdbcUrlParser {
    
    private static final String SCHEMA_PATTERN = "(?<schema>[\\w\\+:%]+)\\s*";
    
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
     * @param jdbcURL JDBC URL to be parsed
     * @return parsed JDBC URL
     */
    public JdbcUrl parse(final String jdbcURL) {
        Matcher matcher = CONNECTION_URL_PATTERN.matcher(jdbcURL);
        if (matcher.matches()) {
            String authority = matcher.group(AUTHORITY_GROUP_KEY);
            if (null == authority) {
                return new JdbcUrl("", -1, "", new Properties());
                // throw new UnrecognizedDatabaseURLException(jdbcURL, CONNECTION_URL_PATTERN.pattern().replaceAll("%", "%%"));
            }
            return new JdbcUrl(parseHostname(authority), parsePort(authority), matcher.group(PATH_GROUP_KEY), parseQueryProperties(matcher.group(QUERY_GROUP_KEY)));
        }
        return new JdbcUrl("", -1, "", new Properties());
        // throw new UnrecognizedDatabaseURLException(jdbcURL, CONNECTION_URL_PATTERN.pattern().replaceAll("%", "%%"));
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
    
    private Properties parseQueryProperties(final String query) {
        if (Strings.isNullOrEmpty(query)) {
            return new Properties();
        }
        Properties result = new Properties();
        for (Entry<String, String> entry : Splitter.on("&").withKeyValueSeparator("=").split(query).entrySet()) {
            result.setProperty(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    /**
     * Append query properties.
     *
     * @param jdbcURL JDBC URL to be appended
     * @param queryProps query properties to be appended
     * @return appended JDBC URL
     */
    public String appendQueryProperties(final String jdbcURL, final Properties queryProps) {
        Properties currentQueryProps = DatabaseTypeRegistry.getDatabaseTypeByURL(jdbcURL).getDataSourceMetaData(jdbcURL, null).getQueryProperties();
        if (hasConflictedQueryProperties(currentQueryProps, queryProps)) {
            Properties newQueryProps = new Properties();
            newQueryProps.putAll(currentQueryProps);
            newQueryProps.putAll(queryProps);
            StringBuilder result = new StringBuilder(jdbcURL.substring(0, jdbcURL.indexOf('?')));
            result.append('?');
            appendQueryPropertiesOnURLBuilder(result, newQueryProps);
            return result.toString();
        }
        StringBuilder result = new StringBuilder(jdbcURL);
        String delimiter = currentQueryProps.isEmpty() ? "?" : "&";
        result.append(delimiter);
        appendQueryPropertiesOnURLBuilder(result, queryProps);
        return result.toString();
    }
    
    private boolean hasConflictedQueryProperties(final Properties currentQueryProps, final Properties queryProps) {
        for (Entry<Object, Object> entry : queryProps.entrySet()) {
            if (currentQueryProps.containsKey(entry.getKey())) {
                return true;
            }
        }
        return false;
    }
    
    private void appendQueryPropertiesOnURLBuilder(final StringBuilder builder, final Properties queryProps) {
        for (Entry<Object, Object> entry : queryProps.entrySet()) {
            builder.append(entry.getKey());
            if (null != entry.getValue()) {
                builder.append("=").append(entry.getValue());
            }
            builder.append("&");
        }
        builder.deleteCharAt(builder.length() - 1);
    }
}
