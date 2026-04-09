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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.encrypt.enums.EncryptDerivedColumnSuffix;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptLiteralAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptParameterAssignmentToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Assignment generator for encrypt.
 */
@Slf4j
@HighFrequencyInvocation
public final class EncryptAssignmentTokenGenerator {
    
    private final EncryptRule rule;
    
    private final String databaseName;
    
    private final DatabaseType databaseType;
    
    public EncryptAssignmentTokenGenerator(final EncryptRule rule, final String databaseName, final DatabaseType databaseType) {
        this.rule = rule;
        this.databaseName = databaseName;
        this.databaseType = databaseType;
    }
    
    /**
     * Generate SQL tokens.
     *
     * @param tablesContext SQL statement context
     * @param setAssignmentSegment set assignment segment
     * @return generated SQL tokens
     */
    public Collection<SQLToken> generateSQLTokens(final TablesContext tablesContext, final SetAssignmentSegment setAssignmentSegment) {
        Collection<SQLToken> result = new LinkedList<>();
        DatabaseTypeRegistry databaseTypeRegistry = new DatabaseTypeRegistry(databaseType);
        String schemaName = tablesContext.getSchemaName().orElseGet(() -> databaseTypeRegistry.getDefaultSchemaName(databaseName));
        QuoteCharacter quoteCharacter = databaseTypeRegistry.getDialectDatabaseMetaData().getQuoteCharacter();
        for (ColumnAssignmentSegment each : setAssignmentSegment.getAssignments()) {
            ColumnSegment assignedColumn = getAssignedColumn(each);
            findEncryptTable(assignedColumn).ifPresent(encryptTable -> {
                String columnName = assignedColumn.getIdentifier().getValue();
                if (encryptTable.isEncryptColumn(columnName)) {
                    result.addAll(generateAssignmentSQLTokens(schemaName, encryptTable.getTable(), encryptTable.getEncryptColumn(columnName), each, quoteCharacter));
                }
            });
        }
        return result;
    }
    
    private Collection<SQLToken> generateAssignmentSQLTokens(final String schemaName, final String tableName, final EncryptColumn encryptColumn,
                                                             final ColumnAssignmentSegment segment, final QuoteCharacter quoteCharacter) {
        ExpressionSegment value = segment.getValue();
        if (value instanceof ParameterMarkerExpressionSegment) {
            return Collections.singleton(generateParameterSQLToken(encryptColumn, segment, quoteCharacter));
        }
        if (value instanceof LiteralExpressionSegment) {
            return Collections.singleton(generateLiteralSQLToken(schemaName, tableName, encryptColumn, segment, quoteCharacter));
        }
        return Collections.emptyList();
    }
    
    private EncryptAssignmentToken generateParameterSQLToken(final EncryptColumn encryptColumn, final ColumnAssignmentSegment segment, final QuoteCharacter quoteCharacter) {
        ColumnSegment leftColumn = getAssignedColumn(segment);
        EncryptParameterAssignmentToken result = new EncryptParameterAssignmentToken(leftColumn.getStartIndex(), segment.getStopIndex(), quoteCharacter);
        appendEncryptColumnTokens(leftColumn, encryptColumn, (targetName, suffix) -> result.addColumnName(targetName));
        return result;
    }
    
    private String getColumnName(final ColumnSegment columnSegment, final EncryptDerivedColumnSuffix derivedColumnSuffix, final String actualColumnName) {
        return TableSourceType.TEMPORARY_TABLE == columnSegment.getColumnBoundInfo().getTableSourceType()
                ? derivedColumnSuffix.getDerivedColumnName(columnSegment.getIdentifier().getValue(), databaseType)
                : actualColumnName;
    }
    
    private EncryptAssignmentToken generateLiteralSQLToken(final String schemaName, final String tableName, final EncryptColumn encryptColumn, final ColumnAssignmentSegment segment,
                                                           final QuoteCharacter quoteCharacter) {
        ColumnSegment leftColumn = getAssignedColumn(segment);
        EncryptLiteralAssignmentToken result = new EncryptLiteralAssignmentToken(leftColumn.getStartIndex(), segment.getStopIndex(), quoteCharacter);
        Object originalValue = ((LiteralExpressionSegment) segment.getValue()).getLiterals();
        appendEncryptColumnTokens(leftColumn, encryptColumn, (targetName, suffix) -> addLiteralSQLToken(schemaName, tableName, encryptColumn, targetName, suffix, result, originalValue));
        return result;
    }
    
    private void addLiteralSQLToken(final String schemaName, final String tableName, final EncryptColumn encryptColumn, final String targetColumnName, final EncryptDerivedColumnSuffix suffix,
                                    final EncryptLiteralAssignmentToken result, final Object originalValue) {
        if (null == suffix) {
            result.addAssignment(targetColumnName, originalValue);
        } else {
            Object encryptValue = encrypt(encryptColumn, suffix, databaseName, schemaName, tableName, encryptColumn.getName(), originalValue);
            result.addAssignment(targetColumnName, encryptValue);
        }
    }
    
    private Object encrypt(final EncryptColumn encryptColumn, final EncryptDerivedColumnSuffix suffix, final String databaseName,
                           final String schemaName, final String tableName, final String logicColumnName, final Object originalValue) {
        List<Object> originalValues = Collections.singletonList(originalValue);
        switch (suffix) {
            case CIPHER:
                return encryptColumn.getCipher().encrypt(databaseName, schemaName, tableName, logicColumnName, originalValues).iterator().next();
            case ASSISTED_QUERY:
                return encryptColumn.getAssistedQuery().map(optional -> optional.encrypt(databaseName, schemaName, tableName, logicColumnName, originalValues).iterator().next()).orElse(null);
            case LIKE_QUERY:
                return encryptColumn.getLikeQuery().map(optional -> optional.encrypt(databaseName, schemaName, tableName, logicColumnName, originalValues).iterator().next()).orElse(null);
            default:
                return null;
        }
    }
    
    private Optional<EncryptTable> findEncryptTable(final ColumnSegment columnSegment) {
        return rule.findEncryptTable(columnSegment.getColumnBoundInfo().getOriginalTable().getValue());
    }
    
    private void appendEncryptColumnTokens(final ColumnSegment leftColumn, final EncryptColumn encryptColumn, final EncryptColumnConsumer consumer) {
        consumer.accept(getColumnName(leftColumn, EncryptDerivedColumnSuffix.CIPHER, encryptColumn.getCipher().getName()), EncryptDerivedColumnSuffix.CIPHER);
        encryptColumn.getAssistedQuery()
                .ifPresent(optional -> consumer.accept(getColumnName(leftColumn, EncryptDerivedColumnSuffix.ASSISTED_QUERY, optional.getName()), EncryptDerivedColumnSuffix.ASSISTED_QUERY));
        encryptColumn.getLikeQuery()
                .ifPresent(optional -> consumer.accept(getColumnName(leftColumn, EncryptDerivedColumnSuffix.LIKE_QUERY, optional.getName()), EncryptDerivedColumnSuffix.LIKE_QUERY));
    }
    
    private ColumnSegment getAssignedColumn(final ColumnAssignmentSegment assignmentSegment) {
        return assignmentSegment.getColumns().get(0);
    }
    
    @FunctionalInterface
    private interface EncryptColumnConsumer {
        
        void accept(String targetColumnName, EncryptDerivedColumnSuffix derivedColumnSuffix);
    }
}
