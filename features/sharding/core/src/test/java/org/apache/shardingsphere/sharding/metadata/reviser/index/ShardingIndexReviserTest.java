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

package org.apache.shardingsphere.sharding.metadata.reviser.index;

import org.apache.shardingsphere.infra.database.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingIndexReviserTest {
    
    private ShardingRule shardingRule;
    
    private ShardingIndexReviser shardingIndexReviser;
    
    @Test
    void assertRevise() {
        shardingRule = createShardingRule();
        ShardingTable shardingTable = mock(ShardingTable.class);
        when(shardingTable.getActualDataNodes()).thenReturn(Arrays.asList(new DataNode("SCHEMA_NAME", "TABLE_NAME_0"), new DataNode("SCHEMA_NAME", "TABLE_NAME_1")));
        shardingIndexReviser = new ShardingIndexReviser(shardingTable);
        IndexMetaData originalMetaData = new IndexMetaData("TEST_INDEX");
        originalMetaData.getColumns().add("TEST_COLUMN");
        originalMetaData.setUnique(false);
        Optional<IndexMetaData> revisedMetaData = shardingIndexReviser.revise("TABLE_NAME_0", originalMetaData, shardingRule);
        assertTrue(revisedMetaData.isPresent());
        assertThat(revisedMetaData.get().getName(), is("TEST_INDEX"));
        assertThat(revisedMetaData.get().getColumns().size(), is(1));
        assertFalse(revisedMetaData.get().isUnique());
    }
    
    @Test
    void assertReviseWhenActualDataNodeIsEmpty() {
        shardingRule = createShardingRule();
        ShardingTable shardingTable = mock(ShardingTable.class);
        when(shardingTable.getActualDataNodes()).thenReturn(Collections.emptyList());
        shardingIndexReviser = new ShardingIndexReviser(shardingTable);
        IndexMetaData originalMetaData = new IndexMetaData("TEST_INDEX");
        Optional<IndexMetaData> revisedMetaData = shardingIndexReviser.revise("TABLE_NAME_1", originalMetaData, shardingRule);
        assertThat(revisedMetaData, is(Optional.empty()));
    }
    
    private ShardingRule createShardingRule() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.setTables(Collections.singleton(new ShardingTableRuleConfiguration("TABLE_NAME", "DS.TABLE_NAME")));
        return new ShardingRule(ruleConfig, Collections.singletonMap("DS", new MockedDataSource()), mock(ComputeNodeInstanceContext.class));
    }
}
