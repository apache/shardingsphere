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
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.state.DataSourceState;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.data.builder.ShardingSphereDataBuilderFactory;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
     * @param parameter database configurations
     * @param instanceContext instance context
     * @return meta data contexts
     * @throws SQLException SQL exception
     */
    public static MetaDataContexts create(final MetaDataPersistService persistService, final ContextManagerBuilderParameter parameter,
                                          final InstanceContext instanceContext) throws SQLException {
        return create(persistService, parameter, instanceContext, Collections.emptyMap());
    }
    
    /**
     * Create meta data contexts.
     *
     * @param persistService persist service
     * @param parameter database configurations
     * @param instanceContext instance context
     * @param storageNodes storage nodes
     * @return meta data contexts
     * @throws SQLException SQL exception
     */
    public static MetaDataContexts create(final MetaDataPersistService persistService, final ContextManagerBuilderParameter parameter,
                                          final InstanceContext instanceContext, final Map<String, StorageNodeDataSource> storageNodes) throws SQLException {
        Collection<String> databaseNames = instanceContext.getInstance().getMetaData() instanceof JDBCInstanceMetaData
                ? parameter.getDatabaseConfigs().keySet()
                : persistService.getDatabaseMetaDataService().loadAllDatabaseNames();
        Map<String, DatabaseConfiguration> effectiveDatabaseConfigs = createEffectiveDatabaseConfigurations(databaseNames, parameter.getDatabaseConfigs(), persistService);
        checkDataSourceStates(effectiveDatabaseConfigs, storageNodes, parameter.isForce());
        Collection<RuleConfiguration> globalRuleConfigs = persistService.getGlobalRuleService().load();
        ConfigurationProperties props = new ConfigurationProperties(persistService.getPropsService().load());
        Map<String, ShardingSphereDatabase> databases = ShardingSphereDatabasesFactory.create(effectiveDatabaseConfigs, props, instanceContext);
        databases.putAll(reloadDatabases(databases, persistService));
        ShardingSphereRuleMetaData globalMetaData = new ShardingSphereRuleMetaData(GlobalRulesBuilder.buildRules(globalRuleConfigs, databases, instanceContext, props));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, globalMetaData, props);
        ShardingSphereData shardingSphereData = initShardingSphereData(persistService, metaData);
        return new MetaDataContexts(persistService, metaData, shardingSphereData);
    }
    
    private static Map<String, DatabaseConfiguration> createEffectiveDatabaseConfigurations(final Collection<String> databaseNames,
                                                                                            final Map<String, DatabaseConfiguration> databaseConfigs, final MetaDataPersistService persistService) {
        return databaseNames.stream().collect(
                Collectors.toMap(each -> each, each -> createEffectiveDatabaseConfiguration(each, databaseConfigs, persistService), (a, b) -> b, () -> new HashMap<>(databaseNames.size(), 1)));
    }
    
    private static DatabaseConfiguration createEffectiveDatabaseConfiguration(final String databaseName,
                                                                              final Map<String, DatabaseConfiguration> databaseConfigs, final MetaDataPersistService persistService) {
        Map<String, DataSource> effectiveDataSources = persistService.getEffectiveDataSources(databaseName, databaseConfigs);
        Collection<RuleConfiguration> databaseRuleConfigs = persistService.getDatabaseRulePersistService().load(databaseName);
        return new DataSourceProvidedDatabaseConfiguration(effectiveDataSources, databaseRuleConfigs);
    }
    
    private static void checkDataSourceStates(final Map<String, DatabaseConfiguration> databaseConfigs, final Map<String, StorageNodeDataSource> storageNodes, final boolean force) {
        Map<String, DataSourceState> storageDataSourceStates = getStorageDataSourceStates(storageNodes);
        databaseConfigs.forEach((key, value) -> {
            if (null != value.getDataSources()) {
                DataSourceStateManager.getInstance().initStates(key, value.getDataSources(), storageDataSourceStates, force);
            }
        });
    }
    
    private static Map<String, DataSourceState> getStorageDataSourceStates(final Map<String, StorageNodeDataSource> storageDataSourceStates) {
        Map<String, DataSourceState> result = new HashMap<>(storageDataSourceStates.size(), 1);
        storageDataSourceStates.forEach((key, value) -> {
            List<String> values = Splitter.on(".").splitToList(key);
            Preconditions.checkArgument(3 == values.size(), "Illegal data source of storage node.");
            String databaseName = values.get(0);
            String dataSourceName = values.get(2);
            result.put(databaseName + "." + dataSourceName, DataSourceState.valueOf(value.getStatus().toUpperCase()));
        });
        return result;
    }
    
    private static Map<String, ShardingSphereDatabase> reloadDatabases(final Map<String, ShardingSphereDatabase> databases, final MetaDataPersistService persistService) {
        Map<String, ShardingSphereDatabase> result = new ConcurrentHashMap<>(databases.size(), 1);
        databases.forEach((key, value) -> {
            Map<String, ShardingSphereSchema> schemas = persistService.getDatabaseMetaDataService().loadSchemas(key);
            result.put(key.toLowerCase(), new ShardingSphereDatabase(value.getName(),
                    value.getProtocolType(), value.getResourceMetaData(), value.getRuleMetaData(), schemas.isEmpty() ? value.getSchemas() : schemas));
        });
        return result;
    }
    
    private static ShardingSphereData initShardingSphereData(final MetaDataPersistService persistService, final ShardingSphereMetaData metaData) {
        ShardingSphereData result = persistService.getShardingSphereDataPersistService().load().orElseGet(() -> ShardingSphereDataBuilderFactory
                .getInstance(metaData.getDatabases().values().iterator().next().getProtocolType()).map(builder -> builder.build(metaData)).orElseGet(ShardingSphereData::new));
        result.registerQueryEngine(metaData);
        return result;
    }
}
