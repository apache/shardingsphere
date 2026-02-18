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
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.statement.core.enums.JoinType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationDecider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingleSQLFederationDeciderTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final SingleSQLFederationDecider decider = (SingleSQLFederationDecider) OrderedSPILoader.getServicesByClass(
            SQLFederationDecider.class, Collections.singleton(SingleRule.class)).get(SingleRule.class);
    
    @Test
    void assertDecideWhenSingleTablesContainView() {
        Collection<QualifiedTable> qualifiedTables = Collections.singletonList(new QualifiedTable("foo_db", "foo_tbl"));
        SingleRule rule = createSingleRule(qualifiedTables, true);
        SelectStatementContext selectStatementContext = createSelectStatementContext(new SelectStatement(DATABASE_TYPE));
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertTrue(decider.decide(selectStatementContext, Collections.emptyList(), mock(RuleMetaData.class), createDatabase(true), rule, includedDataNodes));
        assertTrue(includedDataNodes.isEmpty());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertDecideWithComputeNodeResultArguments")
    void assertDecideWithComputeNodeResult(final String name, final boolean allTablesInSameComputeNode, final boolean hasOrderDataNode, final boolean expectedDecideResult,
                                           final int expectedIncludedDataNodeSize) {
        Collection<QualifiedTable> qualifiedTables = Collections.singletonList(new QualifiedTable("foo_db", "foo_tbl"));
        SelectStatementContext selectStatementContext = createSelectStatementContext(new SelectStatement(DATABASE_TYPE));
        SingleRule rule = createSingleRule(qualifiedTables, hasOrderDataNode);
        when(rule.isAllTablesInSameComputeNode(any(), any())).thenReturn(allTablesInSameComputeNode);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        assertThat(name, decider.decide(selectStatementContext, Collections.emptyList(), mock(RuleMetaData.class), createDatabase(false), rule, includedDataNodes), is(expectedDecideResult));
        assertThat(includedDataNodes.size(), is(expectedIncludedDataNodeSize));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertDecideWithIncludedDataNodesAndJoinTypeArguments")
    void assertDecideWithIncludedDataNodesAndJoinType(final String name, final SelectStatement sqlStatement, final boolean expectedDecideResult, final int expectedIncludedDataNodeSize) {
        Collection<QualifiedTable> qualifiedTables = Collections.singletonList(new QualifiedTable("foo_db", "foo_tbl"));
        SingleRule rule = createSingleRule(qualifiedTables, true);
        when(rule.isAllTablesInSameComputeNode(any(), any())).thenReturn(true);
        SelectStatementContext selectStatementContext = createSelectStatementContext(sqlStatement);
        Collection<DataNode> includedDataNodes = new HashSet<>(Collections.singleton(new DataNode("ds_1", (String) null, "t_user")));
        assertThat(name, decider.decide(selectStatementContext, Collections.emptyList(), mock(RuleMetaData.class), createDatabase(false), rule, includedDataNodes), is(expectedDecideResult));
        assertThat(includedDataNodes.size(), is(expectedIncludedDataNodeSize));
    }
    
    @Test
    void assertDecideWhenExplainStatementContext() {
        ExplainStatementContext explainStatementContext = mock(ExplainStatementContext.class);
        SelectStatementContext explainableSQLStatementContext = createSelectStatementContext(new SelectStatement(DATABASE_TYPE));
        when(explainStatementContext.getExplainableSQLStatementContext()).thenReturn(explainableSQLStatementContext);
        Collection<DataNode> includedDataNodes = new HashSet<>();
        SingleRule rule = createSingleRule(Collections.emptyList(), true);
        assertFalse(decider.decide(explainStatementContext, Collections.emptyList(), mock(RuleMetaData.class), createDatabase(false), rule, includedDataNodes));
        assertTrue(includedDataNodes.isEmpty());
    }
    
    @Test
    void assertDecideWhenSQLStatementContextNotSupported() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(new SelectStatement(DATABASE_TYPE));
        UnsupportedSQLOperationException actual = assertThrows(UnsupportedSQLOperationException.class,
                () -> decider.decide(sqlStatementContext, Collections.emptyList(), mock(RuleMetaData.class), createDatabase(false), mock(SingleRule.class), new HashSet<>()));
        assertThat(actual.getMessage(), is("Unsupported SQL operation: unsupported SQL statement SelectStatement in sql federation."));
    }
    
    private SingleRule createSingleRule(final Collection<QualifiedTable> qualifiedTables, final boolean hasOrderDataNode) {
        SingleRule result = mock(SingleRule.class);
        when(result.getSingleTables(any())).thenReturn(qualifiedTables);
        when(result.getQualifiedTables(any(), any())).thenReturn(qualifiedTables);
        MutableDataNodeRuleAttribute ruleAttribute = mock(MutableDataNodeRuleAttribute.class);
        when(ruleAttribute.findTableDataNode("foo_db", "foo_tbl")).thenReturn(hasOrderDataNode ? Optional.of(new DataNode("ds_0", (String) null, "foo_tbl")) : Optional.empty());
        when(result.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        return result;
    }
    
    private SelectStatementContext createSelectStatementContext(final SelectStatement sqlStatement) {
        SelectStatementContext result = mock(SelectStatementContext.class);
        when(result.getSqlStatement()).thenReturn(sqlStatement);
        return result;
    }
    
    private ShardingSphereDatabase createDatabase(final boolean containsView) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_db");
        when(result.getSchema("foo_db").containsView("foo_tbl")).thenReturn(containsView);
        return result;
    }
    
    private static Stream<Arguments> assertDecideWithComputeNodeResultArguments() {
        return Stream.of(
                Arguments.of("all tables in same compute node with table data node", true, true, false, 1),
                Arguments.of("all tables not in same compute node with table data node", false, true, true, 1),
                Arguments.of("all tables in same compute node without table data node", true, false, false, 0));
    }
    
    private static Stream<Arguments> assertDecideWithIncludedDataNodesAndJoinTypeArguments() {
        return Stream.of(
                Arguments.of("select statement without from segment", new SelectStatement(DATABASE_TYPE), false, 2),
                Arguments.of("select statement with non join from segment", createSelectStatementWithNonJoinFrom(), false, 2),
                Arguments.of("select statement with inner join", createSelectStatementWithJoinType(JoinType.INNER.name()), false, 2),
                Arguments.of("select statement with comma join", createSelectStatementWithJoinType(JoinType.COMMA.name()), false, 2),
                Arguments.of("select statement with left join", createSelectStatementWithJoinType(JoinType.LEFT.name()), true, 1));
    }
    
    private static SelectStatement createSelectStatementWithNonJoinFrom() {
        SelectStatement result = new SelectStatement(DATABASE_TYPE);
        result.setFrom(mock(TableSegment.class));
        return result;
    }
    
    private static SelectStatement createSelectStatementWithJoinType(final String joinType) {
        SelectStatement result = new SelectStatement(DATABASE_TYPE);
        JoinTableSegment joinTableSegment = new JoinTableSegment();
        joinTableSegment.setJoinType(joinType);
        result.setFrom(joinTableSegment);
        return result;
    }
}
