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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceGeneratedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsBuilder;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.factory.ExternalMetaDataFactory;
import org.apache.shardingsphere.mode.metadata.factory.InternalMetaDataFactory;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.metadata.decorator.RuleConfigurationPersistDecorateEngine;
import org.apache.shardingsphere.mode.metadata.manager.SwitchingResource;
import org.apache.shardingsphere.mode.spi.RuleConfigurationPersistDecorator;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Meta data contexts factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetaDataContextsFactory {
    
    /**
     * Create meta data contexts.
     *
     * @param persistService persist service
     * @param param context manager builder parameter
     * @param instanceContext compute node instance context
     * @return meta data contexts
     * @throws SQLException SQL exception
     */
    public static MetaDataContexts create(final MetaDataPersistService persistService, final ContextManagerBuilderParameter param,
                                          final ComputeNodeInstanceContext instanceContext) throws SQLException {
        return isCreateByLocal(persistService) ? createByLocal(persistService, param, instanceContext) : createByRepository(persistService, param, instanceContext);
    }
    
    private static boolean isCreateByLocal(final MetaDataPersistService persistService) {
        return persistService.getDatabaseMetaDataFacade().getDatabase().loadAllDatabaseNames().isEmpty();
    }
    
    private static MetaDataContexts createByLocal(final MetaDataPersistService persistService, final ContextManagerBuilderParameter param,
                                                  final ComputeNodeInstanceContext instanceContext) throws SQLException {
        Map<String, DatabaseConfiguration> effectiveDatabaseConfigs = param.getDatabaseConfigs();
        Collection<RuleConfiguration> globalRuleConfigs;
        if (instanceContext.getModeConfiguration().isCluster()) {
            globalRuleConfigs = new RuleConfigurationPersistDecorateEngine(instanceContext).tryRestore(param.getGlobalRuleConfigs());
            param.getGlobalRuleConfigs().clear();
            param.getGlobalRuleConfigs().addAll(globalRuleConfigs);
        } else {
            globalRuleConfigs = param.getGlobalRuleConfigs();
        }
        ConfigurationProperties props = new ConfigurationProperties(param.getProps());
        Map<String, ShardingSphereDatabase> databases = ExternalMetaDataFactory.create(effectiveDatabaseConfigs, props, instanceContext);
        MetaDataContexts result = newMetaDataContexts(persistService, param, globalRuleConfigs, databases, props);
        persistDatabaseConfigurations(result, param, persistService, instanceContext);
        persistMetaData(result, persistService);
        return result;
    }
    
    private static MetaDataContexts createByRepository(final MetaDataPersistService persistService, final ContextManagerBuilderParameter param, final ComputeNodeInstanceContext instanceContext) {
        Map<String, DatabaseConfiguration> effectiveDatabaseConfigs = createEffectiveDatabaseConfigurations(
                getDatabaseNames(instanceContext, param.getDatabaseConfigs(), persistService), param.getDatabaseConfigs(), persistService);
        Collection<RuleConfiguration> globalRuleConfigs = persistService.getGlobalRuleService().load();
        ConfigurationProperties props = new ConfigurationProperties(persistService.getPropsService().load());
        Map<String, ShardingSphereDatabase> databases = InternalMetaDataFactory.create(persistService, effectiveDatabaseConfigs, props, instanceContext);
        MetaDataContexts result = newMetaDataContexts(persistService, param, globalRuleConfigs, databases, props);
        restoreRules(result, instanceContext);
        return result;
    }
    
    private static MetaDataContexts newMetaDataContexts(final MetaDataPersistService persistService, final ContextManagerBuilderParameter param,
                                                        final Collection<RuleConfiguration> globalRuleConfigs, final Map<String, ShardingSphereDatabase> databases,
                                                        final ConfigurationProperties props) {
        // TODO load global data sources from persist service
        ResourceMetaData globalResourceMetaData = new ResourceMetaData(param.getGlobalDataSources());
        RuleMetaData globalRuleMetaData = new RuleMetaData(GlobalRulesBuilder.buildRules(globalRuleConfigs, databases.values(), props));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases.values(), globalResourceMetaData, globalRuleMetaData, props);
        return new MetaDataContexts(metaData, createStatistics(persistService, metaData));
    }
    
    private static Collection<String> getDatabaseNames(final ComputeNodeInstanceContext instanceContext,
                                                       final Map<String, DatabaseConfiguration> databaseConfigs, final MetaDataPersistService persistService) {
        return instanceContext.getInstance().getMetaData() instanceof JDBCInstanceMetaData
                ? databaseConfigs.keySet()
                : persistService.getDatabaseMetaDataFacade().getDatabase().loadAllDatabaseNames();
    }
    
    private static Map<String, DatabaseConfiguration> createEffectiveDatabaseConfigurations(final Collection<String> databaseNames,
                                                                                            final Map<String, DatabaseConfiguration> databaseConfigs, final MetaDataPersistService persistService) {
        return databaseNames.stream().collect(Collectors.toMap(each -> each, each -> createEffectiveDatabaseConfiguration(each, databaseConfigs, persistService)));
    }
    
    private static DatabaseConfiguration createEffectiveDatabaseConfiguration(final String databaseName,
                                                                              final Map<String, DatabaseConfiguration> databaseConfigs, final MetaDataPersistService persistService) {
        closeGeneratedDataSources(databaseName, databaseConfigs);
        Map<String, DataSourceConfiguration> dataSources = persistService.loadDataSourceConfigurations(databaseName);
        Collection<RuleConfiguration> databaseRuleConfigs = persistService.getDatabaseRulePersistService().load(databaseName);
        return new DataSourceGeneratedDatabaseConfiguration(dataSources, databaseRuleConfigs);
    }
    
    private static void closeGeneratedDataSources(final String databaseName, final Map<String, ? extends DatabaseConfiguration> databaseConfigs) {
        if (databaseConfigs.containsKey(databaseName) && !databaseConfigs.get(databaseName).getStorageUnits().isEmpty()) {
            databaseConfigs.get(databaseName).getDataSources().values().forEach(each -> new DataSourcePoolDestroyer(each).asyncDestroy());
        }
    }
    
    /**
     * Create statistics.
     *
     * @param persistService meta data persist service
     * @param metaData meta data
     * @return created statistics
     */
    public static ShardingSphereStatistics createStatistics(final MetaDataPersistService persistService, final ShardingSphereMetaData metaData) {
        if (metaData.getAllDatabases().isEmpty()) {
            return new ShardingSphereStatistics();
        }
        Optional<ShardingSphereStatisticsBuilder> statisticsBuilder = DatabaseTypedSPILoader.findService(ShardingSphereStatisticsBuilder.class, getDatabaseType(metaData));
        if (!statisticsBuilder.isPresent()) {
            return new ShardingSphereStatistics();
        }
        ShardingSphereStatistics result = statisticsBuilder.get().build(metaData);
        persistService.getShardingSphereDataPersistService().load(metaData).ifPresent(optional -> useLoadedToReplaceInit(result, optional));
        return result;
    }
    
    private static DatabaseType getDatabaseType(final ShardingSphereMetaData metaData) {
        DatabaseType protocolType = metaData.getAllDatabases().iterator().next().getProtocolType();
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(protocolType).getDialectDatabaseMetaData();
        // TODO can `protocolType instanceof SchemaSupportedDatabaseType ? "PostgreSQL" : protocolType.getType()` replace to trunk database type?
        return dialectDatabaseMetaData.getDefaultSchema().isPresent() ? TypedSPILoader.getService(DatabaseType.class, "PostgreSQL") : protocolType;
    }
    
    private static void useLoadedToReplaceInit(final ShardingSphereStatistics initStatistics, final ShardingSphereStatistics loadedStatistics) {
        for (Entry<String, ShardingSphereDatabaseData> entry : initStatistics.getDatabaseData().entrySet()) {
            if (loadedStatistics.getDatabaseData().containsKey(entry.getKey())) {
                useLoadedToReplaceInitByDatabaseData(entry.getValue(), loadedStatistics.getDatabaseData().get(entry.getKey()));
            }
        }
    }
    
    private static void useLoadedToReplaceInitByDatabaseData(final ShardingSphereDatabaseData initDatabaseData, final ShardingSphereDatabaseData loadedDatabaseData) {
        for (Entry<String, ShardingSphereSchemaData> entry : initDatabaseData.getSchemaData().entrySet()) {
            if (loadedDatabaseData.getSchemaData().containsKey(entry.getKey())) {
                useLoadedToReplaceInitBySchemaData(entry.getValue(), loadedDatabaseData.getSchemaData().get(entry.getKey()));
            }
        }
    }
    
    private static void useLoadedToReplaceInitBySchemaData(final ShardingSphereSchemaData initSchemaData, final ShardingSphereSchemaData loadedSchemaData) {
        for (Entry<String, ShardingSphereTableData> entry : initSchemaData.getTableData().entrySet()) {
            if (loadedSchemaData.getTableData().containsKey(entry.getKey())) {
                entry.setValue(loadedSchemaData.getTableData().get(entry.getKey()));
            }
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void restoreRules(final MetaDataContexts metaDataContexts, final ComputeNodeInstanceContext instanceContext) {
        if (!instanceContext.getModeConfiguration().isCluster()) {
            return;
        }
        for (RuleConfigurationPersistDecorator each : ShardingSphereServiceLoader.getServiceInstances(RuleConfigurationPersistDecorator.class)) {
            ShardingSphereRule rule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(each.getRuleType());
            metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules().removeIf(eachRule -> each.getRuleType() == eachRule.getClass());
            RuleConfiguration restoredRuleConfig = each.restore(rule.getConfiguration());
            ShardingSphereRule rebuiltRule = GlobalRulesBuilder.buildRules(
                    Collections.singleton(restoredRuleConfig), metaDataContexts.getMetaData().getAllDatabases(), metaDataContexts.getMetaData().getProps()).iterator().next();
            metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules().add(rebuiltRule);
        }
    }
    
    private static void persistDatabaseConfigurations(final MetaDataContexts metadataContexts, final ContextManagerBuilderParameter param, final MetaDataPersistService persistService,
                                                      final ComputeNodeInstanceContext instanceContext) {
        RuleConfigurationPersistDecorateEngine ruleConfigPersistDecorateEngine = new RuleConfigurationPersistDecorateEngine(instanceContext);
        persistService.persistGlobalRuleConfiguration(ruleConfigPersistDecorateEngine.decorate(metadataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations()), param.getProps());
        for (Entry<String, ? extends DatabaseConfiguration> entry : param.getDatabaseConfigs().entrySet()) {
            String databaseName = entry.getKey();
            persistService.persistConfigurations(entry.getKey(), entry.getValue(),
                    metadataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData().getStorageUnits().entrySet().stream()
                            .collect(Collectors.toMap(Entry::getKey, each -> each.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)),
                    metadataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getRules());
        }
    }
    
    private static void persistMetaData(final MetaDataContexts metaDataContexts, final MetaDataPersistService persistService) {
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
    
    /**
     * Create meta data contexts by switch resource.
     *
     * @param databaseName database name
     * @param internalLoadMetaData internal load meta data
     * @param switchingResource switching resource
     * @param originalMetaDataContexts original meta data contexts
     * @param persistService meta data persist service
     * @param instanceContext compute node instance context
     * @return meta data contexts
     * @throws SQLException SQL exception
     */
    public static MetaDataContexts createBySwitchResource(final String databaseName, final boolean internalLoadMetaData, final SwitchingResource switchingResource,
                                                          final MetaDataContexts originalMetaDataContexts, final MetaDataPersistService persistService,
                                                          final ComputeNodeInstanceContext instanceContext) throws SQLException {
        ShardingSphereDatabase changedDatabase = createChangedDatabase(
                databaseName, internalLoadMetaData, switchingResource, null, originalMetaDataContexts, persistService, instanceContext);
        ConfigurationProperties props = originalMetaDataContexts.getMetaData().getProps();
        ShardingSphereMetaData clonedMetaData = cloneMetaData(originalMetaDataContexts.getMetaData(), changedDatabase);
        RuleMetaData changedGlobalMetaData = new RuleMetaData(
                GlobalRulesBuilder.buildRules(originalMetaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), clonedMetaData.getAllDatabases(), props));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                clonedMetaData.getAllDatabases(), originalMetaDataContexts.getMetaData().getGlobalResourceMetaData(), changedGlobalMetaData, props);
        return new MetaDataContexts(metaData, createStatistics(persistService, metaData));
    }
    
    /**
     * Create meta data contexts by alter rule.
     *
     * @param databaseName database name
     * @param internalLoadMetaData internal load meta data
     * @param ruleConfigs rule configs
     * @param originalMetaDataContexts original meta data contexts
     * @param persistService meta data persist service
     * @param instanceContext compute node instance context
     * @return meta data contexts
     * @throws SQLException SQL exception
     */
    public static MetaDataContexts createByAlterRule(final String databaseName, final boolean internalLoadMetaData, final Collection<RuleConfiguration> ruleConfigs,
                                                     final MetaDataContexts originalMetaDataContexts, final MetaDataPersistService persistService,
                                                     final ComputeNodeInstanceContext instanceContext) throws SQLException {
        ShardingSphereDatabase changedDatabase = createChangedDatabase(
                databaseName, internalLoadMetaData, null, ruleConfigs, originalMetaDataContexts, persistService, instanceContext);
        ShardingSphereMetaData clonedMetaData = cloneMetaData(originalMetaDataContexts.getMetaData(), changedDatabase);
        ConfigurationProperties props = originalMetaDataContexts.getMetaData().getProps();
        RuleMetaData changedGlobalMetaData = new RuleMetaData(
                GlobalRulesBuilder.buildRules(originalMetaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), clonedMetaData.getAllDatabases(), props));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                clonedMetaData.getAllDatabases(), originalMetaDataContexts.getMetaData().getGlobalResourceMetaData(), changedGlobalMetaData, props);
        return new MetaDataContexts(metaData, createStatistics(persistService, metaData));
    }
    
    private static ShardingSphereMetaData cloneMetaData(final ShardingSphereMetaData originalMetaData, final ShardingSphereDatabase changedDatabase) {
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
     * @param persistService meta data persist service
     * @param instanceContext compute node instance context
     * @return changed database
     * @throws SQLException SQL exception
     */
    public static ShardingSphereDatabase createChangedDatabase(final String databaseName, final boolean internalLoadMetaData,
                                                               final SwitchingResource switchingResource, final Collection<RuleConfiguration> ruleConfigs,
                                                               final MetaDataContexts originalMetaDataContext, final MetaDataPersistService persistService,
                                                               final ComputeNodeInstanceContext instanceContext) throws SQLException {
        ResourceMetaData effectiveResourceMetaData = getEffectiveResourceMetaData(originalMetaDataContext.getMetaData().getDatabase(databaseName), switchingResource);
        Collection<RuleConfiguration> toBeCreatedRuleConfigs = null == ruleConfigs
                ? originalMetaDataContext.getMetaData().getDatabase(databaseName).getRuleMetaData().getConfigurations()
                : ruleConfigs;
        DatabaseConfiguration toBeCreatedDatabaseConfig = getDatabaseConfiguration(effectiveResourceMetaData, switchingResource, toBeCreatedRuleConfigs);
        return createChangedDatabase(originalMetaDataContext.getMetaData().getDatabase(databaseName).getName(), internalLoadMetaData,
                persistService, toBeCreatedDatabaseConfig, originalMetaDataContext.getMetaData().getProps(), instanceContext);
    }
    
    private static ShardingSphereDatabase createChangedDatabase(final String databaseName, final boolean internalLoadMetaData, final MetaDataPersistService persistService,
                                                                final DatabaseConfiguration databaseConfig, final ConfigurationProperties props,
                                                                final ComputeNodeInstanceContext instanceContext) throws SQLException {
        return internalLoadMetaData
                ? InternalMetaDataFactory.create(databaseName, persistService, databaseConfig, props, instanceContext)
                : ExternalMetaDataFactory.create(databaseName, databaseConfig, props, instanceContext);
    }
    
    private static ResourceMetaData getEffectiveResourceMetaData(final ShardingSphereDatabase database, final SwitchingResource resource) {
        Map<StorageNode, DataSource> storageNodes = getStorageNodes(database.getResourceMetaData().getDataSources(), resource);
        Map<String, StorageUnit> storageUnits = getStorageUnits(database.getResourceMetaData().getStorageUnits(), resource);
        return new ResourceMetaData(storageNodes, storageUnits);
    }
    
    private static Map<StorageNode, DataSource> getStorageNodes(final Map<StorageNode, DataSource> currentStorageNodes, final SwitchingResource resource) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(currentStorageNodes.size(), 1F);
        for (Entry<StorageNode, DataSource> entry : currentStorageNodes.entrySet()) {
            if (null == resource || !resource.getStaleDataSources().containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private static Map<String, StorageUnit> getStorageUnits(final Map<String, StorageUnit> currentStorageUnits, final SwitchingResource resource) {
        Map<String, StorageUnit> result = new LinkedHashMap<>(currentStorageUnits.size(), 1F);
        for (Entry<String, StorageUnit> entry : currentStorageUnits.entrySet()) {
            if (null == resource || !resource.getStaleStorageUnitNames().contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private static DatabaseConfiguration getDatabaseConfiguration(final ResourceMetaData resourceMetaData, final SwitchingResource switchingResource,
                                                                  final Collection<RuleConfiguration> toBeCreatedRuleConfigs) {
        Map<String, DataSourcePoolProperties> propsMap = null == switchingResource ? resourceMetaData.getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSourcePoolProperties(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new))
                : switchingResource.getMergedDataSourcePoolPropertiesMap();
        return new DataSourceProvidedDatabaseConfiguration(getMergedStorageNodeDataSources(resourceMetaData, switchingResource), toBeCreatedRuleConfigs, propsMap);
    }
    
    private static Map<StorageNode, DataSource> getMergedStorageNodeDataSources(final ResourceMetaData currentResourceMetaData, final SwitchingResource switchingResource) {
        Map<StorageNode, DataSource> result = currentResourceMetaData.getDataSources();
        if (null != switchingResource && !switchingResource.getNewDataSources().isEmpty()) {
            result.putAll(switchingResource.getNewDataSources());
        }
        return result;
    }
}
