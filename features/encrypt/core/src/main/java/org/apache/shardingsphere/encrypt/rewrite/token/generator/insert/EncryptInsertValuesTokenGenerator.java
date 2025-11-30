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
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptInsertValuesToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.expression.DerivedLiteralExpressionSegment;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.expression.DerivedParameterMarkerExpressionSegment;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
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
            encryptToken(insertValuesToken.getInsertValues().get(count), schemaName, encryptTable, insertStatementContext, each);
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
            encryptToken(insertValueToken, schemaName, encryptTable, insertStatementContext, each);
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
    
    private void encryptToken(final InsertValue insertValueToken, final String schemaName, final EncryptTable encryptTable,
                              final InsertStatementContext insertStatementContext, final InsertValueContext insertValueContext) {
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
            setCipherColumn(schemaName, tableName, encryptColumn, insertValueToken, valueExpression, columnIndex, originalValue);
            int indexDelta = 1;
            if (encryptColumn.getAssistedQuery().isPresent()) {
                addAssistedQueryColumn(schemaName, tableName, encryptColumn, insertValueToken, valueExpression, columnIndex, indexDelta, originalValue);
                indexDelta++;
            }
            if (encryptColumn.getLikeQuery().isPresent()) {
                addLikeQueryColumn(schemaName, tableName, encryptColumn, insertValueToken, valueExpression, columnIndex, indexDelta, originalValue);
            }
        }
    }
    
    private void setCipherColumn(final String schemaName, final String tableName, final EncryptColumn encryptColumn,
                                 final InsertValue insertValueToken, final ExpressionSegment valueExpression, final int columnIndex, final Object originalValue) {
        if (valueExpression instanceof LiteralExpressionSegment) {
            insertValueToken.getValues().set(columnIndex, new LiteralExpressionSegment(
                    valueExpression.getStartIndex(), valueExpression.getStopIndex(),
                    encryptColumn.getCipher().encrypt(database.getName(), schemaName, tableName, encryptColumn.getName(), originalValue)));
        }
    }
    
    private void addAssistedQueryColumn(final String schemaName, final String tableName, final EncryptColumn encryptColumn, final InsertValue insertValueToken,
                                        final ExpressionSegment valueExpression, final int columnIndex, final int indexDelta, final Object originalValue) {
        Optional<AssistedQueryColumnItem> assistedQueryColumnItem = encryptColumn.getAssistedQuery();
        Preconditions.checkState(assistedQueryColumnItem.isPresent());
        Object derivedValue = assistedQueryColumnItem.get().encrypt(database.getName(), schemaName, tableName, encryptColumn.getName(), originalValue);
        addDerivedColumn(insertValueToken, valueExpression, columnIndex, indexDelta, derivedValue, assistedQueryColumnItem.get().getName());
    }
    
    private void addLikeQueryColumn(final String schemaName, final String tableName, final EncryptColumn encryptColumn, final InsertValue insertValueToken,
                                    final ExpressionSegment valueExpression, final int columnIndex, final int indexDelta, final Object originalValue) {
        Optional<LikeQueryColumnItem> likeQueryColumnItem = encryptColumn.getLikeQuery();
        Preconditions.checkState(likeQueryColumnItem.isPresent());
        Object derivedValue = likeQueryColumnItem.get().encrypt(database.getName(), schemaName, tableName, encryptColumn.getName(), originalValue);
        addDerivedColumn(insertValueToken, valueExpression, columnIndex, indexDelta, derivedValue, likeQueryColumnItem.get().getName());
    }
    
    private void addDerivedColumn(final InsertValue insertValueToken, final ExpressionSegment valueExpression, final int columnIndex, final int indexDelta, final Object derivedValue,
                                  final String derivedColumnName) {
        ExpressionSegment derivedExpression;
        if (valueExpression instanceof LiteralExpressionSegment) {
            derivedExpression = new DerivedLiteralExpressionSegment(derivedValue);
        } else if (valueExpression instanceof ParameterMarkerExpressionSegment) {
            derivedExpression = new DerivedParameterMarkerExpressionSegment(getParameterIndexCount(insertValueToken));
        } else if (valueExpression instanceof ColumnSegment) {
            derivedExpression = createColumnSegment((ColumnSegment) valueExpression, derivedColumnName);
        } else {
            derivedExpression = valueExpression;
        }
        insertValueToken.getValues().add(columnIndex + indexDelta, derivedExpression);
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
    
    private int getParameterIndexCount(final InsertValue insertValueToken) {
        int result = 0;
        for (ExpressionSegment each : insertValueToken.getValues()) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                result++;
            }
        }
        return result;
    }
}
