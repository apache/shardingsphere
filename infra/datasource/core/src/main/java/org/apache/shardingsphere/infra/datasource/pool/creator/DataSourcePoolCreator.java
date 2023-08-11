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
import org.apache.shardingsphere.infra.database.core.GlobalDataSourceRegistry;
import org.apache.shardingsphere.infra.database.core.connector.url.JdbcUrl;
import org.apache.shardingsphere.infra.database.core.connector.url.StandardJdbcUrlParser;
import org.apache.shardingsphere.infra.database.core.connector.url.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaDataReflection;
import org.apache.shardingsphere.infra.datasource.pool.props.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.custom.CustomDataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.storage.StorageNode;
import org.apache.shardingsphere.infra.datasource.storage.StorageNodeProperties;
import org.apache.shardingsphere.infra.datasource.storage.StorageResource;
import org.apache.shardingsphere.infra.datasource.storage.StorageResourceWithProperties;
import org.apache.shardingsphere.infra.datasource.storage.StorageUnitNodeMapper;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
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
     * Create storage resource.
     *
     * @param propsMap data source pool properties map
     * @return created storage resource
     */
    public static StorageResource createStorageResource(final Map<String, DataSourcePoolProperties> propsMap) {
        return createStorageResource(propsMap, true);
    }
    
    /**
     * Create storage resource.
     *
     * @param propsMap data source pool properties map
     * @param cacheEnabled cache enabled
     * @return created storage resource
     */
    public static StorageResource createStorageResource(final Map<String, DataSourcePoolProperties> propsMap, final boolean cacheEnabled) {
        Map<StorageNode, DataSource> storageNodes = new LinkedHashMap<>();
        Map<String, StorageUnitNodeMapper> storageUnitNodeMappers = new LinkedHashMap<>();
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            StorageNodeProperties storageNodeProps = getStorageNodeProperties(entry.getKey(), entry.getValue());
            StorageNode storageNode = new StorageNode(storageNodeProps.getName());
            if (storageNodes.containsKey(storageNode)) {
                appendStorageUnitNodeMapper(storageUnitNodeMappers, storageNodeProps, entry.getKey(), entry.getValue());
                continue;
            }
            DataSource dataSource;
            try {
                dataSource = create(entry.getKey(), entry.getValue(), cacheEnabled);
                // CHECKSTYLE:OFF
            } catch (final RuntimeException ex) {
                // CHECKSTYLE:ON
                if (!cacheEnabled) {
                    storageNodes.values().stream().map(DataSourcePoolDestroyer::new).forEach(DataSourcePoolDestroyer::asyncDestroy);
                }
                throw ex;
            }
            storageNodes.put(storageNode, dataSource);
            appendStorageUnitNodeMapper(storageUnitNodeMappers, storageNodeProps, entry.getKey(), entry.getValue());
        }
        return new StorageResource(storageNodes, storageUnitNodeMappers);
    }
    
    /**
     * Create storage resource without data source.
     *
     * @param propsMap data source pool properties map
     * @return created storage resource
     */
    public static StorageResourceWithProperties createStorageResourceWithoutDataSource(final Map<String, DataSourcePoolProperties> propsMap) {
        Map<StorageNode, DataSource> storageNodes = new LinkedHashMap<>();
        Map<String, StorageUnitNodeMapper> storageUnitNodeMappers = new LinkedHashMap<>();
        Map<String, DataSourcePoolProperties> newPropsMap = new LinkedHashMap<>();
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            StorageNodeProperties storageNodeProperties = getStorageNodeProperties(entry.getKey(), entry.getValue());
            StorageNode storageNode = new StorageNode(storageNodeProperties.getName());
            if (storageNodes.containsKey(storageNode)) {
                appendStorageUnitNodeMapper(storageUnitNodeMappers, storageNodeProperties, entry.getKey(), entry.getValue());
                continue;
            }
            storageNodes.put(storageNode, null);
            appendStorageUnitNodeMapper(storageUnitNodeMappers, storageNodeProperties, entry.getKey(), entry.getValue());
            newPropsMap.put(storageNodeProperties.getName(), entry.getValue());
        }
        return new StorageResourceWithProperties(storageNodes, storageUnitNodeMappers, newPropsMap);
    }
    
    private static void appendStorageUnitNodeMapper(final Map<String, StorageUnitNodeMapper> storageUnitNodeMappers, final StorageNodeProperties storageNodeProps,
                                                    final String unitName, final DataSourcePoolProperties props) {
        String url = props.getConnectionPropertySynonyms().getStandardProperties().get("url").toString();
        storageUnitNodeMappers.put(unitName, getStorageUnitNodeMapper(storageNodeProps, unitName, url));
    }
    
    private static StorageUnitNodeMapper getStorageUnitNodeMapper(final StorageNodeProperties storageNodeProps, final String unitName, final String url) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(storageNodeProps.getDatabaseType()).getDialectDatabaseMetaData();
        return dialectDatabaseMetaData.isInstanceConnectionAvailable()
                ? new StorageUnitNodeMapper(unitName, new StorageNode(storageNodeProps.getName()), storageNodeProps.getCatalog(), url)
                : new StorageUnitNodeMapper(unitName, new StorageNode(storageNodeProps.getName()), url);
    }
    
    private static StorageNodeProperties getStorageNodeProperties(final String dataSourceName, final DataSourcePoolProperties storageNodeProps) {
        Map<String, Object> standardProperties = storageNodeProps.getConnectionPropertySynonyms().getStandardProperties();
        String url = standardProperties.get("url").toString();
        String username = standardProperties.get("username").toString();
        DatabaseType databaseType = DatabaseTypeFactory.get(url);
        return getStorageNodeProperties(dataSourceName, url, username, databaseType);
    }
    
    private static StorageNodeProperties getStorageNodeProperties(final String dataSourceName, final String url, final String username, final DatabaseType databaseType) {
        try {
            JdbcUrl jdbcUrl = new StandardJdbcUrlParser().parse(url);
            DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
            String nodeName = dialectDatabaseMetaData.isInstanceConnectionAvailable() ? generateStorageNodeName(jdbcUrl.getHostname(), jdbcUrl.getPort(), username) : dataSourceName;
            return new StorageNodeProperties(nodeName, databaseType, jdbcUrl.getDatabase());
        } catch (final UnrecognizedDatabaseURLException ex) {
            return new StorageNodeProperties(dataSourceName, databaseType, null);
        }
    }
    
    private static String generateStorageNodeName(final String hostname, final int port, final String username) {
        return String.format("%s_%s_%s", hostname, port, username);
    }
    
    /**
     * Create data sources.
     *
     * @param propsMap data source pool properties map
     * @return created data sources
     */
    public static Map<String, DataSource> create(final Map<String, DataSourcePoolProperties> propsMap) {
        return create(propsMap, true);
    }
    
    /**
     * Create data sources.
     *
     * @param propsMap data source pool properties map
     * @param cacheEnabled cache enabled
     * @return created data sources
     */
    public static Map<String, DataSource> create(final Map<String, DataSourcePoolProperties> propsMap, final boolean cacheEnabled) {
        Map<String, DataSource> result = new LinkedHashMap<>();
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            DataSource dataSource;
            try {
                dataSource = create(entry.getKey(), entry.getValue(), cacheEnabled);
                // CHECKSTYLE:OFF
            } catch (final RuntimeException ex) {
                // CHECKSTYLE:ON
                if (!cacheEnabled) {
                    result.values().stream().map(DataSourcePoolDestroyer::new).forEach(DataSourcePoolDestroyer::asyncDestroy);
                }
                throw ex;
            }
            result.put(entry.getKey(), dataSource);
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
        DataSource result = createDataSource(props.getPoolClassName());
        Optional<DataSourcePoolMetaData> poolMetaData = TypedSPILoader.findService(DataSourcePoolMetaData.class, props.getPoolClassName());
        DataSourceReflection dataSourceReflection = new DataSourceReflection(result);
        if (poolMetaData.isPresent()) {
            setDefaultFields(dataSourceReflection, poolMetaData.get());
            setConfiguredFields(props, dataSourceReflection, poolMetaData.get());
            appendJdbcUrlProperties(props.getCustomDataSourcePoolProperties(), result, poolMetaData.get(), dataSourceReflection);
            dataSourceReflection.addDefaultDataSourcePoolProperties(poolMetaData.get());
        } else {
            setConfiguredFields(props, dataSourceReflection);
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
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static DataSource createDataSource(final String dataSourceClassName) {
        return (DataSource) Class.forName(dataSourceClassName).getConstructor().newInstance();
    }
    
    private static void setDefaultFields(final DataSourceReflection dataSourceReflection, final DataSourcePoolMetaData poolMetaData) {
        for (Entry<String, Object> entry : poolMetaData.getDefaultProperties().entrySet()) {
            dataSourceReflection.setField(entry.getKey(), entry.getValue());
        }
    }
    
    private static void setConfiguredFields(final DataSourcePoolProperties props, final DataSourceReflection dataSourceReflection) {
        for (Entry<String, Object> entry : props.getAllLocalProperties().entrySet()) {
            dataSourceReflection.setField(entry.getKey(), entry.getValue());
        }
    }
    
    private static void setConfiguredFields(final DataSourcePoolProperties props, final DataSourceReflection dataSourceReflection, final DataSourcePoolMetaData poolMetaData) {
        for (Entry<String, Object> entry : props.getAllLocalProperties().entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();
            if (isValidProperty(fieldName, fieldValue, poolMetaData) && !fieldName.equals(poolMetaData.getFieldMetaData().getJdbcUrlPropertiesFieldName())) {
                dataSourceReflection.setField(fieldName, fieldValue);
            }
        }
    }
    
    private static boolean isValidProperty(final String key, final Object value, final DataSourcePoolMetaData poolMetaData) {
        return !poolMetaData.getSkippedProperties().containsKey(key) || null == value || !value.equals(poolMetaData.getSkippedProperties().get(key));
    }
    
    @SuppressWarnings("unchecked")
    private static void appendJdbcUrlProperties(final CustomDataSourcePoolProperties customPoolProps, final DataSource targetDataSource, final DataSourcePoolMetaData poolMetaData,
                                                final DataSourceReflection dataSourceReflection) {
        String jdbcUrlPropertiesFieldName = poolMetaData.getFieldMetaData().getJdbcUrlPropertiesFieldName();
        if (null != jdbcUrlPropertiesFieldName && customPoolProps.getProperties().containsKey(jdbcUrlPropertiesFieldName)) {
            Map<String, Object> jdbcUrlProps = (Map<String, Object>) customPoolProps.getProperties().get(jdbcUrlPropertiesFieldName);
            DataSourcePoolMetaDataReflection dataSourcePoolMetaDataReflection = new DataSourcePoolMetaDataReflection(targetDataSource, poolMetaData.getFieldMetaData());
            dataSourcePoolMetaDataReflection.getJdbcConnectionProperties().ifPresent(optional -> setJdbcUrlProperties(dataSourceReflection, optional, jdbcUrlProps, jdbcUrlPropertiesFieldName));
        }
    }
    
    private static void setJdbcUrlProperties(final DataSourceReflection dataSourceReflection, final Properties jdbcConnectionProps, final Map<String, Object> customProps,
                                             final String jdbcUrlPropertiesFieldName) {
        for (Entry<String, Object> entry : customProps.entrySet()) {
            jdbcConnectionProps.setProperty(entry.getKey(), entry.getValue().toString());
        }
        dataSourceReflection.setField(jdbcUrlPropertiesFieldName, jdbcConnectionProps);
    }
}
