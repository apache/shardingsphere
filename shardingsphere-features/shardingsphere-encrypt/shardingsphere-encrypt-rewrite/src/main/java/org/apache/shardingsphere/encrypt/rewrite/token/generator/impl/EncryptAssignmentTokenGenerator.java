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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.impl;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptLiteralAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptParameterAssignmentToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Assignment generator for encrypt.
 */
public final class EncryptAssignmentTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator {
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof UpdateStatementContext
                || (sqlStatementContext instanceof InsertStatementContext 
                && InsertStatementHandler.getSetAssignmentSegment(((InsertStatementContext) sqlStatementContext).getSqlStatement()).isPresent());
    }
    
    @Override
    public Collection<EncryptAssignmentToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<EncryptAssignmentToken> result = new LinkedList<>();
        String tableName = ((TableAvailable) sqlStatementContext).getAllTables().iterator().next().getTableName().getIdentifier().getValue();
        for (AssignmentSegment each : getSetAssignmentSegment(sqlStatementContext.getSqlStatement()).getAssignments()) {
            if (getEncryptRule().findEncryptor(tableName, each.getColumn().getIdentifier().getValue()).isPresent()) {
                generateSQLToken(tableName, each).ifPresent(result::add);
            }
        }
        return result;
    }
    
    private SetAssignmentSegment getSetAssignmentSegment(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof InsertStatement) {
            Optional<SetAssignmentSegment> result = InsertStatementHandler.getSetAssignmentSegment((InsertStatement) sqlStatement);
            Preconditions.checkState(result.isPresent());
            return result.get();
        }
        return ((UpdateStatement) sqlStatement).getSetAssignment();
    }
    
    private Optional<EncryptAssignmentToken> generateSQLToken(final String tableName, final AssignmentSegment assignmentSegment) {
        if (assignmentSegment.getValue() instanceof ParameterMarkerExpressionSegment) {
            return Optional.of(generateParameterSQLToken(tableName, assignmentSegment));
        }
        if (assignmentSegment.getValue() instanceof LiteralExpressionSegment) {
            return Optional.of(generateLiteralSQLToken(tableName, assignmentSegment));
        }
        return Optional.empty();
    }
    
    private EncryptAssignmentToken generateParameterSQLToken(final String tableName, final AssignmentSegment assignmentSegment) {
        EncryptParameterAssignmentToken result = new EncryptParameterAssignmentToken(assignmentSegment.getColumn().getStartIndex(), assignmentSegment.getStopIndex());
        String columnName = assignmentSegment.getColumn().getIdentifier().getValue();
        addCipherColumn(tableName, columnName, result);
        addAssistedQueryColumn(tableName, columnName, result);
        addPlainColumn(tableName, columnName, result);
        return result;
    }
    
    private void addCipherColumn(final String tableName, final String columnName, final EncryptParameterAssignmentToken token) {
        token.addColumnName(getEncryptRule().getCipherColumn(tableName, columnName));
    }
    
    private void addAssistedQueryColumn(final String tableName, final String columnName, final EncryptParameterAssignmentToken token) {
        getEncryptRule().findAssistedQueryColumn(tableName, columnName).ifPresent(token::addColumnName);
    }
    
    private void addPlainColumn(final String tableName, final String columnName, final EncryptParameterAssignmentToken token) {
        getEncryptRule().findPlainColumn(tableName, columnName).ifPresent(token::addColumnName);
    }
    
    private EncryptAssignmentToken generateLiteralSQLToken(final String tableName, final AssignmentSegment assignmentSegment) {
        EncryptLiteralAssignmentToken result = new EncryptLiteralAssignmentToken(assignmentSegment.getColumn().getStartIndex(), assignmentSegment.getStopIndex());
        addCipherAssignment(tableName, assignmentSegment, result);
        addAssistedQueryAssignment(tableName, assignmentSegment, result);
        addPlainAssignment(tableName, assignmentSegment, result);
        return result;
    }
    
    private void addCipherAssignment(final String tableName, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Object cipherValue = getEncryptRule().getEncryptValues(tableName, assignmentSegment.getColumn().getIdentifier().getValue(), Collections.singletonList(originalValue)).iterator().next();
        token.addAssignment(getEncryptRule().getCipherColumn(tableName, assignmentSegment.getColumn().getIdentifier().getValue()), cipherValue);
    }
    
    private void addAssistedQueryAssignment(final String tableName, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(tableName, assignmentSegment.getColumn().getIdentifier().getValue());
        assistedQueryColumn.ifPresent(s -> {
            Object assistedQueryValue = getEncryptRule().getEncryptAssistedQueryValues(
                    tableName, assignmentSegment.getColumn().getIdentifier().getValue(), Collections.singletonList(originalValue)).iterator().next();
            token.addAssignment(s, assistedQueryValue);
        });
    }
    
    private void addPlainAssignment(final String tableName, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        getEncryptRule().findPlainColumn(tableName, assignmentSegment.getColumn().getIdentifier().getValue()).ifPresent(plainColumn -> token.addAssignment(plainColumn, originalValue));
    }
}
