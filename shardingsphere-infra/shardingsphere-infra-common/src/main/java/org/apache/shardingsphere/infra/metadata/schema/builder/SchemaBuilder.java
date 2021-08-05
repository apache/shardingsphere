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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * Schema builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaBuilder {
    
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 2,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingSphere-SchemaBuilder-%d").build());
    
    static {
        ShardingSphereServiceLoader.register(DialectTableMetaDataLoader.class);
    }
    
    /**
     * Build actual and logic table metadata.
     *
     * @param materials schema builder materials
     * @return actual and logic table metadata
     * @throws SQLException SQL exception
     */
    public static Map<TableMetaData, TableMetaData> build(final SchemaBuilderMaterials materials) throws SQLException {
        Map<String, TableMetaData> actualTableMetaMap = buildActualTableMetaDataMap(materials);
        Map<String, TableMetaData> logicTableMetaMap = buildLogicTableMetaDataMap(materials, actualTableMetaMap);
        Map<TableMetaData, TableMetaData> tableMetaDataMap = new HashMap<>(actualTableMetaMap.size(), 1);
        for (Map.Entry<String, TableMetaData> entry : actualTableMetaMap.entrySet()) {
            tableMetaDataMap.put(entry.getValue(), logicTableMetaMap.getOrDefault(entry.getKey(), entry.getValue()));
        }
        return tableMetaDataMap;
    }
    
    private static Map<String, TableMetaData> buildActualTableMetaDataMap(final SchemaBuilderMaterials materials) throws SQLException {
        Map<String, Collection<DataNode>> logicTableDataNodesMap = getLogicTableDataNodesMap(materials);
        Optional<DialectTableMetaDataLoader> dialectLoader = SchemaBuilderWithDialectLoader.findDialectTableMetaDataLoader(materials);
        Map<String, TableMetaData> result;
        if (dialectLoader.isPresent()) {
            result = SchemaBuilderWithDialectLoader.build(dialectLoader.get(), EXECUTOR_SERVICE, materials, logicTableDataNodesMap);
        } else {
            result = SchemaBuilderWithDefaultLoader.build(EXECUTOR_SERVICE, materials, logicTableDataNodesMap);
        }
        return result;
    }
    
    private static Map<String, Collection<DataNode>> getLogicTableDataNodesMap(final SchemaBuilderMaterials materials) {
        List<DataNodeContainedRule> dataNodeContainedRuleList = materials.getRules().stream()
                .filter(each -> each instanceof DataNodeContainedRule)
                .map(each -> (DataNodeContainedRule) each)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(dataNodeContainedRuleList)) {
            return Collections.emptyMap();
        }

        Map<String, Collection<DataNode>> logicTableDataNodes = getLogicTableDataNodesMap(dataNodeContainedRuleList);
        Map<String, Collection<String>> logicActualdataSourceMap = getLogicActualDataSourceMap(materials);
        if (MapUtils.isEmpty(logicActualdataSourceMap)) {
            return logicTableDataNodes;
        }

        return replaceLogicDataNodeWithActualDataNode(logicTableDataNodes, logicActualdataSourceMap);
    }
    
    private static Map<String, Collection<DataNode>> getLogicTableDataNodesMap(
            final List<DataNodeContainedRule> dataNodeContainedRuleList) {
        BinaryOperator<Collection<DataNode>> logicTableMergeFunction = getMergeFunction(DataNode.class);
        Map<String, Collection<DataNode>> logicTableDataNodes = dataNodeContainedRuleList.stream()
                .flatMap(each -> each.getAllDataNodes().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, logicTableMergeFunction));
        return logicTableDataNodes;
    }
    
    private static Map<String, Collection<String>> getLogicActualDataSourceMap(final SchemaBuilderMaterials materials) {
        BinaryOperator<Collection<String>> dataSourceContainedMergeFunction = getMergeFunction(String.class);
        Map<String, Collection<String>> dataSourceContainedMap = materials.getRules().stream()
                .filter(each -> each instanceof DataSourceContainedRule)
                .flatMap(each -> ((DataSourceContainedRule) each).getDataSourceMapper().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, dataSourceContainedMergeFunction));
        return dataSourceContainedMap;
    }
    
    private static <T> BinaryOperator<Collection<T>> getMergeFunction(final Class<T> tClass) {
        return (a, b) -> {
            Collection<T> result = (a instanceof LinkedList) ? a : new LinkedList<>(a);
            result.addAll(b);
            return result;
        };
    }
    
    private static Map<String, Collection<DataNode>> replaceLogicDataNodeWithActualDataNode(
            final Map<String, Collection<DataNode>> logicTableDataNodes,
            final Map<String, Collection<String>> logicActualdataSourceMap) {
        Map<String, Collection<DataNode>> replaceResult = new HashMap<>(logicTableDataNodes.size(), 1);
        for (Map.Entry<String, Collection<DataNode>> entry : logicTableDataNodes.entrySet()) {
            Collection<DataNode> dataNodes = entry.getValue();
            Collection<DataNode> actualDataNodeList = new LinkedList<>();
            for (DataNode each : dataNodes) {
                Collection<String> actualDataSourceNameList = logicActualdataSourceMap.get(each.getDataSourceName());
                if (CollectionUtils.isEmpty(actualDataSourceNameList)) {
                    actualDataNodeList.add(each);
                } else {
                    for (String actualDataSourceName : actualDataSourceNameList) {
                        actualDataNodeList.add(new DataNode(actualDataSourceName, each.getTableName()));
                    }
                }
            }
            replaceResult.put(entry.getKey(), actualDataNodeList);
        }
        return replaceResult;
    }
    
    private static Map<String, TableMetaData> buildLogicTableMetaDataMap(final SchemaBuilderMaterials materials,
            final Map<String, TableMetaData> tables) throws SQLException {
        Map<String, TableMetaData> result = new HashMap<>(materials.getRules().size(), 1);

        Collection<String> ruleLogicTables = materials.getRules().stream()
                .filter(rule -> rule instanceof TableContainedRule)
                .flatMap(rule -> ((TableContainedRule) rule).getTables().stream())
                .collect(Collectors.toSet());
        for (String table : ruleLogicTables) {
            TableMetaData tableMetaData = tables.get(table);
            if (null != tableMetaData) {
                result.put(table, TableMetaDataBuilder.decorate(table, tableMetaData, materials.getRules()));
            }
        }
        return result;
    }
}
