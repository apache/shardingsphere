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
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.ShardingSphereBuiltInSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.SystemSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShardingSphere database.
 */
@Getter
public final class ShardingSphereDatabase {
    
    private final String name;
    
    private final DatabaseType protocolType;
    
    private final ShardingSphereResource resource;
    
    private final ShardingSphereRuleMetaData ruleMetaData;
    
    private final Map<String, ShardingSphereSchema> schemas;
    
    public ShardingSphereDatabase(final String name, final DatabaseType protocolType, final ShardingSphereResource resource,
                                  final ShardingSphereRuleMetaData ruleMetaData, final Map<String, ShardingSphereSchema> schemas) {
        this.name = name;
        this.protocolType = protocolType;
        this.resource = resource;
        this.ruleMetaData = ruleMetaData;
        this.schemas = new ConcurrentHashMap<>(schemas.size(), 1);
        schemas.forEach((key, value) -> this.schemas.put(key.toLowerCase(), value));
    }
    
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
     */
    public static ShardingSphereDatabase create(final String name, final DatabaseType protocolType) {
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(new LinkedHashMap<>(), new LinkedList<>());
        return create(name, protocolType, databaseConfig, new LinkedList<>(), SystemSchemaBuilder.build(name, protocolType));
    }
    
    private static ShardingSphereDatabase create(final String name, final DatabaseType protocolType, final DatabaseConfiguration databaseConfig,
                                                 final Collection<ShardingSphereRule> rules, final Map<String, ShardingSphereSchema> schemas) {
        ShardingSphereResource resource = createResource(name, databaseConfig.getDataSources());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(rules);
        return new ShardingSphereDatabase(name, protocolType, resource, ruleMetaData, schemas);
    }
    
    private static ShardingSphereResource createResource(final String databaseName, final Map<String, DataSource> dataSourceMap) {
        return new ShardingSphereResource(databaseName, dataSourceMap);
    }
    
    /**
     * Create built in database.
     * 
     * @param databaseName database name
     * @param protocolType protocol type
     * @return sharding sphere database
     */
    public static ShardingSphereDatabase createBuiltInShardingSphereDatabase(final String databaseName, final DatabaseType protocolType) {
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(new LinkedHashMap<>(), new LinkedList<>());
        return create(databaseName, protocolType, databaseConfig, new LinkedList<>(), ShardingSphereBuiltInSchemaBuilder.build(protocolType));
    }
    
    /**
     * Get schema.
     *
     * @param schemaName schema name
     * @return schema
     */
    public ShardingSphereSchema getSchema(final String schemaName) {
        return schemas.get(schemaName.toLowerCase());
    }
    
    /**
     * Put schema.
     *
     * @param schemaName schema name
     * @param schema schema
     */
    public void putSchema(final String schemaName, final ShardingSphereSchema schema) {
        schemas.put(schemaName.toLowerCase(), schema);
    }
    
    /**
     * Remove schema.
     *
     * @param schemaName schema name
     */
    public void removeSchema(final String schemaName) {
        schemas.remove(schemaName.toLowerCase());
    }
    
    /**
     * Judge contains schema from database or not.
     *
     * @param schemaName schema name
     * @return contains schema from database or not
     */
    public boolean containsSchema(final String schemaName) {
        return schemas.containsKey(schemaName.toLowerCase());
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
     * @param ruleClass to be reloaded rule class
     */
    public synchronized void reloadRules(final Class<? extends ShardingSphereRule> ruleClass) {
        Collection<? extends ShardingSphereRule> toBeReloadedRules = ruleMetaData.findRules(ruleClass);
        RuleConfiguration config = toBeReloadedRules.stream().map(ShardingSphereRule::getConfiguration).findFirst().orElse(null);
        toBeReloadedRules.stream().findFirst().ifPresent(optional -> {
            ruleMetaData.getRules().removeAll(toBeReloadedRules);
            ruleMetaData.getRules().add(((MutableDataNodeRule) optional).reloadRule(config, name, resource.getDataSources(), ruleMetaData.getRules()));
        });
    }
}
