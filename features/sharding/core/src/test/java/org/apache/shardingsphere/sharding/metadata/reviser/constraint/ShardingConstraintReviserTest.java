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

package org.apache.shardingsphere.sharding.metadata.reviser.constraint;

import org.apache.shardingsphere.database.connector.core.metadata.data.model.ConstraintMetaData;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
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

class ShardingConstraintReviserTest {
    
    private ShardingConstraintReviser reviser;
    
    private ShardingRule shardingRule;
    
    @BeforeEach
    void setUp() {
        shardingRule = createShardingRule();
        ShardingTable shardingTable = mock(ShardingTable.class);
        when(shardingTable.getActualDataNodes()).thenReturn(Arrays.asList(new DataNode("schema_name", (String) null, "table_name_0"), new DataNode("schema_name", (String) null, "table_name_1")));
        reviser = new ShardingConstraintReviser(shardingTable);
    }
    
    private ShardingRule createShardingRule() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.setTables(Collections.singleton(new ShardingTableRuleConfiguration("table_name", "ds.table_name")));
        return new ShardingRule(ruleConfig, Collections.singletonMap("ds", new MockedDataSource()), mock(ComputeNodeInstanceContext.class), Collections.emptyList());
    }
    
    @Test
    void assertReviseWhenTableMatches() {
        ConstraintMetaData originalMetaData = new ConstraintMetaData("test_table_name_1", "referenced_table_name");
        Optional<ConstraintMetaData> actual = reviser.revise("table_name_1", originalMetaData, shardingRule);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("test"));
        assertThat(actual.get().getReferencedTableName(), is("referenced_table_name"));
    }
    
    @Test
    void assertReviseWhenTableDoesNotMatch() {
        assertFalse(reviser.revise("table_name_1", new ConstraintMetaData("test_table_name_2", "referenced_table_name"), shardingRule).isPresent());
    }
}
