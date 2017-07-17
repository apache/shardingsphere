/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql;

import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.constant.ShardingOperator;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Column;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Condition;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Conditions;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.LimitValue;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Table;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Tables;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLTextExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Assert;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Asserts;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.GroupByColumn;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.OrderByColumn;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Value;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractBaseParseTest {
    
    @Getter(AccessLevel.PROTECTED)
    private final String sql;
    
    private final Tables expectedTables;
    
    private final Conditions expectedConditions;
    
    private final Iterator<OrderItem> expectedOrderByColumns;
    
    private final Iterator<OrderItem> expectedGroupByColumns;
    
    private final Iterator<AggregationSelectItem> expectedAggregationSelectItems;
    
    private final Limit expectedLimit;
    
    protected AbstractBaseParseTest(final String testCaseName, final String sql, final Tables expectedTables, final Conditions expectedConditions, final SQLStatement expectedSQLStatement) {
        this.sql = sql;
        this.expectedTables = expectedTables;
        this.expectedConditions = expectedConditions;
        if (expectedSQLStatement instanceof SelectStatement) {
            expectedOrderByColumns = ((SelectStatement) expectedSQLStatement).getOrderByItems().iterator();
            expectedGroupByColumns = ((SelectStatement) expectedSQLStatement).getGroupByItems().iterator();
            expectedAggregationSelectItems = ((SelectStatement) expectedSQLStatement).getAggregationSelectItems().iterator();
            expectedLimit = ((SelectStatement) expectedSQLStatement).getLimit();
        } else {
            expectedOrderByColumns = null;
            expectedGroupByColumns = null;
            expectedAggregationSelectItems = null;
            expectedLimit = null;
        }
    }
    
    protected static Collection<Object[]> dataParameters(final String path) {
        Collection<Object[]> result = new ArrayList<>();
        for (File each : new File(AbstractBaseParseTest.class.getClassLoader().getResource(path).getPath()).listFiles()) {
            result.addAll(dataParameters(each));
        }
        return result;
    }
    
    private static Collection<Object[]> dataParameters(final File file) {
        Asserts asserts = loadAsserts(file);
        Object[][] result = new Object[asserts.getAsserts().size()][6];
        for (int i = 0; i < asserts.getAsserts().size(); i++) {
            result[i] = getDataParameter(asserts.getAsserts().get(i));
        }
        return Arrays.asList(result);
    }
    
    private static Asserts loadAsserts(final File file) {
        try {
            return (Asserts) JAXBContext.newInstance(Asserts.class).createUnmarshaller().unmarshal(file);
        } catch (final JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static Object[] getDataParameter(final Assert assertObj) {
        final Object[] result = new Object[5];
        result[0] = assertObj.getId();
        result[1] = assertObj.getSql();
        result[2] = new Tables();
        if (null != assertObj.getTables()) {
            Tables tables = new Tables();
            for (com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Table each : assertObj.getTables().getTables()) {
                Table table = new Table(each.getName(), Optional.fromNullable(each.getAlias())); 
                tables.add(table);
            }
            result[2] = tables;
        }
        result[3] = new Conditions();
        if (null != assertObj.getConditions()) {
            Conditions conditions = new Conditions();
            for (com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Condition each : assertObj.getConditions().getConditions()) {
                List<SQLExpression> sqlExpressions = new LinkedList<>();
                for (Value value : each.getValues()) {
                    if (null != value.getIndex()) {
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
                conditions.add(condition);
            }
            result[3] = conditions;
        }
        final SelectStatement selectStatement = new SelectStatement();
        if (null != assertObj.getOrderByColumns()) {
            List<OrderItem> orderItems = Lists.transform(assertObj.getOrderByColumns(), new Function<OrderByColumn, OrderItem>() {
                
                @Override
                public OrderItem apply(final OrderByColumn input) {
                    if (Strings.isNullOrEmpty(input.getName())) {
                        return new OrderItem(input.getIndex(), OrderType.valueOf(input.getOrderByType().toUpperCase()));
                    }
                    if (Strings.isNullOrEmpty(input.getOwner())) {
                        return new OrderItem(input.getName(), OrderType.valueOf(input.getOrderByType().toUpperCase()), Optional.fromNullable(input.getAlias()));
                    }
                    return new OrderItem(input.getOwner(), input.getName(), OrderType.valueOf(input.getOrderByType().toUpperCase()), Optional.fromNullable(input.getAlias()));
                }
            });
            selectStatement.getOrderByItems().addAll(orderItems);
        }
        if (null != assertObj.getGroupByColumns()) {
            selectStatement.getGroupByItems().addAll(Lists.transform(assertObj.getGroupByColumns(), new Function<GroupByColumn, OrderItem>() {
                
                @Override
                public OrderItem apply(final GroupByColumn input) {
                    if (null == input.getOwner()) {
                        return new OrderItem(input.getName(), OrderType.valueOf(input.getOrderByType().toUpperCase()), Optional.fromNullable(input.getAlias()));
                    }
                    return new OrderItem(input.getOwner(), input.getName(), OrderType.valueOf(input.getOrderByType().toUpperCase()), Optional.fromNullable(input.getAlias())); 
                }
            }));
        }
        if (null != assertObj.getAggregationSelectItems()) {
            List<AggregationSelectItem> selectItems = Lists.transform(assertObj.getAggregationSelectItems(),
                    new Function<com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.AggregationSelectItem, AggregationSelectItem>() {
                        
                        @Override
                        public AggregationSelectItem apply(final com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.AggregationSelectItem input) {
                            AggregationSelectItem result = new AggregationSelectItem(
                                    AggregationType.valueOf(input.getAggregationType().toUpperCase()), input.getInnerExpression(), Optional.fromNullable(input.getAlias()));
                            for (com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.AggregationSelectItem each : input.getDerivedColumns()) {
                                result.getDerivedAggregationSelectItems().add(new AggregationSelectItem(
                                        AggregationType.valueOf(each.getAggregationType().toUpperCase()), each.getInnerExpression(), Optional.fromNullable(each.getAlias())));
                            }
                            return result;
                        }
                    });
            selectStatement.getItems().addAll(selectItems);
        }
        if (null != assertObj.getLimit()) {
            Limit limit = new Limit(true);
            if (null != assertObj.getLimit().getOffset() && null != assertObj.getLimit().getOffsetParameterIndex()) {
                limit.setRowCount(new LimitValue(assertObj.getLimit().getRowCount(), assertObj.getLimit().getRowCountParameterIndex()));
                limit.setOffset(new LimitValue(assertObj.getLimit().getOffset(), assertObj.getLimit().getOffsetParameterIndex()));
            } else {
                limit.setRowCount(new LimitValue(assertObj.getLimit().getRowCount(), assertObj.getLimit().getRowCountParameterIndex()));
            }
            selectStatement.setLimit(limit);
        }
        result[4] = selectStatement;
        return result;
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
        assertTrue(new ReflectionEquals(expectedTables).matches(actual.getTables()));
    }
    
    private void assertExpectedConditions(final SQLStatement actual) {
        assertTrue(new ReflectionEquals(expectedConditions).matches(actual.getConditions()));
    }
    
    private void assertOrderBy(final SelectStatement actual) {
        for (OrderItem each : actual.getOrderByItems()) {
            assertTrue(new ReflectionEquals(expectedOrderByColumns.next()).matches(each));
        }
        assertFalse(expectedOrderByColumns.hasNext());
    }
    
    private void assertGroupBy(final SelectStatement actual) {
        for (OrderItem each : actual.getGroupByItems()) {
            assertTrue(new ReflectionEquals(expectedGroupByColumns.next()).matches(each));
        }
        assertFalse(expectedGroupByColumns.hasNext());
    }
    
    private void assertAggregationSelectItem(final SelectStatement actual) {
        for (AggregationSelectItem each : actual.getAggregationSelectItems()) {
            AggregationSelectItem expected = expectedAggregationSelectItems.next();
            assertTrue(new ReflectionEquals(expected, "derivedAggregationSelectItems").matches(each));
            for (int i = 0; i < each.getDerivedAggregationSelectItems().size(); i++) {
                assertTrue(new ReflectionEquals(expected.getDerivedAggregationSelectItems().get(i)).matches(each.getDerivedAggregationSelectItems().get(i)));
            }
        }
        assertFalse(expectedAggregationSelectItems.hasNext());
    }
    
    private void assertLimit(final SelectStatement actual) {
        if (null != actual.getLimit()) {
            if (null != actual.getLimit().getOffset()) {
                assertTrue(new ReflectionEquals(expectedLimit.getOffset()).matches(actual.getLimit().getOffset()));
            }
            if (null != actual.getLimit().getRowCount()) {
                assertTrue(new ReflectionEquals(expectedLimit.getRowCount()).matches(actual.getLimit().getRowCount()));
            }
        }
    }
}
