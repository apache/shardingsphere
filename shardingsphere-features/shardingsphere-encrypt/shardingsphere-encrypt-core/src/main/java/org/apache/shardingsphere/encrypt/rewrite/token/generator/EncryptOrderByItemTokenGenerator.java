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
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.aware.EncryptRuleAware;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Order by item token generator for encrypt.
 */
@Setter
public final class EncryptOrderByItemTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext<?>>, SchemaMetaDataAware, EncryptRuleAware {
    
    private String databaseName;
    
    private Map<String, ShardingSphereSchema> schemas;
    
    private EncryptRule encryptRule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext<?> sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && containsOrderByItem(sqlStatementContext);
    }
    
    @Override
    public Collection<SubstitutableColumnNameToken> generateSQLTokens(final SQLStatementContext<?> sqlStatementContext) {
        Collection<SubstitutableColumnNameToken> result = new LinkedHashSet<>();
        String defaultSchema = DatabaseTypeEngine.getDefaultSchemaName(sqlStatementContext.getDatabaseType(), databaseName);
        ShardingSphereSchema schema = sqlStatementContext.getTablesContext().getSchemaName().map(schemas::get).orElseGet(() -> schemas.get(defaultSchema));
        for (OrderByItem each : getOrderByItems(sqlStatementContext)) {
            if (each.getSegment() instanceof ColumnOrderByItemSegment) {
                ColumnSegment columnSegment = ((ColumnOrderByItemSegment) each.getSegment()).getColumn();
                Map<String, String> columnTableNames = sqlStatementContext.getTablesContext().findTableNamesByColumnSegment(Collections.singletonList(columnSegment), schema);
                result.addAll(generateSQLTokensWithColumnSegments(Collections.singletonList(columnSegment), columnTableNames));
            }
        }
        return result;
    }
    
    private Collection<SubstitutableColumnNameToken> generateSQLTokensWithColumnSegments(final Collection<ColumnSegment> columnSegments, final Map<String, String> columnTableNames) {
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        for (ColumnSegment column : columnSegments) {
            String tableName = columnTableNames.getOrDefault(column.getExpression(), "");
            Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
            if (!encryptTable.isPresent() || !encryptTable.get().findEncryptorName(column.getIdentifier().getValue()).isPresent()) {
                continue;
            }
            int startIndex = column.getOwner().isPresent() ? column.getOwner().get().getStopIndex() + 2 : column.getStartIndex();
            int stopIndex = column.getStopIndex();
            boolean queryWithCipherColumn = encryptRule.isQueryWithCipherColumn(tableName, column.getIdentifier().getValue());
            if (!queryWithCipherColumn) {
                Optional<String> plainColumn = encryptTable.get().findPlainColumn(column.getIdentifier().getValue());
                if (plainColumn.isPresent()) {
                    result.add(new SubstitutableColumnNameToken(startIndex, stopIndex, createColumnProjections(plainColumn.get())));
                    continue;
                }
            }
            Optional<String> assistedQueryColumn = encryptTable.get().findAssistedQueryColumn(column.getIdentifier().getValue());
            SubstitutableColumnNameToken encryptColumnNameToken = assistedQueryColumn.map(optional -> new SubstitutableColumnNameToken(startIndex, stopIndex, createColumnProjections(optional)))
                    .orElseGet(() -> new SubstitutableColumnNameToken(startIndex, stopIndex, createColumnProjections(encryptTable.get().getCipherColumn(column.getIdentifier().getValue()))));
            result.add(encryptColumnNameToken);
        }
        return result;
    }
    
    private Collection<OrderByItem> getOrderByItems(final SQLStatementContext<?> sqlStatementContext) {
        if (!(sqlStatementContext instanceof SelectStatementContext)) {
            return Collections.emptyList();
        }
        Collection<OrderByItem> result = new LinkedList<>();
        SelectStatementContext statementContext = (SelectStatementContext) sqlStatementContext;
        result.addAll(statementContext.getOrderByContext().getItems());
        result.addAll(statementContext.getGroupByContext().getItems());
        for (SelectStatementContext each : statementContext.getSubqueryContexts().values()) {
            result.addAll(getOrderByItems(each));
        }
        return result;
    }
    
    private boolean containsOrderByItem(final SQLStatementContext<?> sqlStatementContext) {
        if (!(sqlStatementContext instanceof SelectStatementContext)) {
            return false;
        }
        SelectStatementContext statementContext = (SelectStatementContext) sqlStatementContext;
        if (!statementContext.getOrderByContext().getItems().isEmpty() || !statementContext.getGroupByContext().getItems().isEmpty()) {
            return true;
        }
        for (SelectStatementContext each : statementContext.getSubqueryContexts().values()) {
            if (containsOrderByItem(each)) {
                return true;
            }
        }
        return false;
    }
    
    private Collection<ColumnProjection> createColumnProjections(final String columnName) {
        return Collections.singletonList(new ColumnProjection(null, columnName, null));
    }
}
