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

import org.apache.shardingsphere.infra.fixture.ReadWriteSplittingRuleFixture;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DataNodesTest {
    
    private static final Map<String, Collection<String>> READ_WRITE_SPLITTING_DATASOURCE_MAP = new HashMap<>();
    
    private static final Map<String, List<String>> SHARDING_ACTUAL_TABLE_MAP = new HashMap<>();
    
    private static final Map<String, List<String>> DATASOURCE_SINGLE_TABLE_MAP = new HashMap<>();
    
    static {
        READ_WRITE_SPLITTING_DATASOURCE_MAP.putIfAbsent("node_0", Arrays.asList("primary_ds0", "replica_ds0_0", "replica_ds0_1"));
        SHARDING_ACTUAL_TABLE_MAP.put("user", Arrays.asList("user_0", "user_1"));
        DATASOURCE_SINGLE_TABLE_MAP.put("primary_ds0", Arrays.asList("primary_ds0_table_0", "primary_ds0_table_1"));
    }
    
    @Test
    public void assertGetDataNodesWithTablePresent() {
        DataNodes dataNodes = new DataNodes(Arrays.asList(buildDataSourceContainedRule(), buildDataNodeContainedRule(true)));
        Collection<DataNode> userDataNodes = dataNodes.getDataNodes("user");
        assertThat(userDataNodes, is(getShardingActualDataNode().get("user")));
        List<String> primaryDs0SingleTables = DATASOURCE_SINGLE_TABLE_MAP.get("primary_ds0");
        for (String primaryDs0SingleTable : primaryDs0SingleTables) {
            Collection<DataNode> primaryDs0SingleTableDataNodes = dataNodes.getDataNodes(primaryDs0SingleTable);
            assertThat(primaryDs0SingleTableDataNodes, is(getSingleTableDataNode().get(primaryDs0SingleTable)));
        }
    }
    
    @Test
    public void assertGetDataNodesWithTableAbsent() {
        DataNodes dataNodes = new DataNodes(Arrays.asList(buildDataSourceContainedRule(), buildDataNodeContainedRule(true)));
        Collection<DataNode> userDataNodes = dataNodes.getDataNodes("order");
        assertThat(userDataNodes, is(Collections.emptyList()));
    }
    
    @Test
    public void assertGetDataNodesWithDataNodeContainedRuleAbsent() {
        DataNodes dataNodes = new DataNodes(Collections.singletonList(buildDataSourceContainedRule()));
        Collection<DataNode> userDataNodes = dataNodes.getDataNodes("user");
        assertThat(userDataNodes, is(Collections.emptyList()));
    }
    
    @Test
    public void assertGetDataNodesWithDataSourceContainedRuleAbsent() {
        DataNodes dataNodes = new DataNodes(Collections.singletonList(buildDataNodeContainedRule(false)));
        Collection<DataNode> userDataNodes = dataNodes.getDataNodes("user");
        assertThat(userDataNodes, is(getShardingActualDataNode().get("user")));
    }
    
    private DataSourceContainedRule buildDataSourceContainedRule() {
        DataSourceContainedRule result = mock(ReadWriteSplittingRuleFixture.class);
        when(result.getDataSourceMapper()).thenReturn(READ_WRITE_SPLITTING_DATASOURCE_MAP);
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
        for (Entry<String, List<String>> entry : DATASOURCE_SINGLE_TABLE_MAP.entrySet()) {
            Map<String, Collection<DataNode>> map = entry.getValue().stream().collect(Collectors.toMap(singleTable -> singleTable,
                singleTable -> Collections.singletonList(new DataNode(entry.getKey(), singleTable)), (Collection<DataNode> oldList, Collection<DataNode> newList) -> {
                    oldList.addAll(newList);
                    return oldList;
                })
            );
            result.putAll(map);
        }
        return result;
    }
    
    private Map<String, Collection<DataNode>> getReplicaShardingDataNode() {
        return SHARDING_ACTUAL_TABLE_MAP.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().stream().map(
            shardingTable -> getActualDataNode(READ_WRITE_SPLITTING_DATASOURCE_MAP.keySet(), shardingTable)).flatMap(Collection::stream).collect(Collectors.toList())));
    }
    
    private Map<String, Collection<DataNode>> getShardingActualDataNode() {
        List<String> allDataSources = READ_WRITE_SPLITTING_DATASOURCE_MAP.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        return SHARDING_ACTUAL_TABLE_MAP.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().stream()
                .map(shardingTable -> getActualDataNode(allDataSources, shardingTable)).flatMap(Collection::stream).collect(Collectors.toList())));
    }
    
    private List<DataNode> getActualDataNode(final Collection<String> dataSources, final String tableName) {
        return dataSources.stream().map(each -> new DataNode(each, tableName)).collect(Collectors.toList());
    }
}
