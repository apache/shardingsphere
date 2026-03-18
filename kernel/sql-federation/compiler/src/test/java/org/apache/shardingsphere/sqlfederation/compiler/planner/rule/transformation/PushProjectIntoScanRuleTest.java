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

package org.apache.shardingsphere.sqlfederation.compiler.planner.rule.transformation;

import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexSubQuery;
import org.apache.calcite.sql.SqlOperator;
import org.apache.shardingsphere.sqlfederation.compiler.rel.operator.logical.LogicalScan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PushProjectIntoScanRuleTest {
    
    private PushProjectIntoScanRule rule;
    
    @Mock
    private RelOptRuleCall call;
    
    @Mock
    private LogicalScan logicalScan;
    
    @Mock
    private LogicalProject logicalProject;
    
    @BeforeEach
    void setUp() {
        rule = PushProjectIntoScanRule.Config.DEFAULT.toRule();
    }
    
    @Test
    void assertNotMatchWhenSystemSchema() {
        mockQualifiedName("mysql", "t_order");
        when(logicalProject.getProjects()).thenReturn(Collections.singletonList(mock(RexNode.class)));
        assertFalse(rule.matches(call));
    }
    
    @Test
    void assertNotMatchWhenProjectContainsSubQuery() {
        mockQualifiedName("public", "t_order");
        when(logicalProject.getProjects()).thenReturn(Collections.singletonList(mock(RexSubQuery.class)));
        assertFalse(rule.matches(call));
    }
    
    @Test
    void assertNotMatchWhenProjectContainsCastFunction() {
        mockQualifiedName("public", "t_order");
        RexCall rexCall = mock(RexCall.class);
        SqlOperator operator = mock(SqlOperator.class);
        when(operator.getName()).thenReturn("CAST");
        when(rexCall.getOperator()).thenReturn(operator);
        when(logicalProject.getProjects()).thenReturn(Collections.singletonList(rexCall));
        assertFalse(rule.matches(call));
    }
    
    @Test
    void assertMatchWhenProjectContainsNonCallExpression() {
        mockQualifiedName("public", "t_order");
        when(logicalProject.getProjects()).thenReturn(Collections.singletonList(mock(RexNode.class)));
        assertTrue(rule.matches(call));
    }
    
    @Test
    void assertMatchAndOnMatchWhenProjectWithoutSubQueryOrCast() {
        mockQualifiedName("public", "t_order");
        RexCall rexCall = mock(RexCall.class);
        SqlOperator operator = mock(SqlOperator.class);
        when(operator.getName()).thenReturn("TRIM");
        when(rexCall.getOperator()).thenReturn(operator);
        when(logicalProject.getProjects()).thenReturn(Collections.singletonList(rexCall));
        assertTrue(rule.matches(call));
        rule.onMatch(call);
        verify(logicalScan).pushDown(logicalProject);
        verify(call).transformTo(logicalScan);
    }
    
    private void mockQualifiedName(final String... names) {
        RelOptTable relOptTable = mock(RelOptTable.class);
        when(relOptTable.getQualifiedName()).thenReturn(Arrays.asList(names));
        when(logicalScan.getTable()).thenReturn(relOptTable);
        when(call.rel(1)).thenReturn(logicalScan);
        when(call.rel(0)).thenReturn(logicalProject);
    }
}
