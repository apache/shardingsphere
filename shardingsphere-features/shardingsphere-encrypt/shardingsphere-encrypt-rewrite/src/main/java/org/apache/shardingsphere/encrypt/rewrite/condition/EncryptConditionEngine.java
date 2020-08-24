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
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.WhereAvailable;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

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
        List<EncryptCondition> result = new LinkedList<>();
        for (AndPredicate each : whereSegment.get().getAndPredicates()) {
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
        for (PredicateSegment predicate : andPredicate.getPredicates()) {
            if (stopIndexes.add(predicate.getStopIndex())) {
                createEncryptCondition(sqlStatementContext, predicate).ifPresent(result::add);
            }
        }
        return result;
    }
    
    private Optional<EncryptCondition> createEncryptCondition(final SQLStatementContext sqlStatementContext, final PredicateSegment predicateSegment) {
        Optional<String> tableName = sqlStatementContext.getTablesContext().findTableName(predicateSegment.getColumn(), schemaMetaData);
        return tableName.isPresent() && encryptRule.findEncryptor(tableName.get(), predicateSegment.getColumn().getIdentifier().getValue()).isPresent()
                ? createEncryptCondition(predicateSegment, tableName.get()) : Optional.empty();
    }
    
    private Optional<EncryptCondition> createEncryptCondition(final PredicateSegment predicateSegment, final String tableName) {
        if (predicateSegment.getRightValue() instanceof PredicateCompareRightValue) {
            PredicateCompareRightValue compareRightValue = (PredicateCompareRightValue) predicateSegment.getRightValue();
            return isSupportedOperator(compareRightValue.getOperator()) ? createCompareEncryptCondition(tableName, predicateSegment, compareRightValue) : Optional.empty();
        }
        if (predicateSegment.getRightValue() instanceof PredicateInRightValue) {
            return createInEncryptCondition(tableName, predicateSegment, (PredicateInRightValue) predicateSegment.getRightValue());
        }
        if (predicateSegment.getRightValue() instanceof PredicateBetweenRightValue) {
            throw new ShardingSphereException("The SQL clause 'BETWEEN...AND...' is unsupported in encrypt rule.");
        }
        return Optional.empty();
    }
    
    private static Optional<EncryptCondition> createCompareEncryptCondition(final String tableName, final PredicateSegment predicateSegment, final PredicateCompareRightValue compareRightValue) {
        return compareRightValue.getExpression() instanceof SimpleExpressionSegment
                ? Optional.of(new EncryptEqualCondition(predicateSegment.getColumn().getIdentifier().getValue(), tableName, compareRightValue.getExpression().getStartIndex(), 
                predicateSegment.getStopIndex(), compareRightValue.getExpression()))
                : Optional.empty();
    }
    
    private static Optional<EncryptCondition> createInEncryptCondition(final String tableName, final PredicateSegment predicateSegment, final PredicateInRightValue inRightValue) {
        List<ExpressionSegment> expressionSegments = new LinkedList<>();
        for (ExpressionSegment each : inRightValue.getSqlExpressions()) {
            if (each instanceof SimpleExpressionSegment) {
                expressionSegments.add(each);
            }
        }
        return expressionSegments.isEmpty() ? Optional.empty()
                : Optional.of(new EncryptInCondition(predicateSegment.getColumn().getIdentifier().getValue(), 
                tableName, inRightValue.getStartIndex(), inRightValue.getStopIndex(), expressionSegments));
    }
    
    private boolean isSupportedOperator(final String operator) {
        return "=".equals(operator) || "<>".equals(operator) || "!=".equals(operator);
    }
}
