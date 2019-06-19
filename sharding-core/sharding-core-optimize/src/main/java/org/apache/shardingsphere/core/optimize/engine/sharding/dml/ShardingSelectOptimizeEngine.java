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

package org.apache.shardingsphere.core.optimize.engine.sharding.dml;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.condition.RouteCondition;
import org.apache.shardingsphere.core.optimize.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
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
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.route.value.BetweenRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Select optimize engine for sharding.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ShardingSelectOptimizeEngine implements OptimizeEngine {
    
    private final ShardingRule shardingRule;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    private final SelectStatement selectStatement;
    
    private final List<Object> parameters;
    
    @Override
    public OptimizeResult optimize() {
        List<RouteCondition> routeConditions = new LinkedList<>();
        for (RouteCondition each : createRouteConditions()) {
            routeConditions.add(optimize(each.getRouteValuesMap()));
        }
        OptimizeResult result = new OptimizeResult(routeConditions);
        setPagination(result);
        return result;
    }
    
    private RouteCondition optimize(final Map<Column, List<RouteValue>> routeValuesMap) {
        RouteCondition result = new RouteCondition();
        for (Entry<Column, List<RouteValue>> entry : routeValuesMap.entrySet()) {
            try {
                RouteValue routeValue = optimize(entry.getKey(), entry.getValue());
                if (routeValue instanceof AlwaysFalseRouteValue) {
                    return new AlwaysFalseRouteCondition();
                }
                result.getRouteValues().add(routeValue);
            } catch (final ClassCastException ex) {
                throw new ShardingException("Found different types for sharding value `%s`.", entry.getKey());
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private RouteValue optimize(final Column column, final List<RouteValue> routeValues) {
        Collection<Comparable<?>> listValue = null;
        Range<Comparable<?>> rangeValue = null;
        for (RouteValue each : routeValues) {
            if (each instanceof ListRouteValue) {
                listValue = optimize(((ListRouteValue) each).getValues(), listValue);
                if (listValue.isEmpty()) {
                    return new AlwaysFalseRouteValue();
                }
            } else if (each instanceof BetweenRouteValue) {
                try {
                    rangeValue = optimize(((BetweenRouteValue) each).getValueRange(), rangeValue);
                } catch (final IllegalArgumentException ex) {
                    return new AlwaysFalseRouteValue();
                }
            }
        }
        if (null == listValue) {
            return new BetweenRouteValue<>(column.getName(), column.getTableName(), rangeValue);
        }
        if (null == rangeValue) {
            return new ListRouteValue<>(column.getName(), column.getTableName(), listValue);
        }
        listValue = optimize(listValue, rangeValue);
        return listValue.isEmpty() ? new AlwaysFalseRouteValue() : new ListRouteValue<>(column.getName(), column.getTableName(), listValue);
    }
    
    private Collection<Comparable<?>> optimize(final Collection<Comparable<?>> value1, final Collection<Comparable<?>> value2) {
        if (null == value2) {
            return value1;
        }
        value1.retainAll(value2);
        return value1;
    }
    
    private Range<Comparable<?>> optimize(final Range<Comparable<?>> value1, final Range<Comparable<?>> value2) {
        return null == value2 ? value1 : value1.intersection(value2);
    }
    
    private Collection<Comparable<?>> optimize(final Collection<Comparable<?>> listValue, final Range<Comparable<?>> rangeValue) {
        Collection<Comparable<?>> result = new LinkedList<>();
        for (Comparable<?> each : listValue) {
            if (rangeValue.contains(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private List<RouteCondition> createRouteConditions() {
        List<RouteCondition> result = new LinkedList<>();
        Optional<OrPredicateSegment> orPredicateSegment = selectStatement.findSQLSegment(OrPredicateSegment.class);
        if (orPredicateSegment.isPresent()) {
            result.addAll(createRouteConditions(orPredicateSegment.get()));
        }
        Optional<SubqueryPredicateSegment> subqueryPredicateSegment = selectStatement.findSQLSegment(SubqueryPredicateSegment.class);
        if (subqueryPredicateSegment.isPresent()) {
            for (OrPredicateSegment each : subqueryPredicateSegment.get().getOrPredicates()) {
                List<RouteCondition> subqueryRouteConditions = createRouteConditions(each);
                if (!result.containsAll(subqueryRouteConditions)) {
                    result.addAll(subqueryRouteConditions);
                }
            }
        }
        return result;
    }
    
    private List<RouteCondition> createRouteConditions(final OrPredicateSegment orPredicateSegment) {
        List<RouteCondition> result = new LinkedList<>();
        for (AndPredicate each : orPredicateSegment.getAndPredicates()) {
            RouteCondition routeCondition = new RouteCondition();
            List<RouteValue> routeValues = createRouteValues(each);
            if (routeValues.isEmpty()) {
                result.clear();
                return result;
            }
            routeCondition.getRouteValues().addAll(routeValues);
            result.add(routeCondition);
        }
        return result;
    }
    
    private List<RouteValue> createRouteValues(final AndPredicate andPredicate) {
        List<RouteValue> result = new LinkedList<>();
        for (PredicateSegment each : andPredicate.getPredicates()) {
            Optional<String> tableName = PredicateUtils.findTableName(each, selectStatement, shardingTableMetaData);
            if (tableName.isPresent() && shardingRule.isShardingColumn(each.getColumn().getName(), tableName.get())) {
                Optional<RouteValue> routeValue = createRouteValue(each, tableName.get());
                if (routeValue.isPresent()) {
                    result.add(routeValue.get());
                }
            }
        }
        return result;
    }
    
    private Optional<RouteValue> createRouteValue(final PredicateSegment predicateSegment, final String tableName) {
        String columnName = predicateSegment.getColumn().getName();
        if (predicateSegment.getRightValue() instanceof PredicateCompareRightValue) {
            return getRouteValue((PredicateCompareRightValue) predicateSegment.getRightValue(), columnName, tableName);
        }
        if (predicateSegment.getRightValue() instanceof PredicateInRightValue) {
            return getRouteValue((PredicateInRightValue) predicateSegment.getRightValue(), columnName, tableName);
        }
        if (predicateSegment.getRightValue() instanceof PredicateInRightValue) {
            return getRouteValue((PredicateInRightValue) predicateSegment.getRightValue(), columnName, tableName);
        }
        if (predicateSegment.getRightValue() instanceof PredicateBetweenRightValue) {
            return getRouteValue((PredicateBetweenRightValue) predicateSegment.getRightValue(), columnName, tableName);
        }
        return Optional.absent();
    }
    
    private Optional<RouteValue> getRouteValue(final PredicateCompareRightValue predicateRightValue, final String columnName, final String tableName) {
        if (!isOperatorSupportedWithSharding(predicateRightValue.getOperator())) {
            return Optional.absent();
        }
        Optional<Comparable> shardingValue = getShardingValue(predicateRightValue.getExpression());
        return shardingValue.isPresent() ? Optional.<RouteValue>of(new ListRouteValue<>(columnName, tableName, Lists.newArrayList(shardingValue.get()))) : Optional.<RouteValue>absent();
    }
    
    private Optional<RouteValue> getRouteValue(final PredicateInRightValue predicateRightValue, final String columnName, final String tableName) {
        List<Comparable> shardingValues = new LinkedList<>();
        for (ExpressionSegment each : predicateRightValue.getSqlExpressions()) {
            Optional<Comparable> shardingValue = getShardingValue(each);
            if (shardingValue.isPresent()) {
                shardingValues.add(shardingValue.get());
            }
        }
        return shardingValues.isEmpty() ? Optional.<RouteValue>absent() : Optional.<RouteValue>of(new ListRouteValue<>(columnName, tableName, shardingValues));
    }
    
    private Optional<RouteValue> getRouteValue(final PredicateBetweenRightValue predicateRightValue, final String columnName, final String tableName) {
        Optional<Comparable> betweenShardingValue = getShardingValue(predicateRightValue.getBetweenExpression());
        Optional<Comparable> andShardingValue = getShardingValue(predicateRightValue.getAndExpression());
        return betweenShardingValue.isPresent() && andShardingValue.isPresent()
                ? Optional.<RouteValue>of(new BetweenRouteValue<>(columnName, tableName, Range.closed(betweenShardingValue.get(), andShardingValue.get()))) : Optional.<RouteValue>absent();
    }
    
    private boolean isOperatorSupportedWithSharding(final String operator) {
        return "=".equals(operator);
    }
    
    private Optional<Comparable> getShardingValue(final ExpressionSegment expression) {
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
    
    private void setPagination(final OptimizeResult result) {
        if (null != selectStatement.getOffset() || null != selectStatement.getRowCount()) {
            result.setPagination(new Pagination(selectStatement.getOffset(), selectStatement.getRowCount(), parameters));
        }
    }
}
