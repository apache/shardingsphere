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

package org.apache.shardingsphere.sql.parser.integrate.asserts.statement;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.groupby.GroupByAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.insert.InsertNamesAndValuesAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.orderby.OrderByAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.pagination.PaginationAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.parameter.ParameterMarkerAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.predicate.WhereAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.projection.ProjectionAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.table.AlterTableAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.root.ParserResult;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.TCLStatement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * SQL statement assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementAssert {
    
    /**
     * Assert SQL statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual SQL statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SQLStatement actual, final ParserResult expected) {
        ParameterMarkerAssert.assertCount(assertContext, actual.getParametersCount(), expected.getParameters().size());
        TableAssert.assertIs(assertContext, actual.findSQLSegments(TableSegment.class), expected.getTables());
        if (actual instanceof SelectStatement) {
            assertSelectStatement(assertContext, (SelectStatement) actual, expected);
        }
        if (actual instanceof InsertStatement) {
            assertInsertStatement(assertContext, (InsertStatement) actual, expected);
        }
        if (actual instanceof AlterTableStatement) {
            assertAlterTableStatement(assertContext, (AlterTableStatement) actual, expected);
        }
        if (actual instanceof TCLStatement) {
            assertTCLStatement((TCLStatement) actual, expected);
        }
    }
    
    private static void assertSelectStatement(final SQLCaseAssertContext assertContext, final SelectStatement actual, final ParserResult expected) {
        ProjectionAssert.assertIs(assertContext, actual.getProjections(), expected.getProjections());
        assertWhere(assertContext, actual, expected);
        assertGroupBy(assertContext, actual, expected);
        assertOrderBy(assertContext, actual, expected);
        assertLimit(assertContext, actual, expected);
    }
    
    private static void assertWhere(final SQLCaseAssertContext assertContext, final SelectStatement actual, final ParserResult expected) {
        if (null != expected.getWhere()) {
            assertTrue(assertContext.getText("Actual where segment should exist."), actual.getWhere().isPresent());
            WhereAssert.assertIs(assertContext, actual.getWhere().get(), expected.getWhere());
        } else {
            assertFalse(assertContext.getText("Actual where segment should not exist."), actual.getWhere().isPresent());
        }
    }
    
    private static void assertGroupBy(final SQLCaseAssertContext assertContext, final SelectStatement actual, final ParserResult expected) {
        if (null != expected.getGroupBy()) {
            assertTrue(assertContext.getText("Actual group by segment should exist."), actual.getGroupBy().isPresent());
            GroupByAssert.assertIs(assertContext, actual.getGroupBy().get(), expected.getGroupBy());
        } else {
            assertFalse(assertContext.getText("Actual group by segment should not exist."), actual.getGroupBy().isPresent());
        }
    }
    
    private static void assertOrderBy(final SQLCaseAssertContext assertContext, final SelectStatement actual, final ParserResult expected) {
        if (null != expected.getOrderBy()) {
            assertTrue(assertContext.getText("Actual order by segment should exist."), actual.getOrderBy().isPresent());
            OrderByAssert.assertIs(assertContext, actual.getOrderBy().get(), expected.getOrderBy());
        } else {
            assertFalse(assertContext.getText("Actual order by segment should not exist."), actual.getOrderBy().isPresent());
        }
    }
    
    private static void assertLimit(final SQLCaseAssertContext assertContext, final SelectStatement actual, final ParserResult expected) {
        Optional<LimitSegment> limitSegment = actual.findSQLSegment(LimitSegment.class);
        if (null != expected.getLimit()) {
            assertTrue(assertContext.getText("Actual limit segment should exist."), limitSegment.isPresent());
            PaginationAssert.assertOffset(assertContext, limitSegment.get().getOffset().orNull(), expected.getLimit().getOffset());
            PaginationAssert.assertRowCount(assertContext, limitSegment.get().getRowCount().orNull(), expected.getLimit().getRowCount());
            SQLSegmentAssert.assertIs(assertContext, limitSegment.get(), expected.getLimit());
        } else {
            assertFalse(assertContext.getText("Actual limit segment should not exist."), limitSegment.isPresent());
        }
    }
    
    private static void assertInsertStatement(final SQLCaseAssertContext assertContext, final InsertStatement actual, final ParserResult expected) {
        InsertNamesAndValuesAssert.assertIs(assertContext, actual, expected.getInsertColumnsAndValues());
    }
    
    private static void assertAlterTableStatement(final SQLCaseAssertContext assertContext, final AlterTableStatement actual, final ParserResult expected) {
        if (null != expected.getAlterTable()) {
            AlterTableAssert.assertIs(assertContext, actual, expected.getAlterTable());
        }
    }
    
    private static void assertTCLStatement(final TCLStatement actual, final ParserResult expected) {
        assertThat(actual.getClass().getName(), is(expected.getTclActualStatementClassType()));
        if (actual instanceof SetAutoCommitStatement) {
            assertThat(((SetAutoCommitStatement) actual).isAutoCommit(), is(expected.isAutoCommit()));
        }
    }
}
