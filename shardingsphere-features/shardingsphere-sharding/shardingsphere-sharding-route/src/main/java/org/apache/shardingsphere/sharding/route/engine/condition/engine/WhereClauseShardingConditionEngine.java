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

package org.apache.shardingsphere.sharding.route.engine.condition.engine;

import com.google.common.collect.Range;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.sharding.route.engine.condition.AlwaysFalseRouteValue;
import org.apache.shardingsphere.sharding.route.engine.condition.AlwaysFalseShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.Column;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.ConditionValueGeneratorFactory;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.strategy.value.ListRouteValue;
import org.apache.shardingsphere.sharding.strategy.value.RangeRouteValue;
import org.apache.shardingsphere.sharding.strategy.value.RouteValue;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.WhereAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.SafeNumberOperationUtils;
import org.apache.shardingsphere.sql.parser.sql.common.util.WhereSegmentExtractUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Sharding condition engine for where clause.
 */
@RequiredArgsConstructor
public final class WhereClauseShardingConditionEngine {
    
    private final ShardingRule shardingRule;
    
    private final SchemaMetaData schemaMetaData;
    
    /**
     * Create sharding conditions.
     * 
     * @param sqlStatementContext SQL statement context
     * @param parameters SQL parameters
     * @return sharding conditions
     */
    public List<ShardingCondition> createShardingConditions(final SQLStatementContext sqlStatementContext, final List<Object> parameters) {
        if (!(sqlStatementContext instanceof WhereAvailable)) {
            return Collections.emptyList();
        }
        List<ShardingCondition> result = new ArrayList<>();
        Optional<WhereSegment> whereSegment = ((WhereAvailable) sqlStatementContext).getWhere();
        if (whereSegment.isPresent()) {
            result.addAll(createShardingConditions(sqlStatementContext, whereSegment.get().getExpr(), parameters));
        }
        Collection<WhereSegment> subqueryWhereSegments = sqlStatementContext.getSqlStatement() instanceof SelectStatement
                ? WhereSegmentExtractUtils.getSubqueryWhereSegments((SelectStatement) sqlStatementContext.getSqlStatement()) : Collections.emptyList();
        for (WhereSegment each : subqueryWhereSegments) {
            Collection<ShardingCondition> subqueryShardingConditions = createShardingConditions(sqlStatementContext, each.getExpr(), parameters);
            if (!result.containsAll(subqueryShardingConditions)) {
                result.addAll(subqueryShardingConditions);
            }
        }
        return result;
    }
    
    private Collection<ShardingCondition> createShardingConditions(final SQLStatementContext sqlStatementContext, final ExpressionSegment expressionSegment, final List<Object> parameters) {
        List<ExpressionSegment> expressions = new LinkedList<>();
        if (!(expressionSegment instanceof BinaryOperationExpression)) {
            expressions.add(expressionSegment);
        } else {
            expressions.addAll(((BinaryOperationExpression) expressionSegment).getExpressionList());
        }
        Collection<ShardingCondition> result = new LinkedList<>();
        for (ExpressionSegment each : expressions) {
            Map<Column, Collection<RouteValue>> routeValueMap = createRouteValueMap(sqlStatementContext, each, parameters);
            if (!routeValueMap.isEmpty()) {
                result.add(createShardingCondition(routeValueMap));
            }
        }
//        for (AndPredicate each : andPredicates) {
//            Map<Column, Collection<RouteValue>> routeValueMap = createRouteValueMap(sqlStatementContext, each, parameters);
//            if (routeValueMap.isEmpty()) {
//                return Collections.emptyList();
//            }
//            result.add(createShardingCondition(routeValueMap));
//        }
        return result;
    }
    
