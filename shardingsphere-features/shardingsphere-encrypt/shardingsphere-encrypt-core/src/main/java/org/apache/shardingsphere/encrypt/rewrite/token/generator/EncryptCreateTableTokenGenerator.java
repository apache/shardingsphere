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
import org.apache.shardingsphere.encrypt.rule.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptConfigDataTypeToken;
import org.apache.shardingsphere.encrypt.rule.EncryptColumn;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        List<ColumnDefinitionSegment> columns = new ArrayList<>(createTableStatementContext.getSqlStatement().getColumnDefinitions());
        for (int index = 0; index < columns.size(); index++) {
            ColumnDefinitionSegment each = columns.get(index);
            String columnName = each.getColumnName().getIdentifier().getValue();
            Optional<EncryptAlgorithm> encryptor = encryptRule.findEncryptor(tableName, columnName);
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
    
    private SQLToken getCipherColumnToken(final String tableName, final String columnName, final ColumnDefinitionSegment column, final int stopIndex) {
        Optional<EncryptConfigDataTypeToken> configDataTypeToken = findEncryptConfigDataTypeToken(tableName, columnName, column, stopIndex);
        return configDataTypeToken.isPresent() ? configDataTypeToken.get() : new SubstitutableColumnNameToken(stopIndex + 1, column.getColumnName().getStopIndex(), 
                getColumnProjections(encryptRule.getCipherColumn(tableName, columnName)));
    }
    
    private Optional<EncryptConfigDataTypeToken> findEncryptConfigDataTypeToken(final String tableName, final String columnName, final ColumnDefinitionSegment column, final int stopIndex) {
        Optional<EncryptColumn> encryptColumn = encryptRule.findEncryptTable(tableName).flatMap(encryptTable -> encryptTable.findEncryptColumn(columnName));
        if (encryptColumn.isPresent() && null != encryptColumn.get().getCipherDataType()) {
            return Optional.of(new EncryptConfigDataTypeToken(stopIndex + 1, column.getStopIndex(), encryptColumn.get().getCipherColumn(), encryptColumn.get().getCipherDataType().getTypeName()));
        }
        return Optional.empty();
    }
    
    private Optional<? extends SQLToken> getAssistedQueryColumnToken(final String tableName, final String columnName, final ColumnDefinitionSegment column, 
                                                                               final int stopIndex, final boolean lastColumn) {
        Optional<EncryptConfigDataTypeToken> encryptConfigDataTypeToken = findAssistedConfigDataTypeToken(tableName, columnName, column, stopIndex, lastColumn);
        if (encryptConfigDataTypeToken.isPresent()) {
            return encryptConfigDataTypeToken;
        } 
        Optional<String> assistedQueryColumn = encryptRule.findAssistedQueryColumn(tableName, columnName);
        return assistedQueryColumn.map(optional -> new SubstitutableColumnNameToken(stopIndex + 1, column.getColumnName().getStopIndex(), getColumnProjections(optional), lastColumn));
    }
    
    private Optional<EncryptConfigDataTypeToken> findAssistedConfigDataTypeToken(final String tableName, final String columnName, final ColumnDefinitionSegment column, 
                                                                                 final int stopIndex, final boolean lastColumn) {
        Optional<EncryptColumn> encryptColumn = encryptRule.findEncryptTable(tableName).flatMap(encryptTable -> encryptTable.findEncryptColumn(columnName));
        Optional<String> assistedQueryColumn = encryptColumn.flatMap(EncryptColumn::getAssistedQueryColumn);
        if (assistedQueryColumn.isPresent()) {
            if (null != encryptColumn.get().getAssistedQueryDataType()) {
                return Optional.of(new EncryptConfigDataTypeToken(stopIndex + 1, column.getStopIndex(), assistedQueryColumn.get(), 
                        encryptColumn.get().getAssistedQueryDataType().getTypeName(), lastColumn));
            }
        }
        return Optional.empty();
    }
    
    private Optional<? extends SQLToken> getPlainColumnToken(final String tableName, final String columnName, final ColumnDefinitionSegment column, 
                                                                       final int stopIndex, final boolean lastColumn) {
        Optional<EncryptConfigDataTypeToken> encryptConfigDataTypeToken = findPlainConfigDataTypeToken(tableName, columnName, column, stopIndex, lastColumn);
        if (encryptConfigDataTypeToken.isPresent()) {
            return encryptConfigDataTypeToken;
        }
        Optional<String> plainColumn = encryptRule.findPlainColumn(tableName, columnName);
        return plainColumn.map(optional -> new SubstitutableColumnNameToken(stopIndex + 1, column.getColumnName().getStopIndex(), getColumnProjections(optional), lastColumn));
    }
    
    private Optional<EncryptConfigDataTypeToken> findPlainConfigDataTypeToken(final String tableName, final String columnName, final ColumnDefinitionSegment column, 
                                                                              final int stopIndex, final boolean lastColumn) {
        Optional<EncryptColumn> encryptColumn = encryptRule.findEncryptTable(tableName).flatMap(encryptTable -> encryptTable.findEncryptColumn(columnName));
        Optional<String> plainColumn = encryptColumn.flatMap(EncryptColumn::getPlainColumn);
        if (plainColumn.isPresent()) {
            if (null != encryptColumn.get().getPlainDataType()) {
                return Optional.of(new EncryptConfigDataTypeToken(stopIndex + 1, column.getStopIndex(), plainColumn.get(),
                        encryptColumn.get().getPlainDataType().getTypeName(), lastColumn));
            }
        }
        return Optional.empty();
    }
    
    private Collection<ColumnProjection> getColumnProjections(final String columnName) {
        return Collections.singletonList(new ColumnProjection(null, columnName, null));
    }
}
