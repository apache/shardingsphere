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

package org.apache.shardingsphere.sharding.route.engine.validator.dml;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.exception.syntax.DMLWithMultipleShardingTablesException;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.validator.ShardingStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Sharding dml statement validator.
 */
public abstract class ShardingDMLStatementValidator<T extends SQLStatement> implements ShardingStatementValidator<T> {
    
    /**
     * Validate multiple table.
     *
     * @param shardingRule sharding rule
     * @param sqlStatementContext sqlStatementContext
     */
    protected void validateMultipleTable(final ShardingRule shardingRule, final SQLStatementContext<T> sqlStatementContext) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        boolean isAllShardingTables = shardingRule.isAllShardingTables(tableNames) && (1 == tableNames.size() || shardingRule.isAllBindingTables(tableNames));
        boolean isAllBroadcastTables = shardingRule.isAllBroadcastTables(tableNames);
        boolean isAllSingleTables = !shardingRule.tableRuleExists(tableNames);
        if (!(isAllShardingTables || isAllBroadcastTables || isAllSingleTables)) {
            throw new DMLWithMultipleShardingTablesException(tableNames);
        }
    }
    
    /**
     * Judge whether is same route context or not.
     * 
     * @param routeContext route context
     * @param subRouteContext  sub route context
     * @return whether is same route context or not
     */
    protected boolean isSameRouteContext(final RouteContext routeContext, final RouteContext subRouteContext) {
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
    
    private boolean isSameTableMapper(final Collection<RouteMapper> tableMappers, final Collection<RouteMapper> setAssignmentTableMappers) {
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
     * Create shardingConditions.
     * 
     * @param sqlStatementContext SQL statement context
     * @param shardingRule shardingRule
     * @param assignments assignments
     * @param params parameters
     * @return sharding conditions
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Optional<ShardingConditions> createShardingConditions(final SQLStatementContext<?> sqlStatementContext, final ShardingRule shardingRule,
                                                                    final Collection<AssignmentSegment> assignments, final List<Object> params) {
        Collection<ShardingConditionValue> values = new LinkedList<>();
        String tableName = sqlStatementContext.getTablesContext().getTableNames().iterator().next();
        for (AssignmentSegment each : assignments) {
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
    
    private Optional<Object> getShardingColumnAssignmentValue(final AssignmentSegment assignmentSegment, final List<Object> params) {
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
