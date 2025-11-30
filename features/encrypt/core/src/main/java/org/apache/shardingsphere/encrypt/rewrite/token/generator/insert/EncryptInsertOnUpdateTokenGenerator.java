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
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptFunctionAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptLiteralAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptParameterAssignmentToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Insert on update values token generator for encrypt.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
@Setter
public final class EncryptInsertOnUpdateTokenGenerator implements CollectionSQLTokenGenerator<InsertStatementContext> {
    
    private final EncryptRule rule;
    
    private final ShardingSphereDatabase database;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext && (((InsertStatementContext) sqlStatementContext).getSqlStatement()).getOnDuplicateKeyColumns().isPresent();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final InsertStatementContext insertStatementContext) {
        InsertStatement insertStatement = insertStatementContext.getSqlStatement();
        Preconditions.checkState(insertStatement.getOnDuplicateKeyColumns().isPresent());
        Collection<ColumnAssignmentSegment> onDuplicateKeyColumnsSegments = insertStatement.getOnDuplicateKeyColumns().get().getColumns();
        if (onDuplicateKeyColumnsSegments.isEmpty()) {
            return Collections.emptyList();
        }
        String schemaName = insertStatementContext.getTablesContext().getSchemaName()
                .orElseGet(() -> new DatabaseTypeRegistry(insertStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(database.getName()));
        String tableName = insertStatement.getTable().map(optional -> optional.getTableName().getIdentifier().getValue()).orElse("");
        EncryptTable encryptTable = rule.getEncryptTable(tableName);
        QuoteCharacter quoteCharacter = new DatabaseTypeRegistry(insertStatementContext.getSqlStatement().getDatabaseType()).getDialectDatabaseMetaData().getQuoteCharacter();
        Collection<SQLToken> result = new LinkedList<>();
        for (ColumnAssignmentSegment each : onDuplicateKeyColumnsSegments) {
            boolean leftColumnIsEncrypt = encryptTable.isEncryptColumn(each.getColumns().get(0).getIdentifier().getValue());
            if (each.getValue() instanceof FunctionSegment && "VALUES".equalsIgnoreCase(((FunctionSegment) each.getValue()).getFunctionName())) {
                FunctionSegment functionSegment = (FunctionSegment) each.getValue();
                ExpressionSegment rightColumnSegment = functionSegment.getParameters().isEmpty() ? null : functionSegment.getParameters().iterator().next();
                ShardingSpherePreconditions.checkNotNull(rightColumnSegment, () -> new IllegalArgumentException("right column segment can not be null"));
                boolean rightColumnIsEncrypt = encryptTable.isEncryptColumn(((ColumnSegment) rightColumnSegment).getIdentifier().getValue());
                if (!leftColumnIsEncrypt && !rightColumnIsEncrypt) {
                    continue;
                }
            }
            if (!leftColumnIsEncrypt) {
                continue;
            }
            EncryptColumn encryptColumn = encryptTable.getEncryptColumn(each.getColumns().get(0).getIdentifier().getValue());
            generateSQLToken(schemaName, encryptTable, encryptColumn, each, quoteCharacter).ifPresent(result::add);
        }
        return result;
    }
    
    private Optional<EncryptAssignmentToken> generateSQLToken(final String schemaName, final EncryptTable encryptTable,
                                                              final EncryptColumn encryptColumn, final ColumnAssignmentSegment assignmentSegment, final QuoteCharacter quoteCharacter) {
        if (assignmentSegment.getValue() instanceof ParameterMarkerExpressionSegment) {
            return Optional.of(generateParameterSQLToken(encryptTable, assignmentSegment, quoteCharacter));
        }
        if (assignmentSegment.getValue() instanceof FunctionSegment && "VALUES".equalsIgnoreCase(((FunctionSegment) assignmentSegment.getValue()).getFunctionName())) {
            return Optional.of(generateValuesSQLToken(encryptTable, assignmentSegment, (FunctionSegment) assignmentSegment.getValue(), quoteCharacter));
        }
        if (assignmentSegment.getValue() instanceof LiteralExpressionSegment) {
            return Optional.of(generateLiteralSQLToken(schemaName, encryptTable.getTable(), encryptColumn, assignmentSegment, quoteCharacter));
        }
        return Optional.empty();
    }
    
    private EncryptAssignmentToken generateParameterSQLToken(final EncryptTable encryptTable, final ColumnAssignmentSegment assignmentSegment, final QuoteCharacter quoteCharacter) {
        EncryptParameterAssignmentToken result = new EncryptParameterAssignmentToken(assignmentSegment.getColumns().get(0).getStartIndex(), assignmentSegment.getStopIndex(), quoteCharacter);
        String columnName = assignmentSegment.getColumns().get(0).getIdentifier().getValue();
        EncryptColumn encryptColumn = encryptTable.getEncryptColumn(columnName);
        result.addColumnName(encryptColumn.getCipher().getName());
        encryptColumn.getAssistedQuery().ifPresent(optional -> result.addColumnName(optional.getName()));
        encryptColumn.getLikeQuery().ifPresent(optional -> result.addColumnName(optional.getName()));
        return result;
    }
    
    private EncryptAssignmentToken generateLiteralSQLToken(final String schemaName, final String tableName,
                                                           final EncryptColumn encryptColumn, final ColumnAssignmentSegment assignmentSegment, final QuoteCharacter quoteCharacter) {
        EncryptLiteralAssignmentToken result = new EncryptLiteralAssignmentToken(assignmentSegment.getColumns().get(0).getStartIndex(), assignmentSegment.getStopIndex(), quoteCharacter);
        addCipherAssignment(schemaName, tableName, encryptColumn, assignmentSegment, result);
        addAssistedQueryAssignment(schemaName, tableName, encryptColumn, assignmentSegment, result);
        addLikeAssignment(schemaName, tableName, encryptColumn, assignmentSegment, result);
        return result;
    }
    
    private EncryptAssignmentToken generateValuesSQLToken(final EncryptTable encryptTable, final ColumnAssignmentSegment assignmentSegment, final FunctionSegment functionSegment,
                                                          final QuoteCharacter quoteCharacter) {
        ColumnSegment columnSegment = assignmentSegment.getColumns().get(0);
        String column = columnSegment.getIdentifier().getValue();
        ExpressionSegment valueColumnSegment = functionSegment.getParameters().isEmpty() ? null : functionSegment.getParameters().iterator().next();
        ShardingSpherePreconditions.checkNotNull(valueColumnSegment, () -> new IllegalArgumentException("value column segment can not be null"));
        String valueColumn = ((ColumnSegment) valueColumnSegment).getIdentifier().getValue();
        EncryptFunctionAssignmentToken result =
                new EncryptFunctionAssignmentToken(columnSegment.getStartIndex(), assignmentSegment.getStopIndex(), quoteCharacter);
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
        ShardingSpherePreconditions.checkState(!result.isAssignmentsEmpty(), () -> new UnsupportedEncryptSQLException(String.format("%s=VALUES(%s)", column, valueColumn)));
        return result;
    }
    
    private void addCipherAssignment(final String schemaName, final String tableName, final EncryptColumn encryptColumn,
                                     final ColumnAssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
        Object cipherValue = encryptColumn.getCipher().encrypt(database.getName(), schemaName, tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue(),
                Collections.singletonList(originalValue)).iterator().next();
        token.addAssignment(encryptColumn.getCipher().getName(), cipherValue);
    }
    
    private void addAssistedQueryAssignment(final String schemaName, final String tableName, final EncryptColumn encryptColumn,
                                            final ColumnAssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        encryptColumn.getAssistedQuery().ifPresent(optional -> {
            Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
            Object assistedQueryValue = optional.encrypt(
                    database.getName(), schemaName, tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue(), Collections.singletonList(originalValue)).iterator().next();
            token.addAssignment(optional.getName(), assistedQueryValue);
        });
    }
    
    private void addLikeAssignment(final String schemaName, final String tableName, final EncryptColumn encryptColumn,
                                   final ColumnAssignmentSegment assignmentSegment, final EncryptLiteralAssignmentToken token) {
        encryptColumn.getLikeQuery().ifPresent(optional -> {
            Object originalValue = ((LiteralExpressionSegment) assignmentSegment.getValue()).getLiterals();
            Object likeValue = optional.encrypt(
                    database.getName(), schemaName, tableName, assignmentSegment.getColumns().get(0).getIdentifier().getValue(), Collections.singletonList(originalValue)).iterator().next();
            token.addAssignment(optional.getName(), likeValue);
        });
    }
}
