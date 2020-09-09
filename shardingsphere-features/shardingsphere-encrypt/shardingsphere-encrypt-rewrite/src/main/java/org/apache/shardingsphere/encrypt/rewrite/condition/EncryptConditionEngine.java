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

package org.apache.shardingsphere.encrypt.rewrite.condition;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.rewrite.condition.impl.EncryptEqualCondition;
import org.apache.shardingsphere.encrypt.rewrite.condition.impl.EncryptInCondition;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.WhereAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionBuildUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Encrypt condition engine.
 */
@RequiredArgsConstructor
public final class EncryptConditionEngine {
    
    private final EncryptRule encryptRule;
    
    private final SchemaMetaData schemaMetaData;
    
    /**
     * Create encrypt conditions.
     *
     * @param sqlStatementContext SQL statement context
     * @return encrypt conditions
     */
    public List<EncryptCondition> createEncryptConditions(final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext instanceof WhereAvailable)) {
            return Collections.emptyList();
        }
        Optional<WhereSegment> whereSegment = ((WhereAvailable) sqlStatementContext).getWhere();
        if (!whereSegment.isPresent()) {
            return Collections.emptyList();
        }
    
        Collection<AndPredicate> andPredicates = new LinkedList<>();
        ExpressionSegment expression = ((WhereAvailable) sqlStatementContext).getWhere().get().getExpr();
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
        List<EncryptCondition> result = new LinkedList<>();
        for (AndPredicate each : andPredicates) {
            result.addAll(createEncryptConditions(sqlStatementContext, each));
        }
        // FIXME process subquery
//        for (SubqueryPredicateSegment each : sqlStatementContext.getSqlStatement().findSQLSegments(SubqueryPredicateSegment.class)) {
//            for (AndPredicate andPredicate : each.getAndPredicates()) {
//                result.addAll(createEncryptConditions((WhereSegmentAvailable) sqlStatementContext.getSqlStatement(), andPredicate));
//            }
//        }
        return result;
    }
    
    private Collection<EncryptCondition> createEncryptConditions(final SQLStatementContext sqlStatementContext, final AndPredicate andPredicate) {
        Collection<EncryptCondition> result = new LinkedList<>();
        Collection<Integer> stopIndexes = new HashSet<>();
        for (ExpressionSegment predicate : andPredicate.getPredicates()) {
            if (stopIndexes.add(predicate.getStopIndex())) {
                createEncryptCondition(sqlStatementContext, predicate).ifPresent(result::add);
            }
        }
        return result;
    }
    
    private Optional<EncryptCondition> createEncryptCondition(final SQLStatementContext sqlStatementContext, final ExpressionSegment expression) {
        ColumnSegment column = null;
        if (expression instanceof BinaryOperationExpression && ((BinaryOperationExpression) expression).getLeft() instanceof ColumnSegment) {
            column = (ColumnSegment) ((BinaryOperationExpression) expression).getLeft();
        } else if (expression instanceof InExpression && ((InExpression) expression).getLeft() instanceof ColumnSegment) {
            column = (ColumnSegment) ((InExpression) expression).getLeft();
        } else if (expression instanceof BetweenExpression && ((BetweenExpression) expression).getLeft() instanceof ColumnSegment) {
            column = (ColumnSegment) ((BetweenExpression) expression).getLeft();
        }
        if (null == column) {
            return Optional.empty();
        }
        Optional<String> tableName = sqlStatementContext.getTablesContext().findTableName(column, schemaMetaData);
        return tableName.isPresent() && encryptRule.findEncryptor(tableName.get(), column.getIdentifier().getValue()).isPresent()
                ? createEncryptCondition(expression, tableName.get()) : Optional.empty();
    }
    
    private Optional<EncryptCondition> createEncryptCondition(final ExpressionSegment expression, final String tableName) {
        if (expression instanceof BinaryOperationExpression) {
            String operator = ((BinaryOperationExpression) expression).getOperator();
            boolean logical = "and".equalsIgnoreCase(operator) || "&&".equalsIgnoreCase(operator) || "OR".equalsIgnoreCase(operator) || "||".equalsIgnoreCase(operator);
            if (!logical) {
                ExpressionSegment rightValue = ((BinaryOperationExpression) expression).getRight();
                return isSupportedOperator(((BinaryOperationExpression) expression).getOperator()) ? createCompareEncryptCondition(tableName, (BinaryOperationExpression) expression, rightValue)
                        : Optional.empty();
            }
            
        }
        if (expression instanceof InExpression) {
            return createInEncryptCondition(tableName, (InExpression) expression, ((InExpression) expression).getRight());
        }
        if (expression instanceof BetweenExpression) {
            throw new ShardingSphereException("The SQL clause 'BETWEEN...AND...' is unsupported in encrypt rule.");
        }
        return Optional.empty();
    }
    
    private static Optional<EncryptCondition> createCompareEncryptCondition(final String tableName, final BinaryOperationExpression expression, final ExpressionSegment compareRightValue) {
        if (!(expression.getLeft() instanceof ColumnSegment)) {
            return Optional.empty();
        }
        return compareRightValue instanceof SimpleExpressionSegment
                ? Optional.of(new EncryptEqualCondition(((ColumnSegment) expression.getLeft()).getIdentifier().getValue(), tableName, compareRightValue.getStartIndex(),
                expression.getStopIndex(), compareRightValue))
                : Optional.empty();
    }
    
    private static Optional<EncryptCondition> createInEncryptCondition(final String tableName, final InExpression inExpression, final ExpressionSegment inRightValue) {
        if (!(inExpression.getLeft() instanceof ColumnSegment)) {
            return Optional.empty();
        }
        List<ExpressionSegment> expressionSegments = new LinkedList<>();
        for (ExpressionSegment each : inExpression.getExpressionList()) {
            if (each instanceof SimpleExpressionSegment) {
                expressionSegments.add(each);
            }
        }
        return Optional.of(new EncryptInCondition(((ColumnSegment) inExpression.getLeft()).getIdentifier().getValue(),
                tableName, inRightValue.getStartIndex(), inRightValue.getStopIndex(), expressionSegments));
    }
    
    private boolean isSupportedOperator(final String operator) {
        return "=".equals(operator) || "<>".equals(operator) || "!=".equals(operator);
    }
}
