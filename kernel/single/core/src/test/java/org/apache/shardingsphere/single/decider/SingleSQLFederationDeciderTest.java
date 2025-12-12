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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
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
        SelectStatementContext select = mockStatementContext();
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertFalse(new SingleSQLFederationDecider().decide(select, Collections.emptyList(), mock(RuleMetaData.class), mockDatabase(), mock(SingleRule.class), includedDataNodes));
        assertTrue(includedDataNodes.isEmpty());
    }
    
    @Test
    void assertDecideWhenAllSingleTablesInSameComputeNode() {
        Collection<QualifiedTable> qualifiedTables = Arrays.asList(new QualifiedTable("foo_db", "t_order"), new QualifiedTable("foo_db", "t_order_item"));
        SingleRule rule = createSingleRule(qualifiedTables);
        SelectStatementContext select = mockStatementContext();
        Collection<DataNode> includedDataNodes = new HashSet<>();
        when(rule.isAllTablesInSameComputeNode(includedDataNodes, qualifiedTables)).thenReturn(true);
        assertFalse(new SingleSQLFederationDecider().decide(select, Collections.emptyList(), mock(RuleMetaData.class), mockDatabase(), rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(2));
    }
    
    @Test
    void assertDecideWhenAllSingleTablesNotInSameComputeNode() {
        Collection<QualifiedTable> qualifiedTables = Arrays.asList(new QualifiedTable("foo_db", "t_order"), new QualifiedTable("foo_db", "t_order_item"));
        SingleRule rule = createSingleRule(qualifiedTables);
        SelectStatementContext select = mockStatementContext();
        Collection<DataNode> includedDataNodes = new HashSet<>();
        when(rule.isAllTablesInSameComputeNode(includedDataNodes, qualifiedTables)).thenReturn(false);
        assertTrue(new SingleSQLFederationDecider().decide(select, Collections.emptyList(), mock(RuleMetaData.class), mockDatabase(), rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(2));
    }
    
    @Test
    void assertDecideWhenAllTablesInSameComputeNode() {
        Collection<QualifiedTable> qualifiedTables = Arrays.asList(new QualifiedTable("foo_db", "t_order"), new QualifiedTable("foo_db", "t_order_item"));
        SingleRule rule = createSingleRule(qualifiedTables);
        SelectStatementContext select = mockStatementContext();
        Collection<DataNode> includedDataNodes = new HashSet<>(Collections.singleton(new DataNode("ds_0", (String) null, "t_user")));
        when(rule.isAllTablesInSameComputeNode(includedDataNodes, qualifiedTables)).thenReturn(true);
        assertFalse(new SingleSQLFederationDecider().decide(select, Collections.emptyList(), mock(RuleMetaData.class), mockDatabase(), rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(3));
    }
    
    @Test
    void assertDecideWhenAllTablesNotInSameComputeNode() {
        Collection<QualifiedTable> qualifiedTables = Arrays.asList(new QualifiedTable("foo_db", "t_order"), new QualifiedTable("foo_db", "t_order_item"));
        SingleRule rule = createSingleRule(qualifiedTables);
        SelectStatementContext select = mockStatementContext();
        Collection<DataNode> includedDataNodes = new HashSet<>(Collections.singleton(new DataNode("ds_1", (String) null, "t_user")));
        when(rule.isAllTablesInSameComputeNode(includedDataNodes, qualifiedTables)).thenReturn(false);
        assertTrue(new SingleSQLFederationDecider().decide(select, Collections.emptyList(), mock(RuleMetaData.class), mockDatabase(), rule, includedDataNodes));
        assertThat(includedDataNodes.size(), is(3));
    }
    
    private SingleRule createSingleRule(final Collection<QualifiedTable> qualifiedTables) {
        SingleRule result = mock(SingleRule.class);
        when(result.getSingleTables(any())).thenReturn(qualifiedTables);
        MutableDataNodeRuleAttribute ruleAttribute = mock(MutableDataNodeRuleAttribute.class);
        when(ruleAttribute.findTableDataNode("foo_db", "t_order")).thenReturn(Optional.of(new DataNode("ds_0", (String) null, "t_order")));
        when(ruleAttribute.findTableDataNode("foo_db", "t_order_item")).thenReturn(Optional.of(new DataNode("ds_0", (String) null, "t_order_item")));
        when(result.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        return result;
    }
    
    private SelectStatementContext mockStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        return result;
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_db");
        return result;
    }
}
