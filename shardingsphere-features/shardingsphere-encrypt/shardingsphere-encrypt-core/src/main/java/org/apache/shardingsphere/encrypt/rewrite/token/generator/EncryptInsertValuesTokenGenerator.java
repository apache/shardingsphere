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

package org.apache.shardingsphere.encrypt.rewrite.token.generator;

import lombok.Setter;
import org.apache.shardingsphere.encrypt.context.EncryptContextBuilder;
import org.apache.shardingsphere.encrypt.exception.UnsupportedEncryptInsertValueException;
import org.apache.shardingsphere.encrypt.rewrite.aware.DatabaseNameAware;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptInsertValuesToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.expression.DerivedLiteralExpressionSegment;
import org.apache.shardingsphere.infra.binder.segment.insert.values.expression.DerivedParameterMarkerExpressionSegment;
import org.apache.shardingsphere.infra.binder.segment.insert.values.expression.DerivedSimpleExpressionSegment;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
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
    public boolean isGenerateSQLToken(final SQLStatementContext<?> sqlStatementContext) {
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
        int count = 0;
        String schemaName = insertStatementContext.getTablesContext().getSchemaName().orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(insertStatementContext.getDatabaseType(), databaseName));
        for (InsertValueContext each : insertStatementContext.getInsertValueContexts()) {
            encryptToken(insertValuesToken.getInsertValues().get(count), schemaName, tableName, insertStatementContext, each);
            count++;
        }
    }
    
    private InsertValuesToken generateNewSQLToken(final InsertStatementContext insertStatementContext) {
        String tableName = insertStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        Collection<InsertValuesSegment> insertValuesSegments = insertStatementContext.getSqlStatement().getValues();
        InsertValuesToken result = new EncryptInsertValuesToken(getStartIndex(insertValuesSegments), getStopIndex(insertValuesSegments));
        String schemaName = insertStatementContext.getTablesContext().getSchemaName().orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(insertStatementContext.getDatabaseType(), databaseName));
        for (InsertValueContext each : insertStatementContext.getInsertValueContexts()) {
            InsertValue insertValueToken = new InsertValue(each.getValueExpressions());
            encryptToken(insertValueToken, schemaName, tableName, insertStatementContext, each);
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
    
    private void encryptToken(final InsertValue insertValueToken, final String schemaName, final String tableName,
                              final InsertStatementContext insertStatementContext, final InsertValueContext insertValueContext) {
        Optional<SQLToken> useDefaultInsertColumnsToken = findPreviousSQLToken(UseDefaultInsertColumnsToken.class);
        Iterator<String> descendingColumnNames = insertStatementContext.getDescendingColumnNames();
        while (descendingColumnNames.hasNext()) {
            String columnName = descendingColumnNames.next();
            Optional<EncryptAlgorithm> encryptor = encryptRule.findEncryptor(tableName, columnName);
            if (encryptor.isPresent()) {
                int columnIndex = useDefaultInsertColumnsToken.map(optional -> ((UseDefaultInsertColumnsToken) optional).getColumns().indexOf(columnName))
                        .orElseGet(() -> insertStatementContext.getColumnNames().indexOf(columnName));
                Object originalValue = insertValueContext.getLiteralValue(columnIndex).orElseThrow(() -> new UnsupportedEncryptInsertValueException(columnIndex));
                EncryptContext encryptContext = EncryptContextBuilder.build(databaseName, schemaName, tableName, columnName);
                addPlainColumn(insertValueToken, columnIndex, encryptContext, insertValueContext, originalValue);
                if (encryptRule.findAssistedQueryEncryptor(tableName, columnName).isPresent()) {
                    addAssistedQueryColumn(insertValueToken, encryptRule.findAssistedQueryEncryptor(tableName, columnName).get(), columnIndex, encryptContext, insertValueContext, originalValue);
                }
                setCipherColumn(insertValueToken, encryptor.get(), columnIndex, encryptContext, insertValueContext.getValueExpressions().get(columnIndex), originalValue);
            }
        }
    }
    
    private void addPlainColumn(final InsertValue insertValueToken, final int columnIndex,
                                final EncryptContext encryptContext, final InsertValueContext insertValueContext, final Object originalValue) {
        if (encryptRule.findPlainColumn(encryptContext.getTableName(), encryptContext.getColumnName()).isPresent()) {
            DerivedSimpleExpressionSegment derivedExpressionSegment = isAddLiteralExpressionSegment(insertValueContext, columnIndex)
                    ? new DerivedLiteralExpressionSegment(originalValue)
                    : new DerivedParameterMarkerExpressionSegment(getParameterIndexCount(insertValueToken));
            insertValueToken.getValues().add(columnIndex + 1, derivedExpressionSegment);
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void addAssistedQueryColumn(final InsertValue insertValueToken, final EncryptAlgorithm encryptAlgorithm, final int columnIndex,
                                        final EncryptContext encryptContext, final InsertValueContext insertValueContext, final Object originalValue) {
        if (encryptRule.findAssistedQueryColumn(encryptContext.getTableName(), encryptContext.getColumnName()).isPresent()) {
            DerivedSimpleExpressionSegment derivedExpressionSegment = isAddLiteralExpressionSegment(insertValueContext, columnIndex)
                    ? new DerivedLiteralExpressionSegment(encryptAlgorithm.encrypt(originalValue, encryptContext))
                    : new DerivedParameterMarkerExpressionSegment(getParameterIndexCount(insertValueToken));
            insertValueToken.getValues().add(columnIndex + 1, derivedExpressionSegment);
        }
    }
    
    private boolean isAddLiteralExpressionSegment(final InsertValueContext insertValueContext, final int columnIndex) {
        return insertValueContext.getParameters().isEmpty()
                || insertValueContext.getValueExpressions().get(columnIndex) instanceof LiteralExpressionSegment;
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
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setCipherColumn(final InsertValue insertValueToken, final EncryptAlgorithm encryptAlgorithm, final int columnIndex,
                                 final EncryptContext encryptContext, final ExpressionSegment valueExpression, final Object originalValue) {
        if (valueExpression instanceof LiteralExpressionSegment) {
            insertValueToken.getValues().set(columnIndex, new LiteralExpressionSegment(
                    valueExpression.getStartIndex(), valueExpression.getStopIndex(), encryptAlgorithm.encrypt(originalValue, encryptContext)));
        }
    }
}
