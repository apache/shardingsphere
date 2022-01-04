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

package org.apache.shardingsphere.infra.config.datasource.pool.creator;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.required.RequiredSPIRegistry;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Data source pool creator.
 */
@RequiredArgsConstructor
public final class DataSourcePoolCreator {
    
    static {
        ShardingSphereServiceLoader.register(DataSourcePoolCreationMetaData.class);
    }
    
    private final DataSourcePoolCreationMetaData creationMetaData;
    
    public DataSourcePoolCreator(final String dataSourceClassName) {
        creationMetaData = TypedSPIRegistry.findRegisteredService(DataSourcePoolCreationMetaData.class, dataSourceClassName, new Properties())
                .orElse(RequiredSPIRegistry.getRegisteredService(DataSourcePoolCreationMetaData.class));
    }
    
    /**
     * Create data source configuration.
     * 
     * @param dataSource data source
     * @return data source configuration
     */
    public DataSourceConfiguration createDataSourceConfiguration(final DataSource dataSource) {
        DataSourceConfiguration result = new DataSourceConfiguration(dataSource.getClass().getName());
        filterInvalidProperties(result, new DataSourceReflection(dataSource).convertToProperties());
        return result;
    }
    
    private void filterInvalidProperties(final DataSourceConfiguration dataSourceConfig, final Map<String, Object> reflectionProps) {
        for (Entry<String, Object> entry : reflectionProps.entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();
            if (isValidProperty(propertyName, propertyValue)) {
                dataSourceConfig.getProps().put(propertyName, propertyValue);
            }
        }
    }
    
    /**
     * Create data source.
     * 
     * @param dataSourceConfig data source configuration
     * @return data source
     */
    public DataSource createDataSource(final DataSourceConfiguration dataSourceConfig) {
        DataSource result = buildDataSource(dataSourceConfig.getDataSourceClassName());
        addPropertySynonym(dataSourceConfig);
        DataSourceReflection dataSourceReflection = new DataSourceReflection(result);
        setConfiguredFields(dataSourceConfig, dataSourceReflection);
        dataSourceReflection.addDefaultDataSourceProperties(
                creationMetaData.getDataSourcePropertiesFieldName(), creationMetaData.getJdbcUrlFieldName(), creationMetaData.getDefaultDataSourceProperties());
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private DataSource buildDataSource(final String dataSourceClassName) {
        return (DataSource) Class.forName(dataSourceClassName).getConstructor().newInstance();
    }
    
    private void addPropertySynonym(final DataSourceConfiguration dataSourceConfig) {
        for (Entry<String, String> entry : creationMetaData.getPropertySynonyms().entrySet()) {
            dataSourceConfig.addPropertySynonym(entry.getKey(), entry.getValue());
        }
    }
    
    private void setConfiguredFields(final DataSourceConfiguration dataSourceConfig, final DataSourceReflection dataSourceReflection) {
        for (Entry<String, Object> entry : dataSourceConfig.getAllProperties().entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();
            if (isValidProperty(fieldName, fieldValue)) {
                dataSourceReflection.setField(fieldName, fieldValue);
            }
        }
    }
    
    private boolean isValidProperty(final String key, final Object value) {
        return !creationMetaData.getInvalidProperties().containsKey(key) || null == value || !value.equals(creationMetaData.getInvalidProperties().get(key));
    }
}
