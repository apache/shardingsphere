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

import org.apache.shardingsphere.sharding.route.engine.condition.Column;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeRouteValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RouteValue;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.SafeNumberOperationUtils;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
        Optional<RouteValue> routeValue = generator.generate(value, column, new LinkedList<>());
        assertTrue(routeValue.isPresent());
        RangeRouteValue<Integer> rangeRouteValue = (RangeRouteValue<Integer>) routeValue.get();
        assertThat(rangeRouteValue.getColumnName(), is(column.getName()));
        assertThat(rangeRouteValue.getTableName(), is(column.getTableName()));
        assertTrue(rangeRouteValue.getValueRange().contains(between));
        assertTrue(rangeRouteValue.getValueRange().contains(and));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGenerateConditionValueWithDifferentNumericType() {
        int between = 3;
        long and = 3147483647L;
        ExpressionSegment betweenSegment = new LiteralExpressionSegment(0, 0, between);
        ExpressionSegment andSegment = new LiteralExpressionSegment(0, 0, and);
        BetweenExpression value = new BetweenExpression(0, 0, null, betweenSegment, andSegment, false);
        Optional<RouteValue> routeValue = generator.generate(value, column, new LinkedList<>());
        assertTrue(routeValue.isPresent());
        RangeRouteValue<Comparable<?>> rangeRouteValue = (RangeRouteValue<Comparable<?>>) routeValue.get();
        assertThat(rangeRouteValue.getColumnName(), is(column.getName()));
        assertThat(rangeRouteValue.getTableName(), is(column.getTableName()));
        assertTrue(SafeNumberOperationUtils.safeContains(rangeRouteValue.getValueRange(), between));
        assertTrue(SafeNumberOperationUtils.safeContains(rangeRouteValue.getValueRange(), and));
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
        Optional<RouteValue> routeValue = generator.generate(value, column, new LinkedList<>());
        assertTrue(routeValue.isPresent());
        RangeRouteValue<Date> rangeRouteValue = (RangeRouteValue<Date>) routeValue.get();
        assertThat(rangeRouteValue.getColumnName(), is(column.getName()));
        assertThat(rangeRouteValue.getTableName(), is(column.getTableName()));
        assertThat(rangeRouteValue.getValueRange().lowerEndpoint(), is(date));
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
        Optional<RouteValue> routeValue = generator.generate(value, column, new LinkedList<>());
        assertTrue(routeValue.isPresent());
        RangeRouteValue<Date> rangeRouteValue = (RangeRouteValue<Date>) routeValue.get();
        assertThat(rangeRouteValue.getColumnName(), is(column.getName()));
        assertThat(rangeRouteValue.getTableName(), is(column.getTableName()));
        assertTrue(rangeRouteValue.getValueRange().upperEndpoint().before(after));
    }
}
