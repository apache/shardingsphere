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

import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.aware.EncryptRuleAware;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.IndexAvailable;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.enums.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Index column token generator for encrypt.
 */
@Setter
public final class EncryptIndexColumnTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext<?>>, EncryptRuleAware {
    
    private EncryptRule encryptRule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext<?> sqlStatementContext) {
        return sqlStatementContext instanceof IndexAvailable;
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext<?> sqlStatementContext) {
        Preconditions.checkArgument(sqlStatementContext instanceof IndexAvailable, "SQLStatementContext must implementation IndexAvailable interface.");
        if (sqlStatementContext.getTablesContext().getTableNames().isEmpty()) {
            return Collections.emptyList();
        }
        Collection<SQLToken> result = new LinkedList<>();
        String tableName = sqlStatementContext.getTablesContext().getTableNames().iterator().next();
        for (ColumnSegment each : ((IndexAvailable) sqlStatementContext).getIndexColumns()) {
            encryptRule.findEncryptor(tableName, each.getIdentifier().getValue()).flatMap(optional -> getColumnToken(tableName, each)).ifPresent(result::add);
        }
        return result;
    }
    
    private Optional<SQLToken> getColumnToken(final String tableName, final ColumnSegment columnSegment) {
        String columnName = columnSegment.getIdentifier().getValue();
        QuoteCharacter quoteCharacter = columnSegment.getIdentifier().getQuoteCharacter();
        int startIndex = columnSegment.getStartIndex();
        int stopIndex = columnSegment.getStopIndex();
        boolean queryWithCipherColumn = encryptRule.isQueryWithCipherColumn(tableName, columnName);
        if (queryWithCipherColumn) {
            return encryptRule.findAssistedQueryColumn(tableName, columnName).map(optional -> getAssistedQueryColumnToken(startIndex, stopIndex, optional, quoteCharacter))
                    .orElseGet(() -> getCipherColumnToken(tableName, startIndex, stopIndex, columnName, quoteCharacter));
        }
        return getPlainColumnToken(tableName, startIndex, stopIndex, columnName, quoteCharacter);
    }
    
    private Optional<SQLToken> getAssistedQueryColumnToken(final int startIndex, final int stopIndex, final String columnName, final QuoteCharacter quoteCharacter) {
        Collection<ColumnProjection> columnProjections = getColumnProjections(columnName);
        return Optional.of(new SubstitutableColumnNameToken(startIndex, stopIndex, columnProjections, quoteCharacter));
    }
    
    private Optional<SQLToken> getCipherColumnToken(final String tableName, final int startIndex, final int stopIndex, final String columnName, final QuoteCharacter quoteCharacter) {
        String cipherColumn = encryptRule.getCipherColumn(tableName, columnName);
        Collection<ColumnProjection> columnProjections = getColumnProjections(cipherColumn);
        return Optional.of(new SubstitutableColumnNameToken(startIndex, stopIndex, columnProjections, quoteCharacter));
    }
    
    private Optional<SQLToken> getPlainColumnToken(final String tableName, final int startIndex, final int stopIndex, final String columnName, final QuoteCharacter quoteCharacter) {
        return encryptRule.findPlainColumn(tableName, columnName)
                .map(optional -> new SubstitutableColumnNameToken(startIndex, stopIndex, getColumnProjections(optional), quoteCharacter));
    }
    
    private Collection<ColumnProjection> getColumnProjections(final String columnName) {
        return Collections.singletonList(new ColumnProjection(null, columnName, null));
    }
}
