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
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CDCDataNodeUtilsTest {
    
    @Test
    void assertBuildDataNodesMap() {
        ShardingSphereDatabase mockDatabase = mock(ShardingSphereDatabase.class);
        RuleMetaData mockRuleMetaData = mock(RuleMetaData.class);
        ShardingRule mockShardingRule = mock(ShardingRule.class);
        TableRule mockTableRule = mock(TableRule.class);
        when(mockTableRule.getActualDataNodes()).thenReturn(Collections.singletonList(new DataNode("ds_0.t_order")));
        when(mockShardingRule.findTableRule("t_order")).thenReturn(Optional.of(mockTableRule));
        when(mockShardingRule.getTableRule("t_order")).thenReturn(mockTableRule);
        when(mockRuleMetaData.findSingleRule(ShardingRule.class)).thenReturn(Optional.of(mockShardingRule));
        SingleRule mockSingleRule = mock(SingleRule.class);
        when(mockRuleMetaData.findSingleRule(SingleRule.class)).thenReturn(Optional.of(mockSingleRule));
        when(mockSingleRule.getAllDataNodes()).thenReturn(Collections.singletonMap("t_order_item", Collections.singletonList(new DataNode("single.t_order_item"))));
        when(mockDatabase.getRuleMetaData()).thenReturn(mockRuleMetaData);
        BroadcastRule mockBroadcastRule = mock(BroadcastRule.class);
        when(mockRuleMetaData.findSingleRule(BroadcastRule.class)).thenReturn(Optional.of(mockBroadcastRule));
        when(mockBroadcastRule.findFirstActualTable("t_address")).thenReturn(Optional.of("broadcast.t_address"));
        when(mockBroadcastRule.getTableDataNodes()).thenReturn(Collections.singletonMap("t_address", Collections.singletonList(new DataNode("broadcast.t_address"))));
        Map<String, List<DataNode>> actual = CDCDataNodeUtils.buildDataNodesMap(mockDatabase, Arrays.asList("t_order", "t_order_item", "t_address"));
        assertTrue(actual.containsKey("t_order"));
        assertTrue(actual.containsKey("t_order_item"));
        assertTrue(actual.containsKey("t_address"));
    }
}
