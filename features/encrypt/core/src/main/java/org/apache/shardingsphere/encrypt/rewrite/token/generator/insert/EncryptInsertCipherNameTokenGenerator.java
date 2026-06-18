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
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.encrypt.checker.cryptographic.InsertSelectColumnsEncryptorChecker;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Insert cipher column name token generator for encrypt.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
@Setter
public final class EncryptInsertCipherNameTokenGenerator implements CollectionSQLTokenGenerator<InsertStatementContext> {
    
    private final EncryptRule rule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext instanceof InsertStatementContext)) {
            return false;
        }
        Optional<InsertColumnsSegment> insertColumnsSegment = ((InsertStatementContext) sqlStatementContext).getSqlStatement().getInsertColumns();
        return insertColumnsSegment.isPresent() && !insertColumnsSegment.get().getColumns().isEmpty();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final InsertStatementContext insertStatementContext) {
        Optional<InsertColumnsSegment> insertColumnsSegment = insertStatementContext.getSqlStatement().getInsertColumns();
        Preconditions.checkState(insertColumnsSegment.isPresent());
        Collection<ColumnSegment> insertColumns = insertColumnsSegment.get().getColumns();
        if (null != insertStatementContext.getInsertSelectContext()) {
            checkInsertSelectEncryptor(insertStatementContext.getInsertSelectContext().getSelectStatementContext(), insertColumns);
        }
        Optional<EncryptTable> encryptTable = rule.findEncryptTable(insertStatementContext.getSqlStatement().getTable()
                .map(optional -> optional.getTableName().getIdentifier().getValue()).orElse(""));
        if (!encryptTable.isPresent()) {
            return Collections.emptyList();
        }
        DatabaseType databaseType = insertStatementContext.getSqlStatement().getDatabaseType();
        QuoteCharacter quoteCharacter = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getQuoteCharacter();
        Collection<SQLToken> result = new LinkedList<>();
        for (ColumnSegment each : insertColumns) {
            String columnName = each.getIdentifier().getValue();
            if (encryptTable.get().isEncryptColumn(columnName)) {
                IdentifierValue name = new IdentifierValue(encryptTable.get().getEncryptColumn(columnName).getCipher().getName(), quoteCharacter);
                Collection<Projection> projections = Collections.singleton(new ColumnProjection(null, name, null, databaseType));
                result.add(new SubstitutableColumnNameToken(each.getStartIndex(), each.getStopIndex(), projections, databaseType));
            }
        }
        return result;
    }
    
    private void checkInsertSelectEncryptor(final SelectStatementContext selectStatementContext, final Collection<ColumnSegment> insertColumns) {
        Collection<Projection> projections = selectStatementContext.getProjectionsContext().getExpandProjections();
        ShardingSpherePreconditions.checkState(insertColumns.size() == projections.size(), () -> new UnsupportedSQLOperationException("Column count doesn't match value count."));
        InsertSelectColumnsEncryptorChecker.checkIsSame(insertColumns, projections, rule);
    }
}
