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

package org.apache.shardingsphere.infra.metadata.database.loader;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SystemSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.schema.loader.SchemaLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database loader.
 */
public final class DatabaseLoader {
    
    /**
     * Load database.
     * 
     * @param databaseName database name
     * @param frontendDatabaseType frontend database type
     * @param backendDatabaseType backend database type
     * @param dataSourceMap data source map
     * @param rules rules
     * @param props configuration properties
     * @return loaded database
     * @throws SQLException SQL exception
     */
    public static ShardingSphereDatabaseMetaData load(final String databaseName, final DatabaseType frontendDatabaseType,
                                                      final DatabaseType backendDatabaseType, final Map<String, DataSource> dataSourceMap,
                                                      final Collection<ShardingSphereRule> rules, final ConfigurationProperties props) throws SQLException {
        Map<String, ShardingSphereSchema> schemas = new ConcurrentHashMap<>();
        schemas.putAll(SchemaLoader.load(databaseName, frontendDatabaseType, backendDatabaseType, dataSourceMap, rules, props));
        schemas.putAll(SystemSchemaBuilder.build(databaseName, frontendDatabaseType));
        return new ShardingSphereDatabaseMetaData(schemas);
    }
    
    /**
     * Load database.
     * 
     * @param databaseName database name
     * @param frontendDatabaseType frontend database type
     * @return loaded database
     */
    public static ShardingSphereDatabaseMetaData load(final String databaseName, final DatabaseType frontendDatabaseType) {
        return new ShardingSphereDatabaseMetaData(SystemSchemaBuilder.build(databaseName, frontendDatabaseType));
    }
}
