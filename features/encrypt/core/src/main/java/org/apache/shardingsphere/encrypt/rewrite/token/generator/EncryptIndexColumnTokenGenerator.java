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
import org.apache.shardingsphere.encrypt.rewrite.aware.DatabaseTypeAware;
import org.apache.shardingsphere.encrypt.rewrite.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.IndexAvailable;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Index column token generator for encrypt.
 */
@Setter
public final class EncryptIndexColumnTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext>, EncryptRuleAware, DatabaseTypeAware {
    
    private EncryptRule encryptRule;
    
    private DatabaseType databaseType;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof IndexAvailable;
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Preconditions.checkArgument(sqlStatementContext instanceof IndexAvailable, "SQLStatementContext must implementation IndexAvailable interface.");
        if (sqlStatementContext.getTablesContext().getTableNames().isEmpty()) {
            return Collections.emptyList();
        }
        String tableName = sqlStatementContext.getTablesContext().getTableNames().iterator().next();
        EncryptTable encryptTable = encryptRule.getEncryptTable(tableName);
        Collection<SQLToken> result = new LinkedList<>();
        for (ColumnSegment each : ((IndexAvailable) sqlStatementContext).getIndexColumns()) {
            if (encryptTable.isEncryptColumn(each.getIdentifier().getValue())) {
                getColumnToken(encryptTable, each).ifPresent(result::add);
            }
        }
        return result;
    }
    
    private Optional<SQLToken> getColumnToken(final EncryptTable encryptTable, final ColumnSegment columnSegment) {
        QuoteCharacter quoteCharacter = columnSegment.getIdentifier().getQuoteCharacter();
        int startIndex = columnSegment.getStartIndex();
        int stopIndex = columnSegment.getStopIndex();
        String columnName = columnSegment.getIdentifier().getValue();
        EncryptColumn encryptColumn = encryptTable.getEncryptColumn(columnName);
        String queryColumnName = encryptColumn.getAssistedQuery().isPresent() ? encryptColumn.getAssistedQuery().get().getName() : encryptColumn.getCipher().getName();
        return getQueryColumnToken(startIndex, stopIndex, queryColumnName, quoteCharacter);
    }
    
    private Optional<SQLToken> getQueryColumnToken(final int startIndex, final int stopIndex, final String queryColumnName, final QuoteCharacter quoteCharacter) {
        Collection<Projection> columnProjections = getColumnProjections(queryColumnName, quoteCharacter);
        return Optional.of(new SubstitutableColumnNameToken(startIndex, stopIndex, columnProjections, quoteCharacter));
    }
    
    private Collection<Projection> getColumnProjections(final String columnName, final QuoteCharacter quoteCharacter) {
        return Collections.singletonList(new ColumnProjection(null, new IdentifierValue(columnName, quoteCharacter), null, databaseType));
    }
}
