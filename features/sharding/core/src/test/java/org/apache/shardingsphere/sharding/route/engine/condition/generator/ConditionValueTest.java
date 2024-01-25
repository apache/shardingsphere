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

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
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
    void assertGetValueFromLiteralExpressionSegmentOfNullValue() {
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
    void assertGetValueFromParameterMarkerSegmentOfNullValue() {
        ExpressionSegment expressionSegment = new ParameterMarkerExpressionSegment(0, 0, 0);
        ConditionValue conditionValue = new ConditionValue(expressionSegment, Collections.singletonList(null));
        assertFalse(conditionValue.getValue().isPresent());
        assertTrue(conditionValue.isNull());
        assertTrue(conditionValue.getParameterMarkerIndex().isPresent());
        assertThat(conditionValue.getParameterMarkerIndex().get(), is(0));
    }
}
