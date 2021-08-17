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

package org.apache.shardingsphere.governance.context;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.manager.ContextManager;
import org.apache.shardingsphere.infra.context.manager.ContextManagerBuilder;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.mode.ShardingSphereMode;
import org.apache.shardingsphere.infra.mode.repository.PersistRepository;
import org.apache.shardingsphere.infra.persist.DistMetaDataPersistService;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Cluster context manager builder.
 */
public final class ClusterContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ShardingSphereMode mode, final Map<String, Map<String, DataSource>> dataSourcesMap,
                                final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs, final Collection<RuleConfiguration> globalRuleConfigs,
                                final Properties props, final boolean isOverwrite) throws SQLException {
        Optional<PersistRepository> persistRepository = mode.getPersistRepository();
        Preconditions.checkState(persistRepository.isPresent());
        DistMetaDataPersistService persistService = new DistMetaDataPersistService(persistRepository.get());
        RegistryCenter registryCenter = new RegistryCenter((RegistryCenterRepository) persistRepository.get());
        persistConfigurations(persistService, dataSourcesMap, schemaRuleConfigs, globalRuleConfigs, props, isOverwrite);
        Collection<String> schemaNames = persistService.getSchemaMetaDataService().loadAllNames();
        MetaDataContexts metaDataContexts;
        // TODO isEmpty for test reg center fixture, will remove after local memory reg center fixture finished
        if (schemaNames.isEmpty()) {
            metaDataContexts = new MetaDataContextsBuilder(dataSourcesMap, schemaRuleConfigs, globalRuleConfigs, props).build(persistService);
            // TODO finish TODO 
        } else {
            metaDataContexts = new MetaDataContextsBuilder(loadDataSourcesMap(persistService, dataSourcesMap, schemaNames), 
                    loadSchemaRules(persistService, schemaNames), persistService.getGlobalRuleService().load(), persistService.getPropsService().load()).build(persistService);
        }
        TransactionContexts transactionContexts = createTransactionContexts(metaDataContexts);
        ContextManager result = new ClusterContextManager(persistService, registryCenter);
        result.init(metaDataContexts, transactionContexts);
        return result;
    }
    
    private void persistConfigurations(final DistMetaDataPersistService persistService, final Map<String, Map<String, DataSource>> dataSourcesMap,
                                       final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs, final Collection<RuleConfiguration> globalRuleConfigs,
                                       final Properties props, final boolean overwrite) {
        if (!isEmptyLocalConfiguration(dataSourcesMap, schemaRuleConfigs, globalRuleConfigs, props)) {
            persistService.persistConfigurations(getDataSourceConfigurations(dataSourcesMap), schemaRuleConfigs, globalRuleConfigs, props, overwrite);
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
            result.put(entry.getKey(), DataSourceConverter.getDataSourceConfigurationMap(entry.getValue()));
        }
        return result;
    }
    
    private Map<String, Map<String, DataSource>> loadDataSourcesMap(final DistMetaDataPersistService persistService, final Map<String, Map<String, DataSource>> dataSourcesMap,
                                                                    final Collection<String> schemaNames) {
        Map<String, Map<String, DataSourceConfiguration>> loadedDataSourceConfigs = loadDataSourceConfigurations(persistService, schemaNames);
        Map<String, Map<String, DataSourceConfiguration>> changedDataSourceConfigs = getChangedDataSourceConfigurations(dataSourcesMap, loadedDataSourceConfigs);
        Map<String, Map<String, DataSource>> result = new LinkedHashMap<>(dataSourcesMap);
        result.putAll(getChangedDataSources(changedDataSourceConfigs));
        return result;
    }
    
    private Map<String, Map<String, DataSourceConfiguration>> loadDataSourceConfigurations(final DistMetaDataPersistService persistService, final Collection<String> schemaNames) {
        Map<String, Map<String, DataSourceConfiguration>> result = new LinkedHashMap<>();
        for (String each : schemaNames) {
            result.put(each, persistService.getDataSourceService().load(each));
        }
        return result;
    }
    
    // TODO finish this method
    private Map<String, Map<String, DataSourceConfiguration>> getChangedDataSourceConfigurations(final Map<String, Map<String, DataSource>> configuredDataSourcesMap, 
                                                                                                 final Map<String, Map<String, DataSourceConfiguration>> loadedDataSourceConfigs) {
        return isEmptyLocalDataSourcesMap(configuredDataSourcesMap) ? loadedDataSourceConfigs : Collections.emptyMap();
    }
    
    private Map<String, Map<String, DataSource>> getChangedDataSources(final Map<String, Map<String, DataSourceConfiguration>> changedDataSourceConfigurations) {
        Map<String, Map<String, DataSource>> result = new LinkedHashMap<>(changedDataSourceConfigurations.size(), 1);
        for (Entry<String, Map<String, DataSourceConfiguration>> entry : changedDataSourceConfigurations.entrySet()) {
            result.put(entry.getKey(), createDataSources(entry.getValue()));
        }
        return result;
    }
    
    private Map<String, DataSource> createDataSources(final Map<String, DataSourceConfiguration> dataSourceConfigs) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSourceConfigs.size(), 1);
        for (Entry<String, DataSourceConfiguration> each : dataSourceConfigs.entrySet()) {
            result.put(each.getKey(), each.getValue().createDataSource());
        }
        return result;
    }
    
    private Map<String, Collection<RuleConfiguration>> loadSchemaRules(final DistMetaDataPersistService persistService, final Collection<String> schemaNames) {
        return schemaNames.stream().collect(Collectors.toMap(
            each -> each, each -> persistService.getSchemaRuleService().load(each), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private TransactionContexts createTransactionContexts(final MetaDataContexts metaDataContexts) {
        Map<String, ShardingTransactionManagerEngine> engines = new HashMap<>(metaDataContexts.getAllSchemaNames().size(), 1);
        String xaTransactionMangerType = metaDataContexts.getProps().getValue(ConfigurationPropertyKey.XA_TRANSACTION_MANAGER_TYPE);
        for (String each : metaDataContexts.getAllSchemaNames()) {
            ShardingTransactionManagerEngine engine = new ShardingTransactionManagerEngine();
            ShardingSphereResource resource = metaDataContexts.getMetaData(each).getResource();
            engine.init(resource.getDatabaseType(), resource.getDataSources(), xaTransactionMangerType);
            engines.put(each, engine);
        }
        return new TransactionContexts(engines);
    }
}
