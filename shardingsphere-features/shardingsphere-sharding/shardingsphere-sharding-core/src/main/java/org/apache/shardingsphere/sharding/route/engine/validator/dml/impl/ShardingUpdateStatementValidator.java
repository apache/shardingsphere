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

package org.apache.shardingsphere.sharding.route.engine.validator.dml.impl;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.ShardingDMLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.UpdateStatementHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Sharding update statement validator.
 */
public final class ShardingUpdateStatementValidator extends ShardingDMLStatementValidator<UpdateStatement> {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext<UpdateStatement> sqlStatementContext, 
                            final List<Object> parameters, final ShardingSphereSchema schema) {
        validateMultipleTable(shardingRule, sqlStatementContext);
    }
    
    @Override
    public void postValidate(final ShardingRule shardingRule, final SQLStatementContext<UpdateStatement> sqlStatementContext, final List<Object> parameters, 
                             final ShardingSphereSchema schema, final ConfigurationProperties props, final RouteContext routeContext) {
        String tableName = sqlStatementContext.getTablesContext().getTableNames().iterator().next();
        Optional<ShardingConditions> shardingConditions = createSetAssignmentShardingConditions(sqlStatementContext, shardingRule, parameters);
        Optional<RouteContext> setAssignmentRouteContext = shardingConditions.map(optional -> new ShardingStandardRoutingEngine(tableName, optional, props).route(shardingRule));
        if (setAssignmentRouteContext.isPresent() && !isSameRouteContext(routeContext, setAssignmentRouteContext.get())) {
            throw new ShardingSphereException("Can not update sharding key since the updated value will change %s's data nodes.", tableName);
        }
        if (UpdateStatementHandler.getLimitSegment(sqlStatementContext.getSqlStatement()).isPresent() && routeContext.getRouteUnits().size() > 1) {
            throw new ShardingSphereException("UPDATE ... LIMIT can not support sharding route to multiple data nodes.");
        }
    }
    
    private boolean isSameRouteContext(final RouteContext routeContext, final RouteContext setAssignmentRouteContext) {
        if (routeContext.getRouteUnits().size() != setAssignmentRouteContext.getRouteUnits().size()) {
            return false;
        }
        Iterator<RouteUnit> routeContextIterator = routeContext.getRouteUnits().iterator();
        Iterator<RouteUnit> setAssignmentRouteContextIterator = setAssignmentRouteContext.getRouteUnits().iterator();
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
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Optional<ShardingConditions> createSetAssignmentShardingConditions(final SQLStatementContext<UpdateStatement> sqlStatementContext,
                                                                               final ShardingRule shardingRule, final List<Object> parameters) {
        List<ShardingConditionValue> values = new LinkedList<>();
        String tableName = sqlStatementContext.getTablesContext().getTableNames().iterator().next();
        for (AssignmentSegment each : sqlStatementContext.getSqlStatement().getSetAssignment().getAssignments()) {
            String shardingColumn = each.getColumns().get(0).getIdentifier().getValue();
            if (shardingRule.isShardingColumn(shardingColumn, tableName)) {
                Optional<Object> setAssignmentValue = getShardingColumnSetAssignmentValue(each, parameters);
                setAssignmentValue.ifPresent(optional -> values.add(new ListShardingConditionValue(shardingColumn, tableName, Collections.singletonList(optional))));
            }
        }
        ShardingConditions result = null;
        if (!values.isEmpty()) {
            ShardingCondition shardingCondition = new ShardingCondition();
            shardingCondition.getValues().addAll(values);
            result = new ShardingConditions(Collections.singletonList(shardingCondition), sqlStatementContext, shardingRule);
        }
        return Optional.ofNullable(result);
    }
    
    private Optional<Object> getShardingColumnSetAssignmentValue(final AssignmentSegment assignmentSegment, final List<Object> parameters) {
        ExpressionSegment segment = assignmentSegment.getValue();
        int shardingSetAssignIndex = -1;
        if (segment instanceof ParameterMarkerExpressionSegment) {
            shardingSetAssignIndex = ((ParameterMarkerExpressionSegment) segment).getParameterMarkerIndex();
        }
        if (segment instanceof LiteralExpressionSegment) {
            return Optional.of(((LiteralExpressionSegment) segment).getLiterals());
        }
        if (-1 == shardingSetAssignIndex || shardingSetAssignIndex > parameters.size() - 1) {
            return Optional.empty();
        }
        return Optional.of(parameters.get(shardingSetAssignIndex));
    }
}
