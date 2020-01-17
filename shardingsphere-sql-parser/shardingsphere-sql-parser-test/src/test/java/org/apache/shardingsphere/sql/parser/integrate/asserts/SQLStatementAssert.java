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
import org.apache.shardingsphere.sql.parser.integrate.asserts.groupby.GroupByAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.index.ParameterMarkerAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.insert.InsertNamesAndValuesAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.orderby.OrderByAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.pagination.PaginationAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.predicate.PredicateAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.projection.ProjectionAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.table.AlterTableAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.table.TableAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.ParserResultSetRegistryFactory;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.root.ParserResult;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
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
import static org.junit.Assert.assertThat;

/**
 * SQL statement assert.
 *
 * @author zhangliang
 */
public final class SQLStatementAssert {
    
    private final SQLStatement actual;
    
    private final ParserResult expected;
    
    private final ParameterMarkerAssert parameterMarkerAssert;
    
    private final TableAssert tableAssert;
    
    private final GroupByAssert groupByAssert;
    
    private final OrderByAssert orderByAssert;
    
    private final PaginationAssert paginationAssert;
    
    private final AlterTableAssert alterTableAssert;

    private final ProjectionAssert projectionAssert;

    private final PredicateAssert predicateAssert;
    
    private final InsertNamesAndValuesAssert insertNamesAndValuesAssert;
    
    public SQLStatementAssert(final SQLStatement actual, final String sqlCaseId, final SQLCaseType sqlCaseType) {
        this.actual = actual;
        SQLStatementAssertMessage assertMessage = new SQLStatementAssertMessage(sqlCaseId, sqlCaseType);
        expected = ParserResultSetRegistryFactory.getInstance().getRegistry().get(sqlCaseId);
        parameterMarkerAssert = new ParameterMarkerAssert(sqlCaseType, assertMessage);
        tableAssert = new TableAssert(assertMessage);
        groupByAssert = new GroupByAssert(assertMessage);
        orderByAssert = new OrderByAssert(assertMessage);
        paginationAssert = new PaginationAssert(sqlCaseType, assertMessage);
        alterTableAssert = new AlterTableAssert(assertMessage);
        projectionAssert = new ProjectionAssert(sqlCaseType, assertMessage);
        predicateAssert = new PredicateAssert(sqlCaseType, assertMessage);
        insertNamesAndValuesAssert = new InsertNamesAndValuesAssert(assertMessage, sqlCaseType);
    }
    
    /**
     * Assert SQL statement.
     */
    public void assertSQLStatement() {
        parameterMarkerAssert.assertCount(actual.getParametersCount(), expected.getParameters().size());
        tableAssert.assertTables(actual.findSQLSegments(TableSegment.class), expected.getTables());
        if (actual instanceof SelectStatement) {
            assertSelectStatement((SelectStatement) actual);
        }
        if (actual instanceof InsertStatement) {
            assertInsertStatement((InsertStatement) actual);
        }
        if (actual instanceof AlterTableStatement) {
            assertAlterTableStatement((AlterTableStatement) actual);
        }
        if (actual instanceof TCLStatement) {
            assertTCLStatement((TCLStatement) actual);
        }
    }
    
    private void assertSelectStatement(final SelectStatement actual) {
        Optional<ProjectionsSegment> projectionsSegment = actual.findSQLSegment(ProjectionsSegment.class);
        if (projectionsSegment.isPresent()) {
            projectionAssert.assertProjections(projectionsSegment.get(), expected.getProjections());
        }
        Optional<GroupBySegment> groupBySegment = actual.findSQLSegment(GroupBySegment.class);
        if (groupBySegment.isPresent()) {
            groupByAssert.assertGroupByItems(groupBySegment.get().getGroupByItems(), expected.getGroupByColumns());
        }
        Optional<OrderBySegment> orderBySegment = actual.findSQLSegment(OrderBySegment.class);
        if (orderBySegment.isPresent()) {
            orderByAssert.assertOrderByItems(orderBySegment.get().getOrderByItems(), expected.getOrderByColumns());
        }
        Optional<LimitSegment> limitSegment = actual.findSQLSegment(LimitSegment.class);
        if (limitSegment.isPresent()) {
            paginationAssert.assertOffset(limitSegment.get().getOffset().orNull(), expected.getOffset());
            paginationAssert.assertRowCount(limitSegment.get().getRowCount().orNull(), expected.getRowCount());
        }
        Optional<WhereSegment> whereSegment = actual.findSQLSegment(WhereSegment.class);
        if (whereSegment.isPresent() && null != expected.getWhereSegment()) {
            predicateAssert.assertPredicate(whereSegment.get(), expected.getWhereSegment());
        }
    }
    
    private void assertInsertStatement(final InsertStatement actual) {
        insertNamesAndValuesAssert.assertInsertNamesAndValues(actual, expected.getInsertColumnsAndValues());
    }
    
    private void assertAlterTableStatement(final AlterTableStatement actual) {
        if (null != expected.getAlterTable()) {
            alterTableAssert.assertAlterTable(actual, expected.getAlterTable());
        }
    }
    
    private void assertTCLStatement(final TCLStatement actual) {
        assertThat(actual.getClass().getName(), is(expected.getTclActualStatementClassType()));
        if (actual instanceof SetAutoCommitStatement) {
            assertThat(((SetAutoCommitStatement) actual).isAutoCommit(), is(expected.isAutoCommit()));
        }
    }
}
