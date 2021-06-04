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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.impl;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Create table token generator for encrypt.
 */
public final class EncryptCreateTableTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator<CreateTableStatementContext> {
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof CreateTableStatementContext && !(((CreateTableStatementContext) sqlStatementContext).getSqlStatement()).getColumnDefinitions().isEmpty();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final CreateTableStatementContext createTableStatementContext) {
        Collection<SQLToken> result = new LinkedList<>();
        String tableName = createTableStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        List<ColumnDefinitionSegment> columns = Lists.newArrayList(createTableStatementContext.getSqlStatement().getColumnDefinitions());
        for (int index = 0; index < columns.size(); index++) {
            ColumnDefinitionSegment each = columns.get(index);
            String columnName = each.getColumnName().getIdentifier().getValue();
            Optional<EncryptAlgorithm> encryptor = getEncryptRule().findEncryptor(tableName, columnName);
            if (encryptor.isPresent()) {
                result.addAll(getColumnTokens(tableName, columnName, each, columns, index));
            }
        }
        return result;
    }
    
    private Collection<SQLToken> getColumnTokens(final String tableName, final String columnName, final ColumnDefinitionSegment column, 
                                                 final List<ColumnDefinitionSegment> columns, final int index) {
        boolean lastColumn = columns.size() - 1 == index;
        int columnStopIndex = lastColumn ? column.getStopIndex() : columns.get(index + 1).getStartIndex() - 1;
        Collection<SQLToken> result = new LinkedList<>();
        result.add(new RemoveToken(column.getStartIndex(), columnStopIndex));
        result.add(getCipherColumnToken(tableName, columnName, column, columnStopIndex));
        getAssistedQueryColumnToken(tableName, columnName, column, columnStopIndex, lastColumn).ifPresent(result::add);
        getPlainColumnToken(tableName, columnName, column, columnStopIndex, lastColumn).ifPresent(result::add);
        return result;
    }
    
    private SubstitutableColumnNameToken getCipherColumnToken(final String tableName, final String columnName, final ColumnDefinitionSegment column, final int stopIndex) {
        String cipherColumn = getEncryptRule().getCipherColumn(tableName, columnName);
        return new SubstitutableColumnNameToken(stopIndex + 1, column.getColumnName().getStopIndex(), cipherColumn);
    }
    
    private Optional<SubstitutableColumnNameToken> getAssistedQueryColumnToken(final String tableName, final String columnName, final ColumnDefinitionSegment column, 
                                                                               final int stopIndex, final boolean lastColumn) {
        Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(tableName, columnName);
        return assistedQueryColumn.map(optional -> new SubstitutableColumnNameToken(stopIndex + 1, column.getColumnName().getStopIndex(), optional, lastColumn));
    }
    
    private Optional<SubstitutableColumnNameToken> getPlainColumnToken(final String tableName, final String columnName, final ColumnDefinitionSegment column, 
                                                                       final int stopIndex, final boolean lastColumn) {
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, columnName);
        return plainColumn.map(optional -> new SubstitutableColumnNameToken(stopIndex + 1, column.getColumnName().getStopIndex(), optional, lastColumn));
    }
}
