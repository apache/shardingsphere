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
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.SafeNumberOperationUtil;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ConditionValueBetweenOperatorGeneratorTest {
    
    private final ConditionValueBetweenOperatorGenerator generator = new ConditionValueBetweenOperatorGenerator();
    
    private final Column column = new Column("id", "tbl");
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGenerateConditionValue() {
        int between = 1;
        int and = 2;
        ExpressionSegment betweenSegment = new LiteralExpressionSegment(0, 0, between);
        ExpressionSegment andSegment = new LiteralExpressionSegment(0, 0, and);
        BetweenExpression value = new BetweenExpression(0, 0, null, betweenSegment, andSegment, false);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(value, column, new LinkedList<>());
        assertTrue(shardingConditionValue.isPresent());
        RangeShardingConditionValue<Integer> rangeShardingConditionValue = (RangeShardingConditionValue<Integer>) shardingConditionValue.get();
        assertThat(rangeShardingConditionValue.getColumnName(), is(column.getName()));
        assertThat(rangeShardingConditionValue.getTableName(), is(column.getTableName()));
        assertTrue(rangeShardingConditionValue.getValueRange().contains(between));
        assertTrue(rangeShardingConditionValue.getValueRange().contains(and));
        assertTrue(rangeShardingConditionValue.getParameterMarkerIndexes().isEmpty());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGenerateConditionValueWithDifferentNumericType() {
        int between = 3;
        long and = 3147483647L;
        ExpressionSegment betweenSegment = new LiteralExpressionSegment(0, 0, between);
        ExpressionSegment andSegment = new LiteralExpressionSegment(0, 0, and);
        BetweenExpression value = new BetweenExpression(0, 0, null, betweenSegment, andSegment, false);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(value, column, new LinkedList<>());
        assertTrue(shardingConditionValue.isPresent());
        RangeShardingConditionValue<Comparable<?>> rangeShardingConditionValue = (RangeShardingConditionValue<Comparable<?>>) shardingConditionValue.get();
        assertThat(rangeShardingConditionValue.getColumnName(), is(column.getName()));
        assertThat(rangeShardingConditionValue.getTableName(), is(column.getTableName()));
        assertTrue(SafeNumberOperationUtil.safeContains(rangeShardingConditionValue.getValueRange(), between));
        assertTrue(SafeNumberOperationUtil.safeContains(rangeShardingConditionValue.getValueRange(), and));
        assertTrue(rangeShardingConditionValue.getParameterMarkerIndexes().isEmpty());
    }
    
    @Test(expected = ClassCastException.class)
    public void assertGenerateErrorConditionValue() {
        int between = 1;
        ExpressionSegment betweenSegment = new LiteralExpressionSegment(0, 0, between);
        ExpressionSegment andSegment = new CommonExpressionSegment(0, 0, "now()");
        BetweenExpression value = new BetweenExpression(0, 0, null, betweenSegment, andSegment, false);
        generator.generate(value, column, new LinkedList<>());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGenerateOneNowConditionValue() {
        Date date = new Date();
        ExpressionSegment betweenSegment = new LiteralExpressionSegment(0, 0, date);
        ExpressionSegment andSegment = new CommonExpressionSegment(0, 0, "now()");
        BetweenExpression value = new BetweenExpression(0, 0, null, betweenSegment, andSegment, false);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(value, column, new LinkedList<>());
        assertTrue(shardingConditionValue.isPresent());
        RangeShardingConditionValue<Date> rangeShardingConditionValue = (RangeShardingConditionValue<Date>) shardingConditionValue.get();
        assertThat(rangeShardingConditionValue.getColumnName(), is(column.getName()));
        assertThat(rangeShardingConditionValue.getTableName(), is(column.getTableName()));
        assertThat(rangeShardingConditionValue.getValueRange().lowerEndpoint(), is(date));
        assertTrue(rangeShardingConditionValue.getParameterMarkerIndexes().isEmpty());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGenerateNowConditionValue() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        final Date after = calendar.getTime();
        ExpressionSegment betweenSegment = new CommonExpressionSegment(0, 0, "now()");
        ExpressionSegment andSegment = new CommonExpressionSegment(0, 0, "now()");
        BetweenExpression value = new BetweenExpression(0, 0, null, betweenSegment, andSegment, false);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(value, column, new LinkedList<>());
        assertTrue(shardingConditionValue.isPresent());
        RangeShardingConditionValue<Date> rangeShardingConditionValue = (RangeShardingConditionValue<Date>) shardingConditionValue.get();
        assertThat(rangeShardingConditionValue.getColumnName(), is(column.getName()));
        assertThat(rangeShardingConditionValue.getTableName(), is(column.getTableName()));
        assertTrue(rangeShardingConditionValue.getValueRange().upperEndpoint().before(after));
        assertTrue(rangeShardingConditionValue.getParameterMarkerIndexes().isEmpty());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGenerateConditionValueWithParameter() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("id"));
        ParameterMarkerExpressionSegment between = new ParameterMarkerExpressionSegment(0, 0, 0);
        ParameterMarkerExpressionSegment and = new ParameterMarkerExpressionSegment(0, 0, 1);
        BetweenExpression predicate = new BetweenExpression(0, 0, left, between, and, false);
        Optional<ShardingConditionValue> actual = generator.generate(predicate, column, Arrays.asList(1, 2));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(RangeShardingConditionValue.class));
        RangeShardingConditionValue<Integer> conditionValue = (RangeShardingConditionValue<Integer>) actual.get();
        assertThat(conditionValue.getTableName(), is("tbl"));
        assertThat(conditionValue.getColumnName(), is("id"));
        assertThat(conditionValue.getValueRange(), is(Range.closed(1, 2)));
        assertThat(conditionValue.getParameterMarkerIndexes(), is(Arrays.asList(0, 1)));
    }
    
    @Test
    public void assertGenerateConditionValueWithoutParameter() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("id"));
        ParameterMarkerExpressionSegment between = new ParameterMarkerExpressionSegment(0, 0, 0);
        ParameterMarkerExpressionSegment and = new ParameterMarkerExpressionSegment(0, 0, 1);
        BetweenExpression predicate = new BetweenExpression(0, 0, left, between, and, false);
        Optional<ShardingConditionValue> actual = generator.generate(predicate, column, new LinkedList<>());
        assertFalse(actual.isPresent());
    }
}
