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

import lombok.SneakyThrows;
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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Storage unit manager.
 */
@Slf4j
public final class StorageUnitManager {
    
    private final AtomicReference<MetaDataContexts> metaDataContexts;
    
    private final ComputeNodeInstanceContext computeNodeInstanceContext;
    
    private final ResourceSwitchManager resourceSwitchManager;
    
    private final MetaDataPersistService metaDataPersistService;
    
    public StorageUnitManager(final AtomicReference<MetaDataContexts> metaDataContexts, final ComputeNodeInstanceContext computeNodeInstanceContext,
                              final PersistRepository repository, final ResourceSwitchManager resourceSwitchManager) {
        this.metaDataContexts = metaDataContexts;
        this.computeNodeInstanceContext = computeNodeInstanceContext;
        this.resourceSwitchManager = resourceSwitchManager;
        metaDataPersistService = new MetaDataPersistService(repository);
    }
    
    /**
     * Register storage unit.
     *
     * @param databaseName database name
     * @param propsMap data source pool properties map
     */
    public synchronized void registerStorageUnit(final String databaseName, final Map<String, DataSourcePoolProperties> propsMap) {
        try {
            closeStaleRules(databaseName);
            SwitchingResource switchingResource = resourceSwitchManager.switchByRegisterStorageUnit(metaDataContexts.get().getMetaData().getDatabase(databaseName).getResourceMetaData(), propsMap);
            buildNewMetaDataContext(databaseName, switchingResource);
        } catch (final SQLException ex) {
            log.error("Alter database: {} register storage unit failed", databaseName, ex);
        }
    }
    
    /**
     * Alter storage unit.
     *
     * @param databaseName database name
     * @param propsMap data source pool properties map
     */
    public synchronized void alterStorageUnit(final String databaseName, final Map<String, DataSourcePoolProperties> propsMap) {
        try {
            closeStaleRules(databaseName);
            SwitchingResource switchingResource = resourceSwitchManager.switchByAlterStorageUnit(metaDataContexts.get().getMetaData().getDatabase(databaseName).getResourceMetaData(), propsMap);
            buildNewMetaDataContext(databaseName, switchingResource);
        } catch (final SQLException ex) {
            log.error("Alter database: {} register storage unit failed", databaseName, ex);
        }
    }
    
    /**
     * UnRegister storage unit.
     *
     * @param databaseName database name
     * @param storageUnitName storage unit name
     */
    public synchronized void unregisterStorageUnit(final String databaseName, final String storageUnitName) {
        try {
            closeStaleRules(databaseName);
            SwitchingResource switchingResource = resourceSwitchManager.switchByUnregisterStorageUnit(metaDataContexts.get().getMetaData().getDatabase(databaseName).getResourceMetaData(),
                    Collections.singletonList(storageUnitName));
            buildNewMetaDataContext(databaseName, switchingResource);
        } catch (final SQLException ex) {
            log.error("Alter database: {} register storage unit failed", databaseName, ex);
        }
    }
    
    private void buildNewMetaDataContext(final String databaseName, final SwitchingResource switchingResource) throws SQLException {
        MetaDataContexts reloadMetaDataContexts = createMetaDataContexts(databaseName, false, switchingResource, null);
        metaDataContexts.set(reloadMetaDataContexts);
        metaDataContexts.get().getMetaData().getDatabases().putAll(buildShardingSphereDatabase(reloadMetaDataContexts.getMetaData().getDatabase(databaseName)));
        switchingResource.closeStaleDataSources();
    }
    
    private MetaDataContexts createMetaDataContexts(final String databaseName, final boolean internalLoadMetaData,
                                                    final SwitchingResource switchingResource, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        Map<String, ShardingSphereDatabase> changedDatabases = createChangedDatabases(databaseName, internalLoadMetaData, switchingResource, ruleConfigs);
        ConfigurationProperties props = metaDataContexts.get().getMetaData().getProps();
        RuleMetaData changedGlobalMetaData = new RuleMetaData(
                GlobalRulesBuilder.buildRules(metaDataContexts.get().getMetaData().getGlobalRuleMetaData().getConfigurations(), changedDatabases, props));
        return newMetaDataContexts(new ShardingSphereMetaData(changedDatabases, metaDataContexts.get().getMetaData().getGlobalResourceMetaData(), changedGlobalMetaData, props));
    }
    
    private synchronized Map<String, ShardingSphereDatabase> createChangedDatabases(final String databaseName, final boolean internalLoadMetaData,
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
    
    private ResourceMetaData getEffectiveResourceMetaData(final ShardingSphereDatabase database, final SwitchingResource resource) {
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
    
    private DatabaseConfiguration getDatabaseConfiguration(final ResourceMetaData resourceMetaData, final SwitchingResource switchingResource,
                                                           final Collection<RuleConfiguration> toBeCreatedRuleConfigs) {
        Map<String, DataSourcePoolProperties> propsMap = null == switchingResource ? resourceMetaData.getStorageUnits().entrySet().stream()
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
    
    private Map<String, ShardingSphereDatabase> buildShardingSphereDatabase(final ShardingSphereDatabase originalDatabase) {
        return Collections.singletonMap(originalDatabase.getName().toLowerCase(), new ShardingSphereDatabase(originalDatabase.getName(),
                originalDatabase.getProtocolType(), originalDatabase.getResourceMetaData(), originalDatabase.getRuleMetaData(), buildSchemas(originalDatabase)));
    }
    
    private Map<String, ShardingSphereSchema> buildSchemas(final ShardingSphereDatabase originalDatabase) {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(originalDatabase.getSchemas().size(), 1F);
        originalDatabase.getSchemas().keySet().forEach(schemaName -> result.put(schemaName.toLowerCase(),
                new ShardingSphereSchema(originalDatabase.getSchema(schemaName).getTables(),
                        metaDataPersistService.getDatabaseMetaDataService().getViewMetaDataPersistService().load(originalDatabase.getName(), schemaName))));
        return result;
    }
    
    @SneakyThrows(Exception.class)
    private void closeStaleRules(final String databaseName) {
        for (ShardingSphereRule each : metaDataContexts.get().getMetaData().getDatabase(databaseName).getRuleMetaData().getRules()) {
            if (each instanceof AutoCloseable) {
                ((AutoCloseable) each).close();
            }
        }
    }
    
    private MetaDataContexts newMetaDataContexts(final ShardingSphereMetaData metaData) {
        return MetaDataContextsFactory.create(metaDataPersistService, metaData);
    }
}
