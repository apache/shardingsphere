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

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.constant.ShardingOperator;
import io.shardingjdbc.core.parsing.integrate.jaxb.condition.ConditionAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.condition.Value;
import io.shardingjdbc.core.parsing.integrate.jaxb.limit.LimitAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.GeneratedKeyTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.IndexTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.ItemsTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.MultipleInsertValuesTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.OffsetTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.OrderByTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.RowCountTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.SQLTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.TableTokenAssert;
import io.shardingjdbc.core.parsing.parser.context.OrderItem;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.context.condition.Condition;
import io.shardingjdbc.core.parsing.parser.context.condition.Conditions;
import io.shardingjdbc.core.parsing.parser.context.limit.Limit;
import io.shardingjdbc.core.parsing.parser.context.limit.LimitValue;
import io.shardingjdbc.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingjdbc.core.parsing.parser.expression.SQLExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLTextExpression;
import io.shardingjdbc.core.parsing.parser.token.GeneratedKeyToken;
import io.shardingjdbc.core.parsing.parser.token.IndexToken;
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
    
    public static void assertConditions(
            final List<ConditionAssert> expected, final Conditions actual, final boolean isPreparedStatement) {
        assertTrue(EqualsBuilder.reflectionEquals(buildExpectedConditions(expected, isPreparedStatement), actual));
    }
    
    private static Conditions buildExpectedConditions(final List<ConditionAssert> conditions, final boolean isPreparedStatement) {
        Conditions result = new Conditions();
        if (null == conditions) {
            return result;
        }
        for (ConditionAssert each : conditions) {
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
    
    public static void assertSqlTokens(final List<SQLTokenAssert> expected, final List<SQLToken> actual, final boolean isPreparedStatement) {
        if (null == expected || expected.size() == 0) {
            return;
        }
        List<SQLToken> expectedSqlTokens = buildExpectedSqlTokens(expected, isPreparedStatement);
        assertTrue(expectedSqlTokens.size() == actual.size());
        for (SQLToken each : actual) {
            boolean hasData = false;
            for (SQLToken sqlToken : expectedSqlTokens) {
                if (each.getBeginPosition() == sqlToken.getBeginPosition()) {
                    hasData = true;
                    assertTrue(EqualsBuilder.reflectionEquals(sqlToken, each));
                }
            }
            assertTrue(hasData);
        }
    }
    
    private static List<SQLToken> buildExpectedSqlTokens(final List<SQLTokenAssert> sqlTokens,
            final boolean isPreparedStatement) {
        List<SQLToken> result = new ArrayList<>(sqlTokens.size());
        for (SQLTokenAssert each : sqlTokens) {
            if (isPreparedStatement && (each instanceof OffsetTokenAssert 
                    || each instanceof RowCountTokenAssert)) {
                continue;
            }
            result.add(buildExpectedSQLToken(each, isPreparedStatement));
        }
        return result;
    }
    
    private static SQLToken buildExpectedSQLToken(final SQLTokenAssert sqlToken, final boolean isPreparedStatement) {
        if (sqlToken instanceof TableTokenAssert) {
            return new TableToken(sqlToken.getBeginPosition(), ((TableTokenAssert) sqlToken).getOriginalLiterals());
        }
        if (sqlToken instanceof IndexTokenAssert) {
            return new IndexToken(sqlToken.getBeginPosition(), ((IndexTokenAssert) sqlToken).getOriginalLiterals(), 
                    ((IndexTokenAssert) sqlToken).getTableName());
        } else if (sqlToken instanceof ItemsTokenAssert) {
            ItemsToken itemsToken = new ItemsToken(sqlToken.getBeginPosition());
            itemsToken.getItems().addAll(((ItemsTokenAssert) sqlToken).getItems());
            return itemsToken;
        } else if (sqlToken instanceof GeneratedKeyTokenAssert) {
            if (isPreparedStatement) {
                return new GeneratedKeyToken(((GeneratedKeyTokenAssert) sqlToken).getBeginPositionOfPreparedStatement());
            } else {
                return new GeneratedKeyToken(((GeneratedKeyTokenAssert) sqlToken).getBeginPositionOfStatement());
            }
        } else if (sqlToken instanceof MultipleInsertValuesTokenAssert) {
            MultipleInsertValuesToken multipleInsertValuesToken = new MultipleInsertValuesToken(sqlToken.getBeginPosition());
            multipleInsertValuesToken.getValues().addAll(((MultipleInsertValuesTokenAssert) sqlToken).getValues());
            return multipleInsertValuesToken;
        } else if (sqlToken instanceof RowCountTokenAssert) {
            return new RowCountToken(sqlToken.getBeginPosition(), ((RowCountTokenAssert) sqlToken).getRowCount());
        } else if (sqlToken instanceof OrderByTokenAssert) {
            if (isPreparedStatement) {
                return new OrderByToken(((OrderByTokenAssert) sqlToken).getBeginPositionOfPreparedStatement());
            } else {
                return new OrderByToken(((OrderByTokenAssert) sqlToken).getBeginPositionOfStatement());
            }
        } else if (sqlToken instanceof OffsetTokenAssert) {
            return new OffsetToken(sqlToken.getBeginPosition(), ((OffsetTokenAssert) sqlToken).getOffset());
        }
        return null;
    }
    
    public static void assertLimit(final LimitAssert limit, final Limit actual, final boolean isPreparedStatement) {
        Limit expected = buildExpectedLimit(limit, isPreparedStatement);
        if (null == expected) {
            assertNull(actual);
            return;
        }
        if (null != expected.getRowCount()) {
            assertTrue(EqualsBuilder.reflectionEquals(expected.getRowCount(), actual.getRowCount(), "boundOpened"));
        }
        if (null != expected.getOffset()) {
            assertTrue(EqualsBuilder.reflectionEquals(expected.getOffset(), actual.getOffset(), "boundOpened"));
        }
    }
    
    private static Limit buildExpectedLimit(final LimitAssert limit, final boolean isPreparedStatement) {
        if (null == limit) {
            return null;
        }
        Limit result = new Limit(DatabaseType.MySQL);
        if (isPreparedStatement) {
            if (null != limit.getOffsetParameterIndex()) {
                result.setOffset(new LimitValue(-1, limit.getOffsetParameterIndex(), true));
            }
            if (null != limit.getRowCountParameterIndex()) {
                result.setRowCount(new LimitValue(-1, limit.getRowCountParameterIndex(), false));
            }
        } else {
            if (null != limit.getOffset()) {
                result.setOffset(new LimitValue(limit.getOffset(), -1, true));
                
            }
            if (null != limit.getRowCount()) {
                result.setRowCount(new LimitValue(limit.getRowCount(), -1, false));
            }
        }
        return result;
    }
    
    public static void assertOrderBy(final List<OrderItem> expected, final List<OrderItem> actual) {
        Iterator<OrderItem> orderByColumns = expected.iterator();
        for (OrderItem each : actual) {
            OrderItem expectedOrderItem = orderByColumns.next();
            // TODO assert nullOrderType
            assertTrue(EqualsBuilder.reflectionEquals(expectedOrderItem, each, "nullOrderDirection"));
        }
        assertFalse(orderByColumns.hasNext());
    }
    
    public static void assertGroupBy(final List<OrderItem> expected, final List<OrderItem> actual) {
        Iterator<OrderItem> groupByColumns = expected.iterator();
        for (OrderItem each : actual) {
            OrderItem groupByColumn = groupByColumns.next();
            // TODO assert nullOrderType
            assertTrue(EqualsBuilder.reflectionEquals(groupByColumn, each, "nullOrderDirection"));
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
