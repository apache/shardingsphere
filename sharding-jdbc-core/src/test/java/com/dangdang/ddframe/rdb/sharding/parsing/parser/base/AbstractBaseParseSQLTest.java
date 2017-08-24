package com.dangdang.ddframe.rdb.sharding.parsing.parser.base;

import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Assert;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.helper.ParserAssertHelper;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.helper.ParserJAXBHelper;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dql.select.SelectStatement;

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
        ParserAssertHelper.assertSqlTokens(expected.getTableTokens(), actual.getSqlTokens());
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
