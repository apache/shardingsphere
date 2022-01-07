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

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.pool.creator.DataSourcePoolCreatorUtil;
import org.apache.shardingsphere.infra.config.datasource.pool.destroyer.DataSourcePoolDestroyerFactory;
import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.loader.SchemaLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
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
import java.util.Collections;
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
        persistConfigurations(metaDataPersistService, parameter.getDataSourcesMap(), parameter.getSchemaRuleConfigs(), parameter.getGlobalRuleConfigs(), parameter.getProps(), parameter.isOverwrite());
        Collection<String> schemaNames = Strings.isNullOrEmpty(parameter.getSchemaName()) ? metaDataPersistService.getSchemaMetaDataService().loadAllNames()
                : Collections.singletonList(parameter.getSchemaName());
        Map<String, Map<String, DataSource>> standaloneDataSources = loadDataSourcesMap(metaDataPersistService, parameter.getDataSourcesMap(), schemaNames);
        Map<String, Collection<RuleConfiguration>> standaloneSchemaRules = loadSchemaRules(metaDataPersistService, schemaNames);
        Properties standaloneProps = metaDataPersistService.getPropsService().load();
        Map<String, Collection<ShardingSphereRule>> rules = SchemaRulesBuilder.buildRules(standaloneDataSources, standaloneSchemaRules, standaloneProps);
        Map<String, ShardingSphereSchema> schemas = new SchemaLoader(standaloneDataSources, standaloneSchemaRules, rules, standaloneProps).load();
        MetaDataContexts metaDataContexts = new MetaDataContextsBuilder(standaloneDataSources, standaloneSchemaRules, metaDataPersistService.getGlobalRuleService().load(), schemas,
                rules, standaloneProps).build(metaDataPersistService);
        TransactionContexts transactionContexts = new TransactionContextsBuilder(metaDataContexts.getMetaDataMap(), metaDataContexts.getGlobalRuleMetaData().getRules()).build();
        ContextManager result = new ContextManager();
        result.init(metaDataContexts, transactionContexts);
        return result;
    }
    
    private void persistConfigurations(final MetaDataPersistService metaDataPersistService, final Map<String, Map<String, DataSource>> dataSourcesMap,
                                       final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs, final Collection<RuleConfiguration> globalRuleConfigs,
                                       final Properties props, final boolean overwrite) {
        if (!isEmptyLocalConfiguration(dataSourcesMap, schemaRuleConfigs, globalRuleConfigs, props)) {
            metaDataPersistService.persistConfigurations(getDataSourceConfigurations(dataSourcesMap), schemaRuleConfigs, globalRuleConfigs, props, overwrite);
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
        closeLocalDataSources(dataSourcesMap, result);
        return result;
    }
    
    private Map<String, Map<String, DataSourceConfiguration>> loadDataSourceConfigurations(final MetaDataPersistService metaDataPersistService, final Collection<String> schemaNames) {
        Map<String, Map<String, DataSourceConfiguration>> result = new LinkedHashMap<>();
        for (String each : schemaNames) {
            result.put(each, metaDataPersistService.getDataSourceService().load(each));
        }
        return result;
    }
    
    private Map<String, Map<String, DataSource>> getLoadDataSources(final Map<String, Map<String, DataSourceConfiguration>> loadDataSourceConfigurations,
                                                                    final Map<String, Map<String, DataSource>> localDataSourcesMap) {
        Map<String, Map<String, DataSource>> result = new LinkedHashMap<>(loadDataSourceConfigurations.size(), 1);
        for (Entry<String, Map<String, DataSourceConfiguration>> each : loadDataSourceConfigurations.entrySet()) {
            Map<String, DataSource> dataSources = new LinkedHashMap<>();
            Map<String, DataSourceConfiguration> loadDataSourceConfigurationMap = loadDataSourceConfigurations.get(each.getKey());
            for (Entry<String, DataSourceConfiguration> entry : loadDataSourceConfigurationMap.entrySet()) {
                Map<String, DataSource> localDataSources = localDataSourcesMap.get(each.getKey());
                if (null != localDataSources && null != localDataSources.get(entry.getKey())
                        && DataSourcePoolCreatorUtil.getDataSourceConfiguration(localDataSources.get(entry.getKey())).equals(entry.getValue())) {
                    dataSources.put(entry.getKey(), localDataSources.get(entry.getKey()));
                } else {
                    dataSources.put(entry.getKey(), DataSourcePoolCreatorUtil.getDataSource(entry.getValue()));
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
    
    @Override
    public String getType() {
        return "Standalone";
    }
}
