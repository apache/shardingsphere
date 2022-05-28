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

package org.apache.shardingsphere.infra.datasource.pool.creator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaDataFactory;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaDataReflection;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.custom.CustomDataSourceProperties;
import org.apache.shardingsphere.infra.datasource.registry.GlobalDataSourceRegistry;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Data source pool creator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourcePoolCreator {
    
    // TODO when all data source configurations of instance are dropped by DistSQL, cached data source should be closed
    
    /**
     * Create data sources.
     *
     * @param dataSourcePropsMap data source properties map
     * @return created data sources
     */
    public static Map<String, DataSource> create(final Map<String, DataSourceProperties> dataSourcePropsMap) {
        return create(dataSourcePropsMap, true);
    }
    
    /**
     * Create data sources.
     *
     * @param dataSourcePropsMap data source properties map
     * @param cacheEnabled cache enabled
     * @return created data sources
     */
    public static Map<String, DataSource> create(final Map<String, DataSourceProperties> dataSourcePropsMap, final boolean cacheEnabled) {
        return dataSourcePropsMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> create(entry.getKey(), entry.getValue(), cacheEnabled), (oldValue, currentValue) -> oldValue,
                LinkedHashMap::new));
    }
    
    /**
     * Create data source.
     *
     * @param dataSourceProps data source properties
     * @return created data source
     */
    public static DataSource create(final DataSourceProperties dataSourceProps) {
        DataSource result = createDataSource(dataSourceProps.getDataSourceClassName());
        Optional<DataSourcePoolMetaData> poolMetaData = DataSourcePoolMetaDataFactory.findInstance(dataSourceProps.getDataSourceClassName());
        DataSourceReflection dataSourceReflection = new DataSourceReflection(result);
        if (poolMetaData.isPresent()) {
            setDefaultFields(dataSourceReflection, poolMetaData.get());
            setConfiguredFields(dataSourceProps, dataSourceReflection, poolMetaData.get());
            appendJdbcUrlProperties(dataSourceProps.getCustomDataSourceProperties(), result, poolMetaData.get());
            dataSourceReflection.addDefaultDataSourceProperties();
        } else {
            setConfiguredFields(dataSourceProps, dataSourceReflection);
        }
        return result;
    }
    
    /**
     * Create data source.
     *
     * @param dataSourceName data source name
     * @param dataSourceProps data source properties
     * @param cacheEnabled cache enabled
     * @return created data source
     */
    public static DataSource create(final String dataSourceName, final DataSourceProperties dataSourceProps, final boolean cacheEnabled) {
        DataSource result = create(dataSourceProps);
        if (cacheEnabled && !GlobalDataSourceRegistry.getInstance().getCachedDataSourceDataSources().containsKey(dataSourceName)) {
            GlobalDataSourceRegistry.getInstance().getCachedDataSourceDataSources().put(dataSourceName, result);
        }
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static DataSource createDataSource(final String dataSourceClassName) {
        return (DataSource) Class.forName(dataSourceClassName).getConstructor().newInstance();
    }
    
    private static void setDefaultFields(final DataSourceReflection dataSourceReflection, final DataSourcePoolMetaData poolMetaData) {
        for (Entry<String, Object> entry : poolMetaData.getDefaultProperties().entrySet()) {
            dataSourceReflection.setField(entry.getKey(), entry.getValue());
        }
    }
    
    private static void setConfiguredFields(final DataSourceProperties dataSourceProps, final DataSourceReflection dataSourceReflection) {
        for (Entry<String, Object> entry : dataSourceProps.getAllLocalProperties().entrySet()) {
            dataSourceReflection.setField(entry.getKey(), entry.getValue());
        }
    }
    
    private static void setConfiguredFields(final DataSourceProperties dataSourceProps, final DataSourceReflection dataSourceReflection, final DataSourcePoolMetaData poolMetaData) {
        for (Entry<String, Object> entry : dataSourceProps.getAllLocalProperties().entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();
            if (isValidProperty(fieldName, fieldValue, poolMetaData) && !fieldName.equals(poolMetaData.getFieldMetaData().getJdbcUrlPropertiesFieldName())) {
                dataSourceReflection.setField(fieldName, fieldValue);
            }
        }
    }
    
    private static boolean isValidProperty(final String key, final Object value, final DataSourcePoolMetaData poolMetaData) {
        return !poolMetaData.getInvalidProperties().containsKey(key) || null == value || !value.equals(poolMetaData.getInvalidProperties().get(key));
    }
    
    @SuppressWarnings("unchecked")
    private static void appendJdbcUrlProperties(final CustomDataSourceProperties customDataSourceProps, final DataSource targetDataSource, final DataSourcePoolMetaData poolMetaData) {
        String jdbcUrlPropertiesFieldName = poolMetaData.getFieldMetaData().getJdbcUrlPropertiesFieldName();
        if (null != jdbcUrlPropertiesFieldName && customDataSourceProps.getProperties().containsKey(jdbcUrlPropertiesFieldName)) {
            Map<String, Object> jdbcUrlProps = (Map<String, Object>) customDataSourceProps.getProperties().get(jdbcUrlPropertiesFieldName);
            DataSourcePoolMetaDataReflection dataSourcePoolMetaDataReflection = new DataSourcePoolMetaDataReflection(targetDataSource, poolMetaData.getFieldMetaData());
            for (Entry<String, Object> entry : jdbcUrlProps.entrySet()) {
                dataSourcePoolMetaDataReflection.getJdbcConnectionProperties().setProperty(entry.getKey(), entry.getValue().toString());
            }
        }
    }
}
