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

package io.shardingjdbc.core.parsing.integrate.tests;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.SQLParsingEngine;
import io.shardingjdbc.core.parsing.integrate.jaxb.root.ParserAssert;
import io.shardingjdbc.core.parsing.integrate.asserts.ParserAssertHelper;
import io.shardingjdbc.core.parsing.integrate.asserts.ParserJAXBHelper;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.core.util.SQLPlaceholderUtil;
import io.shardingjdbc.test.sql.SQLCasesLoader;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

@RequiredArgsConstructor
public final class IntegrateSupportedSQLParsingTest extends AbstractBaseIntegrateSQLParsingTest {
    
    private static SQLCasesLoader sqlCasesLoader = SQLCasesLoader.getInstance();
    
    private static ParserAssertsLoader parserAssertsLoader = ParserAssertsLoader.getInstance();
    
    private final String sqlCaseId;
    
    private final DatabaseType databaseType;
    
    @Parameters(name = "{0}In{1}")
    public static Collection<Object[]> getTestParameters() {
        return sqlCasesLoader.getSupportedSQLTestParameters(Arrays.<Enum>asList(DatabaseType.values()), DatabaseType.class);
    }
    
    @Test
    public void assertLiteralSQL() {
        assertSQLStatement(new SQLParsingEngine(databaseType, 
                SQLPlaceholderUtil.replaceStatement(sqlCasesLoader.getSupportedSQL(sqlCaseId), parserAssertsLoader.getParserAssert(sqlCaseId).getParameters()), getShardingRule()).parse(), false);
    }
    
    @Test
    public void assertPlaceholderSQL() {
        assertSQLStatement(new SQLParsingEngine(databaseType, SQLPlaceholderUtil.replacePreparedStatement(sqlCasesLoader.getSupportedSQL(sqlCaseId)), getShardingRule()).parse(), true);
    }
    
    private void assertSQLStatement(final SQLStatement actual, final boolean isPreparedStatement) {
        ParserAssert parserAssert = parserAssertsLoader.getParserAssert(sqlCaseId);
        ParserAssertHelper.assertTables(parserAssert.getTables(), actual.getTables());
        ParserAssertHelper.assertConditions(parserAssert.getConditions(), actual.getConditions(), isPreparedStatement);
        ParserAssertHelper.assertSqlTokens(parserAssert.getTokens().getTokenAsserts(), actual.getSqlTokens(), isPreparedStatement);
        if (actual instanceof SelectStatement) {
            SelectStatement selectStatement = (SelectStatement) actual;
            SelectStatement expectedSqlStatement = ParserJAXBHelper.getSelectStatement(parserAssert);
            ParserAssertHelper.assertOrderBy(expectedSqlStatement.getOrderByItems(), selectStatement.getOrderByItems());
            ParserAssertHelper.assertGroupBy(expectedSqlStatement.getGroupByItems(), selectStatement.getGroupByItems());
            ParserAssertHelper.assertAggregationSelectItem(expectedSqlStatement.getAggregationSelectItems(), selectStatement.getAggregationSelectItems());
            ParserAssertHelper.assertLimit(parserAssert.getLimit(), selectStatement.getLimit(), isPreparedStatement);
        }
    }
}
