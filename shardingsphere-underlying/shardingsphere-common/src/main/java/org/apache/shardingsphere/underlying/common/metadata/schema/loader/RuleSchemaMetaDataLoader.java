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

package org.apache.shardingsphere.underlying.common.metadata.schema.loader;

import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseType;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Rule schema meta data loader.
 */
public final class RuleSchemaMetaDataLoader {
    
    private final Map<BaseRule, RuleMetaDataLoader> loaders = new LinkedHashMap<>();
    
    /**
     * Register loader.
     * 
     * @param rule rule
     * @param loader loader
     */
    public void registerLoader(final BaseRule rule, final RuleMetaDataLoader loader) {
        loaders.put(rule, loader);
    }
    
    /**
     * Load schema meta data.
     * 
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param properties configuration properties
     * @return schema meta data
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("unchecked")
    public SchemaMetaData load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final ConfigurationProperties properties) throws SQLException {
        Map<String, TableMetaData> result = new HashMap<>(); 
        for (Entry<BaseRule, RuleMetaDataLoader> entry : loaders.entrySet()) {
            result.putAll(entry.getValue().load(databaseType, dataSourceMap, entry.getKey(), properties));
        }
        // TODO load remain tables
        return new SchemaMetaData(result);
    }
    
    /**
     * Load schema meta data.
     *
     * @param databaseType database type
     * @param dataSource data source
     * @param properties configuration properties
     * @return schema meta data
     * @throws SQLException SQL exception
     */
    public SchemaMetaData load(final DatabaseType databaseType, final DataSource dataSource, final ConfigurationProperties properties) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put("ds", dataSource);
        return load(databaseType, dataSourceMap, properties);
    }
    
    /**
     * Load schema meta data.
     *
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param tableName table name
     * @param properties configuration properties
     * @return schema meta data
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("unchecked")
    public Optional<TableMetaData> load(final DatabaseType databaseType, 
                                        final Map<String, DataSource> dataSourceMap, final String tableName, final ConfigurationProperties properties) throws SQLException {
        for (Entry<BaseRule, RuleMetaDataLoader> entry : loaders.entrySet()) {
            Optional<TableMetaData> result = entry.getValue().load(databaseType, dataSourceMap, tableName, entry.getKey(), properties);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
    
    /**
     * Load schema meta data.
     *
     * @param databaseType database type
     * @param dataSource data source
     * @param tableName table name
     * @param properties configuration properties
     * @return schema meta data
     * @throws SQLException SQL exception
     */
    public Optional<TableMetaData> load(final DatabaseType databaseType,
                                        final DataSource dataSource, final String tableName, final ConfigurationProperties properties) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put("ds", dataSource);
        return load(databaseType, dataSourceMap, tableName, properties);
    }
}
