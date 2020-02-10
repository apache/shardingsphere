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

package org.apache.shardingsphere.core.route.router.sharding.condition.generator.impl;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.core.route.router.sharding.condition.Column;
import org.apache.shardingsphere.core.strategy.route.value.RangeRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;

public final class ConditionValueBetweenOperatorGeneratorTest {

    private ConditionValueBetweenOperatorGenerator generator = new ConditionValueBetweenOperatorGenerator();

    private Column column = new Column("shardsphere", "apache");

    @Test
    public void assertGenerateConditionValue() {
        int between = 1;
        int and = 2;
        ExpressionSegment betweenSegment = new LiteralExpressionSegment(0, 0, between);
        ExpressionSegment andSegment = new LiteralExpressionSegment(0, 0, and);
        PredicateBetweenRightValue value = new PredicateBetweenRightValue(betweenSegment, andSegment);
        Optional<RouteValue> optional = generator.generate(value, column, new LinkedList<>());
        assertTrue(optional.isPresent());
        assertTrue(optional.get() instanceof RangeRouteValue);
        RangeRouteValue<Integer> rangeRouteValue = (RangeRouteValue<Integer>) optional.get();
        assertEquals(rangeRouteValue.getColumnName(), column.getName());
        assertEquals(rangeRouteValue.getTableName(), column.getTableName());
        assertTrue(rangeRouteValue.getValueRange().contains(between));
        assertTrue(rangeRouteValue.getValueRange().contains(and));
    }

    @Test(expected = ClassCastException.class)
    public void assertGenerateErrorConditionValue() {
        int between = 1;
        ExpressionSegment betweenSegment = new LiteralExpressionSegment(0, 0, between);
        ExpressionSegment andSegment = new CommonExpressionSegment(0, 0, "now()");
        PredicateBetweenRightValue value = new PredicateBetweenRightValue(betweenSegment, andSegment);
        generator.generate(value, column, new LinkedList<>());
    }

    @Test
    public void assertGenerateOneNowConditionValue() {
        Date date = new Date();
        ExpressionSegment betweenSegment = new LiteralExpressionSegment(0, 0, date);
        ExpressionSegment andSegment = new CommonExpressionSegment(0, 0, "now()");
        PredicateBetweenRightValue value = new PredicateBetweenRightValue(betweenSegment, andSegment);
        Optional<RouteValue> optional = generator.generate(value, column, new LinkedList<>());
        assertTrue(optional.isPresent());
        assertTrue(optional.get() instanceof RangeRouteValue);
        RangeRouteValue<Date> rangeRouteValue = (RangeRouteValue<Date>) optional.get();
        assertEquals(rangeRouteValue.getColumnName(), column.getName());
        assertEquals(rangeRouteValue.getTableName(), column.getTableName());
        assertThat(rangeRouteValue.getValueRange().lowerEndpoint(), is(date));
    }

    @Test
    public void assertGenerateNowConditionValue() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        final Date after = calendar.getTime();
        ExpressionSegment betweenSegment = new CommonExpressionSegment(0, 0, "now()");
        ExpressionSegment andSegment = new CommonExpressionSegment(0, 0, "now()");
        PredicateBetweenRightValue value = new PredicateBetweenRightValue(betweenSegment, andSegment);
        Optional<RouteValue> optional = generator.generate(value, column, new LinkedList<>());
        assertTrue(optional.isPresent());
        assertTrue(optional.get() instanceof RangeRouteValue);
        RangeRouteValue<Date> rangeRouteValue = (RangeRouteValue<Date>) optional.get();
        assertEquals(rangeRouteValue.getColumnName(), column.getName());
        assertEquals(rangeRouteValue.getTableName(), column.getTableName());
        assertTrue(rangeRouteValue.getValueRange().upperEndpoint().before(after));
    }
}
