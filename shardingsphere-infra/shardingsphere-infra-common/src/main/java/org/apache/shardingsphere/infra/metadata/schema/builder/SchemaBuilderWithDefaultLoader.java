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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.ColumnMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.SchemaMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.adapter.MetaDataLoaderConnectionAdapter;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchemaBuilderWithDefaultLoader {

    /**
     * build table meta data with default loader.
     * @param executorService executorService
     * @param materials schema builder materials
     * @param logicTable2DataNodes Map of logicTable to DataNodes
     * @return meta data map
     * @throws SQLException SQL exception
     */
    public static Map<String, TableMetaData> build(final ExecutorService executorService, final SchemaBuilderMaterials materials,
            final Map<String, Collection<DataNode>> logicTable2DataNodes) throws SQLException {
        Map<String, TableMetaData> result = new HashMap<>(materials.getRules().size(), 1);
        result.putAll(appendActualTables(executorService, materials, logicTable2DataNodes));
        result.putAll(appendRuleLogicTables(executorService, materials, logicTable2DataNodes));
        return result;
    }

    private static Map<String, TableMetaData> appendActualTables(final ExecutorService executorService, final SchemaBuilderMaterials materials,
            final Map<String, Collection<DataNode>> logicTable2DataNodes) throws SQLException {
        Collection<String> existedTableNames = logicTable2DataNodes.values().stream()
                .flatMap(each -> each.stream().map(DataNode::getTableName))
                .collect(Collectors.toSet());

        Map<String, TableMetaData> result = new HashMap<>(materials.getRules().size());
        for (Map.Entry<String, DataSource> entry : materials.getDataSourceMap().entrySet()) {
            Collection<String> tableNames = SchemaMetaDataLoader.loadAllTableNames(entry.getValue(), materials.getDatabaseType());
            tableNames.removeAll(existedTableNames);

            Collection<Future<TableMetaData>> futures = new ArrayList<>(tableNames.size());
            for (String each : tableNames) {
                futures.add(executorService.submit(() -> loadTableMetaData(each, entry.getValue(), materials.getDatabaseType())));
            }
            for (Future<TableMetaData> each : futures) {
                try {
                    TableMetaData tableMetaData = each.get();
                    result.put(tableMetaData.getName(), tableMetaData);
                } catch (final InterruptedException | ExecutionException ex) {
                    if (ex.getCause() instanceof SQLException) {
                        throw (SQLException) ex.getCause();
                    }
                    throw new ShardingSphereException(ex);
                }
            }
        }
        return result;
    }

    private static TableMetaData loadTableMetaData(final String tableName, final DataSource dataSource, final DatabaseType databaseType) throws SQLException {
        TableMetaData result = new TableMetaData(tableName);
        try (Connection connection = new MetaDataLoaderConnectionAdapter(databaseType, dataSource.getConnection())) {
            result.getColumns().putAll(loadColumnMetaDataMap(tableName, databaseType, connection));
        }
        return result;
    }

    private static Map<String, ColumnMetaData> loadColumnMetaDataMap(final String tableName, final DatabaseType databaseType, final Connection connection) throws SQLException {
        return ColumnMetaDataLoader.load(connection, tableName, databaseType).stream()
                .collect(Collectors.toMap(ColumnMetaData::getName, each -> each, (a, b) -> b, LinkedHashMap::new));
    }

    private static Map<String, TableMetaData> appendRuleLogicTables(final ExecutorService executorService, final SchemaBuilderMaterials materials,
            final Map<String, Collection<DataNode>> logicTable2DataNodes) throws SQLException {
        Collection<String> toLoadTables = logicTable2DataNodes.keySet().stream().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(toLoadTables)) {
            return Collections.emptyMap();
        }
        Collection<Future<Optional<TableMetaData>>> futures = new ArrayList<>(toLoadTables.size());
        for (String table : toLoadTables) {
            futures.add(executorService.submit(() -> TableMetaDataBuilder.load(table, materials)));
        }
        final Map<String, TableMetaData> result = new HashMap<>(futures.size(), 1);
        for (Future<Optional<TableMetaData>> each : futures) {
            try {
                each.get().map(optional -> result.put(optional.getName(), optional));
            } catch (final InterruptedException | ExecutionException ex) {
                if (ex.getCause() instanceof SQLException) {
                    throw (SQLException) ex.getCause();
                }
                throw new ShardingSphereException(ex);
            }
        }
        return result;
    }
}
