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
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.Column;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GroupBy;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderBy;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Condition;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Conditions;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.OffsetLimit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.RowCountLimit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Table;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Tables;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLTextExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Assert;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Asserts;
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
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractBaseParseTest {
    
    @Getter(AccessLevel.PROTECTED)
    private final String sql;
    
    private final Tables expectedTables;
    
    private final Conditions expectedConditions;
    
    private final Iterator<OrderBy> orderByColumns;
    
    private final Iterator<GroupBy> groupByColumns;
    
    private final Iterator<AggregationSelectItem> aggregationSelectItems;
    
    private final Limit limit;
    
    protected AbstractBaseParseTest(final String testCaseName, final String sql, final Tables expectedTables, 
                                    final Conditions expectedConditions, final SQLStatement expectedSQLStatement) {
        this.sql = sql;
        this.expectedTables = expectedTables;
        this.expectedConditions = expectedConditions;
        this.orderByColumns = expectedSQLStatement.getOrderByList().iterator();
        this.groupByColumns = expectedSQLStatement.getGroupByList().iterator();
        this.aggregationSelectItems = expectedSQLStatement.getAggregationSelectItems().iterator();
        this.limit = expectedSQLStatement.getLimit();
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
            List<OrderBy> orderBys = Lists.transform(assertObj.getOrderByColumns(), new Function<com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.OrderByColumn, OrderBy>() {
        
                @Override
                public OrderBy apply(final com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.OrderByColumn input) {
                    return Strings.isNullOrEmpty(input.getName()) ? new OrderBy(input.getIndex(), OrderType.valueOf(input.getOrderByType().toUpperCase()))
                            : new OrderBy(input.getOwner(), input.getName(), OrderType.valueOf(input.getOrderByType().toUpperCase()), Optional.fromNullable(input.getAlias()));
                }
            });
            selectStatement.getOrderByList().addAll(orderBys);
        }
        if (null != assertObj.getGroupByColumns()) {
            selectStatement.getGroupByList().addAll(Lists.transform(assertObj.getGroupByColumns(), new Function<com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.GroupByColumn, GroupBy>() {
                
                @Override
                public GroupBy apply(final com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.GroupByColumn input) {
                    return new GroupBy(
                            Optional.fromNullable(input.getOwner()), input.getName(), OrderType.valueOf(input.getOrderByType().toUpperCase()), Optional.fromNullable(input.getAlias()));
                }
            }));
        }
        if (null != assertObj.getAggregationSelectItems()) {
            List<AggregationSelectItem> selectItems = Lists.transform(assertObj.getAggregationSelectItems(),
                    new Function<com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.AggregationSelectItem, AggregationSelectItem>() {
                
                        @Override
                        public AggregationSelectItem apply(final com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.AggregationSelectItem input) {
                            AggregationSelectItem result = new AggregationSelectItem(input.getInnerExpression(), Optional.fromNullable(input.getAlias()), -1,
                                    AggregationType.valueOf(input.getAggregationType().toUpperCase()));
                            if (null != input.getIndex()) {
                                result.setColumnIndex(input.getIndex());
                            }
                            for (com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.AggregationSelectItem each : input.getDerivedColumns()) {
                                result.getDerivedAggregationSelectItems().add(new AggregationSelectItem(each.getInnerExpression(), Optional.fromNullable(each.getAlias()), -1,
                                        AggregationType.valueOf(each.getAggregationType().toUpperCase())));
                            }
                            return result;
                        }
                    });
            selectStatement.getItems().addAll(selectItems);
        }
        if (null != assertObj.getLimit()) {
            if (null != assertObj.getLimit().getOffset() && null != assertObj.getLimit().getOffsetParameterIndex()) {
                selectStatement.setLimit(new Limit(
                        new OffsetLimit(assertObj.getLimit().getOffset(), assertObj.getLimit().getOffsetParameterIndex()), new RowCountLimit(assertObj.getLimit().getRowCount(), assertObj.getLimit().getRowCountParameterIndex())));
            } else {
                selectStatement.setLimit(new Limit(new RowCountLimit(assertObj.getLimit().getRowCount(), assertObj.getLimit().getRowCountParameterIndex())));
            }
        }
        result[4] = selectStatement;
        return result;
    }
    
    protected final void assertSQLStatement(final SQLStatement actual) {
        assertExpectedTables(actual);
        assertExpectedConditions(actual);
        assertOrderBy(actual);
        assertGroupBy(actual);
        assertAggregationSelectItem(actual);
        assertLimit(actual);
    }
    
    private void assertExpectedTables(final SQLStatement actual) {
        assertTrue(new ReflectionEquals(expectedTables).matches(actual.getTables()));
    }
    
    private void assertExpectedConditions(final SQLStatement actual) {
        assertTrue(new ReflectionEquals(expectedConditions).matches(actual.getConditions()));
    }
    
    private void assertOrderBy(final SQLStatement actual) {
        for (OrderBy each : actual.getOrderByList()) {
            assertTrue(new ReflectionEquals(orderByColumns.next()).matches(each));
        }
        assertFalse(orderByColumns.hasNext());
    }
    
    private void assertGroupBy(final SQLStatement actual) {
        for (GroupBy each : actual.getGroupByList()) {
            assertTrue(new ReflectionEquals(groupByColumns.next()).matches(each));
        }
        assertFalse(groupByColumns.hasNext());
    }
    
    private void assertAggregationSelectItem(final SQLStatement actual) {
        for (AggregationSelectItem each : actual.getAggregationSelectItems()) {
            AggregationSelectItem expected = aggregationSelectItems.next();
            assertTrue(new ReflectionEquals(expected, "derivedColumns").matches(each));
            for (int i = 0; i < each.getDerivedAggregationSelectItems().size(); i++) {
                assertTrue(new ReflectionEquals(expected.getDerivedAggregationSelectItems().get(i)).matches(each.getDerivedAggregationSelectItems().get(i)));
            }
        }
        assertFalse(aggregationSelectItems.hasNext());
    }
    
    private void assertLimit(final SQLStatement actual) {
        if (null != actual.getLimit()) {
            if (null != actual.getLimit().getOffsetLimit()) {
                assertTrue(new ReflectionEquals(limit.getOffsetLimit()).matches(actual.getLimit().getOffsetLimit()));
            }
            if (null != actual.getLimit().getRowCountLimit()) {
                assertTrue(new ReflectionEquals(limit.getRowCountLimit()).matches(actual.getLimit().getRowCountLimit()));
            }
        }
    }
}
