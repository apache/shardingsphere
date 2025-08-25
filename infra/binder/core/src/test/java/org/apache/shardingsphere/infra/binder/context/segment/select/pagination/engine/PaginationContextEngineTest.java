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
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.RowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.top.TopProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PaginationContextEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertCreatePaginationContextWithLimitSegment() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setLimit(new LimitSegment(0, 10, new NumberLiteralLimitValueSegment(0, 10, 100L),
                new NumberLiteralLimitValueSegment(11, 20, 200L)));
        PaginationContext paginationContext = new PaginationContextEngine(
                new DialectPaginationOption(false, "", false)).createPaginationContext(selectStatement, mock(ProjectionsContext.class), Collections.emptyList(), Collections.emptyList());
        assertTrue(paginationContext.getOffsetSegment().isPresent());
        assertThat(paginationContext.getOffsetSegment().get(), isA(LimitValueSegment.class));
        assertTrue(paginationContext.getRowCountSegment().isPresent());
        assertThat(paginationContext.getRowCountSegment().get(), isA(LimitValueSegment.class));
    }
    
    @Test
    void assertCreatePaginationContextWithTopSegment() {
        SelectStatement subquerySelectStatement = new SelectStatement(databaseType);
        subquerySelectStatement.setProjections(new ProjectionsSegment(0, 0));
        RowNumberValueSegment topValueSegment = new NumberLiteralRowNumberValueSegment(0, 0, 100L, false);
        subquerySelectStatement.getProjections().getProjections().add(new TopProjectionSegment(0, 10, topValueSegment, ""));
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.getProjections().getProjections().add(new SubqueryProjectionSegment(new SubquerySegment(0, 0, subquerySelectStatement, ""), ""));
        PaginationContext paginationContext = new PaginationContextEngine(
                new DialectPaginationOption(false, "", true)).createPaginationContext(selectStatement, mock(ProjectionsContext.class), Collections.emptyList(), Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertTrue(paginationContext.getRowCountSegment().isPresent());
        assertThat(paginationContext.getRowCountSegment().get(), isA(NumberLiteralRowNumberValueSegment.class));
    }
    
    @Test
    void assertCreatePaginationContextWithoutTopSegment() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        PaginationContext paginationContext = new PaginationContextEngine(
                new DialectPaginationOption(false, "", true)).createPaginationContext(selectStatement, mock(ProjectionsContext.class), Collections.emptyList(), Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    void assertCreatePaginationContextWithWhereAndRowNumberSegment() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        BinaryOperationExpression binaryOperationExpr = new BinaryOperationExpression(
                0, 5, new ColumnSegment(0, 5, new IdentifierValue("ROW_NUMBER")), new LiteralExpressionSegment(5, 10, 100), "<", "");
        WhereSegment where = new WhereSegment(0, 10, binaryOperationExpr);
        selectStatement.setWhere(where);
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.emptyList());
        PaginationContext paginationContext = new PaginationContextEngine(
                new DialectPaginationOption(true, "ROW_NUMBER", false)).createPaginationContext(selectStatement, projectionsContext, Collections.emptyList(), Collections.singletonList(where));
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertTrue(paginationContext.getRowCountSegment().isPresent());
        assertThat(paginationContext.getRowCountSegment().get(), isA(NumberLiteralRowNumberValueSegment.class));
    }
    
    @Test
    void assertCreatePaginationContextWithWhereAndWithoutRowNumberSegment() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        BinaryOperationExpression binaryOperationExpr = new BinaryOperationExpression(
                0, 5, new ColumnSegment(0, 5, new IdentifierValue("ROW_NUMBER")), new LiteralExpressionSegment(5, 10, 100), "<", "");
        WhereSegment where = new WhereSegment(0, 10, binaryOperationExpr);
        selectStatement.setWhere(where);
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.emptyList());
        PaginationContext paginationContext = new PaginationContextEngine(
                new DialectPaginationOption(false, "", false)).createPaginationContext(selectStatement, projectionsContext, Collections.emptyList(), Collections.singletonList(where));
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    void assertCreatePaginationContextWithEmpty() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.emptyList());
        PaginationContext paginationContext = new PaginationContextEngine(
                new DialectPaginationOption(false, "", false)).createPaginationContext(selectStatement, projectionsContext, Collections.emptyList(), Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
}
