/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.integrate.asserts;

import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.CreateTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.statement.tcl.TCLStatement;
import io.shardingsphere.core.parsing.integrate.asserts.condition.ConditionAssert;
import io.shardingsphere.core.parsing.integrate.asserts.groupby.GroupByAssert;
import io.shardingsphere.core.parsing.integrate.asserts.index.IndexAssert;
import io.shardingsphere.core.parsing.integrate.asserts.item.ItemAssert;
import io.shardingsphere.core.parsing.integrate.asserts.limit.LimitAssert;
import io.shardingsphere.core.parsing.integrate.asserts.meta.TableMetaDataAssert;
import io.shardingsphere.core.parsing.integrate.asserts.orderby.OrderByAssert;
import io.shardingsphere.core.parsing.integrate.asserts.table.AlterTableAssert;
import io.shardingsphere.core.parsing.integrate.asserts.table.TableAssert;
import io.shardingsphere.core.parsing.integrate.asserts.token.TokenAssert;
import io.shardingsphere.core.parsing.integrate.jaxb.root.ParserResult;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;

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
    
    private final TokenAssert tokenAssert;
    
    private final IndexAssert indexAssert;
    
    private final ItemAssert itemAssert;
    
    private final GroupByAssert groupByAssert;
    
    private final OrderByAssert orderByAssert;
    
    private final LimitAssert limitAssert;
    
    private final TableMetaDataAssert metaAssert;
    
    private final AlterTableAssert alterTableAssert;
    
    public SQLStatementAssert(final SQLStatement actual, final String sqlCaseId, final SQLCaseType sqlCaseType) {
        this(actual, sqlCaseId, sqlCaseType, SQLCasesLoader.getInstance(), ParserResultSetLoader.getInstance());
    }
    
    public SQLStatementAssert(final SQLStatement actual, final String sqlCaseId, final SQLCaseType sqlCaseType, final SQLCasesLoader sqlLoader, final ParserResultSetLoader parserResultSetLoader) {
        SQLStatementAssertMessage assertMessage = new SQLStatementAssertMessage(sqlLoader, parserResultSetLoader, sqlCaseId, sqlCaseType);
        this.actual = actual;
        expected = parserResultSetLoader.getParserResult(sqlCaseId);
        tableAssert = new TableAssert(assertMessage);
        conditionAssert = new ConditionAssert(assertMessage);
        tokenAssert = new TokenAssert(sqlCaseType, assertMessage);
        indexAssert = new IndexAssert(sqlCaseType, assertMessage);
        itemAssert = new ItemAssert(assertMessage);
        groupByAssert = new GroupByAssert(assertMessage);
        orderByAssert = new OrderByAssert(assertMessage);
        limitAssert = new LimitAssert(sqlCaseType, assertMessage);
        metaAssert = new TableMetaDataAssert(assertMessage);
        alterTableAssert = new AlterTableAssert(assertMessage);
    }
    
    /**
     * Assert SQL statement.
     */
    public void assertSQLStatement() {
        tableAssert.assertTables(actual.getTables(), expected.getTables());
        conditionAssert.assertOrCondition(actual.getConditions().getOrCondition(), expected.getOrCondition());
        tokenAssert.assertTokens(actual.getSQLTokens(), expected.getTokens());
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
        limitAssert.assertLimit(actual.getLimit(), expected.getLimit());
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
