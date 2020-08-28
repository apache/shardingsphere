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

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.sharding.route.engine.condition.ExpressionConditionUtils;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.spi.SPITimeService;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.strategy.value.ListRouteValue;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.SimpleExpressionSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
public class UpdateClauseShardingConditionEngine {
    
    private final ShardingRule shardingRule;
    
    private final SchemaMetaData schemaMetaData;
    
    /**
     * Create sharding conditions.
     *
     * @param updateStatementContext Update statement context
     * @param parameters             SQL parameters
     * @return sharding conditions
     */
    public List<ShardingCondition> createShardingConditions(final UpdateStatementContext updateStatementContext, final List<Object> parameters) {
        List<ShardingCondition> result = new LinkedList<>();
        Collection<String> tableNames = updateStatementContext.getTablesContext().getTableNames();
        
        Collection<AssignmentSegment> assignments = updateStatementContext.getSqlStatement().getSetAssignment().getAssignments();
        
        for (String tableName : tableNames) {
            for (AssignmentSegment assignment : assignments) {
                String columnName = assignment.getColumn().getQualifiedName();
                
                if (shardingRule.isShardingColumn(columnName, tableName)) {
                    result.add(createShardingCondition(tableName, columnName, assignment.getValue(), parameters));
                }
            }
        }
        
        if (updateStatementContext.getWhere().isPresent()) {
            result.addAll(new WhereClauseShardingConditionEngine(shardingRule, schemaMetaData).createShardingConditions(updateStatementContext, parameters));
        }
        
        return result;
    }
    
    private ShardingCondition createShardingCondition(final String tableName, final String columnName, final ExpressionSegment expressionSegment, final List<Object> parameters) {
        ShardingCondition result = new ShardingCondition();
        SPITimeService timeService = new SPITimeService();
        
        if (expressionSegment instanceof SimpleExpressionSegment) {
            result.getRouteValues().add(new ListRouteValue<>(columnName, tableName, Collections.singletonList(getRouteValue((SimpleExpressionSegment) expressionSegment, parameters))));
        } else if (ExpressionConditionUtils.isNowExpression(expressionSegment)) {
            result.getRouteValues().add(new ListRouteValue<>(columnName, tableName, Collections.singletonList(timeService.getTime())));
        } else if (ExpressionConditionUtils.isNullExpression(expressionSegment)) {
            throw new ShardingSphereException("Insert clause sharding column can't be null.");
        }
        
        return result;
    }
    
    private Comparable<?> getRouteValue(final SimpleExpressionSegment expressionSegment, final List<Object> parameters) {
        Object result;
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            result = parameters.get(((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex());
        } else {
            result = ((LiteralExpressionSegment) expressionSegment).getLiterals();
        }
        Preconditions.checkArgument(result instanceof Comparable, "Sharding value must implements Comparable.");
        return (Comparable) result;
    }
}
