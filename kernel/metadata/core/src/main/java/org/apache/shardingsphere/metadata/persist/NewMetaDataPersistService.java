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

package org.apache.shardingsphere.metadata.persist;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.decorator.RuleConfigurationDecorator;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.metadata.persist.data.ShardingSphereDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.config.database.NewDataSourcePersistService;
import org.apache.shardingsphere.metadata.persist.service.config.database.NewDatabaseRulePersistService;
import org.apache.shardingsphere.metadata.persist.service.config.global.NewGlobalRulePersistService;
import org.apache.shardingsphere.metadata.persist.service.config.global.NewPropertiesPersistService;
import org.apache.shardingsphere.metadata.persist.service.database.NewDatabaseMetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * TODO replace the old implementation after meta data refactor completed
 * New meta data persist service.
 */
@Getter
public final class NewMetaDataPersistService implements MetaDataBasedPersistService {
    
    private final PersistRepository repository;
    
    private final MetaDataVersionPersistService metaDataVersionPersistService;
    
    private final NewDataSourcePersistService dataSourceService;
    
    private final NewDatabaseMetaDataPersistService databaseMetaDataService;
    
    private final NewDatabaseRulePersistService databaseRulePersistService;
    
    private final NewGlobalRulePersistService globalRuleService;
    
    private final NewPropertiesPersistService propsService;
    
    private final ShardingSphereDataPersistService shardingSphereDataPersistService;
    
    public NewMetaDataPersistService(final PersistRepository repository) {
        this.repository = repository;
        metaDataVersionPersistService = new MetaDataVersionPersistService(repository);
        dataSourceService = new NewDataSourcePersistService(repository);
        databaseMetaDataService = new NewDatabaseMetaDataPersistService(repository, metaDataVersionPersistService);
        databaseRulePersistService = new NewDatabaseRulePersistService(repository);
        globalRuleService = new NewGlobalRulePersistService(repository);
        propsService = new NewPropertiesPersistService(repository);
        shardingSphereDataPersistService = new ShardingSphereDataPersistService(repository);
    }
    
    /**
     * Persist global rule configurations.
     *
     * @param globalRuleConfigs global rule configurations
     * @param props properties
     */
    @Override
    public void persistGlobalRuleConfiguration(final Collection<RuleConfiguration> globalRuleConfigs, final Properties props) {
        globalRuleService.persist(globalRuleConfigs);
        propsService.persist(props);
    }
    
    @Override
    public void persistConfigurations(final String databaseName, final DatabaseConfiguration databaseConfigs,
                                      final Map<String, DataSource> dataSources, final Collection<ShardingSphereRule> rules) {
        Map<String, DataSourceProperties> dataSourcePropertiesMap = getDataSourcePropertiesMap(databaseConfigs.getDataSources());
        if (dataSourcePropertiesMap.isEmpty() && databaseConfigs.getRuleConfigurations().isEmpty()) {
            databaseMetaDataService.addDatabase(databaseName);
        } else {
            dataSourceService.persist(databaseName, getDataSourcePropertiesMap(databaseConfigs.getDataSources()));
            databaseRulePersistService.persist(databaseName, decorateRuleConfigs(databaseName, dataSources, rules));
        }
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> decorateRuleConfigs(final String databaseName, final Map<String, DataSource> dataSources, final Collection<ShardingSphereRule> rules) {
        Collection<RuleConfiguration> result = new LinkedList<>();
        for (ShardingSphereRule each : rules) {
            RuleConfiguration ruleConfig = each.getConfiguration();
            if (TypedSPILoader.contains(RuleConfigurationDecorator.class, ruleConfig.getClass().getName())) {
                result.add(TypedSPILoader.getService(RuleConfigurationDecorator.class, ruleConfig.getClass().getName()).decorate(databaseName, dataSources, rules, ruleConfig));
            } else {
                result.add(each.getConfiguration());
            }
        }
        return result;
    }
    
    private Map<String, DataSourceProperties> getDataSourcePropertiesMap(final Map<String, DataSource> dataSourceMap) {
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(dataSourceMap.size(), 1F);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            result.put(entry.getKey(), DataSourcePropertiesCreator.create(entry.getValue()));
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
    @Override
    public Map<String, DataSource> getEffectiveDataSources(final String databaseName, final Map<String, ? extends DatabaseConfiguration> databaseConfigs) {
        Map<String, DataSourceProperties> persistedDataPropsMap = dataSourceService.load(databaseName);
        return databaseConfigs.containsKey(databaseName)
                ? mergeEffectiveDataSources(persistedDataPropsMap, databaseConfigs.get(databaseName).getDataSources())
                : DataSourcePoolCreator.create(persistedDataPropsMap);
    }
    
    private Map<String, DataSource> mergeEffectiveDataSources(final Map<String, DataSourceProperties> persistedDataSourcePropsMap, final Map<String, DataSource> localConfiguredDataSources) {
        Map<String, DataSource> result = new LinkedHashMap<>(persistedDataSourcePropsMap.size(), 1F);
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
}
