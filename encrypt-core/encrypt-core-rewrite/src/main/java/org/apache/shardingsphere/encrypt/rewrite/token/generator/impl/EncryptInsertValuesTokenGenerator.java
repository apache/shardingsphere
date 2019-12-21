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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.impl;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptInsertValuesToken;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.spi.encrypt.ShardingQueryAssistedEncryptor;
import org.apache.shardingsphere.sql.parser.relation.segment.insert.InsertValueContext;
import org.apache.shardingsphere.sql.parser.relation.segment.insert.expression.DerivedLiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.relation.segment.insert.expression.DerivedParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.relation.segment.insert.expression.DerivedSimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.generic.InsertValue;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.generic.InsertValuesToken;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.generic.UseDefaultInsertColumnsToken;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Insert values token generator for encrypt.
 *
 * @author panjuan
 */
@Setter
public final class EncryptInsertValuesTokenGenerator extends BaseEncryptSQLTokenGenerator implements OptionalSQLTokenGenerator, PreviousSQLTokensAware {
    
    private List<SQLToken> previousSQLTokens;
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertSQLStatementContext && !sqlStatementContext.getSqlStatement().findSQLSegments(InsertValuesSegment.class).isEmpty();
    }
    
    @Override
    public InsertValuesToken generateSQLToken(final SQLStatementContext sqlStatementContext) {
        Optional<SQLToken> insertValuesToken = findPreviousSQLToken(InsertValuesToken.class);
        if (insertValuesToken.isPresent()) {
            processPreviousSQLToken((InsertSQLStatementContext) sqlStatementContext, (InsertValuesToken) insertValuesToken.get());
            return (InsertValuesToken) insertValuesToken.get();
        }
        return generateNewSQLToken((InsertSQLStatementContext) sqlStatementContext);
    }
    
    private Optional<SQLToken> findPreviousSQLToken(final Class<?> sqlToken) {
        for (SQLToken each : previousSQLTokens) {
            if (sqlToken.isAssignableFrom(each.getClass())) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private void processPreviousSQLToken(final InsertSQLStatementContext sqlStatementContext, final InsertValuesToken insertValuesToken) {
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        int count = 0;
        for (InsertValueContext each : sqlStatementContext.getInsertValueContexts()) {
            encryptToken(insertValuesToken.getInsertValues().get(count), tableName, sqlStatementContext, each);
            count++;
        }
    }
    
    private InsertValuesToken generateNewSQLToken(final InsertSQLStatementContext sqlStatementContext) {
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        Collection<InsertValuesSegment> insertValuesSegments = sqlStatementContext.getSqlStatement().findSQLSegments(InsertValuesSegment.class);
        InsertValuesToken result = new EncryptInsertValuesToken(getStartIndex(insertValuesSegments), getStopIndex(insertValuesSegments));
        for (InsertValueContext each : sqlStatementContext.getInsertValueContexts()) {
            InsertValue insertValueToken = new InsertValue(each.getValueExpressions());
            encryptToken(insertValueToken, tableName, sqlStatementContext, each);
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
    
    private void encryptToken(final InsertValue insertValueToken, final String tableName, final InsertSQLStatementContext sqlStatementContext, final InsertValueContext insertValueContext) {
        Optional<SQLToken> useDefaultInsertColumnsToken = findPreviousSQLToken(UseDefaultInsertColumnsToken.class);
        Set<String> columnNames = new HashSet<>(sqlStatementContext.getColumnNames());
        Iterator<String> descendingColumnNames = sqlStatementContext.getDescendingColumnNames();
        while (descendingColumnNames.hasNext()) {
            String columnName = descendingColumnNames.next();
            Optional<ShardingEncryptor> encryptor = getEncryptRule().findShardingEncryptor(tableName, columnName);
            if (encryptor.isPresent()) {
                int columnIndex = useDefaultInsertColumnsToken.isPresent()
                        ? ((UseDefaultInsertColumnsToken) useDefaultInsertColumnsToken.get()).getColumns().indexOf(columnName) : sqlStatementContext.getColumnNames().indexOf(columnName);
                Object originalValue = insertValueContext.getValue(columnIndex);
                addPlainColumn(insertValueToken, columnIndex, tableName, columnName, insertValueContext, originalValue, columnNames);
                addAssistedQueryColumn(insertValueToken, encryptor.get(), columnIndex, tableName, columnName, insertValueContext, originalValue, columnNames);
                setCipherColumn(insertValueToken, encryptor.get(), columnIndex, insertValueContext.getValueExpressions().get(columnIndex), originalValue);
            }
        }
    }
    
    private void addPlainColumn(final InsertValue insertValueToken, final int columnIndex,
                                final String tableName, final String columnName, final InsertValueContext insertValueContext, final Object originalValue, final Set<String> columnNames) {
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, columnName);
        if (plainColumn.isPresent()) {
            if (columnNames.contains(plainColumn.get())) {
                return;
            }
            DerivedSimpleExpressionSegment derivedExpressionSegment = insertValueContext.getParameters().isEmpty()
                    ? new DerivedLiteralExpressionSegment(originalValue) : new DerivedParameterMarkerExpressionSegment(getParameterIndexCount(insertValueToken));
            insertValueToken.getValues().add(columnIndex + 1, derivedExpressionSegment);
        }
    }
    
    private void addAssistedQueryColumn(final InsertValue insertValueToken, final ShardingEncryptor encryptor, final int columnIndex,
                                        final String tableName, final String columnName, final InsertValueContext insertValueContext, final Object originalValue, final Set<String> columnNames) {
        Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(tableName, columnName);
        if (assistedQueryColumn.isPresent()) {
            if (columnNames.contains(assistedQueryColumn.get())) {
                return;
            }
            DerivedSimpleExpressionSegment derivedExpressionSegment = insertValueContext.getParameters().isEmpty()
                    ? new DerivedLiteralExpressionSegment(((ShardingQueryAssistedEncryptor) encryptor).queryAssistedEncrypt(null == originalValue ? null : originalValue.toString()))
                    : new DerivedParameterMarkerExpressionSegment(getParameterIndexCount(insertValueToken));
            insertValueToken.getValues().add(columnIndex + 1, derivedExpressionSegment);
        }
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
    
    private void setCipherColumn(final InsertValue insertValueToken, final ShardingEncryptor encryptor,
                                 final int columnIndex, final ExpressionSegment valueExpression, final Object originalValue) {
        if (valueExpression instanceof LiteralExpressionSegment) {
            insertValueToken.getValues().set(columnIndex, new LiteralExpressionSegment(valueExpression.getStartIndex(), valueExpression.getStopIndex(), encryptor.encrypt(originalValue)));
        }
    }
}
