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
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.ShardingDMLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.UpdateStatementHandler;

import java.util.List;
import java.util.Optional;

/**
 * Sharding update statement validator.
 */
public final class ShardingUpdateStatementValidator extends ShardingDMLStatementValidator<UpdateStatement> {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext<UpdateStatement> sqlStatementContext, 
                            final List<Object> parameters, final ShardingSphereSchema schema) {
        validateShardingMultipleTable(shardingRule, sqlStatementContext);
        UpdateStatement sqlStatement = sqlStatementContext.getSqlStatement();
        String tableName = sqlStatementContext.getTablesContext().getTables().iterator().next().getTableName().getIdentifier().getValue();
        for (AssignmentSegment each : sqlStatement.getSetAssignment().getAssignments()) {
            String shardingColumn = each.getColumn().getIdentifier().getValue();
            if (shardingRule.isShardingColumn(shardingColumn, tableName)) {
                Optional<Object> shardingColumnSetAssignmentValue = getShardingColumnSetAssignmentValue(each, parameters);
                Optional<Object> shardingValue = Optional.empty();
                Optional<WhereSegment> whereSegmentOptional = sqlStatement.getWhere();
                if (whereSegmentOptional.isPresent()) {
                    shardingValue = getShardingValue(whereSegmentOptional.get(), parameters, shardingColumn);
                }
                if (shardingColumnSetAssignmentValue.isPresent() && shardingValue.isPresent() && shardingColumnSetAssignmentValue.get().equals(shardingValue.get())) {
                    continue;
                }
                throw new ShardingSphereException("Can not update sharding key, logic table: [%s], column: [%s].", tableName, shardingColumn);
            }
        }
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
    
    private Optional<Object> getShardingValue(final WhereSegment whereSegment, final List<Object> parameters, final String shardingColumn) {
        if (null != whereSegment) {
            return getShardingValue(whereSegment.getExpr(), parameters, shardingColumn);
        }
        return Optional.empty();
    }
    
    private Optional<Object> getShardingValue(final ExpressionSegment expression, final List<Object> parameters, final String shardingColumn) {
        if (expression instanceof InExpression && ((InExpression) expression).getLeft() instanceof ColumnSegment) {
            ColumnSegment column = (ColumnSegment) ((InExpression) expression).getLeft();
            if (!shardingColumn.equalsIgnoreCase(column.getIdentifier().getValue())) {
                return getPredicateInShardingValue(((InExpression) expression).getRight(), parameters);
            }
        }
        if (!(expression instanceof BinaryOperationExpression)) {
            return Optional.empty();
        }
        String operator = ((BinaryOperationExpression) expression).getOperator();
        boolean compare = ">".equalsIgnoreCase(operator) || ">=".equalsIgnoreCase(operator) || "=".equalsIgnoreCase(operator) || "<".equalsIgnoreCase(operator) || "<=".equalsIgnoreCase(operator);
        if (compare && ((BinaryOperationExpression) expression).getLeft() instanceof ColumnSegment) {
            ColumnSegment column = (ColumnSegment) ((BinaryOperationExpression) expression).getLeft();
            if (shardingColumn.equalsIgnoreCase(column.getIdentifier().getValue())) {
                return getPredicateCompareShardingValue(((BinaryOperationExpression) expression).getRight(), parameters);
            }
        }
        boolean logical = "and".equalsIgnoreCase(operator) || "&&".equalsIgnoreCase(operator) || "OR".equalsIgnoreCase(operator) || "||".equalsIgnoreCase(operator);
        if (logical) {
            Optional<Object> leftResult = getShardingValue(((BinaryOperationExpression) expression).getLeft(), parameters, shardingColumn);
            return leftResult.isPresent() ? leftResult : getShardingValue(((BinaryOperationExpression) expression).getRight(), parameters, shardingColumn);
        }
        return Optional.empty();
    }
    
    private Optional<Object> getPredicateCompareShardingValue(final ExpressionSegment segment, final List<Object> parameters) {
        int shardingValueParameterMarkerIndex;
        if (segment instanceof ParameterMarkerExpressionSegment) {
            shardingValueParameterMarkerIndex = ((ParameterMarkerExpressionSegment) segment).getParameterMarkerIndex();
            if (-1 == shardingValueParameterMarkerIndex || shardingValueParameterMarkerIndex > parameters.size() - 1) {
                return Optional.empty();
            }
            return Optional.of(parameters.get(shardingValueParameterMarkerIndex));
        }
        if (segment instanceof LiteralExpressionSegment) {
            return Optional.of(((LiteralExpressionSegment) segment).getLiterals());
        }
        return Optional.empty();
    }
    
    private Optional<Object> getPredicateInShardingValue(final ExpressionSegment segments, final List<Object> parameters) {
        int shardingColumnWhereIndex;
        if (!(segments instanceof ListExpression)) {
            return Optional.empty();
        }
        List<ExpressionSegment> expressionSegments = ((ListExpression) segments).getItems();
        for (ExpressionSegment each : expressionSegments) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                shardingColumnWhereIndex = ((ParameterMarkerExpressionSegment) each).getParameterMarkerIndex();
                if (-1 == shardingColumnWhereIndex || shardingColumnWhereIndex > parameters.size() - 1) {
                    continue;
                }
                return Optional.of(parameters.get(shardingColumnWhereIndex));
            }
            if (each instanceof LiteralExpressionSegment) {
                return Optional.of(((LiteralExpressionSegment) each).getLiterals());
            }
        }
        return Optional.empty();
    }
    
    @Override
    public void postValidate(final UpdateStatement sqlStatement, final RouteContext routeContext) {
        if (UpdateStatementHandler.getLimitSegment(sqlStatement).isPresent() && routeContext.getRouteUnits().size() > 1) {
            throw new ShardingSphereException("UPDATE ... LIMIT can not support sharding route to multiple data nodes.");
        }
    }
}
