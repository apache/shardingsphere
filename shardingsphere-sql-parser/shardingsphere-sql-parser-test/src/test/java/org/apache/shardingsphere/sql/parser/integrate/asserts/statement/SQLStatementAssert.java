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
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertMessage;
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
     * @param assertMessage assert message
     * @param actual actual SQL statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertMessage assertMessage, final SQLStatement actual, final ParserResult expected) {
        ParameterMarkerAssert.assertCount(assertMessage, actual.getParametersCount(), expected.getParameters().size());
        TableAssert.assertIs(assertMessage, actual.findSQLSegments(TableSegment.class), expected.getTables());
        if (actual instanceof SelectStatement) {
            assertSelectStatement(assertMessage, (SelectStatement) actual, expected);
        }
        if (actual instanceof InsertStatement) {
            assertInsertStatement(assertMessage, (InsertStatement) actual, expected);
        }
        if (actual instanceof AlterTableStatement) {
            assertAlterTableStatement(assertMessage, (AlterTableStatement) actual, expected);
        }
        if (actual instanceof TCLStatement) {
            assertTCLStatement((TCLStatement) actual, expected);
        }
    }
    
    private static void assertSelectStatement(final SQLCaseAssertMessage assertMessage, final SelectStatement actual, final ParserResult expected) {
        ProjectionAssert.assertIs(assertMessage, actual.getProjections(), expected.getProjections());
        assertWhere(assertMessage, actual, expected);
        assertGroupBy(assertMessage, actual, expected);
        assertOrderBy(assertMessage, actual, expected);
        assertLimit(assertMessage, actual, expected);
    }
    
    private static void assertWhere(final SQLCaseAssertMessage assertMessage, final SelectStatement actual, final ParserResult expected) {
        if (null != expected.getWhere()) {
            assertTrue(assertMessage.getText("Actual where segment should exist."), actual.getWhere().isPresent());
            WhereAssert.assertIs(assertMessage, actual.getWhere().get(), expected.getWhere());
        } else {
            assertFalse(assertMessage.getText("Actual where segment should not exist."), actual.getWhere().isPresent());
        }
    }
    
    private static void assertGroupBy(final SQLCaseAssertMessage assertMessage, final SelectStatement actual, final ParserResult expected) {
        if (null != expected.getGroupBy()) {
            assertTrue(assertMessage.getText("Actual group by segment should exist."), actual.getGroupBy().isPresent());
            GroupByAssert.assertIs(assertMessage, actual.getGroupBy().get(), expected.getGroupBy());
        } else {
            assertFalse(assertMessage.getText("Actual group by segment should not exist."), actual.getGroupBy().isPresent());
        }
    }
    
    private static void assertOrderBy(final SQLCaseAssertMessage assertMessage, final SelectStatement actual, final ParserResult expected) {
        if (null != expected.getOrderBy()) {
            assertTrue(assertMessage.getText("Actual order by segment should exist."), actual.getOrderBy().isPresent());
            OrderByAssert.assertIs(assertMessage, actual.getOrderBy().get(), expected.getOrderBy());
        } else {
            assertFalse(assertMessage.getText("Actual order by segment should not exist."), actual.getOrderBy().isPresent());
        }
    }
    
    private static void assertLimit(final SQLCaseAssertMessage assertMessage, final SelectStatement actual, final ParserResult expected) {
        Optional<LimitSegment> limitSegment = actual.findSQLSegment(LimitSegment.class);
        if (null != expected.getLimit()) {
            assertTrue(assertMessage.getText("Actual limit segment should exist."), limitSegment.isPresent());
            PaginationAssert.assertOffset(assertMessage, limitSegment.get().getOffset().orNull(), expected.getLimit().getOffset());
            PaginationAssert.assertRowCount(assertMessage, limitSegment.get().getRowCount().orNull(), expected.getLimit().getRowCount());
            SQLSegmentAssert.assertIs(assertMessage, limitSegment.get(), expected.getLimit());
        } else {
            assertFalse(assertMessage.getText("Actual limit segment should not exist."), limitSegment.isPresent());
        }
    }
    
    private static void assertInsertStatement(final SQLCaseAssertMessage assertMessage, final InsertStatement actual, final ParserResult expected) {
        InsertNamesAndValuesAssert.assertIs(assertMessage, actual, expected.getInsertColumnsAndValues());
    }
    
    private static void assertAlterTableStatement(final SQLCaseAssertMessage assertMessage, final AlterTableStatement actual, final ParserResult expected) {
        if (null != expected.getAlterTable()) {
            AlterTableAssert.assertIs(assertMessage, actual, expected.getAlterTable());
        }
    }
    
    private static void assertTCLStatement(final TCLStatement actual, final ParserResult expected) {
        assertThat(actual.getClass().getName(), is(expected.getTclActualStatementClassType()));
        if (actual instanceof SetAutoCommitStatement) {
            assertThat(((SetAutoCommitStatement) actual).isAutoCommit(), is(expected.isAutoCommit()));
        }
    }
}
