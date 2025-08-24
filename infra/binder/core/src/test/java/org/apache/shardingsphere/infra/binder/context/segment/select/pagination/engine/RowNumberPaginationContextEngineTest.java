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

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.pagination.DialectPaginationOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.ParameterMarkerRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class RowNumberPaginationContextEngineTest {
    
    private static final String ROW_NUMBER_COLUMN_NAME = "rownum";
    
    private static final String ROW_NUMBER_COLUMN_ALIAS = "predicateRowNumberAlias";
    
    private final RowNumberPaginationContextEngine paginationContextEngine = new RowNumberPaginationContextEngine(new DialectPaginationOption(true, "ROWNUM", false));
    
    @Test
    void assertCreatePaginationContextWhenRowNumberAliasNotPresent() {
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.emptyList());
        PaginationContext paginationContext = paginationContextEngine.createPaginationContext(Collections.emptyList(), projectionsContext, Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    void assertCreatePaginationContextWhenRowNumberAliasIsPresentAndRowNumberPredicatesIsEmpty() {
        Projection projectionWithRowNumberAlias = new ColumnProjection(null, ROW_NUMBER_COLUMN_NAME, ROW_NUMBER_COLUMN_ALIAS, mock(DatabaseType.class));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.singleton(projectionWithRowNumberAlias));
        PaginationContext paginationContext = paginationContextEngine.createPaginationContext(Collections.emptyList(), projectionsContext, Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    void assertCreatePaginationContextWhenRowNumberAliasIsPresentAndRowNumberPredicatesNotEmptyWithLessThanOperator() {
        assertCreatePaginationContextWhenRowNumberAliasPresentAndRowNumberPredicatedNotEmptyWithGivenOperator("<");
    }
    
    @Test
    void assertCreatePaginationContextWhenRowNumberAliasIsPresentAndRowNumberPredicatesNotEmptyWithLessThanEqualOperator() {
        assertCreatePaginationContextWhenRowNumberAliasPresentAndRowNumberPredicatedNotEmptyWithGivenOperator("<=");
    }
    
    @Test
    void assertCreatePaginationContextWhenOffsetSegmentInstanceOfNumberLiteralRowNumberValueSegmentWithGreatThanOperator() {
        assertCreatePaginationContextWhenOffsetSegmentInstanceOfNumberLiteralRowNumberValueSegmentWithGivenOperator(">");
    }
    
    @Test
    void assertCreatePaginationContextWhenOffsetSegmentInstanceOfNumberLiteralRowNumberValueSegmentWithGreatThanEqualOperator() {
        assertCreatePaginationContextWhenOffsetSegmentInstanceOfNumberLiteralRowNumberValueSegmentWithGivenOperator(">=");
    }
    
    @Test
    void assertCreatePaginationContextWhenRowNumberAliasIsPresentAndRowNumberPredicatesNotEmpty() {
        Projection projectionWithRowNumberAlias = new ColumnProjection(null, ROW_NUMBER_COLUMN_NAME, ROW_NUMBER_COLUMN_ALIAS, mock(DatabaseType.class));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.singleton(projectionWithRowNumberAlias));
        AndPredicate andPredicate = new AndPredicate();
        ColumnSegment left = new ColumnSegment(0, 10, new IdentifierValue(ROW_NUMBER_COLUMN_NAME));
        BinaryOperationExpression predicateSegment = new BinaryOperationExpression(0, 0, left, null, null, null);
        andPredicate.getPredicates().add(predicateSegment);
        PaginationContext paginationContext = paginationContextEngine.createPaginationContext(Collections.emptyList(), projectionsContext, Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    void assertCreatePaginationContextWhenParameterMarkerRowNumberValueSegment() {
        Projection projectionWithRowNumberAlias = new ColumnProjection(null, ROW_NUMBER_COLUMN_NAME, ROW_NUMBER_COLUMN_ALIAS, mock(DatabaseType.class));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.singleton(projectionWithRowNumberAlias));
        ColumnSegment left = new ColumnSegment(0, 10, new IdentifierValue(ROW_NUMBER_COLUMN_NAME));
        ParameterMarkerExpressionSegment right = new ParameterMarkerExpressionSegment(0, 10, 0);
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, left, right, ">", null);
        PaginationContext paginationContext = paginationContextEngine.createPaginationContext(Collections.singletonList(expression), projectionsContext, Collections.singletonList(1));
        Optional<PaginationValueSegment> offSetSegmentPaginationValue = paginationContext.getOffsetSegment();
        assertTrue(offSetSegmentPaginationValue.isPresent());
        assertThat(offSetSegmentPaginationValue.get(), isA(ParameterMarkerRowNumberValueSegment.class));
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    private void assertCreatePaginationContextWhenRowNumberAliasPresentAndRowNumberPredicatedNotEmptyWithGivenOperator(final String operator) {
        Projection projectionWithRowNumberAlias = new ColumnProjection(null, ROW_NUMBER_COLUMN_NAME, ROW_NUMBER_COLUMN_ALIAS, mock(DatabaseType.class));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.singleton(projectionWithRowNumberAlias));
        ColumnSegment left = new ColumnSegment(0, 10, new IdentifierValue(ROW_NUMBER_COLUMN_NAME));
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 10, 100);
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, left, right, operator, null);
        PaginationContext paginationContext = paginationContextEngine.createPaginationContext(Collections.singletonList(expression), projectionsContext, Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        Optional<PaginationValueSegment> paginationValueSegment = paginationContext.getRowCountSegment();
        assertTrue(paginationValueSegment.isPresent());
        NumberLiteralRowNumberValueSegment numberLiteralRowNumberValueSegment = (NumberLiteralRowNumberValueSegment) paginationValueSegment.get();
        assertThat(numberLiteralRowNumberValueSegment.getStartIndex(), is(0));
        assertThat(numberLiteralRowNumberValueSegment.getStopIndex(), is(10));
        assertThat(numberLiteralRowNumberValueSegment.getValue(), is(100L));
    }
    
    private void assertCreatePaginationContextWhenOffsetSegmentInstanceOfNumberLiteralRowNumberValueSegmentWithGivenOperator(final String operator) {
        Projection projectionWithRowNumberAlias = new ColumnProjection(null, ROW_NUMBER_COLUMN_NAME, ROW_NUMBER_COLUMN_ALIAS, mock(DatabaseType.class));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.singleton(projectionWithRowNumberAlias));
        ColumnSegment left = new ColumnSegment(0, 10, new IdentifierValue(ROW_NUMBER_COLUMN_NAME));
        LiteralExpressionSegment right = new LiteralExpressionSegment(0, 10, 100);
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, left, right, operator, null);
        PaginationContext rowNumberPaginationContextEngine = paginationContextEngine.createPaginationContext(Collections.singletonList(expression), projectionsContext, Collections.emptyList());
        Optional<PaginationValueSegment> paginationValueSegment = rowNumberPaginationContextEngine.getOffsetSegment();
        assertTrue(paginationValueSegment.isPresent());
        PaginationValueSegment actualPaginationValueSegment = paginationValueSegment.get();
        assertThat(actualPaginationValueSegment, isA(NumberLiteralRowNumberValueSegment.class));
        assertThat(actualPaginationValueSegment.getStartIndex(), is(0));
        assertThat(actualPaginationValueSegment.getStopIndex(), is(10));
        assertThat(((NumberLiteralRowNumberValueSegment) actualPaginationValueSegment).getValue(), is(100L));
        assertFalse(rowNumberPaginationContextEngine.getRowCountSegment().isPresent());
    }
}
