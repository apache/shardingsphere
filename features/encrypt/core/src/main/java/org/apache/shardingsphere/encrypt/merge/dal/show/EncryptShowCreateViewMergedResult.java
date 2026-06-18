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

package org.apache.shardingsphere.encrypt.merge.dal.show;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.decorator.DecoratorMergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Encrypt show create table merged result.
 */
public final class EncryptShowCreateViewMergedResult extends DecoratorMergedResult {
    
    private static final String COMMA = ", ";
    
    private static final int CREATE_TABLE_DEFINITION_INDEX = 2;
    
    private final MergedResult mergedResult;
    
    private final String viewName;
    
    private final EncryptRule rule;
    
    private final ShardingSphereMetaData metaData;
    
    private final SQLParserEngine sqlParserEngine;
    
    private final String currentDatabaseName;
    
    private final DatabaseType databaseType;
    
    public EncryptShowCreateViewMergedResult(final ShardingSphereMetaData metaData, final MergedResult mergedResult, final String viewName, final EncryptRule rule, final String currentDatabaseName) {
        super(mergedResult);
        this.mergedResult = mergedResult;
        this.viewName = viewName;
        this.rule = rule;
        this.metaData = metaData;
        this.currentDatabaseName = currentDatabaseName;
        databaseType = metaData.getDatabase(this.currentDatabaseName).getProtocolType();
        sqlParserEngine = metaData.getGlobalRuleMetaData().getSingleRule(SQLParserRule.class).getSQLParserEngine(databaseType);
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        if (CREATE_TABLE_DEFINITION_INDEX != columnIndex) {
            return mergedResult.getValue(columnIndex, type);
        }
        String createViewSQL = mergedResult.getValue(CREATE_TABLE_DEFINITION_INDEX, type).toString();
        Optional<EncryptTable> encryptView = rule.findEncryptTable(viewName);
        if (!encryptView.isPresent()) {
            return createViewSQL;
        }
        CreateViewStatement createViewStatement = (CreateViewStatement) sqlParserEngine.parse(createViewSQL, false);
        Optional<TableSegment> from = createViewStatement.getSelect().getFrom();
        if (!from.isPresent() || !(from.get() instanceof SimpleTableSegment)) {
            return createViewSQL;
        }
        String tableName = ((SimpleTableSegment) from.get()).getTableName().getIdentifier().getValue();
        Optional<EncryptTable> encryptTable =
                metaData.getDatabase(getDatabaseName((SimpleTableSegment) from.get(), databaseType, currentDatabaseName).getValue()).getRuleMetaData().findSingleRule(EncryptRule.class)
                        .flatMap(encryptRule -> encryptRule.findEncryptTable(tableName));
        if (!encryptTable.isPresent()) {
            return createViewSQL;
        }
        List<ProjectionSegment> projections = createViewStatement.getSelect().getProjections().getProjections();
        StringBuilder result = new StringBuilder(createViewSQL.substring(0, projections.get(0).getStartIndex()));
        for (ProjectionSegment each : projections) {
            findLogicColumnDefinition(each, encryptView.get(), encryptTable.get(), createViewSQL).ifPresent(optional -> result.append(optional).append(COMMA));
        }
        result.delete(result.length() - COMMA.length(), result.length()).append(createViewSQL.substring(projections.get(projections.size() - 1).getStopIndex() + 1));
        return result.toString();
    }
    
    private static IdentifierValue getDatabaseName(final SimpleTableSegment segment, final DatabaseType databaseType, final String currentDatabaseName) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
        Optional<OwnerSegment> owner = dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().isPresent() ? segment.getOwner().flatMap(OwnerSegment::getOwner) : segment.getOwner();
        return new IdentifierValue(owner.map(optional -> optional.getIdentifier().getValue()).orElse(currentDatabaseName));
    }
    
    private Optional<String> findLogicColumnDefinition(final ProjectionSegment projectionSegment, final EncryptTable encryptView, final EncryptTable encryptTable, final String createTableSQL) {
        if (!(projectionSegment instanceof ColumnProjectionSegment)) {
            return Optional.of(createTableSQL.substring(projectionSegment.getStartIndex(), projectionSegment.getStopIndex() + 1));
        }
        ColumnSegment columnSegment = ((ColumnProjectionSegment) projectionSegment).getColumn();
        String columnName = ((ColumnProjectionSegment) projectionSegment).getAliasName().orElse(columnSegment.getIdentifier().getValue());
        if (encryptView.isCipherColumn(columnName)) {
            String logicColumn = encryptView.getLogicColumnByCipherColumn(columnName);
            String actualColumn = encryptTable.getLogicColumnByCipherColumn(columnSegment.getIdentifier().getValue());
            StringBuilder result = new StringBuilder(actualColumn);
            Optional<IdentifierValue> alias = ((ColumnProjectionSegment) projectionSegment).getAlias();
            alias.ifPresent(identifierValue -> result.append(" AS ").append(identifierValue.getQuoteCharacter().wrap(logicColumn)));
            return Optional.of(result.toString());
        }
        if (isDerivedColumn(encryptView, columnName)) {
            return Optional.empty();
        }
        return Optional.of(createTableSQL.substring(projectionSegment.getStartIndex(), projectionSegment.getStopIndex() + 1));
    }
    
    private boolean isDerivedColumn(final EncryptTable encryptTable, final String columnName) {
        return encryptTable.isAssistedQueryColumn(columnName) || encryptTable.isLikeQueryColumn(columnName);
    }
}
