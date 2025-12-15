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
import org.apache.shardingsphere.infra.metadata.database.schema.HashColumn;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.util.SafeNumberOperationUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.timeservice.config.TimestampServiceRuleConfiguration;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ConditionValueBetweenOperatorGeneratorTest {
    
    private final ConditionValueBetweenOperatorGenerator generator = new ConditionValueBetweenOperatorGenerator();
    
    private final HashColumn column = new HashColumn("id", "tbl", false);
    
    private final TimestampServiceRule timestampServiceRule = new TimestampServiceRule(new TimestampServiceRuleConfiguration("System", new Properties()));
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGenerateConditionValue() {
        int between = 1;
        int and = 2;
        ExpressionSegment betweenSegment = new LiteralExpressionSegment(0, 0, between);
        ExpressionSegment andSegment = new LiteralExpressionSegment(0, 0, and);
        BetweenExpression value = new BetweenExpression(0, 0, null, betweenSegment, andSegment, false);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(value, column, new LinkedList<>(), timestampServiceRule);
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
    void assertGenerateConditionValueWithDifferentNumericType() {
        int between = 3;
        long and = 3147483647L;
        ExpressionSegment betweenSegment = new LiteralExpressionSegment(0, 0, between);
        ExpressionSegment andSegment = new LiteralExpressionSegment(0, 0, and);
        BetweenExpression value = new BetweenExpression(0, 0, null, betweenSegment, andSegment, false);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(value, column, new LinkedList<>(), timestampServiceRule);
        assertTrue(shardingConditionValue.isPresent());
        RangeShardingConditionValue<Comparable<?>> rangeShardingConditionValue = (RangeShardingConditionValue<Comparable<?>>) shardingConditionValue.get();
        assertThat(rangeShardingConditionValue.getColumnName(), is(column.getName()));
        assertThat(rangeShardingConditionValue.getTableName(), is(column.getTableName()));
        assertTrue(SafeNumberOperationUtils.safeContains(rangeShardingConditionValue.getValueRange(), between));
        assertTrue(SafeNumberOperationUtils.safeContains(rangeShardingConditionValue.getValueRange(), and));
        assertTrue(rangeShardingConditionValue.getParameterMarkerIndexes().isEmpty());
    }
    
    @Test
    void assertGenerateErrorConditionValue() {
        int between = 1;
        ExpressionSegment betweenSegment = new LiteralExpressionSegment(0, 0, between);
        ExpressionSegment andSegment = new CommonExpressionSegment(0, 0, "now()");
        BetweenExpression value = new BetweenExpression(0, 0, null, betweenSegment, andSegment, false);
        assertThrows(ClassCastException.class, () -> generator.generate(value, column, new LinkedList<>(), timestampServiceRule));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGenerateOneNowConditionValue() {
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        ExpressionSegment betweenSegment = new LiteralExpressionSegment(0, 0, timestamp);
        ExpressionSegment andSegment = new CommonExpressionSegment(0, 0, "now()");
        BetweenExpression value = new BetweenExpression(0, 0, null, betweenSegment, andSegment, false);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(value, column, new LinkedList<>(), timestampServiceRule);
        assertTrue(shardingConditionValue.isPresent());
        RangeShardingConditionValue<Date> rangeShardingConditionValue = (RangeShardingConditionValue<Date>) shardingConditionValue.get();
        assertThat(rangeShardingConditionValue.getColumnName(), is(column.getName()));
        assertThat(rangeShardingConditionValue.getTableName(), is(column.getTableName()));
        assertThat(rangeShardingConditionValue.getValueRange().lowerEndpoint(), is(timestamp));
        assertTrue(rangeShardingConditionValue.getParameterMarkerIndexes().isEmpty());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGenerateNowConditionValue() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        final Date after = calendar.getTime();
        ExpressionSegment betweenSegment = new CommonExpressionSegment(0, 0, "now()");
        ExpressionSegment andSegment = new CommonExpressionSegment(0, 0, "now()");
        BetweenExpression value = new BetweenExpression(0, 0, null, betweenSegment, andSegment, false);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(value, column, new LinkedList<>(), timestampServiceRule);
        assertTrue(shardingConditionValue.isPresent());
        RangeShardingConditionValue<Date> rangeShardingConditionValue = (RangeShardingConditionValue<Date>) shardingConditionValue.get();
        assertThat(rangeShardingConditionValue.getColumnName(), is(column.getName()));
        assertThat(rangeShardingConditionValue.getTableName(), is(column.getTableName()));
        assertTrue(rangeShardingConditionValue.getValueRange().upperEndpoint().before(after));
        assertTrue(rangeShardingConditionValue.getParameterMarkerIndexes().isEmpty());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGenerateConditionValueWithParameter() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("id"));
        ParameterMarkerExpressionSegment between = new ParameterMarkerExpressionSegment(0, 0, 0);
        ParameterMarkerExpressionSegment and = new ParameterMarkerExpressionSegment(0, 0, 1);
        BetweenExpression predicate = new BetweenExpression(0, 0, left, between, and, false);
        Optional<ShardingConditionValue> actual = generator.generate(predicate, column, Arrays.asList(1, 2), timestampServiceRule);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(RangeShardingConditionValue.class));
        RangeShardingConditionValue<Integer> conditionValue = (RangeShardingConditionValue<Integer>) actual.get();
        assertThat(conditionValue.getTableName(), is("tbl"));
        assertThat(conditionValue.getColumnName(), is("id"));
        assertThat(conditionValue.getValueRange(), is(Range.closed(1, 2)));
        assertThat(conditionValue.getParameterMarkerIndexes(), is(Arrays.asList(0, 1)));
    }
    
    @Test
    void assertGenerateConditionValueWithoutParameter() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("id"));
        ParameterMarkerExpressionSegment between = new ParameterMarkerExpressionSegment(0, 0, 0);
        ParameterMarkerExpressionSegment and = new ParameterMarkerExpressionSegment(0, 0, 1);
        BetweenExpression predicate = new BetweenExpression(0, 0, left, between, and, false);
        Optional<ShardingConditionValue> actual = generator.generate(predicate, column, new LinkedList<>(), timestampServiceRule);
        assertFalse(actual.isPresent());
    }
}
