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
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.StorageResource;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.SystemSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.infra.state.datasource.DataSourceStateManager;

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
    
    private final ResourceMetaData resourceMetaData;
    
    private final RuleMetaData ruleMetaData;
    
    private final Map<String, ShardingSphereSchema> schemas;
    
    public ShardingSphereDatabase(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                  final RuleMetaData ruleMetaData, final Map<String, ShardingSphereSchema> schemas) {
        this.name = name;
        this.protocolType = protocolType;
        this.resourceMetaData = resourceMetaData;
        this.ruleMetaData = ruleMetaData;
        this.schemas = new ConcurrentHashMap<>(schemas.size(), 1F);
        schemas.forEach((key, value) -> this.schemas.put(key.toLowerCase(), value));
    }
    
    /**
     * Create database meta data.
     * 
     * @param name database name
     * @param protocolType database protocol type
     * @param storageTypes storage types
     * @param databaseConfig database configuration
     * @param props configuration properties
     * @param instanceContext instance context
     * @return database meta data
     * @throws SQLException SQL exception
     */
    public static ShardingSphereDatabase create(final String name, final DatabaseType protocolType, final Map<String, DatabaseType> storageTypes,
                                                final DatabaseConfiguration databaseConfig, final ConfigurationProperties props, final InstanceContext instanceContext) throws SQLException {
        Collection<ShardingSphereRule> databaseRules = DatabaseRulesBuilder.build(name, databaseConfig, instanceContext);
        Map<String, ShardingSphereSchema> schemas = new ConcurrentHashMap<>(GenericSchemaBuilder
                .build(new GenericSchemaBuilderMaterial(protocolType, storageTypes, DataSourceStateManager.getInstance().getEnabledDataSources(name, databaseConfig.getDataSources()), databaseRules,
                        props, new DatabaseTypeRegistry(protocolType).getDefaultSchemaName(name))));
        SystemSchemaBuilder.build(name, protocolType, props).forEach(schemas::putIfAbsent);
        return create(name, protocolType, databaseConfig, databaseRules, schemas);
    }
    
    /**
     * Create system database meta data.
     * 
     * @param name system database name
     * @param protocolType protocol database type
     * @param props configuration properties
     * @return system database meta data
     */
    public static ShardingSphereDatabase create(final String name, final DatabaseType protocolType, final ConfigurationProperties props) {
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(new LinkedHashMap<>(), new LinkedList<>());
        return create(name, protocolType, databaseConfig, new LinkedList<>(), SystemSchemaBuilder.build(name, protocolType, props));
    }
    
    /**
     * Create database meta data.
     *
     * @param name database name
     * @param protocolType database protocol type
     * @param databaseConfig database configuration
     * @param rules rules
     * @param schemas schemas
     * @return database meta data
     */
    public static ShardingSphereDatabase create(final String name, final DatabaseType protocolType, final DatabaseConfiguration databaseConfig,
                                                final Collection<ShardingSphereRule> rules, final Map<String, ShardingSphereSchema> schemas) {
        ResourceMetaData resourceMetaData = createResourceMetaData(name, databaseConfig.getStorageResource(), databaseConfig.getDataSourcePoolPropertiesMap());
        RuleMetaData ruleMetaData = new RuleMetaData(rules);
        return new ShardingSphereDatabase(name, protocolType, resourceMetaData, ruleMetaData, schemas);
    }
    
    private static ResourceMetaData createResourceMetaData(final String databaseName, final StorageResource storageResource, final Map<String, DataSourcePoolProperties> propsMap) {
        return new ResourceMetaData(databaseName, storageResource.getDataSources(), storageResource.getStorageUnitNodeMap(), propsMap);
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
     * Get schema.
     *
     * @param schemaName schema name
     * @return schema
     */
    public ShardingSphereSchema getSchema(final String schemaName) {
        return schemas.get(schemaName.toLowerCase());
    }
    
    /**
     * Add schema.
     *
     * @param schemaName schema name
     * @param schema schema
     */
    public void addSchema(final String schemaName, final ShardingSphereSchema schema) {
        schemas.put(schemaName.toLowerCase(), schema);
    }
    
    /**
     * Drop schema.
     *
     * @param schemaName schema name
     */
    public void dropSchema(final String schemaName) {
        schemas.remove(schemaName.toLowerCase());
    }
    
    /**
     * Judge whether is completed.
     *
     * @return is completed or not
     */
    public boolean isComplete() {
        return !ruleMetaData.getRules().isEmpty() && !resourceMetaData.getStorageUnitMetaData().getStorageUnits().isEmpty();
    }
    
    /**
     * Judge whether contains data source.
     *
     * @return contains data source or not
     */
    public boolean containsDataSource() {
        return !resourceMetaData.getStorageUnitMetaData().getStorageUnits().isEmpty();
    }
    
    /**
     * Reload rules.
     *
     * @param ruleClass to be reloaded rule class
     */
    public synchronized void reloadRules(final Class<? extends ShardingSphereRule> ruleClass) {
        Collection<? extends ShardingSphereRule> toBeReloadedRules = ruleMetaData.findRules(ruleClass);
        RuleConfiguration ruleConfig = toBeReloadedRules.stream().map(ShardingSphereRule::getConfiguration).findFirst().orElse(null);
        Collection<ShardingSphereRule> databaseRules = new LinkedList<>(ruleMetaData.getRules());
        toBeReloadedRules.stream().findFirst().ifPresent(optional -> {
            databaseRules.removeAll(toBeReloadedRules);
            databaseRules.add(((MutableDataNodeRule) optional).reloadRule(ruleConfig, name, resourceMetaData.getStorageUnitMetaData().getDataSources(), databaseRules));
        });
        ruleMetaData.getRules().clear();
        ruleMetaData.getRules().addAll(databaseRules);
    }
}
