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
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.encrypt.enums.EncryptDerivedColumnSuffix;
import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptLiteralAssignmentToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptOpenQuerySQLToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptParameterAssignmentToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Assignment generator for encrypt.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public final class EncryptAssignmentTokenGenerator {
    
    private static final String UNSUPPORTED_ASSIGNMENT_EXPRESSION = "OPENQUERY with unsupported assignment expression";
    
    private final EncryptRule rule;
    
    private final ShardingSphereDatabase database;
    
    private final DatabaseType databaseType;
    
    /**
     * Generate SQL tokens.
     *
     * @param tablesContext SQL statement context
     * @param setAssignmentSegment set assignment segment
     * @return generated SQL tokens
     */
    public Collection<SQLToken> generateSQLTokens(final TablesContext tablesContext, final SetAssignmentSegment setAssignmentSegment) {
        return generateNormalUpdateTokens(tablesContext, setAssignmentSegment);
    }
    
    /**
     * Generate SQL tokens.
     *
     * @param tablesContext SQL statement context
     * @param setAssignmentSegment set assignment segment
     * @param openQueryTable OPENQUERY function table segment
     * @return generated SQL tokens
     */
    Collection<SQLToken> generateSQLTokens(final TablesContext tablesContext, final SetAssignmentSegment setAssignmentSegment, final TableSegment openQueryTable) {
        return generateOpenQueryUpdateTokens(tablesContext, setAssignmentSegment, openQueryTable);
    }
    
    private Collection<SQLToken> generateNormalUpdateTokens(final TablesContext tablesContext, final SetAssignmentSegment setAssignmentSegment) {
        Collection<SQLToken> result = new LinkedList<>();
        for (ColumnAssignmentSegment each : setAssignmentSegment.getAssignments()) {
            ColumnSegment assignedColumn = getAssignedColumn(each);
            String columnName = assignedColumn.getIdentifier().getValue();
            Optional<EncryptTable> encryptTable = rule.findEncryptTable(assignedColumn.getColumnBoundInfo().getOriginalTable().getValue());
            if (!encryptTable.isPresent() || !encryptTable.get().isEncryptColumn(columnName)) {
                continue;
            }
            EncryptColumn encryptColumn = encryptTable.get().getEncryptColumn(columnName);
            appendNormalAssignmentTokens(result, tablesContext, each, encryptTable.get(), encryptColumn);
        }
        return result;
    }
    
    private Collection<SQLToken> generateOpenQueryUpdateTokens(final TablesContext tablesContext, final SetAssignmentSegment setAssignmentSegment, final TableSegment openQueryTable) {
        Optional<EncryptTable> encryptTable = findOpenQueryEncryptTable(openQueryTable);
        if (!encryptTable.isPresent()) {
            return Collections.emptyList();
        }
        EncryptTable table = encryptTable.get();
        Collection<SQLToken> result = new LinkedList<>();
        boolean hasEncryptAssignment = false;
        for (ColumnAssignmentSegment each : setAssignmentSegment.getAssignments()) {
            String columnName = getAssignedColumn(each).getIdentifier().getValue();
            if (!table.isEncryptColumn(columnName)) {
                continue;
            }
            appendOpenQueryAssignmentTokens(result, tablesContext, openQueryTable, each, table, table.getEncryptColumn(columnName));
            hasEncryptAssignment = true;
        }
        if (hasEncryptAssignment) {
            appendComposedOpenQuerySQLToken(result, openQueryTable, table.getEncryptColumns());
        }
        return result;
    }
    
    private void appendComposedOpenQuerySQLToken(final Collection<SQLToken> result, final TableSegment openQueryTable, final Collection<EncryptColumn> encryptColumns) {
        Optional<LiteralExpressionSegment> openQuerySQL = EncryptOpenQueryUtils.findOpenQuerySQLLiteral(openQueryTable);
        if (!openQuerySQL.isPresent()) {
            return;
        }
        result.add(generateOpenQuerySQLToken(openQuerySQL.get(), encryptColumns));
    }
    
    private void appendNormalAssignmentTokens(final Collection<SQLToken> result, final TablesContext tablesContext,
                                              final ColumnAssignmentSegment assignmentSegment, final EncryptTable encryptTable, final EncryptColumn encryptColumn) {
        DatabaseTypeRegistry databaseTypeRegistry = new DatabaseTypeRegistry(databaseType);
        String schemaName = tablesContext.getSchemaName().orElseGet(() -> databaseTypeRegistry.getDefaultSchemaName(database.getName()));
        QuoteCharacter quoteCharacter = databaseTypeRegistry.getDialectDatabaseMetaData().getQuoteCharacter();
        result.addAll(generateAssignmentSQLTokens(schemaName, encryptTable.getTable(), encryptColumn, assignmentSegment, quoteCharacter, false));
    }
    
    private void appendOpenQueryAssignmentTokens(final Collection<SQLToken> result, final TablesContext tablesContext, final TableSegment openQueryTable,
                                                 final ColumnAssignmentSegment assignmentSegment, final EncryptTable encryptTable, final EncryptColumn encryptColumn) {
        DatabaseTypeRegistry databaseTypeRegistry = new DatabaseTypeRegistry(databaseType);
        String schemaName = EncryptOpenQueryUtils.findSchemaName(openQueryTable)
                .orElseGet(() -> tablesContext.getSchemaName().orElseGet(() -> databaseTypeRegistry.getDefaultSchemaName(database.getName())));
        QuoteCharacter quoteCharacter = databaseTypeRegistry.getDialectDatabaseMetaData().getQuoteCharacter();
        Collection<SQLToken> assignmentTokens = generateAssignmentSQLTokens(schemaName, encryptTable.getTable(), encryptColumn, assignmentSegment, quoteCharacter, true);
        if (assignmentTokens.isEmpty()) {
            throw new UnsupportedEncryptSQLException(UNSUPPORTED_ASSIGNMENT_EXPRESSION);
        }
        result.addAll(assignmentTokens);
    }
    
    private Optional<EncryptTable> findOpenQueryEncryptTable(final TableSegment openQueryTable) {
        return EncryptOpenQueryUtils.findEncryptTable(rule, openQueryTable);
    }
    
    private EncryptOpenQuerySQLToken generateOpenQuerySQLToken(final LiteralExpressionSegment openQuerySQL, final Collection<EncryptColumn> encryptColumns) {
        String rewrittenSQL = EncryptOpenQueryPassThroughSQL.parse(openQuerySQL.getText()).rewrite(encryptColumns);
        return new EncryptOpenQuerySQLToken(openQuerySQL.getStartIndex(), openQuerySQL.getStopIndex(), rewrittenSQL);
    }
    
    private Collection<SQLToken> generateAssignmentSQLTokens(final String schemaName, final String tableName, final EncryptColumn encryptColumn,
                                                             final ColumnAssignmentSegment segment, final QuoteCharacter quoteCharacter, final boolean useActualColumnName) {
        ExpressionSegment value = segment.getValue();
        if (value instanceof ParameterMarkerExpressionSegment) {
            return Collections.singleton(generateParameterSQLToken(encryptColumn, segment, quoteCharacter, useActualColumnName));
        }
        if (value instanceof LiteralExpressionSegment) {
            return Collections.singleton(generateLiteralSQLToken(schemaName, tableName, encryptColumn, segment, quoteCharacter, useActualColumnName));
        }
        return Collections.emptyList();
    }
    
    private EncryptAssignmentToken generateParameterSQLToken(final EncryptColumn encryptColumn, final ColumnAssignmentSegment segment, final QuoteCharacter quoteCharacter,
                                                             final boolean useActualColumnName) {
        ColumnSegment leftColumn = getAssignedColumn(segment);
        EncryptParameterAssignmentToken result = new EncryptParameterAssignmentToken(leftColumn.getStartIndex(), segment.getStopIndex(), quoteCharacter);
        appendEncryptColumnTokens(leftColumn, encryptColumn, useActualColumnName, (targetName, suffix) -> result.addColumnName(targetName));
        return result;
    }
    
    private String getColumnName(final ColumnSegment columnSegment, final EncryptDerivedColumnSuffix derivedColumnSuffix, final String actualColumnName, final boolean useActualColumnName) {
        return !useActualColumnName && TableSourceType.TEMPORARY_TABLE == columnSegment.getColumnBoundInfo().getTableSourceType()
                ? derivedColumnSuffix.getDerivedColumnName(columnSegment.getIdentifier().getValue(), databaseType)
                : actualColumnName;
    }
    
    private EncryptAssignmentToken generateLiteralSQLToken(final String schemaName, final String tableName, final EncryptColumn encryptColumn, final ColumnAssignmentSegment segment,
                                                           final QuoteCharacter quoteCharacter, final boolean useActualColumnName) {
        ColumnSegment leftColumn = getAssignedColumn(segment);
        EncryptLiteralAssignmentToken result = new EncryptLiteralAssignmentToken(leftColumn.getStartIndex(), segment.getStopIndex(), quoteCharacter);
        Object literalValue = ((LiteralExpressionSegment) segment.getValue()).getLiterals();
        appendEncryptColumnTokens(leftColumn, encryptColumn, useActualColumnName,
                (targetName, suffix) -> addLiteralSQLToken(schemaName, tableName, encryptColumn, targetName, suffix, result, literalValue));
        return result;
    }
    
    private void addLiteralSQLToken(final String schemaName, final String tableName, final EncryptColumn encryptColumn, final String targetColumnName, final EncryptDerivedColumnSuffix suffix,
                                    final EncryptLiteralAssignmentToken result, final Object literalValue) {
        if (null == suffix) {
            result.addAssignment(targetColumnName, literalValue);
        } else {
            Object encryptValue = encrypt(encryptColumn, suffix, database.getName(), schemaName, tableName, encryptColumn.getName(), literalValue);
            result.addAssignment(targetColumnName, encryptValue);
        }
    }
    
    private Object encrypt(final EncryptColumn encryptColumn, final EncryptDerivedColumnSuffix suffix, final String databaseName, final String schemaName,
                           final String tableName, final String logicColumnName, final Object originalValue) {
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
    
    private void appendEncryptColumnTokens(final ColumnSegment leftColumn, final EncryptColumn encryptColumn, final boolean useActualColumnName, final EncryptColumnConsumer consumer) {
        consumer.accept(getColumnName(leftColumn, EncryptDerivedColumnSuffix.CIPHER, encryptColumn.getCipher().getName(), useActualColumnName), EncryptDerivedColumnSuffix.CIPHER);
        encryptColumn.getAssistedQuery()
                .ifPresent(optional -> consumer.accept(getColumnName(leftColumn, EncryptDerivedColumnSuffix.ASSISTED_QUERY, optional.getName(), useActualColumnName),
                        EncryptDerivedColumnSuffix.ASSISTED_QUERY));
        encryptColumn.getLikeQuery()
                .ifPresent(optional -> consumer.accept(getColumnName(leftColumn, EncryptDerivedColumnSuffix.LIKE_QUERY, optional.getName(), useActualColumnName),
                        EncryptDerivedColumnSuffix.LIKE_QUERY));
    }
    
    private ColumnSegment getAssignedColumn(final ColumnAssignmentSegment assignmentSegment) {
        return assignmentSegment.getColumns().get(0);
    }
    
    @FunctionalInterface
    private interface EncryptColumnConsumer {
        
        void accept(String targetColumnName, EncryptDerivedColumnSuffix derivedColumnSuffix);
    }
}
