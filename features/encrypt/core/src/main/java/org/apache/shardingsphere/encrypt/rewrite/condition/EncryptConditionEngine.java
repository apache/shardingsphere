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

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rewrite.condition.impl.EncryptBinaryCondition;
import org.apache.shardingsphere.encrypt.rewrite.condition.impl.EncryptInCondition;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Encrypt condition engine.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public final class EncryptConditionEngine {
    
    private static final Collection<String> LOGICAL_OPERATORS = new CaseInsensitiveSet<>();
    
    private static final Collection<String> SUPPORTED_COMPARE_OPERATORS = new CaseInsensitiveSet<>();
    
    private final EncryptRule rule;
    
    static {
        LOGICAL_OPERATORS.add("AND");
        LOGICAL_OPERATORS.add("&&");
        LOGICAL_OPERATORS.add("OR");
        LOGICAL_OPERATORS.add("||");
        SUPPORTED_COMPARE_OPERATORS.add("=");
        SUPPORTED_COMPARE_OPERATORS.add("<>");
        SUPPORTED_COMPARE_OPERATORS.add("!=");
        SUPPORTED_COMPARE_OPERATORS.add(">");
        SUPPORTED_COMPARE_OPERATORS.add("<");
        SUPPORTED_COMPARE_OPERATORS.add(">=");
        SUPPORTED_COMPARE_OPERATORS.add("<=");
        SUPPORTED_COMPARE_OPERATORS.add("IS");
        SUPPORTED_COMPARE_OPERATORS.add("LIKE");
        SUPPORTED_COMPARE_OPERATORS.add("NOT LIKE");
    }
    
    /**
     * Create encrypt conditions.
     *
     * @param whereSegments where segments
     * @return encrypt conditions
     */
    public Collection<EncryptCondition> createEncryptConditions(final Collection<WhereSegment> whereSegments) {
        Collection<EncryptCondition> result = new LinkedList<>();
        for (WhereSegment each : whereSegments) {
            Collection<ExpressionSegment> expressions = ExpressionExtractor.extractAllExpressions(each.getExpr());
            addEncryptConditions(result, expressions);
        }
        return result;
    }
    
    private void addEncryptConditions(final Collection<EncryptCondition> encryptConditions, final Collection<ExpressionSegment> predicates) {
        Collection<Integer> stopIndexes = new HashSet<>(predicates.size(), 1F);
        for (ExpressionSegment each : predicates) {
            if (stopIndexes.add(each.getStopIndex())) {
                addEncryptConditions(encryptConditions, each);
            }
        }
    }
    
    private void addEncryptConditions(final Collection<EncryptCondition> encryptConditions, final ExpressionSegment expression) {
        if (!findNotContainsNullLiteralsExpression(expression).isPresent()) {
            return;
        }
        for (ColumnSegment each : ColumnExtractor.extract(expression)) {
            String tableName = each.getColumnBoundInfo().getOriginalTable().getValue();
            Optional<EncryptTable> encryptTable = rule.findEncryptTable(tableName);
            if (encryptTable.isPresent() && encryptTable.get().isEncryptColumn(each.getColumnBoundInfo().getOriginalColumn().getValue())) {
                encryptConditions.addAll(createEncryptCondition(expression, tableName));
            }
        }
    }
    
    private Optional<ExpressionSegment> findNotContainsNullLiteralsExpression(final ExpressionSegment expression) {
        if (isContainsNullLiterals(expression)) {
            return Optional.empty();
        }
        if (expression instanceof BinaryOperationExpression && isContainsNullLiterals(((BinaryOperationExpression) expression).getRight())) {
            return Optional.empty();
        }
        return Optional.ofNullable(expression);
    }
    
    private boolean isContainsNullLiterals(final ExpressionSegment expression) {
        if (!(expression instanceof LiteralExpressionSegment)) {
            return false;
        }
        String literals = String.valueOf(((LiteralExpressionSegment) expression).getLiterals());
        return "NULL".equalsIgnoreCase(literals) || "NOT NULL".equalsIgnoreCase(literals);
    }
    
    private Collection<EncryptCondition> createEncryptCondition(final ExpressionSegment expression, final String tableName) {
        if (expression instanceof BinaryOperationExpression) {
            return createBinaryEncryptCondition((BinaryOperationExpression) expression, tableName);
        }
        if (expression instanceof InExpression) {
            return createInEncryptCondition(tableName, (InExpression) expression, ((InExpression) expression).getRight());
        }
        if (expression instanceof BetweenExpression) {
            throw new UnsupportedEncryptSQLException("BETWEEN...AND...");
        }
        return Collections.emptyList();
    }
    
    private Collection<EncryptCondition> createBinaryEncryptCondition(final BinaryOperationExpression expression, final String tableName) {
        String operator = expression.getOperator();
        if (LOGICAL_OPERATORS.contains(operator)) {
            return Collections.emptyList();
        }
        ShardingSpherePreconditions.checkContains(SUPPORTED_COMPARE_OPERATORS, operator, () -> new UnsupportedEncryptSQLException(operator));
        return createCompareEncryptCondition(tableName, expression);
    }
    
    private Collection<EncryptCondition> createCompareEncryptCondition(final String tableName, final BinaryOperationExpression expression) {
        if (isLeftRightContainsSubquerySegment(expression)) {
            return Collections.emptyList();
        }
        Optional<ColumnSegment> columnSegment = Optional.ofNullable(isCompareValueSegment(expression.getLeft()) ? expression.getRight() : expression.getLeft()).filter(ColumnSegment.class::isInstance)
                .map(ColumnSegment.class::cast);
        if (!columnSegment.isPresent()) {
            return Collections.emptyList();
        }
        return getEncryptConditions(tableName, expression, columnSegment.get());
    }
    
    private Collection<EncryptCondition> getEncryptConditions(final String tableName, final BinaryOperationExpression expression, final ColumnSegment columnSegment) {
        ExpressionSegment compareValueSegment = isCompareValueSegment(expression.getLeft()) ? expression.getLeft() : expression.getRight();
        return getEncryptCondition(tableName, expression, compareValueSegment, columnSegment).map(Collections::singleton).orElseGet(Collections::emptySet);
    }
    
    private Optional<EncryptCondition> getEncryptCondition(final String tableName, final BinaryOperationExpression expression, final ExpressionSegment expressionSegment,
                                                           final ColumnSegment columnSegment) {
        if (expressionSegment instanceof SimpleExpressionSegment) {
            return Optional.of(createEncryptBinaryOperationCondition(tableName, expression, columnSegment, expressionSegment));
        }
        if (expressionSegment instanceof ListExpression) {
            // TODO check this logic when items contain multiple values @duanzhengqiang
            return Optional.of(createEncryptBinaryOperationCondition(tableName, expression, columnSegment, ((ListExpression) expressionSegment).getItems().get(0)));
        }
        return Optional.empty();
    }
    
    private boolean isCompareValueSegment(final ExpressionSegment expressionSegment) {
        return expressionSegment instanceof SimpleExpressionSegment || expressionSegment instanceof ListExpression;
    }
    
    private boolean isLeftRightContainsSubquerySegment(final BinaryOperationExpression expression) {
        return expression.getLeft() instanceof SubqueryExpressionSegment || expression.getRight() instanceof SubqueryExpressionSegment;
    }
    
    private EncryptBinaryCondition createEncryptBinaryOperationCondition(final String tableName, final BinaryOperationExpression expression, final ColumnSegment columnSegment,
                                                                         final ExpressionSegment compareValueSegment) {
        return new EncryptBinaryCondition(columnSegment, tableName, expression.getOperator(), compareValueSegment.getStartIndex(), compareValueSegment.getStopIndex(), compareValueSegment);
    }
    
    private static Collection<EncryptCondition> createInEncryptCondition(final String tableName, final InExpression inExpression, final ExpressionSegment inRightValue) {
        ColumnSegment columnSegment;
        if (inExpression.getLeft() instanceof ColumnSegment) {
            columnSegment = (ColumnSegment) inExpression.getLeft();
        } else {
            return Collections.emptyList();
        }
        List<ExpressionSegment> expressionSegments = new LinkedList<>();
        for (ExpressionSegment each : inExpression.getExpressionList()) {
            if (each instanceof SimpleExpressionSegment) {
                expressionSegments.add(each);
            }
        }
        if (expressionSegments.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singleton(new EncryptInCondition(columnSegment, tableName, inRightValue.getStartIndex(), inRightValue.getStopIndex(), expressionSegments));
    }
}
