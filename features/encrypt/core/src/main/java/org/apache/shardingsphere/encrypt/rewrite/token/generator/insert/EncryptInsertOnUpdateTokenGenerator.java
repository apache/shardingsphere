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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.insert;

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
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
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
        Collection<AssignmentSegment> onDuplicateKeyColumnsSegments = InsertStatementHandler.getOnDuplicateKeyColumnsSegment(insertStatement).get().getColumns();
        if (onDuplicateKeyColumnsSegments.isEmpty()) {
            return Collections.emptyList();
        }
        String schemaName = insertStatementContext.getTablesContext().getSchemaName().orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(insertStatementContext.getDatabaseType(), databaseName));
        String tableName = insertStatement.getTable().getTableName().getIdentifier().getValue();
        EncryptTable encryptTable = encryptRule.getEncryptTable(tableName);
        Collection<SQLToken> result = new LinkedList<>();
        for (AssignmentSegment each : onDuplicateKeyColumnsSegments) {
            boolean leftColumnIsEncrypt = encryptTable.isEncryptColumn(each.getColumns().get(0).getIdentifier().getValue());
            if (each.getValue() instanceof FunctionSegment && "VALUES".equalsIgnoreCase(((FunctionSegment) each.getValue()).getFunctionName())) {
                Optional<ExpressionSegment> rightColumnSegment = ((FunctionSegment) each.getValue()).getParameters().stream().findFirst();
                Preconditions.checkState(rightColumnSegment.isPresent());
                boolean rightColumnIsEncrypt = encryptTable.isEncryptColumn(((ColumnSegment) rightColumnSegment.get()).getIdentifier().getValue());
                if (!leftColumnIsEncrypt && !rightColumnIsEncrypt) {
                    continue;
                }
            }
            if (!leftColumnIsEncrypt) {
                continue;
            }
            EncryptColumn encryptColumn = encryptTable.getEncryptColumn(each.getColumns().get(0).getIdentifier().getValue());
            generateSQLToken(schemaName, encryptTable, encryptColumn, each).ifPresent(result::add);
        }
        return result;
    }
    
    private Optional<EncryptAssignmentToken> generateSQLToken(final String schemaName, final EncryptTable encryptTable, final EncryptColumn encryptColumn, final AssignmentSegment assignmentSegment) {
        if (assignmentSegment.getValue() instanceof ParameterMarkerExpressionSegment) {
            return Optional.of(generateParameterSQLToken(encryptTable, assignmentSegment));
        }
        if (assignmentSegment.getValue() instanceof FunctionSegment && "VALUES".equalsIgnoreCase(((FunctionSegment) assignmentSegment.getValue()).getFunctionName())) {
            return Optional.of(generateValuesSQLToken(encryptTable, assignmentSegment, (FunctionSegment) assignmentSegment.getValue()));
        }
        if (assignmentSegment.getValue() instanceof LiteralExpressionSegment) {
            return Optional.of(generateLiteralSQLToken(schemaName, encryptTable.getTable(), encryptColumn, assignmentSegment));
        }
        return Optional.empty();
    }
    
    private EncryptAssignmentToken generateParameterSQLToken(final EncryptTable encryptTable, final AssignmentSegment assignmentSegment) {
        EncryptParameterAssignmentToken result = new EncryptParameterAssignmentToken(assignmentSegment.getColumns().get(0).getStartIndex(), assignmentSegment.getStopIndex());
        String columnName = assignmentSegment.getColumns().get(0).getIdentifier().getValue();
        EncryptColumn encryptColumn = encryptTable.getEncryptColumn(columnName);
        result.addColumnName(encryptColumn.getCipher().getName());
        encryptColumn.getAssistedQuery().ifPresent(optional -> result.addColumnName(optional.getName()));
        encryptColumn.getLikeQuery().ifPresent(optional -> result.addColumnName(optional.getName()));
        return result;
    }
    
    private EncryptAssignmentToken generateLiteralSQLToken(final String schemaName, final String tableName, final EncryptColumn encryptColumn, final AssignmentSegment assignmentSegment) {
        EncryptLiteralAssignmentToken result = new EncryptLiteralAssignmentToken(assignmentSegment.getColumns().get(0).getStartIndex(), assignmentSegment.getStopIndex());
        addCipherAssignment(schemaName, tableName, encryptColumn, assignmentSegment, result);
        addAssistedQueryAssignment(schemaName, tableName, encryptColumn, assignmentSegment, result);
        addLikeAssignment(schemaName, tableName, encryptColumn, assignmentSegment, result);
        return result;
    }
    
    private EncryptAssignmentToken generateValuesSQLToken(final EncryptTable encryptTable, final AssignmentSegment assignmentSegment, final FunctionSegment functionSegment) {
        ColumnSegment columnSegment = assignmentSegment.getColumns().get(0);
        String column = columnSegment.getIdentifier().getValue();
        Optional<ExpressionSegment> valueColumnSegment = functionSegment.getParameters().stream().findFirst();
        Preconditions.checkState(valueColumnSegment.isPresent());
        String valueColumn = ((ColumnSegment) valueColumnSegment.get()).getIdentifier().getValue();
        EncryptFunctionAssignmentToken result = new EncryptFunctionAssignmentToken(columnSegment.getStartIndex(), assignmentSegment.getStopIndex());
        boolean isEncryptColumn = encryptTable.isEncryptColumn(column);
        boolean isEncryptValueColumn = encryptTable.isEncryptColumn(valueColumn);
        EncryptColumn encryptColumn = encryptTable.getEncryptColumn(column);
        EncryptColumn encryptValueColumn = encryptTable.getEncryptColumn(column);
        if (isEncryptColumn && isEncryptValueColumn) {
            String cipherColumn = encryptColumn.getCipher().getName();
            String cipherValueColumn = encryptValueColumn.getCipher().getName();
            result.addAssignment(cipherColumn, "VALUES(" + cipherValueColumn + ")");
        } else if (isEncryptColumn != isEncryptValueColumn) {
            throw new UnsupportedEncryptSQLException(String.format("%s=VALUES(%s)", column, valueColumn));
        }
        Optional<AssistedQueryColumnItem> assistedQueryColumn = encryptColumn.getAssistedQuery();
        Optional<AssistedQueryColumnItem> valueAssistedQueryColumn = encryptValueColumn.getAssistedQuery();
        if (assistedQueryColumn.isPresent() && valueAssistedQueryColumn.isPresent()) {
            result.addAssignment(assistedQueryColumn.get().getName(), "VALUES(" + valueAssistedQueryColumn.get().getName() + ")");
        } else if (assistedQueryColumn.isPresent() != valueAssistedQueryColumn.isPresent()) {
            throw new UnsupportedEncryptSQLException(String.format("%s=VALUES(%s)", column, valueColumn));
        }
        Optional<LikeQueryColumnItem> likeQueryColumn = encryptColumn.getLikeQuery();
        Optional<LikeQueryColumnItem> valueLikeQueryColumn = encryptValueColumn.getLikeQuery();
        if (likeQueryColumn.isPresent() && valueLikeQueryColumn.isPresent()) {
            result.addAssignment(likeQueryColumn.get().getName(), "VALUES(" + valueLikeQueryColumn.get().getName() + ")");
        } else if (likeQueryColumn.isPresent() != valueLikeQueryColumn.isPresent()) {
            throw new UnsupportedEncryptSQLException(String.format("%s=VALUES(%s)", column, valueColumn));
        }
        if (result.getAssignment().isEmpty()) {
            throw new UnsupportedEncryptSQLException(String.format("%s=VALUES(%s)", column, valueColumn));
        }
        return result;
    }
    
    private void addCipherAssignment(final String schemaName, final String tableName, final EncryptColumn encryptColumn,
                                     final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Object cipherValue = encryptColumn.getCipher().encrypt(databaseName, schemaName, tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue(),
                Collections.singletonList(originalValue)).iterator().next();
        token.addAssignment(encryptColumn.getCipher().getName(), cipherValue);
    }
    
    private void addAssistedQueryAssignment(final String schemaName, final String tableName, final EncryptColumn encryptColumn,
                                            final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        encryptColumn.getAssistedQuery().ifPresent(optional -> {
            Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
            Object assistedQueryValue = optional.encrypt(
                    databaseName, schemaName, tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue(), Collections.singletonList(originalValue)).iterator().next();
            token.addAssignment(optional.getName(), assistedQueryValue);
        });
    }
    
    private void addLikeAssignment(final String schemaName, final String tableName, final EncryptColumn encryptColumn,
                                   final AssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        encryptColumn.getLikeQuery().ifPresent(optional -> {
            Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
            Object likeValue = optional.encrypt(
                    databaseName, schemaName, tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue(), Collections.singletonList(originalValue)).iterator().next();
            token.addAssignment(optional.getName(), likeValue);
        });
    }
}
