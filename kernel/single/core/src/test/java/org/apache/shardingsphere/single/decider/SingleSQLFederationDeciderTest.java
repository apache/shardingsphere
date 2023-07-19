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

import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.single.rule.SingleRule;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingleSQLFederationDeciderTest {
    
    @Test
    void assertDecideWhenNotContainsSingleTable() {
        SelectStatementContext select = createStatementContext();
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertFalse(new SingleSQLFederationDecider().decide(select, Collections.emptyList(), mock(ShardingSphereRuleMetaData.class), createDatabase(), mock(SingleRule.class), includedDataNodes));
        assertTrue(includedDataNodes.isEmpty());
    }
    
    @Test
    void assertDecideWhenAllSingleTablesInSameDataSource() {
        Collection<QualifiedTable> qualifiedTables = Arrays.asList(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "t_order"), new QualifiedTable(DefaultDatabase.LOGIC_NAME, "t_order_item"));
        SingleRule rule = createSingleRule(qualifiedTables);
        when(rule.isSingleTablesInSameDataSource(qualifiedTables)).thenReturn(true);
        SelectStatementContext select = createStatementContext();
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertFalse(new SingleSQLFederationDecider().decide(select, Collections.emptyList(), mock(ShardingSphereRuleMetaData.class), createDatabase(), rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(2));
    }
    
    @Test
    void assertDecideWhenAllSingleTablesNotInSameDataSource() {
        Collection<QualifiedTable> qualifiedTables = Arrays.asList(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "t_order"), new QualifiedTable(DefaultDatabase.LOGIC_NAME, "t_order_item"));
        SingleRule rule = createSingleRule(qualifiedTables);
        when(rule.isSingleTablesInSameDataSource(qualifiedTables)).thenReturn(false);
        SelectStatementContext select = createStatementContext();
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertTrue(new SingleSQLFederationDecider().decide(select, Collections.emptyList(), mock(ShardingSphereRuleMetaData.class), createDatabase(), rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(2));
    }
    
    @Test
    void assertDecideWhenAllTablesInSameDataSource() {
        Collection<QualifiedTable> qualifiedTables = Arrays.asList(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "t_order"), new QualifiedTable(DefaultDatabase.LOGIC_NAME, "t_order_item"));
        SingleRule rule = createSingleRule(qualifiedTables);
        when(rule.isSingleTablesInSameDataSource(qualifiedTables)).thenReturn(true);
        SelectStatementContext select = createStatementContext();
        Collection<DataNode> includedDataNodes = new HashSet<>(Collections.singleton(new DataNode("ds_0", "t_user")));
        assertFalse(new SingleSQLFederationDecider().decide(select, Collections.emptyList(), mock(ShardingSphereRuleMetaData.class), createDatabase(), rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(3));
    }
    
    @Test
    void assertDecideWhenAllTablesNotInSameDataSource() {
        Collection<QualifiedTable> qualifiedTables = Arrays.asList(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "t_order"), new QualifiedTable(DefaultDatabase.LOGIC_NAME, "t_order_item"));
        SingleRule rule = createSingleRule(qualifiedTables);
        when(rule.isSingleTablesInSameDataSource(qualifiedTables)).thenReturn(true);
        SelectStatementContext select = createStatementContext();
        Collection<DataNode> includedDataNodes = new HashSet<>(Collections.singleton(new DataNode("ds_1", "t_user")));
        assertTrue(new SingleSQLFederationDecider().decide(select, Collections.emptyList(), mock(ShardingSphereRuleMetaData.class), createDatabase(), rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(3));
    }
    
    private SingleRule createSingleRule(final Collection<QualifiedTable> qualifiedTables) {
        SingleRule result = mock(SingleRule.class);
        when(result.getSingleTableNames(any())).thenReturn(qualifiedTables);
        when(result.findTableDataNode(DefaultDatabase.LOGIC_NAME, "t_order")).thenReturn(Optional.of(new DataNode("ds_0", "t_order")));
        when(result.findTableDataNode(DefaultDatabase.LOGIC_NAME, "t_order_item")).thenReturn(Optional.of(new DataNode("ds_0", "t_order_item")));
        return result;
    }
    
    private SelectStatementContext createStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        return result;
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        when(result.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        return result;
    }
}
