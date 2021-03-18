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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DataNodesTest {

    private static Map<String, Collection<String>> replicaDataSourcesMap = new HashMap<>();

    private static Map<String, List<String>> shardingActualTablesMap = new HashMap<>();

    private static Map<String, List<String>> dataSourceSingleTablesMap = new HashMap<>();

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
        DataSourceContainedRule dataSourceContainedRule = mock(DataSourceContainedRule.class);
        when(dataSourceContainedRule.getDataSourceMapper()).thenReturn(replicaDataSourcesMap);
        return dataSourceContainedRule;
    }

    private DataNodeContainedRule buildDataNodeContainedRule(final boolean replicaQuery) {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        Map<String, Collection<DataNode>> dataNodes = new HashMap<>();
        dataNodes.putAll(getSingleTableDataNode());
        dataNodes.putAll(replicaQuery ? getReplicaShardingDataNode() : getShardingActualDataNode());
        when(dataNodeContainedRule.getAllDataNodes()).thenReturn(dataNodes);
        return dataNodeContainedRule;
    }

    private Map<String, Collection<DataNode>> getSingleTableDataNode() {
        Map<String, Collection<DataNode>> singleTableDataNodeMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry :dataSourceSingleTablesMap.entrySet()) {
            Map<String, Collection<DataNode>> map = entry.getValue().stream().collect(Collectors.toMap(singleTable -> singleTable,
                singleTable -> Lists.newArrayList(new DataNode(entry.getKey(), singleTable)), (Collection<DataNode> oldList, Collection<DataNode> newList) -> {
                    oldList.addAll(newList);
                    return oldList;
                })
            );
            singleTableDataNodeMap.putAll(map);
        }
        return singleTableDataNodeMap;
    }

    private Map<String, Collection<DataNode>> getReplicaShardingDataNode() {
        return shardingActualTablesMap.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey,
                    entry -> entry.getValue().stream().map(shardingTable -> getActualDataNode(replicaDataSourcesMap.keySet(), shardingTable))
                        .flatMap(Collection::stream).collect(Collectors.toList())));
    }

    private Map<String, Collection<DataNode>> getShardingActualDataNode() {
        List<String> allDataSources = replicaDataSourcesMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        return shardingActualTablesMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                .map(shardingTable -> getActualDataNode(allDataSources, shardingTable)).flatMap(Collection::stream).collect(Collectors.toList())));
    }

    private List<DataNode> getActualDataNode(final Collection<String> dataSources, final String tableName) {
        return dataSources.stream().map(dataSource -> new DataNode(dataSource, tableName)).collect(Collectors.toList());
    }
}
