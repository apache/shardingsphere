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

package org.apache.shardingsphere.infra.instance.mode;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaPOJO;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Mode context manager.
 */
public interface ModeContextManager {
    
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
     * @param alterSchemaPOJO alter schema pojo
     */
    void alterSchema(AlterSchemaPOJO alterSchemaPOJO);
    
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
     * @param toBeRegisterStorageUnitProps to be register storage unit props
     * @throws SQLException SQL exception
     */
    void registerStorageUnits(String databaseName, Map<String, DataSourceProperties> toBeRegisterStorageUnitProps) throws SQLException;
    
    /**
     * Alter storage units.
     *
     * @param databaseName database name
     * @param toBeUpdatedStorageUnitProps to be updated storage unit props
     * @throws SQLException SQL exception
     */
    void alterStorageUnits(String databaseName, Map<String, DataSourceProperties> toBeUpdatedStorageUnitProps) throws SQLException;
    
    /**
     * Unregister storage units.
     * @param databaseName database name
     * @param toBeDroppedStorageUnitNames to be dropped storage unit names
     * @throws SQLException SQL exception
     */
    void unregisterStorageUnits(String databaseName, Collection<String> toBeDroppedStorageUnitNames) throws SQLException;
    
    /**
     * Alter rule configuration.
     *
     * @param databaseName database name
     * @param ruleConfigs rule configs
     */
    void alterRuleConfiguration(String databaseName, Collection<RuleConfiguration> ruleConfigs);
    
    /**
     * Alter rule configuration.
     *
     * @param databaseName database name
     * @param toBeAlteredRuleConfig to be altered rule config
     * @return meta data versions
     */
    default Collection<MetaDataVersion> alterRuleConfiguration(String databaseName, RuleConfiguration toBeAlteredRuleConfig) {
        return Collections.emptyList();
    }
    
    /**
     * Remove rule configuration item.
     *
     * @param databaseName database name
     * @param toBeRemovedRuleConfig to be removed rule config
     */
    default void removeRuleConfigurationItem(String databaseName, RuleConfiguration toBeRemovedRuleConfig) {
    }
    
    /**
     * Remove rule configuration.
     *
     * @param databaseName database name
     * @param ruleName rule name
     */
    default void removeRuleConfiguration(String databaseName, String ruleName) {
    }
    
    /**
     * Alter global rule configuration.
     *
     * @param globalRuleConfigs global rule configs
     */
    void alterGlobalRuleConfiguration(Collection<RuleConfiguration> globalRuleConfigs);
    
    /**
     * TODO Need to DistSQL handle call it
     * Alter global rule configuration.
     *
     * @param globalRuleConfig global rule config
     */
    default void alterGlobalRuleConfiguration(RuleConfiguration globalRuleConfig) {
    }
    
    /**
     * Alter properties.
     *
     * @param props pros
     */
    void alterProperties(Properties props);
}
