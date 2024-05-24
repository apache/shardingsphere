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

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceGeneratedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsBuilder;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.infra.state.datasource.DataSourceStateManager;
import org.apache.shardingsphere.metadata.factory.ExternalMetaDataFactory;
import org.apache.shardingsphere.metadata.factory.InternalMetaDataFactory;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.storage.QualifiedDataSourceStatus;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
     * @param computeNodeInstanceContext compute node instance context
     * @return meta data contexts
     * @throws SQLException SQL exception
     */
    public static MetaDataContexts create(final MetaDataPersistService persistService, final ContextManagerBuilderParameter param,
                                          final ComputeNodeInstanceContext computeNodeInstanceContext) throws SQLException {
        return create(persistService, param, computeNodeInstanceContext, Collections.emptyMap());
    }
    
    /**
     * Create meta data contexts.
     * 
     * @param persistService persist service
     * @param param context manager builder parameter
     * @param computeNodeInstanceContext compute node instance context
     * @param statusMap qualified data source status map
     * @return meta data contexts
     * @throws SQLException SQL exception
     */
    public static MetaDataContexts create(final MetaDataPersistService persistService, final ContextManagerBuilderParameter param,
                                          final ComputeNodeInstanceContext computeNodeInstanceContext, final Map<String, QualifiedDataSourceStatus> statusMap) throws SQLException {
        boolean isDatabaseMetaDataExisted = !persistService.getDatabaseMetaDataService().loadAllDatabaseNames().isEmpty();
        Map<String, DatabaseConfiguration> effectiveDatabaseConfigs = isDatabaseMetaDataExisted
                ? createEffectiveDatabaseConfigurations(getDatabaseNames(computeNodeInstanceContext, param.getDatabaseConfigs(), persistService), param.getDatabaseConfigs(), persistService)
                : param.getDatabaseConfigs();
        checkDataSourceStates(effectiveDatabaseConfigs, statusMap, param.isForce());
        // TODO load global data sources from persist service
        Map<String, DataSource> globalDataSources = param.getGlobalDataSources();
        Collection<RuleConfiguration> globalRuleConfigs = isDatabaseMetaDataExisted ? persistService.getGlobalRuleService().load() : param.getGlobalRuleConfigs();
        ConfigurationProperties props = isDatabaseMetaDataExisted ? new ConfigurationProperties(persistService.getPropsService().load()) : new ConfigurationProperties(param.getProps());
        Map<String, ShardingSphereDatabase> databases = isDatabaseMetaDataExisted
                ? InternalMetaDataFactory.create(persistService, effectiveDatabaseConfigs, props, computeNodeInstanceContext)
                : ExternalMetaDataFactory.create(effectiveDatabaseConfigs, props, computeNodeInstanceContext);
        ResourceMetaData globalResourceMetaData = new ResourceMetaData(globalDataSources);
        RuleMetaData globalRuleMetaData = new RuleMetaData(GlobalRulesBuilder.buildRules(globalRuleConfigs, databases, props));
        ShardingSphereMetaData shardingSphereMetaData = new ShardingSphereMetaData(databases, globalResourceMetaData, globalRuleMetaData, props);
        ShardingSphereStatistics shardingSphereStatistics = initStatistics(persistService, shardingSphereMetaData);
        MetaDataContexts result = new MetaDataContexts(shardingSphereMetaData, shardingSphereStatistics);
        if (!isDatabaseMetaDataExisted) {
            persistDatabaseConfigurations(result, param, persistService);
            persistMetaData(result, persistService);
        }
        return result;
    }
    
    /**
     * Create meta data contexts.
     *
     * @param persistService meta data persist service
     * @param metaData shardingsphere meta data
     * @return meta data contexts
     */
    public static MetaDataContexts create(final MetaDataPersistService persistService, final ShardingSphereMetaData metaData) {
        return new MetaDataContexts(metaData, initStatistics(persistService, metaData));
    }
    
    private static Collection<String> getDatabaseNames(final ComputeNodeInstanceContext computeNodeInstanceContext,
                                                       final Map<String, DatabaseConfiguration> databaseConfigs, final MetaDataPersistService persistService) {
        return computeNodeInstanceContext.getInstance().getMetaData() instanceof JDBCInstanceMetaData ? databaseConfigs.keySet() : persistService.getDatabaseMetaDataService().loadAllDatabaseNames();
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
    
    private static void checkDataSourceStates(final Map<String, DatabaseConfiguration> databaseConfigs, final Map<String, QualifiedDataSourceStatus> statusMap, final boolean force) {
        Map<String, DataSourceState> storageDataSourceStates = getStorageDataSourceStates(statusMap);
        databaseConfigs.forEach((key, value) -> {
            if (!value.getStorageUnits().isEmpty()) {
                DataSourceStateManager.getInstance().initStates(key, value.getStorageUnits(), storageDataSourceStates, force);
            }
        });
    }
    
    private static Map<String, DataSourceState> getStorageDataSourceStates(final Map<String, QualifiedDataSourceStatus> statusMap) {
        Map<String, DataSourceState> result = new HashMap<>(statusMap.size(), 1F);
        statusMap.forEach((key, value) -> {
            List<String> values = Splitter.on(".").splitToList(key);
            Preconditions.checkArgument(3 == values.size(), "Illegal data source of storage node.");
            String databaseName = values.get(0);
            String dataSourceName = values.get(2);
            result.put(databaseName + "." + dataSourceName, DataSourceState.valueOf(value.getStatus().name()));
        });
        return result;
    }
    
    private static ShardingSphereStatistics initStatistics(final MetaDataPersistService persistService, final ShardingSphereMetaData metaData) {
        if (metaData.getDatabases().isEmpty()) {
            return new ShardingSphereStatistics();
        }
        DatabaseType protocolType = metaData.getDatabases().values().iterator().next().getProtocolType();
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(protocolType).getDialectDatabaseMetaData();
        // TODO can `protocolType instanceof SchemaSupportedDatabaseType ? "PostgreSQL" : protocolType.getType()` replace to trunk database type?
        DatabaseType databaseType = dialectDatabaseMetaData.getDefaultSchema().isPresent() ? TypedSPILoader.getService(DatabaseType.class, "PostgreSQL") : protocolType;
        Optional<ShardingSphereStatisticsBuilder> statisticsBuilder = DatabaseTypedSPILoader.findService(ShardingSphereStatisticsBuilder.class, databaseType);
        if (!statisticsBuilder.isPresent()) {
            return new ShardingSphereStatistics();
        }
        ShardingSphereStatistics result = statisticsBuilder.get().build(metaData);
        Optional<ShardingSphereStatistics> loadedStatistics = persistService.getShardingSphereDataPersistService().load(metaData);
        loadedStatistics.ifPresent(optional -> useLoadedToReplaceInit(result, optional));
        return result;
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
    
    private static void persistDatabaseConfigurations(final MetaDataContexts metadataContexts, final ContextManagerBuilderParameter param, final MetaDataPersistService persistService) {
        persistService.persistGlobalRuleConfiguration(param.getGlobalRuleConfigs(), param.getProps());
        for (Entry<String, ? extends DatabaseConfiguration> entry : param.getDatabaseConfigs().entrySet()) {
            String databaseName = entry.getKey();
            persistService.persistConfigurations(entry.getKey(), entry.getValue(),
                    metadataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData().getStorageUnits().entrySet().stream()
                            .collect(Collectors.toMap(Entry::getKey, each -> each.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)),
                    metadataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getRules());
        }
    }
    
    private static void persistMetaData(final MetaDataContexts metaDataContexts, final MetaDataPersistService persistService) {
        metaDataContexts.getMetaData().getDatabases().values().forEach(each -> each.getSchemas().forEach((schemaName, schema) -> persistService.getDatabaseMetaDataService()
                .persistByAlterConfiguration(each.getName(), schemaName, schema)));
        metaDataContexts.getStatistics().getDatabaseData().forEach((databaseName, databaseData) -> databaseData.getSchemaData().forEach((schemaName, schemaData) -> persistService
                .getShardingSphereDataPersistService().persist(databaseName, schemaName, schemaData, metaDataContexts.getMetaData().getDatabases())));
    }
}
