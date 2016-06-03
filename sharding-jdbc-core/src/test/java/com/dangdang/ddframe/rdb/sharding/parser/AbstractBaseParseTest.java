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

package com.dangdang.ddframe.rdb.sharding.parser;

import com.dangdang.ddframe.rdb.sharding.parser.jaxb.Assert;
import com.dangdang.ddframe.rdb.sharding.parser.jaxb.Asserts;
import com.dangdang.ddframe.rdb.sharding.parser.jaxb.Value;
import com.dangdang.ddframe.rdb.sharding.parser.result.SQLParsedResult;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.GroupByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.Limit;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.MergeContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn.OrderByType;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition.BinaryOperator;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition.Column;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Table;
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
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public abstract class AbstractBaseParseTest {
    
    @Getter(AccessLevel.PROTECTED)
    private final String testCaseName;
    
    @Getter(AccessLevel.PROTECTED)
    private final String sql;
    
    private final String expectedSQL;
    
    private final Iterator<Table> expectedTables;
    
    private final Iterator<ConditionContext> expectedConditionContexts;
    
    private final Iterator<OrderByColumn> orderByColumns;
    
    private final Iterator<GroupByColumn> groupByColumns;
    
    private final Iterator<AggregationColumn> aggregationColumns;
    
    private final Limit limit;
    
    protected AbstractBaseParseTest(final String testCaseName, final String sql, final String expectedSQL,
                                 final Collection<Table> expectedTables, final Collection<ConditionContext> expectedConditionContext, final MergeContext expectedMergeContext) {
        this.testCaseName = testCaseName;
        this.sql = sql;
        this.expectedSQL = expectedSQL;
        this.expectedTables = expectedTables.iterator();
        this.expectedConditionContexts = expectedConditionContext.iterator();
        this.orderByColumns = expectedMergeContext.getOrderByColumns().iterator();
        this.groupByColumns = expectedMergeContext.getGroupByColumns().iterator();
        this.aggregationColumns = expectedMergeContext.getAggregationColumns().iterator();
        this.limit = expectedMergeContext.getLimit();
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
        Object[] result = new Object[6];
        result[0] = assertObj.getId();
        result[1] = assertObj.getSql();
        result[2] = assertObj.getExpectedSQL();
        result[3] = Lists.transform(assertObj.getTables(), new Function<com.dangdang.ddframe.rdb.sharding.parser.jaxb.Table, Table>() {
            
            @Override
            public Table apply(final com.dangdang.ddframe.rdb.sharding.parser.jaxb.Table input) {
                return new Table(input.getName(), input.getAlias());
            }
        });
        if (null == assertObj.getConditionContexts()) {
            result[4] = Collections.<ConditionContext>emptyList();
        } else {
            result[4] = Lists.transform(assertObj.getConditionContexts(), new Function<com.dangdang.ddframe.rdb.sharding.parser.jaxb.ConditionContext, ConditionContext>() {
                
                @Override
                public ConditionContext apply(final com.dangdang.ddframe.rdb.sharding.parser.jaxb.ConditionContext input) {
                    ConditionContext result = new ConditionContext();
                    if (null == input.getConditions()) {
                        return result;
                    }
                    for (com.dangdang.ddframe.rdb.sharding.parser.jaxb.Condition each : input.getConditions()) {
                        Condition condition = new Condition(new Column(each.getColumnName(), each.getTableName()), BinaryOperator.valueOf(each.getOperator().toUpperCase()));
                        condition.getValues().addAll(Lists.transform(each.getValues(), new Function<Value, Comparable<?>>() {
                            
                            @Override
                            public Comparable<?> apply(final Value input) {
                                return input.getValueWithType();
                            }
                        }));
                        result.add(condition);
                    }
                    return result;
                }
            });
        }
        MergeContext mergeContext = new MergeContext();
        if (null != assertObj.getOrderByColumns()) {
            mergeContext.getOrderByColumns().addAll(Lists.transform(assertObj.getOrderByColumns(), new Function<com.dangdang.ddframe.rdb.sharding.parser.jaxb.OrderByColumn, OrderByColumn>() {
                
                @Override
                public OrderByColumn apply(final com.dangdang.ddframe.rdb.sharding.parser.jaxb.OrderByColumn input) {
                    return Strings.isNullOrEmpty(input.getName()) ? new OrderByColumn(input.getIndex(), OrderByType.valueOf(input.getOrderByType().toUpperCase())) 
                            : new OrderByColumn(Optional.fromNullable(input.getOwner()), input.getName(), 
                                    Optional.fromNullable(input.getAlias()), OrderByType.valueOf(input.getOrderByType().toUpperCase()));
                }
            }));
        }
        if (null != assertObj.getGroupByColumns()) {
            mergeContext.getGroupByColumns().addAll(Lists.transform(assertObj.getGroupByColumns(), new Function<com.dangdang.ddframe.rdb.sharding.parser.jaxb.GroupByColumn, GroupByColumn>() {
                
                @Override
                public GroupByColumn apply(final com.dangdang.ddframe.rdb.sharding.parser.jaxb.GroupByColumn input) {
                    return new GroupByColumn(
                            Optional.fromNullable(input.getOwner()), input.getName(), Optional.fromNullable(input.getAlias()), OrderByType.valueOf(input.getOrderByType().toUpperCase()));
                }
            }));
        }
        if (null != assertObj.getAggregationColumns()) {
            mergeContext.getAggregationColumns().addAll(Lists.transform(assertObj.getAggregationColumns(), 
                    new Function<com.dangdang.ddframe.rdb.sharding.parser.jaxb.AggregationColumn, AggregationColumn>() {
                        
                        @Override
                        public AggregationColumn apply(final com.dangdang.ddframe.rdb.sharding.parser.jaxb.AggregationColumn input) {
                            AggregationColumn result = new AggregationColumn(input.getExpression(), 
                                    AggregationType.valueOf(input.getAggregationType().toUpperCase()), Optional.fromNullable(input.getAlias()), Optional.fromNullable(input.getOption()));
                            if (null != input.getIndex()) {
                                result.setColumnIndex(input.getIndex());
                            }
                            for (com.dangdang.ddframe.rdb.sharding.parser.jaxb.AggregationColumn each : input.getDerivedColumns()) {
                                result.getDerivedColumns().add(new AggregationColumn(each.getExpression(), 
                                        AggregationType.valueOf(each.getAggregationType().toUpperCase()), Optional.fromNullable(each.getAlias()), Optional.fromNullable(each.getOption())));
                            }
                            return result;
                        }
                }));
        }
        if (null != assertObj.getLimit()) {
            mergeContext.setLimit(new Limit(assertObj.getLimit().getOffset(), assertObj.getLimit().getRowCount()));
        }
        result[5] = mergeContext;
        return result;
    }
    
    protected final void assertSQLParsedResult(final SQLParsedResult actual) {
        assertRouteContext(actual);
        assertConditionContexts(actual);
        assertMergeContext(actual);
    }
    
    private void assertRouteContext(final SQLParsedResult actual) {
        assertThat(actual.getRouteContext().getSqlBuilder().toString(), is(expectedSQL));
        for (Table each : actual.getRouteContext().getTables()) {
            assertThat(each, new ReflectionEquals(expectedTables.next()));
        }
        assertFalse(expectedTables.hasNext());
    }
    
    private void assertConditionContexts(final SQLParsedResult actual) {
        for (ConditionContext each : actual.getConditionContexts()) {
            assertThat(each, is(new ReflectionEquals(expectedConditionContexts.next())));
        }
        assertFalse(expectedConditionContexts.hasNext());
    }
    
    private void assertMergeContext(final SQLParsedResult actual) {
        for (OrderByColumn each : actual.getMergeContext().getOrderByColumns()) {
            assertThat(each, new ReflectionEquals(orderByColumns.next()));
        }
        assertFalse(orderByColumns.hasNext());
        for (GroupByColumn each : actual.getMergeContext().getGroupByColumns()) {
            assertThat(each, new ReflectionEquals(groupByColumns.next()));
        }
        assertFalse(groupByColumns.hasNext());
        for (AggregationColumn each : actual.getMergeContext().getAggregationColumns()) {
            AggregationColumn expected = aggregationColumns.next();
            assertThat(each, new ReflectionEquals(expected, "derivedColumns"));
            for (int i = 0; i < each.getDerivedColumns().size(); i++) {
                assertThat(each.getDerivedColumns().get(i), new ReflectionEquals(expected.getDerivedColumns().get(i)));
            }
        }
        assertFalse(aggregationColumns.hasNext());
        assertThat(actual.getMergeContext().getLimit(), new ReflectionEquals(limit));
    }
}
