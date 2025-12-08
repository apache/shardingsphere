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

package org.apache.shardingsphere.infra.binder.context.statement.type.dml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.pagination.DialectPaginationOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.segment.select.invalues.InValueContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.engine.PaginationContextEngine;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Select SQL statement binding context.
 */
public final class SelectStatementBindingContext implements SQLStatementContext {
    
    private final SelectStatementBaseContext baseContext;
    
    @Getter
    private final PaginationContext paginationContext;
    
    @Getter
    private final InValueContext inValueContext;
    
    @Getter
    @Setter
    private boolean needInValuesRewrite;
    
    public SelectStatementBindingContext(final List<Object> params, final SelectStatementBaseContext baseContext) {
        this.baseContext = baseContext;
        DialectPaginationOption paginationOption = new DatabaseTypeRegistry(baseContext.getSqlStatement().getDatabaseType()).getDialectDatabaseMetaData().getPaginationOption();
        paginationContext =
                new PaginationContextEngine(paginationOption).createPaginationContext(baseContext.getSqlStatement(), baseContext.getProjectionsContext(), params, baseContext.getWhereSegments());
        inValueContext = createInValueContext(baseContext.getSqlStatement(), params);
    }
    
    private InExpression extractInExpression(final SelectStatement sqlStatement) {
        if (!sqlStatement.getWhere().isPresent()) {
            return null;
        }
        return findInExpression(sqlStatement.getWhere().get().getExpr());
    }
    
    private InExpression findInExpression(final ExpressionSegment expression) {
        Deque<ExpressionSegment> stack = new LinkedList<>();
        stack.push(expression);
        while (!stack.isEmpty()) {
            ExpressionSegment current = stack.pop();
            if (current instanceof InExpression) {
                InExpression inExpr = (InExpression) current;
                if (inExpr.getRight() instanceof ListExpression) {
                    return inExpr;
                }
            }
            if (current instanceof BinaryOperationExpression) {
                BinaryOperationExpression binaryExpr = (BinaryOperationExpression) current;
                stack.push(binaryExpr.getRight());
                stack.push(binaryExpr.getLeft());
            }
        }
        return null;
    }
    
    private InValueContext createInValueContext(final SelectStatement sqlStatement, final List<Object> params) {
        InExpression inExpression = extractInExpression(sqlStatement);
        if (null == inExpression) {
            return null;
        }
        int parametersOffset = calculateInParametersOffset(params, inExpression);
        return new InValueContext(inExpression, params, parametersOffset);
    }
    
    private int calculateInParametersOffset(final List<Object> params, final InExpression inExpression) {
        if (null == params || params.isEmpty()) {
            return 0;
        }
        ExpressionSegment right = inExpression.getRight();
        if (!(right instanceof ListExpression)) {
            return 0;
        }
        for (ExpressionSegment each : ((ListExpression) right).getItems()) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                return ((ParameterMarkerExpressionSegment) each).getParameterIndex();
            }
        }
        return 0;
    }
    
    /**
     * Check if has IN expression.
     *
     * @return true if has IN expression
     */
    public boolean hasInExpression() {
        return null != inValueContext;
    }
    
    /**
     * Get grouped parameters for IN query.
     *
     * @return grouped parameters
     */
    public List<List<Object>> getGroupedParameters() {
        if (null == inValueContext) {
            return Collections.emptyList();
        }
        return inValueContext.getGroupedParameters();
    }
    
    /**
     * Get generic parameters before IN expression.
     *
     * @param params all parameters
     * @return parameters before IN expression
     */
    public List<Object> getBeforeGenericParameters(final List<Object> params) {
        if (null == inValueContext || null == params || params.isEmpty()) {
            return Collections.emptyList();
        }
        int parametersOffset = inValueContext.getParametersOffset();
        if (parametersOffset == 0) {
            return Collections.emptyList();
        }
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < parametersOffset && i < params.size(); i++) {
            result.add(params.get(i));
        }
        return result;
    }
    
    /**
     * Get generic parameters after IN expression.
     *
     * @param params all parameters
     * @return parameters after IN expression
     */
    public List<Object> getAfterGenericParameters(final List<Object> params) {
        if (null == inValueContext || null == params || params.isEmpty()) {
            return Collections.emptyList();
        }
        int parametersOffset = inValueContext.getParametersOffset();
        int parameterCount = inValueContext.getParameterCount();
        int afterStart = parametersOffset + parameterCount;
        if (afterStart >= params.size()) {
            return Collections.emptyList();
        }
        List<Object> result = new ArrayList<>();
        for (int i = afterStart; i < params.size(); i++) {
            result.add(params.get(i));
        }
        return result;
    }
    
    @Override
    public SelectStatement getSqlStatement() {
        return baseContext.getSqlStatement();
    }
    
    @Override
    public TablesContext getTablesContext() {
        return baseContext.getTablesContext();
    }
}
