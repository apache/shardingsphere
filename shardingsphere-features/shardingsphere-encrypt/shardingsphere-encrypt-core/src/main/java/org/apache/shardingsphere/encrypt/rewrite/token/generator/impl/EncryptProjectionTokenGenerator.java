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

import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.rewrite.aware.QueryWithCipherColumnAware;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Projection token generator for encrypt.
 */
@SuppressWarnings("rawtypes")
@Setter
public final class EncryptProjectionTokenGenerator extends BaseEncryptSQLTokenGenerator 
        implements CollectionSQLTokenGenerator<SQLStatementContext>, QueryWithCipherColumnAware, PreviousSQLTokensAware {
    
    private boolean queryWithCipherColumn;
    
    private List<SQLToken> previousSQLTokens;
    
    @SuppressWarnings("rawtypes")
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return (sqlStatementContext instanceof SelectStatementContext && !((SelectStatementContext) sqlStatementContext).getAllTables().isEmpty())
                || ((sqlStatementContext instanceof InsertStatementContext) && null != ((InsertStatementContext) sqlStatementContext).getInsertSelectContext());
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public Collection<SubstitutableColumnNameToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<SubstitutableColumnNameToken> result = new LinkedHashSet<>();
        if (sqlStatementContext instanceof InsertStatementContext) {
            SelectStatementContext selectStatementContext = ((InsertStatementContext) sqlStatementContext).getInsertSelectContext().getSelectStatementContext();
            result.addAll(generateSQLTokens(selectStatementContext));
        }
        if (sqlStatementContext instanceof SelectStatementContext) {
            SelectStatementContext selectStatementContext = (SelectStatementContext) sqlStatementContext;
            result.addAll(generateProjectionSQLTokens(selectStatementContext));
            for (SelectStatementContext each : selectStatementContext.getSubqueryContexts().values()) {
                result.addAll(generateProjectionSQLTokens(each));
            }
        }
        return result;
    }
    
    private Collection<SubstitutableColumnNameToken> generateProjectionSQLTokens(final SelectStatementContext selectStatementContext) {
        Collection<SubstitutableColumnNameToken> result = new LinkedHashSet<>();
        for (String each : selectStatementContext.getTablesContext().getTableNames()) {
            Optional<EncryptTable> encryptTable = getEncryptRule().findEncryptTable(each);
            if (!encryptTable.isPresent()) {
                continue;
            }
            result.addAll(generateProjectionSQLTokens(selectStatementContext, encryptTable.get(), each));
        }
        return result;
    }
    
    private Collection<SubstitutableColumnNameToken> generateProjectionSQLTokens(final SelectStatementContext selectStatementContext, 
                                                                                 final EncryptTable encryptTable, final String tableName) {
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        boolean subqueryTable = selectStatementContext.isSubqueryTable();
        for (ProjectionSegment each : selectStatementContext.getSqlStatement().getProjections().getProjections()) {
            if (each instanceof ColumnProjectionSegment) {
                String columnName = ((ColumnProjectionSegment) each).getColumn().getIdentifier().getValue();
                Optional<String> owner = ((ColumnProjectionSegment) each).getColumn().getOwner().map(optional -> optional.getIdentifier().getValue());
                if (selectStatementContext.getTablesContext().findTableNameFromSubquery(columnName, owner.orElse(null)).isPresent()) {
                    result.addAll(generateProjectionSQLTokens((ColumnProjectionSegment) each, selectStatementContext.getTablesContext()));
                }
                if (encryptTable.getLogicColumns().contains(columnName)
                        && columnMatchTableAndCheckAmbiguous(selectStatementContext, (ColumnProjectionSegment) each, tableName)) {
                    result.add(generateProjectionSQLTokens((ColumnProjectionSegment) each, tableName, subqueryTable));
                }
            }
            if (isToGeneratedSQLToken(each, selectStatementContext, tableName)) {
                ShorthandProjection shorthandProjection = getShorthandProjection((ShorthandProjectionSegment) each, selectStatementContext.getProjectionsContext());
                if (!shorthandProjection.getActualColumns().isEmpty()) {
                    result.add(generateProjectionSQLTokens((ShorthandProjectionSegment) each, shorthandProjection, 
                            tableName, encryptTable, selectStatementContext.getDatabaseType(), subqueryTable));
                }
            }
        }
        return result;
    }

    private Collection<SubstitutableColumnNameToken> generateProjectionSQLTokens(final ColumnProjectionSegment segment, final TablesContext tablesContext) {
        ColumnSegment column = segment.getColumn();
        String columnName = column.getIdentifier().getValue();
        String owner = column.getOwner().map(optional -> optional.getIdentifier().getValue()).orElse(null);
        Optional<String> tableName = tablesContext.findTableNameFromSubquery(columnName, owner);
        if (tableName.isPresent()) {
            String cipherColumn = getEncryptRule().getCipherColumn(tableName.get(), columnName);
            int startIndex = column.getOwner().isPresent() ? column.getOwner().get().getStopIndex() + 2 : column.getStartIndex();
            int stopIndex = column.getStopIndex();
            return Collections.singletonList(new SubstitutableColumnNameToken(startIndex, stopIndex, Collections.singletonList(new ColumnProjection(null, cipherColumn, columnName))));
        }
        return Collections.emptyList();
    }

    private SubstitutableColumnNameToken generateProjectionSQLTokens(final ColumnProjectionSegment segment, final String tableName, final boolean subqueryTable) {
        Collection<ColumnProjection> projections = new LinkedList<>();
        String encryptColumnName = getEncryptColumnName(tableName, segment.getColumn().getIdentifier().getValue());
        String alias = segment.getAlias().orElse(segment.getColumn().getIdentifier().getValue());
        projections.add(new ColumnProjection(null, encryptColumnName, alias));
        Optional<String> assistedQueryColumn = findAssistedQueryColumn(tableName, segment.getColumn().getIdentifier().getValue());
        if (subqueryTable && assistedQueryColumn.isPresent()) {
            projections.add(new ColumnProjection(null, assistedQueryColumn.get(), null));
        }
        return segment.getColumn().getOwner().isPresent()
                ? new SubstitutableColumnNameToken(segment.getColumn().getOwner().get().getStopIndex() + 2, segment.getStopIndex(), projections)
                : new SubstitutableColumnNameToken(segment.getStartIndex(), segment.getStopIndex(), projections);
    }
    
    private SubstitutableColumnNameToken generateProjectionSQLTokens(final ShorthandProjectionSegment segment, final ShorthandProjection shorthandProjection, 
                                                                     final String tableName, final EncryptTable encryptTable, final DatabaseType databaseType, final boolean subqueryTable) {
        List<ColumnProjection> projections = new LinkedList<>();
        for (ColumnProjection each : shorthandProjection.getActualColumns().values()) {
            if (encryptTable.getLogicColumns().contains(each.getName())) {
                String owner = null == each.getOwner() ? null : each.getOwner();
                projections.add(new ColumnProjection(owner, getEncryptColumnName(tableName, each.getName()), each.getName()));
                Optional<String> assistedQueryColumn = findAssistedQueryColumn(tableName, each.getName());
                if (subqueryTable && assistedQueryColumn.isPresent()) {
                    projections.add(new ColumnProjection(owner, assistedQueryColumn.get(), null));
                }
            } else {
                projections.add(new ColumnProjection(each.getOwner(), each.getName(), each.getAlias().orElse(null)));
            }
        }
        previousSQLTokens.removeIf(each -> each.getStartIndex() == segment.getStartIndex());
        return new SubstitutableColumnNameToken(segment.getStartIndex(), segment.getStopIndex(), projections, databaseType.getQuoteCharacter());
    }
    
    private boolean columnMatchTableAndCheckAmbiguous(final SelectStatementContext selectStatementContext, final ColumnProjectionSegment columnProjectionSegment, final String tableName) {
        return isOwnerExistsMatchTableAlias(selectStatementContext, columnProjectionSegment, tableName) 
                || isOwnerExistsMatchTableName(selectStatementContext, columnProjectionSegment, tableName) 
                || isColumnUnAmbiguous(selectStatementContext, columnProjectionSegment);
    }
    
    private boolean isOwnerExistsMatchTableAlias(final SelectStatementContext selectStatementContext, final ColumnProjectionSegment columnProjectionSegment, final String tableName) {
        if (!columnProjectionSegment.getColumn().getOwner().isPresent()) {
            return false;
        }
        return selectStatementContext.getTablesContext().getOriginalTables().stream().anyMatch(table -> tableName.equals(table.getTableName().getIdentifier().getValue())
                && table.getAlias().isPresent() && columnProjectionSegment.getColumn().getOwner().get().getIdentifier().getValue().equals(table.getAlias().get()));
    }
    
    private boolean isOwnerExistsMatchTableName(final SelectStatementContext selectStatementContext, final ColumnProjectionSegment columnProjectionSegment, final String tableName) {
        if (!columnProjectionSegment.getColumn().getOwner().isPresent()) {
            return false;
        }
        return selectStatementContext.getTablesContext().getOriginalTables().stream().anyMatch(table -> tableName.equals(table.getTableName().getIdentifier().getValue())
                && !table.getAlias().isPresent() && columnProjectionSegment.getColumn().getOwner().get().getIdentifier().getValue().equals(tableName));
    }
    
    private boolean isColumnUnAmbiguous(final SelectStatementContext selectStatementContext, final ColumnProjectionSegment columnProjectionSegment) {
        if (columnProjectionSegment.getColumn().getOwner().isPresent()) {
            return false;
        }
        int columnCount = 0;
        for (String each : selectStatementContext.getTablesContext().getTableNames()) {
            Optional<EncryptTable> encryptTable;
            if ((encryptTable = getEncryptRule().findEncryptTable(each)).isPresent() 
                    && encryptTable.get().getLogicColumns().contains(columnProjectionSegment.getColumn().getIdentifier().getValue())) {
                columnCount++;
            }
        }
        Preconditions.checkState(columnCount <= 1, "column `%s` is ambiguous in encrypt rules", columnProjectionSegment.getColumn().getIdentifier().getValue());
        return true;
    }
    
    private boolean isToGeneratedSQLToken(final ProjectionSegment projectionSegment, final SelectStatementContext selectStatementContext, final String tableName) {
        if (!(projectionSegment instanceof ShorthandProjectionSegment)) {
            return false;
        }
        Optional<OwnerSegment> ownerSegment = ((ShorthandProjectionSegment) projectionSegment).getOwner();
        return ownerSegment.map(segment -> selectStatementContext.getTablesContext().findTableNameFromSQL(segment.getIdentifier().getValue()).orElse("").equalsIgnoreCase(tableName)).orElse(true);
    }
    
    private Optional<String> findAssistedQueryColumn(final String tableName, final String logicEncryptColumnName) {
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, logicEncryptColumnName);
        return plainColumn.isPresent() && !queryWithCipherColumn ? plainColumn : getEncryptRule().findAssistedQueryColumn(tableName, logicEncryptColumnName);
    }
    
    private String getEncryptColumnName(final String tableName, final String logicEncryptColumnName) {
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, logicEncryptColumnName);
        return plainColumn.isPresent() && !queryWithCipherColumn ? plainColumn.get() : getEncryptRule().getCipherColumn(tableName, logicEncryptColumnName);
    }
    
    private ShorthandProjection getShorthandProjection(final ShorthandProjectionSegment segment, final ProjectionsContext projectionsContext) {
        Optional<String> owner = segment.getOwner().isPresent() ? Optional.of(segment.getOwner().get().getIdentifier().getValue()) : Optional.empty();
        for (Projection each : projectionsContext.getProjections()) {
            if (each instanceof ShorthandProjection) {
                if (!owner.isPresent() && !((ShorthandProjection) each).getOwner().isPresent()) {
                    return (ShorthandProjection) each;
                }
                if (owner.isPresent() && owner.get().equals(((ShorthandProjection) each).getOwner().orElse(null))) {
                    return (ShorthandProjection) each;
                }
            }
        }
        throw new IllegalStateException(String.format("Can not find shorthand projection segment, owner is: `%s`", owner.orElse(null)));
    }
    
    @Override
    public void setPreviousSQLTokens(final List<SQLToken> previousSQLTokens) {
        this.previousSQLTokens = previousSQLTokens;
    }
}
