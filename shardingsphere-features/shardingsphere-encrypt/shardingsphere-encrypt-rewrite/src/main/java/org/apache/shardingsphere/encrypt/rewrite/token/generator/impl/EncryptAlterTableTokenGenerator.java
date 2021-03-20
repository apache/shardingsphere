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
    public Collection<EncryptAlterTableToken> generateSQLTokens(final AlterTableStatementContext alterTableStatementContext) {
        Collection<EncryptAlterTableToken> result = new LinkedList<>();
        String tableName = alterTableStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        addColumns(tableName, alterTableStatementContext.getSqlStatement().getAddColumnDefinitions(), "ADD COLUMN", result);
        modifyColumns(tableName, alterTableStatementContext.getSqlStatement().getModifyColumnDefinitions(), "MODIFY COLUMN", result);
        dropColumns(tableName, alterTableStatementContext.getSqlStatement().getDropColumnDefinitions(), "DROP COLUMN", result);
        return result;
    }

    private void addColumns(final String tableName, final Collection<AddColumnDefinitionSegment> columnDefinitionSegments,
                            final String operationType, final Collection<EncryptAlterTableToken> result) {
        for (AddColumnDefinitionSegment addColumnDefinitionSegment : columnDefinitionSegments) {
            for (ColumnDefinitionSegment each : addColumnDefinitionSegment.getColumnDefinitions()) {
                String columnName = each.getColumnName().getIdentifier().getValue();
                Optional<EncryptAlgorithm> encryptor = getEncryptRule().findEncryptor(tableName, columnName);
                if (encryptor.isPresent()) {
                    if (result.isEmpty()) {
                        result.add(new EncryptAlterTableToken(addColumnDefinitionSegment.getStartIndex() - 1, each.getStopIndex() + 1, "", ""));
                    }
                    addPlainColumn(tableName, columnName, result.size() == 1 ? operationType : ", " + operationType, each).ifPresent(result::add);
                    addAssistedQueryColumn(tableName, columnName, result.size() == 1 ? operationType : ", " + operationType, each).ifPresent(result::add);
                    addCipherColumn(tableName, columnName, result.size() == 1 ? operationType : ", " + operationType, each).ifPresent(result::add);
                }
            }
        }
    }

    private void modifyColumns(final String tableName, final Collection<ModifyColumnDefinitionSegment> columnDefinitionSegments,
                               final String operationType, final Collection<EncryptAlterTableToken> result) {
        for (ModifyColumnDefinitionSegment modifyColumnDefinitionSegment : columnDefinitionSegments) {
            ColumnDefinitionSegment segment = modifyColumnDefinitionSegment.getColumnDefinition();
            String columnName = segment.getColumnName().getIdentifier().getValue();
            Optional<EncryptAlgorithm> encryptor = getEncryptRule().findEncryptor(tableName, columnName);
            if (encryptor.isPresent()) {
                if (result.isEmpty()) {
                    result.add(new EncryptAlterTableToken(modifyColumnDefinitionSegment.getStartIndex() - 1, modifyColumnDefinitionSegment.getStopIndex(), "", ""));
                }
                addPlainColumn(tableName, columnName, result.size() == 1 ? operationType : ", " + operationType, modifyColumnDefinitionSegment, segment).ifPresent(result::add);
                addAssistedQueryColumn(tableName, columnName, result.size() == 1 ? operationType : ", " + operationType, modifyColumnDefinitionSegment, segment).ifPresent(result::add);
                addCipherColumn(tableName, columnName, result.size() == 1 ? operationType : ", " + operationType, modifyColumnDefinitionSegment, segment).ifPresent(result::add);
            }
        }
    }

    private void dropColumns(final String tableName, final Collection<DropColumnDefinitionSegment> columnDefinitionSegments,
                             final String operationType, final Collection<EncryptAlterTableToken> result) {
        for (DropColumnDefinitionSegment dropColumnDefinitionSegment : columnDefinitionSegments) {
            for (ColumnSegment each : dropColumnDefinitionSegment.getColumns()) {
                String columnName = each.getQualifiedName();
                Optional<EncryptAlgorithm> encryptor = getEncryptRule().findEncryptor(tableName, columnName);
                if (encryptor.isPresent()) {
                    if (result.isEmpty()) {
                        result.add(new EncryptAlterTableToken(dropColumnDefinitionSegment.getStartIndex() - 1, each.getStopIndex() + 1, "", ""));
                    }
                    addPlainColumn(tableName, columnName, result.size() == 1 ? operationType : ", " + operationType, each).ifPresent(result::add);
                    addAssistedQueryColumn(tableName, columnName, result.size() == 1 ? operationType : ", " + operationType, each).ifPresent(result::add);
                    addCipherColumn(tableName, columnName, result.size() == 1 ? operationType : ", " + operationType, each).ifPresent(result::add);
                }
            }
        }
    }

    private Optional<EncryptAlterTableToken> addPlainColumn(final String tableName, final String columnName, final String operationType, final ColumnDefinitionSegment columnDefinitionSegment) {
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, columnName);
        return plainColumn.map(plainColumnName -> new EncryptAlterTableToken(
                columnDefinitionSegment.getStopIndex() + 1,
                columnDefinitionSegment.getStartIndex() + columnName.length(),
                plainColumnName,
                operationType
        ));
    }

    private Optional<EncryptAlterTableToken> addPlainColumn(final String tableName, final String columnName,
                                                            final String operationType, final ModifyColumnDefinitionSegment columnDefinitionSegment, final ColumnDefinitionSegment segment) {
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, columnName);
        return plainColumn.map(plainColumnName -> new EncryptAlterTableToken(
                columnDefinitionSegment.getStopIndex() + 1,
                segment.getDataType().getStartIndex() - 1,
                plainColumnName,
                operationType
        ));
    }

    private Optional<EncryptAlterTableToken> addPlainColumn(final String tableName, final String columnName, final String operationType, final ColumnSegment columnSegment) {
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, columnName);
        return plainColumn.map(plainColumnName -> new EncryptAlterTableToken(
                columnSegment.getStopIndex() + 1,
                columnSegment.getStartIndex() + columnName.length(),
                plainColumnName,
                operationType
        ));
    }

    private Optional<EncryptAlterTableToken> addAssistedQueryColumn(final String tableName, final String columnName,
                                                                    final String operationType, final ColumnDefinitionSegment columnDefinitionSegment) {
        Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(tableName, columnName);
        return assistedQueryColumn.map(assistedQueryColumnName -> new EncryptAlterTableToken(
                columnDefinitionSegment.getStopIndex() + 1,
                columnDefinitionSegment.getStartIndex() + columnName.length(),
                assistedQueryColumnName,
                operationType
        ));
    }

    private Optional<EncryptAlterTableToken> addAssistedQueryColumn(final String tableName, final String columnName,
                                                                    final String operationType, final ModifyColumnDefinitionSegment columnDefinitionSegment, final ColumnDefinitionSegment segment) {
        Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(tableName, columnName);
        return assistedQueryColumn.map(assistedQueryColumnName -> new EncryptAlterTableToken(
                columnDefinitionSegment.getStopIndex() + 1,
                segment.getDataType().getStartIndex() - 1,
                assistedQueryColumnName,
                operationType
        ));
    }

    private Optional<EncryptAlterTableToken> addAssistedQueryColumn(final String tableName, final String columnName,
                                                                    final String operationType, final ColumnSegment columnSegment) {
        Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(tableName, columnName);
        return assistedQueryColumn.map(assistedQueryColumnName -> new EncryptAlterTableToken(
                columnSegment.getStopIndex() + 1,
                columnSegment.getStartIndex() + columnName.length(),
                assistedQueryColumnName,
                operationType
        ));
    }

    private Optional<EncryptAlterTableToken> addCipherColumn(final String tableName, final String columnName,
                                                             final String operationType, final ColumnDefinitionSegment columnDefinitionSegment) {
        String cipherColumn = getEncryptRule().getCipherColumn(tableName, columnName);
        return Optional.of(new EncryptAlterTableToken(
                columnDefinitionSegment.getStopIndex() + 1,
                columnDefinitionSegment.getStartIndex() + columnName.length(),
                cipherColumn,
                operationType
        ));
    }

    private Optional<EncryptAlterTableToken> addCipherColumn(final String tableName, final String columnName,
                                                             final String operationType, final ModifyColumnDefinitionSegment columnDefinitionSegment, final ColumnDefinitionSegment segment) {
        String cipherColumn = getEncryptRule().getCipherColumn(tableName, columnName);
        return Optional.of(new EncryptAlterTableToken(
                columnDefinitionSegment.getStopIndex() + 1,
                segment.getDataType().getStartIndex() - 1,
                cipherColumn,
                operationType
        ));
    }

    private Optional<EncryptAlterTableToken> addCipherColumn(final String tableName, final String columnName,
                                                             final String operationType, final ColumnSegment columnSegment) {
        String cipherColumn = getEncryptRule().getCipherColumn(tableName, columnName);
        return Optional.of(new EncryptAlterTableToken(
                columnSegment.getStopIndex() + 1,
                columnSegment.getStartIndex() + columnName.length(),
                cipherColumn,
                operationType
        ));
    }
}
