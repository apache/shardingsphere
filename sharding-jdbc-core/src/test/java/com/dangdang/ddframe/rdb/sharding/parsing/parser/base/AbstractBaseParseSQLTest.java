package com.dangdang.ddframe.rdb.sharding.parsing.parser.base;

import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Conditions;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Tables;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractBaseParseSQLTest extends AbstractBaseParseTest {
    
    protected AbstractBaseParseSQLTest(
            final String testCaseName, final String sql, final String[] parameters, final Set<DatabaseType> types, 
            final Tables expectedTables, final Conditions expectedConditions, final SQLStatement expectedSQLStatement) {
        super(testCaseName, sql, parameters, types, expectedTables, expectedConditions, expectedSQLStatement);
    }
    
    protected final void assertSQLStatement(final SQLStatement actual) {
        assertExpectedTables(actual);
        assertExpectedConditions(actual);
        if (actual instanceof SelectStatement) {
            assertOrderBy((SelectStatement) actual);
            assertGroupBy((SelectStatement) actual);
            assertAggregationSelectItem((SelectStatement) actual);
            assertLimit((SelectStatement) actual);
        }
    }
    
    private void assertExpectedTables(final SQLStatement actual) {
        assertTrue(new ReflectionEquals(getExpectedTables()).matches(actual.getTables()));
    }
    
    private void assertExpectedConditions(final SQLStatement actual) {
        assertTrue(new ReflectionEquals(getExpectedConditions()).matches(actual.getConditions()));
    }
    
    private void assertOrderBy(final SelectStatement actual) {
        for (OrderItem each : actual.getOrderByItems()) {
            assertTrue(new ReflectionEquals(getExpectedOrderByColumns().next()).matches(each));
        }
        assertFalse(getExpectedOrderByColumns().hasNext());
    }
    
    private void assertGroupBy(final SelectStatement actual) {
        for (OrderItem each : actual.getGroupByItems()) {
            assertTrue(new ReflectionEquals(getExpectedGroupByColumns().next()).matches(each));
        }
        assertFalse(getExpectedGroupByColumns().hasNext());
    }
    
    private void assertAggregationSelectItem(final SelectStatement actual) {
        for (AggregationSelectItem each : actual.getAggregationSelectItems()) {
            AggregationSelectItem expected = getExpectedAggregationSelectItems().next();
            assertTrue(new ReflectionEquals(expected, "derivedAggregationSelectItems").matches(each));
            for (int i = 0; i < each.getDerivedAggregationSelectItems().size(); i++) {
                assertTrue(new ReflectionEquals(expected.getDerivedAggregationSelectItems().get(i)).matches(each.getDerivedAggregationSelectItems().get(i)));
            }
        }
        assertFalse(getExpectedAggregationSelectItems().hasNext());
    }
    
    private void assertLimit(final SelectStatement actual) {
        if (null != actual.getLimit()) {
            if (null != actual.getLimit().getOffset()) {
                assertTrue(new ReflectionEquals(getExpectedLimit().getOffset()).matches(actual.getLimit().getOffset()));
            }
            if (null != actual.getLimit().getRowCount()) {
                assertTrue(new ReflectionEquals(getExpectedLimit().getRowCount()).matches(actual.getLimit().getRowCount()));
            }
        }
    }
}
