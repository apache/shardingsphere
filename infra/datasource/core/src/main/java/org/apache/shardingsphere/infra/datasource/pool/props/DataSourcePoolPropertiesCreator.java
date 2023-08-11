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

package org.apache.shardingsphere.infra.datasource.pool.props;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.datasource.pool.config.ConnectionConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.PoolConfiguration;
import org.apache.shardingsphere.infra.datasource.CatalogSwitchableDataSource;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourceReflection;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.pool.props.custom.CustomDataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.synonym.ConnectionPropertySynonyms;
import org.apache.shardingsphere.infra.datasource.pool.props.synonym.PoolPropertySynonyms;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Data source pool properties creator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourcePoolPropertiesCreator {
    
    /**
     * Create data source pool properties.
     *
     * @param dataSourceConfigs data source configurations
     * @return created data source pool properties
     */
    public static Map<String, DataSourcePoolProperties> createFromConfiguration(final Map<String, DataSourceConfiguration> dataSourceConfigs) {
        return dataSourceConfigs.entrySet().stream().collect(Collectors
                .toMap(Entry::getKey, entry -> create(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    /**
     * Create data source properties.
     *
     * @param dataSourceConfig data source configuration
     * @return created data source properties
     */
    public static DataSourcePoolProperties create(final DataSourceConfiguration dataSourceConfig) {
        return new DataSourcePoolProperties(dataSourceConfig.getConnection().getDataSourceClassName(), createProperties(dataSourceConfig));
    }
    
    /**
     * Create data source properties.
     *
     * @param dataSources data sources
     * @return created data source properties
     */
    public static Map<String, DataSourcePoolProperties> create(final Map<String, DataSource> dataSources) {
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>();
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            result.put(entry.getKey(), create(entry.getValue()));
        }
        return result;
    }
    
    /**
     * Create data source properties.
     * 
     * @param dataSource data source
     * @return created data source properties
     */
    public static DataSourcePoolProperties create(final DataSource dataSource) {
        return dataSource instanceof CatalogSwitchableDataSource
                ? new DataSourcePoolProperties(((CatalogSwitchableDataSource) dataSource).getDataSource().getClass().getName(),
                        createProperties(((CatalogSwitchableDataSource) dataSource).getDataSource()))
                : new DataSourcePoolProperties(dataSource.getClass().getName(), createProperties(dataSource));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, Object> createProperties(final DataSourceConfiguration dataSourceConfig) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dataSourceClassName", dataSourceConfig.getConnection().getDataSourceClassName());
        result.put("url", dataSourceConfig.getConnection().getUrl());
        result.put("username", dataSourceConfig.getConnection().getUsername());
        result.put("password", dataSourceConfig.getConnection().getPassword());
        result.put("connectionTimeoutMilliseconds", dataSourceConfig.getPool().getConnectionTimeoutMilliseconds());
        result.put("idleTimeoutMilliseconds", dataSourceConfig.getPool().getIdleTimeoutMilliseconds());
        result.put("maxLifetimeMilliseconds", dataSourceConfig.getPool().getMaxLifetimeMilliseconds());
        result.put("maxPoolSize", dataSourceConfig.getPool().getMaxPoolSize());
        result.put("minPoolSize", dataSourceConfig.getPool().getMinPoolSize());
        result.put("readOnly", dataSourceConfig.getPool().getReadOnly());
        if (null != dataSourceConfig.getPool().getCustomProperties()) {
            result.putAll((Map) dataSourceConfig.getPool().getCustomProperties());
        }
        return result;
    }
    
    private static Map<String, Object> createProperties(final DataSource dataSource) {
        Map<String, Object> result = new LinkedHashMap<>();
        Optional<DataSourcePoolMetaData> poolMetaData = TypedSPILoader.findService(DataSourcePoolMetaData.class, dataSource.getClass().getName());
        for (Entry<String, Object> entry : new DataSourceReflection(dataSource).convertToProperties().entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();
            if (!poolMetaData.isPresent() || isValidProperty(propertyName, propertyValue, poolMetaData.get()) && !poolMetaData.get().getTransientFieldNames().contains(propertyName)) {
                result.put(propertyName, propertyValue);
            }
        }
        return result;
    }
    
    private static boolean isValidProperty(final String key, final Object value, final DataSourcePoolMetaData poolMetaData) {
        return !poolMetaData.getSkippedProperties().containsKey(key) || null == value || !value.equals(poolMetaData.getSkippedProperties().get(key));
    }
    
    /**
     * Create data source configuration.
     *
     * @param dataSourceProps data source properties
     * @return created data source configuration
     */
    public static DataSourceConfiguration createConfiguration(final DataSourcePoolProperties dataSourceProps) {
        return new DataSourceConfiguration(getConnectionConfiguration(dataSourceProps.getConnectionPropertySynonyms()),
                getPoolConfiguration(dataSourceProps.getPoolPropertySynonyms(), dataSourceProps.getCustomDataSourcePoolProperties()));
    }
    
    private static ConnectionConfiguration getConnectionConfiguration(final ConnectionPropertySynonyms connectionPropertySynonyms) {
        Map<String, Object> standardProperties = connectionPropertySynonyms.getStandardProperties();
        return new ConnectionConfiguration((String) standardProperties.get("dataSourceClassName"), (String) standardProperties.get("url"),
                (String) standardProperties.get("username"), (String) standardProperties.get("password"));
    }
    
    private static PoolConfiguration getPoolConfiguration(final PoolPropertySynonyms poolPropertySynonyms, final CustomDataSourcePoolProperties customDataSourcePoolProperties) {
        Map<String, Object> standardProperties = poolPropertySynonyms.getStandardProperties();
        Long connectionTimeoutMilliseconds = toLong(standardProperties, "connectionTimeoutMilliseconds", null);
        Long idleTimeoutMilliseconds = toLong(standardProperties, "idleTimeoutMilliseconds", null);
        Long maxLifetimeMilliseconds = toLong(standardProperties, "maxLifetimeMilliseconds", null);
        Integer maxPoolSize = toInt(standardProperties, "maxPoolSize", null);
        Integer minPoolSize = toInt(standardProperties, "minPoolSize", null);
        Boolean readOnly = toBoolean(standardProperties, "readOnly", null);
        Properties customProperties = new Properties();
        customProperties.putAll(customDataSourcePoolProperties.getProperties());
        return new PoolConfiguration(connectionTimeoutMilliseconds, idleTimeoutMilliseconds, maxLifetimeMilliseconds, maxPoolSize, minPoolSize, readOnly, customProperties);
    }
    
    private static Long toLong(final Map<String, Object> properties, final String name, final Long defaultValue) {
        if (!properties.containsKey(name)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(String.valueOf(properties.get(name)));
        } catch (final NumberFormatException ex) {
            return defaultValue;
        }
    }
    
    private static Integer toInt(final Map<String, Object> properties, final String name, final Integer defaultValue) {
        if (!properties.containsKey(name)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(properties.get(name)));
        } catch (final NumberFormatException ex) {
            return defaultValue;
        }
    }
    
    private static Boolean toBoolean(final Map<String, Object> properties, final String name, final Boolean defaultValue) {
        if (!properties.containsKey(name)) {
            return defaultValue;
        }
        try {
            return Boolean.parseBoolean(String.valueOf(properties.get(name)));
        } catch (final NumberFormatException ex) {
            return defaultValue;
        }
    }
}
