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

import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.metadata.persist.data.ShardingSphereDataBasedPersistService;
import org.apache.shardingsphere.metadata.persist.service.config.database.DatabaseBasedPersistService;
import org.apache.shardingsphere.metadata.persist.service.config.global.GlobalPersistService;
import org.apache.shardingsphere.metadata.persist.service.database.DatabaseMetaDataBasedPersistService;
import org.apache.shardingsphere.metadata.persist.service.version.MetaDataVersionBasedPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * TODO replace the old implementation after meta data refactor completed
 * Abstract meta data persist service.
 */
public interface MetaDataBasedPersistService {
    
    /**
     * Get repository.
     * 
     * @return repository
     */
    PersistRepository getRepository();
    
    /**
     * Get data source unit service.
     * 
     * @return persist service
     */
    DatabaseBasedPersistService<Map<String, DataSourceProperties>> getDataSourceUnitService();
    
    /**
     * Get data source node service.
     *
     * @return persist service
     */
    DatabaseBasedPersistService<Map<String, DataSourceProperties>> getDataSourceNodeService();
    
    /**
     * Get database meta data service.
     * 
     * @return persist service
     */
    DatabaseMetaDataBasedPersistService getDatabaseMetaDataService();
    
    /**
     * Get database rule persist service.
     * 
     * @return persist service
     */
    DatabaseBasedPersistService<Collection<RuleConfiguration>> getDatabaseRulePersistService();
    
    /**
     * Get global rule service.
     * 
     * @return repository
     */
    GlobalPersistService<Collection<RuleConfiguration>> getGlobalRuleService();
    
    /**
     * Get props service.
     * 
     * @return persist service
     */
    GlobalPersistService<Properties> getPropsService();
    
    /**
     * Get meta data version persist service.
     * 
     * @return persist service
     */
    MetaDataVersionBasedPersistService getMetaDataVersionPersistService();
    
    /**
     * Get ShardingSphere data persist service.
     * 
     * @return persist service
     */
    ShardingSphereDataBasedPersistService getShardingSphereDataPersistService();
    
    /**
     * Persist global rule configurations.
     * 
     * @param globalRuleConfigs global rule configurations
     * @param props properties
     */
    void persistGlobalRuleConfiguration(Collection<RuleConfiguration> globalRuleConfigs, Properties props);
    
    /**
     * Persist configurations.
     * 
     * @param databaseName database name
     * @param databaseConfigs database configurations
     * @param dataSources data sources
     * @param rules rules
     */
    void persistConfigurations(String databaseName, DatabaseConfiguration databaseConfigs, Map<String, DataSource> dataSources, Collection<ShardingSphereRule> rules);
    
    /**
     * Get effective data sources.
     *
     * @param databaseName database name
     * @param databaseConfigs database configurations
     * @return effective data sources
     */
    Map<String, DataSourceConfiguration> getEffectiveDataSources(String databaseName, Map<String, ? extends DatabaseConfiguration> databaseConfigs);
}
