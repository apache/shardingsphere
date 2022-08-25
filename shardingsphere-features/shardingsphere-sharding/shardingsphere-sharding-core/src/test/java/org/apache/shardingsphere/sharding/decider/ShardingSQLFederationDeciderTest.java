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

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.decider.context.SQLFederationDeciderContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingSQLFederationDeciderTest {
    
    @Test
    public void assertDecideWhenNotContainsShardingTable() {
        ShardingSQLFederationDecider federationDecider = new ShardingSQLFederationDecider();
        SelectStatementContext select = createStatementContext();
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getShardingLogicTableNames(Arrays.asList("t_order", "t_order_item"))).thenReturn(Collections.emptyList());
        QueryContext queryContext = new QueryContext(select, "", Collections.emptyList());
        SQLFederationDeciderContext actual = new SQLFederationDeciderContext();
        federationDecider.decide(actual, queryContext, mock(ShardingSphereDatabase.class), rule, new ConfigurationProperties(new Properties()));
        assertTrue(actual.getDataNodes().isEmpty());
        assertFalse(actual.isUseSQLFederation());
    }
    
    @Test
    public void assertDecideWhenContainsPagination() {
        SelectStatementContext select = createStatementContext();
        when(select.getPaginationContext().isHasPagination()).thenReturn(true);
        QueryContext queryContext = new QueryContext(select, "", Collections.emptyList());
        SQLFederationDeciderContext actual = new SQLFederationDeciderContext();
        ShardingSQLFederationDecider federationDecider = new ShardingSQLFederationDecider();
        federationDecider.decide(actual, queryContext, createDatabase(), createShardingRule(), new ConfigurationProperties(new Properties()));
        assertThat(actual.getDataNodes().size(), is(4));
        assertFalse(actual.isUseSQLFederation());
    }
    
    @Test
    public void assertDecideWhenContainsSubquery() {
        SelectStatementContext select = createStatementContext();
        when(select.isContainsSubquery()).thenReturn(true);
        QueryContext queryContext = new QueryContext(select, "", Collections.emptyList());
        SQLFederationDeciderContext actual = new SQLFederationDeciderContext();
        ShardingSQLFederationDecider federationDecider = new ShardingSQLFederationDecider();
        federationDecider.decide(actual, queryContext, createDatabase(), createShardingRule(), new ConfigurationProperties(new Properties()));
        assertThat(actual.getDataNodes().size(), is(4));
        assertTrue(actual.isUseSQLFederation());
    }
    
    @Test
    public void assertDecideWhenContainsHaving() {
        SelectStatementContext select = createStatementContext();
        when(select.isContainsHaving()).thenReturn(true);
        QueryContext queryContext = new QueryContext(select, "", Collections.emptyList());
        SQLFederationDeciderContext actual = new SQLFederationDeciderContext();
        ShardingSQLFederationDecider federationDecider = new ShardingSQLFederationDecider();
        federationDecider.decide(actual, queryContext, createDatabase(), createShardingRule(), new ConfigurationProperties(new Properties()));
        assertThat(actual.getDataNodes().size(), is(4));
        assertTrue(actual.isUseSQLFederation());
    }
    
    @Test
    public void assertDecideWhenContainsCombine() {
        SelectStatementContext select = createStatementContext();
        when(select.isContainsCombine()).thenReturn(true);
        QueryContext queryContext = new QueryContext(select, "", Collections.emptyList());
        SQLFederationDeciderContext actual = new SQLFederationDeciderContext();
        ShardingSQLFederationDecider federationDecider = new ShardingSQLFederationDecider();
        federationDecider.decide(actual, queryContext, createDatabase(), createShardingRule(), new ConfigurationProperties(new Properties()));
        assertThat(actual.getDataNodes().size(), is(4));
        assertTrue(actual.isUseSQLFederation());
    }
    
    @Test
    public void assertDecideWhenContainsPartialDistinctAggregation() {
        SelectStatementContext select = createStatementContext();
        when(select.isContainsPartialDistinctAggregation()).thenReturn(true);
        QueryContext queryContext = new QueryContext(select, "", Collections.emptyList());
        SQLFederationDeciderContext actual = new SQLFederationDeciderContext();
        ShardingSQLFederationDecider federationDecider = new ShardingSQLFederationDecider();
        federationDecider.decide(actual, queryContext, createDatabase(), createShardingRule(), new ConfigurationProperties(new Properties()));
        assertThat(actual.getDataNodes().size(), is(4));
        assertTrue(actual.isUseSQLFederation());
    }
    
    @Test
    public void assertDecideWhenAllTablesInSameDataSource() {
        SelectStatementContext select = createStatementContext();
        when(select.isContainsJoinQuery()).thenReturn(true);
        QueryContext queryContext = new QueryContext(select, "", Collections.emptyList());
        SQLFederationDeciderContext actual = new SQLFederationDeciderContext();
        ShardingSQLFederationDecider federationDecider = new ShardingSQLFederationDecider();
        ShardingRule shardingRule = createShardingRule();
        when(shardingRule.isAllTablesInSameDataSource(Arrays.asList("t_order", "t_order_item"))).thenReturn(true);
        federationDecider.decide(actual, queryContext, createDatabase(), shardingRule, new ConfigurationProperties(new Properties()));
        assertThat(actual.getDataNodes().size(), is(4));
        assertFalse(actual.isUseSQLFederation());
    }
    
    @Test
    public void assertDecideWhenAllTablesIsBindingTables() {
        SelectStatementContext select = createStatementContext();
        when(select.isContainsJoinQuery()).thenReturn(true);
        QueryContext queryContext = new QueryContext(select, "", Collections.emptyList());
        SQLFederationDeciderContext actual = new SQLFederationDeciderContext();
        ShardingSQLFederationDecider federationDecider = new ShardingSQLFederationDecider();
        ShardingRule shardingRule = createShardingRule();
        ShardingSphereDatabase database = createDatabase();
        when(shardingRule.isAllBindingTables(database, select, Arrays.asList("t_order", "t_order_item"))).thenReturn(true);
        federationDecider.decide(actual, queryContext, database, shardingRule, new ConfigurationProperties(new Properties()));
        assertThat(actual.getDataNodes().size(), is(4));
        assertFalse(actual.isUseSQLFederation());
    }
    
    @Test
    public void assertDecideWhenAllTablesIsNotBindingTables() {
        SelectStatementContext select = createStatementContext();
        when(select.isContainsJoinQuery()).thenReturn(true);
        QueryContext queryContext = new QueryContext(select, "", Collections.emptyList());
        SQLFederationDeciderContext actual = new SQLFederationDeciderContext();
        ShardingSQLFederationDecider federationDecider = new ShardingSQLFederationDecider();
        ShardingRule shardingRule = createShardingRule();
        ShardingSphereDatabase database = createDatabase();
        when(shardingRule.isAllBindingTables(database, select, Arrays.asList("t_order", "t_order_item"))).thenReturn(false);
        federationDecider.decide(actual, queryContext, database, shardingRule, new ConfigurationProperties(new Properties()));
        assertThat(actual.getDataNodes().size(), is(4));
        assertTrue(actual.isUseSQLFederation());
    }
    
    private static SelectStatementContext createStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getTablesContext().getTableNames()).thenReturn(Arrays.asList("t_order", "t_order_item"));
        when(result.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        return result;
    }
    
    private static ShardingSphereDatabase createDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        return result;
    }
    
    private static ShardingRule createShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        when(result.getShardingLogicTableNames(Arrays.asList("t_order", "t_order_item"))).thenReturn(Arrays.asList("t_order", "t_order_item"));
        TableRule tableRule = mock(TableRule.class);
        when(tableRule.getActualDataNodes()).thenReturn(Arrays.asList(
                new DataNode("ds_0", "t_order"), new DataNode("ds_1", "t_order"),
                new DataNode("ds_0", "t_order_item"), new DataNode("ds_1", "t_order_item")));
        when(result.findTableRule("t_order")).thenReturn(Optional.of(tableRule));
        return result;
    }
}
