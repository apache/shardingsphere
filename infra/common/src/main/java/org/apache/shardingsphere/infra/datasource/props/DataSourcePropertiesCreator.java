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

package org.apache.shardingsphere.infra.datasource.props;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.datasource.config.ConnectionConfiguration;
import org.apache.shardingsphere.infra.datasource.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.config.PoolConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourceReflection;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.props.custom.CustomDataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.synonym.ConnectionPropertySynonyms;
import org.apache.shardingsphere.infra.datasource.props.synonym.PoolPropertySynonyms;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Data source properties creator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourcePropertiesCreator {
    
    /**
     * Create data source properties.
     *
     * @param dataSourceConfigs data source configurations
     * @return created data source properties
     */
    public static Map<String, DataSourceProperties> createFromConfiguration(final Map<String, DataSourceConfiguration> dataSourceConfigs) {
        return dataSourceConfigs.entrySet().stream().collect(Collectors
                .toMap(Entry::getKey, entry -> create("com.zaxxer.hikari.HikariDataSource", entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    /**
     * Create data source properties.
     *
     * @param dataSourcePoolClassName data source pool class name
     * @param dataSourceConfig data source configuration
     * @return created data source properties
     */
    public static DataSourceProperties create(final String dataSourcePoolClassName, final DataSourceConfiguration dataSourceConfig) {
        return new DataSourceProperties(dataSourcePoolClassName, createProperties(dataSourceConfig));
    }
    
    /**
     * Create data source properties.
     *
     * @param dataSources data sources
     * @return created data source properties
     */
    public static Map<String, DataSourceProperties> create(final Map<String, DataSource> dataSources) {
        Map<String, DataSourceProperties> result = new LinkedHashMap<>();
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
    public static DataSourceProperties create(final DataSource dataSource) {
        return new DataSourceProperties(dataSource.getClass().getName(), createProperties(dataSource));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, Object> createProperties(final DataSourceConfiguration dataSourceConfig) {
        Map<String, Object> result = new LinkedHashMap<>();
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
        return !poolMetaData.getInvalidProperties().containsKey(key) || null == value || !value.equals(poolMetaData.getInvalidProperties().get(key));
    }
    
    /**
     * Create data source configuration.
     *
     * @param dataSourceProps data source properties
     * @return created data source configuration
     */
    public static DataSourceConfiguration createConfiguration(final DataSourceProperties dataSourceProps) {
        return new DataSourceConfiguration(getConnectionConfiguration(dataSourceProps.getConnectionPropertySynonyms()),
                getPoolConfiguration(dataSourceProps.getPoolPropertySynonyms(), dataSourceProps.getCustomDataSourceProperties()));
    }
    
    private static ConnectionConfiguration getConnectionConfiguration(final ConnectionPropertySynonyms connectionPropertySynonyms) {
        Map<String, Object> standardProperties = connectionPropertySynonyms.getStandardProperties();
        return new ConnectionConfiguration((String) standardProperties.get("url"), (String) standardProperties.get("username"), (String) standardProperties.get("password"));
    }
    
    private static PoolConfiguration getPoolConfiguration(final PoolPropertySynonyms poolPropertySynonyms, final CustomDataSourceProperties customDataSourceProperties) {
        Map<String, Object> standardProperties = poolPropertySynonyms.getStandardProperties();
        Long connectionTimeoutMilliseconds = standardProperties.containsKey("connectionTimeoutMilliseconds")
                ? Long.valueOf(String.valueOf(standardProperties.get("connectionTimeoutMilliseconds")))
                : null;
        Long idleTimeoutMilliseconds = standardProperties.containsKey("idleTimeoutMilliseconds") ? Long.valueOf(String.valueOf(standardProperties.get("idleTimeoutMilliseconds"))) : null;
        Long maxLifetimeMilliseconds = standardProperties.containsKey("maxLifetimeMilliseconds") ? Long.valueOf(String.valueOf(standardProperties.get("maxLifetimeMilliseconds"))) : null;
        Integer maxPoolSize = standardProperties.containsKey("maxPoolSize") ? Integer.valueOf(String.valueOf(standardProperties.get("maxPoolSize"))) : null;
        Integer minPoolSize = standardProperties.containsKey("minPoolSize") ? Integer.valueOf(String.valueOf(standardProperties.get("minPoolSize"))) : null;
        Boolean readOnly = standardProperties.containsKey("readOnly") ? Boolean.valueOf(String.valueOf(standardProperties.get("readOnly"))) : null;
        Properties customProperties = new Properties();
        customProperties.putAll(customDataSourceProperties.getProperties());
        return new PoolConfiguration(connectionTimeoutMilliseconds, idleTimeoutMilliseconds, maxLifetimeMilliseconds, maxPoolSize, minPoolSize, readOnly, customProperties);
    }
}
