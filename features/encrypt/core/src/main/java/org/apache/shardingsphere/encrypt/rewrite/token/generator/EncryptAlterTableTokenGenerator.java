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
import org.apache.shardingsphere.encrypt.api.encrypt.standard.StandardEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptColumnAlterException;
import org.apache.shardingsphere.encrypt.rewrite.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAlterTableToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.Substitutable;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
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
        EncryptTable encryptTable = encryptRule.getEncryptTable(tableName);
        Collection<SQLToken> result = new LinkedList<>(getAddColumnTokens(encryptTable, alterTableStatementContext.getSqlStatement().getAddColumnDefinitions()));
        result.addAll(getModifyColumnTokens(encryptTable, alterTableStatementContext.getSqlStatement().getModifyColumnDefinitions()));
        result.addAll(getChangeColumnTokens(encryptTable, alterTableStatementContext.getSqlStatement().getChangeColumnDefinitions()));
        List<SQLToken> dropColumnTokens = getDropColumnTokens(encryptTable, alterTableStatementContext.getSqlStatement().getDropColumnDefinitions());
        String databaseName = alterTableStatementContext.getDatabaseType().getType();
        if ("SQLServer".equals(databaseName)) {
            result.addAll(mergeDropColumnStatement(dropColumnTokens, "", ""));
        } else if ("Oracle".equals(databaseName)) {
            result.addAll(mergeDropColumnStatement(dropColumnTokens, "(", ")"));
        } else {
            result.addAll(dropColumnTokens);
        }
        return result;
    }
    
    private Collection<SQLToken> getAddColumnTokens(final EncryptTable encryptTable, final Collection<AddColumnDefinitionSegment> columnDefinitionSegments) {
        Collection<SQLToken> result = new LinkedList<>();
        for (AddColumnDefinitionSegment each : columnDefinitionSegments) {
            result.addAll(getAddColumnTokens(encryptTable, each));
        }
        return result;
    }
    
    private Collection<SQLToken> getAddColumnTokens(final EncryptTable encryptTable, final AddColumnDefinitionSegment addColumnDefinitionSegment) {
        Collection<SQLToken> result = new LinkedList<>();
        for (ColumnDefinitionSegment each : addColumnDefinitionSegment.getColumnDefinitions()) {
            String columnName = each.getColumnName().getIdentifier().getValue();
            if (encryptTable.isEncryptColumn(columnName)) {
                result.addAll(getAddColumnTokens(encryptTable.getEncryptColumn(columnName), addColumnDefinitionSegment, each));
            }
        }
        getAddColumnPositionToken(encryptTable, addColumnDefinitionSegment).ifPresent(result::add);
        return result;
    }
    
    private Collection<SQLToken> getAddColumnTokens(final EncryptColumn encryptColumn,
                                                    final AddColumnDefinitionSegment addColumnDefinitionSegment, final ColumnDefinitionSegment columnDefinitionSegment) {
        Collection<SQLToken> result = new LinkedList<>();
        result.add(new RemoveToken(columnDefinitionSegment.getStartIndex(), columnDefinitionSegment.getColumnName().getStopIndex()));
        result.add(new EncryptAlterTableToken(columnDefinitionSegment.getColumnName().getStopIndex() + 1, columnDefinitionSegment.getColumnName().getStopIndex(),
                encryptColumn.getCipher().getName(), null));
        encryptColumn.getAssistedQuery().map(optional -> new EncryptAlterTableToken(
                addColumnDefinitionSegment.getStopIndex() + 1, columnDefinitionSegment.getColumnName().getStopIndex(), optional.getName(), ", ADD COLUMN")).ifPresent(result::add);
        encryptColumn.getLikeQuery().map(optional -> new EncryptAlterTableToken(
                addColumnDefinitionSegment.getStopIndex() + 1, columnDefinitionSegment.getColumnName().getStopIndex(), optional.getName(), ", ADD COLUMN")).ifPresent(result::add);
        return result;
    }
    
    private Optional<SQLToken> getAddColumnPositionToken(final EncryptTable encryptTable, final AddColumnDefinitionSegment addColumnDefinitionSegment) {
        Optional<ColumnPositionSegment> columnPositionSegment = addColumnDefinitionSegment.getColumnPosition().filter(optional -> null != optional.getColumnName());
        if (columnPositionSegment.isPresent()) {
            String columnName = columnPositionSegment.get().getColumnName().getIdentifier().getValue();
            if (encryptTable.isEncryptColumn(columnName)) {
                return Optional.of(getPositionColumnToken(encryptTable, addColumnDefinitionSegment.getColumnPosition().get()));
            }
        }
        return Optional.empty();
    }
    
    private Collection<SQLToken> getModifyColumnTokens(final EncryptTable encryptTable, final Collection<ModifyColumnDefinitionSegment> columnDefinitionSegments) {
        Collection<SQLToken> result = new LinkedList<>();
        for (ModifyColumnDefinitionSegment each : columnDefinitionSegments) {
            String columnName = each.getColumnDefinition().getColumnName().getIdentifier().getValue();
            if (encryptTable.isEncryptColumn(columnName)) {
                result.addAll(getModifyColumnTokens(encryptTable.getEncryptColumn(columnName), each));
            }
            each.getColumnPosition().flatMap(optional -> getColumnPositionToken(encryptTable, optional)).ifPresent(result::add);
        }
        return result;
    }
    
    private Collection<SQLToken> getModifyColumnTokens(final EncryptColumn encryptColumn, final ModifyColumnDefinitionSegment modifyColumnDefinitionSegment) {
        Collection<SQLToken> result = new LinkedList<>();
        ColumnDefinitionSegment columnDefinitionSegment = modifyColumnDefinitionSegment.getColumnDefinition();
        result.add(new RemoveToken(columnDefinitionSegment.getColumnName().getStartIndex(), columnDefinitionSegment.getColumnName().getStopIndex()));
        result.add(new EncryptAlterTableToken(columnDefinitionSegment.getColumnName().getStopIndex() + 1, columnDefinitionSegment.getColumnName().getStopIndex(),
                encryptColumn.getCipher().getName(), null));
        encryptColumn.getAssistedQuery().map(optional -> new EncryptAlterTableToken(modifyColumnDefinitionSegment.getStopIndex() + 1,
                columnDefinitionSegment.getColumnName().getStopIndex(), optional.getName(), ", MODIFY COLUMN")).ifPresent(result::add);
        encryptColumn.getLikeQuery().map(optional -> new EncryptAlterTableToken(modifyColumnDefinitionSegment.getStopIndex() + 1,
                columnDefinitionSegment.getColumnName().getStopIndex(), optional.getName(), ", MODIFY COLUMN")).ifPresent(result::add);
        return result;
    }
    
    private EncryptAlterTableToken getPositionColumnToken(final EncryptTable encryptTable, final ColumnPositionSegment positionSegment) {
        return new EncryptAlterTableToken(positionSegment.getColumnName().getStartIndex(), positionSegment.getStopIndex(),
                encryptTable.getEncryptColumn(positionSegment.getColumnName().getIdentifier().getValue()).getCipher().getName(), null);
    }
    
    private Optional<SQLToken> getColumnPositionToken(final EncryptTable encryptTable, final ColumnPositionSegment columnPositionSegment) {
        return null != columnPositionSegment.getColumnName() && encryptTable.isEncryptColumn(columnPositionSegment.getColumnName().getIdentifier().getValue())
                ? Optional.of(getPositionColumnToken(encryptTable, columnPositionSegment))
                : Optional.empty();
    }
    
    private Collection<SQLToken> getChangeColumnTokens(final EncryptTable encryptTable, final Collection<ChangeColumnDefinitionSegment> changeColumnDefinitions) {
        Collection<SQLToken> result = new LinkedList<>();
        for (ChangeColumnDefinitionSegment each : changeColumnDefinitions) {
            result.addAll(getChangeColumnTokens(encryptTable, each));
            each.getColumnPosition().flatMap(optional -> getColumnPositionToken(encryptTable, optional)).ifPresent(result::add);
        }
        return result;
    }
    
    private Collection<? extends SQLToken> getChangeColumnTokens(final EncryptTable encryptTable, final ChangeColumnDefinitionSegment segment) {
        isSameEncryptColumn(encryptTable, segment);
        String columnName = segment.getColumnDefinition().getColumnName().getIdentifier().getValue();
        String previousColumnName = segment.getPreviousColumn().getIdentifier().getValue();
        if (!encryptTable.isEncryptColumn(columnName) || !encryptTable.isEncryptColumn(previousColumnName)) {
            return Collections.emptyList();
        }
        Collection<SQLToken> result = new LinkedList<>();
        result.addAll(getPreviousColumnTokens(encryptTable, segment));
        result.addAll(getColumnTokens(encryptTable.getEncryptColumn(columnName), encryptTable.getEncryptColumn(previousColumnName), segment));
        return result;
    }
    
    private void isSameEncryptColumn(final EncryptTable encryptTable, final ChangeColumnDefinitionSegment segment) {
        Optional<StandardEncryptAlgorithm<?, ?>> previousEncryptor = encryptTable.findEncryptor(segment.getPreviousColumn().getIdentifier().getValue());
        Optional<StandardEncryptAlgorithm<?, ?>> currentEncryptor = encryptTable.findEncryptor(segment.getColumnDefinition().getColumnName().getIdentifier().getValue());
        if (!previousEncryptor.isPresent() && !currentEncryptor.isPresent()) {
            return;
        }
        ShardingSpherePreconditions.checkState(previousEncryptor.equals(currentEncryptor) && checkPreviousAndAfterHasSameColumnNumber(encryptTable, segment),
                () -> new EncryptColumnAlterException(
                        encryptTable.getTable(), segment.getColumnDefinition().getColumnName().getIdentifier().getValue(), segment.getPreviousColumn().getIdentifier().getValue()));
    }
    
    private boolean checkPreviousAndAfterHasSameColumnNumber(final EncryptTable encryptTable, final ChangeColumnDefinitionSegment changeColumnDefinitionSegment) {
        EncryptColumn previousColumn = encryptTable.getEncryptColumn(changeColumnDefinitionSegment.getPreviousColumn().getIdentifier().getValue());
        EncryptColumn currentColumn = encryptTable.getEncryptColumn(changeColumnDefinitionSegment.getColumnDefinition().getColumnName().getIdentifier().getValue());
        if (previousColumn.getAssistedQuery().isPresent() && !currentColumn.getAssistedQuery().isPresent()) {
            return false;
        }
        if (previousColumn.getLikeQuery().isPresent() && !currentColumn.getLikeQuery().isPresent()) {
            return false;
        }
        return previousColumn.getAssistedQuery().isPresent() || !currentColumn.getAssistedQuery().isPresent();
    }
    
    private Collection<? extends SQLToken> getColumnTokens(final EncryptColumn encryptColumn, final EncryptColumn previousEncryptColumn, final ChangeColumnDefinitionSegment segment) {
        Collection<SQLToken> result = new LinkedList<>();
        result.add(new RemoveToken(segment.getColumnDefinition().getColumnName().getStartIndex(), segment.getColumnDefinition().getColumnName().getStopIndex()));
        result.add(new EncryptAlterTableToken(segment.getColumnDefinition().getColumnName().getStopIndex() + 1, segment.getColumnDefinition().getColumnName().getStopIndex(),
                encryptColumn.getCipher().getName(), null));
        previousEncryptColumn.getAssistedQuery()
                .map(optional -> new EncryptAlterTableToken(segment.getStopIndex() + 1, segment.getColumnDefinition().getColumnName().getStopIndex(),
                        encryptColumn.getAssistedQuery().map(AssistedQueryColumnItem::getName).orElse(""), ", CHANGE COLUMN " + optional.getName()))
                .ifPresent(result::add);
        previousEncryptColumn.getLikeQuery()
                .map(optional -> new EncryptAlterTableToken(segment.getStopIndex() + 1, segment.getColumnDefinition().getColumnName().getStopIndex(),
                        encryptColumn.getLikeQuery().map(LikeQueryColumnItem::getName).orElse(""), ", CHANGE COLUMN " + optional.getName()))
                .ifPresent(result::add);
        return result;
    }
    
    private Collection<? extends SQLToken> getPreviousColumnTokens(final EncryptTable encryptTable, final ChangeColumnDefinitionSegment segment) {
        Collection<SQLToken> result = new LinkedList<>();
        result.add(new RemoveToken(segment.getPreviousColumn().getStartIndex(), segment.getPreviousColumn().getStopIndex()));
        result.add(new EncryptAlterTableToken(segment.getPreviousColumn().getStopIndex() + 1, segment.getPreviousColumn().getStopIndex(),
                encryptTable.getEncryptColumn(segment.getPreviousColumn().getIdentifier().getValue()).getCipher().getName(), null));
        return result;
    }
    
    private List<SQLToken> getDropColumnTokens(final EncryptTable encryptTable, final Collection<DropColumnDefinitionSegment> columnDefinitionSegments) {
        List<SQLToken> result = new ArrayList<>();
        for (DropColumnDefinitionSegment each : columnDefinitionSegments) {
            result.addAll(getDropColumnTokens(encryptTable, each));
        }
        return result;
    }
    
    private Collection<SQLToken> getDropColumnTokens(final EncryptTable encryptTable, final DropColumnDefinitionSegment dropColumnDefinitionSegment) {
        Collection<SQLToken> result = new LinkedList<>();
        for (ColumnSegment each : dropColumnDefinitionSegment.getColumns()) {
            String columnName = each.getQualifiedName();
            if (encryptTable.isEncryptColumn(columnName)) {
                result.addAll(getDropColumnTokens(encryptTable.getEncryptColumn(columnName), each, dropColumnDefinitionSegment));
            }
        }
        return result;
    }
    
    private Collection<SQLToken> getDropColumnTokens(final EncryptColumn encryptColumn,
                                                     final ColumnSegment columnSegment, final DropColumnDefinitionSegment dropColumnDefinitionSegment) {
        Collection<SQLToken> result = new LinkedList<>();
        result.add(new RemoveToken(columnSegment.getStartIndex(), columnSegment.getStopIndex()));
        result.add(new EncryptAlterTableToken(columnSegment.getStopIndex() + 1, columnSegment.getStopIndex(), encryptColumn.getCipher().getName(), null));
        encryptColumn.getAssistedQuery().map(optional -> new EncryptAlterTableToken(dropColumnDefinitionSegment.getStopIndex() + 1,
                dropColumnDefinitionSegment.getStopIndex(), optional.getName(), ", DROP COLUMN")).ifPresent(result::add);
        encryptColumn.getLikeQuery().map(optional -> new EncryptAlterTableToken(dropColumnDefinitionSegment.getStopIndex() + 1,
                dropColumnDefinitionSegment.getStopIndex(), optional.getName(), ", DROP COLUMN")).ifPresent(result::add);
        return result;
    }
    
    private Collection<SQLToken> mergeDropColumnStatement(final List<SQLToken> dropSQLTokens, final String leftJoiner, final String rightJoiner) {
        Collection<SQLToken> result = new LinkedList<>();
        Collection<String> dropColumns = new LinkedList<>();
        int lastStartIndex = -1;
        for (int i = 0; i < dropSQLTokens.size(); i++) {
            SQLToken token = dropSQLTokens.get(i);
            if (token instanceof RemoveToken) {
                result.add(0 == i ? token : new RemoveToken(lastStartIndex, ((RemoveToken) token).getStopIndex()));
            } else {
                EncryptAlterTableToken encryptAlterTableToken = (EncryptAlterTableToken) token;
                dropColumns.add(encryptAlterTableToken.getColumnName());
                if (i == dropSQLTokens.size() - 1) {
                    result.add(new EncryptAlterTableToken(token.getStartIndex(), encryptAlterTableToken.getStopIndex(), leftJoiner + String.join(",", dropColumns) + rightJoiner, "DROP COLUMN"));
                }
            }
            lastStartIndex = ((Substitutable) token).getStartIndex();
        }
        return result;
    }
}
