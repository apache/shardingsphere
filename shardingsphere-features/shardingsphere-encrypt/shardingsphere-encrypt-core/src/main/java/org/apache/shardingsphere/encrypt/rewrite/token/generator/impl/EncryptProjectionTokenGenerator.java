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
import com.google.common.base.Strings;
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
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.constant.SubqueryType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Projection token generator for encrypt.
 */
@Setter
public final class EncryptProjectionTokenGenerator extends BaseEncryptSQLTokenGenerator 
        implements CollectionSQLTokenGenerator<SQLStatementContext>, QueryWithCipherColumnAware, PreviousSQLTokensAware {
    
    private boolean queryWithCipherColumn;
    
    private List<SQLToken> previousSQLTokens;
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && !((SelectStatementContext) sqlStatementContext).getAllTables().isEmpty();    
    }
    
    @Override
    public Collection<SubstitutableColumnNameToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Preconditions.checkState(sqlStatementContext instanceof SelectStatementContext);
        Collection<SubstitutableColumnNameToken> result = new LinkedHashSet<>();
        for (SelectStatementContext each : getSelectStatementContexts((SelectStatementContext) sqlStatementContext)) {
            for (String table : each.getTablesContext().getTableNames()) {
                Optional<EncryptTable> encryptTable = getEncryptRule().findEncryptTable(table);
                encryptTable.ifPresent(optional -> result.addAll(generateSQLTokens(each, optional, table)));
            }
        }
        return result;
    }
    
    private Collection<SubstitutableColumnNameToken> generateSQLTokens(final SelectStatementContext selectStatementContext,
                                                                       final EncryptTable encryptTable, final String tableName) {
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        SubqueryType subqueryType = selectStatementContext.getSubqueryType();
        TablesContext tablesContext = selectStatementContext.getTablesContext();
        Collection<ProjectionSegment> projections = selectStatementContext.getSqlStatement().getProjections().getProjections();
        for (ProjectionSegment each : projections) {
            if (each instanceof ColumnProjectionSegment) {
                ColumnProjectionSegment columnSegment = (ColumnProjectionSegment) each;
                String columnName = columnSegment.getColumn().getIdentifier().getValue();
                String owner = columnSegment.getColumn().getOwner().map(optional -> optional.getIdentifier().getValue()).orElse(null);
                if (encryptTable.getLogicColumns().contains(columnName) && isOwnerSameWithTableNameOrAlias(tableName, owner, tablesContext)) {
                    result.add(generateSQLTokens(columnSegment, encryptTable, tableName, subqueryType));
                }
                Optional<String> subqueryTableName = tablesContext.findTableNameFromSubquery(columnName, owner);
                subqueryTableName.ifPresent(optional -> result.add(generateSQLTokens(columnSegment, encryptTable, optional, subqueryType)));
            }
            if (isToGeneratedSQLToken(each, selectStatementContext, tableName)) {
                ShorthandProjectionSegment shorthandSegment = (ShorthandProjectionSegment) each;
                ShorthandProjection shorthandProjection = getShorthandProjection(shorthandSegment, selectStatementContext.getProjectionsContext());
                if (!shorthandProjection.getActualColumns().isEmpty()) {
                    result.add(generateSQLTokens(shorthandSegment, shorthandProjection, tableName, encryptTable, selectStatementContext.getDatabaseType(), subqueryType));
                }
            }
        }
        return result;
    }
    
    private SubstitutableColumnNameToken generateSQLTokens(final ColumnProjectionSegment segment, final EncryptTable encryptTable,
                                                           final String tableName, final SubqueryType subqueryType) {
        String columnName = segment.getColumn().getIdentifier().getValue();
        String alias = segment.getAlias().orElseGet(() -> segment.getColumn().getIdentifier().getValue());
        Collection<ColumnProjection> projections = generateProjections(tableName, columnName, alias, null, encryptTable, subqueryType);
        int startIndex = segment.getColumn().getOwner().isPresent() ? segment.getColumn().getOwner().get().getStopIndex() + 2 : segment.getColumn().getStartIndex();
        int stopIndex = segment.getStopIndex();
        return new SubstitutableColumnNameToken(startIndex, stopIndex, projections);
    }
    
    private SubstitutableColumnNameToken generateSQLTokens(final ShorthandProjectionSegment segment, final ShorthandProjection shorthandProjection, final String tableName,
                                                           final EncryptTable encryptTable, final DatabaseType databaseType, final SubqueryType subqueryType) {
        List<ColumnProjection> projections = new LinkedList<>();
        for (ColumnProjection each : shorthandProjection.getActualColumns().values()) {
            if (encryptTable.getLogicColumns().contains(each.getName())) {
                String owner = null == each.getOwner() ? null : each.getOwner();
                projections.addAll(generateProjections(tableName, each.getName(), each.getName(), owner, encryptTable, subqueryType));
            } else {
                projections.add(new ColumnProjection(each.getOwner(), each.getName(), each.getAlias().orElse(null)));
            }
        }
        previousSQLTokens.removeIf(each -> each.getStartIndex() == segment.getStartIndex());
        return new SubstitutableColumnNameToken(segment.getStartIndex(), segment.getStopIndex(), projections, databaseType.getQuoteCharacter());
    }
    
    private Collection<SelectStatementContext> getSelectStatementContexts(final SelectStatementContext selectStatementContext) {
        Collection<SelectStatementContext> result = new LinkedList<>();
        result.add(selectStatementContext);
        result.addAll(selectStatementContext.getSubqueryContexts().values());
        return result;
    }
    
    private boolean isOwnerSameWithTableNameOrAlias(final String tableName, final String owner, final TablesContext tablesContext) {
        if (Strings.isNullOrEmpty(owner)) {
            return true;
        }
        return tablesContext.findTableNameFromSQL(owner).filter(optional -> optional.equals(tableName)).isPresent();
    }
    
    private Collection<ColumnProjection> generateProjections(final String tableName, final String columnName, final String alias, final String owner, 
                                                             final EncryptTable encryptTable, final SubqueryType subqueryType) {
        Collection<ColumnProjection> result = new LinkedList<>();
        if (SubqueryType.PREDICATE_SUBQUERY.equals(subqueryType)) {
            result.add(generatePredicateSubqueryProjection(tableName, columnName, owner, encryptTable));
        } else if (SubqueryType.TABLE_SUBQUERY.equals(subqueryType)) {
            result.addAll(generateTableSubqueryProjections(tableName, columnName, alias, owner));
        } else {
            result.add(generateCommonProjection(tableName, columnName, alias, owner));
        }
        return result;
    }
    
    private ColumnProjection generatePredicateSubqueryProjection(final String tableName, final String columnName, final String owner, final EncryptTable encryptTable) {
        if (Boolean.FALSE.equals(encryptTable.getQueryWithCipherColumn()) || !queryWithCipherColumn) {
            Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, columnName);
            if (plainColumn.isPresent()) {
                return new ColumnProjection(owner, plainColumn.get(), null);
            }
        }
        Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(tableName, columnName);
        if (assistedQueryColumn.isPresent()) {
            return new ColumnProjection(owner, assistedQueryColumn.get(), null);
        }
        String cipherColumn = getEncryptRule().getCipherColumn(tableName, columnName);
        return new ColumnProjection(owner, cipherColumn, null);
    }
    
    private Collection<ColumnProjection> generateTableSubqueryProjections(final String tableName, final String columnName, final String alias, final String owner) {
        Collection<ColumnProjection> result = new LinkedList<>();
        result.add(new ColumnProjection(owner, getEncryptRule().getCipherColumn(tableName, columnName), alias));
        Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(tableName, columnName);
        assistedQueryColumn.ifPresent(optional -> result.add(new ColumnProjection(owner, optional, null)));
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, columnName);
        plainColumn.ifPresent(optional -> result.add(new ColumnProjection(owner, optional, null)));
        return result;
    }
    
    private ColumnProjection generateCommonProjection(final String tableName, final String columnName, final String alias, final String owner) {
        String encryptColumnName = getEncryptColumnName(tableName, columnName);
        return new ColumnProjection(owner, encryptColumnName, alias);
    }
    
    private boolean isToGeneratedSQLToken(final ProjectionSegment projectionSegment, final SelectStatementContext selectStatementContext, final String tableName) {
        if (!(projectionSegment instanceof ShorthandProjectionSegment)) {
            return false;
        }
        Optional<OwnerSegment> ownerSegment = ((ShorthandProjectionSegment) projectionSegment).getOwner();
        return ownerSegment.map(segment -> selectStatementContext.getTablesContext().findTableNameFromSQL(segment.getIdentifier().getValue()).orElse("").equalsIgnoreCase(tableName)).orElse(true);
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
