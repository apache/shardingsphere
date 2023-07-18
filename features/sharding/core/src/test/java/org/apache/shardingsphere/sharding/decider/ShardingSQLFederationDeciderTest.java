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

package org.apache.shardingsphere.sharding.decider;

import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.postgresql.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingSQLFederationDeciderTest {
    
    @Test
    void assertDecideWhenNotContainsShardingTable() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getShardingLogicTableNames(Arrays.asList("t_order", "t_order_item"))).thenReturn(Collections.emptyList());
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertFalse(new ShardingSQLFederationDecider().decide(
                createStatementContext(), Collections.emptyList(), mock(ShardingSphereRuleMetaData.class), mock(ShardingSphereDatabase.class), rule, includedDataNodes));
        assertTrue(includedDataNodes.isEmpty());
    }
    
    @Test
    void assertDecideWhenContainsSameShardingCondition() {
        SelectStatementContext select = createStatementContext();
        when(select.isContainsSubquery()).thenReturn(true);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertTrue(new ShardingSQLFederationDecider().decide(select, Collections.emptyList(), mock(ShardingSphereRuleMetaData.class), createDatabase(), createShardingRule(), includedDataNodes));
        assertThat(includedDataNodes.size(), is(4));
    }
    
    @Test
    void assertDecideWhenContainsSubquery() {
        SelectStatementContext select = createStatementContext();
        when(select.isContainsSubquery()).thenReturn(true);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertTrue(new ShardingSQLFederationDecider().decide(select, Collections.emptyList(), mock(ShardingSphereRuleMetaData.class), createDatabase(), createShardingRule(), includedDataNodes));
        assertThat(includedDataNodes.size(), is(4));
    }
    
    @Test
    void assertDecideWhenContainsHaving() {
        SelectStatementContext select = createStatementContext();
        when(select.isContainsHaving()).thenReturn(true);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertTrue(new ShardingSQLFederationDecider().decide(select, Collections.emptyList(), mock(ShardingSphereRuleMetaData.class), createDatabase(), createShardingRule(), includedDataNodes));
        assertThat(includedDataNodes.size(), is(4));
    }
    
    @Test
    void assertDecideWhenContainsCombine() {
        SelectStatementContext select = createStatementContext();
        when(select.isContainsCombine()).thenReturn(true);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertTrue(new ShardingSQLFederationDecider().decide(select, Collections.emptyList(), mock(ShardingSphereRuleMetaData.class), createDatabase(), createShardingRule(), includedDataNodes));
        assertThat(includedDataNodes.size(), is(4));
    }
    
    @Test
    void assertDecideWhenContainsPartialDistinctAggregation() {
        SelectStatementContext select = createStatementContext();
        when(select.isContainsPartialDistinctAggregation()).thenReturn(true);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertTrue(new ShardingSQLFederationDecider().decide(select, Collections.emptyList(), mock(ShardingSphereRuleMetaData.class), createDatabase(), createShardingRule(), includedDataNodes));
        assertThat(includedDataNodes.size(), is(4));
    }
    
    @Test
    void assertDecideWhenAllTablesInSameDataSource() {
        SelectStatementContext select = createStatementContext();
        when(select.isContainsJoinQuery()).thenReturn(true);
        ShardingRule shardingRule = createShardingRule();
        when(shardingRule.isAllTablesInSameDataSource(Arrays.asList("t_order", "t_order_item"))).thenReturn(true);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertFalse(new ShardingSQLFederationDecider().decide(select, Collections.emptyList(), mock(ShardingSphereRuleMetaData.class), createDatabase(), shardingRule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(4));
    }
    
    @Test
    void assertDecideWhenAllTablesIsBindingTables() {
        SelectStatementContext select = createStatementContext();
        when(select.isContainsJoinQuery()).thenReturn(true);
        ShardingRule shardingRule = createShardingRule();
        ShardingSphereDatabase database = createDatabase();
        when(shardingRule.isAllBindingTables(database, select, Arrays.asList("t_order", "t_order_item"))).thenReturn(true);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertFalse(new ShardingSQLFederationDecider().decide(select, Collections.emptyList(), mock(ShardingSphereRuleMetaData.class), database, shardingRule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(4));
    }
    
    @Test
    void assertDecideWhenAllTablesIsNotBindingTables() {
        SelectStatementContext select = createStatementContext();
        when(select.isContainsJoinQuery()).thenReturn(true);
        ShardingRule shardingRule = createShardingRule();
        ShardingSphereDatabase database = createDatabase();
        when(shardingRule.isAllBindingTables(database, select, Arrays.asList("t_order", "t_order_item"))).thenReturn(false);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertTrue(new ShardingSQLFederationDecider().decide(select, Collections.emptyList(), mock(ShardingSphereRuleMetaData.class), database, shardingRule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(4));
    }
    
    @Test
    void assertDecideWhenContainsOnlyOneTable() {
        SelectStatementContext select = createStatementContext();
        when(select.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(select.isContainsJoinQuery()).thenReturn(true);
        ShardingRule shardingRule = createShardingRule();
        when(shardingRule.getShardingLogicTableNames(Collections.singletonList("t_order"))).thenReturn(Collections.singletonList("t_order"));
        ShardingSphereDatabase database = createDatabase();
        when(shardingRule.isAllBindingTables(database, select, Collections.singletonList("t_order"))).thenReturn(false);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertFalse(new ShardingSQLFederationDecider().decide(select, Collections.emptyList(), mock(ShardingSphereRuleMetaData.class), database, shardingRule, includedDataNodes));
    }
    
    @Test
    void assertDecideWhenAllTablesIsNotBindingTablesAndContainsPagination() {
        SelectStatementContext select = createStatementContext();
        when(select.isContainsJoinQuery()).thenReturn(true);
        when(select.getPaginationContext().isHasPagination()).thenReturn(true);
        ShardingRule shardingRule = createShardingRule();
        ShardingSphereDatabase database = createDatabase();
        when(shardingRule.isAllBindingTables(database, select, Arrays.asList("t_order", "t_order_item"))).thenReturn(false);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertTrue(new ShardingSQLFederationDecider().decide(select, Collections.emptyList(), mock(ShardingSphereRuleMetaData.class), database, shardingRule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(4));
    }
    
    private SelectStatementContext createStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getTablesContext().getTableNames()).thenReturn(Arrays.asList("t_order", "t_order_item"));
        when(result.getDatabaseType()).thenReturn(new PostgreSQLDatabaseType());
        return result;
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        return result;
    }
    
    private ShardingRule createShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        when(result.getShardingLogicTableNames(Arrays.asList("t_order", "t_order_item"))).thenReturn(Arrays.asList("t_order", "t_order_item"));
        TableRule tableRule = mock(TableRule.class);
        when(tableRule.getActualDataNodes()).thenReturn(Arrays.asList(
                new DataNode("ds_0", "t_order"), new DataNode("ds_1", "t_order"),
                new DataNode("ds_0", "t_order_item"), new DataNode("ds_1", "t_order_item")));
        when(result.findTableRule("t_order")).thenReturn(Optional.of(tableRule));
        BindingTableRule bindingTableRule = mock(BindingTableRule.class);
        when(bindingTableRule.hasLogicTable("t_order")).thenReturn(true);
        when(bindingTableRule.hasLogicTable("t_order_item")).thenReturn(true);
        when(result.findBindingTableRule("t_order")).thenReturn(Optional.of(bindingTableRule));
        when(result.findBindingTableRule("t_order_item")).thenReturn(Optional.of(bindingTableRule));
        return result;
    }
}
