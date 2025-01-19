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

package org.apache.shardingsphere.mode.metadata;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceGeneratedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.metadata.manager.SwitchingResource;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Meta data contexts factory.
 */
@RequiredArgsConstructor
public final class MetaDataContextsFactory {
    
    private final MetaDataPersistService persistService;
    
    /**
     * Create meta data contexts.
     *
     * @param param context manager builder parameter
     * @param instanceContext compute node instance context
     * @return meta data contexts
     * @throws SQLException SQL exception
     */
    public MetaDataContexts create(final ContextManagerBuilderParameter param, final ComputeNodeInstanceContext instanceContext) throws SQLException {
        return containsRegisteredDatabases() ? createFromRegisterCenter(param, instanceContext) : createFromLocalConfiguration(param, instanceContext);
    }
    
    private boolean containsRegisteredDatabases() {
        return !persistService.getDatabaseMetaDataFacade().getDatabase().loadAllDatabaseNames().isEmpty();
    }
    
    private MetaDataContexts createFromRegisterCenter(final ContextManagerBuilderParameter param, final ComputeNodeInstanceContext instanceContext) {
        Map<String, DatabaseConfiguration> effectiveDatabaseConfigs = createEffectiveDatabaseConfigurations(
                getDatabaseNames(param.getDatabaseConfigs(), instanceContext), param.getDatabaseConfigs());
        Collection<RuleConfiguration> globalRuleConfigs = persistService.getGlobalRuleService().load();
        // TODO load global data sources from persist service
        Map<String, DataSource> globalDataSources = param.getGlobalDataSources();
        ConfigurationProperties props = new ConfigurationProperties(persistService.getPropsService().load());
        Collection<ShardingSphereDatabase> databases = ShardingSphereDatabasesFactory.create(
                effectiveDatabaseConfigs, loadSchemas(effectiveDatabaseConfigs.keySet()), props, instanceContext);
        return createMetaDataContexts(globalRuleConfigs, globalDataSources, databases, props);
    }
    
    private MetaDataContexts createFromLocalConfiguration(final ContextManagerBuilderParameter param, final ComputeNodeInstanceContext instanceContext) throws SQLException {
        ConfigurationProperties props = new ConfigurationProperties(param.getProps());
        Collection<ShardingSphereDatabase> databases = ShardingSphereDatabasesFactory.create(param.getDatabaseConfigs(), props, instanceContext);
        MetaDataContexts result = createMetaDataContexts(param.getGlobalRuleConfigs(), param.getGlobalDataSources(), databases, props);
        persistDatabaseConfigurations(result, param);
        persistMetaData(result);
        return result;
    }
    
