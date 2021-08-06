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
import org.apache.commons.collections4.MapUtils;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaBuilderHelper {
    
    /**
     * Get mapping between logic-table name and data nodes.
     * @param materials schema builder materials
     * @return logic-table to data nodes map
     */
    public static Map<String, Collection<DataNode>> getLogicTableDataNodesMap(final SchemaBuilderMaterials materials) {
        Map<String, Collection<DataNode>> logicTableDataNodesMap = materials.getRules().stream()
                .filter(each -> each instanceof DataNodeContainedRule)
                .flatMap(each -> ((DataNodeContainedRule) each).getAllDataNodes().entrySet().stream())
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (MapUtils.isEmpty(logicTableDataNodesMap)) {
            return Collections.emptyMap();
        }
        Map<String, Collection<String>> logicActualdataSourceMap = getLogicActualDataSourceMap(materials);
        if (MapUtils.isNotEmpty(logicActualdataSourceMap)) {
            logicTableDataNodesMap = replaceLogicDataNodeWithActualDataNode(logicTableDataNodesMap, logicActualdataSourceMap);
        }
        return logicTableDataNodesMap;
    }
    
    private static Map<String, Collection<String>> getLogicActualDataSourceMap(final SchemaBuilderMaterials materials) {
        Map<String, Collection<String>> dataSourceContainedMap = materials.getRules().stream()
                .filter(each -> each instanceof DataSourceContainedRule)
                .flatMap(each -> ((DataSourceContainedRule) each).getDataSourceMapper().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
                    Collection<String> result = (a instanceof LinkedList) ? a : new LinkedList<>(a);
                    result.addAll(b);
                    return result;
                }));
        return dataSourceContainedMap;
    }
    
    private static Map<String, Collection<DataNode>> replaceLogicDataNodeWithActualDataNode(
            final Map<String, Collection<DataNode>> logicTableDataNodesMap,
            final Map<String, Collection<String>> logicActualdataSourceMap) {
        Map<String, Collection<DataNode>> replaceResult = new HashMap<>(logicTableDataNodesMap.size(), 1);
        for (Map.Entry<String, Collection<DataNode>> entry : logicTableDataNodesMap.entrySet()) {
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
    
    /**
     * Get mapping between logic-table name and the first data node of 'logicTableDataNodesMap'.
     * @param logicTableDataNodesMap data nodes map
     * @return logic-table to first data node map
     */
    public static Map<String, DataNode> getLogicTableFirstDataNodeMap(final Map<String, Collection<DataNode>> logicTableDataNodesMap) {
        Map<String, DataNode> logicTableFirstDataNodeMap = logicTableDataNodesMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().iterator().next()));
        return logicTableFirstDataNodeMap;
    }
    
    /**
     * Get mapping between datasource name and exclude tables.
     * @param logicTableDataNodesMap    logic-table data nodes map
     * @param logicTableFirstDataNodeMap    logic-table first data node map
     * @return datasource to exclude tables map
     */
    public static Map<String, Collection<String>> getDatasourceExcludeTablesMap(
            final Map<String, Collection<DataNode>> logicTableDataNodesMap, final Map<String, DataNode> logicTableFirstDataNodeMap) {
        return logicTableDataNodesMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .filter(eachDataNode -> !logicTableFirstDataNodeMap.containsValue(eachDataNode))
                .collect(Collectors.groupingBy(DataNode::getDataSourceName, Collectors.mapping(DataNode::getTableName, Collectors.toCollection(
                        LinkedHashSet::new))));
    }
}
