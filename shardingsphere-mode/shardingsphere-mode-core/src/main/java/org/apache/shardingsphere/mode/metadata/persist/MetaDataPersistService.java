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

package org.apache.shardingsphere.mode.metadata.persist;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.mode.metadata.persist.service.DatabaseVersionPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.SchemaMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.DataSourcePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.DatabaseRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.GlobalRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.PropertiesPersistService;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

/**
 * Meta data persist service.
 */
@Getter
public final class MetaDataPersistService {
    
    private final PersistRepository repository;
    
    private final DataSourcePersistService dataSourceService;
    
    private final SchemaMetaDataPersistService schemaMetaDataService;
    
    private final DatabaseRulePersistService databaseRulePersistService;
    
    private final GlobalRulePersistService globalRuleService;
    
    private final PropertiesPersistService propsService;
    
    private final DatabaseVersionPersistService databaseVersionPersistService;
    
    public MetaDataPersistService(final PersistRepository repository) {
        this.repository = repository;
        dataSourceService = new DataSourcePersistService(repository);
        schemaMetaDataService = new SchemaMetaDataPersistService(repository);
        databaseRulePersistService = new DatabaseRulePersistService(repository);
        globalRuleService = new GlobalRulePersistService(repository);
        propsService = new PropertiesPersistService(repository);
        databaseVersionPersistService = new DatabaseVersionPersistService(repository);
    }
    
    /**
     * Persist configurations.
     *
     * @param schemaConfigs schema configurations
     * @param globalRuleConfigs global rule configurations
     * @param props properties
     * @param isOverwrite whether overwrite registry center's configuration if existed
     */
    public void persistConfigurations(final Map<String, ? extends DatabaseConfiguration> schemaConfigs,
                                      final Collection<RuleConfiguration> globalRuleConfigs, final Properties props, final boolean isOverwrite) {
        globalRuleService.persist(globalRuleConfigs, isOverwrite);
        propsService.persist(props, isOverwrite);
        for (Entry<String, ? extends DatabaseConfiguration> entry : schemaConfigs.entrySet()) {
            String databaseName = entry.getKey();
            Map<String, DataSourceProperties> dataSourcePropertiesMap = getDataSourcePropertiesMap(entry.getValue().getDataSources());
            Collection<RuleConfiguration> ruleConfigurations = entry.getValue().getRuleConfigurations();
            if (dataSourcePropertiesMap.isEmpty() && ruleConfigurations.isEmpty()) {
                schemaMetaDataService.persistDatabase(databaseName);
            } else {
                dataSourceService.persist(databaseName, getDataSourcePropertiesMap(entry.getValue().getDataSources()), isOverwrite);
                databaseRulePersistService.persist(databaseName, entry.getValue().getRuleConfigurations(), isOverwrite);
            }
        }
    }
    
    private Map<String, DataSourceProperties> getDataSourcePropertiesMap(final Map<String, DataSource> dataSourceMap) {
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(dataSourceMap.size(), 1);
        for (Entry<String, DataSource> each : dataSourceMap.entrySet()) {
            result.put(each.getKey(), DataSourcePropertiesCreator.create(each.getValue()));
        }
        return result;
    }
    
    /**
     * Get effective data sources.
     *
     * @param databaseName database name
     * @param databaseConfigs database configurations
     * @return effective data sources
     */
    public Map<String, DataSource> getEffectiveDataSources(final String databaseName, final Map<String, ? extends DatabaseConfiguration> databaseConfigs) {
        Map<String, DataSourceProperties> persistedDataPropsMap = dataSourceService.load(databaseName);
        return databaseConfigs.containsKey(databaseName)
                ? mergeEffectiveDataSources(persistedDataPropsMap, databaseConfigs.get(databaseName).getDataSources())
                : DataSourcePoolCreator.create(persistedDataPropsMap);
    }
    
    private Map<String, DataSource> mergeEffectiveDataSources(final Map<String, DataSourceProperties> persistedDataSourcePropsMap, final Map<String, DataSource> localConfiguredDataSources) {
        Map<String, DataSource> result = new LinkedHashMap<>(persistedDataSourcePropsMap.size(), 1);
        for (Entry<String, DataSourceProperties> entry : persistedDataSourcePropsMap.entrySet()) {
            String dataSourceName = entry.getKey();
            DataSourceProperties persistedDataSourceProps = entry.getValue();
            DataSource localConfiguredDataSource = localConfiguredDataSources.get(dataSourceName);
            if (null == localConfiguredDataSource) {
                result.put(dataSourceName, DataSourcePoolCreator.create(persistedDataSourceProps));
            } else if (DataSourcePropertiesCreator.create(localConfiguredDataSource).equals(persistedDataSourceProps)) {
                result.put(dataSourceName, localConfiguredDataSource);
            } else {
                result.put(dataSourceName, DataSourcePoolCreator.create(persistedDataSourceProps));
                new DataSourcePoolDestroyer(localConfiguredDataSource).asyncDestroy();
            }
        }
        return result;
    }
    
    /**
     * Persist transaction rule.
     *
     * @param props transaction props
     * @param isOverwrite whether overwrite registry center's configuration if existed
     */
    public void persistTransactionRule(final Properties props, final boolean isOverwrite) {
        Collection<RuleConfiguration> ruleConfigs = globalRuleService.load();
        Optional<RuleConfiguration> ruleConfig = ruleConfigs.stream().filter(each -> each instanceof TransactionRuleConfiguration).findFirst();
        Preconditions.checkState(ruleConfig.isPresent());
        if (!props.equals(((TransactionRuleConfiguration) ruleConfig.get()).getProps())) {
            ((TransactionRuleConfiguration) ruleConfig.get()).getProps().clear();
            ((TransactionRuleConfiguration) ruleConfig.get()).getProps().putAll(props);
            globalRuleService.persist(ruleConfigs, isOverwrite);
        }
    }
}
