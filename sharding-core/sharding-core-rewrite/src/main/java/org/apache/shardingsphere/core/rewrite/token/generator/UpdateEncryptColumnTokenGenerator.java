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
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.rewrite.builder.BaseParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.EncryptColumnToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.UpdateEncryptLiteralColumnToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.UpdateEncryptParameterColumnToken;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Update encrypt column token generator.
 *
 * @author panjuan
 */
public final class UpdateEncryptColumnTokenGenerator implements CollectionSQLTokenGenerator<EncryptRule> {
    
    @Override
    public Collection<EncryptColumnToken> generateSQLTokens(
            final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final boolean isQueryWithCipherColumn) {
        return optimizedStatement.getSQLStatement() instanceof UpdateStatement 
                ? createUpdateEncryptColumnTokens(parameterBuilder, encryptRule, optimizedStatement) : Collections.<EncryptColumnToken>emptyList();
    }
    
    private Collection<EncryptColumnToken> createUpdateEncryptColumnTokens(final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final OptimizedStatement optimizedStatement) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        String tableName = optimizedStatement.getTables().getSingleTableName();
        for (AssignmentSegment each : ((UpdateStatement) optimizedStatement.getSQLStatement()).getSetAssignment().getAssignments()) {
            if (encryptRule.getShardingEncryptor(tableName, each.getColumn().getName()).isPresent()) {
                result.add(createUpdateEncryptColumnToken(parameterBuilder, encryptRule, tableName, each));
            }
        }
        return result;
    }
    
    private EncryptColumnToken createUpdateEncryptColumnToken(final ParameterBuilder parameterBuilder, 
                                                              final EncryptRule encryptRule, final String tableName, final AssignmentSegment assignmentSegment) {
        return assignmentSegment.getValue() instanceof ParameterMarkerExpressionSegment 
                ? createUpdateEncryptParameterColumnToken(parameterBuilder, encryptRule, tableName, assignmentSegment) 
                : createUpdateEncryptLiteralColumnToken(encryptRule, tableName, assignmentSegment);
    }
    
    private EncryptColumnToken createUpdateEncryptParameterColumnToken(final ParameterBuilder parameterBuilder, 
                                                                       final EncryptRule encryptRule, final String tableName, final AssignmentSegment assignmentSegment) {
        UpdateEncryptParameterColumnToken result = new UpdateEncryptParameterColumnToken(assignmentSegment.getColumn().getStartIndex(), assignmentSegment.getStopIndex());
        Optional<String> plainColumn = encryptRule.getPlainColumn(tableName, assignmentSegment.getColumn().getName());
        if (plainColumn.isPresent()) {
            result.addUpdateColumn(plainColumn.get());
            result.addUpdateColumn(encryptRule.getCipherColumn(tableName, assignmentSegment.getColumn().getName()));
            addCipherColumnValueToParameterBuilder(parameterBuilder, encryptRule, tableName, assignmentSegment);
        } else {
            result.addUpdateColumn(encryptRule.getCipherColumn(tableName, assignmentSegment.getColumn().getName()));
            setCipherColumnValueToParameterBuilder(parameterBuilder, encryptRule, tableName, assignmentSegment);
        }
        addAssistedQueryUpdateColumn(parameterBuilder, encryptRule, tableName, assignmentSegment, result);
        return result;
    }
    
    private void addCipherColumnValueToParameterBuilder(final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final String tableName, final AssignmentSegment assignmentSegment) {
        int logicColumnValueIndex = ((ParameterMarkerExpressionSegment) assignmentSegment.getValue()).getParameterMarkerIndex();
        Object originalColumnValue = parameterBuilder.getOriginalParameters().get(logicColumnValueIndex);
        Object cipherColumnValue = encryptRule.getEncryptColumnValues(
                tableName, assignmentSegment.getColumn().getName(), Collections.singletonList(originalColumnValue)).iterator().next();
        ((BaseParameterBuilder) parameterBuilder).getAddedIndexAndParameters().put(logicColumnValueIndex + 1, cipherColumnValue);
    }
    
    private void setCipherColumnValueToParameterBuilder(final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final String tableName, final AssignmentSegment assignmentSegment) {
        int logicColumnValueIndex = ((ParameterMarkerExpressionSegment) assignmentSegment.getValue()).getParameterMarkerIndex();
        Object originalColumnValue = parameterBuilder.getOriginalParameters().get(logicColumnValueIndex);
        Object cipherColumnValue = encryptRule.getEncryptColumnValues(
                tableName, assignmentSegment.getColumn().getName(), Collections.singletonList(originalColumnValue)).iterator().next();
        ((BaseParameterBuilder) parameterBuilder).getReplacedIndexAndParameters().put(logicColumnValueIndex, cipherColumnValue);
    }
    
    private void addAssistedQueryUpdateColumn(final ParameterBuilder parameterBuilder, final EncryptRule encryptRule,
                                              final String tableName, final AssignmentSegment assignmentSegment, final UpdateEncryptParameterColumnToken result) {
        Optional<String> assistedQueryColumn = encryptRule.getAssistedQueryColumn(tableName, assignmentSegment.getColumn().getName());
        if (assistedQueryColumn.isPresent()) {
            result.addUpdateColumn(assistedQueryColumn.get());
            addAssistedQueryColumnValueToParameterBuilder(parameterBuilder, encryptRule, tableName, assignmentSegment);
        }
    }
    
    private void addAssistedQueryColumnValueToParameterBuilder(final ParameterBuilder parameterBuilder, 
                                                               final EncryptRule encryptRule, final String tableName, final AssignmentSegment assignmentSegment) {
        int logicColumnValueIndex = ((ParameterMarkerExpressionSegment) assignmentSegment.getValue()).getParameterMarkerIndex();
        Object originalColumnValue = parameterBuilder.getOriginalParameters().get(logicColumnValueIndex);
        Object assistedQueryColumnValue = encryptRule.getEncryptAssistedColumnValues(
                tableName, assignmentSegment.getColumn().getName(), Collections.singletonList(originalColumnValue)).iterator().next();
        ((BaseParameterBuilder) parameterBuilder).getAddedIndexAndParameters().put(logicColumnValueIndex + 2, assistedQueryColumnValue);
    }
    
    private EncryptColumnToken createUpdateEncryptLiteralColumnToken(final EncryptRule encryptRule, final String tableName, final AssignmentSegment assignmentSegment) {
        UpdateEncryptLiteralColumnToken result = new UpdateEncryptLiteralColumnToken(assignmentSegment.getColumn().getStartIndex(), assignmentSegment.getStopIndex());
        addPlainUpdateColumn(encryptRule, tableName, assignmentSegment, result);
        addCipherUpdateColumn(encryptRule, tableName, assignmentSegment, result);
        addAssistedQueryUpdateColumn(encryptRule, tableName, assignmentSegment, result);
        return result;
    }
    
    private void addPlainUpdateColumn(final EncryptRule encryptRule, final String tableName, final AssignmentSegment assignmentSegment, final UpdateEncryptLiteralColumnToken result) {
        Object originalColumnValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Optional<String> plainColumn = encryptRule.getPlainColumn(tableName, assignmentSegment.getColumn().getName());
        if (plainColumn.isPresent()) {
            result.addUpdateColumn(plainColumn.get(), originalColumnValue);
        }
    }
    
    private void addCipherUpdateColumn(final EncryptRule encryptRule, final String tableName, final AssignmentSegment assignmentSegment, final UpdateEncryptLiteralColumnToken result) {
        Object originalColumnValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Object cipherColumnValue = encryptRule.getEncryptColumnValues(
                tableName, assignmentSegment.getColumn().getName(), Collections.singletonList(originalColumnValue)).iterator().next();
        result.addUpdateColumn(encryptRule.getCipherColumn(tableName, assignmentSegment.getColumn().getName()), cipherColumnValue);
    }
    
    private void addAssistedQueryUpdateColumn(final EncryptRule encryptRule, final String tableName, final AssignmentSegment assignmentSegment, final UpdateEncryptLiteralColumnToken result) {
        Object originalColumnValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Optional<String> assistedQueryColumn = encryptRule.getAssistedQueryColumn(tableName, assignmentSegment.getColumn().getName());
        if (assistedQueryColumn.isPresent()) {
            Object assistedQueryColumnValue = encryptRule.getEncryptAssistedColumnValues(
                    tableName, assignmentSegment.getColumn().getName(), Collections.singletonList(originalColumnValue)).iterator().next();
            result.addUpdateColumn(assistedQueryColumn.get(), assistedQueryColumnValue);
        }
    }
}
