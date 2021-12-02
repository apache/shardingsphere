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

package org.apache.shardingsphere.scaling.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.datasource.typed.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.typed.TypedDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.common.datasource.JdbcUri;
import org.apache.shardingsphere.driver.config.datasource.ShardingSphereJDBCDataSourceConfiguration;

import java.util.Map;
import java.util.Map.Entry;

/**
 * JDBC util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JDBCUtil {
    
    /**
     * Append jdbc parameter.
     *
     * @param dataSourceConfig data source configuration
     * @param parameters parameters
     */
    public static void appendJDBCParameter(final TypedDataSourceConfiguration dataSourceConfig, final Map<String, String> parameters) {
        if (dataSourceConfig instanceof StandardJDBCDataSourceConfiguration) {
            append((StandardJDBCDataSourceConfiguration) dataSourceConfig, parameters);
        } else if (dataSourceConfig instanceof ShardingSphereJDBCDataSourceConfiguration) {
            append((ShardingSphereJDBCDataSourceConfiguration) dataSourceConfig, parameters);
        }
    }
    
    /**
     * Append jdbc parameter.
     *
     * @param jdbcUrl jdbc url
     * @param parameters parameters
     *
     * @return new jdbc url
     */
    public static String appendJDBCParameter(final String jdbcUrl, final Map<String, String> parameters) {
        return append(jdbcUrl, parameters);
    }
    
    private static void append(final StandardJDBCDataSourceConfiguration dataSourceConfig, final Map<String, String> parameters) {
        dataSourceConfig.getHikariConfig().setJdbcUrl(append(dataSourceConfig.getHikariConfig().getJdbcUrl(), parameters));
    }
    
    private static void append(final ShardingSphereJDBCDataSourceConfiguration dataSourceConfig, final Map<String, String> parameters) {
        dataSourceConfig.getRootConfig().getDataSources()
                .forEach((key, value) -> {
                    String jdbcUrlKey = value.containsKey("url") ? "url" : "jdbcUrl";
                    value.replace(jdbcUrlKey, append(value.get(jdbcUrlKey).toString(), parameters));
                });
    }
    
    private static String append(final String url, final Map<String, String> parameters) {
        JdbcUri uri = new JdbcUri(url);
        return String.format("jdbc:%s://%s/%s?%s", uri.getScheme(), uri.getHost(), uri.getDatabase(), mergeParameters(uri.getParameters(), parameters));
    }
    
    private static String mergeParameters(final Map<String, String> parameters, final Map<String, String> appendParameters) {
        parameters.putAll(appendParameters);
        return formatParameters(parameters);
    }
    
    private static String formatParameters(final Map<String, String> parameters) {
        StringBuilder result = new StringBuilder();
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
