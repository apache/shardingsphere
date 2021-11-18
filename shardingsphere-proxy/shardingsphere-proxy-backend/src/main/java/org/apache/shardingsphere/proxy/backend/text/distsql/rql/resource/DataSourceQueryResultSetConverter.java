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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql.resource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Data source query result set converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceQueryResultSetConverter {
    
    /**
     * Convert to data source parameter map.
     *
     * @param dataSourceConfigMap data source configuration map
     * @return data source parameter map
     */
    public static Map<String, DataSourceParameter> covert(final Map<String, DataSourceConfiguration> dataSourceConfigMap) {
        return dataSourceConfigMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> covert(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static DataSourceParameter covert(final DataSourceConfiguration dataSourceConfig) {
        bindSynonym(dataSourceConfig);
        DataSourceParameter result = new DataSourceParameter();
        for (Field each : result.getClass().getDeclaredFields()) {
            try {
                Object dataSourceConfigProp =
                        DataSourceConfiguration.CUSTOM_POOL_PROPS_KEY.equals(each.getName()) ? dataSourceConfig.getCustomPoolProps() : dataSourceConfig.getProps().get(each.getName());
                if (null == dataSourceConfigProp) {
                    continue;
                }
                each.setAccessible(true);
                setDataSourceParameterField(each, result, dataSourceConfigProp);
            } catch (final ReflectiveOperationException ignored) {
            }
        }
        return result;
    }
    
    private static void bindSynonym(final DataSourceConfiguration dataSourceConfig) {
        dataSourceConfig.addPropertySynonym("url", "jdbcUrl");
        dataSourceConfig.addPropertySynonym("user", "username");
        dataSourceConfig.addPropertySynonym("connectionTimeout", "connectionTimeoutMilliseconds");
        dataSourceConfig.addPropertySynonym("maxLifetime", "maxLifetimeMilliseconds");
        dataSourceConfig.addPropertySynonym("idleTimeout", "idleTimeoutMilliseconds");
        dataSourceConfig.addPropertySynonym("maxPoolSize", "maximumPoolSize");
        dataSourceConfig.addPropertySynonym("minPoolSize", "minimumIdle");
    }
    
    private static void setDataSourceParameterField(final Field field, final DataSourceParameter object, final Object value) throws IllegalAccessException {
        Class<?> fieldType = field.getType();
        if (fieldType == int.class) {
            field.set(object, Integer.parseInt(value.toString()));
        } else if (fieldType == long.class) {
            field.set(object, Long.parseLong(value.toString()));
        } else if (fieldType == boolean.class) {
            field.set(object, Boolean.parseBoolean(value.toString()));
        } else if (fieldType == String.class) {
            field.set(object, value.toString());
        } else {
            field.set(object, value);
        }
    }
}
