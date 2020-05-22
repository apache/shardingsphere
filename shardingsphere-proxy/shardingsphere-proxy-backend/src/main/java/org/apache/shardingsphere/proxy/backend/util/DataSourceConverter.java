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

package org.apache.shardingsphere.proxy.backend.util;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.kernal.context.schema.DataSourceParameter;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source parameter converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceConverter {
    
    /**
     * Get data source map.
     *
     * @param dataSourceConfigurationMap data source configuration map
     * @return data source parameter map
     */
    public static Map<String, DataSourceParameter> getDataSourceParameterMap(final Map<String, DataSourceConfiguration> dataSourceConfigurationMap) {
        Map<String, DataSourceParameter> result = new LinkedHashMap<>(dataSourceConfigurationMap.size(), 1);
        for (Entry<String, DataSourceConfiguration> entry : dataSourceConfigurationMap.entrySet()) {
            result.put(entry.getKey(), createDataSourceParameter(entry.getValue()));
        }
        return result;
    }
    
    private static DataSourceParameter createDataSourceParameter(final DataSourceConfiguration dataSourceConfiguration) {
        bindAlias(dataSourceConfiguration);
        DataSourceParameter result = new DataSourceParameter();
        for (Field each : result.getClass().getDeclaredFields()) {
            try {
                each.setAccessible(true);
                if (dataSourceConfiguration.getProperties().containsKey(each.getName())) {
                    each.set(result, dataSourceConfiguration.getProperties().get(each.getName()));
                }
            } catch (final ReflectiveOperationException ignored) {
            }
        }
        return result;
    }

    private static void bindAlias(final DataSourceConfiguration dataSourceConfiguration) {
        dataSourceConfiguration.addAlias("url", "jdbcUrl");
        dataSourceConfiguration.addAlias("user", "username");
        dataSourceConfiguration.addAlias("connectionTimeout", "connectionTimeoutMilliseconds");
        dataSourceConfiguration.addAlias("maxLifetime", "maxLifetimeMilliseconds");
        dataSourceConfiguration.addAlias("idleTimeout", "idleTimeoutMilliseconds");
    }
    
    /**
     * Get data source configuration map.
     *
     * @param dataSourceParameterMap data source map
     * @return data source configuration map
     */
    public static Map<String, DataSourceConfiguration> getDataSourceConfigurationMap(final Map<String, DataSourceParameter> dataSourceParameterMap) {
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(dataSourceParameterMap.size());
        for (Entry<String, DataSourceParameter> entry : dataSourceParameterMap.entrySet()) {
            result.put(entry.getKey(), createDataSourceConfiguration(entry.getValue()));
        }
        return result;
    }
    
    private static DataSourceConfiguration createDataSourceConfiguration(final DataSourceParameter dataSourceParameter) {
        DataSourceConfiguration result = new DataSourceConfiguration(HikariDataSource.class.getName());
        result.getProperties().put("jdbcUrl", dataSourceParameter.getUrl());
        result.getProperties().put("username", dataSourceParameter.getUsername());
        result.getProperties().put("password", dataSourceParameter.getPassword());
        result.getProperties().put("connectionTimeout", dataSourceParameter.getConnectionTimeoutMilliseconds());
        result.getProperties().put("idleTimeout", dataSourceParameter.getIdleTimeoutMilliseconds());
        result.getProperties().put("maxLifetime", dataSourceParameter.getMaxLifetimeMilliseconds());
        result.getProperties().put("maxPoolSize", dataSourceParameter.getMaxPoolSize());
        result.getProperties().put("minPoolSize", dataSourceParameter.getMinPoolSize());
        result.getProperties().put("maintenanceIntervalMilliseconds", dataSourceParameter.getMaintenanceIntervalMilliseconds());
        result.getProperties().put("readOnly", dataSourceParameter.isReadOnly());
        return result;
    }
}
