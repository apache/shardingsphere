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

package org.apache.shardingsphere.core.rewrite.token.generator;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.rewrite.builder.BaseParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.EncryptColumnToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.UpdateEncryptAssistedItemToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.UpdateEncryptItemToken;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Encrypt column token generator.
 *
 * @author panjuan
 */
public final class UpdateEncryptColumnTokenGenerator implements CollectionSQLTokenGenerator<EncryptRule> {
    
    private ParameterBuilder parameterBuilder;
    
    private EncryptRule encryptRule;
    
    @Override
    public Collection<EncryptColumnToken> generateSQLTokens(
            final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final boolean isQueryWithCipherColumn) {
        initParameter(parameterBuilder, encryptRule);
        return optimizedStatement.getSQLStatement() instanceof UpdateStatement ? createEncryptColumnToken(optimizedStatement) : Collections.<EncryptColumnToken>emptyList();
    }
    
    private void initParameter(final ParameterBuilder parameterBuilder, final EncryptRule encryptRule) {
        this.parameterBuilder = parameterBuilder;
        this.encryptRule = encryptRule;
    }
    
    private Collection<EncryptColumnToken> createEncryptColumnToken(final OptimizedStatement optimizedStatement) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        String tableName = optimizedStatement.getTables().getSingleTableName();
        for (AssignmentSegment each : ((UpdateStatement) optimizedStatement.getSQLStatement()).getSetAssignment().getAssignments()) {
            if (encryptRule.getEncryptEngine().getShardingEncryptor(tableName, each.getColumn().getName()).isPresent()) {
                result.add(createEncryptColumnToken(tableName, each));
            }
        }
        return result;
    }
    
    private EncryptColumnToken createEncryptColumnToken(final String tableName, final AssignmentSegment assignmentSegment) {
        Object originalColumnValue = getOriginalValue(assignmentSegment, parameterBuilder.getOriginalParameters());
        Object cipherColumnValue = encryptRule.getEncryptEngine().getEncryptColumnValues(
                tableName, assignmentSegment.getColumn().getName(), Collections.singletonList(originalColumnValue)).iterator().next();
        if (assignmentSegment.getValue() instanceof ParameterMarkerExpressionSegment) {
            parameterBuilder.getOriginalParameters().set(((ParameterMarkerExpressionSegment) assignmentSegment.getValue()).getParameterMarkerIndex(), cipherColumnValue);
        }
        Optional<String> assistedQueryColumnName = encryptRule.getEncryptEngine().getAssistedQueryColumn(tableName, assignmentSegment.getColumn().getName());
        if (!assistedQueryColumnName.isPresent()) {
            return createUpdateEncryptItemToken(tableName, assignmentSegment, cipherColumnValue);
        }
        Object assistedQueryColumnValue = encryptRule.getEncryptEngine().getEncryptAssistedColumnValues(
                tableName, assignmentSegment.getColumn().getName(), Collections.singletonList(originalColumnValue)).iterator().next();
        if (assignmentSegment.getValue() instanceof ParameterMarkerExpressionSegment) {
            ((BaseParameterBuilder) parameterBuilder).getAddedIndexAndParameters().put(((ParameterMarkerExpressionSegment) assignmentSegment.getValue()).getParameterMarkerIndex() + 1, assistedQueryColumnValue);
        }
        return createUpdateEncryptAssistedItemToken(tableName, assignmentSegment, cipherColumnValue, assistedQueryColumnName.get(), assistedQueryColumnValue);
    }
    
    private Object getOriginalValue(final AssignmentSegment assignmentSegment, final List<Object> parameters) {
        ExpressionSegment expressionSegment = assignmentSegment.getValue();
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            return parameters.get(((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex());
        }
        if (expressionSegment instanceof LiteralExpressionSegment) {
            return ((LiteralExpressionSegment) expressionSegment).getLiterals();
        }
        throw new ShardingException("Can not find column value by %s.", assignmentSegment.getColumn().getName());
    }
    
    private UpdateEncryptItemToken createUpdateEncryptItemToken(final String tableName, final AssignmentSegment assignmentSegment, final Object cipherColumnValue) {
        int startIndex = assignmentSegment.getColumn().getStartIndex();
        int stopIndex = assignmentSegment.getStopIndex();
        String cipherColumnName = encryptRule.getEncryptEngine().getCipherColumn(tableName, assignmentSegment.getColumn().getName());
        return assignmentSegment.getValue() instanceof ParameterMarkerExpressionSegment
                ? new UpdateEncryptItemToken(startIndex, stopIndex, cipherColumnName) : new UpdateEncryptItemToken(startIndex, stopIndex, cipherColumnName, cipherColumnValue);
    }
    
    private UpdateEncryptAssistedItemToken createUpdateEncryptAssistedItemToken(
            final String tableName, final AssignmentSegment assignmentSegment, final Object cipherColumnValue, final String assistedQueryColumnName, final Object assistedQueryColumnValue) {
        int startIndex = assignmentSegment.getColumn().getStartIndex();
        int stopIndex = assignmentSegment.getStopIndex();
        String cipherColumnName = encryptRule.getEncryptEngine().getCipherColumn(tableName, assignmentSegment.getColumn().getName());
        return assignmentSegment.getValue() instanceof ParameterMarkerExpressionSegment
                ? new UpdateEncryptAssistedItemToken(startIndex, stopIndex, cipherColumnName, assistedQueryColumnName)
                : new UpdateEncryptAssistedItemToken(startIndex, stopIndex, cipherColumnName, cipherColumnValue, assistedQueryColumnName, assistedQueryColumnValue);
    }
}
