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

package org.apache.shardingsphere.mode.persist.service;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Meta data manager persist service.
 */
public interface MetaDataManagerPersistService {
    
    /**
     * Create database.
     *
     * @param databaseName database name
     */
    void createDatabase(String databaseName);
    
    /**
     * Drop database.
     *
     * @param database database
     */
    void dropDatabase(ShardingSphereDatabase database);
    
    /**
     * Create schema.
     *
     * @param database database
     * @param schemaName schema name
     */
    void createSchema(ShardingSphereDatabase database, String schemaName);
    
    /**
     * Alter schema.
     *
     * @param database database
     * @param schemaName schema name
     * @param alteredTables altered tables
     * @param alteredViews altered views
     * @param droppedTables dropped tables
     * @param droppedViews dropped views
     */
    void alterSchema(ShardingSphereDatabase database, String schemaName,
                     Collection<ShardingSphereTable> alteredTables, Collection<ShardingSphereView> alteredViews, Collection<String> droppedTables, Collection<String> droppedViews);
    
    /**
     * Rename schema.
     *
     * @param database database
     * @param schemaName schema name
     * @param renameSchemaName rename schema name
     */
    void renameSchema(ShardingSphereDatabase database, String schemaName, String renameSchemaName);
    
    /**
     * Drop schema.
     *
     * @param database database
     * @param schemaNames schema names
     */
    void dropSchema(ShardingSphereDatabase database, Collection<String> schemaNames);
    
    /**
     * Create table.
     *
     * @param database database
     * @param schemaName schema name
     * @param table table
     */
    void createTable(ShardingSphereDatabase database, String schemaName, ShardingSphereTable table);
    
    /**
     * Drop tables.
     *
     * @param database database
     * @param schemaName schema name
     * @param tableName table name
     */
    void dropTable(ShardingSphereDatabase database, String schemaName, String tableName);
    
    /**
     * Register storage units.
     *
     * @param databaseName database name
     * @param toBeRegisteredProps to be registered storage unit properties
     */
    void registerStorageUnits(String databaseName, Map<String, DataSourcePoolProperties> toBeRegisteredProps);
    
    /**
     * Alter storage units.
     *
     * @param database database
     * @param toBeUpdatedProps to be updated storage unit properties
     */
    void alterStorageUnits(ShardingSphereDatabase database, Map<String, DataSourcePoolProperties> toBeUpdatedProps);
    
    /**
     * Unregister storage units.
     * @param database database
     * @param toBeDroppedStorageUnitNames to be dropped storage unit names
     */
    void unregisterStorageUnits(ShardingSphereDatabase database, Collection<String> toBeDroppedStorageUnitNames);
    
    /**
     * Alter single rule configuration.
     *
     * @param database database
     * @param ruleMetaData rule meta data
     * @throws SQLException SQL exception
     */
    void alterSingleRuleConfiguration(ShardingSphereDatabase database, RuleMetaData ruleMetaData) throws SQLException;
    
    /**
     * Alter rule configuration.
     *
     * @param database database
     * @param toBeAlteredRuleConfig to be altered rule config
     * @throws SQLException SQL exception
     */
    void alterRuleConfiguration(ShardingSphereDatabase database, RuleConfiguration toBeAlteredRuleConfig) throws SQLException;
    
    /**
     * Remove rule configuration item.
     *
     * @param database database
     * @param toBeRemovedRuleConfig to be removed rule config
     * @throws SQLException SQL exception
     */
    void removeRuleConfigurationItem(ShardingSphereDatabase database, RuleConfiguration toBeRemovedRuleConfig) throws SQLException;
    
    /**
     * Remove rule configuration.
     *
     * @param database database
     * @param ruleType rule type
     */
    void removeRuleConfiguration(ShardingSphereDatabase database, String ruleType);
    
    /**
     * Alter global rule configuration.
     *
     * @param globalRuleConfig global rule config
     */
    void alterGlobalRuleConfiguration(RuleConfiguration globalRuleConfig);
    
    /**
     * Alter properties.
     *
     * @param props pros
     */
    void alterProperties(Properties props);
}
