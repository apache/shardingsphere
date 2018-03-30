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
    
    private final ParserResultSetLoader parserResultSetLoader = ParserResultSetLoader.getInstance();
    
    private final SQLStatement actual;
    
    private final ParserResult expected;
    
    private final TableAssert tableAssert;
    
    private final ConditionAssert conditionAssert;
    
    private final TokenAssert tokenAssert;
    
    private final ItemAssert itemAssert;
    
    private final GroupByAssert groupByAssert;
    
    private final OrderByAssert orderByAssert;
    
    private final LimitAssert limitAssert;
    
    public SQLStatementAssert(final SQLStatement actual, final String sqlCaseId, final SQLCaseType sqlCaseType) {
        this.actual = actual;
        expected = parserResultSetLoader.getParserResult(sqlCaseId);
        SQLStatementAssertMessage assertMessage = new SQLStatementAssertMessage(sqlCaseId, sqlCaseType);
        tableAssert = new TableAssert(assertMessage);
        conditionAssert = new ConditionAssert(assertMessage);
        tokenAssert = new TokenAssert(sqlCaseType, assertMessage);
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
        conditionAssert.assertConditions(actual.getConditions(), expected.getConditions());
        tokenAssert.assertTokens(actual.getSqlTokens(), expected.getTokens());
        if (actual instanceof SelectStatement) {
            SelectStatement selectStatement = (SelectStatement) actual;
            itemAssert.assertItems(selectStatement.getItems(), expected.getAggregationSelectItems());
            groupByAssert.assertGroupByItems(selectStatement.getGroupByItems(), expected.getGroupByColumns());
            orderByAssert.assertOrderByItems(selectStatement.getOrderByItems(), expected.getOrderByColumns());
            limitAssert.assertLimit(selectStatement.getLimit(), expected.getLimit());
        }
    }
}
