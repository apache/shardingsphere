package com.dangdang.ddframe.rdb.sharding.parsing.parser.base;

import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.constant.ShardingOperator;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Column;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Condition;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Conditions;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Tables;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLTextExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Value;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractBaseParseSQLTest extends AbstractBaseParseTest {
    
    protected AbstractBaseParseSQLTest(
            final String testCaseName, final String sql, final String[] parameters, final Set<DatabaseType> types, 
            final Tables expectedTables, final com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Conditions expectedConditions, final SQLStatement expectedSQLStatement) {
        super(testCaseName, sql, parameters, types, expectedTables, expectedConditions, expectedSQLStatement);
    }
    
    protected final void assertStatement(final SQLStatement actual) {
        assertSQLStatement(actual, false);
    }
    
    protected final void assertPreparedStatement(final SQLStatement actual) {
        assertSQLStatement(actual, true);
    }
    
    private void assertSQLStatement(final SQLStatement actual, final boolean isPreparedStatement) {
        assertExpectedTables(actual);
        assertExpectedConditions(actual, isPreparedStatement);
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
    
    private void assertExpectedConditions(final SQLStatement actual, final boolean isPreparedStatement) {
        assertTrue(new ReflectionEquals(buildExpectedConditions(isPreparedStatement)).matches(actual.getConditions()));
    }
    
    private Conditions buildExpectedConditions(final boolean isPreparedStatement) {
        com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Conditions conditions = getExpectedConditions();
        Conditions result = new Conditions();
        if (null == conditions) {
            return result;
        }
        for (com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Condition each : conditions.getConditions()) {
            List<SQLExpression> sqlExpressions = new LinkedList<>();
            for (Value value : each.getValues()) {
                if (isPreparedStatement) {
                    sqlExpressions.add(new SQLPlaceholderExpression(value.getIndex()));
                } else {
                    Comparable<?> valueWithType = value.getValueWithType();
                    if (valueWithType instanceof Number) {
                        sqlExpressions.add(new SQLNumberExpression((Number) valueWithType));
                    } else {
                        sqlExpressions.add(new SQLTextExpression(valueWithType.toString()));
                    }
                }
            }
            Condition condition;
            switch (ShardingOperator.valueOf(each.getOperator().toUpperCase())) {
                case EQUAL:
                    condition = new Condition(new Column(each.getColumnName(), each.getTableName()), sqlExpressions.get(0));
                    break;
                case BETWEEN:
                    condition = new Condition(new Column(each.getColumnName(), each.getTableName()), sqlExpressions.get(0), sqlExpressions.get(1));
                    break;
                case IN:
                    condition = new Condition(new Column(each.getColumnName(), each.getTableName()), sqlExpressions);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            result.add(condition);
        }
        return result;
    }
    
    private void assertOrderBy(final SelectStatement actual) {
        Iterator<OrderItem> orderByColumns = getExpectedOrderByColumns().iterator();
        for (OrderItem each : actual.getOrderByItems()) {
            assertTrue(new ReflectionEquals(orderByColumns.next()).matches(each));
        }
        assertFalse(orderByColumns.hasNext());
    }
    
    private void assertGroupBy(final SelectStatement actual) {
        Iterator<OrderItem> groupByColumns = getExpectedGroupByColumns().iterator();
        for (OrderItem each : actual.getGroupByItems()) {
            assertTrue(new ReflectionEquals(groupByColumns.next()).matches(each));
        }
        assertFalse(groupByColumns.hasNext());
    }
    
    private void assertAggregationSelectItem(final SelectStatement actual) {
        Iterator<AggregationSelectItem> aggregationSelectItems = getExpectedAggregationSelectItems().iterator();
        for (AggregationSelectItem each : actual.getAggregationSelectItems()) {
            AggregationSelectItem expected = aggregationSelectItems.next();
            assertTrue(new ReflectionEquals(expected, "derivedAggregationSelectItems").matches(each));
            for (int i = 0; i < each.getDerivedAggregationSelectItems().size(); i++) {
                assertTrue(new ReflectionEquals(expected.getDerivedAggregationSelectItems().get(i)).matches(each.getDerivedAggregationSelectItems().get(i)));
            }
        }
        assertFalse(aggregationSelectItems.hasNext());
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
