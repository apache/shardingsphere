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

package org.apache.shardingsphere.mode.persist.service.divided;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaPOJO;

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
     * @throws SQLException SQL exception
     */
    void createDatabase(String databaseName) throws SQLException;
    
    /**
     * Drop database.
     *
     * @param databaseName database name
     * @throws SQLException SQL exception
     */
    void dropDatabase(String databaseName) throws SQLException;
    
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
     * @param alterSchemaPOJO alter schema pojo
     * @throws SQLException SQL exception
     */
    void alterSchema(AlterSchemaPOJO alterSchemaPOJO) throws SQLException;
    
    /**
     * Drop schema.
     *
     * @param databaseName database name
     * @param schemaNames schema names
     */
    void dropSchema(String databaseName, Collection<String> schemaNames);
    
    /**
     * Alter schema metadata.
     *
     * @param alterSchemaMetaDataPOJO alter schema metadata pojo
     */
    void alterSchemaMetaData(AlterSchemaMetaDataPOJO alterSchemaMetaDataPOJO);
    
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
     * @param ruleConfigs rule configs
     * @throws SQLException SQL exception
     */
    void alterSingleRuleConfiguration(String databaseName, Collection<RuleConfiguration> ruleConfigs) throws SQLException;
    
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
     * @throws SQLException SQL exception
     */
    void alterGlobalRuleConfiguration(RuleConfiguration globalRuleConfig) throws SQLException;
    
    /**
     * Alter properties.
     *
     * @param props pros
     * @throws SQLException SQL exception
     */
    void alterProperties(Properties props) throws SQLException;
    
    /**
     * Create table.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param table table
     * @param logicDataSourceName logic data source name
     */
    void createTable(String databaseName, String schemaName, ShardingSphereTable table, String logicDataSourceName);
    
    /**
     * Drop tables.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableNames table names
     */
    void dropTables(String databaseName, String schemaName, Collection<String> tableNames);
}
