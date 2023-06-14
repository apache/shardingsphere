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
import org.apache.shardingsphere.encrypt.rewrite.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptFunctionAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptLiteralAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptParameterAssignmentToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
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
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext
                && InsertStatementHandler.getOnDuplicateKeyColumnsSegment(((InsertStatementContext) sqlStatementContext).getSqlStatement()).isPresent();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final InsertStatementContext insertStatementContext) {
        InsertStatement insertStatement = insertStatementContext.getSqlStatement();
        Preconditions.checkState(InsertStatementHandler.getOnDuplicateKeyColumnsSegment(insertStatement).isPresent());
        OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment = InsertStatementHandler.getOnDuplicateKeyColumnsSegment(insertStatement).get();
        Collection<AssignmentSegment> onDuplicateKeyColumnsSegments = onDuplicateKeyColumnsSegment.getColumns();
        if (onDuplicateKeyColumnsSegments.isEmpty()) {
            return Collections.emptyList();
        }
        String schemaName = insertStatementContext.getTablesContext().getSchemaName().orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(insertStatementContext.getDatabaseType(), databaseName));
        String tableName = insertStatement.getTable().getTableName().getIdentifier().getValue();
        EncryptTable encryptTable = encryptRule.getEncryptTable(tableName);
        Collection<SQLToken> result = new LinkedList<>();
        for (AssignmentSegment each : onDuplicateKeyColumnsSegments) {
            boolean leftEncryptorPresent = encryptRule.findStandardEncryptor(tableName, each.getColumns().get(0).getIdentifier().getValue()).isPresent();
            if (each.getValue() instanceof FunctionSegment && "VALUES".equalsIgnoreCase(((FunctionSegment) each.getValue()).getFunctionName())) {
                ColumnSegment rightColumn = (ColumnSegment) ((FunctionSegment) each.getValue()).getParameters().stream().findFirst().get();
                boolean rightEncryptorPresent = encryptRule.findStandardEncryptor(tableName, rightColumn.getIdentifier().getValue()).isPresent();
                if (!leftEncryptorPresent && !rightEncryptorPresent) {
                    continue;
                }
            }
            if (!leftEncryptorPresent) {
                continue;
            }
            generateSQLToken(schemaName, encryptTable, each).ifPresent(result::add);
        }
        return result;
    }
    
    private Optional<EncryptAssignmentToken> generateSQLToken(final String schemaName, final EncryptTable encryptTable, final AssignmentSegment assignmentSegment) {
        if (assignmentSegment.getValue() instanceof ParameterMarkerExpressionSegment) {
            return Optional.of(generateParameterSQLToken(encryptTable, assignmentSegment));
        }
        if (assignmentSegment.getValue() instanceof FunctionSegment && "VALUES".equalsIgnoreCase(((FunctionSegment) assignmentSegment.getValue()).getFunctionName())) {
            return Optional.of(generateValuesSQLToken(encryptTable, assignmentSegment, (FunctionSegment) assignmentSegment.getValue()));
        }
        if (assignmentSegment.getValue() instanceof LiteralExpressionSegment) {
            return Optional.of(generateLiteralSQLToken(schemaName, encryptTable, assignmentSegment));
        }
        return Optional.empty();
    }
    
    private EncryptAssignmentToken generateParameterSQLToken(final EncryptTable encryptTable, final AssignmentSegment assignmentSegment) {
        EncryptParameterAssignmentToken result = new EncryptParameterAssignmentToken(assignmentSegment.getColumns().get(0).getStartIndex(), assignmentSegment.getStopIndex());
        String columnName = assignmentSegment.getColumns().get(0).getIdentifier().getValue();
        result.addColumnName(encryptTable.getCipherColumn(columnName));
        encryptTable.findAssistedQueryColumn(columnName).ifPresent(result::addColumnName);
        encryptTable.findLikeQueryColumn(columnName).ifPresent(result::addColumnName);
        return result;
    }
    
    private EncryptAssignmentToken generateLiteralSQLToken(final String schemaName, final EncryptTable encryptTable, final AssignmentSegment assignmentSegment) {
        EncryptLiteralAssignmentToken result = new EncryptLiteralAssignmentToken(assignmentSegment.getColumns().get(0).getStartIndex(), assignmentSegment.getStopIndex());
        addCipherAssignment(schemaName, encryptTable, assignmentSegment, result);
        addAssistedQueryAssignment(schemaName, encryptTable, assignmentSegment, result);
        addLikeAssignment(schemaName, encryptTable, assignmentSegment, result);
        return result;
    }
    
    private EncryptAssignmentToken generateValuesSQLToken(final EncryptTable encryptTable, final AssignmentSegment assignmentSegment, final FunctionSegment functionSegment) {
        ColumnSegment columnSegment = assignmentSegment.getColumns().get(0);
        String column = columnSegment.getIdentifier().getValue();
        ColumnSegment valueColumnSegment = (ColumnSegment) functionSegment.getParameters().stream().findFirst().get();
        String valueColumn = valueColumnSegment.getIdentifier().getValue();
        EncryptFunctionAssignmentToken result = new EncryptFunctionAssignmentToken(columnSegment.getStartIndex(), assignmentSegment.getStopIndex());
        boolean cipherColumnPresent = encryptRule.findStandardEncryptor(encryptTable.getTable(), column).isPresent();
        boolean cipherValueColumnPresent = encryptRule.findStandardEncryptor(encryptTable.getTable(), valueColumn).isPresent();
        if (cipherColumnPresent && cipherValueColumnPresent) {
            String cipherColumn = encryptTable.getCipherColumn(column);
            String cipherValueColumn = encryptTable.getCipherColumn(valueColumn);
            result.addAssignment(cipherColumn, "VALUES(" + cipherValueColumn + ")");
        } else if (cipherColumnPresent != cipherValueColumnPresent) {
            throw new UnsupportedEncryptSQLException(String.format("%s=VALUES(%s)", column, valueColumn));
        }
        Optional<String> assistedQueryColumn = encryptTable.findAssistedQueryColumn(column);
        Optional<String> valueAssistedQueryColumn = encryptTable.findAssistedQueryColumn(valueColumn);
        if (assistedQueryColumn.isPresent() && valueAssistedQueryColumn.isPresent()) {
            result.addAssignment(assistedQueryColumn.get(), "VALUES(" + valueAssistedQueryColumn.get() + ")");
        } else if (assistedQueryColumn.isPresent() != valueAssistedQueryColumn.isPresent()) {
            throw new UnsupportedEncryptSQLException(String.format("%s=VALUES(%s)", column, valueColumn));
        }
        Optional<String> likeQueryColumn = encryptTable.findLikeQueryColumn(column);
        Optional<String> valueLikeQueryColumn = encryptTable.findLikeQueryColumn(valueColumn);
        if (likeQueryColumn.isPresent() && valueLikeQueryColumn.isPresent()) {
            result.addAssignment(likeQueryColumn.get(), "VALUES(" + valueLikeQueryColumn.get() + ")");
        } else if (likeQueryColumn.isPresent() != valueLikeQueryColumn.isPresent()) {
            throw new UnsupportedEncryptSQLException(String.format("%s=VALUES(%s)", column, valueColumn));
        }
        if (result.getAssignment().isEmpty()) {
            throw new UnsupportedEncryptSQLException(String.format("%s=VALUES(%s)", column, valueColumn));
        }
        return result;
    }
    
    private void addCipherAssignment(final String schemaName, final EncryptTable encryptTable, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Object cipherValue = encryptRule.encrypt(databaseName, schemaName, encryptTable.getTable(), assignmentSegment.getColumns().get(0).getIdentifier().getValue(),
                Collections.singletonList(originalValue)).iterator().next();
        token.addAssignment(encryptTable.getCipherColumn(assignmentSegment.getColumns().get(0).getIdentifier().getValue()), cipherValue);
    }
    
    private void addAssistedQueryAssignment(final String schemaName, final EncryptTable encryptTable, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        encryptTable.findAssistedQueryColumn(assignmentSegment.getColumns().get(0).getIdentifier().getValue()).ifPresent(optional -> {
            Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
            Object assistedQueryValue = encryptRule.getEncryptAssistedQueryValues(
                    databaseName, schemaName, encryptTable.getTable(), assignmentSegment.getColumns().get(0).getIdentifier().getValue(), Collections.singletonList(originalValue)).iterator().next();
            token.addAssignment(optional, assistedQueryValue);
        });
    }
    
    private void addLikeAssignment(final String schemaName, final EncryptTable encryptTable, final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        encryptTable.findLikeQueryColumn(assignmentSegment.getColumns().get(0).getIdentifier().getValue()).ifPresent(optional -> {
            Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
            Object likeValue = encryptRule.getEncryptLikeQueryValues(
                    databaseName, schemaName, encryptTable.getTable(), assignmentSegment.getColumns().get(0).getIdentifier().getValue(), Collections.singletonList(originalValue)).iterator().next();
            token.addAssignment(optional, likeValue);
        });
    }
}
