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
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.datasource.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.metadata.persist.data.ShardingSphereDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.config.database.datasource.DataSourceNodePersistService;
import org.apache.shardingsphere.metadata.persist.service.config.database.datasource.DataSourceUnitPersistService;
import org.apache.shardingsphere.metadata.persist.service.config.database.rule.DatabaseRulePersistService;
import org.apache.shardingsphere.metadata.persist.service.config.global.GlobalRulePersistService;
import org.apache.shardingsphere.metadata.persist.service.config.global.PropertiesPersistService;
import org.apache.shardingsphere.metadata.persist.service.database.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Meta data persist service.
 */
@Getter
public final class MetaDataPersistService implements MetaDataBasedPersistService {
    
    private final PersistRepository repository;
    
    private final DataSourceUnitPersistService dataSourceUnitService;
    
    private final DataSourceNodePersistService dataSourceNodeService;
    
    private final DatabaseMetaDataPersistService databaseMetaDataService;
    
    private final DatabaseRulePersistService databaseRulePersistService;
    
    private final GlobalRulePersistService globalRuleService;
    
    private final PropertiesPersistService propsService;
    
    private final MetaDataVersionPersistService metaDataVersionPersistService;
    
    private final ShardingSphereDataPersistService shardingSphereDataPersistService;
    
    public MetaDataPersistService(final PersistRepository repository) {
        this.repository = repository;
        dataSourceUnitService = new DataSourceUnitPersistService(repository);
        dataSourceNodeService = new DataSourceNodePersistService(repository);
        databaseMetaDataService = new DatabaseMetaDataPersistService(repository);
        databaseRulePersistService = new DatabaseRulePersistService(repository);
        globalRuleService = new GlobalRulePersistService(repository);
        propsService = new PropertiesPersistService(repository);
        metaDataVersionPersistService = new MetaDataVersionPersistService(repository);
        shardingSphereDataPersistService = new ShardingSphereDataPersistService(repository);
    }
    
    @Override
    public void persistGlobalRuleConfiguration(final Collection<RuleConfiguration> globalRuleConfigs, final Properties props) {
        globalRuleService.persist(globalRuleConfigs);
        propsService.persist(props);
    }
    
    @Override
    public void persistConfigurations(final String databaseName, final DatabaseConfiguration databaseConfigs,
                                      final Map<String, DataSource> dataSources, final Collection<ShardingSphereRule> rules) {
        Map<String, DataSourceProperties> dataSourcePropertiesMap = getDataSourcePropertiesMap(databaseConfigs);
        if (dataSourcePropertiesMap.isEmpty() && databaseConfigs.getRuleConfigurations().isEmpty()) {
            databaseMetaDataService.addDatabase(databaseName);
        } else {
            dataSourceUnitService.persist(databaseName, dataSourcePropertiesMap);
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
    
    private Map<String, DataSourceProperties> getDataSourcePropertiesMap(final DatabaseConfiguration databaseConfigs) {
        if (!databaseConfigs.getDataSources().isEmpty() && databaseConfigs.getDataSourcePropsMap().isEmpty()) {
            return getDataSourcePropertiesMap(databaseConfigs.getStorageResource().getStorageNodes());
        }
        return databaseConfigs.getDataSourcePropsMap();
    }
    
    private Map<String, DataSourceProperties> getDataSourcePropertiesMap(final Map<String, DataSource> dataSourceMap) {
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(dataSourceMap.size(), 1F);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            result.put(entry.getKey(), DataSourcePropertiesCreator.create(entry.getValue()));
        }
        return result;
    }
    
    @Override
    public Map<String, DataSourceConfiguration> getEffectiveDataSources(final String databaseName, final Map<String, ? extends DatabaseConfiguration> databaseConfigs) {
        Map<String, DataSourceProperties> persistedDataPropsMap = dataSourceUnitService.load(databaseName);
        if (databaseConfigs.containsKey(databaseName) && !databaseConfigs.get(databaseName).getDataSources().isEmpty()) {
            databaseConfigs.get(databaseName).getStorageResource().getStorageNodes().values().forEach(each -> new DataSourcePoolDestroyer(each).asyncDestroy());
        }
        return persistedDataPropsMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey,
                entry -> DataSourcePropertiesCreator.createConfiguration(entry.getValue()), (key, value) -> value, LinkedHashMap::new));
    }
}
