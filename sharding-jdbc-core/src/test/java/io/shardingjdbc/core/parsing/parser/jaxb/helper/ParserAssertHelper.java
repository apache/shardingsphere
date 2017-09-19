package io.shardingjdbc.core.parsing.parser.jaxb.helper;

import io.shardingjdbc.core.constant.ShardingOperator;
import io.shardingjdbc.core.parsing.parser.context.OrderItem;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.context.limit.Limit;
import io.shardingjdbc.core.parsing.parser.context.limit.LimitValue;
import io.shardingjdbc.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingjdbc.core.parsing.parser.expression.SQLExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLTextExpression;
import io.shardingjdbc.core.parsing.parser.jaxb.Value;
import io.shardingjdbc.core.parsing.parser.token.SQLToken;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ParserAssertHelper {
    
    public static void assertTables(final io.shardingjdbc.core.parsing.parser.jaxb.Tables expected, final io.shardingjdbc.core.parsing.parser.context.table.Tables actual) {
        assertTrue(new ReflectionEquals(ParserJAXBHelper.getTables(expected)).matches(actual));
    }
    
    public static void assertConditions(final io.shardingjdbc.core.parsing.parser.jaxb.Conditions expected, final io.shardingjdbc.core.parsing.parser.context.condition.Conditions actual, final boolean isPreparedStatement) {
        assertTrue(new ReflectionEquals(buildExpectedConditions(expected, isPreparedStatement)).matches(actual));
    }
    
    private static io.shardingjdbc.core.parsing.parser.context.condition.Conditions buildExpectedConditions(final io.shardingjdbc.core.parsing.parser.jaxb.Conditions conditions, final boolean isPreparedStatement) {
        io.shardingjdbc.core.parsing.parser.context.condition.Conditions result = new io.shardingjdbc.core.parsing.parser.context.condition.Conditions();
        if (null == conditions) {
            return result;
        }
        for (io.shardingjdbc.core.parsing.parser.jaxb.Condition each : conditions.getConditions()) {
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
            io.shardingjdbc.core.parsing.parser.context.condition.Condition condition;
            switch (ShardingOperator.valueOf(each.getOperator().toUpperCase())) {
                case EQUAL:
                    condition = new io.shardingjdbc.core.parsing.parser.context.condition.Condition(new Column(each.getColumnName(), each.getTableName()), sqlExpressions.get(0));
                    break;
                case BETWEEN:
                    condition = new io.shardingjdbc.core.parsing.parser.context.condition.Condition(new Column(each.getColumnName(), each.getTableName()), sqlExpressions.get(0), sqlExpressions.get(1));
                    break;
                case IN:
                    condition = new io.shardingjdbc.core.parsing.parser.context.condition.Condition(new Column(each.getColumnName(), each.getTableName()), sqlExpressions);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            result.add(condition);
        }
        return result;
    }
    
    public static void assertSqlTokens(final List<io.shardingjdbc.core.parsing.parser.jaxb.TableToken> expected, final List<SQLToken> actual) {
        // TODO add more sql tokens
        if (null == expected) {
            return;
        }
        Iterator<io.shardingjdbc.core.parsing.parser.token.TableToken> sqlTokenIterator = buildExpectedTableTokens(expected).iterator();
        for (SQLToken each : actual) {
            io.shardingjdbc.core.parsing.parser.token.TableToken tableToken = sqlTokenIterator.next();
            assertTrue(new ReflectionEquals(tableToken).matches(each));
        }
        assertFalse(sqlTokenIterator.hasNext());
    }
    
    private static List<io.shardingjdbc.core.parsing.parser.token.TableToken> buildExpectedTableTokens(final List<io.shardingjdbc.core.parsing.parser.jaxb.TableToken> tableTokens) {
        List<io.shardingjdbc.core.parsing.parser.token.TableToken> result = new ArrayList<>();
        for (io.shardingjdbc.core.parsing.parser.jaxb.TableToken each : tableTokens) {
            result.add(new io.shardingjdbc.core.parsing.parser.token.TableToken(each.getBeginPosition(), each.getOriginalLiterals()));
        }
        return result;
    }
    
    public static void assertLimit(final io.shardingjdbc.core.parsing.parser.jaxb.Limit limit, final Limit actual, final boolean isPreparedStatement) {
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
    
    private static Limit buildExpectedLimit(final io.shardingjdbc.core.parsing.parser.jaxb.Limit limit, final boolean isPreparedStatement) {
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
