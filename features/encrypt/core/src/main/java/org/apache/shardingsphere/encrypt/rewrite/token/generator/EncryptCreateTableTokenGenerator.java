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
import org.apache.shardingsphere.encrypt.constant.EncryptColumnDataType;
import org.apache.shardingsphere.encrypt.rewrite.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptColumnToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.CipherColumnItem;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Create table token generator for encrypt.
 */
@Setter
public final class EncryptCreateTableTokenGenerator implements CollectionSQLTokenGenerator<CreateTableStatementContext>, EncryptRuleAware {
    
    private EncryptRule encryptRule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof CreateTableStatementContext && !(((CreateTableStatementContext) sqlStatementContext).getSqlStatement()).getColumnDefinitions().isEmpty();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final CreateTableStatementContext createTableStatementContext) {
        Collection<SQLToken> result = new LinkedList<>();
        String tableName = createTableStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        EncryptTable encryptTable = encryptRule.getEncryptTable(tableName);
        List<ColumnDefinitionSegment> columns = new ArrayList<>(createTableStatementContext.getSqlStatement().getColumnDefinitions());
        for (int index = 0; index < columns.size(); index++) {
            ColumnDefinitionSegment each = columns.get(index);
            String columnName = each.getColumnName().getIdentifier().getValue();
            if (encryptTable.isEncryptColumn(columnName)) {
                result.addAll(getColumnTokens(encryptTable.getEncryptColumn(columnName), each, columns, index));
            }
        }
        return result;
    }
    
    private Collection<SQLToken> getColumnTokens(final EncryptColumn encryptColumn, final ColumnDefinitionSegment column, final List<ColumnDefinitionSegment> columns, final int index) {
        boolean lastColumn = columns.size() - 1 == index;
        int columnStopIndex = lastColumn ? column.getStopIndex() : columns.get(index + 1).getStartIndex() - 1;
        Collection<SQLToken> result = new LinkedList<>();
        result.add(new RemoveToken(column.getStartIndex(), columnStopIndex));
        result.add(getCipherColumnToken(encryptColumn, column, columnStopIndex));
        getAssistedQueryColumnToken(encryptColumn, column, columnStopIndex, lastColumn).ifPresent(result::add);
        getLikeQueryColumnToken(encryptColumn, column, columnStopIndex, lastColumn).ifPresent(result::add);
        return result;
    }
    
    private SQLToken getCipherColumnToken(final EncryptColumn encryptColumn, final ColumnDefinitionSegment column, final int stopIndex) {
        CipherColumnItem cipherColumnItem = encryptColumn.getCipher();
        return new EncryptColumnToken(stopIndex + 1, column.getStopIndex(), cipherColumnItem.getName(), EncryptColumnDataType.DEFAULT_DATA_TYPE);
    }
    
    private Optional<? extends SQLToken> getAssistedQueryColumnToken(final EncryptColumn encryptColumn, final ColumnDefinitionSegment column, final int stopIndex, final boolean lastColumn) {
        return encryptColumn.getAssistedQuery()
                .map(optional -> new EncryptColumnToken(stopIndex + 1, column.getStopIndex(), encryptColumn.getAssistedQuery().get().getName(), EncryptColumnDataType.DEFAULT_DATA_TYPE, lastColumn));
    }
    
    private Optional<? extends SQLToken> getLikeQueryColumnToken(final EncryptColumn encryptColumn, final ColumnDefinitionSegment column, final int stopIndex, final boolean lastColumn) {
        return encryptColumn.getLikeQuery()
                .map(optional -> new EncryptColumnToken(stopIndex + 1, column.getStopIndex(), encryptColumn.getLikeQuery().get().getName(), EncryptColumnDataType.DEFAULT_DATA_TYPE, lastColumn));
    }
}
