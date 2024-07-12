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

package org.apache.shardingsphere.mode.metadata.manager;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.GenericSchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.metadata.factory.ExternalMetaDataFactory;
import org.apache.shardingsphere.metadata.factory.InternalMetaDataFactory;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Configuration context manager.
 */
@Slf4j
public final class ConfigurationManager {
    
    private final AtomicReference<MetaDataContexts> metaDataContexts;
    
    private final ComputeNodeInstanceContext computeNodeInstanceContext;
    
    private final MetaDataPersistService metaDataPersistService;
    
    public ConfigurationManager(final AtomicReference<MetaDataContexts> metaDataContexts, final ComputeNodeInstanceContext computeNodeInstanceContext,
                                final PersistRepository repository) {
        this.metaDataContexts = metaDataContexts;
        this.computeNodeInstanceContext = computeNodeInstanceContext;
        metaDataPersistService = new MetaDataPersistService(repository);
    }
    
    /**
     * Alter schema meta data.
     *
     * @param databaseName database name
     * @param reloadDatabase reload database
     * @param currentDatabase current database
     * @param isDropConfig is drop configuration
     */
    public void alterSchemaMetaData(final String databaseName, final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase, final boolean isDropConfig) {
        Map<String, ShardingSphereSchema> toBeAlterSchemas = GenericSchemaManager.getToBeDeletedTablesBySchemas(reloadDatabase.getSchemas(), currentDatabase.getSchemas());
        Map<String, ShardingSphereSchema> toBeAddedSchemas = GenericSchemaManager.getToBeAddedTablesBySchemas(reloadDatabase.getSchemas(), currentDatabase.getSchemas());
        if (isDropConfig) {
            toBeAddedSchemas.forEach((key, value) -> metaDataPersistService.getDatabaseMetaDataService().persistByDropConfiguration(databaseName, key, value));
        } else {
            toBeAddedSchemas.forEach((key, value) -> metaDataPersistService.getDatabaseMetaDataService().persistByAlterConfiguration(databaseName, key, value));
        }
        toBeAlterSchemas.forEach((key, value) -> metaDataPersistService.getDatabaseMetaDataService().delete(databaseName, key, value));
    }
    
    /**
     * Get effective resource meta data.
     *
     * @param database database
     * @param resource resource
     * @return effective resource meta data
     */
    public ResourceMetaData getEffectiveResourceMetaData(final ShardingSphereDatabase database, final SwitchingResource resource) {
        Map<StorageNode, DataSource> storageNodes = getStorageNodes(database.getResourceMetaData().getDataSources(), resource);
        Map<String, StorageUnit> storageUnits = getStorageUnits(database.getResourceMetaData().getStorageUnits(), resource);
        return new ResourceMetaData(storageNodes, storageUnits);
    }
    
