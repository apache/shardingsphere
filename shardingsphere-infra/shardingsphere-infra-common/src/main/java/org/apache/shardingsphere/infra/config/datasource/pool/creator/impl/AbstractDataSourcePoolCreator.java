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

package org.apache.shardingsphere.infra.config.datasource.pool.creator.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceReflection;
import org.apache.shardingsphere.infra.config.datasource.pool.creator.DataSourcePoolCreator;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Abstract data source pool creator.
 */
public abstract class AbstractDataSourcePoolCreator implements DataSourcePoolCreator {
    
    @Override
    public final DataSourceConfiguration createDataSourceConfiguration(final DataSource dataSource) {
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
    
    @Override
    public final DataSource createDataSource(final DataSourceConfiguration dataSourceConfig) {
        DataSource result = buildDataSource(dataSourceConfig.getDataSourceClassName());
        addPropertySynonym(dataSourceConfig);
        DataSourceReflection dataSourceReflection = new DataSourceReflection(result);
        setConfiguredFields(dataSourceConfig, dataSourceReflection);
        dataSourceReflection.addDefaultDataSourceProperties(getDataSourcePropertiesFieldName(), getJdbcUrlFieldName(), getDefaultDataSourceProperties());
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    protected final DataSource buildDataSource(final String dataSourceClassName) {
        return (DataSource) Class.forName(dataSourceClassName).getConstructor().newInstance();
    }
    
    private void addPropertySynonym(final DataSourceConfiguration dataSourceConfig) {
        for (Entry<String, String> entry : getPropertySynonyms().entrySet()) {
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
        return !getInvalidProperties().containsKey(key) || null == value || !value.equals(getInvalidProperties().get(key));
    }
    
    protected abstract Map<String, Object> getInvalidProperties();
    
    protected abstract Map<String, String> getPropertySynonyms();
    
    protected abstract String getDataSourcePropertiesFieldName();
    
    protected abstract String getJdbcUrlFieldName();
    
    protected abstract Properties getDefaultDataSourceProperties();
}
