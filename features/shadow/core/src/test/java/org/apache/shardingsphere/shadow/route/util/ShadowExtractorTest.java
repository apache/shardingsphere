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

package org.apache.shardingsphere.shadow.route.util;

import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShadowExtractorTest {
    
    @Test
    void assertExtractValuesWithBinaryOperationExpression() {
        SimpleExpressionSegment leftSegment = new LiteralExpressionSegment(1, 2, "left");
        SimpleExpressionSegment rightSegment = new LiteralExpressionSegment(1, 2, "right");
        ExpressionSegment expressionSegment = new BinaryOperationExpression(1, 2, leftSegment, rightSegment, "=", "text");
        Optional<Collection<Comparable<?>>> actual = ShadowExtractor.extractValues(expressionSegment, Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get().iterator().next(), is("right"));
    }
    
    @Test
    void assertExtractValuesWithInExpression() {
        SimpleExpressionSegment leftSegment = new LiteralExpressionSegment(1, 2, "left");
        SimpleExpressionSegment rightSegment = new LiteralExpressionSegment(1, 2, "right");
        ExpressionSegment expressionSegment = new InExpression(1, 2, leftSegment, rightSegment, false);
        Optional<Collection<Comparable<?>>> actual = ShadowExtractor.extractValues(expressionSegment, Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get().iterator().next(), is("right"));
    }
    
    @Test
    void assertExtractValuesWithListExpression() {
        SimpleExpressionSegment segment0 = new LiteralExpressionSegment(1, 2, "expect0");
        SimpleExpressionSegment segment1 = new LiteralExpressionSegment(1, 2, "expect1");
        ListExpression expressionSegment = new ListExpression(1, 2);
        expressionSegment.getItems().add(segment0);
        expressionSegment.getItems().add(segment1);
        Optional<Collection<Comparable<?>>> actual = ShadowExtractor.extractValues(expressionSegment, Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get().iterator().next(), is("expect0"));
    }
    
    @Test
    void assertExtractValuesWithLiteralExpressionSegment() {
        SimpleExpressionSegment simpleExpressionSegment = new LiteralExpressionSegment(1, 2, "expected");
        Optional<Collection<Comparable<?>>> actual = ShadowExtractor.extractValues(simpleExpressionSegment, Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get().iterator().next(), is("expected"));
    }
    
    @Test
    void assertExtractValuesWithInvalidLiteralExpressionSegment() {
        SimpleExpressionSegment simpleExpressionSegment = new LiteralExpressionSegment(1, 2, new Object());
        Optional<Collection<Comparable<?>>> actual = ShadowExtractor.extractValues(simpleExpressionSegment, Collections.emptyList());
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertExtractValuesWithParameterMarkerExpressionSegment() {
        SimpleExpressionSegment simpleExpressionSegment = new ParameterMarkerExpressionSegment(1, 2, 0);
        Optional<Collection<Comparable<?>>> actual = ShadowExtractor.extractValues(simpleExpressionSegment, Collections.singletonList(10));
        assertTrue(actual.isPresent());
        assertThat(actual.get().iterator().next(), is(10));
    }
    
    @Test
    void assertExtractValuesWithInvalidParameterMarkerExpressionSegment() {
        SimpleExpressionSegment simpleExpressionSegment = new ParameterMarkerExpressionSegment(1, 2, 0);
        Optional<Collection<Comparable<?>>> actual = ShadowExtractor.extractValues(simpleExpressionSegment, Collections.singletonList(new Object()));
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertExtractValuesWithSubqueryExpressionSegment() {
        SimpleExpressionSegment simpleExpressionSegment = new SubqueryExpressionSegment(null);
        List<Object> params = Collections.singletonList(new Object());
        Optional<Collection<Comparable<?>>> actual = ShadowExtractor.extractValues(simpleExpressionSegment, params);
        assertFalse(actual.isPresent());
    }
}
