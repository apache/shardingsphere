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
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Insert on update values token generator for encrypt.
 */
public final class EncryptInsertOnUpdateTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator<InsertStatementContext> {
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext 
                && InsertStatementHandler.getOnDuplicateKeyColumnsSegment(((InsertStatementContext) sqlStatementContext).getSqlStatement()).isPresent();
    }
    
    @Override
    public Collection<EncryptAssignmentToken> generateSQLTokens(final InsertStatementContext insertStatementContext) {
        Collection<EncryptAssignmentToken> result = new LinkedList<>();
        InsertStatement insertStatement = insertStatementContext.getSqlStatement();
        String tableName = insertStatement.getTable().getTableName().getIdentifier().getValue();
        Preconditions.checkState(InsertStatementHandler.getOnDuplicateKeyColumnsSegment(insertStatement).isPresent());
        OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment = InsertStatementHandler.getOnDuplicateKeyColumnsSegment(insertStatement).get();
        Collection<AssignmentSegment> onDuplicateKeyColumnsSegments = onDuplicateKeyColumnsSegment.getColumns();
        if (onDuplicateKeyColumnsSegments.isEmpty()) {
            return result;
        }
        String schemaName = insertStatementContext.getSchemaName();
        for (AssignmentSegment each : onDuplicateKeyColumnsSegments) {
            if (getEncryptRule().findEncryptor(schemaName, tableName, each.getColumns().get(0).getIdentifier().getValue()).isPresent()) {
                generateSQLToken(schemaName, tableName, each).ifPresent(result::add);
            }
        }
        return result;
    }
    
    private Optional<EncryptAssignmentToken> generateSQLToken(final String schemaName, final String tableName, final AssignmentSegment assignmentSegment) {
        if (assignmentSegment.getValue() instanceof ParameterMarkerExpressionSegment) {
            return Optional.of(generateParameterSQLToken(tableName, assignmentSegment));
        }
        if (assignmentSegment.getValue() instanceof FunctionSegment && "VALUES".equalsIgnoreCase(((FunctionSegment) assignmentSegment.getValue()).getFunctionName())) {
            return Optional.of(generateValuesSQLToken(schemaName, tableName, assignmentSegment, (FunctionSegment) assignmentSegment.getValue()));
        }
        if (assignmentSegment.getValue() instanceof LiteralExpressionSegment) {
            return Optional.of(generateLiteralSQLToken(schemaName, tableName, assignmentSegment));
        }
        return Optional.empty();
    }
    
    private EncryptAssignmentToken generateParameterSQLToken(final String tableName, final AssignmentSegment assignmentSegment) {
        EncryptParameterAssignmentToken result = new EncryptParameterAssignmentToken(assignmentSegment.getColumns().get(0).getStartIndex(), assignmentSegment.getStopIndex());
        String columnName = assignmentSegment.getColumns().get(0).getIdentifier().getValue();
        addCipherColumn(tableName, columnName, result);
        addAssistedQueryColumn(tableName, columnName, result);
        addPlainColumn(tableName, columnName, result);
        return result;
    }
    
    private EncryptAssignmentToken generateLiteralSQLToken(final String schemaName, final String tableName, final AssignmentSegment assignmentSegment) {
        EncryptLiteralAssignmentToken result = new EncryptLiteralAssignmentToken(assignmentSegment.getColumns().get(0).getStartIndex(), assignmentSegment.getStopIndex());
        addCipherAssignment(schemaName, tableName, assignmentSegment, result);
        addAssistedQueryAssignment(schemaName, tableName, assignmentSegment, result);
        addPlainAssignment(tableName, assignmentSegment, result);
        return result;
    }
    
    private EncryptAssignmentToken generateValuesSQLToken(final String schemaName, final String tableName, final AssignmentSegment assignmentSegment, final FunctionSegment functionSegment) {
        ColumnSegment column = assignmentSegment.getColumns().get(0);
        ColumnSegment valueColumn = (ColumnSegment) functionSegment.getParameters().stream().findFirst().get();
        EncryptLiteralAssignmentToken result = new EncryptLiteralAssignmentToken(column.getStartIndex(), assignmentSegment.getStopIndex());
        String cipherColumn = getEncryptRule().getCipherColumn(tableName, column.getIdentifier().getValue());
        String cipherValueColumn = getEncryptRule().getCipherColumn(tableName, valueColumn.getIdentifier().getValue());
        result.addAssignment(cipherColumn, String.format("VALUES(%s)", cipherValueColumn), false);
        getEncryptRule().findAssistedQueryColumn(tableName, column.getIdentifier().getValue()).ifPresent(assistedQueryColumn -> {
            getEncryptRule().findAssistedQueryColumn(tableName, valueColumn.getIdentifier().getValue()).ifPresent(valueAssistedQueryColumn -> {
                result.addAssignment(assistedQueryColumn, String.format("VALUES(%s)", valueAssistedQueryColumn), false);
            });
        });
        getEncryptRule().findPlainColumn(tableName, column.getIdentifier().getValue()).ifPresent(plainColumn -> {
            getEncryptRule().findPlainColumn(tableName, valueColumn.getIdentifier().getValue()).ifPresent(valuePlainColumn -> {
                result.addAssignment(plainColumn, String.format("VALUES(%s)", valuePlainColumn), false);
            });
        });
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
    
    private void addCipherAssignment(final String schemaName, final String tableName, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Object cipherValue = getEncryptRule().getEncryptValues(schemaName, tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue(), 
                Collections.singletonList(originalValue)).iterator().next();
        token.addAssignment(getEncryptRule().getCipherColumn(tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue()), cipherValue, true);
    }
    
    private void addAssistedQueryAssignment(final String schemaName, final String tableName, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        getEncryptRule().findAssistedQueryColumn(tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue()).ifPresent(assistedQueryColumn -> {
            Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
            Object assistedQueryValue = getEncryptRule()
                    .getEncryptAssistedQueryValues(schemaName, tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue(), Collections.singletonList(originalValue))
                    .iterator().next();
            token.addAssignment(assistedQueryColumn, assistedQueryValue, true);
        });
    }
    
    private void addPlainAssignment(final String tableName, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        getEncryptRule().findPlainColumn(tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue()).ifPresent(plainColumn -> token.addAssignment(plainColumn, originalValue, true));
    }
}
