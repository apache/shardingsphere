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

package org.apache.shardingsphere.infra.metadata.schema.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.ColumnMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.SchemaMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.adapter.MetaDataLoaderConnectionAdapter;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.type.TableContainedRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Schema builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaBuilder {
    
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    
    static {
        ShardingSphereServiceLoader.register(DialectTableMetaDataLoader.class);
    }
    
    /**
     * Build ShardingSphere schema.
     * 
     * @param materials schema builder materials
     * @return ShardingSphere schema
     * @throws SQLException SQL exception
     */
    public static ShardingSphereSchema build(final SchemaBuilderMaterials materials) throws SQLException {
        ShardingSphereSchema result = new ShardingSphereSchema();
        addRuleConfiguredTables(materials, result);
        appendRemainTables(materials, result);
        return result;
    }
    
    private static void addRuleConfiguredTables(final SchemaBuilderMaterials materials, final ShardingSphereSchema schema) throws SQLException {
        for (ShardingSphereRule rule : materials.getRules()) {
            if (rule instanceof TableContainedRule) {
                for (String table : ((TableContainedRule) rule).getTables()) {
                    if (!schema.containsTable(table)) {
                        TableMetaDataBuilder.build(table, materials).ifPresent(optional -> schema.put(table, optional));
                    }
                }
            }
        }
    }
    
    private static void appendRemainTables(final SchemaBuilderMaterials materials, final ShardingSphereSchema schema) throws SQLException {
        Optional<DialectTableMetaDataLoader> dialectLoader = findDialectTableMetaDataLoader(materials);
        if (dialectLoader.isPresent()) {
            appendDialectRemainTables(dialectLoader.get(), materials, schema);
            return;
        }
        appendDefaultRemainTables(materials, schema);
    }
    
    private static Optional<DialectTableMetaDataLoader> findDialectTableMetaDataLoader(final SchemaBuilderMaterials materials) {
        for (DialectTableMetaDataLoader each : ShardingSphereServiceLoader.getSingletonServiceInstances(DialectTableMetaDataLoader.class)) {
            if (each.getDatabaseType().equals(materials.getDatabaseType().getName())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private static void appendDialectRemainTables(final DialectTableMetaDataLoader dialectLoader, final SchemaBuilderMaterials materials, final ShardingSphereSchema schema) throws SQLException {
        Collection<Future<Map<String, TableMetaData>>> futures = new LinkedList<>();
        Collection<String> existedTables = getExistedTables(materials.getRules(), schema);
        for (DataSource each : materials.getDataSourceMap().values()) {
            futures.add(EXECUTOR_SERVICE.submit(() -> dialectLoader.load(each, existedTables)));
        }
        for (Future<Map<String, TableMetaData>> each : futures) {
            try {
                schema.putAll(each.get());
            } catch (final InterruptedException | ExecutionException ex) {
                if (ex.getCause() instanceof SQLException) {
                    throw (SQLException) ex.getCause();
                }
                throw new ShardingSphereException(ex);
            }
        }
    }
    
    private static void appendDefaultRemainTables(final SchemaBuilderMaterials materials, final ShardingSphereSchema schema) throws SQLException {
        Collection<String> existedTableNames = getExistedTables(materials.getRules(), schema);
        for (Entry<String, DataSource> entry : materials.getDataSourceMap().entrySet()) {
            Collection<String> tableNames = SchemaMetaDataLoader.loadAllTableNames(entry.getValue(), materials.getDatabaseType());
            tableNames.removeAll(existedTableNames);
            for (String each : tableNames) {
                schema.put(each, loadTableMetaData(each, entry.getValue(), materials.getDatabaseType()));
            }
        }
    }
    
    private static TableMetaData loadTableMetaData(final String tableName, final DataSource dataSource, final DatabaseType databaseType) throws SQLException {
        TableMetaData result = new TableMetaData();
        try (Connection connection = new MetaDataLoaderConnectionAdapter(databaseType, dataSource.getConnection())) {
            result.getColumns().putAll(loadColumnMetaDataMap(tableName, databaseType, connection));
        }
        return result;
    }
    
    private static Map<String, ColumnMetaData> loadColumnMetaDataMap(final String tableName, final DatabaseType databaseType, final Connection connection) throws SQLException {
        return ColumnMetaDataLoader.load(connection, tableName, databaseType).stream().collect(Collectors.toMap(ColumnMetaData::getName, each -> each, (a, b) -> b, LinkedHashMap::new));
    }
    
    private static Collection<String> getExistedTables(final Collection<ShardingSphereRule> rules, final ShardingSphereSchema schema) {
        Collection<String> result = new LinkedHashSet<>();
        for (ShardingSphereRule each : rules) {
            if (each instanceof DataNodeContainedRule) {
                result.addAll(((DataNodeContainedRule) each).getAllActualTables());
            }
        }
        result.addAll(schema.getAllTableNames());
        return result;
    }
}
