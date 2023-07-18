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
import org.apache.shardingsphere.encrypt.rewrite.aware.DatabaseNameAware;
import org.apache.shardingsphere.encrypt.rewrite.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptInsertValuesToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.expression.DerivedLiteralExpressionSegment;
import org.apache.shardingsphere.infra.binder.segment.insert.values.expression.DerivedParameterMarkerExpressionSegment;
import org.apache.shardingsphere.infra.binder.segment.insert.values.expression.DerivedSimpleExpressionSegment;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.InsertValue;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.InsertValuesToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.UseDefaultInsertColumnsToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Insert values token generator for encrypt.
 */
@Setter
public final class EncryptInsertValuesTokenGenerator implements OptionalSQLTokenGenerator<InsertStatementContext>, PreviousSQLTokensAware, EncryptRuleAware, DatabaseNameAware {
    
    private List<SQLToken> previousSQLTokens;
    
    private EncryptRule encryptRule;
    
    private String databaseName;
    
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
        String tableName = insertStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        EncryptTable encryptTable = encryptRule.getEncryptTable(tableName);
        int count = 0;
        String schemaName = insertStatementContext.getTablesContext().getSchemaName().orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(insertStatementContext.getDatabaseType(), databaseName));
        for (InsertValueContext each : insertStatementContext.getInsertValueContexts()) {
            encryptToken(insertValuesToken.getInsertValues().get(count), schemaName, encryptTable, insertStatementContext, each);
            count++;
        }
    }
    
    private InsertValuesToken generateNewSQLToken(final InsertStatementContext insertStatementContext) {
        String tableName = insertStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        Collection<InsertValuesSegment> insertValuesSegments = insertStatementContext.getSqlStatement().getValues();
        InsertValuesToken result = new EncryptInsertValuesToken(getStartIndex(insertValuesSegments), getStopIndex(insertValuesSegments));
        EncryptTable encryptTable = encryptRule.getEncryptTable(tableName);
        String schemaName = insertStatementContext.getTablesContext().getSchemaName().orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(insertStatementContext.getDatabaseType(), databaseName));
        for (InsertValueContext each : insertStatementContext.getInsertValueContexts()) {
            InsertValue insertValueToken = new InsertValue(each.getValueExpressions());
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
            EncryptColumn encryptColumn = encryptRule.getEncryptTable(tableName).getEncryptColumn(columnName);
            int columnIndex = useDefaultInsertColumnsToken
                    .map(optional -> ((UseDefaultInsertColumnsToken) optional).getColumns().indexOf(columnName)).orElseGet(() -> insertStatementContext.getColumnNames().indexOf(columnName));
            Object originalValue = insertValueContext.getLiteralValue(columnIndex).orElse(null);
            setCipherColumn(schemaName, tableName, encryptColumn, insertValueToken, insertValueContext.getValueExpressions().get(columnIndex), columnIndex, originalValue);
            int indexDelta = 1;
            if (encryptColumn.getAssistedQuery().isPresent()) {
                addAssistedQueryColumn(schemaName, tableName, encryptColumn, insertValueContext, insertValueToken, columnIndex, indexDelta, originalValue);
                indexDelta++;
            }
            if (encryptColumn.getLikeQuery().isPresent()) {
                addLikeQueryColumn(schemaName, tableName, encryptColumn, insertValueContext, insertValueToken, columnIndex, indexDelta, originalValue);
            }
        }
    }
    
    private void setCipherColumn(final String schemaName, final String tableName, final EncryptColumn encryptColumn,
                                 final InsertValue insertValueToken, final ExpressionSegment valueExpression, final int columnIndex, final Object originalValue) {
        if (valueExpression instanceof LiteralExpressionSegment) {
            insertValueToken.getValues().set(columnIndex, new LiteralExpressionSegment(
                    valueExpression.getStartIndex(), valueExpression.getStopIndex(), encryptColumn.getCipher().encrypt(databaseName, schemaName, tableName, encryptColumn.getName(), originalValue)));
        }
    }
    
    private void addAssistedQueryColumn(final String schemaName, final String tableName, final EncryptColumn encryptColumn,
                                        final InsertValueContext insertValueContext, final InsertValue insertValueToken, final int columnIndex, final int indexDelta, final Object originalValue) {
        Optional<AssistedQueryColumnItem> assistedQueryColumnItem = encryptColumn.getAssistedQuery();
        Preconditions.checkState(assistedQueryColumnItem.isPresent());
        Object derivedValue = assistedQueryColumnItem.get().encrypt(databaseName, schemaName, tableName, encryptColumn.getName(), originalValue);
        addDerivedColumn(insertValueContext, insertValueToken, columnIndex, indexDelta, derivedValue);
    }
    
    private void addLikeQueryColumn(final String schemaName, final String tableName, final EncryptColumn encryptColumn,
                                    final InsertValueContext insertValueContext, final InsertValue insertValueToken, final int columnIndex, final int indexDelta, final Object originalValue) {
        Optional<LikeQueryColumnItem> likeQueryColumnItem = encryptColumn.getLikeQuery();
        Preconditions.checkState(likeQueryColumnItem.isPresent());
        Object derivedValue = likeQueryColumnItem.get().encrypt(databaseName, schemaName, tableName, encryptColumn.getName(), originalValue);
        addDerivedColumn(insertValueContext, insertValueToken, columnIndex, indexDelta, derivedValue);
    }
    
    private void addDerivedColumn(final InsertValueContext insertValueContext, final InsertValue insertValueToken, final int columnIndex, final int indexDelta, final Object derivedValue) {
        DerivedSimpleExpressionSegment derivedExpressionSegment = isAddLiteralExpressionSegment(insertValueContext, columnIndex)
                ? new DerivedLiteralExpressionSegment(derivedValue)
                : new DerivedParameterMarkerExpressionSegment(getParameterIndexCount(insertValueToken));
        insertValueToken.getValues().add(columnIndex + indexDelta, derivedExpressionSegment);
    }
    
    private boolean isAddLiteralExpressionSegment(final InsertValueContext insertValueContext, final int columnIndex) {
        return insertValueContext.getParameters().isEmpty() || insertValueContext.getValueExpressions().get(columnIndex) instanceof LiteralExpressionSegment;
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
