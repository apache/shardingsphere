/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.engine;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.AlwaysFalseRouteValue;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.AlwaysFalseShardingCondition;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.ShardingCondition;
import org.apache.shardingsphere.core.parse.filler.common.dml.PredicateUtils;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.SubqueryPredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RangeRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sharding condition engine for where clause.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class WhereClauseShardingConditionEngine {
    
    private final ShardingRule shardingRule;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    /**
     * Create sharding conditions.
     * 
     * @param dmlStatement DML statement
     * @param parameters SQL parameters
     * @return sharding conditions
     */
    public Collection<ShardingCondition> createShardingConditions(final DMLStatement dmlStatement, final List<Object> parameters) {
        Collection<ShardingCondition> result = new LinkedList<>();
        Optional<OrPredicateSegment> orPredicateSegment = dmlStatement.findSQLSegment(OrPredicateSegment.class);
        if (orPredicateSegment.isPresent()) {
            result.addAll(createShardingConditions(dmlStatement, parameters, orPredicateSegment.get()));
        }
        Optional<SubqueryPredicateSegment> subqueryPredicateSegment = dmlStatement.findSQLSegment(SubqueryPredicateSegment.class);
        if (subqueryPredicateSegment.isPresent()) {
            for (OrPredicateSegment each : subqueryPredicateSegment.get().getOrPredicates()) {
                List<ShardingCondition> subqueryShardingConditions = createShardingConditions(dmlStatement, parameters, each);
                if (!result.containsAll(subqueryShardingConditions)) {
                    result.addAll(subqueryShardingConditions);
                }
            }
        }
        return result;
    }
    
    private List<ShardingCondition> createShardingConditions(final DMLStatement dmlStatement, final List<Object> parameters, final OrPredicateSegment orPredicateSegment) {
        List<ShardingCondition> result = new LinkedList<>();
        for (AndPredicate each : orPredicateSegment.getAndPredicates()) {
            Map<Column, List<RouteValue>> routeValueMap = createRouteValueMap(dmlStatement, parameters, each);
            if (routeValueMap.isEmpty()) {
                return Collections.emptyList();
            }
            result.add(createShardingCondition(routeValueMap));
        }
        return result;
    }
    
    private Map<Column, List<RouteValue>> createRouteValueMap(final DMLStatement dmlStatement, final List<Object> parameters, final AndPredicate andPredicate) {
        Map<Column, List<RouteValue>> result = new HashMap<>();
        for (PredicateSegment each : andPredicate.getPredicates()) {
            Optional<String> tableName = PredicateUtils.findTableName(each, dmlStatement, shardingTableMetaData);
            if (!tableName.isPresent() || !shardingRule.isShardingColumn(each.getColumn().getName(), tableName.get())) {
                continue;
            }
            Optional<RouteValue> routeValue = createRouteValue(parameters, each, tableName.get());
            if (!routeValue.isPresent()) {
                continue;
            }
            Column column = new Column(routeValue.get().getColumnName(), routeValue.get().getTableName());
            if (!result.containsKey(column)) {
                result.put(column, new LinkedList<RouteValue>());
            }
            result.get(column).add(routeValue.get());
        }
        return result;
    }
    
    private Optional<RouteValue> createRouteValue(final List<Object> parameters, final PredicateSegment predicateSegment, final String tableName) {
        String columnName = predicateSegment.getColumn().getName();
        if (predicateSegment.getRightValue() instanceof PredicateCompareRightValue) {
            return getRouteValue(parameters, (PredicateCompareRightValue) predicateSegment.getRightValue(), columnName, tableName);
        }
        if (predicateSegment.getRightValue() instanceof PredicateInRightValue) {
            return getRouteValue(parameters, (PredicateInRightValue) predicateSegment.getRightValue(), columnName, tableName);
        }
        if (predicateSegment.getRightValue() instanceof PredicateInRightValue) {
            return getRouteValue(parameters, (PredicateInRightValue) predicateSegment.getRightValue(), columnName, tableName);
        }
        if (predicateSegment.getRightValue() instanceof PredicateBetweenRightValue) {
            return getRouteValue(parameters, (PredicateBetweenRightValue) predicateSegment.getRightValue(), columnName, tableName);
        }
        return Optional.absent();
    }
    
    private Optional<RouteValue> getRouteValue(final List<Object> parameters, final PredicateCompareRightValue predicateRightValue, final String columnName, final String tableName) {
        if (!isOperatorSupportedWithSharding(predicateRightValue.getOperator())) {
            return Optional.absent();
        }
        Optional<Comparable> routeValue = getRouteValue(parameters, predicateRightValue.getExpression());
        return routeValue.isPresent() ? Optional.<RouteValue>of(new ListRouteValue<>(columnName, tableName, Lists.newArrayList(routeValue.get()))) : Optional.<RouteValue>absent();
    }
    
    private Optional<RouteValue> getRouteValue(final List<Object> parameters, final PredicateInRightValue predicateRightValue, final String columnName, final String tableName) {
        List<Comparable> routeValues = new LinkedList<>();
        for (ExpressionSegment each : predicateRightValue.getSqlExpressions()) {
            Optional<Comparable> routeValue = getRouteValue(parameters, each);
            if (routeValue.isPresent()) {
                routeValues.add(routeValue.get());
            }
        }
        return routeValues.isEmpty() ? Optional.<RouteValue>absent() : Optional.<RouteValue>of(new ListRouteValue<>(columnName, tableName, routeValues));
    }
    
    private Optional<RouteValue> getRouteValue(final List<Object> parameters, final PredicateBetweenRightValue predicateRightValue, final String columnName, final String tableName) {
        Optional<Comparable> betweenRouteValue = getRouteValue(parameters, predicateRightValue.getBetweenExpression());
        Optional<Comparable> andRouteValue = getRouteValue(parameters, predicateRightValue.getAndExpression());
        return betweenRouteValue.isPresent() && andRouteValue.isPresent()
                ? Optional.<RouteValue>of(new RangeRouteValue<>(columnName, tableName, Range.closed(betweenRouteValue.get(), andRouteValue.get()))) : Optional.<RouteValue>absent();
    }
    
    private boolean isOperatorSupportedWithSharding(final String operator) {
        return "=".equals(operator);
    }
    
    private Optional<Comparable> getRouteValue(final List<Object> parameters, final ExpressionSegment expression) {
        Object result = null;
        if (expression instanceof ParameterMarkerExpressionSegment) {
            result = parameters.get(((ParameterMarkerExpressionSegment) expression).getParameterMarkerIndex());
        }
        if (expression instanceof LiteralExpressionSegment) {
            result = ((LiteralExpressionSegment) expression).getLiterals();
        }
        if (null == result) {
            return Optional.absent();
        }
        Preconditions.checkArgument(result instanceof Comparable, "Sharding value must implements Comparable.");
        return Optional.of((Comparable) result);
    }
    
    private ShardingCondition createShardingCondition(final Map<Column, List<RouteValue>> routeValueMap) {
        ShardingCondition result = new ShardingCondition();
        for (Entry<Column, List<RouteValue>> entry : routeValueMap.entrySet()) {
            try {
                RouteValue routeValue = mergeRouteValues(entry.getKey(), entry.getValue());
                if (routeValue instanceof AlwaysFalseRouteValue) {
                    return new AlwaysFalseShardingCondition();
                }
                result.getRouteValues().add(routeValue);
            } catch (final ClassCastException ex) {
                throw new ShardingException("Found different types for sharding value `%s`.", entry.getKey());
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private RouteValue mergeRouteValues(final Column column, final List<RouteValue> routeValues) {
        Collection<Comparable<?>> listValue = null;
        Range<Comparable<?>> rangeValue = null;
        for (RouteValue each : routeValues) {
            if (each instanceof ListRouteValue) {
                listValue = mergeListRouteValues(((ListRouteValue) each).getValues(), listValue);
                if (listValue.isEmpty()) {
                    return new AlwaysFalseRouteValue();
                }
            } else if (each instanceof RangeRouteValue) {
                try {
                    rangeValue = mergeRangeRouteValues(((RangeRouteValue) each).getValueRange(), rangeValue);
                } catch (final IllegalArgumentException ex) {
                    return new AlwaysFalseRouteValue();
                }
            }
        }
        if (null == listValue) {
            return new RangeRouteValue<>(column.getName(), column.getTableName(), rangeValue);
        }
        if (null == rangeValue) {
            return new ListRouteValue<>(column.getName(), column.getTableName(), listValue);
        }
        listValue = mergeListAndRangeRouteValues(listValue, rangeValue);
        return listValue.isEmpty() ? new AlwaysFalseRouteValue() : new ListRouteValue<>(column.getName(), column.getTableName(), listValue);
    }
    
    private Collection<Comparable<?>> mergeListRouteValues(final Collection<Comparable<?>> value1, final Collection<Comparable<?>> value2) {
        if (null == value2) {
            return value1;
        }
        value1.retainAll(value2);
        return value1;
    }
    
    private Range<Comparable<?>> mergeRangeRouteValues(final Range<Comparable<?>> value1, final Range<Comparable<?>> value2) {
        return null == value2 ? value1 : value1.intersection(value2);
    }
    
    private Collection<Comparable<?>> mergeListAndRangeRouteValues(final Collection<Comparable<?>> listValue, final Range<Comparable<?>> rangeValue) {
        Collection<Comparable<?>> result = new LinkedList<>();
        for (Comparable<?> each : listValue) {
            if (rangeValue.contains(each)) {
                result.add(each);
            }
        }
        return result;
    }
}
