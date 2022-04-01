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
import org.apache.shardingsphere.infra.config.schema.SchemaConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRecognizer;
import org.apache.shardingsphere.infra.metadata.resource.CachedDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * ShardingSphere meta data.
 */
@RequiredArgsConstructor
@Getter
public final class ShardingSphereMetaData {
    
    private final String name;
    
    private final ShardingSphereResource resource;
    
    private final ShardingSphereRuleMetaData ruleMetaData;
    
    private final Map<String, ShardingSphereSchema> schemas;
    
    /**
     * Create ShardingSphere meta data.
     * 
     * @param databaseName database name
     * @param schemas schemas
     * @param schemaConfig schema configuration
     * @param rules rules
     * @param defaultDatabaseType default database type
     * @return ShardingSphere meta data
     * @throws SQLException SQL exception
     */
    public static ShardingSphereMetaData create(final String databaseName, final Map<String, ShardingSphereSchema> schemas, final SchemaConfiguration schemaConfig, 
                                                final Collection<ShardingSphereRule> rules, final DatabaseType defaultDatabaseType) throws SQLException {
        ShardingSphereResource resource = createResource(schemaConfig.getDataSources(), defaultDatabaseType);
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(schemaConfig.getRuleConfigurations(), rules);
        return new ShardingSphereMetaData(databaseName, resource, ruleMetaData, schemas);
    }
    
    private static ShardingSphereResource createResource(final Map<String, DataSource> dataSourceMap, final DatabaseType defaultDatabaseType) throws SQLException {
        DatabaseType databaseType = dataSourceMap.isEmpty() ? defaultDatabaseType : DatabaseTypeRecognizer.getDatabaseType(dataSourceMap.values());
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
    
    /**
     * Get default schema.
     * 
     * @return ShardingSphereSchema default schema
     */
    public ShardingSphereSchema getDefaultSchema() {
        return schemas.get(name);
    }
    
    /**
     * Get schema by name.
     * 
     * @param schemaName schema name
     * @return ShardingSphereSchema schema
     */
    public ShardingSphereSchema getSchemaByName(final String schemaName) {
        return schemas.get(schemaName);
    }
}
