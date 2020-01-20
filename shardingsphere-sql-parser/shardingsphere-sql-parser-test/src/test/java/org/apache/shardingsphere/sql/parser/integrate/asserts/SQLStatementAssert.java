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

package org.apache.shardingsphere.sql.parser.integrate.asserts;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.groupby.GroupByAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.insert.InsertNamesAndValuesAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.orderby.OrderByAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.pagination.PaginationAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.parameter.ParameterMarkerAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.predicate.WhereAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.projection.ProjectionAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.table.AlterTableAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.table.TableAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.root.ParserResult;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.TCLStatement;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

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
     * @param sqlCaseType SQL case type
     */
    public static void assertIs(final SQLStatementAssertMessage assertMessage, final SQLStatement actual, final ParserResult expected, final SQLCaseType sqlCaseType) {
        ParameterMarkerAssert.assertCount(assertMessage, actual.getParametersCount(), expected.getParameters().size(), sqlCaseType);
        TableAssert.assertIs(assertMessage, actual.findSQLSegments(TableSegment.class), expected.getTables());
        if (actual instanceof SelectStatement) {
            assertSelectStatement(assertMessage, (SelectStatement) actual, expected, sqlCaseType);
        }
        if (actual instanceof InsertStatement) {
            assertInsertStatement(assertMessage, (InsertStatement) actual, expected, sqlCaseType);
        }
        if (actual instanceof AlterTableStatement) {
            assertAlterTableStatement(assertMessage, (AlterTableStatement) actual, expected);
        }
        if (actual instanceof TCLStatement) {
            assertTCLStatement((TCLStatement) actual, expected);
        }
    }
    
    private static void assertSelectStatement(final SQLStatementAssertMessage assertMessage, final SelectStatement actual, final ParserResult expected, final SQLCaseType sqlCaseType) {
        ProjectionAssert.assertIs(assertMessage, actual.getProjections(), expected.getProjections());
        Optional<WhereSegment> whereSegment = actual.getWhere();
        if (whereSegment.isPresent() && null != expected.getWhere()) {
//        if (whereSegment.isPresent()) {
            assertNotNull(assertMessage.getText("Expected where assertion should exist: "), expected.getWhere());
            WhereAssert.assertIs(assertMessage, whereSegment.get(), expected.getWhere(), sqlCaseType);
        }
        Optional<GroupBySegment> groupBySegment = actual.getGroupBy();
        if (groupBySegment.isPresent()) {
            GroupByAssert.assertIs(assertMessage, groupBySegment.get().getGroupByItems(), expected.getGroupByColumns());
        }
        Optional<OrderBySegment> orderBySegment = actual.getOrderBy();
        if (orderBySegment.isPresent()) {
            OrderByAssert.assertIs(assertMessage, orderBySegment.get().getOrderByItems(), expected.getOrderByColumns());
        }
        Optional<LimitSegment> limitSegment = actual.findSQLSegment(LimitSegment.class);
        if (limitSegment.isPresent()) {
            PaginationAssert.assertOffset(assertMessage, limitSegment.get().getOffset().orNull(), expected.getOffset(), sqlCaseType);
            PaginationAssert.assertRowCount(assertMessage, limitSegment.get().getRowCount().orNull(), expected.getRowCount(), sqlCaseType);
        }
    }
    
    private static void assertInsertStatement(final SQLStatementAssertMessage assertMessage, final InsertStatement actual, final ParserResult expected, final SQLCaseType sqlCaseType) {
        InsertNamesAndValuesAssert.assertIs(assertMessage, actual, expected.getInsertColumnsAndValues(), sqlCaseType);
    }
    
    private static void assertAlterTableStatement(final SQLStatementAssertMessage assertMessage, final AlterTableStatement actual, final ParserResult expected) {
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
