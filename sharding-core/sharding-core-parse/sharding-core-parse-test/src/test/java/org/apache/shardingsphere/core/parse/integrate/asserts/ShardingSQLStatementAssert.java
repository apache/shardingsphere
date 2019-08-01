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

package org.apache.shardingsphere.core.parse.integrate.asserts;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.parse.integrate.asserts.groupby.GroupByAssert;
import org.apache.shardingsphere.core.parse.integrate.asserts.index.IndexAssert;
import org.apache.shardingsphere.core.parse.integrate.asserts.orderby.OrderByAssert;
import org.apache.shardingsphere.core.parse.integrate.asserts.pagination.PaginationAssert;
import org.apache.shardingsphere.core.parse.integrate.asserts.table.AlterTableAssert;
import org.apache.shardingsphere.core.parse.integrate.asserts.table.TableAssert;
import org.apache.shardingsphere.core.parse.integrate.jaxb.ShardingParserResultSetRegistry;
import org.apache.shardingsphere.core.parse.integrate.jaxb.root.ParserResult;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.sql.statement.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.core.parse.sql.statement.tcl.TCLStatement;
import org.apache.shardingsphere.test.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.loader.sharding.ShardingSQLCasesRegistry;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * SQL statement assert for sharding.
 *
 * @author zhangliang
 */
public final class ShardingSQLStatementAssert {
    
    private final SQLStatement actual;
    
    private final ParserResult expected;
    
    private final TableAssert tableAssert;
    
    private final IndexAssert indexAssert;
    
    private final GroupByAssert groupByAssert;
    
    private final OrderByAssert orderByAssert;
    
    private final PaginationAssert paginationAssert;
    
    private final AlterTableAssert alterTableAssert;
    
    public ShardingSQLStatementAssert(final SQLStatement actual, final String sqlCaseId, final SQLCaseType sqlCaseType) {
        SQLStatementAssertMessage assertMessage = new SQLStatementAssertMessage(
                ShardingSQLCasesRegistry.getInstance().getSqlCasesLoader(), ShardingParserResultSetRegistry.getInstance().getRegistry(), sqlCaseId, sqlCaseType);
        this.actual = actual;
        expected = ShardingParserResultSetRegistry.getInstance().getRegistry().get(sqlCaseId);
        tableAssert = new TableAssert(assertMessage);
        indexAssert = new IndexAssert(sqlCaseType, assertMessage);
        groupByAssert = new GroupByAssert(assertMessage);
        orderByAssert = new OrderByAssert(assertMessage);
        paginationAssert = new PaginationAssert(sqlCaseType, assertMessage);
        alterTableAssert = new AlterTableAssert(assertMessage);
    }
    
    /**
     * Assert SQL statement.
     */
    public void assertSQLStatement() {
        tableAssert.assertTables(actual.findSQLSegments(TableSegment.class), expected.getTables());
        indexAssert.assertParametersCount(actual.getParametersCount(), expected.getParameters().size());
        if (actual instanceof SelectStatement) {
            assertSelectStatement((SelectStatement) actual);
        }
        if (actual instanceof AlterTableStatement) {
            assertAlterTableStatement((AlterTableStatement) actual);
        }
        if (actual instanceof TCLStatement) {
            assertTCLStatement((TCLStatement) actual);
        }
    }
    
    private void assertSelectStatement(final SelectStatement actual) {
        // TODO do select items assert
//        Optional<SelectItemsSegment> selectItemsSegment = actual.findSQLSegment(SelectItemsSegment.class);
//        if (selectItemsSegment.isPresent()) {
//        }
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