    private MetaDataContexts createMetaDataContexts(final Collection<RuleConfiguration> globalRuleConfigs, final Map<String, DataSource> globalDataSources,
                                                    final Collection<ShardingSphereDatabase> databases, final ConfigurationProperties props) {
        Collection<ShardingSphereRule> globalRules = GlobalRulesBuilder.buildRules(globalRuleConfigs, databases, props);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, new ResourceMetaData(globalDataSources), new RuleMetaData(globalRules), props);
        ShardingSphereStatistics statistics = ShardingSphereStatisticsFactory.create(metaData, persistService.getShardingSphereDataPersistService().load(metaData));
        return new MetaDataContexts(metaData, statistics);
    }
    
    private void persistDatabaseConfigurations(final MetaDataContexts metadataContexts, final ContextManagerBuilderParameter param) {
        Collection<RuleConfiguration> globalRuleConfigs = metadataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations();
        persistService.persistGlobalRuleConfiguration(globalRuleConfigs, param.getProps());
        for (Entry<String, ? extends DatabaseConfiguration> entry : param.getDatabaseConfigs().entrySet()) {
            String databaseName = entry.getKey();
            persistService.persistConfigurations(entry.getKey(), entry.getValue(),
                    metadataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData().getStorageUnits().entrySet().stream()
                            .collect(Collectors.toMap(Entry::getKey, each -> each.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)),
                    metadataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getRules());
        }
    }
    
    private void persistMetaData(final MetaDataContexts metaDataContexts) {
        metaDataContexts.getMetaData().getAllDatabases().forEach(each -> each.getAllSchemas().forEach(schema -> {
            if (schema.isEmpty()) {
                persistService.getDatabaseMetaDataFacade().getSchema().add(each.getName(), schema.getName());
            }
            persistService.getDatabaseMetaDataFacade().getTable().persist(each.getName(), schema.getName(), schema.getAllTables());
        }));
        for (Entry<String, ShardingSphereDatabaseData> databaseDataEntry : metaDataContexts.getStatistics().getDatabaseData().entrySet()) {
            for (Entry<String, ShardingSphereSchemaData> schemaDataEntry : databaseDataEntry.getValue().getSchemaData().entrySet()) {
                persistService.getShardingSphereDataPersistService().persist(
                        metaDataContexts.getMetaData().getDatabase(databaseDataEntry.getKey()), schemaDataEntry.getKey(), schemaDataEntry.getValue());
            }
        }
    }
    
    private Collection<String> getDatabaseNames(final Map<String, DatabaseConfiguration> databaseConfigs, final ComputeNodeInstanceContext instanceContext) {
        return instanceContext.getInstance().getMetaData() instanceof JDBCInstanceMetaData
                ? databaseConfigs.keySet()
                : persistService.getDatabaseMetaDataFacade().getDatabase().loadAllDatabaseNames();
    }
    
    private Map<String, DatabaseConfiguration> createEffectiveDatabaseConfigurations(final Collection<String> databaseNames, final Map<String, DatabaseConfiguration> databaseConfigs) {
        return databaseNames.stream().collect(Collectors.toMap(each -> each, each -> createEffectiveDatabaseConfiguration(each, databaseConfigs)));
    }
    
    private DatabaseConfiguration createEffectiveDatabaseConfiguration(final String databaseName, final Map<String, DatabaseConfiguration> databaseConfigs) {
        closeGeneratedDataSources(databaseName, databaseConfigs);
        Map<String, DataSourceConfiguration> dataSources = persistService.loadDataSourceConfigurations(databaseName);
        Collection<RuleConfiguration> databaseRuleConfigs = persistService.getDatabaseRulePersistService().load(databaseName);
        return new DataSourceGeneratedDatabaseConfiguration(dataSources, databaseRuleConfigs);
    }
    
    private void closeGeneratedDataSources(final String databaseName, final Map<String, ? extends DatabaseConfiguration> databaseConfigs) {
        if (databaseConfigs.containsKey(databaseName) && !databaseConfigs.get(databaseName).getStorageUnits().isEmpty()) {
            databaseConfigs.get(databaseName).getDataSources().values().forEach(each -> new DataSourcePoolDestroyer(each).asyncDestroy());
        }
    }
    
    private Map<String, Collection<ShardingSphereSchema>> loadSchemas(final Collection<String> databaseNames) {
        return databaseNames.stream().collect(Collectors.toMap(each -> each, each -> persistService.getDatabaseMetaDataFacade().getSchema().load(each)));
    }
    
    /**
     * Create meta data contexts by switch resource.
     *
     * @param databaseName database name
     * @param internalLoadMetaData internal load meta data
     * @param switchingResource switching resource
     * @param originalMetaDataContexts original meta data contexts
     * @param instanceContext compute node instance context
     * @return meta data contexts
     * @throws SQLException SQL exception
     */
    public MetaDataContexts createBySwitchResource(final String databaseName, final boolean internalLoadMetaData, final SwitchingResource switchingResource,
                                                   final MetaDataContexts originalMetaDataContexts, final ComputeNodeInstanceContext instanceContext) throws SQLException {
        ShardingSphereDatabase changedDatabase = createChangedDatabase(
                databaseName, internalLoadMetaData, switchingResource, null, originalMetaDataContexts, instanceContext);
        ConfigurationProperties props = originalMetaDataContexts.getMetaData().getProps();
        ShardingSphereMetaData clonedMetaData = cloneMetaData(originalMetaDataContexts.getMetaData(), changedDatabase);
        RuleMetaData changedGlobalMetaData = new RuleMetaData(
                GlobalRulesBuilder.buildRules(originalMetaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), clonedMetaData.getAllDatabases(), props));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                clonedMetaData.getAllDatabases(), originalMetaDataContexts.getMetaData().getGlobalResourceMetaData(), changedGlobalMetaData, props);
        return new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, persistService.getShardingSphereDataPersistService().load(metaData)));
    }
    
    /**
     * Create meta data contexts by alter rule.
     *
     * @param databaseName database name
     * @param internalLoadMetaData internal load meta data
     * @param ruleConfigs rule configs
     * @param originalMetaDataContexts original meta data contexts
     * @param instanceContext compute node instance context
     * @return meta data contexts
     * @throws SQLException SQL exception
     */
    public MetaDataContexts createByAlterRule(final String databaseName, final boolean internalLoadMetaData, final Collection<RuleConfiguration> ruleConfigs,
                                              final MetaDataContexts originalMetaDataContexts, final ComputeNodeInstanceContext instanceContext) throws SQLException {
        ShardingSphereDatabase changedDatabase = createChangedDatabase(
                databaseName, internalLoadMetaData, null, ruleConfigs, originalMetaDataContexts, instanceContext);
        ShardingSphereMetaData clonedMetaData = cloneMetaData(originalMetaDataContexts.getMetaData(), changedDatabase);
        ConfigurationProperties props = originalMetaDataContexts.getMetaData().getProps();
        RuleMetaData changedGlobalMetaData = new RuleMetaData(
                GlobalRulesBuilder.buildRules(originalMetaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), clonedMetaData.getAllDatabases(), props));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                clonedMetaData.getAllDatabases(), originalMetaDataContexts.getMetaData().getGlobalResourceMetaData(), changedGlobalMetaData, props);
        return new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, persistService.getShardingSphereDataPersistService().load(metaData)));
    }
    
    private ShardingSphereMetaData cloneMetaData(final ShardingSphereMetaData originalMetaData, final ShardingSphereDatabase changedDatabase) {
        ShardingSphereMetaData result = new ShardingSphereMetaData(
                originalMetaData.getAllDatabases(), originalMetaData.getGlobalResourceMetaData(), originalMetaData.getGlobalRuleMetaData(), originalMetaData.getProps());
        result.putDatabase(changedDatabase);
        return result;
    }
    
    /**
     * Create changed database by switch resource.
     *
     * @param databaseName database name
     * @param internalLoadMetaData internal load meta data
     * @param switchingResource switching resource
     * @param ruleConfigs rule configurations
     * @param originalMetaDataContext original meta data contexts
     * @param instanceContext compute node instance context
     * @return changed database
     * @throws SQLException SQL exception
     */
    public ShardingSphereDatabase createChangedDatabase(final String databaseName, final boolean internalLoadMetaData,
                                                        final SwitchingResource switchingResource, final Collection<RuleConfiguration> ruleConfigs,
                                                        final MetaDataContexts originalMetaDataContext, final ComputeNodeInstanceContext instanceContext) throws SQLException {
        ResourceMetaData effectiveResourceMetaData = getEffectiveResourceMetaData(originalMetaDataContext.getMetaData().getDatabase(databaseName), switchingResource);
        Collection<RuleConfiguration> toBeCreatedRuleConfigs = null == ruleConfigs
                ? originalMetaDataContext.getMetaData().getDatabase(databaseName).getRuleMetaData().getConfigurations()
                : ruleConfigs;
        DatabaseConfiguration toBeCreatedDatabaseConfig = getDatabaseConfiguration(effectiveResourceMetaData, switchingResource, toBeCreatedRuleConfigs);
        return createChangedDatabase(originalMetaDataContext.getMetaData().getDatabase(databaseName).getName(), internalLoadMetaData,
                toBeCreatedDatabaseConfig, originalMetaDataContext.getMetaData().getProps(), instanceContext);
    }
    
    private ShardingSphereDatabase createChangedDatabase(final String databaseName, final boolean internalLoadMetaData, final DatabaseConfiguration databaseConfig,
                                                         final ConfigurationProperties props, final ComputeNodeInstanceContext instanceContext) throws SQLException {
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(databaseConfig, props);
        return internalLoadMetaData
                ? ShardingSphereDatabase.create(databaseName, protocolType, databaseConfig, instanceContext, persistService.getDatabaseMetaDataFacade().getSchema().load(databaseName))
                : ShardingSphereDatabase.create(databaseName, protocolType, databaseConfig, props, instanceContext);
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
    
    private DatabaseConfiguration getDatabaseConfiguration(final ResourceMetaData resourceMetaData,
                                                           final SwitchingResource switchingResource, final Collection<RuleConfiguration> toBeCreatedRuleConfigs) {
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
}
