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

package org.apache.shardingsphere.infra.metadata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.resource.CachedDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SystemSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.schema.loader.SchemaLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.DatabaseRulesBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
     * @param backendDatabaseType backend database type
     * @param databaseConfig database configuration
     * @param props configuration properties
     * @return database meta data
     * @throws SQLException SQL exception
     */
    public static ShardingSphereDatabase create(final String name, final DatabaseType protocolType, final DatabaseType backendDatabaseType,
                                                final DatabaseConfiguration databaseConfig, final ConfigurationProperties props) throws SQLException {
        Collection<ShardingSphereRule> databaseRules = DatabaseRulesBuilder.build(name, databaseConfig, props);
        Map<String, ShardingSphereSchema> schemas = new ConcurrentHashMap<>();
        schemas.putAll(SchemaLoader.load(name, protocolType, backendDatabaseType, databaseConfig.getDataSources(), databaseRules, props));
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
    
    private static ShardingSphereDatabase create(final String name, final DatabaseType frontendDatabaseType, final DatabaseConfiguration databaseConfig,
                                                 final Collection<ShardingSphereRule> rules, final Map<String, ShardingSphereSchema> schemas) throws SQLException {
        ShardingSphereResource resource = createResource(frontendDatabaseType, databaseConfig.getDataSources());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(databaseConfig.getRuleConfigurations(), rules);
        return new ShardingSphereDatabase(name, frontendDatabaseType, resource, ruleMetaData, schemas);
    }
    
    private static ShardingSphereResource createResource(final DatabaseType frontendDatabaseType, final Map<String, DataSource> dataSourceMap) throws SQLException {
        DatabaseType databaseType = dataSourceMap.isEmpty() ? frontendDatabaseType : DatabaseTypeEngine.getDatabaseType(dataSourceMap.values());
        DataSourcesMetaData dataSourcesMetaData = new DataSourcesMetaData(databaseType, dataSourceMap);
        CachedDatabaseMetaData cachedDatabaseMetaData = createCachedDatabaseMetaData(dataSourceMap).orElse(null);
        return new ShardingSphereResource(dataSourceMap, dataSourcesMetaData, cachedDatabaseMetaData, databaseType);
    }
    
    private static Optional<CachedDatabaseMetaData> createCachedDatabaseMetaData(final Map<String, DataSource> dataSources) throws SQLException {
        if (dataSources.isEmpty()) {
            return Optional.empty();
        }
        try (Connection connection = dataSources.values().iterator().next().getConnection()) {
            return Optional.of(new CachedDatabaseMetaData(connection.getMetaData()));
        }
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
     * Determine whether there is a data source.
     *
     * @return has datasource or not
     */
    public boolean hasDataSource() {
        return !resource.getDataSources().isEmpty();
    }
}
