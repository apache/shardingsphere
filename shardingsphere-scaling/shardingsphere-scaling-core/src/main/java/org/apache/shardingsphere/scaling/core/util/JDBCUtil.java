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

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.scaling.core.common.datasource.JdbcUri;
import org.apache.shardingsphere.scaling.core.config.datasource.ScalingDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.StandardJDBCDataSourceConfiguration;

import java.util.Map;

/**
 * JDBC util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JDBCUtil {
    
    /**
     * Append jdbc parameter.
     *
     * @param scalingDataSourceConfiguration data source configuration
     * @param parameters parameters
     */
    public static void appendJDBCParameter(final ScalingDataSourceConfiguration scalingDataSourceConfiguration, final Map<String, String> parameters) {
        if (scalingDataSourceConfiguration instanceof StandardJDBCDataSourceConfiguration) {
            append((StandardJDBCDataSourceConfiguration) scalingDataSourceConfiguration, parameters);
        } else if (scalingDataSourceConfiguration instanceof ShardingSphereJDBCDataSourceConfiguration) {
            append((ShardingSphereJDBCDataSourceConfiguration) scalingDataSourceConfiguration, parameters);
        }
    }
    
    private static void append(final StandardJDBCDataSourceConfiguration dataSourceConfig, final Map<String, String> parameters) {
        dataSourceConfig.getHikariConfig().setJdbcUrl(append(dataSourceConfig.getHikariConfig().getJdbcUrl(), parameters));
    }
    
    private static void append(final ShardingSphereJDBCDataSourceConfiguration dataSourceConfig, final Map<String, String> parameters) {
        dataSourceConfig.getRootRuleConfigs().getDataSources()
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
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            result.append(entry.getKey());
            if (null != entry.getValue()) {
                result.append("=").append(entry.getValue());
            }
            result.append("&");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }
    
    /**
     * Get jdbc url from parameters, the key can be url or jdbcUrl.
     *
     * @param parameters parameters
     * @return jdbc url
     */
    public static String getJdbcUrl(final Map<String, Object> parameters) {
        Object result = parameters.getOrDefault("url", parameters.get("jdbcUrl"));
        Preconditions.checkNotNull(result, "url or jdbcUrl is required.");
        return result.toString();
    }
}
