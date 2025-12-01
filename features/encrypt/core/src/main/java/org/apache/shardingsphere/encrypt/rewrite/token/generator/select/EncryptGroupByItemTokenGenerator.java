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

import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Group by item token generator for encrypt.
 */
@HighFrequencyInvocation
@Setter
public final class EncryptGroupByItemTokenGenerator implements CollectionSQLTokenGenerator<SelectStatementContext> {
    
    private final EncryptRule rule;
    
    public EncryptGroupByItemTokenGenerator(final EncryptRule rule) {
        this.rule = rule;
    }
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && containsGroupByItem((SelectStatementContext) sqlStatementContext);
    }
    
    private boolean containsGroupByItem(final SelectStatementContext sqlStatementContext) {
        if (!sqlStatementContext.getGroupByContext().getItems().isEmpty()) {
            return true;
        }
        for (SelectStatementContext each : sqlStatementContext.getSubqueryContexts().values()) {
            if (containsGroupByItem(each)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SelectStatementContext sqlStatementContext) {
        Collection<SQLToken> result = new LinkedList<>();
        for (GroupByContext each : getGroupByItems(sqlStatementContext)) {
            for (OrderByItem item : each.getItems()) {
                if (item.getSegment() instanceof ColumnOrderByItemSegment) {
                    ColumnSegment columnSegment = ((ColumnOrderByItemSegment) item.getSegment()).getColumn();
                    generateSQLToken(columnSegment, sqlStatementContext.getSqlStatement().getDatabaseType()).ifPresent(result::add);
                }
            }
        }
        return result;
    }
    
    private Optional<SubstitutableColumnNameToken> generateSQLToken(final ColumnSegment columnSegment, final DatabaseType databaseType) {
        Optional<EncryptTable> encryptTable = rule.findEncryptTable(columnSegment.getColumnBoundInfo().getOriginalTable().getValue());
        String columnName = columnSegment.getColumnBoundInfo().getOriginalColumn().getValue();
        if (!encryptTable.isPresent() || !encryptTable.get().isEncryptColumn(columnName)) {
            return Optional.empty();
        }
        EncryptColumn encryptColumn = encryptTable.get().getEncryptColumn(columnName);
        int startIndex = columnSegment.getOwner().isPresent() ? columnSegment.getOwner().get().getStopIndex() + 2 : columnSegment.getStartIndex();
        int stopIndex = columnSegment.getStopIndex();
        QuoteCharacter quoteCharacter = getQuoteCharacter(columnSegment, databaseType);
        return Optional.of(encryptColumn.getAssistedQuery()
                .map(optional -> new SubstitutableColumnNameToken(startIndex, stopIndex, createColumnProjections(optional.getName(), quoteCharacter, databaseType),
                        databaseType))
                .orElseGet(() -> new SubstitutableColumnNameToken(startIndex, stopIndex,
                        createColumnProjections(encryptColumn.getCipher().getName(), quoteCharacter, databaseType), databaseType)));
    }
    
    private Collection<GroupByContext> getGroupByItems(final SelectStatementContext sqlStatementContext) {
        Collection<GroupByContext> result = new LinkedList<>();
        result.add(sqlStatementContext.getGroupByContext());
        for (SelectStatementContext each : sqlStatementContext.getSubqueryContexts().values()) {
            result.addAll(getGroupByItems(each));
        }
        return result;
    }
    
    private QuoteCharacter getQuoteCharacter(final ColumnSegment columnSegment, final DatabaseType databaseType) {
        return TableSourceType.PHYSICAL_TABLE == columnSegment.getColumnBoundInfo().getTableSourceType() ? new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getQuoteCharacter()
                : columnSegment.getIdentifier().getQuoteCharacter();
    }
    
    private Collection<Projection> createColumnProjections(final String columnName, final QuoteCharacter quoteCharacter, final DatabaseType databaseType) {
        return Collections.singleton(new ColumnProjection(null, new IdentifierValue(columnName, quoteCharacter), null, databaseType));
    }
}
