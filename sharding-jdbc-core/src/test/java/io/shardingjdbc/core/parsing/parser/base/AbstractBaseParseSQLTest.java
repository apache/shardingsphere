package io.shardingjdbc.core.parsing.parser.base;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.parser.jaxb.Assert;
import io.shardingjdbc.core.parsing.parser.jaxb.helper.ParserAssertHelper;
import io.shardingjdbc.core.parsing.parser.jaxb.helper.ParserJAXBHelper;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;

public abstract class AbstractBaseParseSQLTest extends AbstractBaseParseTest {
    
    private final Assert expected;
    
    protected AbstractBaseParseSQLTest(
            final String testCaseName, final DatabaseType databaseType, final Assert assertObj) {
        super(testCaseName, databaseType, assertObj);
        this.expected = assertObj;
    }
    
    protected final void assertStatement(final SQLStatement actual) {
        assertSQLStatement(actual, false);
    }
    
    protected final void assertPreparedStatement(final SQLStatement actual) {
        assertSQLStatement(actual, true);
    }
    
    private void assertSQLStatement(final SQLStatement actual, final boolean isPreparedStatement) {
        ParserAssertHelper.assertTables(expected.getTables(), actual.getTables());
        ParserAssertHelper.assertConditions(expected.getConditions(), actual.getConditions(), isPreparedStatement);
        ParserAssertHelper.assertSqlTokens(expected.getSqlTokens(), actual.getSqlTokens(), isPreparedStatement);
        if (actual instanceof SelectStatement) {
            SelectStatement selectStatement = (SelectStatement) actual;
            SelectStatement expectedSqlStatement = ParserJAXBHelper.getSelectStatement(expected); 
            ParserAssertHelper.assertOrderBy(expectedSqlStatement.getOrderByItems(), selectStatement.getOrderByItems());
            ParserAssertHelper.assertGroupBy(expectedSqlStatement.getGroupByItems(), selectStatement.getGroupByItems());
            ParserAssertHelper.assertAggregationSelectItem(expectedSqlStatement.getAggregationSelectItems(), selectStatement.getAggregationSelectItems());
            ParserAssertHelper.assertLimit(expected.getLimit(), selectStatement.getLimit(), isPreparedStatement);
        }
    }
}
