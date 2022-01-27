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

package org.apache.shardingsphere.mode.manager.standalone;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.config.schema.SchemaConfiguration;
import org.apache.shardingsphere.infra.config.schema.impl.DataSourceProvidedSchemaConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyerFactory;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.loader.SchemaLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;
import org.apache.shardingsphere.infra.rule.identifier.type.InstanceAwareRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.standalone.workerid.generator.StandaloneWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
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
 * Standalone context manager builder.
 */
@Slf4j
public final class StandaloneContextManagerBuilder implements ContextManagerBuilder {
    
    static {
        ShardingSphereServiceLoader.register(StandalonePersistRepository.class);
    }
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter parameter) throws SQLException {
        PersistRepositoryConfiguration repositoryConfig = null == parameter.getModeConfig().getRepository() ? new StandalonePersistRepositoryConfiguration("File", new Properties())
                : parameter.getModeConfig().getRepository();
        StandalonePersistRepository repository = TypedSPIRegistry.getRegisteredService(StandalonePersistRepository.class, repositoryConfig.getType(), repositoryConfig.getProps());
        MetaDataPersistService metaDataPersistService = new MetaDataPersistService(repository);
        persistConfigurations(metaDataPersistService, parameter.getSchemaConfigs(), parameter.getGlobalRuleConfigs(), parameter.getProps(), parameter.getModeConfig().isOverwrite());
        Collection<String> schemaNames = parameter.getInstanceDefinition().getInstanceType() == InstanceType.JDBC ? parameter.getSchemaConfigs().keySet()
                : metaDataPersistService.getSchemaMetaDataService().loadAllNames();
        Map<String, Map<String, DataSource>> standaloneDataSources = loadDataSourcesMap(metaDataPersistService, parameter.getSchemaConfigs(), schemaNames);
        Map<String, Collection<RuleConfiguration>> standaloneSchemaRules = loadSchemaRules(metaDataPersistService, schemaNames);
        Map<String, SchemaConfiguration> schemaConfigs = new LinkedHashMap<>(standaloneDataSources.size(), 1);
        for (String each : standaloneDataSources.keySet()) {
            schemaConfigs.put(each, new DataSourceProvidedSchemaConfiguration(standaloneDataSources.get(each), standaloneSchemaRules.get(each)));
        }
        Properties loadedProps = metaDataPersistService.getPropsService().load();
        Map<String, Collection<ShardingSphereRule>> rules = SchemaRulesBuilder.buildRules(schemaConfigs, loadedProps);
        MetaDataContexts metaDataContexts = new MetaDataContextsBuilder(
                schemaConfigs, metaDataPersistService.getGlobalRuleService().load(), getShardingSphereSchemas(schemaConfigs, rules, loadedProps), rules, loadedProps).build(metaDataPersistService);
        TransactionContexts transactionContexts = new TransactionContextsBuilder(metaDataContexts.getMetaDataMap(), metaDataContexts.getGlobalRuleMetaData().getRules(), parameter.getInstanceDefinition().getInstanceId().getId()).build();
        ContextManager result = new ContextManager();
        result.init(metaDataContexts, transactionContexts, new InstanceContext(metaDataPersistService.getComputeNodePersistService().loadComputeNodeInstance(parameter.getInstanceDefinition()), 
                new StandaloneWorkerIdGenerator()));
        buildSpecialRules(result);
        return result;
    }
    
    private void persistConfigurations(final MetaDataPersistService metaDataPersistService, final Map<String, ? extends SchemaConfiguration> schemaConfig, 
                                       final Collection<RuleConfiguration> globalRuleConfigs,
                                       final Properties props, final boolean overwrite) {
        if (!isEmptyLocalConfiguration(schemaConfig, globalRuleConfigs, props)) {
            metaDataPersistService.persistConfigurations(schemaConfig, globalRuleConfigs, props, overwrite);
        }
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
    
    private Map<String, Map<String, DataSource>> loadDataSourcesMap(final MetaDataPersistService metaDataPersistService, 
                                                                    final Map<String, ? extends SchemaConfiguration> schemaConfig, final Collection<String> schemaNames) {
        Map<String, Map<String, DataSourceProperties>> loadedDataSourcePropsMaps = loadedDataSourcePropertiesMaps(metaDataPersistService, schemaNames);
        Map<String, Map<String, DataSource>> dataSourcesMap = getDataSourcesMap(schemaConfig);
        Map<String, Map<String, DataSource>> result = getLoadDataSources(loadedDataSourcePropsMaps, dataSourcesMap);
        closeLocalDataSources(dataSourcesMap, result);
        return result;
    }
    
    private Map<String, Map<String, DataSource>> getDataSourcesMap(final Map<String, ? extends SchemaConfiguration> schemaConfig) {
        Map<String, Map<String, DataSource>> result = new LinkedHashMap<>(schemaConfig.size(), 1);
        for (Entry<String, ? extends SchemaConfiguration> entry : schemaConfig.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getDataSources());
        }
        return result;
    }
    
    private Map<String, Map<String, DataSourceProperties>> loadedDataSourcePropertiesMaps(final MetaDataPersistService metaDataPersistService, final Collection<String> schemaNames) {
        Map<String, Map<String, DataSourceProperties>> result = new LinkedHashMap<>();
        for (String each : schemaNames) {
            result.put(each, metaDataPersistService.getDataSourceService().load(each));
        }
        return result;
    }
    
    private Map<String, Map<String, DataSource>> getLoadDataSources(final Map<String, Map<String, DataSourceProperties>> loadedDataSourcePropsMaps,
                                                                    final Map<String, Map<String, DataSource>> localDataSourcesMap) {
        Map<String, Map<String, DataSource>> result = new LinkedHashMap<>(loadedDataSourcePropsMaps.size(), 1);
        for (Entry<String, Map<String, DataSourceProperties>> each : loadedDataSourcePropsMaps.entrySet()) {
            Map<String, DataSource> dataSources = new LinkedHashMap<>();
            Map<String, DataSourceProperties> loadDataSourcePropsMap = loadedDataSourcePropsMaps.get(each.getKey());
            for (Entry<String, DataSourceProperties> entry : loadDataSourcePropsMap.entrySet()) {
                Map<String, DataSource> localDataSources = localDataSourcesMap.get(each.getKey());
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
    
    private void buildSpecialRules(final ContextManager contextManager) {
        contextManager.getMetaDataContexts().getMetaDataMap().forEach((key, value)
            -> value.getRuleMetaData().getRules().stream().filter(each -> each instanceof InstanceAwareRule)
            .forEach(each -> ((InstanceAwareRule) each).setInstanceContext(contextManager.getInstanceContext())));
    }
    
    private Map<String, ShardingSphereSchema> getShardingSphereSchemas(final Map<String, ? extends SchemaConfiguration> schemaConfigs, final Map<String, Collection<ShardingSphereRule>> rules,
                                                                       final Properties props) throws SQLException {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(schemaConfigs.size(), 1);
        for (String each : schemaConfigs.keySet()) {
            result.put(each, SchemaLoader.load(schemaConfigs.get(each).getDataSources(), rules.get(each), props));
        }
        return result;
    }
    
    @Override
    public String getType() {
        return "Standalone";
    }
}
