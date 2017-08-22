package com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.helper;

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
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.TableToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Value;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.SQLToken;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ParserAssertHelper {
    
    public static void assertTables(final com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Tables expected, final Tables actual) {
        assertTrue(new ReflectionEquals(ParserJAXBHelper.getTables(expected)).matches(actual));
    }
    
    public static void assertConditions(final com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Conditions expected, final Conditions actual, final boolean isPreparedStatement) {
        assertTrue(new ReflectionEquals(buildExpectedConditions(expected, isPreparedStatement)).matches(actual));
    }
    
    private static Conditions buildExpectedConditions(final com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Conditions conditions, final boolean isPreparedStatement) {
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
    
    public static void assertSqlTokens(final List<TableToken> expected, final List<SQLToken> actual) {
        // TODO add more sql tokens
        if (null == expected) {
            return;
        }
        Iterator<com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken> sqlTokenIterator = buildExpectedTableTokens(expected).iterator();
        for (SQLToken each : actual) {
            com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken tableToken = sqlTokenIterator.next();
            assertTrue(new ReflectionEquals(tableToken).matches(each));
        }
        assertFalse(sqlTokenIterator.hasNext());
    }
    
    private static List<com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken> buildExpectedTableTokens(final List<TableToken> tableTokens) {
        List<com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken> result = new ArrayList<>();
        for (TableToken each : tableTokens) {
            result.add(new com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken(each.getBeginPosition(), each.getOriginalLiterals()));
        }
        return result;
    }
    
    public static void assertLimit(final com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Limit limit, final Limit actual, final boolean isPreparedStatement) {
        Limit expected = buildExpectedLimit(limit, isPreparedStatement);
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
    
    private static Limit buildExpectedLimit(final com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Limit limit, final boolean isPreparedStatement) {
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
    
    public static void assertOrderBy(final List<OrderItem> expected, final List<OrderItem> actual) {
        Iterator<OrderItem> orderByColumns = expected.iterator();
        for (OrderItem each : actual) {
            OrderItem expectedOrderItem = orderByColumns.next();
            // TODO assert nullOrderType
            assertTrue(new ReflectionEquals(expectedOrderItem, "nullOrderType").matches(each));
        }
        assertFalse(orderByColumns.hasNext());
    }
    
    public static void assertGroupBy(final List<OrderItem> expected, final List<OrderItem> actual) {
        Iterator<OrderItem> groupByColumns = expected.iterator();
        for (OrderItem each : actual) {
            OrderItem groupByColumn = groupByColumns.next();
            // TODO assert nullOrderType
            assertTrue(new ReflectionEquals(groupByColumn, "nullOrderType").matches(each));
        }
        assertFalse(groupByColumns.hasNext());
    }
    
    public static void assertAggregationSelectItem(final List<AggregationSelectItem> expected, final List<AggregationSelectItem> actual) {
        Iterator<AggregationSelectItem> aggregationSelectItems = expected.iterator();
        for (AggregationSelectItem each : actual) {
            AggregationSelectItem aggregationSelectItem = aggregationSelectItems.next();
            assertTrue(new ReflectionEquals(aggregationSelectItem, "derivedAggregationSelectItems").matches(each));
            for (int i = 0; i < each.getDerivedAggregationSelectItems().size(); i++) {
                assertTrue(new ReflectionEquals(aggregationSelectItem.getDerivedAggregationSelectItems().get(i)).matches(each.getDerivedAggregationSelectItems().get(i)));
            }
        }
        assertFalse(aggregationSelectItems.hasNext());
    }
    
}
