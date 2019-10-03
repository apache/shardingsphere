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

package org.apache.shardingsphere.core.rewrite.sql.token.generator.collection.impl;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.collection.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.impl.EncryptAssignmentToken;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.impl.EncryptLiteralAssignmentToken;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.impl.EncryptParameterAssignmentToken;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Assignment generator for encrypt.
 *
 * @author panjuan
 */
@Setter
public final class EncryptAssignmentTokenGenerator implements CollectionSQLTokenGenerator, EncryptRuleAware {
    
    private EncryptRule encryptRule;
    
    @Override
    public Collection<EncryptAssignmentToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext.getSqlStatement() instanceof UpdateStatement) {
            Collection<EncryptAssignmentToken> result = new LinkedList<>();
            String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
            for (AssignmentSegment each : ((UpdateStatement) sqlStatementContext.getSqlStatement()).getSetAssignment().getAssignments()) {
                if (encryptRule.findShardingEncryptor(tableName, each.getColumn().getName()).isPresent()) {
                    result.add(generateSQLToken(tableName, each));
                }
            }
            return result;
        }
        return Collections.emptyList();
    }
    
    private EncryptAssignmentToken generateSQLToken(final String tableName, final AssignmentSegment assignmentSegment) {
        return assignmentSegment.getValue() instanceof ParameterMarkerExpressionSegment 
                ? generateParameterSQLToken(tableName, assignmentSegment) : generateLiteralSQLToken(tableName, assignmentSegment);
    }
    
    private EncryptAssignmentToken generateParameterSQLToken(final String tableName, final AssignmentSegment assignmentSegment) {
        EncryptParameterAssignmentToken result = new EncryptParameterAssignmentToken(assignmentSegment.getColumn().getStartIndex(), assignmentSegment.getStopIndex());
        String columnName = assignmentSegment.getColumn().getName();
        Optional<String> plainColumn = encryptRule.findPlainColumn(tableName, columnName);
        if (plainColumn.isPresent()) {
            result.addColumnName(plainColumn.get());
        }
        result.addColumnName(encryptRule.getCipherColumn(tableName, columnName));
        Optional<String> assistedQueryColumn = encryptRule.findAssistedQueryColumn(tableName, columnName);
        if (assistedQueryColumn.isPresent()) {
            result.addColumnName(assistedQueryColumn.get());
        }
        return result;
    }
    
    private EncryptAssignmentToken generateLiteralSQLToken(final String tableName, final AssignmentSegment assignmentSegment) {
        EncryptLiteralAssignmentToken result = new EncryptLiteralAssignmentToken(assignmentSegment.getColumn().getStartIndex(), assignmentSegment.getStopIndex());
        addPlainAssignment(tableName, assignmentSegment, result);
        addCipherAssignment(tableName, assignmentSegment, result);
        addAssistedQueryAssignment(tableName, assignmentSegment, result);
        return result;
    }
    
    private void addPlainAssignment(final String tableName, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Optional<String> plainColumn = encryptRule.findPlainColumn(tableName, assignmentSegment.getColumn().getName());
        if (plainColumn.isPresent()) {
            token.addAssignment(plainColumn.get(), originalValue);
        }
    }
    
    private void addCipherAssignment(final String tableName, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Object cipherValue = encryptRule.getEncryptValues(tableName, assignmentSegment.getColumn().getName(), Collections.singletonList(originalValue)).iterator().next();
        token.addAssignment(encryptRule.getCipherColumn(tableName, assignmentSegment.getColumn().getName()), cipherValue);
    }
    
    private void addAssistedQueryAssignment(final String tableName, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Optional<String> assistedQueryColumn = encryptRule.findAssistedQueryColumn(tableName, assignmentSegment.getColumn().getName());
        if (assistedQueryColumn.isPresent()) {
            Object assistedQueryValue = encryptRule.getEncryptAssistedQueryValues(tableName, assignmentSegment.getColumn().getName(), Collections.singletonList(originalValue)).iterator().next();
            token.addAssignment(assistedQueryColumn.get(), assistedQueryValue);
        }
    }
}
