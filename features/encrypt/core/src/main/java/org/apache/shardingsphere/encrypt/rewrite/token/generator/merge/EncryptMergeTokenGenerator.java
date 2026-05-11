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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.merge;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAttachableTextToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptSubstitutableTextToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.InsertColumnsToken;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.merge.MergeWhenAndThenSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Merge token generator for encrypt.
 */
@RequiredArgsConstructor
public final class EncryptMergeTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext> {
    
    private final EncryptRule rule;
    
    private final ShardingSphereDatabase database;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof MergeStatement;
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        MergeStatement mergeStatement = (MergeStatement) sqlStatementContext.getSqlStatement();
        Optional<SimpleTableSegment> targetTableSegment = getTargetTableSegment(mergeStatement);
        if (!targetTableSegment.isPresent()) {
            return new LinkedList<>();
        }
        String tableName = targetTableSegment.get().getTableName().getIdentifier().getValue();
        Optional<EncryptTable> encryptTable = rule.findEncryptTable(tableName);
        if (!encryptTable.isPresent()) {
            return new LinkedList<>();
        }
        Collection<SQLToken> result = new LinkedList<>();
        String schemaName = sqlStatementContext.getTablesContext().getSchemaName()
                .orElseGet(() -> new DatabaseTypeRegistry(mergeStatement.getDatabaseType()).getDefaultSchemaName(database.getName()));
        Map<String, ExpressionProjectionSegment> sourceProjections = getSourceProjections(mergeStatement);
        result.addAll(generateSourceProjectionTokens(schemaName, encryptTable.get(), sourceProjections));
        result.addAll(generatePredicateColumnTokens(encryptTable.get(), mergeStatement, getTargetAlias(targetTableSegment.get())));
        result.addAll(generateInsertTokens(encryptTable.get(), mergeStatement, sourceProjections.keySet()));
        return result;
    }
    
    private Optional<SimpleTableSegment> getTargetTableSegment(final MergeStatement mergeStatement) {
        return mergeStatement.getTarget() instanceof SimpleTableSegment ? Optional.of((SimpleTableSegment) mergeStatement.getTarget()) : Optional.empty();
    }
    
    private String getTargetAlias(final SimpleTableSegment targetTableSegment) {
        return targetTableSegment.getAliasName().orElseGet(() -> targetTableSegment.getTableName().getIdentifier().getValue());
    }
    
    private Map<String, ExpressionProjectionSegment> getSourceProjections(final MergeStatement mergeStatement) {
        if (!(mergeStatement.getSource() instanceof SubqueryTableSegment)) {
            return Collections.emptyMap();
        }
        SubquerySegment subquerySegment = ((SubqueryTableSegment) mergeStatement.getSource()).getSubquery();
        if (null == subquerySegment.getSelect()) {
            return Collections.emptyMap();
        }
        SelectStatement selectStatement = subquerySegment.getSelect();
        return selectStatement.getProjections().getProjections().stream()
                .filter(ExpressionProjectionSegment.class::isInstance)
                .map(ExpressionProjectionSegment.class::cast)
                .collect(Collectors.toMap(ProjectionSegment::getColumnLabel, each -> each, (oldValue, currentValue) -> oldValue));
    }
    
    private Collection<SQLToken> generateSourceProjectionTokens(final String schemaName, final EncryptTable encryptTable,
                                                                final Map<String, ExpressionProjectionSegment> sourceProjections) {
        Collection<SQLToken> result = new LinkedList<>();
        for (Entry<String, ExpressionProjectionSegment> entry : sourceProjections.entrySet()) {
            if (!encryptTable.isEncryptColumn(entry.getKey())) {
                continue;
            }
            EncryptColumn encryptColumn = encryptTable.getEncryptColumn(entry.getKey());
            ExpressionProjectionSegment projectionSegment = entry.getValue();
            if (null == projectionSegment || !(projectionSegment.getExpr() instanceof LiteralExpressionSegment)) {
                continue;
            }
            LiteralExpressionSegment literalExpressionSegment = (LiteralExpressionSegment) projectionSegment.getExpr();
            Object encryptedValue = encryptColumn.getCipher()
                    .encrypt(database.getName(), schemaName, encryptTable.getTable(), encryptColumn.getName(), literalExpressionSegment.getLiterals());
            result.add(new EncryptSubstitutableTextToken(literalExpressionSegment.getStartIndex(), literalExpressionSegment.getStopIndex(), formatLiteral(encryptedValue)));
        }
        return result;
    }
    
    private Collection<SQLToken> generatePredicateColumnTokens(final EncryptTable encryptTable, final MergeStatement mergeStatement, final String targetAlias) {
        Collection<SQLToken> result = new LinkedList<>();
        if (null == mergeStatement.getExpression()) {
            return result;
        }
        QuoteCharacter quoteCharacter = new DatabaseTypeRegistry(mergeStatement.getDatabaseType()).getDialectDatabaseMetaData().getQuoteCharacter();
        for (ColumnSegment each : ColumnExtractor.extract(mergeStatement.getExpression().getExpr())) {
            if (!isTargetEncryptColumn(encryptTable, targetAlias, each)) {
                continue;
            }
            String cipherColumn = quoteCharacter.wrap(encryptTable.getEncryptColumn(each.getIdentifier().getValue()).getCipher().getName());
            result.add(new EncryptSubstitutableTextToken(each.getStartIndex(), each.getStopIndex(), getColumnExpression(each, cipherColumn)));
        }
        return result;
    }
    
    private boolean isTargetEncryptColumn(final EncryptTable encryptTable, final String targetAlias, final ColumnSegment columnSegment) {
        return columnSegment.getOwner().map(optional -> targetAlias.equalsIgnoreCase(optional.getIdentifier().getValue()))
                .orElse(false)
                && encryptTable.isEncryptColumn(columnSegment.getIdentifier().getValue());
    }
    
    private String getColumnExpression(final ColumnSegment columnSegment, final String cipherColumn) {
        return columnSegment.getOwner().map(optional -> optional.getIdentifier().getValue() + "." + cipherColumn)
                .orElse(cipherColumn);
    }
    
    private Collection<SQLToken> generateInsertTokens(final EncryptTable encryptTable, final MergeStatement mergeStatement, final Collection<String> sourceColumnNames) {
        Collection<SQLToken> result = new LinkedList<>();
        for (MergeWhenAndThenSegment each : mergeStatement.getWhenAndThens()) {
            if (null != each.getInsert()) {
                result.addAll(generateInsertTokens(encryptTable, each.getInsert(), sourceColumnNames));
            }
        }
        return result;
    }
    
    private Collection<SQLToken> generateInsertTokens(final EncryptTable encryptTable, final InsertStatement insertStatement, final Collection<String> sourceColumnNames) {
        Collection<SQLToken> result = new LinkedList<>();
        if (!insertStatement.getInsertColumns().isPresent() || insertStatement.getColumns().isEmpty() || insertStatement.getValues().isEmpty()) {
            return result;
        }
        Collection<EncryptColumn> insertEncryptColumns = sourceColumnNames.stream()
                .filter(encryptTable::isEncryptColumn)
                .map(encryptTable::getEncryptColumn)
                .filter(each -> !containsColumn(insertStatement.getColumns(), each.getName()))
                .filter(each -> !containsColumn(insertStatement.getColumns(), each.getCipher().getName()))
                .collect(Collectors.toList());
        if (insertEncryptColumns.isEmpty()) {
            return result;
        }
        ColumnSegment lastColumnSegment = getLastColumn(insertStatement.getInsertColumns().get());
        QuoteCharacter quoteCharacter = new DatabaseTypeRegistry(insertStatement.getDatabaseType()).getDialectDatabaseMetaData().getQuoteCharacter();
        result.add(new InsertColumnsToken(lastColumnSegment.getStopIndex() + 1, getCipherColumns(insertEncryptColumns), quoteCharacter));
        for (InsertValuesSegment each : insertStatement.getValues()) {
            result.add(new EncryptAttachableTextToken(getLastValue(each).getStopIndex() + 1, getInsertValues(insertEncryptColumns)));
        }
        return result;
    }
    
    private boolean containsColumn(final Collection<ColumnSegment> columns, final String columnName) {
        return columns.stream()
                .anyMatch(each -> columnName.equalsIgnoreCase(each.getIdentifier().getValue()));
    }
    
    private ColumnSegment getLastColumn(final InsertColumnsSegment insertColumnsSegment) {
        return insertColumnsSegment.getColumns().stream()
                .reduce((previous, current) -> current)
                .orElseThrow(IllegalStateException::new);
    }
    
    private List<String> getCipherColumns(final Collection<EncryptColumn> insertEncryptColumns) {
        return insertEncryptColumns.stream()
                .map(each -> each.getCipher().getName())
                .collect(Collectors.toList());
    }
    
    private ExpressionSegment getLastValue(final InsertValuesSegment insertValuesSegment) {
        return insertValuesSegment.getValues().stream()
                .reduce((previous, current) -> current)
                .orElseThrow(IllegalStateException::new);
    }
    
    private String getInsertValues(final Collection<EncryptColumn> insertEncryptColumns) {
        return insertEncryptColumns.stream()
                .map(each -> "src." + each.getName())
                .collect(Collectors.joining(", ", ", ", ""));
    }
    
    private String formatLiteral(final Object literal) {
        if (null == literal) {
            return "NULL";
        }
        if (literal instanceof String) {
            return "'" + literal + "'";
        }
        return String.valueOf(literal);
    }
}
