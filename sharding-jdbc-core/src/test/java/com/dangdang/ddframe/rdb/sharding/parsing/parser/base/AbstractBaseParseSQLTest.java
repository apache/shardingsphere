package com.dangdang.ddframe.rdb.sharding.parsing.parser.base;

import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.constant.ShardingOperator;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Column;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Condition;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Conditions;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.LimitValue;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Tables;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLTextExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Value;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.SQLToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.TableToken;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractBaseParseSQLTest extends AbstractBaseParseTest {
    
    protected AbstractBaseParseSQLTest(
            final String testCaseName, final DatabaseType databaseType, final String[] parameters,
            final Tables expectedTables, final List<TableToken> expectedSqlTokens, final com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Conditions expectedConditions,
            final SQLStatement expectedSQLStatement, final com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Limit expectedLimit) {
        super(testCaseName, databaseType, parameters, expectedTables, expectedSqlTokens, expectedConditions, expectedSQLStatement, expectedLimit);
    }
    
    protected final void assertStatement(final SQLStatement actual) {
        assertSQLStatement(actual, false);
    }
    
    protected final void assertPreparedStatement(final SQLStatement actual) {
        assertSQLStatement(actual, true);
    }
    
    private void assertSQLStatement(final SQLStatement actual, final boolean isPreparedStatement) {
        assertTables(actual.getTables());
        assertConditions(actual.getConditions(), isPreparedStatement);
        assertSqlTokens(actual.getSqlTokens());
        if (actual instanceof SelectStatement) {
            SelectStatement selectStatement = (SelectStatement) actual;
            assertOrderBy(selectStatement.getOrderByItems());
            assertGroupBy(selectStatement.getGroupByItems());
            assertAggregationSelectItem(selectStatement.getAggregationSelectItems());
            assertLimit(selectStatement.getLimit(), isPreparedStatement);
        }
    }
    
    private void assertTables(final Tables actual) {
        assertTrue(new ReflectionEquals(getExpectedTables()).matches(actual));
    }
    
    private void assertConditions(final Conditions actual, final boolean isPreparedStatement) {
        assertTrue(new ReflectionEquals(buildExpectedConditions(isPreparedStatement)).matches(actual));
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
    
    private void assertSqlTokens(final List<SQLToken> actual) {
        // TODO add more sql tokens
        if (null == getExpectedTableTokens()) {
            return;
        }
        Iterator<com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken> sqlTokenIterator = buildExpectedTableTokens().iterator();
        for (SQLToken each : actual) {
            com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken expected = sqlTokenIterator.next();
            assertTrue(new ReflectionEquals(expected).matches(each));
        }
        assertFalse(sqlTokenIterator.hasNext());
    }
    
    private List<com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken> buildExpectedTableTokens() {
        List<com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken> result = new ArrayList<>();
        for (SQLToken each : getExpectedTableTokens()) {
            TableToken tableToken = (TableToken) each;
            result.add(new com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken(tableToken.getBeginPosition(), tableToken.getOriginalLiterals()));
        }
        return result;
    }
    
    
    private Limit buildExpectedLimit(final boolean isPreparedStatement) {
        com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Limit limit = getExpectedLimit();
        if (null == limit) {
            return null;
        }
        Limit result = new Limit(true);
        if (isPreparedStatement) {
            if (null != limit.getOffsetParameterIndex()) {
                result.setOffset(new LimitValue(-1, limit.getOffsetParameterIndex()));
            }
            if (null != limit.getRowCountParameterIndex()) {
                result.setRowCount(new LimitValue(-1, limit.getRowCountParameterIndex()));
            }
        } else {
            if (null != limit.getOffset()) {
                result.setOffset(new LimitValue(limit.getOffset(), -1));
                
            }
            if (null != limit.getRowCount()) {
                result.setRowCount(new LimitValue(limit.getRowCount(), -1));
            }
        }
        return result;
    }
    
    private void assertOrderBy(final List<OrderItem> actual) {
        Iterator<OrderItem> orderByColumns = getExpectedOrderByColumns().iterator();
        for (OrderItem each : actual) {
            OrderItem expectedOrderItem = orderByColumns.next();
            // TODO assert nullOrderType
            assertTrue(new ReflectionEquals(expectedOrderItem, "nullOrderType").matches(each));
        }
        assertFalse(orderByColumns.hasNext());
    }
    
    private void assertGroupBy(final List<OrderItem> actual) {
        Iterator<OrderItem> groupByColumns = getExpectedGroupByColumns().iterator();
        for (OrderItem each : actual) {
            OrderItem groupByColumn = groupByColumns.next();
            // TODO assert nullOrderType
            assertTrue(new ReflectionEquals(groupByColumn, "nullOrderType").matches(each));
        }
        assertFalse(groupByColumns.hasNext());
    }
    
    private void assertAggregationSelectItem(final List<AggregationSelectItem> actual) {
        Iterator<AggregationSelectItem> aggregationSelectItems = getExpectedAggregationSelectItems().iterator();
        for (AggregationSelectItem each : actual) {
            AggregationSelectItem expected = aggregationSelectItems.next();
            assertTrue(new ReflectionEquals(expected, "derivedAggregationSelectItems").matches(each));
            for (int i = 0; i < each.getDerivedAggregationSelectItems().size(); i++) {
                assertTrue(new ReflectionEquals(expected.getDerivedAggregationSelectItems().get(i)).matches(each.getDerivedAggregationSelectItems().get(i)));
            }
        }
        assertFalse(aggregationSelectItems.hasNext());
    }
    
    private void assertLimit(final Limit actual, final boolean isPreparedStatement) {
        Limit expected = buildExpectedLimit(isPreparedStatement);
        if (null == expected) {
            assertNull(actual);
            return;
        }
        if (null != expected.getRowCount()) {
            assertTrue(new ReflectionEquals(expected.getRowCount()).matches(actual.getRowCount()));
        }
        if (null != expected.getOffset()) {
            assertTrue(new ReflectionEquals(expected.getOffset()).matches(actual.getOffset()));
        }
    }
}
