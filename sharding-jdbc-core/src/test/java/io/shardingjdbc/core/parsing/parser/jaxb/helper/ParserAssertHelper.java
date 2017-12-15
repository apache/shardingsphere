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
import io.shardingjdbc.core.parsing.parser.token.GeneratedKeyToken;
import io.shardingjdbc.core.parsing.parser.token.ItemsToken;
import io.shardingjdbc.core.parsing.parser.token.MultipleInsertValuesToken;
import io.shardingjdbc.core.parsing.parser.token.OffsetToken;
import io.shardingjdbc.core.parsing.parser.token.OrderByToken;
import io.shardingjdbc.core.parsing.parser.token.RowCountToken;
import io.shardingjdbc.core.parsing.parser.token.SQLToken;
import io.shardingjdbc.core.parsing.parser.token.TableToken;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ParserAssertHelper {
    
    public static void assertTables(final io.shardingjdbc.core.parsing.parser.jaxb.Tables expected, final io.shardingjdbc.core.parsing.parser.context.table.Tables actual) {
        assertTrue(EqualsBuilder.reflectionEquals(ParserJAXBHelper.getTables(expected), actual));
    }
    
    public static void assertConditions(
            final io.shardingjdbc.core.parsing.parser.jaxb.Conditions expected, final io.shardingjdbc.core.parsing.parser.context.condition.Conditions actual, final boolean isPreparedStatement) {
        assertTrue(EqualsBuilder.reflectionEquals(buildExpectedConditions(expected, isPreparedStatement), actual));
    }
    
    private static io.shardingjdbc.core.parsing.parser.context.condition.Conditions buildExpectedConditions(
            final io.shardingjdbc.core.parsing.parser.jaxb.Conditions conditions, final boolean isPreparedStatement) {
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
                        sqlExpressions.add(new SQLTextExpression(null == valueWithType ? "" : valueWithType.toString()));
                    }
                }
            }
            io.shardingjdbc.core.parsing.parser.context.condition.Condition condition;
            switch (ShardingOperator.valueOf(each.getOperator().toUpperCase())) {
                case EQUAL:
                    condition = new io.shardingjdbc.core.parsing.parser.context.condition.Condition(new Column(each.getColumnName(), each.getTableName()), sqlExpressions.get(0));
                    break;
                case BETWEEN:
                    condition = new io.shardingjdbc.core.parsing.parser.context.condition.Condition(
                            new Column(each.getColumnName(), each.getTableName()), sqlExpressions.get(0), sqlExpressions.get(1));
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
    
    public static void assertSqlTokens(final List<io.shardingjdbc.core.parsing.parser.jaxb.SQLToken> expected, final List<SQLToken> actual, final boolean isPreparedStatement) {
        if (null == expected || expected.size() == 0) {
            return;
        }
        List<io.shardingjdbc.core.parsing.parser.jaxb.SQLToken> filteredSqlTokens = filterSqlToken(expected, isPreparedStatement);
        Iterator<io.shardingjdbc.core.parsing.parser.jaxb.SQLToken> sqlTokenIterator = filteredSqlTokens.iterator();
        for (SQLToken each : actual) {
            SQLToken sqlToken = buildExpectedSQLToken(sqlTokenIterator.next(), isPreparedStatement);
            assertTrue(EqualsBuilder.reflectionEquals(sqlToken, each));
        }
        assertFalse(sqlTokenIterator.hasNext());
    }
    
    private static List<io.shardingjdbc.core.parsing.parser.jaxb.SQLToken> filterSqlToken(final List<io.shardingjdbc.core.parsing.parser.jaxb.SQLToken> sqlTokens,
            final boolean isPreparedStatement) {
        List<io.shardingjdbc.core.parsing.parser.jaxb.SQLToken> result = new ArrayList<>(sqlTokens.size());
        for (io.shardingjdbc.core.parsing.parser.jaxb.SQLToken each : sqlTokens) {
            if (isPreparedStatement && (each instanceof io.shardingjdbc.core.parsing.parser.jaxb.OffsetToken 
                    || each instanceof io.shardingjdbc.core.parsing.parser.jaxb.RowCountToken)) {
                continue;
            }
            result.add(each);
        }
        return result;
    }
    
    private static SQLToken buildExpectedSQLToken(final io.shardingjdbc.core.parsing.parser.jaxb.SQLToken sqlToken, final boolean isPreparedStatement) {
        if (sqlToken instanceof io.shardingjdbc.core.parsing.parser.jaxb.TableToken) {
            return new TableToken(sqlToken.getBeginPosition(), ((io.shardingjdbc.core.parsing.parser.jaxb.TableToken) sqlToken).getOriginalLiterals());
        } else if (sqlToken instanceof io.shardingjdbc.core.parsing.parser.jaxb.ItemsToken) {
            ItemsToken itemsToken = new ItemsToken(sqlToken.getBeginPosition());
            itemsToken.getItems().addAll(((io.shardingjdbc.core.parsing.parser.jaxb.ItemsToken) sqlToken).getItems());
            return itemsToken;
        } else if (sqlToken instanceof io.shardingjdbc.core.parsing.parser.jaxb.GeneratedKeyToken) {
            if (isPreparedStatement) {
                return new GeneratedKeyToken(((io.shardingjdbc.core.parsing.parser.jaxb.GeneratedKeyToken) sqlToken).getBeginPositionOfPreparedStatement());
            } else {
                return new GeneratedKeyToken(((io.shardingjdbc.core.parsing.parser.jaxb.GeneratedKeyToken) sqlToken).getBeginPositionOfStatement());
            }
        } else if (sqlToken instanceof io.shardingjdbc.core.parsing.parser.jaxb.MultipleInsertValuesToken) {
            MultipleInsertValuesToken multipleInsertValuesToken = new MultipleInsertValuesToken(sqlToken.getBeginPosition());
            multipleInsertValuesToken.getValues().addAll(((io.shardingjdbc.core.parsing.parser.jaxb.MultipleInsertValuesToken) sqlToken).getValues());
            return multipleInsertValuesToken;
        } else if (sqlToken instanceof io.shardingjdbc.core.parsing.parser.jaxb.RowCountToken) {
            return new RowCountToken(sqlToken.getBeginPosition(), ((io.shardingjdbc.core.parsing.parser.jaxb.RowCountToken) sqlToken).getRowCount());
        } else if (sqlToken instanceof io.shardingjdbc.core.parsing.parser.jaxb.OrderByToken) {
            if (isPreparedStatement) {
                return new OrderByToken(((io.shardingjdbc.core.parsing.parser.jaxb.OrderByToken) sqlToken).getBeginPositionOfPreparedStatement());
            } else {
                return new OrderByToken(((io.shardingjdbc.core.parsing.parser.jaxb.OrderByToken) sqlToken).getBeginPositionOfStatement());
            }
        } else if (sqlToken instanceof io.shardingjdbc.core.parsing.parser.jaxb.OffsetToken) {
            return new OffsetToken(sqlToken.getBeginPosition(), ((io.shardingjdbc.core.parsing.parser.jaxb.OffsetToken) sqlToken).getOffset());
        }
        return null;
    }
    
    public static void assertLimit(final io.shardingjdbc.core.parsing.parser.jaxb.Limit limit, final Limit actual, final boolean isPreparedStatement) {
        Limit expected = buildExpectedLimit(limit, isPreparedStatement);
        if (null == expected) {
            assertNull(actual);
            return;
        }
        if (null != expected.getRowCount()) {
            assertTrue(EqualsBuilder.reflectionEquals(expected.getRowCount(), actual.getRowCount()));
        }
        if (null != expected.getOffset()) {
            assertTrue(EqualsBuilder.reflectionEquals(expected.getOffset(), actual.getOffset()));
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
            assertTrue(EqualsBuilder.reflectionEquals(expectedOrderItem, each, "nullOrderType"));
        }
        assertFalse(orderByColumns.hasNext());
    }
    
    public static void assertGroupBy(final List<OrderItem> expected, final List<OrderItem> actual) {
        Iterator<OrderItem> groupByColumns = expected.iterator();
        for (OrderItem each : actual) {
            OrderItem groupByColumn = groupByColumns.next();
            // TODO assert nullOrderType
            assertTrue(EqualsBuilder.reflectionEquals(groupByColumn, each, "nullOrderType"));
        }
        assertFalse(groupByColumns.hasNext());
    }
    
    public static void assertAggregationSelectItem(final List<AggregationSelectItem> expected, final List<AggregationSelectItem> actual) {
        Iterator<AggregationSelectItem> aggregationSelectItems = expected.iterator();
        for (AggregationSelectItem each : actual) {
            AggregationSelectItem aggregationSelectItem = aggregationSelectItems.next();
            assertTrue(EqualsBuilder.reflectionEquals(aggregationSelectItem, each, "derivedAggregationSelectItems"));
            for (int i = 0; i < each.getDerivedAggregationSelectItems().size(); i++) {
                assertTrue(EqualsBuilder.reflectionEquals(aggregationSelectItem.getDerivedAggregationSelectItems().get(i), each.getDerivedAggregationSelectItems().get(i)));
            }
        }
        assertFalse(aggregationSelectItems.hasNext());
    }
    
}
