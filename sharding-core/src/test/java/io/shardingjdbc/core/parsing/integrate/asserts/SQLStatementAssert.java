/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.parsing.integrate.asserts;

import io.shardingjdbc.core.parsing.integrate.asserts.condition.ConditionAssert;
import io.shardingjdbc.core.parsing.integrate.asserts.groupby.GroupByAssert;
import io.shardingjdbc.core.parsing.integrate.asserts.index.IndexAssert;
import io.shardingjdbc.core.parsing.integrate.asserts.item.ItemAssert;
import io.shardingjdbc.core.parsing.integrate.asserts.limit.LimitAssert;
import io.shardingjdbc.core.parsing.integrate.asserts.orderby.OrderByAssert;
import io.shardingjdbc.core.parsing.integrate.asserts.table.TableAssert;
import io.shardingjdbc.core.parsing.integrate.asserts.token.TokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.root.ParserResult;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.test.sql.SQLCaseType;

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
    
    public SQLStatementAssert(final SQLStatement actual, final String sqlCaseId, final SQLCaseType sqlCaseType) {
        SQLStatementAssertMessage assertMessage = new SQLStatementAssertMessage(sqlCaseId, sqlCaseType);
        this.actual = actual;
        final ParserResultSetLoader parserResultSetLoader = ParserResultSetLoader.getInstance();
        expected = parserResultSetLoader.getParserResult(sqlCaseId);
        tableAssert = new TableAssert(assertMessage);
        conditionAssert = new ConditionAssert(assertMessage);
        tokenAssert = new TokenAssert(sqlCaseType, assertMessage);
        indexAssert = new IndexAssert(sqlCaseType, assertMessage);
        itemAssert = new ItemAssert(assertMessage);
        groupByAssert = new GroupByAssert(assertMessage);
        orderByAssert = new OrderByAssert(assertMessage);
        limitAssert = new LimitAssert(sqlCaseType, assertMessage);
    }
    
    /**
     * Assert SQL statement.
     */
    public void assertSQLStatement() {
        tableAssert.assertTables(actual.getTables(), expected.getTables());
        conditionAssert.assertOrConditions(actual.getConditions().getOrConditions(), expected.getOrConditions());
        tokenAssert.assertTokens(actual.getSqlTokens(), expected.getTokens());
        indexAssert.assertParametersIndex(actual.getParametersIndex(), expected.getParameters().size());
        if (actual instanceof SelectStatement) {
            assertSelectStatement((SelectStatement) actual);
        }
    }
    
    private void assertSelectStatement(final SelectStatement actual) {
        itemAssert.assertItems(actual.getItems(), expected.getAggregationSelectItems());
        groupByAssert.assertGroupByItems(actual.getGroupByItems(), expected.getGroupByColumns());
        orderByAssert.assertOrderByItems(actual.getOrderByItems(), expected.getOrderByColumns());
        limitAssert.assertLimit(actual.getLimit(), expected.getLimit());
    }
}
