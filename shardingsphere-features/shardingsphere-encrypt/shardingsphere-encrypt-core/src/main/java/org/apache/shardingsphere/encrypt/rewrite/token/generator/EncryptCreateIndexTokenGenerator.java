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
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.aware.EncryptRuleAware;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Create index token generator for encrypt.
 */
@Setter
public final class EncryptCreateIndexTokenGenerator implements CollectionSQLTokenGenerator<CreateIndexStatementContext>, EncryptRuleAware {
    
    private EncryptRule encryptRule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext<?> sqlStatementContext) {
        return sqlStatementContext instanceof CreateIndexStatementContext && !(((CreateIndexStatementContext) sqlStatementContext).getSqlStatement()).getColumns().isEmpty();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final CreateIndexStatementContext createIndexStatementContext) {
        Collection<SQLToken> result = new LinkedList<>();
        String tableName = createIndexStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        Collection<ColumnSegment> columns = createIndexStatementContext.getSqlStatement().getColumns();
        for (ColumnSegment each : columns) {
            encryptRule.findEncryptor(tableName, each.getIdentifier().getValue()).flatMap(optional -> getColumnToken(tableName, each)).ifPresent(result::add);
        }
        return result;
    }
    
    private Optional<SQLToken> getColumnToken(final String tableName, final ColumnSegment columnSegment) {
        String columnName = columnSegment.getIdentifier().getValue();
        boolean queryWithCipherColumn = encryptRule.isQueryWithCipherColumn(tableName, columnName);
        if (queryWithCipherColumn) {
            Optional<String> assistedQueryColumn = encryptRule.findAssistedQueryColumn(tableName, columnName);
            return assistedQueryColumn.map(optional -> getAssistedQueryColumnToken(columnSegment, optional)).orElseGet(() -> getCipherColumnToken(tableName, columnSegment, columnName));
        }
        return getPlainColumnToken(tableName, columnSegment, columnName);
    }
    
    private Optional<SQLToken> getAssistedQueryColumnToken(final ColumnSegment columnSegment, final String columnName) {
        Collection<ColumnProjection> columnProjections = getColumnProjections(columnName);
        return Optional.of(new SubstitutableColumnNameToken(columnSegment.getStartIndex(), columnSegment.getStopIndex(), columnProjections));
    }
    
    private Optional<SQLToken> getCipherColumnToken(final String tableName, final ColumnSegment columnSegment, final String columnName) {
        String cipherColumn = encryptRule.getCipherColumn(tableName, columnName);
        Collection<ColumnProjection> columnProjections = getColumnProjections(cipherColumn);
        return Optional.of(new SubstitutableColumnNameToken(columnSegment.getStartIndex(), columnSegment.getStopIndex(), columnProjections));
    }
    
    private Optional<SQLToken> getPlainColumnToken(final String tableName, final ColumnSegment columnSegment, final String columnName) {
        Optional<String> plainColumn = encryptRule.findPlainColumn(tableName, columnName);
        return plainColumn.map(optional -> new SubstitutableColumnNameToken(columnSegment.getStartIndex(), columnSegment.getStopIndex(), getColumnProjections(optional)));
    }
    
    private Collection<ColumnProjection> getColumnProjections(final String columnName) {
        return Collections.singletonList(new ColumnProjection(null, columnName, null));
    }
}
