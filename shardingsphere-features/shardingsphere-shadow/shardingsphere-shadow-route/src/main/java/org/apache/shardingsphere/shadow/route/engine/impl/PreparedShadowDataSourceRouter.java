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

package org.apache.shardingsphere.shadow.route.engine.impl;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shadow.route.engine.ShadowDataSourceRouter;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.WhereAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionBuildUtil;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Shadow judgement engine for prepared.
 */
@RequiredArgsConstructor
public final class PreparedShadowDataSourceRouter implements ShadowDataSourceRouter {
    
    private final ShadowRule shadowRule;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final List<Object> parameters;
    
    @Override
    public boolean isShadowSQL() {
        if (sqlStatementContext instanceof InsertStatementContext) {
            Collection<ColumnSegment> columnSegments = (((InsertStatementContext) sqlStatementContext).getSqlStatement()).getColumns();
            int count = 0;
            for (ColumnSegment each : columnSegments) {
                if (each.getIdentifier().getValue().equals(shadowRule.getColumn())) {
                    Object value = parameters.get(count);
                    return isShadowField(value);
                }
                count++;
            }
            return false;
        }
        if (!(sqlStatementContext instanceof WhereAvailable)) {
            return false;
        }
        Optional<WhereSegment> whereSegment = ((WhereAvailable) sqlStatementContext).getWhere();
        if (!whereSegment.isPresent()) {
            return false;
        }
        Collection<AndPredicate> andPredicates = new LinkedList<>();
        ExpressionSegment expression = whereSegment.get().getExpr();
        if (expression instanceof BinaryOperationExpression) {
            String operator = ((BinaryOperationExpression) expression).getOperator();
            boolean logical = "and".equalsIgnoreCase(operator) || "&&".equalsIgnoreCase(operator) || "OR".equalsIgnoreCase(operator) || "||".equalsIgnoreCase(operator);
            if (logical) {
                ExpressionBuildUtil utils = new ExpressionBuildUtil(((BinaryOperationExpression) expression).getLeft(), ((BinaryOperationExpression) expression).getRight(), operator);
                andPredicates.addAll(utils.mergePredicate().getAndPredicates());
        
            } else {
                AndPredicate andPredicate = new AndPredicate();
                andPredicate.getPredicates().add(expression);
                andPredicates.add(andPredicate);
            }
        } else {
            AndPredicate andPredicate = new AndPredicate();
            andPredicate.getPredicates().add(expression);
            andPredicates.add(andPredicate);
        }
        for (AndPredicate andPredicate : andPredicates) {
            if (judgePredicateSegments(andPredicate.getPredicates())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean judgePredicateSegments(final Collection<ExpressionSegment> predicates) {
        for (ExpressionSegment each : predicates) {
            if (!(each instanceof BinaryOperationExpression)) {
                continue;
            }
            BinaryOperationExpression expression = (BinaryOperationExpression) each;
            ColumnSegment column = null;
            ExpressionSegment right = null;
            if (expression.getLeft() instanceof ColumnSegment) {
                column = (ColumnSegment) ((BinaryOperationExpression) each).getLeft();
                right = ((BinaryOperationExpression) each).getRight();
            }
            if (null == column) {
                continue;
            }
            if (column.getIdentifier().getValue().equals(shadowRule.getColumn())) {
                Preconditions.checkArgument(each instanceof BinaryOperationExpression, "must be BinaryOperationExpression");
                if (!(right instanceof ParameterMarkerExpressionSegment)) {
                    continue;
                }
                int parameterMarkerIndex = ((ParameterMarkerExpressionSegment) right).getParameterMarkerIndex();
                Object value = parameters.get(parameterMarkerIndex);
                return isShadowField(value);
            }
        }
        return false;
    }
}
