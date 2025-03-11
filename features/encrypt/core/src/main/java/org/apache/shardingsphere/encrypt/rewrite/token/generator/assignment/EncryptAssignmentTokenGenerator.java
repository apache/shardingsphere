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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.assignment;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptLiteralAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptParameterAssignmentToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Assignment generator for encrypt.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public final class EncryptAssignmentTokenGenerator {
    
    private final EncryptRule rule;
    
    private final String databaseName;
    
    private final DatabaseType databaseType;
    
    /**
     * Generate SQL tokens.
     *
     * @param tablesContext SQL statement context
     * @param setAssignmentSegment set assignment segment
     * @return generated SQL tokens
     */
    public Collection<SQLToken> generateSQLTokens(final TablesContext tablesContext, final SetAssignmentSegment setAssignmentSegment) {
        String tableName = tablesContext.getSimpleTables().iterator().next().getTableName().getIdentifier().getValue();
        EncryptTable encryptTable = rule.getEncryptTable(tableName);
        Collection<SQLToken> result = new LinkedList<>();
        String schemaName = tablesContext.getSchemaName().orElseGet(() -> new DatabaseTypeRegistry(databaseType).getDefaultSchemaName(databaseName));
        QuoteCharacter quoteCharacter = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getQuoteCharacter();
        for (ColumnAssignmentSegment each : setAssignmentSegment.getAssignments()) {
            String columnName = each.getColumns().get(0).getIdentifier().getValue();
            if (encryptTable.isEncryptColumn(columnName)) {
                generateSQLToken(schemaName, encryptTable.getTable(), encryptTable.getEncryptColumn(columnName), each, quoteCharacter).ifPresent(result::add);
            }
        }
        return result;
    }
    
    private Optional<EncryptAssignmentToken> generateSQLToken(final String schemaName, final String tableName, final EncryptColumn encryptColumn, final ColumnAssignmentSegment segment,
                                                              final QuoteCharacter quoteCharacter) {
        if (segment.getValue() instanceof ParameterMarkerExpressionSegment) {
            return Optional.of(generateParameterSQLToken(encryptColumn, segment, quoteCharacter));
        }
        if (segment.getValue() instanceof LiteralExpressionSegment) {
            return Optional.of(generateLiteralSQLToken(schemaName, tableName, encryptColumn, segment, quoteCharacter));
        }
        return Optional.empty();
    }
    
    private EncryptAssignmentToken generateParameterSQLToken(final EncryptColumn encryptColumn, final ColumnAssignmentSegment segment, final QuoteCharacter quoteCharacter) {
        EncryptParameterAssignmentToken result =
                new EncryptParameterAssignmentToken(segment.getColumns().get(0).getStartIndex(), segment.getStopIndex(), quoteCharacter);
        result.addColumnName(encryptColumn.getCipher().getName());
        encryptColumn.getAssistedQuery().ifPresent(optional -> result.addColumnName(optional.getName()));
        encryptColumn.getLikeQuery().ifPresent(optional -> result.addColumnName(optional.getName()));
        return result;
    }
    
    private EncryptAssignmentToken generateLiteralSQLToken(final String schemaName, final String tableName, final EncryptColumn encryptColumn, final ColumnAssignmentSegment segment,
                                                           final QuoteCharacter quoteCharacter) {
        EncryptLiteralAssignmentToken result =
                new EncryptLiteralAssignmentToken(segment.getColumns().get(0).getStartIndex(), segment.getStopIndex(), quoteCharacter);
        addCipherAssignment(schemaName, tableName, encryptColumn, segment, result);
        addAssistedQueryAssignment(schemaName, tableName, encryptColumn, segment, result);
        addLikeAssignment(schemaName, tableName, encryptColumn, segment, result);
        return result;
    }
    
    private void addCipherAssignment(final String schemaName, final String tableName,
                                     final EncryptColumn encryptColumn, final ColumnAssignmentSegment segment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) segment.getValue()).getLiterals();
        Object cipherValue = encryptColumn.getCipher().encrypt(databaseName, schemaName, tableName, encryptColumn.getName(), Collections.singletonList(originalValue)).iterator().next();
        token.addAssignment(encryptColumn.getCipher().getName(), cipherValue);
    }
    
    private void addAssistedQueryAssignment(final String schemaName, final String tableName, final EncryptColumn encryptColumn,
                                            final ColumnAssignmentSegment segment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) segment.getValue()).getLiterals();
        if (encryptColumn.getAssistedQuery().isPresent()) {
            Object assistedQueryValue = encryptColumn.getAssistedQuery().get().encrypt(
                    databaseName, schemaName, tableName, encryptColumn.getName(), Collections.singletonList(originalValue)).iterator().next();
            token.addAssignment(encryptColumn.getAssistedQuery().get().getName(), assistedQueryValue);
        }
    }
    
    private void addLikeAssignment(final String schemaName, final String tableName,
                                   final EncryptColumn encryptColumn, final ColumnAssignmentSegment segment, final EncryptLiteralAssignmentToken token) {
        Object originalValue = ((LiteralExpressionSegment) segment.getValue()).getLiterals();
        if (encryptColumn.getLikeQuery().isPresent()) {
            Object assistedQueryValue = encryptColumn.getLikeQuery().get().encrypt(databaseName, schemaName,
                    tableName, segment.getColumns().get(0).getIdentifier().getValue(), Collections.singletonList(originalValue)).iterator().next();
            token.addAssignment(encryptColumn.getLikeQuery().get().getName(), assistedQueryValue);
        }
    }
}
