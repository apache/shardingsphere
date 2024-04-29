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

package org.apache.shardingsphere.sharding.rule;

import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.exception.metadata.ActualTableNotFoundException;
import org.apache.shardingsphere.sharding.exception.metadata.BindingTableNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BindingTableRuleTest {
    
    @Test
    void assertHasLogicTable() {
        assertTrue(createBindingTableRule().hasLogicTable("Logic_Table"));
    }
    
    @Test
    void assertNotHasLogicTable() {
        assertFalse(createBindingTableRule().hasLogicTable("New_Table"));
    }
    
    @Test
    void assertGetBindingActualTablesSuccess() {
        assertThat(createBindingTableRule().getBindingActualTable("ds1", "Sub_Logic_Table", "LOGIC_TABLE", "table_1"), is("SUB_TABLE_1"));
    }
    
    @Test
    void assertGetBindingActualTablesFailureWhenNotFound() {
        assertThrows(ActualTableNotFoundException.class, () -> createBindingTableRule().getBindingActualTable("no_ds", "Sub_Logic_Table", "LOGIC_TABLE", "table_1"));
    }
    
    @Test
    void assertGetBindingActualTablesFailureWhenLogicTableNotFound() {
        assertThrows(BindingTableNotFoundException.class, () -> createBindingTableRule().getBindingActualTable("ds0", "No_Logic_Table", "LOGIC_TABLE", "table_1"));
    }
    
    @Test
    void assertGetAllLogicTables() {
        assertThat(createBindingTableRule().getAllLogicTables(), is(new LinkedHashSet<>(Arrays.asList("logic_table", "sub_logic_table"))));
    }
    
    @Test
    void assertGetTableRules() {
        List<ShardingTable> shardingTables = new ArrayList<>(createBindingTableRule().getShardingTables().values());
        assertThat(shardingTables.size(), is(2));
        assertThat(shardingTables.get(0).getLogicTable(), is(createShardingTable().getLogicTable()));
        assertThat(shardingTables.get(0).getActualDataNodes(), is(createShardingTable().getActualDataNodes()));
        assertThat(shardingTables.get(1).getLogicTable(), is(createSubShardingTable().getLogicTable()));
        assertThat(shardingTables.get(1).getActualDataNodes(), is(createSubShardingTable().getActualDataNodes()));
    }
    
    private BindingTableRule createBindingTableRule() {
        Map<String, ShardingTable> shardingTables = Stream.of(createShardingTable(), createSubShardingTable())
                .collect(Collectors.toMap(each -> each.getLogicTable().toLowerCase(), Function.identity(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        BindingTableRule result = new BindingTableRule();
        result.getShardingTables().putAll(shardingTables);
        return result;
    }
    
    private ShardingTable createShardingTable() {
        return new ShardingTable(new ShardingTableRuleConfiguration("LOGIC_TABLE", "ds${0..1}.table_${0..1}"), Arrays.asList("ds0", "ds1"), null);
    }
    
    private ShardingTable createSubShardingTable() {
        return new ShardingTable(new ShardingTableRuleConfiguration("SUB_LOGIC_TABLE", "ds${0..1}.SUB_TABLE_${0..1}"), Arrays.asList("ds0", "ds1"), null);
    }
}
