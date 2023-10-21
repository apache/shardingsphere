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
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.metadata.persist.data.ShardingSphereDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.config.database.datasource.NewDataSourceNodePersistService;
import org.apache.shardingsphere.metadata.persist.service.config.database.datasource.NewDataSourceUnitPersistService;
import org.apache.shardingsphere.metadata.persist.service.config.database.rule.NewDatabaseRulePersistService;
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
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * TODO replace the old implementation after meta data refactor completed
 * New meta data persist service.
 */
@Getter
public final class NewMetaDataPersistService implements MetaDataBasedPersistService {
    
    private final PersistRepository repository;
    
    private final MetaDataVersionPersistService metaDataVersionPersistService;
    
    private final NewDataSourceUnitPersistService dataSourceUnitService;
    
    private final NewDataSourceNodePersistService dataSourceNodeService;
    
    private final NewDatabaseMetaDataPersistService databaseMetaDataService;
    
    private final NewDatabaseRulePersistService databaseRulePersistService;
    
    private final NewGlobalRulePersistService globalRuleService;
    
    private final NewPropertiesPersistService propsService;
    
    private final ShardingSphereDataPersistService shardingSphereDataPersistService;
    
    public NewMetaDataPersistService(final PersistRepository repository) {
        this.repository = repository;
        metaDataVersionPersistService = new MetaDataVersionPersistService(repository);
        dataSourceUnitService = new NewDataSourceUnitPersistService(repository);
        dataSourceNodeService = new NewDataSourceNodePersistService(repository);
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
    public void persistConfigurations(final String databaseName, final DatabaseConfiguration databaseConfigs, final Map<String, DataSource> dataSources, final Collection<ShardingSphereRule> rules) {
        Map<String, DataSourcePoolProperties> propsMap = getDataSourcePoolPropertiesMap(databaseConfigs);
        if (propsMap.isEmpty() && databaseConfigs.getRuleConfigurations().isEmpty()) {
            databaseMetaDataService.addDatabase(databaseName);
        } else {
            dataSourceUnitService.persist(databaseName, propsMap);
            databaseRulePersistService.persist(databaseName, decorateRuleConfigs(databaseName, dataSources, rules));
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection<RuleConfiguration> decorateRuleConfigs(final String databaseName, final Map<String, DataSource> dataSources, final Collection<ShardingSphereRule> rules) {
        Collection<RuleConfiguration> result = new LinkedList<>();
        for (ShardingSphereRule each : rules) {
            RuleConfiguration ruleConfig = each.getConfiguration();
            Optional<RuleConfigurationDecorator> decorator = TypedSPILoader.findService(RuleConfigurationDecorator.class, ruleConfig.getClass());
            result.add(decorator.map(optional -> optional.decorate(databaseName, dataSources, rules, ruleConfig)).orElse(ruleConfig));
        }
        return result;
    }
    
    private Map<String, DataSourcePoolProperties> getDataSourcePoolPropertiesMap(final DatabaseConfiguration databaseConfigs) {
        return databaseConfigs.getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSourcePoolProperties(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    @Override
    public Map<String, DataSourceConfiguration> loadDataSourceConfigurations(final String databaseName) {
        return dataSourceUnitService.load(databaseName).entrySet().stream().collect(Collectors.toMap(Entry::getKey,
                entry -> DataSourcePoolPropertiesCreator.createConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
}
