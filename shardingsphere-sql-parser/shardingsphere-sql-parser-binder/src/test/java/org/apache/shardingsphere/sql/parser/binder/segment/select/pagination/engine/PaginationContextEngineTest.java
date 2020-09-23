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
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.top.TopProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class PaginationContextEngineTest {
    
    @Test
    public void assertCreatePaginationContextWhenLimitSegmentIsPresentForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 10, new NumberLiteralLimitValueSegment(0, 10, 100L),
                new NumberLiteralLimitValueSegment(0, 10, 100L)));
        PaginationContext paginationContext = new PaginationContextEngine().createPaginationContext(selectStatement, null, Collections.emptyList());
        assertTrue(paginationContext.getOffsetSegment().isPresent());
        assertTrue(paginationContext.getRowCountSegment().isPresent());
    }

    @Test
    public void assertCreatePaginationContextWhenLimitSegmentIsPresentForPostgreSQL() {
        PostgreSQLSelectStatement selectStatement = new PostgreSQLSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 10, new NumberLiteralLimitValueSegment(0, 10, 100L),
                new NumberLiteralLimitValueSegment(0, 10, 100L)));
        PaginationContext paginationContext = new PaginationContextEngine().createPaginationContext(selectStatement, null, Collections.emptyList());
        assertTrue(paginationContext.getOffsetSegment().isPresent());
        assertTrue(paginationContext.getRowCountSegment().isPresent());
    }

    @Test
    public void assertCreatePaginationContextWhenLimitSegmentIsPresentForSQL92() {
        SQL92SelectStatement selectStatement = new SQL92SelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 10, new NumberLiteralLimitValueSegment(0, 10, 100L),
                new NumberLiteralLimitValueSegment(0, 10, 100L)));
        PaginationContext paginationContext = new PaginationContextEngine().createPaginationContext(selectStatement, null, Collections.emptyList());
        assertTrue(paginationContext.getOffsetSegment().isPresent());
        assertTrue(paginationContext.getRowCountSegment().isPresent());
    }

    @Test
    public void assertCreatePaginationContextWhenLimitSegmentIsPresentForSQLServer() {
        SQLServerSelectStatement selectStatement = new SQLServerSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 10, new NumberLiteralLimitValueSegment(0, 10, 100L),
                new NumberLiteralLimitValueSegment(0, 10, 100L)));
        PaginationContext paginationContext = new PaginationContextEngine().createPaginationContext(selectStatement, null, Collections.emptyList());
        assertTrue(paginationContext.getOffsetSegment().isPresent());
        assertTrue(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    public void assertCreatePaginationContextWhenLimitSegmentAbsentAndTopSegmentPresent() {
        SQLServerSelectStatement subquerySelectStatement = new SQLServerSelectStatement();
        subquerySelectStatement.setProjections(new ProjectionsSegment(0, 0));
        subquerySelectStatement.getProjections().getProjections().add(new TopProjectionSegment(0, 10, null, "rowNumberAlias"));
        SQLServerSelectStatement selectStatement = new SQLServerSelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.getProjections().getProjections().add(new SubqueryProjectionSegment(new SubquerySegment(0, 0, subquerySelectStatement)));
        PaginationContext paginationContext = new PaginationContextEngine().createPaginationContext(selectStatement, null, Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    public void assertCreatePaginationContextWhenLimitSegmentTopSegmentAbsentAndWhereSegmentPresent() {
        SQLServerSelectStatement selectStatement = new SQLServerSelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setWhere(new WhereSegment(0, 10, null));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.emptyList());
        PaginationContext paginationContext = new PaginationContextEngine().createPaginationContext(selectStatement, projectionsContext, Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }

    @Test
    public void assertCreatePaginationContextWhenResultIsPaginationContextForMySQL() {
        assertCreatePaginationContextWhenResultIsPaginationContext(new MySQLSelectStatement());
    }

    @Test
    public void assertCreatePaginationContextWhenResultIsPaginationContextForOracle() {
        assertCreatePaginationContextWhenResultIsPaginationContext(new OracleSelectStatement());
    }

    @Test
    public void assertCreatePaginationContextWhenResultIsPaginationContextForPostgreSQL() {
        assertCreatePaginationContextWhenResultIsPaginationContext(new PostgreSQLSelectStatement());
    }

    @Test
    public void assertCreatePaginationContextWhenResultIsPaginationContextForSQL92() {
        assertCreatePaginationContextWhenResultIsPaginationContext(new SQL92SelectStatement());
    }

    @Test
    public void assertCreatePaginationContextWhenResultIsPaginationContextForSQLServer() {
        assertCreatePaginationContextWhenResultIsPaginationContext(new SQLServerSelectStatement());
    }
    
    private void assertCreatePaginationContextWhenResultIsPaginationContext(final SelectStatement selectStatement) {
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.emptyList());
        assertThat(new PaginationContextEngine().createPaginationContext(selectStatement, projectionsContext, Collections.emptyList()), instanceOf(PaginationContext.class));
    }
}
