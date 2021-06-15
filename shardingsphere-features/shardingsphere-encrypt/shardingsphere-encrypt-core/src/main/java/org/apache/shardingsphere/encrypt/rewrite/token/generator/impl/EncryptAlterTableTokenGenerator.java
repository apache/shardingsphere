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

import org.apache.shardingsphere.encrypt.rewrite.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAlterTableToken;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Create table token generator for encrypt.
 */
public final class EncryptAlterTableTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator<AlterTableStatementContext> {
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof AlterTableStatementContext;
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final AlterTableStatementContext alterTableStatementContext) {
        String tableName = alterTableStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        Collection<SQLToken> result = new LinkedList<>(getAddColumnTokens(tableName, alterTableStatementContext.getSqlStatement().getAddColumnDefinitions()));
        result.addAll(getModifyColumnTokens(tableName, alterTableStatementContext.getSqlStatement().getModifyColumnDefinitions()));
        result.addAll(getDropColumnTokens(tableName, alterTableStatementContext.getSqlStatement().getDropColumnDefinitions()));
        return result;
    }
    
    private Collection<SQLToken> getAddColumnTokens(final String tableName, final Collection<AddColumnDefinitionSegment> columnDefinitionSegments) {
        Collection<SQLToken> result = new LinkedList<>();
        for (AddColumnDefinitionSegment each : columnDefinitionSegments) {
            result.addAll(getAddColumnTokens(tableName, each));
        }
        return result;
    }
    
    private Collection<SQLToken> getAddColumnTokens(final String tableName, final AddColumnDefinitionSegment addColumnDefinitionSegment) {
        Collection<SQLToken> result = new LinkedList<>();
        for (ColumnDefinitionSegment each : addColumnDefinitionSegment.getColumnDefinitions()) {
            String columnName = each.getColumnName().getIdentifier().getValue();
            Optional<EncryptAlgorithm> encryptor = getEncryptRule().findEncryptor(tableName, columnName);
            if (encryptor.isPresent()) {
                result.addAll(getAddColumnTokens(tableName, columnName, addColumnDefinitionSegment, each));
            }
        }
        return result;
    }
    
    private Collection<SQLToken> getAddColumnTokens(final String tableName, final String columnName, 
                                                    final AddColumnDefinitionSegment addColumnDefinitionSegment, final ColumnDefinitionSegment columnDefinitionSegment) {
        Collection<SQLToken> result = new LinkedList<>();
        result.add(new RemoveToken(addColumnDefinitionSegment.getStartIndex() - 1, columnDefinitionSegment.getStopIndex() + 1));
        result.add(getCipherColumn(tableName, columnName, columnDefinitionSegment));
        getAssistedQueryColumn(tableName, columnName, columnDefinitionSegment).ifPresent(result::add);
        getPlainColumn(tableName, columnName, columnDefinitionSegment).ifPresent(result::add);
        return result;
    }
    
    private Collection<SQLToken> getModifyColumnTokens(final String tableName, final Collection<ModifyColumnDefinitionSegment> columnDefinitionSegments) {
        Collection<SQLToken> result = new LinkedList<>();
        for (ModifyColumnDefinitionSegment each : columnDefinitionSegments) {
            ColumnDefinitionSegment segment = each.getColumnDefinition();
            String columnName = segment.getColumnName().getIdentifier().getValue();
            Optional<EncryptAlgorithm> encryptor = getEncryptRule().findEncryptor(tableName, columnName);
            if (encryptor.isPresent()) {
                result.addAll(getModifyColumnTokens(tableName, columnName, each, segment));
            }
        }
        return result;
    }
    
    private Collection<SQLToken> getModifyColumnTokens(final String tableName, final String columnName, 
                                                       final ModifyColumnDefinitionSegment modifyColumnDefinitionSegment, final ColumnDefinitionSegment columnDefinitionSegment) {
        Collection<SQLToken> result = new LinkedList<>();
        result.add(new RemoveToken(modifyColumnDefinitionSegment.getStartIndex() - 1, modifyColumnDefinitionSegment.getStopIndex()));
        result.add(getCipherColumn(tableName, columnName, modifyColumnDefinitionSegment, columnDefinitionSegment));
        getAssistedQueryColumn(tableName, columnName, modifyColumnDefinitionSegment, columnDefinitionSegment).ifPresent(result::add);
        getPlainColumn(tableName, columnName, modifyColumnDefinitionSegment, columnDefinitionSegment).ifPresent(result::add);
        return result;
    }
    
    private Collection<SQLToken> getDropColumnTokens(final String tableName, final Collection<DropColumnDefinitionSegment> columnDefinitionSegments) {
        Collection<SQLToken> result = new LinkedList<>();
        for (DropColumnDefinitionSegment each : columnDefinitionSegments) {
            result.addAll(getDropColumnTokens(tableName, each));
        }
        return result;
    }
    
    private Collection<SQLToken> getDropColumnTokens(final String tableName, final DropColumnDefinitionSegment dropColumnDefinitionSegment) {
        Collection<SQLToken> result = new LinkedList<>();
        for (ColumnSegment each : dropColumnDefinitionSegment.getColumns()) {
            String columnName = each.getQualifiedName();
            Optional<EncryptAlgorithm> encryptor = getEncryptRule().findEncryptor(tableName, columnName);
            if (encryptor.isPresent()) {
                result.addAll(getDropColumnTokens(tableName, columnName, each, dropColumnDefinitionSegment));
            }
        }
        return result;
    }
    
    private Collection<SQLToken> getDropColumnTokens(final String tableName, final String columnName, 
                                                     final ColumnSegment columnSegment, final DropColumnDefinitionSegment dropColumnDefinitionSegment) {
        Collection<SQLToken> result = new LinkedList<>();
        result.add(new RemoveToken(dropColumnDefinitionSegment.getStartIndex() - 1, columnSegment.getStopIndex()));
        result.add(getCipherColumn(tableName, columnName, columnSegment));
        getAssistedQueryColumn(tableName, columnName, columnSegment).ifPresent(result::add);
        getPlainColumn(tableName, columnName, columnSegment).ifPresent(result::add);
        return result;
    }
    
    private EncryptAlterTableToken getCipherColumn(final String tableName, final String columnName, final ColumnDefinitionSegment columnDefinitionSegment) {
        String cipherColumn = getEncryptRule().getCipherColumn(tableName, columnName);
        return new EncryptAlterTableToken(columnDefinitionSegment.getStopIndex() + 1, columnDefinitionSegment.getStartIndex() + columnName.length(), cipherColumn, "ADD COLUMN");
    }
    
    private EncryptAlterTableToken getCipherColumn(final String tableName, final String columnName,
                                                   final ModifyColumnDefinitionSegment modifyColumnDefinitionSegment, final ColumnDefinitionSegment columnDefinitionSegment) {
        String cipherColumn = getEncryptRule().getCipherColumn(tableName, columnName);
        return new EncryptAlterTableToken(modifyColumnDefinitionSegment.getStopIndex() + 1, columnDefinitionSegment.getDataType().getStartIndex() - 1, cipherColumn, "MODIFY COLUMN");
    }
    
    private EncryptAlterTableToken getCipherColumn(final String tableName, final String columnName, final ColumnSegment columnSegment) {
        String cipherColumn = getEncryptRule().getCipherColumn(tableName, columnName);
        return new EncryptAlterTableToken(columnSegment.getStopIndex() + 1, columnSegment.getStopIndex(), cipherColumn, "DROP COLUMN");
    }
    
    private Optional<EncryptAlterTableToken> getAssistedQueryColumn(final String tableName, final String columnName, final ColumnDefinitionSegment columnDefinitionSegment) {
        Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(tableName, columnName);
        return assistedQueryColumn.map(optional -> new EncryptAlterTableToken(
                columnDefinitionSegment.getStopIndex() + 1, columnDefinitionSegment.getStartIndex() + columnName.length(), optional, ", ADD COLUMN"));
    }
    
    private Optional<EncryptAlterTableToken> getAssistedQueryColumn(final String tableName, final String columnName,
                                                                    final ModifyColumnDefinitionSegment modifyColumnDefinitionSegment, final ColumnDefinitionSegment columnDefinitionSegment) {
        Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(tableName, columnName);
        return assistedQueryColumn.map(optional -> new EncryptAlterTableToken(
                modifyColumnDefinitionSegment.getStopIndex() + 1, columnDefinitionSegment.getDataType().getStartIndex() - 1, optional, ", MODIFY COLUMN"));
    }
    
    private Optional<EncryptAlterTableToken> getAssistedQueryColumn(final String tableName, final String columnName, final ColumnSegment columnSegment) {
        Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(tableName, columnName);
        return assistedQueryColumn.map(optional -> new EncryptAlterTableToken(columnSegment.getStopIndex() + 1, columnSegment.getStopIndex(), optional, ", DROP COLUMN"));
    }
    
    private Optional<EncryptAlterTableToken> getPlainColumn(final String tableName, final String columnName, final ColumnDefinitionSegment columnDefinitionSegment) {
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, columnName);
        return plainColumn.map(optional -> new EncryptAlterTableToken(
                columnDefinitionSegment.getStopIndex() + 1, columnDefinitionSegment.getStartIndex() + columnName.length(), optional, ", ADD COLUMN"));
    }
    
    private Optional<EncryptAlterTableToken> getPlainColumn(final String tableName, final String columnName,
                                                            final ModifyColumnDefinitionSegment modifyColumnDefinitionSegment, final ColumnDefinitionSegment columnDefinitionSegment) {
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, columnName);
        return plainColumn.map(optional -> new EncryptAlterTableToken(
                modifyColumnDefinitionSegment.getStopIndex() + 1, columnDefinitionSegment.getDataType().getStartIndex() - 1, optional, ", MODIFY COLUMN"));
    }
    
    private Optional<EncryptAlterTableToken> getPlainColumn(final String tableName, final String columnName, final ColumnSegment columnSegment) {
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, columnName);
        return plainColumn.map(optional -> new EncryptAlterTableToken(columnSegment.getStopIndex() + 1, columnSegment.getStopIndex(), optional, ", DROP COLUMN"));
    }
}