    private Map<Column, Collection<RouteValue>> createRouteValueMap(final SQLStatementContext sqlStatementContext, final ExpressionSegment expression, final List<Object> parameters) {
        Optional<RouteValue> routeValue = Optional.empty();
        Column column = null;
        if (expression instanceof BinaryOperationExpression && ((BinaryOperationExpression) expression).getLeft() instanceof ColumnSegment) {
            ColumnSegment columnSegment = (ColumnSegment) ((BinaryOperationExpression) expression).getLeft();
            Optional<String> tableName = sqlStatementContext.getTablesContext().findTableName(columnSegment, schemaMetaData);
            if (tableName.isPresent() && shardingRule.isShardingColumn(columnSegment.getIdentifier().getValue(), tableName.get())) {
                column = new Column(columnSegment.getIdentifier().getValue(), tableName.get());
                routeValue = ConditionValueGeneratorFactory.generate(expression, column, parameters);
            }
        }
        if (expression instanceof InExpression && ((InExpression) expression).getLeft() instanceof ColumnSegment) {
            ColumnSegment columnSegment = (ColumnSegment) ((InExpression) expression).getLeft();
            Optional<String> tableName = sqlStatementContext.getTablesContext().findTableName(columnSegment, schemaMetaData);
            if (tableName.isPresent() && shardingRule.isShardingColumn(columnSegment.getIdentifier().getValue(), tableName.get())) {
                column = new Column(columnSegment.getIdentifier().getValue(), tableName.get());
                routeValue = ConditionValueGeneratorFactory.generate(expression, column, parameters);
            }
        }
        if (expression instanceof BetweenExpression && ((BetweenExpression) expression).getLeft() instanceof ColumnSegment) {
            ColumnSegment columnSegment = (ColumnSegment) ((BetweenExpression) expression).getLeft();
            Optional<String> tableName = sqlStatementContext.getTablesContext().findTableName(columnSegment, schemaMetaData);
            if (tableName.isPresent() && shardingRule.isShardingColumn(columnSegment.getIdentifier().getValue(), tableName.get())) {
                column = new Column(columnSegment.getIdentifier().getValue(), tableName.get());
                routeValue = ConditionValueGeneratorFactory.generate(expression, column, parameters);
            }
        }
        Map<Column, Collection<RouteValue>> result = new HashMap<>();
        if (routeValue.isPresent()) {
            if (!result.containsKey(column)) {
                Collection<RouteValue> routeValues = new LinkedList<>();
                routeValues.add(routeValue.get());
                result.put(column, routeValues);
            } else {
                result.get(column).add(routeValue.get());
            }
        }
        
//        for (PredicateSegment each : andPredicate.getPredicates()) {
//            Optional<String> tableName = sqlStatementContext.getTablesContext().findTableName(each.getColumn(), schemaMetaData);
//            if (!tableName.isPresent() || !shardingRule.isShardingColumn(each.getColumn().getIdentifier().getValue(), tableName.get())) {
//                continue;
//            }
//            Column column = new Column(each.getColumn().getIdentifier().getValue(), tableName.get());
//            Optional<RouteValue> routeValue = ConditionValueGeneratorFactory.generate(each.getRightValue(), column, parameters);
//            if (!routeValue.isPresent()) {
//                continue;
//            }
//            if (!result.containsKey(column)) {
//                result.put(column, new LinkedList<>());
//            }
//            result.get(column).add(routeValue.get());
//        }
        return result;
    }
    
    private ShardingCondition createShardingCondition(final Map<Column, Collection<RouteValue>> routeValueMap) {
        ShardingCondition result = new ShardingCondition();
        for (Entry<Column, Collection<RouteValue>> entry : routeValueMap.entrySet()) {
            try {
                RouteValue routeValue = mergeRouteValues(entry.getKey(), entry.getValue());
                if (routeValue instanceof AlwaysFalseRouteValue) {
                    return new AlwaysFalseShardingCondition();
                }
                result.getRouteValues().add(routeValue);
            } catch (final ClassCastException ex) {
                throw new ShardingSphereException("Found different types for sharding value `%s`.", entry.getKey());
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private RouteValue mergeRouteValues(final Column column, final Collection<RouteValue> routeValues) {
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
        return null == value2 ? value1 : SafeNumberOperationUtils.safeIntersection(value1, value2);
    }
    
    private Collection<Comparable<?>> mergeListAndRangeRouteValues(final Collection<Comparable<?>> listValue, final Range<Comparable<?>> rangeValue) {
        Collection<Comparable<?>> result = new LinkedList<>();
        for (Comparable<?> each : listValue) {
            if (SafeNumberOperationUtils.safeContains(rangeValue, each)) {
                result.add(each);
            }
        }
        return result;
    }
}
