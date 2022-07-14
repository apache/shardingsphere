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

package org.apache.shardingsphere.infra.metadata.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.SystemSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ShardingSphere database.
 */
@RequiredArgsConstructor
@Getter
public final class ShardingSphereDatabase {
    
    private final String name;
    
    private final DatabaseType protocolType;
    
    private final ShardingSphereResource resource;
    
    private final ShardingSphereRuleMetaData ruleMetaData;
    
    private final Map<String, ShardingSphereSchema> schemas;
    
    /**
     * Create database meta data.
     * 
     * @param name database name
     * @param protocolType database protocol type
     * @param storageType storage type
     * @param databaseConfig database configuration
     * @param props configuration properties
     * @param instanceContext instance context
     * @return database meta data
     * @throws SQLException SQL exception
     */
    public static ShardingSphereDatabase create(final String name, final DatabaseType protocolType, final DatabaseType storageType,
                                                final DatabaseConfiguration databaseConfig, final ConfigurationProperties props, final InstanceContext instanceContext) throws SQLException {
        Collection<ShardingSphereRule> databaseRules = DatabaseRulesBuilder.build(name, databaseConfig, instanceContext);
        Map<String, ShardingSphereSchema> schemas = new ConcurrentHashMap<>();
        schemas.putAll(GenericSchemaBuilder.build(new GenericSchemaBuilderMaterials(protocolType, storageType, databaseConfig.getDataSources(), databaseRules, props,
                DatabaseTypeEngine.getDefaultSchemaName(storageType, name))));
        schemas.putAll(SystemSchemaBuilder.build(name, protocolType));
        return create(name, protocolType, databaseConfig, databaseRules, schemas);
    }
    
    /**
     * Create system database meta data.
     * 
     * @param name system database name
     * @param protocolType protocol database type
     * @return system database meta data
     * @throws SQLException SQL exception
     */
    public static ShardingSphereDatabase create(final String name, final DatabaseType protocolType) throws SQLException {
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(new LinkedHashMap<>(), new LinkedList<>());
        return create(name, protocolType, databaseConfig, new LinkedList<>(), SystemSchemaBuilder.build(name, protocolType));
    }
    
    private static ShardingSphereDatabase create(final String name, final DatabaseType protocolType, final DatabaseConfiguration databaseConfig,
                                                 final Collection<ShardingSphereRule> rules, final Map<String, ShardingSphereSchema> schemas) {
        ShardingSphereResource resource = createResource(databaseConfig.getDataSources());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(rules);
        return new ShardingSphereDatabase(name, protocolType, resource, ruleMetaData, schemas);
    }
    
    private static ShardingSphereResource createResource(final Map<String, DataSource> dataSourceMap) {
        return new ShardingSphereResource(dataSourceMap);
    }
    
    /**
     * Judge whether is completed.
     *
     * @return is completed or not
     */
    public boolean isComplete() {
        return !ruleMetaData.getRules().isEmpty() && !resource.getDataSources().isEmpty();
    }
    
    /**
     * Judge whether contains data source.
     *
     * @return contains data source or not
     */
    public boolean containsDataSource() {
        return !resource.getDataSources().isEmpty();
    }
    
    /**
     * Reload rules.
     * 
     * @param instanceContext instance context
     */
    public synchronized void reloadRules(final InstanceContext instanceContext) {
        Collection<ShardingSphereRule> databaseRules = DatabaseRulesBuilder.build(
                name, new DataSourceProvidedDatabaseConfiguration(resource.getDataSources(), ruleMetaData.getConfigurations()), instanceContext);
        ruleMetaData.getRules().clear();
        ruleMetaData.getRules().addAll(databaseRules);
    }
    
    /**
     * Reload rules.
     *
     * @param ruleClass to be reloaded rule class
     * @param instanceContext instance context
     */
    public synchronized void reloadRules(final Class<? extends ShardingSphereRule> ruleClass, final InstanceContext instanceContext) {
        Collection<? extends ShardingSphereRule> toBeReloadedRules = ruleMetaData.findRules(ruleClass);
        Collection<RuleConfiguration> toBeReloadedRuleConfigs = toBeReloadedRules.stream().map(ShardingSphereRule::getConfiguration).collect(Collectors.toList());
        Collection<ShardingSphereRule> reloadedDatabaseRules = DatabaseRulesBuilder.build(
                name, new DataSourceProvidedDatabaseConfiguration(resource.getDataSources(), toBeReloadedRuleConfigs), instanceContext);
        ruleMetaData.getRules().removeAll(toBeReloadedRules);
        ruleMetaData.getRules().addAll(reloadedDatabaseRules);
    }
}
