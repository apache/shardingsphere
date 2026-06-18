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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationDecider;
import org.junit.jupiter.api.BeforeEach;
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
    
    private ShardingSQLFederationDecider decider;
    
    @BeforeEach
    void setUp() {
        decider = (ShardingSQLFederationDecider) OrderedSPILoader.getServicesByClass(SQLFederationDecider.class, Collections.singleton(ShardingRule.class)).get(ShardingRule.class);
    }
    
    @Test
    void assertDecideWithoutShardingTable() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getShardingLogicTableNames(Arrays.asList("foo_tbl", "bar_tbl"))).thenReturn(Collections.emptyList());
        when(rule.findShardingTable("foo_tbl")).thenReturn(Optional.of(mock(ShardingTable.class)));
        when(rule.findShardingTable("bar_tbl")).thenReturn(Optional.of(mock(ShardingTable.class)));
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertFalse(decider.decide(createSQLStatementContext(), Collections.emptyList(), mock(RuleMetaData.class), mock(ShardingSphereDatabase.class), rule, includedDataNodes));
        assertTrue(includedDataNodes.isEmpty());
    }
    
    @Test
    void assertDecideWithSubquery() {
        SelectStatementContext sqlStatementContext = createSQLStatementContext();
        when(sqlStatementContext.isContainsSubquery()).thenReturn(true);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        ShardingRule rule = createShardingRule();
        assertTrue(decider.decide(sqlStatementContext, Collections.emptyList(), mock(RuleMetaData.class), createDatabase(rule), rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(4));
    }
    
    @Test
    void assertDecideWithHaving() {
        SelectStatementContext sqlStatementContext = createSQLStatementContext();
        when(sqlStatementContext.isContainsHaving()).thenReturn(true);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        ShardingRule rule = createShardingRule();
        assertTrue(decider.decide(sqlStatementContext, Collections.emptyList(), mock(RuleMetaData.class), createDatabase(rule), rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(4));
    }
    
    @Test
    void assertDecideWithCombine() {
        SelectStatementContext sqlStatementContext = createSQLStatementContext();
        when(sqlStatementContext.isContainsCombine()).thenReturn(true);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        ShardingRule rule = createShardingRule();
        assertTrue(decider.decide(sqlStatementContext, Collections.emptyList(), mock(RuleMetaData.class), createDatabase(rule), rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(4));
    }
    
    @Test
    void assertDecideWithPartialDistinctAggregation() {
        SelectStatementContext sqlStatementContext = createSQLStatementContext();
        when(sqlStatementContext.isContainsPartialDistinctAggregation()).thenReturn(true);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        ShardingRule rule = createShardingRule();
        assertTrue(decider.decide(sqlStatementContext, Collections.emptyList(), mock(RuleMetaData.class), createDatabase(rule), rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(4));
    }
    
    @Test
    void assertDecideWithoutJoinQuery() {
        SelectStatementContext sqlStatementContext = createSQLStatementContext();
        ShardingRule rule = createShardingRule();
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertFalse(decider.decide(sqlStatementContext, Collections.emptyList(), mock(RuleMetaData.class), createDatabase(rule), rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(4));
    }
    
    @Test
    void assertDecideWithAllTablesInSameDataSource() {
        SelectStatementContext sqlStatementContext = createSQLStatementContext();
        when(sqlStatementContext.isContainsJoinQuery()).thenReturn(true);
        ShardingRule rule = createShardingRule();
        ShardingSphereDatabase database = createDatabase(rule);
        when(rule.isAllTablesInSameDataSource(Arrays.asList("foo_tbl", "bar_tbl"))).thenReturn(true);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertFalse(decider.decide(sqlStatementContext, Collections.emptyList(), mock(RuleMetaData.class), database, rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(4));
    }
    
    @Test
    void assertDecideWithSelfJoinWithoutShardingColumn() {
        SelectStatementContext sqlStatementContext = createSQLStatementContext();
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singleton("foo_tbl"));
        when(sqlStatementContext.isContainsJoinQuery()).thenReturn(true);
        ShardingRule rule = createShardingRule();
        when(rule.getShardingLogicTableNames(Collections.singleton("foo_tbl"))).thenReturn(Collections.singleton("foo_tbl"));
        ShardingSphereDatabase database = createDatabase(rule);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertTrue(decider.decide(sqlStatementContext, Collections.emptyList(), mock(RuleMetaData.class), database, rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(2));
    }
    
    @Test
    void assertDecideWithNotSelfJoin() {
        SelectStatementContext sqlStatementContext = createSQLStatementContext();
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singleton("foo_tbl"));
        ShardingRule rule = createShardingRule();
        when(rule.getShardingLogicTableNames(Collections.singleton("foo_tbl"))).thenReturn(Collections.singleton("foo_tbl"));
        ShardingSphereDatabase database = createDatabase(rule);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertFalse(decider.decide(sqlStatementContext, Collections.emptyList(), mock(RuleMetaData.class), database, rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(2));
    }
    
    @Test
    void assertDecideWithSelfJoinAndShardingColumn() {
        SelectStatementContext sqlStatementContext = createSQLStatementContext();
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singleton("foo_tbl"));
        when(sqlStatementContext.isContainsJoinQuery()).thenReturn(true);
        ShardingRule rule = createShardingRule();
        when(rule.getShardingLogicTableNames(Collections.singleton("foo_tbl"))).thenReturn(Collections.singleton("foo_tbl"));
        ShardingSphereDatabase database = createDatabase(rule);
        when(rule.isBindingTablesUseShardingColumnsJoin(sqlStatementContext, Collections.singleton("foo_tbl"))).thenReturn(true);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertFalse(decider.decide(sqlStatementContext, Collections.emptyList(), mock(RuleMetaData.class), database, rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(2));
    }
    
    @Test
    void assertDecideWithAllBindingTables() {
        SelectStatementContext sqlStatementContext = createSQLStatementContext();
        when(sqlStatementContext.isContainsJoinQuery()).thenReturn(true);
        ShardingRule rule = createShardingRule();
        ShardingSphereDatabase database = createDatabase(rule);
        when(rule.isBindingTablesUseShardingColumnsJoin(sqlStatementContext, Arrays.asList("foo_tbl", "bar_tbl"))).thenReturn(true);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertFalse(decider.decide(sqlStatementContext, Collections.emptyList(), mock(RuleMetaData.class), database, rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(4));
    }
    
    @Test
    void assertDecideWithNotAllBindingTables() {
        SelectStatementContext sqlStatementContext = createSQLStatementContext();
        when(sqlStatementContext.isContainsJoinQuery()).thenReturn(true);
        ShardingRule rule = createShardingRule();
        ShardingSphereDatabase database = createDatabase(rule);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertTrue(decider.decide(sqlStatementContext, Collections.emptyList(), mock(RuleMetaData.class), database, rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(4));
    }
    
    private SelectStatementContext createSQLStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getTablesContext().getTableNames()).thenReturn(Arrays.asList("foo_tbl", "bar_tbl"));
        when(result.getSqlStatement().getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(result.getSubqueryContexts().values()).thenReturn(Collections.emptyList());
        return result;
    }
    
    private ShardingRule createShardingRule() {
        ShardingRule result = mock(ShardingRule.class, RETURNS_DEEP_STUBS);
        when(result.getShardingLogicTableNames(Arrays.asList("foo_tbl", "bar_tbl"))).thenReturn(Arrays.asList("foo_tbl", "bar_tbl"));
        DataNodeRuleAttribute dataNodeRuleAttribute = mock(DataNodeRuleAttribute.class);
        when(dataNodeRuleAttribute.getDataNodesByTableName("foo_tbl")).thenReturn(Arrays.asList(new DataNode("ds_0", (String) null, "foo_tbl"), new DataNode("ds_1", (String) null, "foo_tbl")));
        when(dataNodeRuleAttribute.getDataNodesByTableName("bar_tbl")).thenReturn(Arrays.asList(new DataNode("ds_0", (String) null, "bar_tbl"), new DataNode("ds_1", (String) null, "bar_tbl")));
        when(result.getAttributes()).thenReturn(new RuleAttributes(dataNodeRuleAttribute));
        when(result.findShardingTable("foo_tbl")).thenReturn(Optional.of(mock(ShardingTable.class)));
        when(result.findShardingTable("bar_tbl")).thenReturn(Optional.of(mock(ShardingTable.class)));
        BindingTableRule bindingTableRule = mock(BindingTableRule.class);
        when(bindingTableRule.hasLogicTable("foo_tbl")).thenReturn(true);
        when(bindingTableRule.hasLogicTable("bar_tbl")).thenReturn(true);
        when(result.findBindingTableRule("foo_tbl")).thenReturn(Optional.of(bindingTableRule));
        when(result.findBindingTableRule("bar_tbl")).thenReturn(Optional.of(bindingTableRule));
        return result;
    }
    
    private ShardingSphereDatabase createDatabase(final ShardingRule rule) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_db");
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
}
