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
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.statement.sharding.condition.ShardingCondition;
import org.apache.shardingsphere.core.parse.filler.common.dml.PredicateUtils;
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
import java.util.LinkedList;
import java.util.List;

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
            ShardingCondition shardingCondition = new ShardingCondition();
            List<RouteValue> routeValues = createRouteValues(dmlStatement, parameters, each);
            if (routeValues.isEmpty()) {
                result.clear();
                return result;
            }
            shardingCondition.getRouteValues().addAll(routeValues);
            result.add(shardingCondition);
        }
        return result;
    }
    
    private List<RouteValue> createRouteValues(final DMLStatement dmlStatement, final List<Object> parameters, final AndPredicate andPredicate) {
        List<RouteValue> result = new LinkedList<>();
        for (PredicateSegment each : andPredicate.getPredicates()) {
            Optional<String> tableName = PredicateUtils.findTableName(each, dmlStatement, shardingTableMetaData);
            if (tableName.isPresent() && shardingRule.isShardingColumn(each.getColumn().getName(), tableName.get())) {
                Optional<RouteValue> routeValue = createRouteValue(parameters, each, tableName.get());
                if (routeValue.isPresent()) {
                    result.add(routeValue.get());
                }
            }
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
        Optional<Comparable> shardingValue = getShardingValue(parameters, predicateRightValue.getExpression());
        return shardingValue.isPresent() ? Optional.<RouteValue>of(new ListRouteValue<>(columnName, tableName, Lists.newArrayList(shardingValue.get()))) : Optional.<RouteValue>absent();
    }
    
    private Optional<RouteValue> getRouteValue(final List<Object> parameters, final PredicateInRightValue predicateRightValue, final String columnName, final String tableName) {
        List<Comparable> shardingValues = new LinkedList<>();
        for (ExpressionSegment each : predicateRightValue.getSqlExpressions()) {
            Optional<Comparable> shardingValue = getShardingValue(parameters, each);
            if (shardingValue.isPresent()) {
                shardingValues.add(shardingValue.get());
            }
        }
        return shardingValues.isEmpty() ? Optional.<RouteValue>absent() : Optional.<RouteValue>of(new ListRouteValue<>(columnName, tableName, shardingValues));
    }
    
    private Optional<RouteValue> getRouteValue(final List<Object> parameters, final PredicateBetweenRightValue predicateRightValue, final String columnName, final String tableName) {
        Optional<Comparable> betweenShardingValue = getShardingValue(parameters, predicateRightValue.getBetweenExpression());
        Optional<Comparable> andShardingValue = getShardingValue(parameters, predicateRightValue.getAndExpression());
        return betweenShardingValue.isPresent() && andShardingValue.isPresent()
                ? Optional.<RouteValue>of(new RangeRouteValue<>(columnName, tableName, Range.closed(betweenShardingValue.get(), andShardingValue.get()))) : Optional.<RouteValue>absent();
    }
    
    private boolean isOperatorSupportedWithSharding(final String operator) {
        return "=".equals(operator);
    }
    
    private Optional<Comparable> getShardingValue(final List<Object> parameters, final ExpressionSegment expression) {
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
}
