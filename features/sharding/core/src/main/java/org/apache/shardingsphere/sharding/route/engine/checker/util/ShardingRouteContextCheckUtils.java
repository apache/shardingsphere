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

package org.apache.shardingsphere.sharding.route.engine.checker.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Sharding route context check utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingRouteContextCheckUtils {
    
    /**
     * Judge whether is same route context or not.
     *
     * @param routeContext route context
     * @param subRouteContext  sub route context
     * @return whether is same route context or not
     */
    public static boolean isSameRouteContext(final RouteContext routeContext, final RouteContext subRouteContext) {
        if (routeContext.getRouteUnits().size() != subRouteContext.getRouteUnits().size()) {
            return false;
        }
        Iterator<RouteUnit> routeContextIterator = routeContext.getRouteUnits().iterator();
        Iterator<RouteUnit> setAssignmentRouteContextIterator = subRouteContext.getRouteUnits().iterator();
        while (routeContextIterator.hasNext()) {
            RouteUnit routeUnit = routeContextIterator.next();
            RouteUnit setAssignmentRouteUnit = setAssignmentRouteContextIterator.next();
            if (!routeUnit.getDataSourceMapper().getLogicName().equals(setAssignmentRouteUnit.getDataSourceMapper().getLogicName())
                    || !routeUnit.getDataSourceMapper().getActualName().equals(setAssignmentRouteUnit.getDataSourceMapper().getActualName())) {
                return false;
            }
            if (!isSameTableMapper(routeUnit.getTableMappers(), setAssignmentRouteUnit.getTableMappers())) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean isSameTableMapper(final Collection<RouteMapper> tableMappers, final Collection<RouteMapper> setAssignmentTableMappers) {
        if (tableMappers.size() != setAssignmentTableMappers.size()) {
            return false;
        }
        Iterator<RouteMapper> tableMapperIterator = tableMappers.iterator();
        Iterator<RouteMapper> setAssignmentTableMapperIterator = setAssignmentTableMappers.iterator();
        while (tableMapperIterator.hasNext()) {
            RouteMapper routeMapper = tableMapperIterator.next();
            RouteMapper setAssignmentRouteMapper = setAssignmentTableMapperIterator.next();
            if (!routeMapper.getLogicName().equals(setAssignmentRouteMapper.getLogicName())
                    || !routeMapper.getActualName().equals(setAssignmentRouteMapper.getActualName())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Create sharding conditions.
     *
     * @param sqlStatementContext SQL statement context
     * @param shardingRule shardingRule
     * @param assignments assignments
     * @param params parameters
     * @return sharding conditions
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Optional<ShardingConditions> createShardingConditions(final SQLStatementContext sqlStatementContext, final ShardingRule shardingRule,
                                                                        final Collection<ColumnAssignmentSegment> assignments, final List<Object> params) {
        Collection<ShardingConditionValue> values = new LinkedList<>();
        String tableName = sqlStatementContext.getTablesContext().getTableNames().iterator().next();
        for (ColumnAssignmentSegment each : assignments) {
            String shardingColumn = each.getColumns().get(0).getIdentifier().getValue();
            if (shardingRule.findShardingColumn(shardingColumn, tableName).isPresent()) {
                Optional<Object> assignmentValue = getShardingColumnAssignmentValue(each, params);
                assignmentValue.ifPresent(optional -> values.add(new ListShardingConditionValue(shardingColumn, tableName, Collections.singletonList(optional))));
            }
        }
        if (values.isEmpty()) {
            return Optional.empty();
        }
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getValues().addAll(values);
        return Optional.of(new ShardingConditions(Collections.singletonList(shardingCondition), sqlStatementContext, shardingRule));
    }
    
    private static Optional<Object> getShardingColumnAssignmentValue(final ColumnAssignmentSegment assignmentSegment, final List<Object> params) {
        ExpressionSegment segment = assignmentSegment.getValue();
        int shardingSetAssignIndex = -1;
        if (segment instanceof ParameterMarkerExpressionSegment) {
            shardingSetAssignIndex = ((ParameterMarkerExpressionSegment) segment).getParameterMarkerIndex();
        }
        if (segment instanceof LiteralExpressionSegment) {
            return Optional.of(((LiteralExpressionSegment) segment).getLiterals());
        }
        if (-1 == shardingSetAssignIndex || shardingSetAssignIndex > params.size() - 1) {
            return Optional.empty();
        }
        return Optional.of(params.get(shardingSetAssignIndex));
    }
}
