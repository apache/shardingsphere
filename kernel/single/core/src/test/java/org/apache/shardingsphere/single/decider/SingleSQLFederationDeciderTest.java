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

package org.apache.shardingsphere.single.decider;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.decider.context.SQLFederationDeciderContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SingleSQLFederationDeciderTest {
    
    @Test
    public void assertDecideWhenNotContainsSingleTable() {
        SingleSQLFederationDecider federationDecider = new SingleSQLFederationDecider();
        SelectStatementContext select = createStatementContext();
        QueryContext queryContext = new QueryContext(select, "", Collections.emptyList());
        SQLFederationDeciderContext actual = new SQLFederationDeciderContext();
        federationDecider.decide(actual, queryContext, mock(ShardingSphereRuleMetaData.class), createDatabase(), mock(SingleRule.class), new ConfigurationProperties(new Properties()));
        assertTrue(actual.getDataNodes().isEmpty());
        assertFalse(actual.isUseSQLFederation());
    }
    
    @Test
    public void assertDecideWhenAllSingleTablesInSameDataSource() {
        Collection<QualifiedTable> qualifiedTables = Arrays.asList(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "t_order"), new QualifiedTable(DefaultDatabase.LOGIC_NAME, "t_order_item"));
        SingleRule rule = createSingleTableRule(qualifiedTables);
        when(rule.isSingleTablesInSameDataSource(qualifiedTables)).thenReturn(true);
        SelectStatementContext select = createStatementContext();
        QueryContext queryContext = new QueryContext(select, "", Collections.emptyList());
        SQLFederationDeciderContext actual = new SQLFederationDeciderContext();
        SingleSQLFederationDecider federationDecider = new SingleSQLFederationDecider();
        federationDecider.decide(actual, queryContext, mock(ShardingSphereRuleMetaData.class), createDatabase(), rule, new ConfigurationProperties(new Properties()));
        assertThat(actual.getDataNodes().size(), is(2));
        assertFalse(actual.isUseSQLFederation());
    }
    
    @Test
    public void assertDecideWhenAllSingleTablesNotInSameDataSource() {
        Collection<QualifiedTable> qualifiedTables = Arrays.asList(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "t_order"), new QualifiedTable(DefaultDatabase.LOGIC_NAME, "t_order_item"));
        SingleRule rule = createSingleTableRule(qualifiedTables);
        when(rule.isSingleTablesInSameDataSource(qualifiedTables)).thenReturn(false);
        SelectStatementContext select = createStatementContext();
        QueryContext queryContext = new QueryContext(select, "", Collections.emptyList());
        SQLFederationDeciderContext actual = new SQLFederationDeciderContext();
        SingleSQLFederationDecider federationDecider = new SingleSQLFederationDecider();
        federationDecider.decide(actual, queryContext, mock(ShardingSphereRuleMetaData.class), createDatabase(), rule, new ConfigurationProperties(new Properties()));
        assertThat(actual.getDataNodes().size(), is(2));
        assertTrue(actual.isUseSQLFederation());
    }
    
    @Test
    public void assertDecideWhenAllTablesInSameDataSource() {
        Collection<QualifiedTable> qualifiedTables = Arrays.asList(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "t_order"), new QualifiedTable(DefaultDatabase.LOGIC_NAME, "t_order_item"));
        SingleRule rule = createSingleTableRule(qualifiedTables);
        when(rule.isSingleTablesInSameDataSource(qualifiedTables)).thenReturn(true);
        SelectStatementContext select = createStatementContext();
        QueryContext queryContext = new QueryContext(select, "", Collections.emptyList());
        SQLFederationDeciderContext actual = new SQLFederationDeciderContext();
        actual.getDataNodes().add(new DataNode("ds_0", "t_user"));
        SingleSQLFederationDecider federationDecider = new SingleSQLFederationDecider();
        federationDecider.decide(actual, queryContext, mock(ShardingSphereRuleMetaData.class), createDatabase(), rule, new ConfigurationProperties(new Properties()));
        assertThat(actual.getDataNodes().size(), is(3));
        assertFalse(actual.isUseSQLFederation());
    }
    
    @Test
    public void assertDecideWhenAllTablesNotInSameDataSource() {
        Collection<QualifiedTable> qualifiedTables = Arrays.asList(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "t_order"), new QualifiedTable(DefaultDatabase.LOGIC_NAME, "t_order_item"));
        SingleRule rule = createSingleTableRule(qualifiedTables);
        when(rule.isSingleTablesInSameDataSource(qualifiedTables)).thenReturn(true);
        SelectStatementContext select = createStatementContext();
        QueryContext queryContext = new QueryContext(select, "", Collections.emptyList());
        SQLFederationDeciderContext actual = new SQLFederationDeciderContext();
        actual.getDataNodes().add(new DataNode("ds_1", "t_user"));
        SingleSQLFederationDecider federationDecider = new SingleSQLFederationDecider();
        federationDecider.decide(actual, queryContext, mock(ShardingSphereRuleMetaData.class), createDatabase(), rule, new ConfigurationProperties(new Properties()));
        assertThat(actual.getDataNodes().size(), is(3));
        assertTrue(actual.isUseSQLFederation());
    }
    
    private static SingleRule createSingleTableRule(final Collection<QualifiedTable> qualifiedTables) {
        SingleRule result = mock(SingleRule.class);
        when(result.getSingleTableNames(any())).thenReturn(qualifiedTables);
        when(result.findSingleTableDataNode(DefaultDatabase.LOGIC_NAME, "t_order")).thenReturn(Optional.of(new DataNode("ds_0", "t_order")));
        when(result.findSingleTableDataNode(DefaultDatabase.LOGIC_NAME, "t_order_item")).thenReturn(Optional.of(new DataNode("ds_0", "t_order_item")));
        return result;
    }
    
    private static SelectStatementContext createStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        return result;
    }
    
    private static ShardingSphereDatabase createDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        when(result.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        return result;
    }
}
