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

package org.apache.shardingsphere.sharding.rule.attribute;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingTableMapperRuleAttributeTest {
    
    private ShardingTableMapperRuleAttribute ruleAttribute;
    
    @BeforeEach
    void setUp() {
        ShardingTable shardingTable = mock(ShardingTable.class);
        when(shardingTable.getLogicTable()).thenReturn("foo_tbl");
        when(shardingTable.getActualDataNodes()).thenReturn(Collections.singletonList(new DataNode("foo_ds.foo_tbl_0")));
        ruleAttribute = new ShardingTableMapperRuleAttribute(Collections.singleton(shardingTable));
    }
    
    @Test
    void assertGetLogicTableMapper() {
        assertThat(new LinkedList<>(ruleAttribute.getLogicTableMapper().getTableNames()), is(Collections.singletonList("foo_tbl")));
    }
    
    @Test
    void assertGetActualTableMapper() {
        assertThat(new LinkedList<>(ruleAttribute.getActualTableMapper().getTableNames()), is(Collections.singletonList("foo_tbl_0")));
    }
    
    @Test
    void assertGetDistributedTableMapper() {
        assertThat(new LinkedList<>(ruleAttribute.getDistributedTableMapper().getTableNames()), is(Collections.singletonList("foo_tbl")));
    }
    
    @Test
    void assertGetEnhancedTableMapper() {
        assertThat(new LinkedList<>(ruleAttribute.getEnhancedTableMapper().getTableNames()), is(Collections.singletonList("foo_tbl")));
    }
}
