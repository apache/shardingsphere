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

package org.apache.shardingsphere.scaling.core.utils;

import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ConfigurationYamlConverter;
import org.apache.shardingsphere.scaling.core.config.datasource.ScalingDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.metadata.JdbcUri;

import java.util.HashMap;
import java.util.Map;

/**
 * JDBC util.
 *
 */
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
        dataSourceConfig.setJdbcUrl(append(dataSourceConfig.getJdbcUrl(), parameters));
    }
    
    private static void append(final ShardingSphereJDBCDataSourceConfiguration dataSourceConfig, final Map<String, String> parameters) {
        Map<String, DataSourceConfiguration> dataSourceConfigMap = new HashMap<>(ConfigurationYamlConverter.loadDataSourceConfigs(dataSourceConfig.getDataSource()));
        dataSourceConfigMap.forEach((key, value) -> {
            String jdbcUrlKey = value.getProps().containsKey("url") ? "url" : "jdbcUrl";
            value.getProps().replace(jdbcUrlKey, append(value.getProps().get(jdbcUrlKey).toString(), parameters));
        });
        dataSourceConfig.setDataSource(ConfigurationYamlConverter.serializeDataSourceConfigs(dataSourceConfigMap));
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
}