    private Map<StorageNode, DataSource> getStorageNodes(final Map<StorageNode, DataSource> currentStorageNodes, final SwitchingResource resource) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(currentStorageNodes.size(), 1F);
        for (Entry<StorageNode, DataSource> entry : currentStorageNodes.entrySet()) {
            if (null == resource || !resource.getStaleDataSources().containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private Map<String, StorageUnit> getStorageUnits(final Map<String, StorageUnit> currentStorageUnits, final SwitchingResource resource) {
        Map<String, StorageUnit> result = new LinkedHashMap<>(currentStorageUnits.size(), 1F);
        for (Entry<String, StorageUnit> entry : currentStorageUnits.entrySet()) {
            if (null == resource || !resource.getStaleStorageUnitNames().contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    /**
     * Create meta data contexts.
     *
     * @param databaseName database name
     * @param internalLoadMetaData internal load meta data
     * @param switchingResource switching resource
     * @param ruleConfigs rule configs
     * @return MetaDataContexts meta data contexts
     * @throws SQLException SQL exception
     */
    public MetaDataContexts createMetaDataContexts(final String databaseName, final boolean internalLoadMetaData,
                                                   final SwitchingResource switchingResource, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        Map<String, ShardingSphereDatabase> changedDatabases = createChangedDatabases(databaseName, internalLoadMetaData, switchingResource, ruleConfigs);
        ConfigurationProperties props = metaDataContexts.get().getMetaData().getProps();
        RuleMetaData changedGlobalMetaData = new RuleMetaData(
                GlobalRulesBuilder.buildRules(metaDataContexts.get().getMetaData().getGlobalRuleMetaData().getConfigurations(), changedDatabases, props));
        return newMetaDataContexts(new ShardingSphereMetaData(changedDatabases, metaDataContexts.get().getMetaData().getGlobalResourceMetaData(), changedGlobalMetaData, props));
    }
    
    /**
     * Create changed databases.
     *
     * @param databaseName database name
     * @param internalLoadMetaData internal load meta data
     * @param switchingResource switching resource
     * @param ruleConfigs rule configs
     * @return ShardingSphere databases
     * @throws SQLException SQL exception
     */
    public synchronized Map<String, ShardingSphereDatabase> createChangedDatabases(final String databaseName, final boolean internalLoadMetaData,
                                                                                   final SwitchingResource switchingResource, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        ResourceMetaData effectiveResourceMetaData = getEffectiveResourceMetaData(metaDataContexts.get().getMetaData().getDatabase(databaseName), switchingResource);
        Collection<RuleConfiguration> toBeCreatedRuleConfigs = null == ruleConfigs
                ? metaDataContexts.get().getMetaData().getDatabase(databaseName).getRuleMetaData().getConfigurations()
                : ruleConfigs;
        DatabaseConfiguration toBeCreatedDatabaseConfig = getDatabaseConfiguration(effectiveResourceMetaData, switchingResource, toBeCreatedRuleConfigs);
        ShardingSphereDatabase changedDatabase = createChangedDatabase(metaDataContexts.get().getMetaData().getDatabase(databaseName).getName(), internalLoadMetaData,
                metaDataPersistService, toBeCreatedDatabaseConfig, metaDataContexts.get().getMetaData().getProps(), computeNodeInstanceContext);
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(metaDataContexts.get().getMetaData().getDatabases());
        result.put(databaseName.toLowerCase(), changedDatabase);
        return result;
    }
    
    private DatabaseConfiguration getDatabaseConfiguration(final ResourceMetaData resourceMetaData, final SwitchingResource switchingResource,
                                                           final Collection<RuleConfiguration> toBeCreatedRuleConfigs) {
        Map<String, DataSourcePoolProperties> propsMap = null == switchingResource
                ? resourceMetaData.getStorageUnits().entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSourcePoolProperties(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new))
                : switchingResource.getMergedDataSourcePoolPropertiesMap();
        return new DataSourceProvidedDatabaseConfiguration(getMergedStorageNodeDataSources(resourceMetaData, switchingResource), toBeCreatedRuleConfigs, propsMap);
    }
    
    private Map<StorageNode, DataSource> getMergedStorageNodeDataSources(final ResourceMetaData currentResourceMetaData, final SwitchingResource switchingResource) {
        Map<StorageNode, DataSource> result = currentResourceMetaData.getDataSources();
        if (null != switchingResource && !switchingResource.getNewDataSources().isEmpty()) {
            result.putAll(switchingResource.getNewDataSources());
        }
        return result;
    }
    
    private ShardingSphereDatabase createChangedDatabase(final String databaseName, final boolean internalLoadMetaData, final MetaDataPersistService persistService,
                                                         final DatabaseConfiguration databaseConfig, final ConfigurationProperties props,
                                                         final ComputeNodeInstanceContext computeNodeInstanceContext) throws SQLException {
        return internalLoadMetaData
                ? InternalMetaDataFactory.create(databaseName, persistService, databaseConfig, props, computeNodeInstanceContext)
                : ExternalMetaDataFactory.create(databaseName, databaseConfig, props, computeNodeInstanceContext);
    }
    
    private MetaDataContexts newMetaDataContexts(final ShardingSphereMetaData metaData) {
        return MetaDataContextsFactory.create(metaDataPersistService, metaData);
    }
}
