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

package org.apache.shardingsphere.core.rewrite.token.generator.collection.impl;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.builder.standard.StandardParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.token.generator.ParameterBuilderAware;
import org.apache.shardingsphere.core.rewrite.token.generator.collection.CollectionSQLTokenGenerator;
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
@Setter
public final class UpdateEncryptColumnTokenGenerator implements CollectionSQLTokenGenerator, EncryptRuleAware, ParameterBuilderAware {
    
    private EncryptRule encryptRule;
    
    private ParameterBuilder parameterBuilder;
    
    @Override
    public Collection<EncryptColumnToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof UpdateStatement ? createUpdateEncryptColumnTokens(sqlStatementContext) : Collections.<EncryptColumnToken>emptyList();
    }
    
    private Collection<EncryptColumnToken> createUpdateEncryptColumnTokens(final SQLStatementContext sqlStatementContext) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        for (AssignmentSegment each : ((UpdateStatement) sqlStatementContext.getSqlStatement()).getSetAssignment().getAssignments()) {
            if (encryptRule.findShardingEncryptor(tableName, each.getColumn().getName()).isPresent()) {
                result.add(createUpdateEncryptColumnToken(tableName, each));
            }
        }
        return result;
    }
    
    private EncryptColumnToken createUpdateEncryptColumnToken(final String tableName, final AssignmentSegment assignmentSegment) {
        return assignmentSegment.getValue() instanceof ParameterMarkerExpressionSegment 
                ? createUpdateEncryptParameterColumnToken(tableName, assignmentSegment) 
                : createUpdateEncryptLiteralColumnToken(tableName, assignmentSegment);
    }
    
    private EncryptColumnToken createUpdateEncryptParameterColumnToken(final String tableName, final AssignmentSegment assignmentSegment) {
        UpdateEncryptParameterColumnToken result = new UpdateEncryptParameterColumnToken(assignmentSegment.getColumn().getStartIndex(), assignmentSegment.getStopIndex());
        Optional<String> plainColumn = encryptRule.findPlainColumn(tableName, assignmentSegment.getColumn().getName());
        if (plainColumn.isPresent()) {
            result.addUpdateColumn(plainColumn.get());
            result.addUpdateColumn(encryptRule.getCipherColumn(tableName, assignmentSegment.getColumn().getName()));
            addCipherValueToParameterBuilder(tableName, assignmentSegment);
        } else {
            result.addUpdateColumn(encryptRule.getCipherColumn(tableName, assignmentSegment.getColumn().getName()));
            setCipherValueToParameterBuilder(tableName, assignmentSegment);
        }
        addEncryptUpdateColumn(tableName, assignmentSegment, result);
        return result;
    }
    
    private void addCipherValueToParameterBuilder(final String tableName, final AssignmentSegment assignmentSegment) {
        int valueIndex = ((ParameterMarkerExpressionSegment) assignmentSegment.getValue()).getParameterMarkerIndex();
        Object originalValue = parameterBuilder.getOriginalParameters().get(valueIndex);
        Object cipherValue = encryptRule.getEncryptValues(tableName, assignmentSegment.getColumn().getName(), Collections.singletonList(originalValue)).iterator().next();
        ((StandardParameterBuilder) parameterBuilder).getAddedIndexAndParameters().put(valueIndex + 1, cipherValue);
    }
    
    private void setCipherValueToParameterBuilder(final String tableName, final AssignmentSegment assignmentSegment) {
        int valueIndex = ((ParameterMarkerExpressionSegment) assignmentSegment.getValue()).getParameterMarkerIndex();
        Object originalValue = parameterBuilder.getOriginalParameters().get(valueIndex);
        Object cipherValue = encryptRule.getEncryptValues(tableName, assignmentSegment.getColumn().getName(), Collections.singletonList(originalValue)).iterator().next();
        ((StandardParameterBuilder) parameterBuilder).getReplacedIndexAndParameters().put(valueIndex, cipherValue);
    }
    
    private void addEncryptUpdateColumn(final String tableName, final AssignmentSegment assignmentSegment, final UpdateEncryptParameterColumnToken token) {
        Optional<String> assistedQueryColumn = encryptRule.findAssistedQueryColumn(tableName, assignmentSegment.getColumn().getName());
        if (assistedQueryColumn.isPresent()) {
            token.addUpdateColumn(assistedQueryColumn.get());
            addEncryptColumnsToParameterBuilder(tableName, assignmentSegment);
        }
    }
    
    private void addEncryptColumnsToParameterBuilder(final String tableName, final AssignmentSegment assignmentSegment) {
        int valueIndex = ((ParameterMarkerExpressionSegment) assignmentSegment.getValue()).getParameterMarkerIndex();
        Object originalValue = parameterBuilder.getOriginalParameters().get(valueIndex);
        Object assistedQueryValue = encryptRule.getEncryptAssistedQueryValues(
                tableName, assignmentSegment.getColumn().getName(), Collections.singletonList(originalValue)).iterator().next();
        ((StandardParameterBuilder) parameterBuilder).getAddedIndexAndParameters().put(valueIndex + 2, assistedQueryValue);
    }
    
    private EncryptColumnToken createUpdateEncryptLiteralColumnToken(final String tableName, final AssignmentSegment assignmentSegment) {
        UpdateEncryptLiteralColumnToken result = new UpdateEncryptLiteralColumnToken(assignmentSegment.getColumn().getStartIndex(), assignmentSegment.getStopIndex());
        addPlainUpdateColumn(tableName, assignmentSegment, result);
        addCipherUpdateColumn(tableName, assignmentSegment, result);
        addAssistedQueryUpdateColumn(tableName, assignmentSegment, result);
        return result;
    }
    
    private void addPlainUpdateColumn(final String tableName, final AssignmentSegment assignmentSegment, final UpdateEncryptLiteralColumnToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Optional<String> plainColumn = encryptRule.findPlainColumn(tableName, assignmentSegment.getColumn().getName());
        if (plainColumn.isPresent()) {
            token.addUpdateColumn(plainColumn.get(), originalValue);
        }
    }
    
    private void addCipherUpdateColumn(final String tableName, final AssignmentSegment assignmentSegment, final UpdateEncryptLiteralColumnToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Object cipherValue = encryptRule.getEncryptValues(tableName, assignmentSegment.getColumn().getName(), Collections.singletonList(originalValue)).iterator().next();
        token.addUpdateColumn(encryptRule.getCipherColumn(tableName, assignmentSegment.getColumn().getName()), cipherValue);
    }
    
    private void addAssistedQueryUpdateColumn(final String tableName, final AssignmentSegment assignmentSegment, final UpdateEncryptLiteralColumnToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Optional<String> assistedQueryColumn = encryptRule.findAssistedQueryColumn(tableName, assignmentSegment.getColumn().getName());
        if (assistedQueryColumn.isPresent()) {
            Object assistedQueryValue = encryptRule.getEncryptAssistedQueryValues(tableName, assignmentSegment.getColumn().getName(), Collections.singletonList(originalValue)).iterator().next();
            token.addUpdateColumn(assistedQueryColumn.get(), assistedQueryValue);
        }
    }
}
