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
import org.apache.shardingsphere.infra.datasource.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.state.DataSourceState;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.metadata.factory.ExternalMetaDataFactory;
import org.apache.shardingsphere.metadata.factory.InternalMetaDataFactory;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.event.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Meta data contexts.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetaDataContextsFactory {
    
    /**
     * Create meta data contexts.
     *
     * @param persistService persist service
     * @param param context manager builder parameter
     * @param instanceContext instance context
     * @return meta data contexts
     * @throws SQLException SQL exception
     */
    public static MetaDataContexts create(final MetaDataPersistService persistService, final ContextManagerBuilderParameter param, final InstanceContext instanceContext) throws SQLException {
        return create(persistService, param, instanceContext, Collections.emptyMap());
    }
    
    /**
     * Create meta data contexts.
     *
     * @param persistService persist service
     * @param param context manager builder parameter
     * @param instanceContext instance context
     * @param storageNodes storage nodes
     * @return meta data contexts
     * @throws SQLException SQL exception
     */
    public static MetaDataContexts create(final MetaDataPersistService persistService, final ContextManagerBuilderParameter param,
                                          final InstanceContext instanceContext, final Map<String, StorageNodeDataSource> storageNodes) throws SQLException {
        boolean isDatabaseMetaDataExisted = !persistService.getDatabaseMetaDataService().loadAllDatabaseNames().isEmpty();
        Map<String, DatabaseConfiguration> effectiveDatabaseConfigs = isDatabaseMetaDataExisted
                ? createEffectiveDatabaseConfigurations(getDatabaseNames(instanceContext, param.getDatabaseConfigs(), persistService), param.getDatabaseConfigs(), persistService)
                : param.getDatabaseConfigs();
        checkDataSourceStates(effectiveDatabaseConfigs, storageNodes, param.isForce());
        // TODO load global data sources from persist service
        Map<String, DataSource> globalDataSources = param.getGlobalDataSources();
        Collection<RuleConfiguration> globalRuleConfigs = isDatabaseMetaDataExisted ? persistService.getGlobalRuleService().load() : param.getGlobalRuleConfigs();
        ConfigurationProperties props = isDatabaseMetaDataExisted ? new ConfigurationProperties(persistService.getPropsService().load()) : new ConfigurationProperties(param.getProps());
        Map<String, ShardingSphereDatabase> databases = isDatabaseMetaDataExisted
                ? InternalMetaDataFactory.create(persistService, effectiveDatabaseConfigs, props, instanceContext)
                : ExternalMetaDataFactory.create(effectiveDatabaseConfigs, props, instanceContext);
        ShardingSphereResourceMetaData globalResourceMetaData = new ShardingSphereResourceMetaData(globalDataSources);
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(GlobalRulesBuilder.buildRules(globalRuleConfigs, databases, props));
        MetaDataContexts result = new MetaDataContexts(persistService, new ShardingSphereMetaData(databases, globalResourceMetaData, globalRuleMetaData, props));
        if (!isDatabaseMetaDataExisted) {
            persistDatabaseConfigurations(result, param);
            persistMetaData(result);
        }
        return result;
    }
    
    private static Collection<String> getDatabaseNames(final InstanceContext instanceContext, final Map<String, DatabaseConfiguration> databaseConfigs, final MetaDataPersistService persistService) {
        return instanceContext.getInstance().getMetaData() instanceof JDBCInstanceMetaData ? databaseConfigs.keySet() : persistService.getDatabaseMetaDataService().loadAllDatabaseNames();
    }
    
    private static Map<String, DatabaseConfiguration> createEffectiveDatabaseConfigurations(final Collection<String> databaseNames,
                                                                                            final Map<String, DatabaseConfiguration> databaseConfigs, final MetaDataPersistService persistService) {
        return databaseNames.stream().collect(
                Collectors.toMap(each -> each, each -> createEffectiveDatabaseConfiguration(each, databaseConfigs, persistService), (a, b) -> b, () -> new HashMap<>(databaseNames.size(), 1F)));
    }
    
    private static DatabaseConfiguration createEffectiveDatabaseConfiguration(final String databaseName,
                                                                              final Map<String, DatabaseConfiguration> databaseConfigs, final MetaDataPersistService persistService) {
        Map<String, DataSourceConfiguration> effectiveDataSources = persistService.getEffectiveDataSources(databaseName, databaseConfigs);
        Collection<RuleConfiguration> databaseRuleConfigs = persistService.getDatabaseRulePersistService().load(databaseName);
        return new DataSourceGeneratedDatabaseConfiguration(effectiveDataSources, databaseRuleConfigs);
    }
    
    private static void checkDataSourceStates(final Map<String, DatabaseConfiguration> databaseConfigs, final Map<String, StorageNodeDataSource> storageNodes, final boolean force) {
        Map<String, DataSourceState> storageDataSourceStates = getStorageDataSourceStates(storageNodes);
        databaseConfigs.forEach((key, value) -> {
            if (!value.getDataSources().isEmpty()) {
                DataSourceStateManager.getInstance().initStates(key, value.getDataSources(), storageDataSourceStates, force);
            }
        });
    }
    
    private static Map<String, DataSourceState> getStorageDataSourceStates(final Map<String, StorageNodeDataSource> storageDataSourceStates) {
        Map<String, DataSourceState> result = new HashMap<>(storageDataSourceStates.size(), 1F);
        storageDataSourceStates.forEach((key, value) -> {
            List<String> values = Splitter.on(".").splitToList(key);
            Preconditions.checkArgument(3 == values.size(), "Illegal data source of storage node.");
            String databaseName = values.get(0);
            String dataSourceName = values.get(2);
            result.put(databaseName + "." + dataSourceName, DataSourceState.valueOf(value.getStatus().name()));
        });
        return result;
    }
    
    private static void persistDatabaseConfigurations(final MetaDataContexts metadataContexts, final ContextManagerBuilderParameter param) {
        metadataContexts.getPersistService().persistGlobalRuleConfiguration(param.getGlobalRuleConfigs(), param.getProps());
        for (Entry<String, ? extends DatabaseConfiguration> entry : param.getDatabaseConfigs().entrySet()) {
            String databaseName = entry.getKey();
            metadataContexts.getPersistService().persistConfigurations(entry.getKey(), entry.getValue(),
                    metadataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData().getDataSources(),
                    metadataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getRules());
        }
    }
    
    private static void persistMetaData(final MetaDataContexts metaDataContexts) {
        metaDataContexts.getMetaData().getDatabases().values().forEach(each -> each.getSchemas()
                .forEach((schemaName, schema) -> metaDataContexts.getPersistService().getDatabaseMetaDataService().persist(each.getName(), schemaName, schema)));
        metaDataContexts.getStatistics().getDatabaseData().forEach((databaseName, databaseData) -> databaseData.getSchemaData().forEach((schemaName, schemaData) -> metaDataContexts
                .getPersistService().getShardingSphereDataPersistService().persist(databaseName, schemaName, schemaData, metaDataContexts.getMetaData().getDatabases())));
    }
}
