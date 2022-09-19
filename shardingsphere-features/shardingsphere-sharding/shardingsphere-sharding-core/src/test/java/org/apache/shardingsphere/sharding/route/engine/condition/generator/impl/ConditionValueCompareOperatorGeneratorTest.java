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

package org.apache.shardingsphere.sharding.route.engine.condition.generator.impl;

import com.google.common.collect.Range;
import org.apache.shardingsphere.sharding.route.engine.condition.Column;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class ConditionValueCompareOperatorGeneratorTest {
    
    private final ConditionValueCompareOperatorGenerator generator = new ConditionValueCompareOperatorGenerator();
    
    private final Column column = new Column("id", "tbl");
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGenerateConditionValue() {
        int value = 1;
        BinaryOperationExpression rightValue = new BinaryOperationExpression(0, 0, mock(ColumnSegment.class), new LiteralExpressionSegment(0, 0, value), "=", null);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(rightValue, column, new LinkedList<>());
        assertTrue(shardingConditionValue.isPresent());
        assertTrue(((ListShardingConditionValue<Integer>) shardingConditionValue.get()).getValues().contains(value));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGenerateConditionValueWithLessThanOperator() {
        BinaryOperationExpression rightValue = new BinaryOperationExpression(0, 0, mock(ColumnSegment.class), new LiteralExpressionSegment(0, 0, 1), "<", null);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(rightValue, column, new LinkedList<>());
        assertTrue(shardingConditionValue.isPresent());
        assertTrue(Range.lessThan(1).encloses(((RangeShardingConditionValue<Integer>) shardingConditionValue.get()).getValueRange()));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGenerateConditionValueWithGreaterThanOperator() {
        BinaryOperationExpression rightValue = new BinaryOperationExpression(0, 0, mock(ColumnSegment.class), new LiteralExpressionSegment(0, 0, 1), ">", null);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(rightValue, column, new LinkedList<>());
        assertTrue(shardingConditionValue.isPresent());
        assertTrue(Range.greaterThan(1).encloses(((RangeShardingConditionValue<Integer>) shardingConditionValue.get()).getValueRange()));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGenerateConditionValueWithAtMostOperator() {
        BinaryOperationExpression rightValue = new BinaryOperationExpression(0, 0, mock(ColumnSegment.class), new LiteralExpressionSegment(0, 0, 1), "<=", null);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(rightValue, column, new LinkedList<>());
        assertTrue(shardingConditionValue.isPresent());
        assertTrue(Range.atMost(1).encloses(((RangeShardingConditionValue<Integer>) shardingConditionValue.get()).getValueRange()));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGenerateConditionValueWithAtLeastOperator() {
        BinaryOperationExpression rightValue = new BinaryOperationExpression(0, 0, mock(ColumnSegment.class), new LiteralExpressionSegment(0, 0, 1), ">=", null);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(rightValue, column, new LinkedList<>());
        assertTrue(shardingConditionValue.isPresent());
        assertTrue(Range.atLeast(1).encloses(((RangeShardingConditionValue<Integer>) shardingConditionValue.get()).getValueRange()));
    }
    
    @Test
    public void assertGenerateConditionValueWithErrorOperator() {
        BinaryOperationExpression rightValue = new BinaryOperationExpression(0, 0, mock(ColumnSegment.class), new LiteralExpressionSegment(0, 0, 1), "!=", null);
        assertFalse(generator.generate(rightValue, column, new LinkedList<>()).isPresent());
    }
    
    @Test
    public void assertGenerateConditionValueWithoutNowExpression() {
        BinaryOperationExpression rightValue = new BinaryOperationExpression(0, 0, mock(ColumnSegment.class), new CommonExpressionSegment(0, 0, "value"), "=", null);
        assertFalse(generator.generate(rightValue, column, new LinkedList<>()).isPresent());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGenerateConditionValueWithNowExpression() {
        BinaryOperationExpression rightValue = new BinaryOperationExpression(0, 0, mock(ColumnSegment.class), new LiteralExpressionSegment(0, 0, "now()"), "=", null);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(rightValue, column, new LinkedList<>());
        assertTrue(shardingConditionValue.isPresent());
        assertFalse(((ListShardingConditionValue<Integer>) shardingConditionValue.get()).getValues().isEmpty());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGenerateConditionValueWithParameter() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("id"));
        ParameterMarkerExpressionSegment right = new ParameterMarkerExpressionSegment(0, 0, 0);
        BinaryOperationExpression predicate = new BinaryOperationExpression(0, 0, left, right, "=", "id = ?");
        Optional<ShardingConditionValue> actual = generator.generate(predicate, column, Collections.singletonList(1));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ListShardingConditionValue.class));
        ListShardingConditionValue<Integer> conditionValue = (ListShardingConditionValue<Integer>) actual.get();
        assertThat(conditionValue.getTableName(), is("tbl"));
        assertThat(conditionValue.getColumnName(), is("id"));
        assertThat(conditionValue.getValues(), is(Collections.singletonList(1)));
    }
    
    @Test
    public void assertGenerateConditionValueWithoutParameter() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        ParameterMarkerExpressionSegment right = new ParameterMarkerExpressionSegment(0, 0, 0);
        BinaryOperationExpression predicate = new BinaryOperationExpression(0, 0, left, right, "=", "order_id = ?");
        Optional<ShardingConditionValue> actual = generator.generate(predicate, column, new LinkedList<>());
        assertFalse(actual.isPresent());
    }
}
