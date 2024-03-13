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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.ConstraintMetaData;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingConstraintReviserTest {

    private ShardingConstraintReviser reviser;
    private ShardingRule shardingRule;

    @BeforeEach
    public void setUp() {
        shardingRule = mockShardingRule();
        ShardingTable shardingTable = mock(ShardingTable.class);
        reviser = new ShardingConstraintReviser(shardingTable);
        when(shardingTable.getActualDataNodes()).thenReturn(Arrays.asList(new DataNode[]{
            new DataNode("schema_name", "table_name_0"),
            new DataNode("schema_name", "table_name_1")
        }));
    }

    private ShardingRule mockShardingRule() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = new ShardingTableRuleConfiguration("table_name",  "ds.table_name");
        ruleConfig.setTables(Collections.singleton(shardingTableRuleConfig));
        return new ShardingRule(ruleConfig, Maps.of("ds", new MockedDataSource()), mock(InstanceContext.class));
    }

    @Test
    public void testReviseWhenTableMatches() {
        ConstraintMetaData originalMetaData = new ConstraintMetaData("test_table_name_1", "referenced_table_name");
        Optional<ConstraintMetaData> result = reviser.revise("table_name_1", originalMetaData, shardingRule);
        assertTrue(result.isPresent());
        assertEquals("test", result.get().getName());
        assertEquals("referenced_table_name", result.get().getReferencedTableName());
    }

    @Test
    public void testReviseWhenTableDoesNotMatch() {
        ConstraintMetaData originalMetaData = new ConstraintMetaData("test_table_name_2", "referenced_table_name");
        Optional<ConstraintMetaData> result = reviser.revise("table_name_1", originalMetaData, shardingRule);
        assertFalse(result.isPresent());
    }

}