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
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.constant.ShardingOperator;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.condition.RouteCondition;
import org.apache.shardingsphere.core.optimize.condition.RouteConditions;
import org.apache.shardingsphere.core.optimize.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.parse.filler.common.dml.PredicateUtils;
import org.apache.shardingsphere.core.parse.sql.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
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

import java.util.ArrayList;
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
        List<RouteCondition> routeConditions = new ArrayList<>(selectStatement.getShardingConditions().getOrConditions().size());
        List<AndCondition> conditions = createShardingConditions();
        for (AndCondition each : conditions) {
            routeConditions.add(optimize(each.getConditionsMap()));
        }
        OptimizeResult result = new OptimizeResult(new RouteConditions(routeConditions));
        setPagination(result);
        return result;
    }
    
    private RouteCondition optimize(final Map<Column, List<Condition>> conditionsMap) {
        RouteCondition result = new RouteCondition();
        for (Entry<Column, List<Condition>> entry : conditionsMap.entrySet()) {
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
    
    private RouteValue optimize(final Column column, final List<Condition> conditions) {
        List<Comparable<?>> listValue = null;
        Range<Comparable<?>> rangeValue = null;
        for (Condition each : conditions) {
            List<Comparable<?>> conditionValues = each.getConditionValues(parameters);
            if (ShardingOperator.EQUAL == each.getOperator() || ShardingOperator.IN == each.getOperator()) {
                listValue = optimize(conditionValues, listValue);
                if (listValue.isEmpty()) {
                    return new AlwaysFalseRouteValue();
                }
            }
            if (ShardingOperator.BETWEEN == each.getOperator()) {
                try {
                    rangeValue = optimize(Range.range(conditionValues.get(0), BoundType.CLOSED, conditionValues.get(1), BoundType.CLOSED), rangeValue);
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
    
    private List<Comparable<?>> optimize(final List<Comparable<?>> value1, final List<Comparable<?>> value2) {
        if (null == value2) {
            return value1;
        }
        value1.retainAll(value2);
        return value1;
    }
    
    private Range<Comparable<?>> optimize(final Range<Comparable<?>> value1, final Range<Comparable<?>> value2) {
        return null == value2 ? value1 : value1.intersection(value2);
    }
    
    private List<Comparable<?>> optimize(final List<Comparable<?>> listValue, final Range<Comparable<?>> rangeValue) {
        List<Comparable<?>> result = new LinkedList<>();
        for (Comparable<?> each : listValue) {
            if (rangeValue.contains(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private List<AndCondition> createShardingConditions() {
        List<AndCondition> conditions = new LinkedList<>();
        Optional<OrPredicateSegment> orPredicateSegment = selectStatement.findSQLSegment(OrPredicateSegment.class);
        if (orPredicateSegment.isPresent()) {
            conditions.addAll(createShardingConditions(orPredicateSegment.get()));
        }
        Optional<SubqueryPredicateSegment> subqueryPredicateSegment = selectStatement.findSQLSegment(SubqueryPredicateSegment.class);
        if (subqueryPredicateSegment.isPresent()) {
            for (OrPredicateSegment each : subqueryPredicateSegment.get().getOrPredicates()) {
                List<AndCondition> subqueryConditions = createShardingConditions(each);
                if (!conditions.containsAll(subqueryConditions)) {
                    conditions.addAll(subqueryConditions);
                }
            }
        }
        return conditions;
    }
    
    private List<AndCondition> createShardingConditions(final OrPredicateSegment orPredicateSegment) {
        List<AndCondition> result = new LinkedList<>();
        for (AndPredicate each : orPredicateSegment.getAndPredicates()) {
            AndCondition andCondition = new AndCondition();
            for (PredicateSegment predicate : each.getPredicates()) {
                Optional<String> tableName = PredicateUtils.findTableName(predicate, selectStatement, shardingTableMetaData);
                if (!tableName.isPresent() || !shardingRule.isShardingColumn(predicate.getColumn().getName(), tableName.get())) {
                    continue;
                }
                Optional<Condition> condition = createCondition(predicate, new Column(predicate.getColumn().getName(), tableName.get()));
                if (condition.isPresent()) {
                    andCondition.getConditions().add(condition.get());
                }
            }
            if (andCondition.getConditions().isEmpty()) {
                result.clear();
                return result;
            }
            result.add(andCondition);
        }
        return result;
    }
    
    private Optional<Condition> createCondition(final PredicateSegment predicateSegment, final Column column) {
        if (predicateSegment.getRightValue() instanceof PredicateCompareRightValue) {
            PredicateCompareRightValue compareRightValue = (PredicateCompareRightValue) predicateSegment.getRightValue();
            return isOperatorSupportedWithSharding(compareRightValue.getOperator())
                    ? PredicateUtils.createCompareCondition(compareRightValue, column, predicateSegment) : Optional.<Condition>absent();
        }
        if (predicateSegment.getRightValue() instanceof PredicateInRightValue) {
            return PredicateUtils.createInCondition((PredicateInRightValue) predicateSegment.getRightValue(), column, predicateSegment);
        }
        if (predicateSegment.getRightValue() instanceof PredicateBetweenRightValue) {
            return PredicateUtils.createBetweenCondition((PredicateBetweenRightValue) predicateSegment.getRightValue(), column, predicateSegment);
        }
        return Optional.absent();
    }
    
    private boolean isOperatorSupportedWithSharding(final String operator) {
        return "=".equals(operator);
    }
    
    private void setPagination(final OptimizeResult result) {
        if (null != selectStatement.getOffset() || null != selectStatement.getRowCount()) {
            result.setPagination(new Pagination(selectStatement.getOffset(), selectStatement.getRowCount(), parameters));
        }
    }
}
