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

package org.apache.shardingsphere.data.pipeline.cdc.util;

import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CDCDataNodeUtilsTest {
    
    @Test
    void assertBuildDataNodesMap() {
        ShardingSphereDatabase mockDatabase = mock(ShardingSphereDatabase.class);
        RuleMetaData mockRuleMetaData = mock(RuleMetaData.class);
        ShardingRule mockShardingRule = mock(ShardingRule.class);
        ShardingTable mockShardingTable = mock(ShardingTable.class);
        when(mockShardingTable.getActualDataNodes()).thenReturn(Collections.singletonList(new DataNode("ds_0.t_order")));
        when(mockShardingRule.findShardingTable("t_order")).thenReturn(Optional.of(mockShardingTable));
        when(mockShardingRule.getShardingTable("t_order")).thenReturn(mockShardingTable);
        when(mockRuleMetaData.findSingleRule(ShardingRule.class)).thenReturn(Optional.of(mockShardingRule));
        SingleRule singleRule = mock(SingleRule.class);
        when(mockRuleMetaData.findSingleRule(SingleRule.class)).thenReturn(Optional.of(singleRule));
        DataNodeRuleAttribute singleDataNodeRuleAttribute = mock(DataNodeRuleAttribute.class);
        when(singleDataNodeRuleAttribute.getAllDataNodes()).thenReturn(Collections.singletonMap("t_order_item", Collections.singletonList(new DataNode("single.t_order_item"))));
        when(singleRule.getAttributes()).thenReturn(new RuleAttributes(singleDataNodeRuleAttribute));
        when(mockDatabase.getRuleMetaData()).thenReturn(mockRuleMetaData);
        BroadcastRule broadcastRule = mock(BroadcastRule.class, RETURNS_DEEP_STUBS);
        when(mockRuleMetaData.findSingleRule(BroadcastRule.class)).thenReturn(Optional.of(broadcastRule));
        DataNodeRuleAttribute broadcastDataNodeRuleAttribute = mock(DataNodeRuleAttribute.class);
        when(broadcastDataNodeRuleAttribute.findFirstActualTable("t_address")).thenReturn(Optional.of("broadcast.t_address"));
        when(broadcastDataNodeRuleAttribute.getAllDataNodes()).thenReturn(Collections.singletonMap("t_address", Collections.singletonList(new DataNode("broadcast.t_address"))));
        when(broadcastRule.getAttributes()).thenReturn(new RuleAttributes(broadcastDataNodeRuleAttribute));
        Map<String, List<DataNode>> actual = CDCDataNodeUtils.buildDataNodesMap(mockDatabase, Arrays.asList("t_order", "t_order_item", "t_address"));
        assertTrue(actual.containsKey("t_order"));
        assertTrue(actual.containsKey("t_order_item"));
        assertTrue(actual.containsKey("t_address"));
        assertThat(actual.get("t_order").get(0).getDataSourceName(), is("ds_0"));
        assertThat(actual.get("t_order_item").get(0).getDataSourceName(), is("single"));
        assertThat(actual.get("t_address").get(0).getDataSourceName(), is("broadcast"));
    }
}
