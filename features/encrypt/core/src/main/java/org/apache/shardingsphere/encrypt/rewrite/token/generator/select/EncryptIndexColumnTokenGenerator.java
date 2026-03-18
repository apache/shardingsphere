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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.select;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.IndexSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Index column token generator for encrypt.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
@Setter
public final class EncryptIndexColumnTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext> {
    
    private final EncryptRule rule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return !sqlStatementContext.getTablesContext().getTableNames().isEmpty()
                && sqlStatementContext.getSqlStatement().getAttributes().findAttribute(IndexSQLStatementAttribute.class).map(optional -> !optional.getIndexes().isEmpty()).orElse(false);
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        String tableName = sqlStatementContext.getTablesContext().getTableNames().iterator().next();
        EncryptTable encryptTable = rule.getEncryptTable(tableName);
        Collection<SQLToken> result = new LinkedList<>();
        for (ColumnSegment each : sqlStatementContext.getSqlStatement().getAttributes()
                .findAttribute(IndexSQLStatementAttribute.class).map(IndexSQLStatementAttribute::getIndexColumns).orElse(Collections.emptyList())) {
            if (encryptTable.isEncryptColumn(each.getIdentifier().getValue())) {
                generateSQLToken(encryptTable, each, sqlStatementContext.getSqlStatement().getDatabaseType()).ifPresent(result::add);
            }
        }
        return result;
    }
    
    private Optional<SQLToken> generateSQLToken(final EncryptTable encryptTable, final ColumnSegment columnSegment, final DatabaseType databaseType) {
        QuoteCharacter quoteCharacter = columnSegment.getIdentifier().getQuoteCharacter();
        int startIndex = columnSegment.getStartIndex();
        int stopIndex = columnSegment.getStopIndex();
        String columnName = columnSegment.getIdentifier().getValue();
        EncryptColumn encryptColumn = encryptTable.getEncryptColumn(columnName);
        String queryColumnName = encryptColumn.getAssistedQuery().isPresent() ? encryptColumn.getAssistedQuery().get().getName() : encryptColumn.getCipher().getName();
        return getQueryColumnToken(startIndex, stopIndex, queryColumnName, quoteCharacter, databaseType);
    }
    
    private Optional<SQLToken> getQueryColumnToken(final int startIndex, final int stopIndex, final String queryColumnName, final QuoteCharacter quoteCharacter, final DatabaseType databaseType) {
        Collection<Projection> columnProjections = getColumnProjections(queryColumnName, quoteCharacter, databaseType);
        return Optional.of(new SubstitutableColumnNameToken(startIndex, stopIndex, columnProjections, databaseType));
    }
    
    private Collection<Projection> getColumnProjections(final String columnName, final QuoteCharacter quoteCharacter, final DatabaseType databaseType) {
        return Collections.singleton(new ColumnProjection(null, new IdentifierValue(columnName, quoteCharacter), null, databaseType));
    }
}
