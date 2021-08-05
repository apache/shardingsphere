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
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaBuilderWithDialectLoader {
    
    /**
     * Find Dialect-Loader.
     * @param materials schema builder materials
     * @return Dialect-Loader or Empty
     */
    public static Optional<DialectTableMetaDataLoader> findDialectTableMetaDataLoader(final SchemaBuilderMaterials materials) {
        for (DialectTableMetaDataLoader each : ShardingSphereServiceLoader.getSingletonServiceInstances(DialectTableMetaDataLoader.class)) {
            if (each.getDatabaseType().equals(materials.getDatabaseType().getName())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Build table metadata with Dialect-Loader.
     * @param dialectLoader dialectLoader
     * @param executorService executorService
     * @param materials schema builder materials
     * @param logicTableDataNodesMap map of logicTable to dataNodes
     * @return metadata map
     * @throws SQLException SQL exception
     */
    public static Map<String, TableMetaData> build(final DialectTableMetaDataLoader dialectLoader, final ExecutorService executorService,
            final SchemaBuilderMaterials materials, final Map<String, Collection<DataNode>> logicTableDataNodesMap) throws SQLException {

        Collection<Future<Map<String, TableMetaData>>> futures = new LinkedList<>();
        Map<String, DataNode> logicTableFirstDataNodeMap = logicTableDataNodesMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().iterator().next()));
        Map<String, Collection<String>> datasourceExcludeTablesMap = getDatasourceExcludeTablesMap(logicTableDataNodesMap, logicTableFirstDataNodeMap);
        for (Map.Entry<String, DataSource> each : materials.getDataSourceMap().entrySet()) {
            Collection<String> excludeTables = datasourceExcludeTablesMap.getOrDefault(each.getKey(), Collections.emptyList());
            futures.add(executorService.submit(() -> dialectLoader.load(each.getValue(), excludeTables)));
        }

        Map<String, TableMetaData> result = new HashMap<>(materials.getRules().size(), 1);
        for (Future<Map<String, TableMetaData>> each : futures) {
            try {
                putTablesForFutures(each.get(), logicTableFirstDataNodeMap, result);
            } catch (final InterruptedException | ExecutionException ex) {
                if (ex.getCause() instanceof SQLException) {
                    throw (SQLException) ex.getCause();
                }
                throw new ShardingSphereException(ex);
            }
        }
        return result;
    }
    
    private static Map<String, Collection<String>> getDatasourceExcludeTablesMap(final Map<String, Collection<DataNode>> logicTableDataNodesMap,
            final Map<String, DataNode> logicTableFirstDataNodeMap) {
        return logicTableDataNodesMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .filter(eachDataNode -> !logicTableFirstDataNodeMap.containsValue(eachDataNode))
                .collect(Collectors.groupingBy(DataNode::getDataSourceName, Collectors.mapping(DataNode::getTableName, Collectors.toCollection(
                        LinkedHashSet::new))));
    }
    
    private static void putTablesForFutures(final Map<String, TableMetaData> tableMetaMap, final Map<String, DataNode> logicTableFirstDataNodeMap,
            final Map<String, TableMetaData> tables) {
        Map<String, String> firstActualLogicTableMap = logicTableFirstDataNodeMap.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getValue().getTableName(), Map.Entry::getKey));
        for (Map.Entry<String, TableMetaData> entry : tableMetaMap.entrySet()) {
            String actualTableName = entry.getKey();
            String logicTableName = firstActualLogicTableMap.get(actualTableName);
            if (null != logicTableName && !logicTableName.equals(actualTableName)) {
                if (!tables.containsKey(logicTableName)) {
                    TableMetaData copyTableMetaData = entry.getValue();
                    tables.put(logicTableName, new TableMetaData(logicTableName, copyTableMetaData.getColumns().values(), copyTableMetaData.getIndexes().values()));
                }
            } else {
                tables.put(actualTableName, entry.getValue());
            }
        }
    }
}
