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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.SystemSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShardingSphere database factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereDatabaseFactory {
    
    /**
     * Create system database.
     *
     * @param name system database name
     * @param protocolType protocol database type
     * @param props configuration properties
     * @return created system database
     */
    public static ShardingSphereDatabase create(final String name, final DatabaseType protocolType, final ConfigurationProperties props) {
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(new LinkedHashMap<>(), new LinkedList<>());
        ResourceMetaData resourceMetaData = new ResourceMetaData(databaseConfig.getDataSources(), databaseConfig.getStorageUnits());
        Map<String, ShardingSphereSchema> systemSchemas = SystemSchemaBuilder.build(name, protocolType, props);
        String defaultSchemaName = new DatabaseTypeRegistry(protocolType).getDefaultSchemaName(name);
        if (!systemSchemas.containsKey(defaultSchemaName)) {
            systemSchemas.put(defaultSchemaName, new ShardingSphereSchema(defaultSchemaName));
        }
        return new ShardingSphereDatabase(name, protocolType, resourceMetaData, new RuleMetaData(new LinkedList<>()), systemSchemas.values());
    }
    
    /**
     * Create database.
     *
     * @param name database name
     * @param protocolType database protocol type
     * @param databaseConfig database configuration
     * @param props configuration properties
     * @param computeNodeInstanceContext compute node instance context
     * @return created database
     * @throws SQLException SQL exception
     */
    public static ShardingSphereDatabase create(final String name, final DatabaseType protocolType, final DatabaseConfiguration databaseConfig,
                                                final ConfigurationProperties props, final ComputeNodeInstanceContext computeNodeInstanceContext) throws SQLException {
        ResourceMetaData resourceMetaData = new ResourceMetaData(databaseConfig.getDataSources(), databaseConfig.getStorageUnits());
        Collection<ShardingSphereRule> databaseRules = DatabaseRulesBuilder.build(name, protocolType, databaseConfig, computeNodeInstanceContext, resourceMetaData);
        Map<String, ShardingSphereSchema> schemas = new ConcurrentHashMap<>(GenericSchemaBuilder.build(protocolType,
                new GenericSchemaBuilderMaterial(resourceMetaData.getStorageUnits(), databaseRules, props, new DatabaseTypeRegistry(protocolType).getDefaultSchemaName(name))));
        SystemSchemaBuilder.build(name, protocolType, props).forEach(schemas::putIfAbsent);
        return new ShardingSphereDatabase(name, protocolType, resourceMetaData, new RuleMetaData(databaseRules), schemas.values());
    }
    
    /**
     * Create database.
     *
     * @param name database name
     * @param protocolType database protocol type
     * @param databaseConfig database configuration
     * @param computeNodeInstanceContext compute node instance context
     * @param schemas schemas
     * @return created database
     */
    public static ShardingSphereDatabase create(final String name, final DatabaseType protocolType, final DatabaseConfiguration databaseConfig,
                                                final ComputeNodeInstanceContext computeNodeInstanceContext, final Collection<ShardingSphereSchema> schemas) {
        ResourceMetaData resourceMetaData = new ResourceMetaData(databaseConfig.getDataSources(), databaseConfig.getStorageUnits());
        Collection<ShardingSphereRule> rules = DatabaseRulesBuilder.build(name, protocolType, databaseConfig, computeNodeInstanceContext, resourceMetaData);
        return new ShardingSphereDatabase(name, protocolType, resourceMetaData, new RuleMetaData(rules), schemas);
    }
    
    /**
     * Create database without system schema.
     *
     * @param name database name
     * @param protocolType database protocol type
     * @param databaseConfig database configuration
     * @param props configuration properties
     * @param computeNodeInstanceContext compute node instance context
     * @return created database
     * @throws SQLException SQL exception
     */
    public static ShardingSphereDatabase createWithoutSystemSchema(final String name, final DatabaseType protocolType, final DatabaseConfiguration databaseConfig,
                                                final ConfigurationProperties props, final ComputeNodeInstanceContext computeNodeInstanceContext) throws SQLException {
        ResourceMetaData resourceMetaData = new ResourceMetaData(databaseConfig.getDataSources(), databaseConfig.getStorageUnits());
        Collection<ShardingSphereRule> databaseRules = DatabaseRulesBuilder.build(name, protocolType, databaseConfig, computeNodeInstanceContext, resourceMetaData);
        Map<String, ShardingSphereSchema> schemas = new ConcurrentHashMap<>(GenericSchemaBuilder.build(protocolType,
                new GenericSchemaBuilderMaterial(resourceMetaData.getStorageUnits(), databaseRules, props, new DatabaseTypeRegistry(protocolType).getDefaultSchemaName(name))));
        return new ShardingSphereDatabase(name, protocolType, resourceMetaData, new RuleMetaData(databaseRules), schemas.values());
    }
}
