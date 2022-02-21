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
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Encrypt condition engine.
 */
@RequiredArgsConstructor
public final class EncryptConditionEngine {
    
    private static final Set<String> LOGICAL_OPERATOR = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    
    private static final Set<String> SUPPORTED_COMPARE_OPERATOR = new HashSet<>();
    
    private final EncryptRule encryptRule;
    
    private final ShardingSphereSchema schema;
    
    static {
        LOGICAL_OPERATOR.add("AND");
        LOGICAL_OPERATOR.add("&&");
        LOGICAL_OPERATOR.add("OR");
        LOGICAL_OPERATOR.add("||");
        SUPPORTED_COMPARE_OPERATOR.add("=");
        SUPPORTED_COMPARE_OPERATOR.add("<>");
        SUPPORTED_COMPARE_OPERATOR.add("!=");
        SUPPORTED_COMPARE_OPERATOR.add(">");
        SUPPORTED_COMPARE_OPERATOR.add("<");
        SUPPORTED_COMPARE_OPERATOR.add(">=");
        SUPPORTED_COMPARE_OPERATOR.add("<=");
    }
    
    /**
     * Create encrypt conditions.
     *
     * @param sqlStatementContext SQL statement context
     * @return encrypt conditions
     */
    public Collection<EncryptCondition> createEncryptConditions(final SQLStatementContext<?> sqlStatementContext) {
        Collection<EncryptCondition> result = new LinkedList<>();
        Collection<WhereSegment> whereSegments = sqlStatementContext instanceof WhereAvailable 
                ? ((WhereAvailable) sqlStatementContext).getWhereSegments() : Collections.emptyList();
        for (WhereSegment each : whereSegments) {
            Collection<AndPredicate> andPredicates = ExpressionExtractUtil.getAndPredicates(each.getExpr());
            Map<String, String> columnTableNames = getColumnTableNames(sqlStatementContext, andPredicates);
            for (AndPredicate predicate : andPredicates) {
                result.addAll(createEncryptConditions(predicate.getPredicates(), columnTableNames));
            }
        }
        return result;
    }
    
    private Collection<EncryptCondition> createEncryptConditions(final Collection<ExpressionSegment> predicates, final Map<String, String> columnTableNames) {
        Collection<EncryptCondition> result = new LinkedList<>();
        Collection<Integer> stopIndexes = new HashSet<>();
        for (ExpressionSegment each : predicates) {
            if (stopIndexes.add(each.getStopIndex())) {
                result.addAll(createEncryptConditions(each, columnTableNames));
            }
        }
        return result;
    }
    
    private Collection<EncryptCondition> createEncryptConditions(final ExpressionSegment expression, final Map<String, String> columnTableNames) {
        Collection<EncryptCondition> result = new LinkedList<>();
        for (ColumnSegment each : ColumnExtractor.extract(expression)) {
            ColumnProjection projection = buildColumnProjection(each);
            Optional<String> tableName = Optional.ofNullable(columnTableNames.get(projection.getExpression()));
            Optional<EncryptCondition> encryptCondition = tableName.isPresent() 
                    && encryptRule.findEncryptor(tableName.get(), projection.getName()).isPresent() ? createEncryptCondition(expression, tableName.get()) : Optional.empty();
            encryptCondition.ifPresent(result::add);
        }
        return result;
    }
    
    private Optional<EncryptCondition> createEncryptCondition(final ExpressionSegment expression, final String tableName) {
        if (expression instanceof BinaryOperationExpression) {
            return createBinaryEncryptCondition((BinaryOperationExpression) expression, tableName);
        }
        if (expression instanceof InExpression) {
            return createInEncryptCondition(tableName, (InExpression) expression, ((InExpression) expression).getRight());
        }
        if (expression instanceof BetweenExpression) {
            throw new ShardingSphereException("The SQL clause 'BETWEEN...AND...' is unsupported in encrypt rule.");
        }
        return Optional.empty();
    }
    
    private Optional<EncryptCondition> createBinaryEncryptCondition(final BinaryOperationExpression expression, final String tableName) {
        String operator = expression.getOperator();
        if (!LOGICAL_OPERATOR.contains(operator)) {
            if (SUPPORTED_COMPARE_OPERATOR.contains(operator)) {
                return createCompareEncryptCondition(tableName, expression, expression.getRight());
            }
            throw new ShardingSphereException("The SQL clause '%s' is unsupported in encrypt rule.", operator);
        }
        return Optional.empty();
    }
    
    private Map<String, String> getColumnTableNames(final SQLStatementContext<?> sqlStatementContext, final Collection<AndPredicate> andPredicates) {
        Collection<ColumnProjection> columns = new LinkedList<>();
        for (AndPredicate each : andPredicates) {
            columns.addAll(getColumnProjections(each));
        }
        return sqlStatementContext.getTablesContext().findTableName(columns, schema);
    }
    
    private Collection<ColumnProjection> getColumnProjections(final AndPredicate predicate) {
        Collection<ColumnProjection> result = new LinkedList<>();
        for (ExpressionSegment each : predicate.getPredicates()) {
            for (ColumnSegment column : ColumnExtractor.extract(each)) {
                result.add(buildColumnProjection(column));
            }
        }
        return result;
    }
    
    private ColumnProjection buildColumnProjection(final ColumnSegment segment) {
        String owner = segment.getOwner().map(optional -> optional.getIdentifier().getValue()).orElse(null);
        return new ColumnProjection(owner, segment.getIdentifier().getValue(), null);
    }
    
    private Optional<EncryptCondition> createCompareEncryptCondition(final String tableName, final BinaryOperationExpression expression, final ExpressionSegment compareRightValue) {
        if (!(expression.getLeft() instanceof ColumnSegment)) {
            return Optional.empty();
        }
        return (compareRightValue instanceof SimpleExpressionSegment && !(compareRightValue instanceof SubqueryExpressionSegment))
                ? Optional.of(createEncryptEqualCondition(tableName, expression, compareRightValue)) : Optional.empty();
    }
    
    private EncryptEqualCondition createEncryptEqualCondition(final String tableName, final BinaryOperationExpression expression, final ExpressionSegment compareRightValue) {
        String columnName = ((ColumnSegment) expression.getLeft()).getIdentifier().getValue();
        return new EncryptEqualCondition(columnName, tableName, compareRightValue.getStartIndex(), expression.getStopIndex(), compareRightValue);
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
        if (expressionSegments.isEmpty()) {
            return Optional.empty();
        }
        String columnName = ((ColumnSegment) inExpression.getLeft()).getIdentifier().getValue();
        return Optional.of(new EncryptInCondition(columnName, tableName, inRightValue.getStartIndex(), inRightValue.getStopIndex(), expressionSegments));
    }
}
