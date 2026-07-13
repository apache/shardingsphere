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

package org.apache.shardingsphere.sharding.merge.dql.groupby;

import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AggregationWrapperExpressionEvaluatorTest {
    
    @Test
    void assertEvaluateIfNullWithMemoryRowFallback() {
        FunctionSegment functionSegment = new FunctionSegment(0, 20, "IFNULL", "IFNULL(SUM(x), 0)");
        AggregationProjectionSegment aggrSegment = new AggregationProjectionSegment(7, 12, AggregationType.SUM, "SUM(x)");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(15, 15, 0);
        functionSegment.getParameters().addAll(Arrays.asList(aggrSegment, literalSegment));
        
        AggregationProjection derivedAggr = new AggregationProjection(AggregationType.SUM, aggrSegment, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggr.setIndex(1);
        
        MemoryQueryResultRow memoryRow = mock(MemoryQueryResultRow.class);
        when(memoryRow.getCell(1)).thenReturn(null);
        
        Object actual = AggregationWrapperExpressionEvaluator.evaluate(functionSegment, Collections.singletonList(derivedAggr), memoryRow, null);
        assertThat(actual, is(0));
    }
    
    @Test
    void assertEvaluateIfNullWithStreamRowSuccess() {
        FunctionSegment functionSegment = new FunctionSegment(0, 20, "IFNULL", "IFNULL(SUM(x), 0)");
        AggregationProjectionSegment aggrSegment = new AggregationProjectionSegment(7, 12, AggregationType.SUM, "SUM(x)");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(15, 15, 0);
        functionSegment.getParameters().addAll(Arrays.asList(aggrSegment, literalSegment));
        
        AggregationProjection derivedAggr = new AggregationProjection(AggregationType.SUM, aggrSegment, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggr.setIndex(2);
        
        Object actual = AggregationWrapperExpressionEvaluator.evaluate(functionSegment, Collections.singletonList(derivedAggr), Arrays.asList("other", 50), null);
        assertThat(actual, is(50));
    }
    
    @Test
    void assertEvaluateWithTypeCoercion() {
        FunctionSegment functionSegment = new FunctionSegment(0, 20, "IFNULL", "IFNULL(SUM(x), 0)");
        AggregationProjectionSegment aggrSegment = new AggregationProjectionSegment(7, 12, AggregationType.SUM, "SUM(x)");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(15, 15, 0);
        functionSegment.getParameters().addAll(Arrays.asList(aggrSegment, literalSegment));
        
        AggregationProjection derivedAggr = new AggregationProjection(AggregationType.SUM, aggrSegment, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggr.setIndex(1);
        
        MemoryQueryResultRow memoryRow = mock(MemoryQueryResultRow.class);
        when(memoryRow.getCell(1)).thenReturn(null);
        
        Object actual = AggregationWrapperExpressionEvaluator.evaluate(functionSegment, Collections.singletonList(derivedAggr), memoryRow, BigDecimal.class);
        assertThat(actual, is(new BigDecimal("0")));
    }
    
    @Test
    void assertEvaluateIfNullWithNonNullAggregation() {
        FunctionSegment functionSegment = new FunctionSegment(0, 20, "IFNULL", "IFNULL(SUM(x), 0)");
        AggregationProjectionSegment aggrSegment = new AggregationProjectionSegment(7, 12, AggregationType.SUM, "SUM(x)");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(15, 15, 0);
        functionSegment.getParameters().addAll(Arrays.asList(aggrSegment, literalSegment));
        
        AggregationProjection derivedAggr = new AggregationProjection(AggregationType.SUM, aggrSegment, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggr.setIndex(1);
        
        MemoryQueryResultRow memoryRow = mock(MemoryQueryResultRow.class);
        when(memoryRow.getCell(1)).thenReturn(100);
        
        Object actual = AggregationWrapperExpressionEvaluator.evaluate(functionSegment, Collections.singletonList(derivedAggr), memoryRow, Integer.class);
        assertThat(actual, is(100));
    }
    
    @Test
    void assertEvaluateCoalesceWithMultipleAggregations() {
        FunctionSegment functionSegment = new FunctionSegment(0, 27, "COALESCE", "COALESCE(SUM(a), SUM(b), 0)");
        
        AggregationProjectionSegment aggrSegmentA = new AggregationProjectionSegment(9, 14, AggregationType.SUM, "SUM(a)");
        AggregationProjectionSegment aggrSegmentB = new AggregationProjectionSegment(17, 22, AggregationType.SUM, "SUM(b)");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(25, 25, 0);
        
        functionSegment.getParameters().addAll(Arrays.asList(aggrSegmentA, aggrSegmentB, literalSegment));
        
        AggregationProjection derivedAggrA = new AggregationProjection(AggregationType.SUM, aggrSegmentA, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggrA.setIndex(1);
        
        AggregationProjection derivedAggrB = new AggregationProjection(AggregationType.SUM, aggrSegmentB, new IdentifierValue("EXPR_DERIVED_1"), null);
        derivedAggrB.setIndex(2);
        
        MemoryQueryResultRow memoryRow = mock(MemoryQueryResultRow.class);
        
        when(memoryRow.getCell(1)).thenReturn(null);
        when(memoryRow.getCell(2)).thenReturn(50);
        
        Object actual = AggregationWrapperExpressionEvaluator.evaluate(
                functionSegment,
                Arrays.asList(derivedAggrA, derivedAggrB),
                memoryRow,
                Integer.class);
        
        assertThat(actual, is(50));
    }
    
    @Test
    void assertEvaluateCoalesceWithAllNullAggregationsFallback() {
        FunctionSegment functionSegment = new FunctionSegment(0, 27, "COALESCE", "COALESCE(SUM(a), SUM(b), 0)");
        
        AggregationProjectionSegment aggrSegmentA = new AggregationProjectionSegment(9, 14, AggregationType.SUM, "SUM(a)");
        AggregationProjectionSegment aggrSegmentB = new AggregationProjectionSegment(17, 22, AggregationType.SUM, "SUM(b)");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(25, 25, 0);
        
        functionSegment.getParameters().addAll(Arrays.asList(aggrSegmentA, aggrSegmentB, literalSegment));
        
        AggregationProjection derivedAggrA = new AggregationProjection(AggregationType.SUM, aggrSegmentA, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggrA.setIndex(1);
        
        AggregationProjection derivedAggrB = new AggregationProjection(AggregationType.SUM, aggrSegmentB, new IdentifierValue("EXPR_DERIVED_1"), null);
        derivedAggrB.setIndex(2);
        
        MemoryQueryResultRow memoryRow = mock(MemoryQueryResultRow.class);
        
        when(memoryRow.getCell(1)).thenReturn(null);
        when(memoryRow.getCell(2)).thenReturn(null);
        
        Object actual = AggregationWrapperExpressionEvaluator.evaluate(
                functionSegment,
                Arrays.asList(derivedAggrA, derivedAggrB),
                memoryRow,
                Integer.class);
        
        assertThat(actual, is(0));
    }
    
    @Test
    void assertEvaluateCoalesceWithStreamRowSuccess() {
        FunctionSegment functionSegment = new FunctionSegment(0, 27, "COALESCE", "COALESCE(SUM(a), SUM(b), 0)");
        
        AggregationProjectionSegment aggrSegmentA = new AggregationProjectionSegment(9, 14, AggregationType.SUM, "SUM(a)");
        AggregationProjectionSegment aggrSegmentB = new AggregationProjectionSegment(17, 22, AggregationType.SUM, "SUM(b)");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(25, 25, 0);
        
        functionSegment.getParameters().addAll(Arrays.asList(aggrSegmentA, aggrSegmentB, literalSegment));
        
        AggregationProjection derivedAggrA = new AggregationProjection(AggregationType.SUM, aggrSegmentA, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggrA.setIndex(1);
        
        AggregationProjection derivedAggrB = new AggregationProjection(AggregationType.SUM, aggrSegmentB, new IdentifierValue("EXPR_DERIVED_1"), null);
        derivedAggrB.setIndex(2);
        
        Object actual = AggregationWrapperExpressionEvaluator.evaluate(
                functionSegment,
                Arrays.asList(derivedAggrA, derivedAggrB),
                Arrays.asList(null, 50),
                Integer.class);
        
        assertThat(actual, is(50));
    }
    
    @Test
    void assertEvaluateCoalesceWithMoreThanTwoAggregations() {
        FunctionSegment functionSegment = new FunctionSegment(0, 32, "COALESCE", "COALESCE(SUM(a), SUM(b), SUM(c))");
        
        AggregationProjectionSegment aggrSegmentA = new AggregationProjectionSegment(9, 14, AggregationType.SUM, "SUM(a)");
        AggregationProjectionSegment aggrSegmentB = new AggregationProjectionSegment(17, 22, AggregationType.SUM, "SUM(b)");
        AggregationProjectionSegment aggrSegmentC = new AggregationProjectionSegment(25, 30, AggregationType.SUM, "SUM(c)");
        
        functionSegment.getParameters().addAll(Arrays.asList(aggrSegmentA, aggrSegmentB, aggrSegmentC));
        
        AggregationProjection derivedAggrA = new AggregationProjection(AggregationType.SUM, aggrSegmentA, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggrA.setIndex(1);
        
        AggregationProjection derivedAggrB = new AggregationProjection(AggregationType.SUM, aggrSegmentB, new IdentifierValue("EXPR_DERIVED_1"), null);
        derivedAggrB.setIndex(2);
        
        AggregationProjection derivedAggrC = new AggregationProjection(AggregationType.SUM, aggrSegmentC, new IdentifierValue("EXPR_DERIVED_2"), null);
        derivedAggrC.setIndex(3);
        
        MemoryQueryResultRow memoryRow = mock(MemoryQueryResultRow.class);
        
        when(memoryRow.getCell(1)).thenReturn(null);
        when(memoryRow.getCell(2)).thenReturn(null);
        when(memoryRow.getCell(3)).thenReturn(99);
        
        Object actual = AggregationWrapperExpressionEvaluator.evaluate(
                functionSegment,
                Arrays.asList(derivedAggrA, derivedAggrB, derivedAggrC),
                memoryRow,
                Integer.class);
        
        assertThat(actual, is(99));
    }
    
    @Test
    void assertEvaluateCoalesceReturnsNullWhenAllAggregationsAreNull() {
        FunctionSegment functionSegment = new FunctionSegment(0, 24, "COALESCE", "COALESCE(SUM(a), SUM(b))");
        
        AggregationProjectionSegment aggrSegmentA = new AggregationProjectionSegment(9, 14, AggregationType.SUM, "SUM(a)");
        AggregationProjectionSegment aggrSegmentB = new AggregationProjectionSegment(17, 22, AggregationType.SUM, "SUM(b)");
        
        functionSegment.getParameters().addAll(Arrays.asList(aggrSegmentA, aggrSegmentB));
        
        AggregationProjection derivedAggrA = new AggregationProjection(AggregationType.SUM, aggrSegmentA, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggrA.setIndex(1);
        
        AggregationProjection derivedAggrB = new AggregationProjection(AggregationType.SUM, aggrSegmentB, new IdentifierValue("EXPR_DERIVED_1"), null);
        derivedAggrB.setIndex(2);
        
        MemoryQueryResultRow memoryRow = mock(MemoryQueryResultRow.class);
        
        when(memoryRow.getCell(1)).thenReturn(null);
        when(memoryRow.getCell(2)).thenReturn(null);
        
        Object actual = AggregationWrapperExpressionEvaluator.evaluate(
                functionSegment,
                Arrays.asList(derivedAggrA, derivedAggrB),
                memoryRow,
                Integer.class);
        
        assertNull(actual);
    }
    
    @Test
    void assertEvaluateUnsupportedFunctionThrowsException() {
        FunctionSegment functionSegment = new FunctionSegment(0, 10, "ABS", "ABS(SUM(x))");
        AggregationProjectionSegment aggrSegment = new AggregationProjectionSegment(4, 9, AggregationType.SUM, "SUM(x)");
        functionSegment.getParameters().add(aggrSegment);
        
        AggregationProjection derivedAggr = new AggregationProjection(AggregationType.SUM, aggrSegment, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggr.setIndex(1);
        
        MemoryQueryResultRow memoryRow = mock(MemoryQueryResultRow.class);
        when(memoryRow.getCell(1)).thenReturn(null);
        
        assertThrows(IllegalArgumentException.class, () -> AggregationWrapperExpressionEvaluator.evaluate(
                functionSegment,
                Collections.singletonList(derivedAggr),
                memoryRow,
                Integer.class));
    }
    
    @Test
    void assertEvaluateCoalesceWithDeduplicatedDerivedAggregation() {
        FunctionSegment functionSegment = new FunctionSegment(0, 27, "COALESCE", "COALESCE(SUM(a), SUM(a), 0)");
        
        AggregationProjectionSegment aggrSegmentA1 = new AggregationProjectionSegment(9, 14, AggregationType.SUM, "SUM(a)");
        AggregationProjectionSegment aggrSegmentA2 = new AggregationProjectionSegment(17, 22, AggregationType.SUM, "SUM(a)");
        LiteralExpressionSegment zeroLiteral = new LiteralExpressionSegment(25, 25, 0);
        
        functionSegment.getParameters().addAll(Arrays.asList(aggrSegmentA1, aggrSegmentA2, zeroLiteral));
        
        AggregationProjection derivedAggrA = new AggregationProjection(AggregationType.SUM, aggrSegmentA1, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggrA.setIndex(1);
        
        MemoryQueryResultRow memoryRow = mock(MemoryQueryResultRow.class);
        
        when(memoryRow.getCell(1)).thenReturn(null);
        
        Object actual = AggregationWrapperExpressionEvaluator.evaluate(
                functionSegment,
                Collections.singletonList(derivedAggrA),
                memoryRow,
                Integer.class);
        
        assertThat(actual, is(0));
    }
    
    @Test
    void assertEvaluateNestedFunctionWrapper() {
        FunctionSegment ifNullSegment = new FunctionSegment(0, 35, "IFNULL", "IFNULL(COALESCE(SUM(a), SUM(b)), 0)");
        FunctionSegment coalesceSegment = new FunctionSegment(7, 31, "COALESCE", "COALESCE(SUM(a), SUM(b))");
        
        AggregationProjectionSegment aggrSegmentA = new AggregationProjectionSegment(16, 21, AggregationType.SUM, "SUM(a)");
        AggregationProjectionSegment aggrSegmentB = new AggregationProjectionSegment(24, 29, AggregationType.SUM, "SUM(b)");
        coalesceSegment.getParameters().addAll(Arrays.asList(aggrSegmentA, aggrSegmentB));
        
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(34, 34, 0);
        
        ifNullSegment.getParameters().addAll(Arrays.asList(coalesceSegment, literalSegment));
        
        AggregationProjection derivedAggrA = new AggregationProjection(AggregationType.SUM, aggrSegmentA, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggrA.setIndex(1);
        
        AggregationProjection derivedAggrB = new AggregationProjection(AggregationType.SUM, aggrSegmentB, new IdentifierValue("EXPR_DERIVED_1"), null);
        derivedAggrB.setIndex(2);
        
        MemoryQueryResultRow memoryRow = mock(MemoryQueryResultRow.class);
        
        when(memoryRow.getCell(1)).thenReturn(null);
        when(memoryRow.getCell(2)).thenReturn(null);
        
        Object actual = AggregationWrapperExpressionEvaluator.evaluate(
                ifNullSegment,
                Arrays.asList(derivedAggrA, derivedAggrB),
                memoryRow,
                Integer.class);
        
        assertThat(actual, is(0));
    }
    
    @Test
    void assertEvaluateCoalesceReturnsNullWithStreamRow() {
        FunctionSegment functionSegment = new FunctionSegment(0, 24, "COALESCE", "COALESCE(SUM(a), SUM(b))");
        
        AggregationProjectionSegment aggrSegmentA = new AggregationProjectionSegment(9, 14, AggregationType.SUM, "SUM(a)");
        AggregationProjectionSegment aggrSegmentB = new AggregationProjectionSegment(17, 22, AggregationType.SUM, "SUM(b)");
        
        functionSegment.getParameters().addAll(Arrays.asList(aggrSegmentA, aggrSegmentB));
        
        AggregationProjection derivedAggrA = new AggregationProjection(AggregationType.SUM, aggrSegmentA, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggrA.setIndex(1);
        
        AggregationProjection derivedAggrB = new AggregationProjection(AggregationType.SUM, aggrSegmentB, new IdentifierValue("EXPR_DERIVED_1"), null);
        derivedAggrB.setIndex(2);
        
        Object actual = AggregationWrapperExpressionEvaluator.evaluate(
                functionSegment,
                Arrays.asList(derivedAggrA, derivedAggrB),
                Arrays.asList(null, null),
                Integer.class);
        
        assertNull(actual);
    }
    
    @Test
    void assertEvaluateCoalesceWithLiteralOnlyFallback() {
        FunctionSegment functionSegment = new FunctionSegment(0, 19, "COALESCE", "COALESCE(SUM(a), 5)");
        
        AggregationProjectionSegment aggrSegmentA = new AggregationProjectionSegment(9, 14, AggregationType.SUM, "SUM(a)");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(17, 17, 5);
        
        functionSegment.getParameters().addAll(Arrays.asList(aggrSegmentA, literalSegment));
        
        AggregationProjection derivedAggrA = new AggregationProjection(AggregationType.SUM, aggrSegmentA, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggrA.setIndex(1);
        
        MemoryQueryResultRow memoryRow = mock(MemoryQueryResultRow.class);
        when(memoryRow.getCell(1)).thenReturn(null);
        
        Object actual = AggregationWrapperExpressionEvaluator.evaluate(
                functionSegment,
                Collections.singletonList(derivedAggrA),
                memoryRow,
                Integer.class);
        
        assertThat(actual, is(5));
    }
    
    @Test
    void assertEvaluateStreamNestedFunctionWrapper() {
        FunctionSegment coalesceSegment = new FunctionSegment(0, 43, "COALESCE", "COALESCE(IFNULL(SUM(a), NULL), SUM(b), 0)");
        FunctionSegment ifNullSegment = new FunctionSegment(9, 29, "IFNULL", "IFNULL(SUM(a), NULL)");
        
        AggregationProjectionSegment aggrSegmentA = new AggregationProjectionSegment(16, 21, AggregationType.SUM, "SUM(a)");
        LiteralExpressionSegment nullLiteral = new LiteralExpressionSegment(24, 27, null);
        ifNullSegment.getParameters().addAll(Arrays.asList(aggrSegmentA, nullLiteral));
        
        AggregationProjectionSegment aggrSegmentB = new AggregationProjectionSegment(32, 37, AggregationType.SUM, "SUM(b)");
        LiteralExpressionSegment zeroLiteral = new LiteralExpressionSegment(41, 41, 0);
        
        coalesceSegment.getParameters().addAll(Arrays.asList(ifNullSegment, aggrSegmentB, zeroLiteral));
        
        AggregationProjection derivedAggrA = new AggregationProjection(AggregationType.SUM, aggrSegmentA, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggrA.setIndex(1);
        
        AggregationProjection derivedAggrB = new AggregationProjection(AggregationType.SUM, aggrSegmentB, new IdentifierValue("EXPR_DERIVED_1"), null);
        derivedAggrB.setIndex(2);
        
        Object actual = AggregationWrapperExpressionEvaluator.evaluate(
                coalesceSegment,
                Arrays.asList(derivedAggrA, derivedAggrB),
                Arrays.asList(null, 75),
                Integer.class);
        
        assertThat(actual, is(75));
    }
    
    @Test
    void assertEvaluateCoalesceShortCircuit() {
        FunctionSegment functionSegment = new FunctionSegment(0, 27, "COALESCE", "COALESCE(SUM(a), SUM(b), 0)");
        
        AggregationProjectionSegment aggrSegmentA = new AggregationProjectionSegment(9, 14, AggregationType.SUM, "SUM(a)");
        AggregationProjectionSegment aggrSegmentB = new AggregationProjectionSegment(17, 22, AggregationType.SUM, "SUM(b)");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(25, 25, 0);
        
        functionSegment.getParameters().addAll(Arrays.asList(aggrSegmentA, aggrSegmentB, literalSegment));
        
        AggregationProjection derivedAggrA = new AggregationProjection(AggregationType.SUM, aggrSegmentA, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggrA.setIndex(1);
        
        AggregationProjection derivedAggrB = new AggregationProjection(AggregationType.SUM, aggrSegmentB, new IdentifierValue("EXPR_DERIVED_1"), null);
        derivedAggrB.setIndex(2);
        
        MemoryQueryResultRow memoryRow = mock(MemoryQueryResultRow.class);
        
        when(memoryRow.getCell(1)).thenReturn(10);
        when(memoryRow.getCell(2)).thenReturn(99);
        
        Object actual = AggregationWrapperExpressionEvaluator.evaluate(
                functionSegment,
                Arrays.asList(derivedAggrA, derivedAggrB),
                memoryRow,
                Integer.class);
        
        assertThat(actual, is(10));
    }
    
    @Test
    void assertEvaluateCoalesceWithTypeCoercion() {
        FunctionSegment functionSegment = new FunctionSegment(0, 19, "COALESCE", "COALESCE(SUM(a), 0)");
        
        AggregationProjectionSegment aggrSegmentA = new AggregationProjectionSegment(9, 14, AggregationType.SUM, "SUM(a)");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(17, 17, 0);
        
        functionSegment.getParameters().addAll(Arrays.asList(aggrSegmentA, literalSegment));
        
        AggregationProjection derivedAggrA = new AggregationProjection(AggregationType.SUM, aggrSegmentA, new IdentifierValue("EXPR_DERIVED_0"), null);
        derivedAggrA.setIndex(1);
        
        MemoryQueryResultRow memoryRow = mock(MemoryQueryResultRow.class);
        
        when(memoryRow.getCell(1)).thenReturn(null);
        
        Object actual = AggregationWrapperExpressionEvaluator.evaluate(
                functionSegment,
                Collections.singletonList(derivedAggrA),
                memoryRow,
                BigDecimal.class);
        
        assertThat(actual, is(new BigDecimal("0")));
    }
}
