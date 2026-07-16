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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.encrypt.enums.EncryptDerivedColumnSuffix;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Assignment generator for encrypt.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public final class EncryptAssignmentTokenGenerator {
    
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
        return generateSQLTokens(tablesContext, setAssignmentSegment, Optional.empty());
    }
    
    /**
     * Generate SQL tokens.
     *
     * @param tablesContext SQL statement context
     * @param setAssignmentSegment set assignment segment
     * @param targetTable target table segment
     * @return generated SQL tokens
     */
    public Collection<SQLToken> generateSQLTokens(final TablesContext tablesContext, final SetAssignmentSegment setAssignmentSegment, final TableSegment targetTable) {
        return generateSQLTokens(tablesContext, setAssignmentSegment, Optional.of(targetTable));
    }
    
    private Collection<SQLToken> generateSQLTokens(final TablesContext tablesContext, final SetAssignmentSegment setAssignmentSegment, final Optional<TableSegment> targetTable) {
        Collection<SQLToken> result = new LinkedList<>();
        for (ColumnAssignmentSegment each : setAssignmentSegment.getAssignments()) {
            ColumnSegment assignedColumn = getAssignedColumn(each);
            Optional<OpenQueryContext> openQueryContext = findOpenQueryContext(targetTable, assignedColumn.getIdentifier().getValue());
            findEncryptTable(assignedColumn, openQueryContext)
                    .ifPresent(encryptTable -> appendEncryptAssignmentTokens(result, tablesContext, targetTable, each, assignedColumn, openQueryContext, encryptTable));
        }
        return result;
    }
    
    private void appendEncryptAssignmentTokens(final Collection<SQLToken> result, final TablesContext tablesContext, final Optional<TableSegment> targetTable,
                                               final ColumnAssignmentSegment assignmentSegment, final ColumnSegment assignedColumn,
                                               final Optional<OpenQueryContext> openQueryContext, final EncryptTable encryptTable) {
        String columnName = assignedColumn.getIdentifier().getValue();
        if (!encryptTable.isEncryptColumn(columnName)) {
            return;
        }
        EncryptColumn encryptColumn = encryptTable.getEncryptColumn(columnName);
        DatabaseTypeRegistry databaseTypeRegistry = new DatabaseTypeRegistry(databaseType);
        String schemaName = targetTable.flatMap(EncryptOpenQueryUtils::findSchemaName)
                .orElseGet(() -> tablesContext.getSchemaName().orElseGet(() -> databaseTypeRegistry.getDefaultSchemaName(database.getName())));
        QuoteCharacter quoteCharacter = databaseTypeRegistry.getDialectDatabaseMetaData().getQuoteCharacter();
        result.addAll(generateAssignmentSQLTokens(schemaName, encryptTable.getTable(), encryptColumn, assignmentSegment, quoteCharacter, openQueryContext.isPresent()));
        openQueryContext.ifPresent(optional -> result.add(generateOpenQuerySQLToken(optional, encryptColumn)));
    }
    
    private Optional<EncryptTable> findEncryptTable(final ColumnSegment assignedColumn, final Optional<OpenQueryContext> openQueryContext) {
        Optional<EncryptTable> result = rule.findEncryptTable(assignedColumn.getColumnBoundInfo().getOriginalTable().getValue());
        return result.isPresent() ? result : openQueryContext.map(OpenQueryContext::getEncryptTable);
    }
    
    private Optional<OpenQueryContext> findOpenQueryContext(final Optional<TableSegment> targetTable, final String columnName) {
        if (!targetTable.isPresent()) {
            return Optional.empty();
        }
        Optional<LiteralExpressionSegment> openQuerySQL = EncryptOpenQueryUtils.findOpenQuerySQLLiteral(targetTable.get());
        Optional<EncryptTable> encryptTable = EncryptOpenQueryUtils.findEncryptTable(rule, targetTable.get()).filter(optional -> optional.isEncryptColumn(columnName));
        return openQuerySQL.isPresent() && encryptTable.isPresent() ? Optional.of(new OpenQueryContext(openQuerySQL.get(), encryptTable.get())) : Optional.empty();
    }
    
    private EncryptOpenQuerySQLToken generateOpenQuerySQLToken(final OpenQueryContext openQueryContext, final EncryptColumn encryptColumn) {
        String openQuerySQL = openQueryContext.getOpenQuerySQL().getText();
        String rewrittenSQL = Pattern.compile(String.join("", "\\b", Pattern.quote(encryptColumn.getName()), "\\b"), Pattern.CASE_INSENSITIVE).matcher(openQuerySQL)
                .replaceAll(Matcher.quoteReplacement(encryptColumn.getCipher().getName()));
        return new EncryptOpenQuerySQLToken(openQueryContext.getOpenQuerySQL().getStartIndex(), openQueryContext.getOpenQuerySQL().getStopIndex(), rewrittenSQL);
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
    
    @Getter
    @RequiredArgsConstructor
    private static final class OpenQueryContext {
        
        private final LiteralExpressionSegment openQuerySQL;
        
        private final EncryptTable encryptTable;
    }
}
