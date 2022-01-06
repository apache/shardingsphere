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

package org.apache.shardingsphere.mode.manager.cluster;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.pool.creator.DataSourcePoolCreatorUtil;
import org.apache.shardingsphere.infra.instance.Instance;
import org.apache.shardingsphere.infra.metadata.schema.QualifiedSchema;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.loader.SchemaLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.infra.rule.identifier.type.StatusContainedRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.ClusterContextManagerCoordinator;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.schedule.core.api.ModeScheduleContextFactory;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.context.TransactionContextsBuilder;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Cluster context manager builder.
 */
@Slf4j
public final class ClusterContextManagerBuilder implements ContextManagerBuilder {
    
    static {
        ShardingSphereServiceLoader.register(ClusterPersistRepository.class);
    }
    
    private RegistryCenter registryCenter;
    
    private MetaDataPersistService metaDataPersistService;
    
    private MetaDataContexts metaDataContexts;
    
    private TransactionContexts transactionContexts;
    
    private ContextManager contextManager;
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter parameter) throws SQLException {
        beforeBuildContextManager(parameter);
        contextManager = new ContextManager();
        contextManager.init(metaDataContexts, transactionContexts, null);
        afterBuildContextManager(parameter);
        return contextManager;
    }
    
    private void beforeBuildContextManager(final ContextManagerBuilderParameter parameter) throws SQLException {
        ClusterPersistRepository repository = createClusterPersistRepository((ClusterPersistRepositoryConfiguration) parameter.getModeConfig().getRepository());
        registryCenter = new RegistryCenter(repository, parameter.getPort());
        ModeScheduleContextFactory.getInstance().init(parameter.getModeConfig());
        metaDataPersistService = new MetaDataPersistService(repository);
        persistConfigurations(metaDataPersistService, parameter.getDataSourcesMap(), parameter.getSchemaRuleConfigs(), parameter.getGlobalRuleConfigs(), parameter.getProps(), parameter.isOverwrite());
        persistInstanceConfigurations(parameter.getLabels());
        Collection<String> schemaNames = Strings.isNullOrEmpty(parameter.getSchemaName()) ? metaDataPersistService.getSchemaMetaDataService()
                .loadAllNames() : Collections.singletonList(parameter.getSchemaName());
        Map<String, Map<String, DataSource>> clusterDataSources = loadDataSourcesMap(metaDataPersistService, parameter.getDataSourcesMap(), schemaNames);
        Map<String, Collection<RuleConfiguration>> clusterSchemaRuleConfigs = loadSchemaRules(metaDataPersistService, schemaNames);
        Properties clusterProps = metaDataPersistService.getPropsService().load();
        Map<String, Collection<ShardingSphereRule>> rules = SchemaRulesBuilder.buildRules(clusterDataSources, clusterSchemaRuleConfigs, clusterProps);
        Map<String, ShardingSphereSchema> schemas = new SchemaLoader(clusterDataSources, clusterSchemaRuleConfigs, rules, clusterProps).load();
        persistMetaData(schemas);
        metaDataContexts = new MetaDataContextsBuilder(clusterDataSources, clusterSchemaRuleConfigs, metaDataPersistService.getGlobalRuleService().load(), schemas, rules, clusterProps)
                .build(metaDataPersistService);
        transactionContexts = new TransactionContextsBuilder(metaDataContexts.getMetaDataMap(), metaDataContexts.getGlobalRuleMetaData().getRules()).build();
    }
    
    private void afterBuildContextManager(final ContextManagerBuilderParameter parameter) {
        new ClusterContextManagerCoordinator(metaDataPersistService, contextManager);
        disableDataSources();
        registryCenter.onlineInstance(parameter.getInstanceType());
    }
    
    private ClusterPersistRepository createClusterPersistRepository(final ClusterPersistRepositoryConfiguration config) {
        Preconditions.checkNotNull(config, "Cluster persist repository configuration cannot be null.");
        ClusterPersistRepository result = TypedSPIRegistry.getRegisteredService(ClusterPersistRepository.class, config.getType(), config.getProps());
        result.init(config);
        return result;
    }
    
    private void persistConfigurations(final MetaDataPersistService metaDataPersistService, final Map<String, Map<String, DataSource>> dataSourcesMap,
                                       final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs, final Collection<RuleConfiguration> globalRuleConfigs,
                                       final Properties props, final boolean overwrite) {
        if (!isEmptyLocalConfiguration(dataSourcesMap, schemaRuleConfigs, globalRuleConfigs, props)) {
            metaDataPersistService.persistConfigurations(getDataSourceConfigurations(dataSourcesMap), schemaRuleConfigs, globalRuleConfigs, props, overwrite);
        }
    }
    
    private void persistInstanceConfigurations(final Collection<String> labels) {
        if (null != labels && !labels.isEmpty()) {
            metaDataPersistService.persistInstanceConfigurations(Instance.getInstance().getId(), labels);
        }
    }
    
    private boolean isEmptyLocalConfiguration(final Map<String, Map<String, DataSource>> dataSourcesMap,
                                              final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs, final Collection<RuleConfiguration> globalRuleConfigs, final Properties props) {
        return isEmptyLocalDataSourcesMap(dataSourcesMap) && isEmptyLocalSchemaRuleConfigurations(schemaRuleConfigs) && globalRuleConfigs.isEmpty() && props.isEmpty();
    }
    
    private boolean isEmptyLocalDataSourcesMap(final Map<String, Map<String, DataSource>> dataSourcesMap) {
        return dataSourcesMap.entrySet().stream().allMatch(entry -> entry.getValue().isEmpty());
    }
    
    private boolean isEmptyLocalSchemaRuleConfigurations(final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs) {
        return schemaRuleConfigs.entrySet().stream().allMatch(entry -> entry.getValue().isEmpty());
    }
    
    private Map<String, Map<String, DataSourceConfiguration>> getDataSourceConfigurations(final Map<String, Map<String, DataSource>> dataSourcesMap) {
        Map<String, Map<String, DataSourceConfiguration>> result = new LinkedHashMap<>(dataSourcesMap.size(), 1);
        for (Entry<String, Map<String, DataSource>> entry : dataSourcesMap.entrySet()) {
            result.put(entry.getKey(), DataSourcePoolCreatorUtil.getDataSourceConfigurationMap(entry.getValue()));
        }
        return result;
    }
    
    private Map<String, Map<String, DataSource>> loadDataSourcesMap(final MetaDataPersistService metaDataPersistService, final Map<String, Map<String, DataSource>> dataSourcesMap,
                                                                    final Collection<String> schemaNames) {
        Map<String, Map<String, DataSourceConfiguration>> loadDataSourceConfigs = loadDataSourceConfigurations(metaDataPersistService, schemaNames);
        Map<String, Map<String, DataSource>> result = getLoadDataSources(loadDataSourceConfigs, dataSourcesMap);
        closeLocalDataSources(dataSourcesMap);
        return result;
    }
    
    private Map<String, Map<String, DataSourceConfiguration>> loadDataSourceConfigurations(final MetaDataPersistService metaDataPersistService, final Collection<String> schemaNames) {
        Map<String, Map<String, DataSourceConfiguration>> result = new LinkedHashMap<>();
        for (String each : schemaNames) {
            result.put(each, metaDataPersistService.getDataSourceService().load(each));
        }
        return result;
    }
    
    private void closeLocalDataSources(final Map<String, Map<String, DataSource>> dataSourceMap) {
        for (Entry<String, Map<String, DataSource>> entry : dataSourceMap.entrySet()) {
            entry.getValue().values().forEach(this::closeDataSource);
        }
    }
    
    private void closeDataSource(final DataSource dataSource) {
        try {
            dataSource.getConnection().close();
            // CHECKSTYLE:OFF
        } catch (SQLException ex) {
            // CHECKSTYLE:ON
            log.error("Close datasource connection failed", ex);
        }
    }
    
    private Map<String, Collection<RuleConfiguration>> loadSchemaRules(final MetaDataPersistService metaDataPersistService, final Collection<String> schemaNames) {
        return schemaNames.stream().collect(Collectors.toMap(
            each -> each, each -> metaDataPersistService.getSchemaRuleService().load(each), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private Map<String, Map<String, DataSource>> getLoadDataSources(final Map<String, Map<String, DataSourceConfiguration>> loadedDataSourceConfigurations,
                                                                      final Map<String, Map<String, DataSource>> dataSourcesMap) {
        if (loadedDataSourceConfigurations.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, DataSource>> result = new LinkedHashMap<>(loadedDataSourceConfigurations.size(), 1);
        for (Entry<String, Map<String, DataSourceConfiguration>> each : loadedDataSourceConfigurations.entrySet()) {
            Map<String, DataSource> dataSources = new LinkedHashMap<>();
            Map<String, DataSourceConfiguration> dataSourceConfigurationMap = loadedDataSourceConfigurations.get(each.getKey());
            for (Entry<String, DataSourceConfiguration> entry : dataSourceConfigurationMap.entrySet()) {
                Map<String, DataSource> dataSourceMap = dataSourcesMap.get(each.getKey());
                if (null != dataSourceMap && null != dataSourceMap.get(entry.getKey())
                        && entry.getValue().equals(DataSourcePoolCreatorUtil.getDataSourceConfiguration(dataSourceMap.get(entry.getKey())))) {
                    dataSources.put(entry.getKey(), dataSourceMap.get(entry.getKey()));
                } else {
                    dataSources.put(entry.getKey(), DataSourcePoolCreatorUtil.getDataSource(entry.getValue()));
                }
            }
            result.put(each.getKey(), dataSources);
        }
        return result;
    }
    
    private void disableDataSources() {
        metaDataContexts.getMetaDataMap().forEach((key, value)
            -> value.getRuleMetaData().getRules().stream().filter(each -> each instanceof StatusContainedRule).forEach(each -> disableDataSources(key, (StatusContainedRule) each)));
    }
    
    private void disableDataSources(final String schemaName, final StatusContainedRule rule) {
        Collection<String> disabledDataSources = registryCenter.getStorageNodeStatusService().loadStorageNodes(schemaName, StorageNodeStatus.DISABLE);
        disabledDataSources.stream().map(this::getDataSourceName).forEach(each -> rule.updateStatus(new DataSourceNameDisabledEvent(each, true)));
    }
    
    private String getDataSourceName(final String disabledDataSource) {
        return new QualifiedSchema(disabledDataSource).getDataSourceName();
    }
    
    private void persistMetaData(final Map<String, ShardingSphereSchema> schemas) {
        schemas.forEach((key, value) -> metaDataPersistService.getSchemaMetaDataService().persist(key, value));
    }
    
    @Override
    public String getType() {
        return "Cluster";
    }
}
