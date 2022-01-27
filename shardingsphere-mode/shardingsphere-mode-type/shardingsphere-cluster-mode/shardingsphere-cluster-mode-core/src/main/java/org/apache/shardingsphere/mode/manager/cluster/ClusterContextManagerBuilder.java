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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.schema.SchemaConfiguration;
import org.apache.shardingsphere.infra.config.schema.impl.DataSourceProvidedSchemaConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyerFactory;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.loader.SchemaLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.ClusterContextManagerCoordinator;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.generator.ClusterWorkerIdGenerator;
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
    
    private InstanceContext instanceContext;
    
    private ContextManager contextManager;
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter parameter) throws SQLException {
        beforeBuildContextManager(parameter);
        contextManager = new ContextManager();
        contextManager.init(metaDataContexts, transactionContexts, instanceContext);
        afterBuildContextManager(parameter);
        return contextManager;
    }
    
    private void beforeBuildContextManager(final ContextManagerBuilderParameter parameter) throws SQLException {
        ClusterPersistRepository repository = createClusterPersistRepository((ClusterPersistRepositoryConfiguration) parameter.getModeConfig().getRepository());
        registryCenter = new RegistryCenter(repository);
        ModeScheduleContextFactory.getInstance().init(parameter.getInstanceDefinition().getInstanceId().getId(), parameter.getModeConfig());
        metaDataPersistService = new MetaDataPersistService(repository);
        persistConfigurations(metaDataPersistService, parameter.getSchemaConfigs(), parameter.getGlobalRuleConfigs(), parameter.getProps(), parameter.getModeConfig().isOverwrite());
        persistInstanceConfigurations(parameter.getLabels(), parameter.getInstanceDefinition(), parameter.getModeConfig().isOverwrite());
        Collection<String> schemaNames = parameter.getInstanceDefinition().getInstanceType() == InstanceType.JDBC ? parameter.getSchemaConfigs().keySet()
                : metaDataPersistService.getSchemaMetaDataService().loadAllNames();
        Map<String, Map<String, DataSource>> clusterDataSources = loadDataSourcesMap(metaDataPersistService, parameter.getSchemaConfigs(), schemaNames);
        Map<String, Collection<RuleConfiguration>> clusterSchemaRuleConfigs = loadSchemaRules(metaDataPersistService, schemaNames);
        Map<String, DataSourceProvidedSchemaConfiguration> schemaConfigs = new LinkedHashMap<>(clusterDataSources.size(), 1);
        for (String each : clusterDataSources.keySet()) {
            schemaConfigs.put(each, new DataSourceProvidedSchemaConfiguration(clusterDataSources.get(each), clusterSchemaRuleConfigs.get(each)));
        }
        Properties loadedProps = metaDataPersistService.getPropsService().load();
        Map<String, Collection<ShardingSphereRule>> rules = SchemaRulesBuilder.buildRules(schemaConfigs, loadedProps);
        Map<String, ShardingSphereSchema> schemas = getShardingSphereSchemas(schemaConfigs, rules, loadedProps);
        persistMetaData(schemas);
        metaDataContexts = new MetaDataContextsBuilder(schemaConfigs, metaDataPersistService.getGlobalRuleService().load(), schemas, rules, loadedProps).build(metaDataPersistService);
        transactionContexts = new TransactionContextsBuilder(metaDataContexts.getMetaDataMap(), metaDataContexts.getGlobalRuleMetaData().getRules(),
                parameter.getInstanceDefinition().getInstanceId().getId()).build();
        instanceContext = new InstanceContext(metaDataPersistService.getComputeNodePersistService().loadComputeNodeInstance(
                parameter.getInstanceDefinition()), new ClusterWorkerIdGenerator(repository, metaDataPersistService, parameter.getInstanceDefinition()));
    }
    
    private ClusterPersistRepository createClusterPersistRepository(final ClusterPersistRepositoryConfiguration config) {
        Preconditions.checkNotNull(config, "Cluster persist repository configuration cannot be null.");
        ClusterPersistRepository result = TypedSPIRegistry.getRegisteredService(ClusterPersistRepository.class, config.getType(), config.getProps());
        result.init(config);
        return result;
    }
    
    private void persistConfigurations(final MetaDataPersistService metaDataPersistService, final Map<String, ? extends SchemaConfiguration> schemaConfigs, 
                                       final Collection<RuleConfiguration> globalRuleConfigs, final Properties props, final boolean overwrite) {
        if (!isEmptyLocalConfiguration(schemaConfigs, globalRuleConfigs, props)) {
            metaDataPersistService.persistConfigurations(schemaConfigs, globalRuleConfigs, props, overwrite);
        }
    }
    
    private void persistInstanceConfigurations(final Collection<String> labels, final InstanceDefinition instanceDefinition, final boolean overwrite) {
        metaDataPersistService.persistInstanceConfigurations(instanceDefinition.getInstanceId().getId(), labels, overwrite);
    }
    
    private boolean isEmptyLocalConfiguration(final Map<String, ? extends SchemaConfiguration> schemaConfigs, final Collection<RuleConfiguration> globalRuleConfigs, final Properties props) {
        return isEmptyLocalDataSourcesMap(schemaConfigs) && isEmptyLocalSchemaRuleConfigurations(schemaConfigs) && globalRuleConfigs.isEmpty() && props.isEmpty();
    }
    
    private boolean isEmptyLocalDataSourcesMap(final Map<String, ? extends SchemaConfiguration> schemaConfigs) {
        return schemaConfigs.entrySet().stream().allMatch(entry -> entry.getValue().getDataSources().isEmpty());
    }
    
    private boolean isEmptyLocalSchemaRuleConfigurations(final Map<String, ? extends SchemaConfiguration> schemaConfigs) {
        return schemaConfigs.entrySet().stream().allMatch(entry -> entry.getValue().getRuleConfigurations().isEmpty());
    }
    
    private Map<String, Map<String, DataSource>> loadDataSourcesMap(final MetaDataPersistService metaDataPersistService, final Map<String, ? extends SchemaConfiguration> schemaConfig,
                                                                    final Collection<String> schemaNames) {
        Map<String, Map<String, DataSourceProperties>> loadedDataSourcePropertiesMap = loadDataSourceDataSourcePropertiesMap(metaDataPersistService, schemaNames);
        Map<String, Map<String, DataSource>> dataSourcesMap = getDataSourcesMap(schemaConfig);
        Map<String, Map<String, DataSource>> result = getLoadedDataSourceMap(loadedDataSourcePropertiesMap, dataSourcesMap);
        closeLocalDataSources(dataSourcesMap, result);
        return result;
    }
    
    private Map<String, Map<String, DataSourceProperties>> loadDataSourceDataSourcePropertiesMap(final MetaDataPersistService metaDataPersistService, final Collection<String> schemaNames) {
        Map<String, Map<String, DataSourceProperties>> result = new LinkedHashMap<>();
        for (String each : schemaNames) {
            result.put(each, metaDataPersistService.getDataSourceService().load(each));
        }
        return result;
    }
    
    private Map<String, Map<String, DataSource>> getDataSourcesMap(final Map<String, ? extends SchemaConfiguration> schemaConfig) {
        Map<String, Map<String, DataSource>> result = new LinkedHashMap<>(schemaConfig.size(), 1);
        for (Entry<String, ? extends SchemaConfiguration> entry : schemaConfig.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getDataSources());
        }
        return result;
    }
    
    private Map<String, Map<String, DataSource>> getLoadedDataSourceMap(final Map<String, Map<String, DataSourceProperties>> loadedDataSourcePropertiesMaps,
                                                                        final Map<String, Map<String, DataSource>> localDataSourceMaps) {
        Map<String, Map<String, DataSource>> result = new LinkedHashMap<>(loadedDataSourcePropertiesMaps.size(), 1);
        for (Entry<String, Map<String, DataSourceProperties>> each : loadedDataSourcePropertiesMaps.entrySet()) {
            Map<String, DataSource> dataSources = new LinkedHashMap<>();
            Map<String, DataSourceProperties> loadedDataSourcePropertiesMap = loadedDataSourcePropertiesMaps.get(each.getKey());
            for (Entry<String, DataSourceProperties> entry : loadedDataSourcePropertiesMap.entrySet()) {
                Map<String, DataSource> localDataSources = localDataSourceMaps.get(each.getKey());
                if (null != localDataSources && null != localDataSources.get(entry.getKey()) && DataSourcePropertiesCreator.create(localDataSources.get(entry.getKey())).equals(entry.getValue())) {
                    dataSources.put(entry.getKey(), localDataSources.get(entry.getKey()));
                } else {
                    dataSources.put(entry.getKey(), DataSourcePoolCreator.create(entry.getValue()));
                }
            }
            result.put(each.getKey(), dataSources);
        }
        return result;
    }
    
    private void closeLocalDataSources(final Map<String, Map<String, DataSource>> localDataSourceMap, final Map<String, Map<String, DataSource>> loadDataSourceMap) {
        for (Entry<String, Map<String, DataSource>> entry : localDataSourceMap.entrySet()) {
            if (loadDataSourceMap.containsKey(entry.getKey())) {
                entry.getValue().forEach((key, value) -> {
                    if (null == loadDataSourceMap.get(entry.getKey()).get(key)) {
                        closeDataSource(value);
                    }
                });
            }
        }
    }
    
    private void closeDataSource(final DataSource dataSource) {
        try {
            DataSourcePoolDestroyerFactory.destroy(dataSource);
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
    
    private Map<String, ShardingSphereSchema> getShardingSphereSchemas(final Map<String, ? extends SchemaConfiguration> schemaConfigs, final Map<String, Collection<ShardingSphereRule>> rules, 
                                                                       final Properties props) throws SQLException {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(schemaConfigs.size(), 1);
        for (String each : schemaConfigs.keySet()) {
            result.put(each, SchemaLoader.load(schemaConfigs.get(each).getDataSources(), rules.get(each), props));
        }
        return result;
    }
    
    private void persistMetaData(final Map<String, ShardingSphereSchema> schemas) {
        schemas.forEach((key, value) -> metaDataPersistService.getSchemaMetaDataService().persist(key, value));
    }
    
    private void afterBuildContextManager(final ContextManagerBuilderParameter parameter) {
        new ClusterContextManagerCoordinator(metaDataPersistService, contextManager, registryCenter);
        registryCenter.onlineInstance(parameter.getInstanceDefinition());
    }
    
    @Override
    public String getType() {
        return "Cluster";
    }
}
