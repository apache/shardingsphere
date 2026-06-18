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
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexSubQuery;
import org.apache.shardingsphere.sqlfederation.compiler.rel.operator.logical.LogicalScan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushFilterIntoScanRuleTest {
    
    private final PushFilterIntoScanRule rule = PushFilterIntoScanRule.Config.DEFAULT.toRule();
    
    @Mock
    private RelOptRuleCall call;
    
    @Mock
    private LogicalScan logicalScan;
    
    @Mock
    private LogicalFilter logicalFilter;
    
    @Test
    void assertNotMatchWhenSystemSchema() {
        mockQualifiedName("shardingsphere", "t_order");
        assertFalse(rule.matches(call));
    }
    
    @Test
    void assertNotMatchWhenConditionIsRexSubQuery() {
        mockQualifiedName("public", "t_order");
        when(call.rel(0)).thenReturn(logicalFilter);
        when(logicalFilter.getCondition()).thenReturn(mock(RexSubQuery.class));
        assertFalse(rule.matches(call));
    }
    
    @Test
    void assertNotMatchWhenConditionContainsRexSubQueryOperand() {
        mockQualifiedName("public", "t_order");
        when(call.rel(0)).thenReturn(logicalFilter);
        RexCall rexCall = mock(RexCall.class);
        when(rexCall.getOperands()).thenReturn(Collections.singletonList(mock(RexSubQuery.class)));
        when(logicalFilter.getCondition()).thenReturn(rexCall);
        assertFalse(rule.matches(call));
    }
    
    @Test
    void assertNotMatchWhenConditionContainsCorrelate() {
        mockQualifiedName("public", "t_order");
        when(call.rel(0)).thenReturn(logicalFilter);
        RexNode operand = mock(RexNode.class);
        when(operand.toString()).thenReturn("$cor0");
        RexCall rexCall = mock(RexCall.class);
        when(rexCall.getOperands()).thenReturn(Collections.singletonList(operand));
        when(logicalFilter.getCondition()).thenReturn(rexCall);
        assertFalse(rule.matches(call));
    }
    
    @Test
    void assertMatchAndOnMatchWhenConditionIsRexCallWithoutCorrelate() {
        mockQualifiedName("public", "t_order");
        when(call.rel(0)).thenReturn(logicalFilter);
        RexNode operand = mock(RexNode.class);
        when(operand.toString()).thenReturn("column");
        RexCall rexCall = mock(RexCall.class);
        when(rexCall.getOperands()).thenReturn(Collections.singletonList(operand));
        when(logicalFilter.getCondition()).thenReturn(rexCall);
        assertTrue(rule.matches(call));
        rule.onMatch(call);
        verify(logicalScan).pushDown(logicalFilter);
        verify(call).transformTo(logicalScan);
    }
    
    @Test
    void assertMatchWhenConditionIsNotRexCall() {
        mockQualifiedName("public", "t_order");
        when(call.rel(0)).thenReturn(logicalFilter);
        when(logicalFilter.getCondition()).thenReturn(mock(RexNode.class));
        assertTrue(rule.matches(call));
    }
    
    private void mockQualifiedName(final String... names) {
        RelOptTable relOptTable = mock(RelOptTable.class);
        when(relOptTable.getQualifiedName()).thenReturn(Arrays.asList(names));
        when(logicalScan.getTable()).thenReturn(relOptTable);
        when(call.rel(1)).thenReturn(logicalScan);
    }
}
