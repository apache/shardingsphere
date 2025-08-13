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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.ddl;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.constant.EncryptColumnDataType;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.CipherColumnItem;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.ColumnDefinitionToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.SubstituteColumnDefinitionToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Create table token generator for encrypt.
 */
@RequiredArgsConstructor
public final class EncryptCreateTableTokenGenerator implements CollectionSQLTokenGenerator<CommonSQLStatementContext> {
    
    private final EncryptRule rule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof CreateTableStatement && !((CreateTableStatement) sqlStatementContext.getSqlStatement()).getColumnDefinitions().isEmpty();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final CommonSQLStatementContext sqlStatementContext) {
        Collection<SQLToken> result = new LinkedList<>();
        CreateTableStatement sqlStatement = (CreateTableStatement) sqlStatementContext.getSqlStatement();
        String tableName = sqlStatement.getTable().getTableName().getIdentifier().getValue();
        EncryptTable encryptTable = rule.getEncryptTable(tableName);
        List<ColumnDefinitionSegment> columns = new ArrayList<>(sqlStatement.getColumnDefinitions());
        for (int index = 0; index < columns.size(); index++) {
            ColumnDefinitionSegment each = columns.get(index);
            String columnName = each.getColumnName().getIdentifier().getValue();
            if (encryptTable.isEncryptColumn(columnName)) {
                result.add(getSubstituteColumnToken(encryptTable.getEncryptColumn(columnName), each, columns, index));
            }
        }
        return result;
    }
    
    private SQLToken getSubstituteColumnToken(final EncryptColumn encryptColumn, final ColumnDefinitionSegment column, final List<ColumnDefinitionSegment> columns, final int index) {
        Collection<SQLToken> columnDefinitionTokens = new LinkedList<>();
        columnDefinitionTokens.add(getCipherColumnToken(encryptColumn, column));
        getAssistedQueryColumnToken(encryptColumn, column).ifPresent(columnDefinitionTokens::add);
        getLikeQueryColumnToken(encryptColumn, column).ifPresent(columnDefinitionTokens::add);
        boolean lastColumn = columns.size() - 1 == index;
        int columnStopIndex = lastColumn ? column.getStopIndex() : columns.get(index + 1).getStartIndex() - 1;
        return new SubstituteColumnDefinitionToken(column.getStartIndex(), columnStopIndex, lastColumn, columnDefinitionTokens);
    }
    
    private SQLToken getCipherColumnToken(final EncryptColumn encryptColumn, final ColumnDefinitionSegment column) {
        CipherColumnItem cipherColumnItem = encryptColumn.getCipher();
        return new ColumnDefinitionToken(cipherColumnItem.getName(), EncryptColumnDataType.DEFAULT_DATA_TYPE, column.getStartIndex());
    }
    
    private Optional<? extends SQLToken> getAssistedQueryColumnToken(final EncryptColumn encryptColumn, final ColumnDefinitionSegment column) {
        return encryptColumn.getAssistedQuery()
                .map(optional -> new ColumnDefinitionToken(encryptColumn.getAssistedQuery().get().getName(), EncryptColumnDataType.DEFAULT_DATA_TYPE, column.getStartIndex()));
    }
    
    private Optional<? extends SQLToken> getLikeQueryColumnToken(final EncryptColumn encryptColumn, final ColumnDefinitionSegment column) {
        return encryptColumn.getLikeQuery()
                .map(optional -> new ColumnDefinitionToken(encryptColumn.getLikeQuery().get().getName(), EncryptColumnDataType.DEFAULT_DATA_TYPE, column.getStartIndex()));
    }
}
