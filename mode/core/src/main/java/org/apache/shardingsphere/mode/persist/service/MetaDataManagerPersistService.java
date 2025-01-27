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
     * @param databaseName database name
     */
    void dropDatabase(String databaseName);
    
    /**
     * Create schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    void createSchema(String databaseName, String schemaName);
    
    /**
     * Alter schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param logicDataSourceName logic data source name
     * @param alteredTables altered tables
     * @param alteredViews altered views
     * @param droppedTables dropped tables
     * @param droppedViews dropped views
     */
    void alterSchema(String databaseName, String schemaName, String logicDataSourceName,
                     Collection<ShardingSphereTable> alteredTables, Collection<ShardingSphereView> alteredViews, Collection<String> droppedTables, Collection<String> droppedViews);
    
    /**
     * Alter schema name.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param renameSchemaName rename schema name
     * @param logicDataSourceName logic data source name
     */
    void alterSchemaName(String databaseName, String schemaName, String renameSchemaName, String logicDataSourceName);
    
    /**
     * Drop schema.
     *
     * @param databaseName database name
     * @param schemaNames schema names
     */
    void dropSchema(String databaseName, Collection<String> schemaNames);
    
    /**
     * Create table.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param table table
     */
    void createTable(String databaseName, String schemaName, ShardingSphereTable table);
    
    /**
     * Drop tables.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     */
    void dropTable(String databaseName, String schemaName, String tableName);
    
    /**
     * Register storage units.
     *
     * @param databaseName database name
     * @param toBeRegisteredProps to be registered storage unit properties
     * @throws SQLException SQL exception
     */
    void registerStorageUnits(String databaseName, Map<String, DataSourcePoolProperties> toBeRegisteredProps) throws SQLException;
    
    /**
     * Alter storage units.
     *
     * @param databaseName database name
     * @param toBeUpdatedProps to be updated storage unit properties
     * @throws SQLException SQL exception
     */
    void alterStorageUnits(String databaseName, Map<String, DataSourcePoolProperties> toBeUpdatedProps) throws SQLException;
    
    /**
     * Unregister storage units.
     * @param databaseName database name
     * @param toBeDroppedStorageUnitNames to be dropped storage unit names
     * @throws SQLException SQL exception
     */
    void unregisterStorageUnits(String databaseName, Collection<String> toBeDroppedStorageUnitNames) throws SQLException;
    
    /**
     * Alter single rule configuration.
     *
     * @param databaseName database name
     * @param ruleMetaData rule meta data
     * @throws SQLException SQL exception
     */
    void alterSingleRuleConfiguration(String databaseName, RuleMetaData ruleMetaData) throws SQLException;
    
    /**
     * Alter rule configuration.
     *
     * @param databaseName database name
     * @param toBeAlteredRuleConfig to be altered rule config
     * @throws SQLException SQL exception
     */
    void alterRuleConfiguration(String databaseName, RuleConfiguration toBeAlteredRuleConfig) throws SQLException;
    
    /**
     * Remove rule configuration item.
     *
     * @param databaseName database name
     * @param toBeRemovedRuleConfig to be removed rule config
     * @throws SQLException SQL exception
     */
    void removeRuleConfigurationItem(String databaseName, RuleConfiguration toBeRemovedRuleConfig) throws SQLException;
    
    /**
     * Remove rule configuration.
     *
     * @param databaseName database name
     * @param ruleName rule name
     * @throws SQLException SQL exception
     */
    void removeRuleConfiguration(String databaseName, String ruleName) throws SQLException;
    
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
     * @throws SQLException SQL exception
     */
    void alterProperties(Properties props) throws SQLException;
}
