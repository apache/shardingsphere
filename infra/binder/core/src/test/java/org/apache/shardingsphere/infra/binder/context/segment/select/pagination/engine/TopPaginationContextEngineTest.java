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

package org.apache.shardingsphere.infra.binder.context.segment.select.pagination.engine;

import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.ParameterMarkerRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.top.TopProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopPaginationContextEngineTest {
    
    private final TopPaginationContextEngine paginationContextEngine = new TopPaginationContextEngine();
    
    @Test
    void assertCreatePaginationContextWhenRowNumberPredicateNotPresent() {
        TopProjectionSegment topProjectionSegment = new TopProjectionSegment(0, 10, null, "rowNumberAlias");
        PaginationContext paginationContext = paginationContextEngine.createPaginationContext(topProjectionSegment, Collections.emptyList(), Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    void assertCreatePaginationContextWhenRowNumberPredicatePresentAndOperatorIsGreatThan() {
        assertCreatePaginationContextWhenRowNumberPredicatePresentAndWithGivenOperator(">");
    }
    
    @Test
    void assertCreatePaginationContextWhenRowNumberPredicatePresentAndOperatorIsGreatThanEqual() {
        assertCreatePaginationContextWhenRowNumberPredicatePresentAndWithGivenOperator(">=");
    }
    
    @Test
    void assertCreatePaginationContextWhenPredicateInRightValue() {
        String name = "rowNumberAlias";
        ColumnSegment columnSegment = new ColumnSegment(0, 10, new IdentifierValue(name));
        InExpression inExpression = new InExpression(0, 0, columnSegment, new ListExpression(0, 0), false);
        PaginationContext paginationContext = paginationContextEngine.createPaginationContext(
                new TopProjectionSegment(0, 10, null, name), Collections.singletonList(inExpression), Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    void assertCreatePaginationContextWhenParameterMarkerRowNumberValueSegment() {
        String name = "rowNumberAlias";
        ColumnSegment left = new ColumnSegment(0, 10, new IdentifierValue(name));
        ParameterMarkerExpressionSegment right = new ParameterMarkerExpressionSegment(0, 10, 0);
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, left, right, ">", null);
        PaginationContext paginationContext = paginationContextEngine.createPaginationContext(
                new TopProjectionSegment(0, 10, null, name), Collections.singletonList(expression), Collections.singletonList(1));
        assertTrue(paginationContext.getOffsetSegment().isPresent());
        PaginationValueSegment paginationValueSegment = paginationContext.getOffsetSegment().get();
        assertThat(paginationValueSegment, isA(ParameterMarkerRowNumberValueSegment.class));
        ParameterMarkerRowNumberValueSegment parameterMarkerRowNumberValueSegment = (ParameterMarkerRowNumberValueSegment) paginationValueSegment;
        assertThat(parameterMarkerRowNumberValueSegment.getStartIndex(), is(0));
        assertThat(parameterMarkerRowNumberValueSegment.getStopIndex(), is(10));
        assertThat(parameterMarkerRowNumberValueSegment.getParameterIndex(), is(0));
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    private void assertCreatePaginationContextWhenRowNumberPredicatePresentAndWithGivenOperator(final String operator) {
        String name = "rowNumberAlias";
        ColumnSegment left = new ColumnSegment(0, 10, new IdentifierValue(name));
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 10, 100);
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, left, right, operator, null);
        PaginationContext paginationContext = paginationContextEngine.createPaginationContext(
                new TopProjectionSegment(0, 10, null, name), Collections.singletonList(expression), Collections.emptyList());
        assertTrue(paginationContext.getOffsetSegment().isPresent());
        PaginationValueSegment paginationValueSegment = paginationContext.getOffsetSegment().get();
        assertThat(paginationValueSegment, isA(NumberLiteralRowNumberValueSegment.class));
        NumberLiteralRowNumberValueSegment numberLiteralRowNumberValueSegment = (NumberLiteralRowNumberValueSegment) paginationValueSegment;
        assertThat(numberLiteralRowNumberValueSegment.getStartIndex(), is(0));
        assertThat(numberLiteralRowNumberValueSegment.getStopIndex(), is(10));
        assertThat(numberLiteralRowNumberValueSegment.getValue(), is(100L));
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
}
