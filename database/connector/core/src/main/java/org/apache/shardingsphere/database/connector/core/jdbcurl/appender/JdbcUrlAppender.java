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

package org.apache.shardingsphere.database.connector.core.jdbcurl.appender;

import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;

import java.util.Properties;
import java.util.stream.Collectors;

/**
 * JDBC URL appender.
 */
public final class JdbcUrlAppender {
    
    /**
     * Append query properties.
     *
     * @param jdbcUrl JDBC URL to be appended
     * @param queryProps query properties to be appended
     * @return appended JDBC URL
     */
    public String appendQueryProperties(final String jdbcUrl, final Properties queryProps) {
        if (queryProps.isEmpty()) {
            return jdbcUrl;
        }
        Properties currentQueryProps = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, DatabaseTypeFactory.get(jdbcUrl)).parse(jdbcUrl, null, null).getQueryProperties();
        return hasConflictedQueryProperties(currentQueryProps, queryProps)
                ? jdbcUrl.substring(0, jdbcUrl.indexOf('?') + 1) + concat(getMergedProperties(currentQueryProps, queryProps))
                : jdbcUrl + getURLDelimiter(currentQueryProps) + concat(queryProps);
    }
    
    private boolean hasConflictedQueryProperties(final Properties currentQueryProps, final Properties toBeAppendedQueryProps) {
        return toBeAppendedQueryProps.keySet().stream().anyMatch(currentQueryProps::containsKey);
    }
    
    private Properties getMergedProperties(final Properties currentQueryProps, final Properties toBeAppendedQueryProps) {
        Properties result = new Properties();
        result.putAll(currentQueryProps);
        result.putAll(toBeAppendedQueryProps);
        return result;
    }
    
    private String getURLDelimiter(final Properties currentQueryProps) {
        return currentQueryProps.isEmpty() ? "?" : "&";
    }
    
    private String concat(final Properties queryProps) {
        return queryProps.entrySet().stream().map(entry -> String.join("=", entry.getKey().toString(), entry.getValue().toString())).collect(Collectors.joining("&"));
    }
}
