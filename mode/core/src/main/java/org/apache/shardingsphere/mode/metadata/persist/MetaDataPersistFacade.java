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

import lombok.Getter;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.decorator.RuleConfigurationDecorator;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.persist.config.database.DataSourceUnitPersistService;
import org.apache.shardingsphere.mode.metadata.persist.config.database.DatabaseRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.config.global.GlobalRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.config.global.PropertiesPersistService;
import org.apache.shardingsphere.mode.metadata.persist.metadata.DatabaseMetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.persist.statistics.StatisticsPersistService;
import org.apache.shardingsphere.mode.metadata.persist.version.VersionPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

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
 * Meta data persist service.
 */
@Getter
public final class MetaDataPersistFacade {
    
    private final PersistRepository repository;
    
    private final VersionPersistService versionService;
    
    private final DataSourceUnitPersistService dataSourceUnitService;
    
    private final DatabaseMetaDataPersistFacade databaseMetaDataFacade;
    
    private final DatabaseRulePersistService databaseRuleService;
    
    private final GlobalRulePersistService globalRuleService;
    
    private final PropertiesPersistService propsService;
    
    private final StatisticsPersistService statisticsService;
    
    public MetaDataPersistFacade(final PersistRepository repository, final boolean persistSchemasEnabled) {
        this.repository = repository;
        versionService = new VersionPersistService(repository);
        dataSourceUnitService = new DataSourceUnitPersistService(repository);
        databaseMetaDataFacade = new DatabaseMetaDataPersistFacade(repository, versionService, persistSchemasEnabled);
        databaseRuleService = new DatabaseRulePersistService(repository);
        globalRuleService = new GlobalRulePersistService(repository, versionService);
        propsService = new PropertiesPersistService(repository, versionService);
        statisticsService = new StatisticsPersistService(repository);
    }
    
    public MetaDataPersistFacade(final PersistRepository repository) {
        this(repository, true);
    }
    
    /**
     * Persist global rule configurations.
     *
     * @param globalRuleConfigs global rule configurations
     * @param props properties
     */
    public void persistGlobalRuleConfiguration(final Collection<RuleConfiguration> globalRuleConfigs, final Properties props) {
        globalRuleService.persist(globalRuleConfigs);
        propsService.persist(props);
    }
    
    /**
     * Persist configurations.
     *
     * @param databaseName database name
     * @param databaseConfig database configuration
     * @param dataSources data sources
     * @param rules rules
     */
    public void persistConfigurations(final String databaseName, final DatabaseConfiguration databaseConfig, final Map<String, DataSource> dataSources, final Collection<ShardingSphereRule> rules) {
        Map<String, DataSourcePoolProperties> propsMap = getDataSourcePoolPropertiesMap(databaseConfig);
        if (propsMap.isEmpty() && databaseConfig.getRuleConfigurations().isEmpty()) {
            databaseMetaDataFacade.getDatabase().add(databaseName);
        } else {
            dataSourceUnitService.persist(databaseName, propsMap);
            databaseRuleService.persist(databaseName, decorateRuleConfigurations(databaseName, dataSources, rules));
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection<RuleConfiguration> decorateRuleConfigurations(final String databaseName, final Map<String, DataSource> dataSources, final Collection<ShardingSphereRule> rules) {
        Collection<RuleConfiguration> result = new LinkedList<>();
        for (ShardingSphereRule each : rules) {
            RuleConfiguration ruleConfig = each.getConfiguration();
            Optional<RuleConfigurationDecorator> decorator = TypedSPILoader.findService(RuleConfigurationDecorator.class, ruleConfig.getClass());
            result.add(decorator.map(optional -> optional.decorate(databaseName, dataSources, rules, ruleConfig)).orElse(ruleConfig));
        }
        return result;
    }
    
    private Map<String, DataSourcePoolProperties> getDataSourcePoolPropertiesMap(final DatabaseConfiguration databaseConfig) {
        return databaseConfig.getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSourcePoolProperties(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    /**
     * Load data source configurations.
     *
     * @param databaseName database name
     * @return data source configurations
     */
    public Map<String, DataSourceConfiguration> loadDataSourceConfigurations(final String databaseName) {
        return dataSourceUnitService.load(databaseName).entrySet().stream().collect(Collectors.toMap(Entry::getKey,
                entry -> DataSourcePoolPropertiesCreator.createConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
}
