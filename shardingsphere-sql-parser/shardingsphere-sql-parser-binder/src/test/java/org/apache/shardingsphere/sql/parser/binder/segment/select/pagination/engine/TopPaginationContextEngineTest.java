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

package org.apache.shardingsphere.sql.parser.binder.segment.select.pagination.engine;

import org.apache.shardingsphere.sql.parser.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum.ParameterMarkerRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.top.TopProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TopPaginationContextEngineTest {
    
    private TopPaginationContextEngine topPaginationContextEngine;
    
    @Before
    public void setUp() {
        topPaginationContextEngine = new TopPaginationContextEngine();
    }
    
    @Test
    public void assertCreatePaginationContextWhenRowNumberPredicateNotPresent() {
        TopProjectionSegment topProjectionSegment = new TopProjectionSegment(0, 10, null, "rowNumberAlias");
        Collection<AndPredicate> andPredicates = Collections.emptyList();
        List<Object> parameters = Collections.emptyList();
        PaginationContext paginationContext = topPaginationContextEngine.createPaginationContext(topProjectionSegment, null, parameters);
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    public void assertCreatePaginationContextWhenRowNumberPredicatePresentAndOperatorIsGreatThan() {
        assertCreatePaginationContextWhenRowNumberPredicatePresentAndWithGivenOperator(">");
    }
    
    @Test
    public void assertCreatePaginationContextWhenRowNumberPredicatePresentAndOperatorIsGreatThanEqual() {
        assertCreatePaginationContextWhenRowNumberPredicatePresentAndWithGivenOperator(">=");
    }
    
    @Test
    public void assertCreatePaginationContextWhenPredicateInRightValue() {
        String name = "rowNumberAlias";
        ColumnSegment columnSegment = new ColumnSegment(0, 10, new IdentifierValue(name));
        InExpression inExpression = new InExpression();
        inExpression.setLeft(new ColumnSegment(0, 10, new IdentifierValue(name)));
        inExpression.setRight(new ListExpression());
        PaginationContext paginationContext = topPaginationContextEngine.createPaginationContext(new TopProjectionSegment(0, 10, null, name), inExpression, Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    public void assertCreatePaginationContextWhenParameterMarkerRowNumberValueSegment() {
        String name = "rowNumberAlias";
        BinaryOperationExpression expression = new BinaryOperationExpression();
        expression.setOperator(">");
        expression.setRight(new ParameterMarkerExpressionSegment(0, 10, 0));
        expression.setLeft(new ColumnSegment(0, 10, new IdentifierValue(name)));
        
        PaginationContext paginationContext = topPaginationContextEngine.createPaginationContext(new TopProjectionSegment(0, 10, null, name), expression, Collections.singletonList(1));
        assertTrue(paginationContext.getOffsetSegment().isPresent());
        PaginationValueSegment paginationValueSegment = paginationContext.getOffsetSegment().get();
        assertThat(paginationValueSegment, instanceOf(ParameterMarkerRowNumberValueSegment.class));
        ParameterMarkerRowNumberValueSegment parameterMarkerRowNumberValueSegment = (ParameterMarkerRowNumberValueSegment) paginationValueSegment;
        assertThat(parameterMarkerRowNumberValueSegment.getStartIndex(), is(0));
        assertThat(parameterMarkerRowNumberValueSegment.getStopIndex(), is(10));
        assertThat(parameterMarkerRowNumberValueSegment.getParameterIndex(), is(0));
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    private void assertCreatePaginationContextWhenRowNumberPredicatePresentAndWithGivenOperator(final String operator) {
        String name = "rowNumberAlias";
        BinaryOperationExpression expression = new BinaryOperationExpression();
        expression.setOperator(operator);
        expression.setRight(new LiteralExpressionSegment(0, 10, 100));
        expression.setLeft(new ColumnSegment(0, 10, new IdentifierValue(name)));
        
        PaginationContext paginationContext = topPaginationContextEngine.createPaginationContext(new TopProjectionSegment(0, 10, null, name), expression, Collections.emptyList());
        assertTrue(paginationContext.getOffsetSegment().isPresent());
        PaginationValueSegment paginationValueSegment = paginationContext.getOffsetSegment().get();
        assertThat(paginationValueSegment, instanceOf(NumberLiteralRowNumberValueSegment.class));
        NumberLiteralRowNumberValueSegment numberLiteralRowNumberValueSegment = (NumberLiteralRowNumberValueSegment) paginationValueSegment;
        assertThat(numberLiteralRowNumberValueSegment.getStartIndex(), is(0));
        assertThat(numberLiteralRowNumberValueSegment.getStopIndex(), is(10));
        assertThat(numberLiteralRowNumberValueSegment.getValue(), is(100L));
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
}
