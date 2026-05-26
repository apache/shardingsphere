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

package org.apache.shardingsphere.sharding.route.engine.condition.generator;

import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionValueTest {
    
    @Test
    void assertGetValueFromLiteralExpressionSegment() {
        ExpressionSegment expressionSegment = new LiteralExpressionSegment(0, 0, "shardingsphere");
        ConditionValue conditionValue = new ConditionValue(expressionSegment, new LinkedList<>());
        assertTrue(conditionValue.getValue().isPresent());
        assertThat(conditionValue.getValue().get(), is("shardingsphere"));
        assertFalse(conditionValue.getParameterMarkerIndex().isPresent());
    }
    
    @Test
    void assertGetNullValueFromLiteralExpressionSegment() {
        ExpressionSegment expressionSegment = new LiteralExpressionSegment(0, 0, null);
        ConditionValue conditionValue = new ConditionValue(expressionSegment, new LinkedList<>());
        assertFalse(conditionValue.getValue().isPresent());
        assertTrue(conditionValue.isNull());
        assertFalse(conditionValue.getParameterMarkerIndex().isPresent());
    }
    
    @Test
    void assertGetValueFromParameterMarkerSegment() {
        ExpressionSegment expressionSegment = new ParameterMarkerExpressionSegment(0, 0, 0);
        ConditionValue conditionValue = new ConditionValue(expressionSegment, Collections.singletonList(1));
        assertTrue(conditionValue.getValue().isPresent());
        assertThat(conditionValue.getValue().get(), is(1));
        assertTrue(conditionValue.getParameterMarkerIndex().isPresent());
        assertThat(conditionValue.getParameterMarkerIndex().get(), is(0));
    }
    
    @Test
    void assertGetNullValueFromParameterMarkerSegment() {
        List<Object> params = Arrays.asList(1, null);
        ConditionValue conditionValue = new ConditionValue(new ParameterMarkerExpressionSegment(0, 0, 0), params);
        assertTrue(conditionValue.getValue().isPresent());
        assertThat(conditionValue.getValue().get(), is(1));
        assertTrue(conditionValue.getParameterMarkerIndex().isPresent());
        assertThat(conditionValue.getParameterMarkerIndex().get(), is(0));
        conditionValue = new ConditionValue(new ParameterMarkerExpressionSegment(0, 0, 1), params);
        assertFalse(conditionValue.getValue().isPresent());
        assertTrue(conditionValue.isNull());
        assertTrue(conditionValue.getParameterMarkerIndex().isPresent());
        assertThat(conditionValue.getParameterMarkerIndex().get(), is(1));
    }
    
    @Test
    void assertGetValueFromTypeCastOverIntegerParameterMarker() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        ConditionValue conditionValue = new ConditionValue(typeCast, Collections.singletonList(7));
        assertTrue(conditionValue.getValue().isPresent());
        assertThat(conditionValue.getValue().get(), is(7));
        assertTrue(conditionValue.getParameterMarkerIndex().isPresent());
        assertThat(conditionValue.getParameterMarkerIndex().get(), is(0));
    }
    
    @Test
    void assertGetValueFromTypeCastOverStringParameterMarker() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        ConditionValue conditionValue = new ConditionValue(typeCast, Collections.singletonList("1"));
        assertTrue(conditionValue.getValue().isPresent());
        assertThat(conditionValue.getValue().get(), is(1));
    }
    
    @Test
    void assertGetValueFromTypeCastOverBigDecimalRoundsHalfEven() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        ConditionValue conditionValue = new ConditionValue(typeCast, Collections.singletonList(new BigDecimal("1.5")));
        assertTrue(conditionValue.getValue().isPresent());
        assertThat(conditionValue.getValue().get(), is(2));
    }
    
    @Test
    void assertGetValueFromTypeCastOverflowReturnsEmpty() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        ConditionValue conditionValue = new ConditionValue(typeCast, Collections.singletonList(new BigDecimal("2147483648")));
        assertFalse(conditionValue.getValue().isPresent());
    }
    
    @Test
    void assertGetValueFromTypeCastUnparseableReturnsEmpty() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        ConditionValue conditionValue = new ConditionValue(typeCast, Collections.singletonList("abc"));
        assertFalse(conditionValue.getValue().isPresent());
    }
    
    @Test
    void assertGetValueFromTypeCastOverLiteralRoutesByCastedValue() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "'1'::int4", new LiteralExpressionSegment(0, 10, "1"), "int4");
        ConditionValue conditionValue = new ConditionValue(typeCast, Collections.emptyList());
        assertTrue(conditionValue.getValue().isPresent());
        assertThat(conditionValue.getValue().get(), is(1));
        assertFalse(conditionValue.getParameterMarkerIndex().isPresent());
    }
    
    @Test
    void assertGetValueFromNestedTypeCastAppliesCastsInsideOut() {
        TypeCastExpression inner = new TypeCastExpression(0, 5, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        TypeCastExpression outer = new TypeCastExpression(0, 12, "?::int4::text", inner, "text");
        ConditionValue conditionValue = new ConditionValue(outer, Collections.singletonList(42));
        assertTrue(conditionValue.getValue().isPresent());
        assertThat(conditionValue.getValue().get(), is("42"));
    }
    
    @Test
    void assertGetNullValueFromTypeCastOverNullParameter() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        ConditionValue conditionValue = new ConditionValue(typeCast, Collections.singletonList(null));
        assertFalse(conditionValue.getValue().isPresent());
        assertTrue(conditionValue.isNull());
    }
}
