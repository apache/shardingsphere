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

package org.apache.shardingsphere.infra.datanode;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.type.DataSourceContainedRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DataNodesTest {
    
    private static Map<String, Collection<String>> replicaDataSourcesMap = new HashMap<>();
    
    private static Map<String, List<String>> shardingActualTablesMap = new HashMap<>();
    
    private static Map<String, List<String>> dataSourceSingleTablesMap = new HashMap<>();
    
    private final String logicTableName1 = "user";
    
    private final String logicTableName2 = "dept";
    
    private final Collection<String> dataSourceNames1 = Arrays.asList("primary_db_1", "primary_db_2", "replica_db_1", "replica_db_2");
    
    private final Collection<String> dataSourceNames2 = Arrays.asList("primary_db_3", "replica_db_3");
    
    private final String logicDataSourceName = "primary_db_1";
    
    private final Collection<String> replicaDataSourceNames = Arrays.asList("route_db_1", "route_db_2");
    
    static {
        replicaDataSourcesMap.putIfAbsent("node_0", Lists.newArrayList("primary_ds0", "replica_ds0_0", "replica_ds0_1"));
        shardingActualTablesMap.put("user", Lists.newArrayList("user_0", "user_1"));
        dataSourceSingleTablesMap.put("primary_ds0", Lists.newArrayList("primary_ds0_table_0", "primary_ds0_table_1"));
    }
    
    @Test
    public void assertGetDataNodesWithTablePresent() {
        DataNodes dataNodes = new DataNodes(Lists.newArrayList(buildDataSourceContainedRule(), buildDataNodeContainedRule(true)));
        Collection<DataNode> userDataNodes = dataNodes.getDataNodes("user");
        assertThat(userDataNodes, is(getShardingActualDataNode().get("user")));
        List<String> primaryDs0SingleTables = dataSourceSingleTablesMap.get("primary_ds0");
        for (String primaryDs0SingleTable : primaryDs0SingleTables) {
            Collection<DataNode> primaryDs0SingleTableDataNodes = dataNodes.getDataNodes(primaryDs0SingleTable);
            assertThat(primaryDs0SingleTableDataNodes, is(getSingleTableDataNode().get(primaryDs0SingleTable)));
        }
    }
    
    @Test(expected = NullPointerException.class)
    public void assertGetDataNodesWithTableAbsent() {
        DataNodes dataNodes = new DataNodes(Lists.newArrayList(buildDataSourceContainedRule(), buildDataNodeContainedRule(true)));
        Collection<DataNode> userDataNodes = dataNodes.getDataNodes("order");
        assertThat(userDataNodes, is(Collections.emptyMap()));
    }
    
    @Test
    public void assertGetDataNodesWithDataNodeContainedRuleAbsent() {
        DataNodes dataNodes = new DataNodes(Lists.newArrayList(buildDataSourceContainedRule()));
        Collection<DataNode> userDataNodes = dataNodes.getDataNodes("user");
        assertThat(userDataNodes, is(Collections.emptyList()));
    }
    
    @Test
    public void assertGetDataNodesWithDataSourceContainedRuleAbsent() {
        DataNodes dataNodes = new DataNodes(Lists.newArrayList(buildDataNodeContainedRule(false)));
        Collection<DataNode> userDataNodes = dataNodes.getDataNodes("user");
        assertThat(userDataNodes, is(getShardingActualDataNode().get("user")));
    }
    
    private DataSourceContainedRule buildDataSourceContainedRule() {
        DataSourceContainedRule result = mock(DataSourceContainedRule.class);
        when(result.getDataSourceMapper()).thenReturn(replicaDataSourcesMap);
        return result;
    }
    
    private DataNodeContainedRule buildDataNodeContainedRule(final boolean replicaQuery) {
        DataNodeContainedRule result = mock(DataNodeContainedRule.class);
        Map<String, Collection<DataNode>> dataNodes = new HashMap<>();
        dataNodes.putAll(getSingleTableDataNode());
        dataNodes.putAll(replicaQuery ? getReplicaShardingDataNode() : getShardingActualDataNode());
        when(result.getAllDataNodes()).thenReturn(dataNodes);
        return result;
    }
    
    private Map<String, Collection<DataNode>> getSingleTableDataNode() {
        Map<String, Collection<DataNode>> result = new HashMap<>();
        for (Entry<String, List<String>> entry :dataSourceSingleTablesMap.entrySet()) {
            Map<String, Collection<DataNode>> map = entry.getValue().stream().collect(Collectors.toMap(singleTable -> singleTable,
                singleTable -> Lists.newArrayList(new DataNode(entry.getKey(), singleTable)), (Collection<DataNode> oldList, Collection<DataNode> newList) -> {
                    oldList.addAll(newList);
                    return oldList;
                })
            );
            result.putAll(map);
        }
        return result;
    }
    
    private Map<String, Collection<DataNode>> getReplicaShardingDataNode() {
        return shardingActualTablesMap.entrySet().stream().collect(
                Collectors.toMap(Entry::getKey,
                    entry -> entry.getValue().stream().map(shardingTable -> getActualDataNode(replicaDataSourcesMap.keySet(), shardingTable))
                        .flatMap(Collection::stream).collect(Collectors.toList())));
    }
    
    private Map<String, Collection<DataNode>> getShardingActualDataNode() {
        List<String> allDataSources = replicaDataSourcesMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        return shardingActualTablesMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().stream()
                .map(shardingTable -> getActualDataNode(allDataSources, shardingTable)).flatMap(Collection::stream).collect(Collectors.toList())));
    }
    
    private List<DataNode> getActualDataNode(final Collection<String> dataSources, final String tableName) {
        return dataSources.stream().map(each -> new DataNode(each, tableName)).collect(Collectors.toList());
    }
    
    @Test
    public void assertGetDataNodeGroups() {
        DataNodes dataNodes = getRoutedRuleDataNodes();
        assertThat(dataNodes.getDataNodeGroups(logicTableName1), is(getExpectedDataNodeGroups(dataSourceNames1, logicTableName1)));
        assertThat(dataNodes.getDataNodeGroups(logicTableName2), is(getExpectedDataNodeGroups(dataSourceNames2, logicTableName2)));
    }
    
    private DataNodes getRoutedRuleDataNodes() {
        Map<String, Collection<DataNode>> nodeMap = new HashMap<>();
        nodeMap.put(logicTableName1, getExpectedDataNodes(dataSourceNames1, logicTableName1));
        nodeMap.put(logicTableName2, getExpectedDataNodes(dataSourceNames2, logicTableName2));
        DataNodeContainedRule rule1 = mock(DataNodeContainedRule.class);
        when(rule1.getAllDataNodes()).thenReturn(nodeMap);
        Map<String, Collection<String>> dataSourceMapper = Collections.singletonMap(logicDataSourceName, replicaDataSourceNames);
        DataSourceContainedRule rule2 = mock(DataSourceContainedRule.class);
        when(rule2.getDataSourceMapper()).thenReturn(dataSourceMapper);
        return new DataNodes(Arrays.asList(rule1, rule2));
    }
    
    private Collection<DataNode> getExpectedDataNodes(final Collection<String> dataSourceNames, final String logicTableName) {
        Collection<DataNode> result = new LinkedList<>();
        for (String each : dataSourceNames) {
            if (logicDataSourceName.equals(each)) {
                replicaDataSourceNames.forEach(dataSourceName -> result.add(new DataNode(dataSourceName, logicTableName)));
            } else {
                result.add(new DataNode(each, logicTableName));
            }
        }
        return result;
    }
    
    private Map<String, List<DataNode>> getExpectedDataNodeGroups(final Collection<String> dataSourceNames, final String logicTableName) {
        return getExpectedDataNodes(dataSourceNames, logicTableName).stream().collect(Collectors.groupingBy(DataNode::getDataSourceName));
    }
}
