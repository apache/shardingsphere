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

import org.apache.shardingsphere.core.parse.integrate.asserts.condition.ConditionAssert;
import org.apache.shardingsphere.core.parse.integrate.asserts.groupby.GroupByAssert;
import org.apache.shardingsphere.core.parse.integrate.asserts.index.IndexAssert;
import org.apache.shardingsphere.core.parse.integrate.asserts.item.ItemAssert;
import org.apache.shardingsphere.core.parse.integrate.asserts.meta.TableMetaDataAssert;
import org.apache.shardingsphere.core.parse.integrate.asserts.orderby.OrderByAssert;
import org.apache.shardingsphere.core.parse.integrate.asserts.pagination.PaginationAssert;
import org.apache.shardingsphere.core.parse.integrate.asserts.table.AlterTableAssert;
import org.apache.shardingsphere.core.parse.integrate.asserts.table.TableAssert;
import org.apache.shardingsphere.core.parse.integrate.jaxb.root.ParserResult;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.sql.statement.tcl.TCLStatement;
import org.apache.shardingsphere.test.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.SQLCasesLoader;

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
    
    private final TableAssert tableAssert;
    
    private final ConditionAssert conditionAssert;
    
    private final IndexAssert indexAssert;
    
    private final ItemAssert itemAssert;
    
    private final GroupByAssert groupByAssert;
    
    private final OrderByAssert orderByAssert;
    
    private final PaginationAssert paginationAssert;
    
    private final TableMetaDataAssert metaAssert;
    
    private final AlterTableAssert alterTableAssert;
    
    private final String databaseType;
    
    public SQLStatementAssert(final SQLStatement actual, final String sqlCaseId, final SQLCaseType sqlCaseType, final String databaseType) {
        this(actual, sqlCaseId, sqlCaseType, SQLCasesLoader.getInstance(), ParserResultSetLoader.getInstance(), databaseType);
    }
    
    public SQLStatementAssert(final SQLStatement actual, final String sqlCaseId, 
                              final SQLCaseType sqlCaseType, final SQLCasesLoader sqlLoader, final ParserResultSetLoader parserResultSetLoader, final String databaseType) {
        SQLStatementAssertMessage assertMessage = new SQLStatementAssertMessage(sqlLoader, parserResultSetLoader, sqlCaseId, sqlCaseType);
        this.actual = actual;
        expected = parserResultSetLoader.getParserResult(sqlCaseId);
        tableAssert = new TableAssert(assertMessage);
        conditionAssert = new ConditionAssert(assertMessage);
        indexAssert = new IndexAssert(sqlCaseType, assertMessage);
        itemAssert = new ItemAssert(assertMessage);
        groupByAssert = new GroupByAssert(assertMessage);
        orderByAssert = new OrderByAssert(assertMessage);
        paginationAssert = new PaginationAssert(sqlCaseType, assertMessage);
        metaAssert = new TableMetaDataAssert(assertMessage);
        alterTableAssert = new AlterTableAssert(assertMessage);
        this.databaseType = databaseType;
    }
    
    /**
     * Assert SQL statement.
     */
    public void assertSQLStatement() {
        tableAssert.assertTables(actual.getTables(), expected.getTables());
        conditionAssert.assertConditions(actual.getRouteCondition(), expected.getOrCondition());
        if ("MySQL".equals(databaseType)) {
            conditionAssert.assertConditions(actual.getEncryptCondition(), expected.getEncryptCondition());
        }
        indexAssert.assertParametersIndex(actual.getParametersIndex(), expected.getParameters().size());
        if (actual instanceof SelectStatement) {
            assertSelectStatement((SelectStatement) actual);
        }
        if (actual instanceof CreateTableStatement) {
            assertCreateTableStatement((CreateTableStatement) actual);
        }
        if (actual instanceof AlterTableStatement) {
            assertAlterTableStatement((AlterTableStatement) actual);
        }
        if (actual instanceof TCLStatement) {
            assertTCLStatement((TCLStatement) actual);
        }
    }
    
    private void assertSelectStatement(final SelectStatement actual) {
        itemAssert.assertItems(actual.getItems(), expected.getSelectItems());
        groupByAssert.assertGroupByItems(actual.getGroupByItems(), expected.getGroupByColumns());
        orderByAssert.assertOrderByItems(actual.getOrderByItems(), expected.getOrderByColumns());
        paginationAssert.assertPagination(actual.getPagination(), expected.getPagination());
    }
    
    private void assertCreateTableStatement(final CreateTableStatement actual) {
        metaAssert.assertMeta(actual.getColumnDefinitions(), expected.getMeta());
    }
    
    private void assertAlterTableStatement(final AlterTableStatement actual) {
        if (null != expected.getAlterTable()) {
            alterTableAssert.assertAlterTable(actual, expected.getAlterTable());
        }
    }
    
    private void assertTCLStatement(final TCLStatement actual) {
        assertThat(actual.getClass().getName(), is(expected.getTclActualStatementClassType()));
    }
}
