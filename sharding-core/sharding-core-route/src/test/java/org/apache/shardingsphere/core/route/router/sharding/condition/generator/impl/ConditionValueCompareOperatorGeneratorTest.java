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
import com.google.common.collect.Range;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.core.route.router.sharding.condition.Column;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RangeRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ConditionValueCompareOperatorGeneratorTest {

    private ConditionValueCompareOperatorGenerator generator = new ConditionValueCompareOperatorGenerator();

    private Column column = new Column("shardsphere", "apache");

    @Test
    public void assertGenerateConditionValue() {
        int value = 1;
        PredicateCompareRightValue rightValue = new PredicateCompareRightValue("=", new LiteralExpressionSegment(0, 0, value));
        Optional<RouteValue> routeValueOptional = generator.generate(rightValue, column, new LinkedList<>());
        assertTrue(routeValueOptional.isPresent());
        assertTrue(routeValueOptional.get() instanceof ListRouteValue);
        ListRouteValue<Integer> listRouteValue = (ListRouteValue<Integer>) routeValueOptional.get();
        assertTrue(listRouteValue.getValues().contains(value));
    }

    @Test
    public void assertGenerateConditionValueWithLessThanOperator() {
        PredicateCompareRightValue rightValue = new PredicateCompareRightValue("<", new LiteralExpressionSegment(0, 0, 1));
        Optional<RouteValue> routeValueOptional = generator.generate(rightValue, column, new LinkedList<>());
        assertTrue(routeValueOptional.isPresent());
        assertTrue(routeValueOptional.get() instanceof RangeRouteValue);
        RangeRouteValue<Integer> rangeRouteValue = (RangeRouteValue<Integer>) routeValueOptional.get();
        assertTrue(Range.lessThan(1).encloses(rangeRouteValue.getValueRange()));
    }

    @Test
    public void assertGenerateConditionValueWithGreaterThanOperator() {
        PredicateCompareRightValue rightValue = new PredicateCompareRightValue(">", new LiteralExpressionSegment(0, 0, 1));
        Optional<RouteValue> routeValueOptional = generator.generate(rightValue, column, new LinkedList<>());
        assertTrue(routeValueOptional.isPresent());
        assertTrue(routeValueOptional.get() instanceof RangeRouteValue);
        RangeRouteValue<Integer> rangeRouteValue = (RangeRouteValue<Integer>) routeValueOptional.get();
        assertTrue(Range.greaterThan(1).encloses(rangeRouteValue.getValueRange()));
    }

    @Test
    public void assertGenerateConditionValueWithAtMostOperator() {
        PredicateCompareRightValue rightValue = new PredicateCompareRightValue("<=", new LiteralExpressionSegment(0, 0, 1));
        Optional<RouteValue> routeValueOptional = generator.generate(rightValue, column, new LinkedList<>());
        assertTrue(routeValueOptional.isPresent());
        assertTrue(routeValueOptional.get() instanceof RangeRouteValue);
        RangeRouteValue<Integer> rangeRouteValue = (RangeRouteValue<Integer>) routeValueOptional.get();
        assertTrue(Range.atMost(1).encloses(rangeRouteValue.getValueRange()));
    }

    @Test
    public void assertGenerateConditionValueWithAtLeastOperator() {
        PredicateCompareRightValue rightValue = new PredicateCompareRightValue(">=", new LiteralExpressionSegment(0, 0, 1));
        Optional<RouteValue> routeValueOptional = generator.generate(rightValue, column, new LinkedList<>());
        assertTrue(routeValueOptional.isPresent());
        assertTrue(routeValueOptional.get() instanceof RangeRouteValue);
        RangeRouteValue<Integer> rangeRouteValue = (RangeRouteValue<Integer>) routeValueOptional.get();
        assertTrue(Range.atLeast(1).encloses(rangeRouteValue.getValueRange()));
    }

    @Test
    public void assertGenerateConditionValueWithErrorOperator() {
        PredicateCompareRightValue rightValue = new PredicateCompareRightValue("!=", new LiteralExpressionSegment(0, 0, 1));
        Optional<RouteValue> routeValueOptional = generator.generate(rightValue, column, new LinkedList<>());
        assertFalse(routeValueOptional.isPresent());
    }

    @Test
    public void assertGenerateConditionValueWithoutNowExpression() {
        PredicateCompareRightValue rightValue = new PredicateCompareRightValue("=", new CommonExpressionSegment(0, 0, "value"));
        Optional<RouteValue> routeValueOptional = generator.generate(rightValue, column, new LinkedList<>());
        assertFalse(routeValueOptional.isPresent());
    }

    @Test
    public void assertGenerateConditionValueWithNowExpression() {
        PredicateCompareRightValue rightValue = new PredicateCompareRightValue("=", new CommonExpressionSegment(0, 0, "now()"));
        Optional<RouteValue> routeValueOptional = generator.generate(rightValue, column, new LinkedList<>());
        assertTrue(routeValueOptional.isPresent());
        assertTrue(routeValueOptional.get() instanceof ListRouteValue);
        ListRouteValue<Integer> listRouteValue = (ListRouteValue<Integer>) routeValueOptional.get();
        assertFalse(listRouteValue.getValues().isEmpty());
    }
}
