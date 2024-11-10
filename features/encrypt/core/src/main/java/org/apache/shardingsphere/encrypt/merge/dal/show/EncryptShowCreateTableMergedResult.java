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

import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateTableStatement;

import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * Encrypt show create table merged result.
 */
public final class EncryptShowCreateTableMergedResult implements MergedResult {
    
    private static final String COMMA = ", ";
    
    private static final int CREATE_TABLE_DEFINITION_INDEX = 2;
    
    private final MergedResult mergedResult;
    
    private final String tableName;
    
    private final EncryptRule rule;
    
    private final SQLParserEngine sqlParserEngine;
    
    public EncryptShowCreateTableMergedResult(final RuleMetaData globalRuleMetaData, final MergedResult mergedResult, final SQLStatementContext sqlStatementContext, final EncryptRule rule) {
        ShardingSpherePreconditions.checkState(sqlStatementContext instanceof TableAvailable && 1 == ((TableAvailable) sqlStatementContext).getTablesContext().getSimpleTables().size(),
                () -> new UnsupportedEncryptSQLException("SHOW CREATE TABLE FOR MULTI TABLES"));
        this.mergedResult = mergedResult;
        tableName = ((TableAvailable) sqlStatementContext).getTablesContext().getSimpleTables().iterator().next().getTableName().getIdentifier().getValue();
        this.rule = rule;
        sqlParserEngine = globalRuleMetaData.getSingleRule(SQLParserRule.class).getSQLParserEngine(sqlStatementContext.getDatabaseType());
    }
    
    @Override
    public boolean next() throws SQLException {
        return mergedResult.next();
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        if (CREATE_TABLE_DEFINITION_INDEX != columnIndex) {
            return mergedResult.getValue(columnIndex, type);
        }
        String createTableSQL = mergedResult.getValue(CREATE_TABLE_DEFINITION_INDEX, type).toString();
        Optional<EncryptTable> encryptTable = rule.findEncryptTable(tableName);
        if (!encryptTable.isPresent() || !createTableSQL.contains("(")) {
            return createTableSQL;
        }
        CreateTableStatement createTableStatement = (CreateTableStatement) sqlParserEngine.parse(createTableSQL, false);
        List<ColumnDefinitionSegment> columnDefinitions = new ArrayList<>(createTableStatement.getColumnDefinitions());
        StringBuilder result = new StringBuilder(createTableSQL.substring(0, columnDefinitions.get(0).getStartIndex()));
        for (ColumnDefinitionSegment each : columnDefinitions) {
            findLogicColumnDefinition(each, encryptTable.get(), createTableSQL).ifPresent(optional -> result.append(optional).append(COMMA));
        }
        // TODO decorate encrypt column index when we support index rewrite
        result.delete(result.length() - COMMA.length(), result.length()).append(createTableSQL.substring(columnDefinitions.get(columnDefinitions.size() - 1).getStopIndex() + 1));
        return result.toString();
    }
    
    private Optional<String> findLogicColumnDefinition(final ColumnDefinitionSegment columnDefinition, final EncryptTable encryptTable, final String createTableSQL) {
        ColumnSegment columnSegment = columnDefinition.getColumnName();
        String columnName = columnSegment.getIdentifier().getValue();
        if (encryptTable.isCipherColumn(columnName)) {
            String logicColumn = encryptTable.getLogicColumnByCipherColumn(columnName);
            return Optional.of(createTableSQL.substring(columnDefinition.getStartIndex(), columnSegment.getStartIndex())
                    + columnSegment.getIdentifier().getQuoteCharacter().wrap(logicColumn) + createTableSQL.substring(columnSegment.getStopIndex() + 1, columnDefinition.getStopIndex() + 1));
        }
        if (isDerivedColumn(encryptTable, columnName)) {
            return Optional.empty();
        }
        return Optional.of(createTableSQL.substring(columnDefinition.getStartIndex(), columnDefinition.getStopIndex() + 1));
    }
    
    private boolean isDerivedColumn(final EncryptTable encryptTable, final String columnName) {
        return encryptTable.isAssistedQueryColumn(columnName) || encryptTable.isLikeQueryColumn(columnName);
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        throw new SQLFeatureNotSupportedException("");
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        throw new SQLFeatureNotSupportedException("");
    }
    
    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("");
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return mergedResult.wasNull();
    }
}
