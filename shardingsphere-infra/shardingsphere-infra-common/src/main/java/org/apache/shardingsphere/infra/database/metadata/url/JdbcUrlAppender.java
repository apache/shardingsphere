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

import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;

import java.util.Map.Entry;
import java.util.Properties;

/**
 * JDBC URL appender.
 */
public final class JdbcUrlAppender {
    
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
            Properties mergedQueryProps = new Properties();
            mergedQueryProps.putAll(currentQueryProps);
            mergedQueryProps.putAll(queryProps);
            return appendQueryPropertiesOnURLBuilder(jdbcURL.substring(0, jdbcURL.indexOf('?') + 1), mergedQueryProps);
        }
        return appendQueryPropertiesOnURLBuilder(jdbcURL + (currentQueryProps.isEmpty() ? "?" : "&"), queryProps);
    }
    
    private boolean hasConflictedQueryProperties(final Properties currentQueryProps, final Properties tobeAppendedQueryProps) {
        return tobeAppendedQueryProps.keySet().stream().anyMatch(currentQueryProps::containsKey);
    }
    
    private String appendQueryPropertiesOnURLBuilder(final String jdbcUrlPrefix, final Properties queryProps) {
        StringBuilder result = new StringBuilder(jdbcUrlPrefix);
        for (Entry<Object, Object> entry : queryProps.entrySet()) {
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
