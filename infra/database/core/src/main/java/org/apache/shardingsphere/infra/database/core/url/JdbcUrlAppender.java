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

import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;

import java.util.Map.Entry;
import java.util.Properties;

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
        Properties currentQueryProps = DatabaseTypeFactory.get(jdbcUrl).getDataSourceMetaData(jdbcUrl, null).getQueryProperties();
        return hasConflictedQueryProperties(currentQueryProps, queryProps)
                ? concat(jdbcUrl.substring(0, jdbcUrl.indexOf('?') + 1), getMergedProperties(currentQueryProps, queryProps))
                : concat(jdbcUrl + getURLDelimiter(currentQueryProps), queryProps);
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
    
    private String concat(final String jdbcUrl, final Properties queryProps) {
        StringBuilder result = new StringBuilder(jdbcUrl);
        for (Entry<Object, Object> entry : queryProps.entrySet()) {
            result.append(entry.getKey());
            if (null != entry.getValue()) {
                result.append('=').append(entry.getValue());
            }
            result.append('&');
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }
    
    private String getURLDelimiter(final Properties currentQueryProps) {
        return currentQueryProps.isEmpty() ? "?" : "&";
    }
}
