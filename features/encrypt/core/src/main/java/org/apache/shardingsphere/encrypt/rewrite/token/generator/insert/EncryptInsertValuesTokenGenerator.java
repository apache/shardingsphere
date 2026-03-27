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
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptInsertAttachableColumnToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptInsertColumnToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptInsertSubstitutableColumnToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptInsertValuesToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.Attachable;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.Substitutable;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.InsertValue;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.InsertValuesToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.UseDefaultInsertColumnsToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Insert values token generator for encrypt.
 */
@RequiredArgsConstructor
@Setter
public final class EncryptInsertValuesTokenGenerator implements OptionalSQLTokenGenerator<InsertStatementContext>, PreviousSQLTokensAware {
    
    private final EncryptRule rule;
    
    private final ShardingSphereDatabase database;
    
    private List<SQLToken> previousSQLTokens;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext && !(((InsertStatementContext) sqlStatementContext).getSqlStatement()).getValues().isEmpty();
    }
    
    @Override
    public InsertValuesToken generateSQLToken(final InsertStatementContext insertStatementContext) {
        Optional<SQLToken> insertValuesToken = findPreviousSQLToken(InsertValuesToken.class);
        if (insertValuesToken.isPresent()) {
            processPreviousSQLToken(insertStatementContext, (InsertValuesToken) insertValuesToken.get());
            return (InsertValuesToken) insertValuesToken.get();
        }
        return generateNewSQLToken(insertStatementContext);
    }
    
    private Optional<SQLToken> findPreviousSQLToken(final Class<?> sqlToken) {
        for (SQLToken each : previousSQLTokens) {
            if (sqlToken.isAssignableFrom(each.getClass())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private void processPreviousSQLToken(final InsertStatementContext insertStatementContext, final InsertValuesToken insertValuesToken) {
        String tableName = insertStatementContext.getSqlStatement().getTable().map(optional -> optional.getTableName().getIdentifier().getValue()).orElse("");
        EncryptTable encryptTable = rule.getEncryptTable(tableName);
        int count = 0;
        String schemaName = insertStatementContext.getTablesContext().getSchemaName()
                .orElseGet(() -> new DatabaseTypeRegistry(insertStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(database.getName()));
        for (InsertValueContext each : insertStatementContext.getInsertValueContexts()) {
            applyInsertColumnTokens(insertValuesToken.getInsertValues().get(count), encryptToken(schemaName, encryptTable, insertStatementContext, each));
            count++;
        }
    }
    
    private InsertValuesToken generateNewSQLToken(final InsertStatementContext insertStatementContext) {
        String tableName = insertStatementContext.getSqlStatement().getTable().map(optional -> optional.getTableName().getIdentifier().getValue()).orElse("");
        Collection<InsertValuesSegment> insertValuesSegments = insertStatementContext.getSqlStatement().getValues();
        InsertValuesToken result = new EncryptInsertValuesToken(getStartIndex(insertValuesSegments), getStopIndex(insertValuesSegments));
        EncryptTable encryptTable = rule.getEncryptTable(tableName);
        String schemaName = insertStatementContext.getTablesContext().getSchemaName()
                .orElseGet(() -> new DatabaseTypeRegistry(insertStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(database.getName()));
        for (InsertValueContext each : insertStatementContext.getInsertValueContexts()) {
            InsertValue insertValueToken = new InsertValue(new LinkedList<>(each.getValueExpressions()));
            applyInsertColumnTokens(insertValueToken, encryptToken(schemaName, encryptTable, insertStatementContext, each));
            result.getInsertValues().add(insertValueToken);
        }
        return result;
    }
    
    private int getStartIndex(final Collection<InsertValuesSegment> segments) {
        int result = segments.iterator().next().getStartIndex();
        for (InsertValuesSegment each : segments) {
            result = Math.min(result, each.getStartIndex());
        }
        return result;
    }
    
    private int getStopIndex(final Collection<InsertValuesSegment> segments) {
        int result = segments.iterator().next().getStopIndex();
        for (InsertValuesSegment each : segments) {
            result = Math.max(result, each.getStopIndex());
        }
        return result;
    }
    
    private Collection<EncryptInsertColumnToken> encryptToken(final String schemaName, final EncryptTable encryptTable,
                                                              final InsertStatementContext insertStatementContext, final InsertValueContext insertValueContext) {
        Collection<EncryptInsertColumnToken> result = new LinkedList<>();
        String tableName = encryptTable.getTable();
        Optional<SQLToken> useDefaultInsertColumnsToken = findPreviousSQLToken(UseDefaultInsertColumnsToken.class);
        Iterator<String> descendingColumnNames = insertStatementContext.getDescendingColumnNames();
        while (descendingColumnNames.hasNext()) {
            String columnName = descendingColumnNames.next();
            if (!encryptTable.isEncryptColumn(columnName)) {
                continue;
            }
            EncryptColumn encryptColumn = rule.getEncryptTable(tableName).getEncryptColumn(columnName);
            int columnIndex = useDefaultInsertColumnsToken
                    .map(optional -> ((UseDefaultInsertColumnsToken) optional).getColumns().indexOf(columnName)).orElseGet(() -> insertStatementContext.getColumnNames().indexOf(columnName));
            Object originalValue = insertValueContext.getLiteralValue(columnIndex).orElse(null);
            ExpressionSegment valueExpression = insertValueContext.getValueExpressions().get(columnIndex);
            int parameterIndexCount = getParameterIndexCount(insertValueContext.getValueExpressions());
            generateCipherColumnToken(schemaName, tableName, encryptColumn, valueExpression, columnIndex, originalValue).ifPresent(result::add);
            if (encryptColumn.getAssistedQuery().isPresent()) {
                addAssistedQueryColumn(schemaName, tableName, encryptColumn, valueExpression, columnIndex, originalValue, parameterIndexCount).ifPresent(result::add);
            }
            if (encryptColumn.getLikeQuery().isPresent()) {
                addLikeQueryColumn(schemaName, tableName, encryptColumn, valueExpression, columnIndex, originalValue, parameterIndexCount).ifPresent(result::add);
            }
        }
        return result;
    }
    
    private void applyInsertColumnTokens(final InsertValue insertValueToken, final Collection<EncryptInsertColumnToken> insertColumnTokens) {
        for (EncryptInsertColumnToken each : insertColumnTokens) {
            if (each instanceof Substitutable) {
                insertValueToken.putSubstitutedSQLToken(each.getColumnIndex(), each);
                continue;
            }
            if (each instanceof Attachable) {
                insertValueToken.addAddedSQLToken(each.getColumnIndex(), each);
            }
        }
    }
    
    private Optional<EncryptInsertColumnToken> generateCipherColumnToken(final String schemaName, final String tableName, final EncryptColumn encryptColumn,
                                                                         final ExpressionSegment valueExpression, final int columnIndex, final Object originalValue) {
        if (valueExpression instanceof LiteralExpressionSegment) {
            return Optional.of(new EncryptInsertSubstitutableColumnToken(columnIndex, new LiteralExpressionSegment(
                    valueExpression.getStartIndex(), valueExpression.getStopIndex(),
                    encryptColumn.getCipher().encrypt(database.getName(), schemaName, tableName, encryptColumn.getName(), originalValue))));
        }
        if (valueExpression instanceof ColumnSegment) {
            return Optional.of(new EncryptInsertSubstitutableColumnToken(columnIndex, createColumnSegment((ColumnSegment) valueExpression, encryptColumn.getCipher().getName())));
        }
        return Optional.empty();
    }
    
    private Optional<EncryptInsertColumnToken> addAssistedQueryColumn(final String schemaName, final String tableName, final EncryptColumn encryptColumn,
                                                                      final ExpressionSegment valueExpression, final int columnIndex, final Object originalValue,
                                                                      final int parameterIndexCount) {
        Optional<AssistedQueryColumnItem> assistedQueryColumnItem = encryptColumn.getAssistedQuery();
        Preconditions.checkState(assistedQueryColumnItem.isPresent());
        Object derivedValue = assistedQueryColumnItem.get().encrypt(database.getName(), schemaName, tableName, encryptColumn.getName(), originalValue);
        return addDerivedColumn(valueExpression, columnIndex, derivedValue, assistedQueryColumnItem.get().getName(), parameterIndexCount);
    }
    
    private Optional<EncryptInsertColumnToken> addLikeQueryColumn(final String schemaName, final String tableName, final EncryptColumn encryptColumn,
                                                                  final ExpressionSegment valueExpression, final int columnIndex, final Object originalValue,
                                                                  final int parameterIndexCount) {
        Optional<LikeQueryColumnItem> likeQueryColumnItem = encryptColumn.getLikeQuery();
        Preconditions.checkState(likeQueryColumnItem.isPresent());
        Object derivedValue = likeQueryColumnItem.get().encrypt(database.getName(), schemaName, tableName, encryptColumn.getName(), originalValue);
        return addDerivedColumn(valueExpression, columnIndex, derivedValue, likeQueryColumnItem.get().getName(), parameterIndexCount);
    }
    
    private Optional<EncryptInsertColumnToken> addDerivedColumn(final ExpressionSegment valueExpression, final int columnIndex, final Object derivedValue,
                                                                final String derivedColumnName, final int parameterIndexCount) {
        if (valueExpression instanceof LiteralExpressionSegment) {
            return Optional.of(new EncryptInsertAttachableColumnToken(columnIndex, new LiteralExpressionSegment(0, 0, derivedValue)));
        }
        if (valueExpression instanceof ParameterMarkerExpressionSegment) {
            return Optional.of(new EncryptInsertAttachableColumnToken(columnIndex, new ParameterMarkerExpressionSegment(0, 0, parameterIndexCount)));
        }
        if (valueExpression instanceof ColumnSegment) {
            return Optional.of(new EncryptInsertAttachableColumnToken(columnIndex, createColumnSegment((ColumnSegment) valueExpression, derivedColumnName)));
        }
        return Optional.of(new EncryptInsertAttachableColumnToken(columnIndex, valueExpression));
    }
    
    private ColumnSegment createColumnSegment(final ColumnSegment originalColumn, final String columnName) {
        ColumnSegment result = new ColumnSegment(originalColumn.getStartIndex(), originalColumn.getStopIndex(), new IdentifierValue(columnName, originalColumn.getIdentifier().getQuoteCharacter()));
        result.setNestedObjectAttributes(originalColumn.getNestedObjectAttributes());
        originalColumn.getOwner().ifPresent(result::setOwner);
        result.setColumnBoundInfo(originalColumn.getColumnBoundInfo());
        result.setOtherUsingColumnBoundInfo(originalColumn.getOtherUsingColumnBoundInfo());
        result.setVariable(originalColumn.isVariable());
        return result;
    }
    
    private int getParameterIndexCount(final Collection<ExpressionSegment> expressionSegments) {
        int result = 0;
        for (ExpressionSegment each : expressionSegments) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                result++;
            }
        }
        return result;
    }
}
