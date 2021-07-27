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
public class SchemaBuilderWithDialectLoader {

    /**
     * find Dialect-Loader.
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
     * build table meta data with Dialect-Loader.
     * @param dialectLoader dialectLoader
     * @param executorService executorService
     * @param materials schema builder materials
     * @param logicTable2DataNodes Map of logicTable to DataNodes
     * @return meta data map
     * @throws SQLException SQL exception
     */
    public static Map<String, TableMetaData> build(final DialectTableMetaDataLoader dialectLoader, final ExecutorService executorService,
            final SchemaBuilderMaterials materials, final Map<String, Collection<DataNode>> logicTable2DataNodes) throws SQLException {

        Collection<Future<Map<String, TableMetaData>>> futures = new LinkedList<>();
        Map<String, DataNode> logicTable2FirstDataNodeMap = logicTable2DataNodes.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().iterator().next()));
        Map<String, Collection<String>> datasourceExcludeTablesMap = getDatasourceExcludeTablesMap(logicTable2DataNodes, logicTable2FirstDataNodeMap);
        for (Map.Entry<String, DataSource> each : materials.getDataSourceMap().entrySet()) {
            Collection<String> excludeTables = datasourceExcludeTablesMap.getOrDefault(each.getKey(), Collections.emptyList());
            futures.add(executorService.submit(() -> dialectLoader.load(each.getValue(), excludeTables)));
        }

        Map<String, TableMetaData> result = new HashMap<>(materials.getRules().size(), 1);
        for (Future<Map<String, TableMetaData>> each : futures) {
            try {
                putTables4Futures(each.get(), logicTable2FirstDataNodeMap, result);
            } catch (final InterruptedException | ExecutionException ex) {
                if (ex.getCause() instanceof SQLException) {
                    throw (SQLException) ex.getCause();
                }
                throw new ShardingSphereException(ex);
            }
        }
        return result;
    }

    private static Map<String, Collection<String>> getDatasourceExcludeTablesMap(final Map<String, Collection<DataNode>> logicTable2DataNodesMap,
            final Map<String, DataNode> logicTable2FirstDataNodeMap) {
        return logicTable2DataNodesMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .filter(eachDataNode -> !logicTable2FirstDataNodeMap.containsValue(eachDataNode))
                .collect(Collectors.groupingBy(DataNode::getDataSourceName, Collectors.mapping(DataNode::getTableName, Collectors.toCollection(
                        LinkedHashSet::new))));
    }

    private static void putTables4Futures(final Map<String, TableMetaData> tableMetaMap, final Map<String, DataNode> logicTable2ChoiceDataNodeMap,
            final Map<String, TableMetaData> tables) {
        Map<String, String> choiceActual2LogicTableMap = logicTable2ChoiceDataNodeMap.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getValue().getTableName(), Map.Entry::getKey));
        for (Map.Entry<String, TableMetaData> entry : tableMetaMap.entrySet()) {
            String actualTableName = entry.getKey();
            String logicTableName = choiceActual2LogicTableMap.get(actualTableName);
            if (null != logicTableName && !logicTableName.equals(actualTableName)) {
                if (!tables.containsKey(logicTableName)) {
                    TableMetaData toCopy = entry.getValue();
                    tables.put(logicTableName, new TableMetaData(logicTableName, toCopy.getColumns().values(), toCopy.getIndexes().values()));
                }
            } else {
                tables.put(actualTableName, entry.getValue());
            }
        }
    }
}
