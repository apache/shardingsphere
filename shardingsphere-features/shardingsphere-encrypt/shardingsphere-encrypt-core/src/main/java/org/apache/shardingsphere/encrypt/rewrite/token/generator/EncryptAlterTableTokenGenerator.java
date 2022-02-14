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
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAlterTableToken;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptConfigDataTypeToken;
import org.apache.shardingsphere.encrypt.rule.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.Substitutable;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ChangeColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.position.ColumnPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Alter table token generator for encrypt.
 */
@Setter
public final class EncryptAlterTableTokenGenerator implements CollectionSQLTokenGenerator<AlterTableStatementContext>, EncryptRuleAware {
    
    private EncryptRule encryptRule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof AlterTableStatementContext;
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final AlterTableStatementContext alterTableStatementContext) {
        String tableName = alterTableStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        Collection<SQLToken> result = new LinkedList<>(getAddColumnTokens(tableName, alterTableStatementContext.getSqlStatement().getAddColumnDefinitions()));
        result.addAll(getModifyColumnTokens(tableName, alterTableStatementContext.getSqlStatement().getModifyColumnDefinitions()));
        result.addAll(getChangeColumnTokens(tableName, alterTableStatementContext.getSqlStatement().getChangeColumnDefinitions()));
        Collection<SQLToken> dropCollection = getDropColumnTokens(tableName, alterTableStatementContext.getSqlStatement().getDropColumnDefinitions());
        String databaseName = alterTableStatementContext.getDatabaseType().getName();
        if ("SQLServer".equals(databaseName)) {
            result.addAll(mergeDropColumnStatement(dropCollection, "", ""));
        } else if ("Oracle".equals(databaseName)) {
            result.addAll(mergeDropColumnStatement(dropCollection, "(", ")"));
        } else {
            result.addAll(dropCollection);
        }
        return result;
    }
    
    private Collection<SQLToken> mergeDropColumnStatement(final Collection<SQLToken> dropCollection, final String leftJoiner, final String rightJoiner) {
        Collection<SQLToken> result = new LinkedList<>();
        ArrayList<String> dropColumnList = new ArrayList<>();
        int lastStartIndex = -1;
        for (int i = 0; i < dropCollection.size(); i++) {
            SQLToken token = (SQLToken) ((List) dropCollection).get(i);
            if (token instanceof RemoveToken) {
                if (i != 0) {
                    result.add(new RemoveToken(lastStartIndex, ((RemoveToken) token).getStopIndex()));
                } else {
                    result.add(token);
                }
            } else {
                EncryptAlterTableToken encryptAlterTableToken = (EncryptAlterTableToken) token;
                dropColumnList.add(encryptAlterTableToken.getColumnName());
                if (i == dropCollection.size() - 1) {
                    result.add(new EncryptAlterTableToken(token.getStartIndex(), encryptAlterTableToken.getStopIndex(),
                            leftJoiner + String.join(",", dropColumnList) + rightJoiner, "DROP COLUMN"));
                }
            }
            lastStartIndex = ((Substitutable) token).getStartIndex();
        }
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
            Optional<EncryptAlgorithm> encryptor = encryptRule.findEncryptor(tableName, columnName);
            if (encryptor.isPresent()) {
                result.addAll(getAddColumnTokens(tableName, columnName, addColumnDefinitionSegment, each));
            }
        }
        getAddColumnPositionToken(tableName, addColumnDefinitionSegment).ifPresent(result::add);
        return result;
    }
    
    private Collection<SQLToken> getAddColumnTokens(final String tableName, final String columnName,
                                                    final AddColumnDefinitionSegment addColumnDefinitionSegment, final ColumnDefinitionSegment columnDefinitionSegment) {
        return encryptRule.containsConfigDataType(tableName, columnName) ? getAddColumnTokensWithConfigDataType(tableName, columnName, addColumnDefinitionSegment, columnDefinitionSegment) 
                : getAddColumnTokensByDefault(tableName, columnName, addColumnDefinitionSegment, columnDefinitionSegment);
    }
    
    private Collection<SQLToken> getAddColumnTokensByDefault(final String tableName, final String columnName, final AddColumnDefinitionSegment addColumnDefinitionSegment, 
                                                             final ColumnDefinitionSegment columnDefinitionSegment) {
        Collection<SQLToken> result = new LinkedList<>();
        result.add(new RemoveToken(columnDefinitionSegment.getStartIndex(), columnDefinitionSegment.getColumnName().getStopIndex()));
        result.add(new EncryptAlterTableToken(columnDefinitionSegment.getColumnName().getStopIndex() + 1, columnDefinitionSegment.getColumnName().getStopIndex(),
                encryptRule.getCipherColumn(tableName, columnName), null));
        Optional<String> assistedQueryColumn = encryptRule.findAssistedQueryColumn(tableName, columnName);
        assistedQueryColumn.map(optional -> new EncryptAlterTableToken(
                addColumnDefinitionSegment.getStopIndex() + 1, columnDefinitionSegment.getColumnName().getStopIndex(), optional, ", ADD COLUMN")).ifPresent(result::add);
        Optional<String> plainColumn = encryptRule.findPlainColumn(tableName, columnName);
        plainColumn.map(optional -> new EncryptAlterTableToken(
                addColumnDefinitionSegment.getStopIndex() + 1, columnDefinitionSegment.getColumnName().getStopIndex(), optional, ", ADD COLUMN")).ifPresent(result::add);
        return result;
    }
    
    private Collection<SQLToken> getAddColumnTokensWithConfigDataType(final String tableName, final String columnName, final AddColumnDefinitionSegment addColumnDefinitionSegment, 
                                                                      final ColumnDefinitionSegment columnDefinitionSegment) {
        Collection<SQLToken> result = new LinkedList<>();
        EncryptColumn encryptColumn = getEncryptColumn(tableName, columnName);
        result.add(new RemoveToken(columnDefinitionSegment.getStartIndex(), columnDefinitionSegment.getStopIndex()));
        result.add(new EncryptConfigDataTypeToken(columnDefinitionSegment.getStopIndex() + 1, columnDefinitionSegment.getStopIndex(),
                encryptRule.getCipherColumn(tableName, columnName), encryptColumn.getCipherDataType().getTypeName()));
        Optional<String> assistedQueryColumn = encryptRule.findAssistedQueryColumn(tableName, columnName);
        assistedQueryColumn.map(assisted -> new EncryptConfigDataTypeToken(addColumnDefinitionSegment.getStopIndex() + 1, 
                addColumnDefinitionSegment.getStopIndex(), ", ADD COLUMN " + assisted, encryptColumn.getAssistedQueryDataType().getTypeName())).ifPresent(result::add);
        Optional<String> plainColumn = encryptRule.findPlainColumn(tableName, columnName);
        plainColumn.map(plain -> new EncryptConfigDataTypeToken(addColumnDefinitionSegment.getStopIndex() + 1, 
                addColumnDefinitionSegment.getStopIndex(), ", ADD COLUMN " + plain, encryptColumn.getPlainDataType().getTypeName())).ifPresent(result::add);
        return result;
    }
    
    private EncryptColumn getEncryptColumn(final String tableName, final String columnName) {
        return encryptRule.findEncryptColumn(tableName, columnName).orElseThrow(() -> new ShardingSphereConfigurationException("Failed finding encrypt column."));
    }
    
    private Optional<SQLToken> getAddColumnPositionToken(final String tableName, final AddColumnDefinitionSegment addColumnDefinitionSegment) {
        Optional<EncryptAlgorithm> encryptor = addColumnDefinitionSegment.getColumnPosition().filter(positionSegment -> null != positionSegment.getColumnName())
                .flatMap(positionSegment -> encryptRule.findEncryptor(tableName, positionSegment.getColumnName().getIdentifier().getValue()));
        if (encryptor.isPresent()) {
            return Optional.of(getPositionColumnToken(addColumnDefinitionSegment.getColumnPosition().get(), tableName));
        }
        return Optional.empty();
    }
    
    private EncryptAlterTableToken getPositionColumnToken(final ColumnPositionSegment positionSegment, final String tableName) {
        return new EncryptAlterTableToken(positionSegment.getColumnName().getStartIndex(), positionSegment.getStopIndex(), encryptRule
                .getCipherColumn(tableName, positionSegment.getColumnName().getIdentifier().getValue()), null);
    }
    
    private Collection<SQLToken> getModifyColumnTokens(final String tableName, final Collection<ModifyColumnDefinitionSegment> columnDefinitionSegments) {
        Collection<SQLToken> result = new LinkedList<>();
        for (ModifyColumnDefinitionSegment each : columnDefinitionSegments) {
            ColumnDefinitionSegment segment = each.getColumnDefinition();
            String columnName = segment.getColumnName().getIdentifier().getValue();
            Optional<EncryptAlgorithm> encryptor = encryptRule.findEncryptor(tableName, columnName);
            if (encryptor.isPresent()) {
                result.addAll(getModifyColumnTokens(tableName, columnName, each));
            }
            each.getColumnPosition().flatMap(columnPositionSegment -> getColumnPositionToken(tableName, columnPositionSegment)).ifPresent(result::add);
        }
        return result;
    }
    
    private Collection<SQLToken> getModifyColumnTokens(final String tableName, final String columnName,
                                                       final ModifyColumnDefinitionSegment modifyColumnDefinitionSegment) {
        return encryptRule.containsConfigDataType(tableName, columnName) ? getModifyColumnTokensWithConfigDataType(tableName, columnName, modifyColumnDefinitionSegment) 
                : getModifyColumnTokensByDefault(tableName, columnName, modifyColumnDefinitionSegment);
    }
    
    private Collection<SQLToken> getModifyColumnTokensWithConfigDataType(final String tableName, final String columnName, final ModifyColumnDefinitionSegment segment) {
        Collection<SQLToken> result = new LinkedList<>();
        ColumnDefinitionSegment columnDefinitionSegment = segment.getColumnDefinition();
        EncryptColumn encryptColumn = getEncryptColumn(tableName, columnName);
        result.add(new RemoveToken(columnDefinitionSegment.getColumnName().getStartIndex(), columnDefinitionSegment.getStopIndex()));
        result.add(new EncryptConfigDataTypeToken(columnDefinitionSegment.getStopIndex() + 1, columnDefinitionSegment.getStopIndex(),
                encryptRule.getCipherColumn(tableName, columnName), encryptColumn.getCipherDataType().getTypeName()));
        encryptRule.findAssistedQueryColumn(tableName, columnName).map(assist -> new EncryptConfigDataTypeToken(segment.getStopIndex() + 1, 
                segment.getStopIndex(), ", MODIFY COLUMN " + assist, encryptColumn.getAssistedQueryDataType().getTypeName())).ifPresent(result::add);
        encryptRule.findPlainColumn(tableName, columnName).map(plain -> new EncryptConfigDataTypeToken(segment.getStopIndex() + 1,
                segment.getStopIndex(), ", MODIFY COLUMN " + plain, encryptColumn.getPlainDataType().getTypeName())).ifPresent(result::add);
        return result;
    }
    
    private Collection<SQLToken> getModifyColumnTokensByDefault(final String tableName, final String columnName, final ModifyColumnDefinitionSegment modifyColumnDefinitionSegment) {
        Collection<SQLToken> result = new LinkedList<>();
        ColumnDefinitionSegment columnDefinitionSegment = modifyColumnDefinitionSegment.getColumnDefinition();
        result.add(new RemoveToken(columnDefinitionSegment.getColumnName().getStartIndex(), columnDefinitionSegment.getColumnName().getStopIndex()));
        result.add(new EncryptAlterTableToken(columnDefinitionSegment.getColumnName().getStopIndex() + 1, columnDefinitionSegment.getColumnName().getStopIndex(),
                encryptRule.getCipherColumn(tableName, columnName), null));
        encryptRule.findAssistedQueryColumn(tableName, columnName).map(optional -> new EncryptAlterTableToken(modifyColumnDefinitionSegment.getStopIndex() + 1,
                columnDefinitionSegment.getColumnName().getStopIndex(), optional, ", MODIFY COLUMN")).ifPresent(result::add);
        encryptRule.findPlainColumn(tableName, columnName).map(optional -> new EncryptAlterTableToken(modifyColumnDefinitionSegment.getStopIndex() + 1,
                columnDefinitionSegment.getColumnName().getStopIndex(), optional, ", MODIFY COLUMN")).ifPresent(result::add);
        return result;
    }
    
    private Optional<SQLToken> getColumnPositionToken(final String tableName, final ColumnPositionSegment columnPositionSegment) {
        Optional<EncryptAlgorithm> encryptor = Optional.of(columnPositionSegment).filter(positionSegment -> null != positionSegment.getColumnName())
                .flatMap(positionSegment -> encryptRule.findEncryptor(tableName, positionSegment.getColumnName().getIdentifier().getValue()));
        if (encryptor.isPresent()) {
            return Optional.of(getPositionColumnToken(columnPositionSegment, tableName));
        }
        return Optional.empty();
    }
    
    private Collection<SQLToken> getChangeColumnTokens(final String tableName, final Collection<ChangeColumnDefinitionSegment> changeColumnDefinitions) {
        Collection<SQLToken> result = new LinkedList<>();
        for (ChangeColumnDefinitionSegment each : changeColumnDefinitions) {
            result.addAll(getChangeColumnTokensEach(tableName, each));
            each.getColumnPosition().flatMap(columnPositionSegment -> getColumnPositionToken(tableName, columnPositionSegment)).ifPresent(result::add);
        }
        return result;
    }
    
    private Collection<? extends SQLToken> getChangeColumnTokensEach(final String tableName, final ChangeColumnDefinitionSegment segment) {
        isSameEncryptColumn(tableName, segment);
        if (!encryptRule.findEncryptor(tableName, segment.getPreviousColumn().getIdentifier().getValue()).isPresent() 
                || !encryptRule.findEncryptor(tableName, segment.getColumnDefinition().getColumnName().getIdentifier().getValue()).isPresent()) {
            return Collections.emptyList();
        }
        Collection<SQLToken> result = new LinkedList<>();
        result.addAll(getPreviousColumnTokens(tableName, segment));
        result.addAll(getColumnTokens(tableName, segment));
        return result;
    }
    
    private void isSameEncryptColumn(final String tableName, final ChangeColumnDefinitionSegment segment) {
        Optional<EncryptAlgorithm> previousAlgorithm = encryptRule.findEncryptor(tableName, segment.getPreviousColumn().getIdentifier().getValue());
        Optional<EncryptAlgorithm> currentAlgorithm = encryptRule.findEncryptor(tableName, segment.getColumnDefinition().getColumnName().getIdentifier().getValue());
        if (!previousAlgorithm.isPresent() && !currentAlgorithm.isPresent()) {
            return;
        }
        if (previousAlgorithm.isPresent() && currentAlgorithm.isPresent() && previousAlgorithm.get().equals(currentAlgorithm.get())) {
            if (checkPreviousAndAfterHasSameColumnNumber(tableName, segment)) {
                return;
            }
        }
        throw new ShardingSphereException("Alter change column statement's columns must both have same encrypt algorithm!");
    }
    
    private boolean checkPreviousAndAfterHasSameColumnNumber(final String tableName, final ChangeColumnDefinitionSegment segment) {
        EncryptColumn previousColumn = getEncryptColumn(tableName, segment.getPreviousColumn().getIdentifier().getValue());
        EncryptColumn currentColumn = getEncryptColumn(tableName, segment.getColumnDefinition().getColumnName().getIdentifier().getValue());
        if (previousColumn.getPlainColumn().isPresent() && !currentColumn.getPlainColumn().isPresent()) {
            return false;
        }
        if (!previousColumn.getPlainColumn().isPresent() && currentColumn.getPlainColumn().isPresent()) {
            return false;
        }
        if (previousColumn.getAssistedQueryColumn().isPresent() && !currentColumn.getAssistedQueryColumn().isPresent()) {
            return false;
        }
        return previousColumn.getAssistedQueryColumn().isPresent() || !currentColumn.getAssistedQueryColumn().isPresent();
    }
    
    private Collection<? extends SQLToken> getColumnTokens(final String tableName, final ChangeColumnDefinitionSegment segment) {
        return encryptRule.containsConfigDataType(tableName, segment.getColumnDefinition().getColumnName().getIdentifier().getValue()) ? getChangeColumnTokensWithConfigDataType(tableName, segment)
                : getChangeColumnTokensByDefault(tableName, segment);
    }
    
    private Collection<? extends SQLToken> getChangeColumnTokensWithConfigDataType(final String tableName, final ChangeColumnDefinitionSegment segment) {
        Collection<SQLToken> result = new LinkedList<>();
        result.add(new RemoveToken(segment.getColumnDefinition().getColumnName().getStartIndex(), segment.getColumnDefinition().getStopIndex()));
        EncryptColumn encryptColumn = getEncryptColumn(tableName, segment.getColumnDefinition().getColumnName().getIdentifier().getValue());
        result.add(new EncryptConfigDataTypeToken(segment.getColumnDefinition().getStopIndex() + 1, segment.getColumnDefinition().getStopIndex(),
                encryptColumn.getCipherColumn(), encryptColumn.getCipherDataType().getTypeName()));
        String previousColumnName = segment.getPreviousColumn().getIdentifier().getValue();
        encryptRule.findAssistedQueryColumn(tableName, previousColumnName).map(optional -> new EncryptConfigDataTypeToken(segment.getStopIndex() + 1, segment.getStopIndex(), 
                ", CHANGE COLUMN " + optional + " " + encryptColumn.getAssistedQueryColumn().orElse(""), encryptColumn.getAssistedQueryDataType().getTypeName())).ifPresent(result::add);
        encryptRule.findPlainColumn(tableName, previousColumnName).map(optional -> new EncryptConfigDataTypeToken(segment.getStopIndex() + 1, segment.getStopIndex(),
                ", CHANGE COLUMN " + optional + " " + encryptColumn.getPlainColumn().orElse(""), encryptColumn.getPlainDataType().getTypeName())).ifPresent(result::add);
        return result;
    }
    
    private Collection<SQLToken> getChangeColumnTokensByDefault(final String tableName, final ChangeColumnDefinitionSegment segment) {
        Collection<SQLToken> result = new LinkedList<>();
        result.add(new RemoveToken(segment.getColumnDefinition().getColumnName().getStartIndex(), segment.getColumnDefinition().getColumnName().getStopIndex()));
        result.add(new EncryptAlterTableToken(segment.getColumnDefinition().getColumnName().getStopIndex() + 1, segment.getColumnDefinition().getColumnName().getStopIndex(),
                encryptRule.getCipherColumn(tableName, segment.getColumnDefinition().getColumnName().getIdentifier().getValue()), null));
        String previousColumnName = segment.getPreviousColumn().getIdentifier().getValue();
        EncryptColumn encryptColumn = getEncryptColumn(tableName, segment.getColumnDefinition().getColumnName().getIdentifier().getValue());
        encryptRule.findAssistedQueryColumn(tableName, previousColumnName).map(optional ->
                new EncryptAlterTableToken(segment.getStopIndex() + 1, segment.getColumnDefinition().getColumnName().getStopIndex(),
                        encryptColumn.getAssistedQueryColumn().orElse(""), ", CHANGE COLUMN " + optional)).ifPresent(result::add);
        encryptRule.findPlainColumn(tableName, previousColumnName).map(optional ->
                new EncryptAlterTableToken(segment.getStopIndex() + 1, segment.getColumnDefinition().getColumnName().getStopIndex(),
                        encryptColumn.getPlainColumn().orElse(""), ", CHANGE COLUMN " + optional)).ifPresent(result::add);
        return result;
    }
    
    private Collection<? extends SQLToken> getPreviousColumnTokens(final String tableName, final ChangeColumnDefinitionSegment segment) {
        Collection<SQLToken> result = new LinkedList<>();
        result.add(new RemoveToken(segment.getPreviousColumn().getStartIndex(), segment.getPreviousColumn().getStopIndex()));
        result.add(new EncryptAlterTableToken(segment.getPreviousColumn().getStopIndex() + 1, segment.getPreviousColumn().getStopIndex(),
                encryptRule.getCipherColumn(tableName, segment.getPreviousColumn().getIdentifier().getValue()), null));
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
            Optional<EncryptAlgorithm> encryptor = encryptRule.findEncryptor(tableName, columnName);
            if (encryptor.isPresent()) {
                result.addAll(getDropColumnTokens(tableName, columnName, each, dropColumnDefinitionSegment));
            }
        }
        return result;
    }
    
    private Collection<SQLToken> getDropColumnTokens(final String tableName, final String columnName, 
                                                     final ColumnSegment columnSegment, final DropColumnDefinitionSegment dropColumnDefinitionSegment) {
        Collection<SQLToken> result = new LinkedList<>();
        result.add(new RemoveToken(columnSegment.getStartIndex(), columnSegment.getStopIndex()));
        result.add(new EncryptAlterTableToken(columnSegment.getStopIndex() + 1, columnSegment.getStopIndex(), encryptRule.getCipherColumn(tableName, columnName), null));
        encryptRule.findAssistedQueryColumn(tableName, columnName).map(optional -> new EncryptAlterTableToken(dropColumnDefinitionSegment.getStopIndex() + 1,
                dropColumnDefinitionSegment.getStopIndex(), optional, ", DROP COLUMN")).ifPresent(result::add);
        encryptRule.findPlainColumn(tableName, columnName).map(optional -> new EncryptAlterTableToken(dropColumnDefinitionSegment.getStopIndex() + 1,
                dropColumnDefinitionSegment.getStopIndex(), optional, ", DROP COLUMN")).ifPresent(result::add);
        return result;
    }
}
