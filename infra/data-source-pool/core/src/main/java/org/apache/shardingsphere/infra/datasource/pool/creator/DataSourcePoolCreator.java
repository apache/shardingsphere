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
import org.apache.shardingsphere.database.connector.core.GlobalDataSourceRegistry;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaDataReflection;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.custom.CustomDataSourcePoolProperties;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

/**
 * Data source pool creator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourcePoolCreator {
    
    /**
     * Create data sources.
     *
     * @param propsMap data source pool properties map
     * @param cacheEnabled cache enabled
     * @return created data sources
     */
    public static Map<String, DataSource> create(final Map<String, DataSourcePoolProperties> propsMap, final boolean cacheEnabled) {
        Map<String, DataSource> result = new LinkedHashMap<>(propsMap.size(), 1F);
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            result.put(entry.getKey(), create(entry.getKey(), entry.getValue(), cacheEnabled, result.values()));
        }
        return result;
    }
    
    /**
     * Create data source.
     *
     * @param props data source pool properties
     * @return created data source
     */
    public static DataSource create(final DataSourcePoolProperties props) {
        DataSource result = create(props.getPoolClassName());
        Optional<DataSourcePoolMetaData> poolMetaData = TypedSPILoader.findService(DataSourcePoolMetaData.class, props.getPoolClassName());
        DataSourcePoolReflection dataSourcePoolReflection = new DataSourcePoolReflection(result);
        if (poolMetaData.isPresent()) {
            setDefaultFields(dataSourcePoolReflection, poolMetaData.get());
            setConfiguredFields(props, dataSourcePoolReflection, poolMetaData.get());
            appendJdbcUrlProperties(props.getCustomProperties(), result, poolMetaData.get(), dataSourcePoolReflection);
            dataSourcePoolReflection.addDefaultDataSourcePoolProperties(poolMetaData.get());
        } else {
            setConfiguredFields(props, dataSourcePoolReflection);
        }
        return result;
    }
    
    /**
     * Create data source.
     *
     * @param dataSourceName data source name
     * @param props data source pool properties
     * @param cacheEnabled cache enabled
     * @return created data source
     */
    public static DataSource create(final String dataSourceName, final DataSourcePoolProperties props, final boolean cacheEnabled) {
        DataSource result = create(props);
        if (cacheEnabled && !GlobalDataSourceRegistry.getInstance().getCachedDataSources().containsKey(dataSourceName)) {
            GlobalDataSourceRegistry.getInstance().getCachedDataSources().put(dataSourceName, result);
        }
        return result;
    }
    
    /**
     * Create data source.
     *
     * @param dataSourceName data source name
     * @param props data source pool properties
     * @param cacheEnabled cache enabled
     * @param storageNodes storage nodes
     * @return created data source
     */
    public static DataSource create(final String dataSourceName, final DataSourcePoolProperties props, final boolean cacheEnabled, final Collection<DataSource> storageNodes) {
        try {
            return create(dataSourceName, props, cacheEnabled);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            if (!cacheEnabled) {
                storageNodes.stream().map(DataSourcePoolDestroyer::new).forEach(DataSourcePoolDestroyer::asyncDestroy);
            }
            throw ex;
        }
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static DataSource create(final String dataSourceClassName) {
        return (DataSource) Class.forName(dataSourceClassName).getConstructor().newInstance();
    }
    
    private static void setDefaultFields(final DataSourcePoolReflection dataSourcePoolReflection, final DataSourcePoolMetaData poolMetaData) {
        for (Entry<String, Object> entry : poolMetaData.getDefaultProperties().entrySet()) {
            dataSourcePoolReflection.setField(entry.getKey(), entry.getValue());
        }
    }
    
    private static void setConfiguredFields(final DataSourcePoolProperties props, final DataSourcePoolReflection dataSourcePoolReflection) {
        for (Entry<String, Object> entry : props.getAllLocalProperties().entrySet()) {
            dataSourcePoolReflection.setField(entry.getKey(), entry.getValue());
        }
    }
    
    private static void setConfiguredFields(final DataSourcePoolProperties props, final DataSourcePoolReflection dataSourcePoolReflection, final DataSourcePoolMetaData poolMetaData) {
        for (Entry<String, Object> entry : props.getAllLocalProperties().entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();
            if (isValidProperty(fieldName, fieldValue, poolMetaData) && !fieldName.equals(poolMetaData.getFieldMetaData().getJdbcUrlPropertiesFieldName())) {
                dataSourcePoolReflection.setField(fieldName, fieldValue);
            }
        }
    }
    
    private static boolean isValidProperty(final String key, final Object value, final DataSourcePoolMetaData poolMetaData) {
        return !poolMetaData.getSkippedProperties().containsKey(key) || null == value || !value.equals(poolMetaData.getSkippedProperties().get(key));
    }
    
    @SuppressWarnings("unchecked")
    private static void appendJdbcUrlProperties(final CustomDataSourcePoolProperties customPoolProps, final DataSource targetDataSource, final DataSourcePoolMetaData poolMetaData,
                                                final DataSourcePoolReflection dataSourcePoolReflection) {
        String jdbcUrlPropertiesFieldName = poolMetaData.getFieldMetaData().getJdbcUrlPropertiesFieldName();
        if (null != jdbcUrlPropertiesFieldName && customPoolProps.getProperties().containsKey(jdbcUrlPropertiesFieldName)) {
            Map<String, Object> jdbcUrlProps = (Map<String, Object>) customPoolProps.getProperties().get(jdbcUrlPropertiesFieldName);
            DataSourcePoolMetaDataReflection dataSourcePoolMetaDataReflection = new DataSourcePoolMetaDataReflection(targetDataSource, poolMetaData.getFieldMetaData());
            dataSourcePoolMetaDataReflection.getJdbcConnectionProperties().ifPresent(optional -> setJdbcUrlProperties(dataSourcePoolReflection, optional, jdbcUrlProps, jdbcUrlPropertiesFieldName));
        }
    }
    
    private static void setJdbcUrlProperties(final DataSourcePoolReflection dataSourcePoolReflection, final Properties jdbcConnectionProps, final Map<String, Object> customProps,
                                             final String jdbcUrlPropertiesFieldName) {
        for (Entry<String, Object> entry : customProps.entrySet()) {
            jdbcConnectionProps.setProperty(entry.getKey(), entry.getValue().toString());
        }
        dataSourcePoolReflection.setField(jdbcUrlPropertiesFieldName, jdbcConnectionProps);
    }
}
