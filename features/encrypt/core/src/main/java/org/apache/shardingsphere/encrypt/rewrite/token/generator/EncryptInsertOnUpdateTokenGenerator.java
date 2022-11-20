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

package org.apache.shardingsphere.encrypt.rewrite.token.generator;

import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rewrite.aware.DatabaseNameAware;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptFunctionAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptLiteralAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptParameterAssignmentToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.aware.EncryptRuleAware;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
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
@Setter
public final class EncryptInsertOnUpdateTokenGenerator implements CollectionSQLTokenGenerator<InsertStatementContext>, EncryptRuleAware, DatabaseNameAware {
    
    private EncryptRule encryptRule;
    
    private String databaseName;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext<?> sqlStatementContext) {
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
        String schemaName = insertStatementContext.getTablesContext().getSchemaName().orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(insertStatementContext.getDatabaseType(), databaseName));
        for (AssignmentSegment each : onDuplicateKeyColumnsSegments) {
            boolean leftEncryptorPresent = encryptRule.findEncryptor(tableName, each.getColumns().get(0).getIdentifier().getValue()).isPresent();
            if (each.getValue() instanceof FunctionSegment && "VALUES".equalsIgnoreCase(((FunctionSegment) each.getValue()).getFunctionName())) {
                ColumnSegment rightColumn = (ColumnSegment) ((FunctionSegment) each.getValue()).getParameters().stream().findFirst().get();
                boolean rightEncryptorPresent = encryptRule.findEncryptor(tableName, rightColumn.getIdentifier().getValue()).isPresent();
                if (!leftEncryptorPresent && !rightEncryptorPresent) {
                    continue;
                }
            }
            if (!leftEncryptorPresent) {
                continue;
            }
            generateSQLToken(schemaName, tableName, each).ifPresent(result::add);
        }
        return result;
    }
    
    private Optional<EncryptAssignmentToken> generateSQLToken(final String schemaName, final String tableName, final AssignmentSegment assignmentSegment) {
        if (assignmentSegment.getValue() instanceof ParameterMarkerExpressionSegment) {
            return Optional.of(generateParameterSQLToken(tableName, assignmentSegment));
        }
        if (assignmentSegment.getValue() instanceof FunctionSegment && "VALUES".equalsIgnoreCase(((FunctionSegment) assignmentSegment.getValue()).getFunctionName())) {
            return Optional.of(generateValuesSQLToken(tableName, assignmentSegment, (FunctionSegment) assignmentSegment.getValue()));
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
        addLikeQueryColumn(tableName, columnName, result);
        addPlainColumn(tableName, columnName, result);
        return result;
    }
    
    private EncryptAssignmentToken generateLiteralSQLToken(final String schemaName, final String tableName, final AssignmentSegment assignmentSegment) {
        EncryptLiteralAssignmentToken result = new EncryptLiteralAssignmentToken(assignmentSegment.getColumns().get(0).getStartIndex(), assignmentSegment.getStopIndex());
        addCipherAssignment(schemaName, tableName, assignmentSegment, result);
        addAssistedQueryAssignment(schemaName, tableName, assignmentSegment, result);
        addLikeAssignment(schemaName, tableName, assignmentSegment, result);
        addPlainAssignment(tableName, assignmentSegment, result);
        return result;
    }
    
    private EncryptAssignmentToken generateValuesSQLToken(final String tableName, final AssignmentSegment assignmentSegment, final FunctionSegment functionSegment) {
        ColumnSegment columnSegment = assignmentSegment.getColumns().get(0);
        String column = columnSegment.getIdentifier().getValue();
        ColumnSegment valueColumnSegment = (ColumnSegment) functionSegment.getParameters().stream().findFirst().get();
        String valueColumn = valueColumnSegment.getIdentifier().getValue();
        EncryptFunctionAssignmentToken result = new EncryptFunctionAssignmentToken(columnSegment.getStartIndex(), assignmentSegment.getStopIndex());
        boolean cipherColumnPresent = encryptRule.findEncryptor(tableName, column).isPresent();
        boolean cipherValueColumnPresent = encryptRule.findEncryptor(tableName, valueColumn).isPresent();
        if (cipherColumnPresent && cipherValueColumnPresent) {
            String cipherColumn = encryptRule.getCipherColumn(tableName, column);
            String cipherValueColumn = encryptRule.getCipherColumn(tableName, valueColumn);
            result.addAssignment(cipherColumn, "VALUES(" + cipherValueColumn + ")");
        } else if (cipherColumnPresent != cipherValueColumnPresent) {
            throw new UnsupportedEncryptSQLException(String.format("%s=VALUES(%s)", column, valueColumn));
        }
        Optional<String> assistedQueryColumn = encryptRule.findAssistedQueryColumn(tableName, column);
        Optional<String> valueAssistedQueryColumn = encryptRule.findAssistedQueryColumn(tableName, valueColumn);
        if (assistedQueryColumn.isPresent() && valueAssistedQueryColumn.isPresent()) {
            result.addAssignment(assistedQueryColumn.get(), "VALUES(" + valueAssistedQueryColumn.get() + ")");
        } else if (assistedQueryColumn.isPresent() != valueAssistedQueryColumn.isPresent()) {
            throw new UnsupportedEncryptSQLException(String.format("%s=VALUES(%s)", column, valueColumn));
        }
        Optional<String> likeQueryColumn = encryptRule.findLikeQueryColumn(tableName, column);
        Optional<String> valueLikeQueryColumn = encryptRule.findLikeQueryColumn(tableName, valueColumn);
        if (likeQueryColumn.isPresent() && valueLikeQueryColumn.isPresent()) {
            result.addAssignment(likeQueryColumn.get(), "VALUES(" + valueLikeQueryColumn.get() + ")");
        } else if (likeQueryColumn.isPresent() != valueLikeQueryColumn.isPresent()) {
            throw new UnsupportedEncryptSQLException(String.format("%s=VALUES(%s)", column, valueColumn));
        }
        Optional<String> plainColumn = encryptRule.findPlainColumn(tableName, column);
        Optional<String> valuePlainColumn = encryptRule.findPlainColumn(tableName, valueColumn);
        if (plainColumn.isPresent() && valuePlainColumn.isPresent()) {
            result.addAssignment(plainColumn.get(), "VALUES(" + valuePlainColumn.get() + ")");
        } else if (plainColumn.isPresent() != valuePlainColumn.isPresent()) {
            throw new UnsupportedEncryptSQLException(String.format("%s=VALUES(%s)", column, valueColumn));
        }
        if (result.getAssignment().isEmpty()) {
            throw new UnsupportedEncryptSQLException(String.format("%s=VALUES(%s)", column, valueColumn));
        }
        return result;
    }
    
    private void addCipherColumn(final String tableName, final String columnName, final EncryptParameterAssignmentToken token) {
        token.addColumnName(encryptRule.getCipherColumn(tableName, columnName));
    }
    
    private void addAssistedQueryColumn(final String tableName, final String columnName, final EncryptParameterAssignmentToken token) {
        encryptRule.findAssistedQueryColumn(tableName, columnName).ifPresent(token::addColumnName);
    }
    
    private void addLikeQueryColumn(final String tableName, final String columnName, final EncryptParameterAssignmentToken token) {
        encryptRule.findLikeQueryColumn(tableName, columnName).ifPresent(token::addColumnName);
    }
    
    private void addPlainColumn(final String tableName, final String columnName, final EncryptParameterAssignmentToken token) {
        encryptRule.findPlainColumn(tableName, columnName).ifPresent(token::addColumnName);
    }
    
    private void addCipherAssignment(final String schemaName, final String tableName, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Object cipherValue = encryptRule.getEncryptValues(databaseName, schemaName, tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue(),
                Collections.singletonList(originalValue)).iterator().next();
        token.addAssignment(encryptRule.getCipherColumn(tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue()), cipherValue);
    }
    
    private void addAssistedQueryAssignment(final String schemaName, final String tableName, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        encryptRule.findAssistedQueryColumn(tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue()).ifPresent(optional -> {
            Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
            Object assistedQueryValue = encryptRule
                    .getEncryptAssistedQueryValues(databaseName, schemaName, tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue(), Collections.singletonList(originalValue))
                    .iterator().next();
            token.addAssignment(optional, assistedQueryValue);
        });
    }
    
    private void addLikeAssignment(final String schemaName, final String tableName, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        encryptRule.findLikeQueryColumn(tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue()).ifPresent(optional -> {
            Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
            Object likeValue = encryptRule
                    .getEncryptLikeQueryValues(databaseName, schemaName, tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue(), Collections.singletonList(originalValue))
                    .iterator().next();
            token.addAssignment(optional, likeValue);
        });
    }
    
    private void addPlainAssignment(final String tableName, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        encryptRule.findPlainColumn(tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue()).ifPresent(optional -> token.addAssignment(optional, originalValue));
    }
}
