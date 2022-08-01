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

import org.apache.shardingsphere.infra.fixture.FixtureRule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DataNodesTest {
    
    private static final Map<String, Collection<String>> READ_WRITE_SPLITTING_DATASOURCE_MAP = new HashMap<>();
    
    static {
        READ_WRITE_SPLITTING_DATASOURCE_MAP.putIfAbsent("readwrite_ds", Arrays.asList("primary_ds", "replica_ds_0", "replica_ds_1"));
    }
    
    @Test
    public void assertGetDataNodesForShardingTableWithoutDataNodeContainedRule() {
        DataNodes dataNodes = new DataNodes(Collections.singletonList(mockDataSourceContainedRule()));
        Collection<DataNode> actual = dataNodes.getDataNodes("t_order");
        assertThat(actual, is(Collections.emptyList()));
    }
    
    @Test
    public void assertGetDataNodesForSingleTableWithoutDataNodeContainedRule() {
        DataNodes dataNodes = new DataNodes(Collections.singletonList(mockDataSourceContainedRule()));
        Collection<DataNode> actual = dataNodes.getDataNodes("t_single");
        assertThat(actual, is(Collections.emptyList()));
    }
    
    @Test
    public void assertGetDataNodesForShardingTableWithDataNodeContainedRuleWithoutDataSourceContainedRule() {
        DataNodes dataNodes = new DataNodes(mockDataNodeContainedRules());
        Collection<DataNode> actual = dataNodes.getDataNodes("t_order");
        assertThat(actual.size(), is(2));
        Iterator<DataNode> iterator = actual.iterator();
        DataNode firstDataNode = iterator.next();
        assertThat(firstDataNode.getDataSourceName(), is("readwrite_ds"));
        assertThat(firstDataNode.getTableName(), is("t_order_0"));
        DataNode secondDataNode = iterator.next();
        assertThat(secondDataNode.getDataSourceName(), is("readwrite_ds"));
        assertThat(secondDataNode.getTableName(), is("t_order_1"));
    }
    
    @Test
    public void assertGetDataNodesForSingleTableWithDataNodeContainedRuleWithoutDataSourceContainedRule() {
        DataNodes dataNodes = new DataNodes(mockDataNodeContainedRules());
        Collection<DataNode> actual = dataNodes.getDataNodes("t_single");
        assertThat(actual.size(), is(1));
        Iterator<DataNode> iterator = actual.iterator();
        DataNode firstDataNode = iterator.next();
        assertThat(firstDataNode.getDataSourceName(), is("readwrite_ds"));
        assertThat(firstDataNode.getTableName(), is("t_single"));
    }
    
    @Test
    public void assertGetDataNodesForShardingTableWithDataNodeContainedRuleAndDataSourceContainedRule() {
        DataNodes dataNodes = new DataNodes(mockShardingSphereRules());
        Collection<DataNode> actual = dataNodes.getDataNodes("t_order");
        assertThat(actual.size(), is(6));
        Iterator<DataNode> iterator = actual.iterator();
        DataNode firstDataNode = iterator.next();
        assertThat(firstDataNode.getDataSourceName(), is("primary_ds"));
        assertThat(firstDataNode.getTableName(), is("t_order_0"));
        DataNode secondDataNode = iterator.next();
        assertThat(secondDataNode.getDataSourceName(), is("replica_ds_0"));
        assertThat(secondDataNode.getTableName(), is("t_order_0"));
        DataNode thirdDataNode = iterator.next();
        assertThat(thirdDataNode.getDataSourceName(), is("replica_ds_1"));
        assertThat(thirdDataNode.getTableName(), is("t_order_0"));
        DataNode fourthDataNode = iterator.next();
        assertThat(fourthDataNode.getDataSourceName(), is("primary_ds"));
        assertThat(fourthDataNode.getTableName(), is("t_order_1"));
        DataNode fifthDataNode = iterator.next();
        assertThat(fifthDataNode.getDataSourceName(), is("replica_ds_0"));
        assertThat(fifthDataNode.getTableName(), is("t_order_1"));
        DataNode sixthDataNode = iterator.next();
        assertThat(sixthDataNode.getDataSourceName(), is("replica_ds_1"));
        assertThat(sixthDataNode.getTableName(), is("t_order_1"));
    }
    
    @Test
    public void assertGetDataNodesForSingleTableWithDataNodeContainedRuleAndDataSourceContainedRule() {
        DataNodes dataNodes = new DataNodes(mockShardingSphereRules());
        Collection<DataNode> actual = dataNodes.getDataNodes("t_single");
        assertThat(actual.size(), is(3));
        Iterator<DataNode> iterator = actual.iterator();
        DataNode firstDataNode = iterator.next();
        assertThat(firstDataNode.getDataSourceName(), is("primary_ds"));
        assertThat(firstDataNode.getTableName(), is("t_single"));
        DataNode secondDataNode = iterator.next();
        assertThat(secondDataNode.getDataSourceName(), is("replica_ds_0"));
        assertThat(secondDataNode.getTableName(), is("t_single"));
        DataNode thirdDataNode = iterator.next();
        assertThat(thirdDataNode.getDataSourceName(), is("replica_ds_1"));
        assertThat(thirdDataNode.getTableName(), is("t_single"));
    }
    
    private Collection<ShardingSphereRule> mockShardingSphereRules() {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        result.add(mockDataSourceContainedRule());
        result.addAll(mockDataNodeContainedRules());
        return result;
    }
    
    private ShardingSphereRule mockDataSourceContainedRule() {
        DataSourceContainedRule result = mock(FixtureRule.class);
        when(result.getDataSourceMapper()).thenReturn(READ_WRITE_SPLITTING_DATASOURCE_MAP);
        return result;
    }
    
    private Collection<ShardingSphereRule> mockDataNodeContainedRules() {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        result.add(mockSingleTableRule());
        result.add(mockShardingRule());
        return result;
    }
    
    private ShardingSphereRule mockSingleTableRule() {
        DataNodeContainedRule result = mock(DataNodeContainedRule.class);
        when(result.getDataNodesByTableName("t_single")).thenReturn(Collections.singletonList(new DataNode("readwrite_ds", "t_single")));
        return result;
    }
    
    private ShardingSphereRule mockShardingRule() {
        DataNodeContainedRule result = mock(DataNodeContainedRule.class);
        Collection<DataNode> dataNodes = new LinkedList<>();
        dataNodes.add(new DataNode("readwrite_ds", "t_order_0"));
        dataNodes.add(new DataNode("readwrite_ds", "t_order_1"));
        when(result.getDataNodesByTableName("t_order")).thenReturn(dataNodes);
        return result;
    }
}
