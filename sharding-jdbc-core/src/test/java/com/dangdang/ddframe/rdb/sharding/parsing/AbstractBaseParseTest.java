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

package com.dangdang.ddframe.rdb.sharding.parsing;

import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.constant.ShardingOperator;
import com.dangdang.ddframe.rdb.sharding.parsing.jaxb.Assert;
import com.dangdang.ddframe.rdb.sharding.parsing.jaxb.Asserts;
import com.dangdang.ddframe.rdb.sharding.parsing.jaxb.Value;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.AggregationSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GroupByContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.LimitContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderByContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ShardingColumnContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.TableContext;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public abstract class AbstractBaseParseTest {
    
    @Getter(AccessLevel.PROTECTED)
    private final String testCaseName;
    
    @Getter(AccessLevel.PROTECTED)
    private final String sql;
    
    private final String expectedSQL;
    
    private final Iterator<TableContext> expectedTables;
    
    private final Iterator<ConditionContext> expectedConditionContexts;
    
    private final Iterator<OrderByContext> orderByContexts;
    
    private final Iterator<GroupByContext> groupByContexts;
    
    private final Iterator<AggregationSelectItemContext> aggregationColumns;
    
    private final LimitContext limit;
    
    protected AbstractBaseParseTest(final String testCaseName, final String sql, final String expectedSQL,
                                 final Collection<TableContext> expectedTables, final Collection<ConditionContext> expectedConditionContext, final SQLContext expectedSQLContext) {
        this.testCaseName = testCaseName;
        this.sql = sql;
        this.expectedSQL = expectedSQL;
        this.expectedTables = expectedTables.iterator();
        this.expectedConditionContexts = expectedConditionContext.iterator();
        this.orderByContexts = expectedSQLContext.getOrderByContexts().iterator();
        this.groupByContexts = expectedSQLContext.getGroupByContexts().iterator();
        this.aggregationColumns = expectedSQLContext.getAggregationSelectItemContexts().iterator();
        this.limit = expectedSQLContext.getLimitContext();
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
        result[3] = Lists.transform(assertObj.getTables(), new Function<com.dangdang.ddframe.rdb.sharding.parsing.jaxb.Table, TableContext>() {
            
            @Override
            public TableContext apply(final com.dangdang.ddframe.rdb.sharding.parsing.jaxb.Table input) {
                return new TableContext(input.getName(), Optional.of(input.getAlias()));
            }
        });
        if (null == assertObj.getConditionContexts()) {
            result[4] = Collections.<ConditionContext>emptyList();
        } else {
            result[4] = Lists.transform(assertObj.getConditionContexts(), new Function<com.dangdang.ddframe.rdb.sharding.parsing.jaxb.ConditionContext, ConditionContext>() {
                
                @Override
                public ConditionContext apply(final com.dangdang.ddframe.rdb.sharding.parsing.jaxb.ConditionContext input) {
                    ConditionContext result = new ConditionContext();
                    if (null == input.getConditions()) {
                        return result;
                    }
                    for (com.dangdang.ddframe.rdb.sharding.parsing.jaxb.Condition each : input.getConditions()) {
                        ConditionContext.Condition condition = new ConditionContext.Condition(
                                new ShardingColumnContext(each.getColumnName(), each.getTableName()), ShardingOperator.valueOf(each.getOperator().toUpperCase()));
                        condition.getValues().addAll(Lists.transform(each.getValues(), new Function<Value, Comparable<?>>() {
                            
                            @Override
                            public Comparable<?> apply(final Value input) {
                                return input.getValueWithType();
                            }
                        }));
                        if (null != each.getValueIndices()) {
                            condition.getValueIndices().addAll(each.getValueIndices());
                        }
                        result.add(condition);
                    }
                    return result;
                }
            });
        }
        SQLContext sqlContext = new SelectSQLContext();
        if (null != assertObj.getOrderByColumns()) {
            sqlContext.getOrderByContexts().addAll(Lists.transform(assertObj.getOrderByColumns(), new Function<com.dangdang.ddframe.rdb.sharding.parsing.jaxb.OrderByColumn, OrderByContext>() {
                
                @Override
                public OrderByContext apply(final com.dangdang.ddframe.rdb.sharding.parsing.jaxb.OrderByColumn input) {
                    return Strings.isNullOrEmpty(input.getName()) ? new OrderByContext(input.getIndex(), OrderType.valueOf(input.getOrderByType().toUpperCase())) 
                            : new OrderByContext(input.getOwner(), input.getName(), OrderType.valueOf(input.getOrderByType().toUpperCase()), Optional.fromNullable(input.getAlias()));
                }
            }));
        }
        if (null != assertObj.getGroupByColumns()) {
            sqlContext.getGroupByContexts().addAll(Lists.transform(assertObj.getGroupByColumns(), new Function<com.dangdang.ddframe.rdb.sharding.parsing.jaxb.GroupByColumn, GroupByContext>() {
                
                @Override
                public GroupByContext apply(final com.dangdang.ddframe.rdb.sharding.parsing.jaxb.GroupByColumn input) {
                    GroupByContext groupByContext = new GroupByContext(
                            Optional.fromNullable(input.getOwner()), input.getName(), OrderType.valueOf(input.getOrderByType().toUpperCase()), Optional.fromNullable(input.getAlias()));
                    return groupByContext;
                }
            }));
        }
        if (null != assertObj.getAggregationColumns()) {
            sqlContext.getAggregationSelectItemContexts().addAll(Lists.transform(assertObj.getAggregationColumns(), 
                    new Function<com.dangdang.ddframe.rdb.sharding.parsing.jaxb.AggregationColumn, AggregationSelectItemContext>() {
                        
                        @Override
                        public AggregationSelectItemContext apply(final com.dangdang.ddframe.rdb.sharding.parsing.jaxb.AggregationColumn input) {
                            AggregationSelectItemContext result = new AggregationSelectItemContext(input.getExpression(), Optional.fromNullable(input.getAlias()), -1, 
                                    AggregationType.valueOf(input.getAggregationType().toUpperCase()));
                            if (null != input.getIndex()) {
                                result.setColumnIndex(input.getIndex());
                            }
                            for (com.dangdang.ddframe.rdb.sharding.parsing.jaxb.AggregationColumn each : input.getDerivedColumns()) {
                                result.getDerivedAggregationSelectItemContexts().add(new AggregationSelectItemContext(each.getExpression(), Optional.fromNullable(each.getAlias()), -1, 
                                        AggregationType.valueOf(each.getAggregationType().toUpperCase())));
                            }
                            return result;
                        }
                }));
        }
        if (null != assertObj.getLimit()) {
            sqlContext.setLimitContext(new LimitContext(
                    assertObj.getLimit().getOffset(), assertObj.getLimit().getRowCount(), assertObj.getLimit().getOffsetParameterIndex(), assertObj.getLimit().getRowCountParameterIndex()));
        }
        result[5] = sqlContext;
        return result;
    }
}
