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
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SubqueryTableContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.SubqueryExtractUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Projection token generator for encrypt.
 */
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
        SubqueryTableContext subqueryTableContext = new SubqueryTableContext();
        if (sqlStatementContext instanceof InsertStatementContext) {
            result.addAll(generateSQLTokens(((InsertStatementContext) sqlStatementContext).getInsertSelectContext().getSelectStatementContext(), Optional.empty(), SubqueryEnum.INSERT_SELECT,
                    subqueryTableContext));
        }
        if (sqlStatementContext instanceof SelectStatementContext) {
            SelectStatementContext selectStatementContext = (SelectStatementContext) sqlStatementContext;
            result.addAll(generateSQLTokens(selectStatementContext, subqueryTableContext));
            result.addAll(generateSQLTokens(selectStatementContext, Optional.empty(), SubqueryEnum.NONE, subqueryTableContext));
        }
        return result;
    }
    
    private Collection<SubstitutableColumnNameToken> generateSQLTokens(final SelectStatementContext selectStatementContext, final SubqueryTableContext subqueryTableContext) {
        if (!selectStatementContext.isContainsSubquery()) {
            return Collections.emptyList();
        }
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        result.addAll(SubqueryExtractUtil.getSubqueryTableSegmentsFromTableSegment(selectStatementContext.getSqlStatement().getFrom()).stream().map(
            each -> generateSQLTokens(selectStatementContext, each, subqueryTableContext)).flatMap(Collection::stream).collect(Collectors.toList()));
        result.addAll(SubqueryExtractUtil.getSubquerySegmentsFromProjections(selectStatementContext.getSqlStatement().getProjections()).stream().map(
            each -> generateSQLTokens(selectStatementContext, each, SubqueryEnum.PROJECTION, subqueryTableContext)).flatMap(Collection::stream).collect(Collectors.toList()));
        selectStatementContext.getWhere().ifPresent(where -> result.addAll(SubqueryExtractUtil.getSubquerySegmentsFromExpression(where.getExpr()).stream().map(
            each -> generateSQLTokens(selectStatementContext, each, SubqueryEnum.EXPRESSION, subqueryTableContext)).flatMap(Collection::stream).collect(Collectors.toList())));
        return result;
    }
    
    private Collection<SubstitutableColumnNameToken> generateSQLTokens(final SelectStatementContext selectStatementContext, final SubquerySegment subquerySegment, 
                                                                       final SubqueryEnum subqueryEnum, final SubqueryTableContext subqueryTableContext) {
        return generateSQLTokens(new SelectStatementContext(selectStatementContext.getMetaDataMap(), selectStatementContext.getParameters(), subquerySegment.getSelect(),
                   selectStatementContext.getSchemaName()), Optional.empty(), subqueryEnum, subqueryTableContext);
    }
    
    private Collection<SubstitutableColumnNameToken> generateSQLTokens(final SelectStatementContext selectStatementContext, final SubqueryTableSegment subqueryTableSegment, 
                                                                       final SubqueryTableContext subqueryTableContext) {
        return generateSQLTokens(new SelectStatementContext(selectStatementContext.getMetaDataMap(), selectStatementContext.getParameters(), subqueryTableSegment.getSubquery().getSelect(),
                   selectStatementContext.getSchemaName()), subqueryTableSegment.getAlias(), SubqueryEnum.NESTED_PROJECTION_TABLE_SEGMENT, subqueryTableContext);
    }

    private Collection<SubstitutableColumnNameToken> generateSQLTokens(final SelectStatementContext selectStatementContext, final Optional<String> alias,
                                                                       final SubqueryEnum subqueryEnum, final SubqueryTableContext subqueryTableContext) {
        Collection<SubstitutableColumnNameToken> result = new LinkedHashSet<>();
        ProjectionsSegment projectionsSegment = selectStatementContext.getSqlStatement().getProjections();
        for (String each : selectStatementContext.getTablesContext().getTableNames()) {
            getEncryptRule().findEncryptTable(each).map(optional -> generateSQLTokens(projectionsSegment, each, selectStatementContext, optional, alias, subqueryEnum,
                    subqueryTableContext)).ifPresent(result::addAll);
        }
        selectStatementContext.setSubqueryTableContext(subqueryTableContext);
        return result;
    }
    
    private Collection<SubstitutableColumnNameToken> generateSQLTokens(final ProjectionsSegment segment, final String tableName, final SelectStatementContext selectStatementContext,
            final EncryptTable encryptTable, final Optional<String> alias, final SubqueryEnum subqueryEnum, final SubqueryTableContext subqueryTableContext) {
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        for (ProjectionSegment each : segment.getProjections()) {
            if (each instanceof ColumnProjectionSegment) {
                if (!subqueryTableContext.isEmpty() && ((ColumnProjectionSegment) each).getColumn().getOwner().isPresent()) {
                    result.addAll(generateSQLTokens(each, subqueryTableContext));
                }
                if (encryptTable.getLogicColumns().contains(((ColumnProjectionSegment) each).getColumn().getIdentifier().getValue())
                        && columnMatchTableAndCheckAmbiguous(selectStatementContext, (ColumnProjectionSegment) each, tableName)) {
                    result.add(generateSQLToken((ColumnProjectionSegment) each, tableName, alias, subqueryEnum, subqueryTableContext));
                }
            }
            if (isToGeneratedSQLToken(each, selectStatementContext, tableName)) {
                ShorthandProjection shorthandProjection = getShorthandProjection((ShorthandProjectionSegment) each, selectStatementContext.getProjectionsContext());
                if (!shorthandProjection.getActualColumns().isEmpty()) {
                    result.add(generateSQLToken((ShorthandProjectionSegment) each, shorthandProjection, tableName, encryptTable, selectStatementContext.getDatabaseType(), subqueryEnum));
                }
            }
        }
        return result;
    }

    private Collection<SubstitutableColumnNameToken> generateSQLTokens(final ProjectionSegment each, final SubqueryTableContext subqueryTableContext) {
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        ColumnSegment column = ((ColumnProjectionSegment) each).getColumn();
        int startIndex = column.getOwner().isPresent() ? column.getOwner().get().getStopIndex() + 2 : column.getStartIndex();
        int stopIndex = column.getStopIndex();
        subqueryTableContext.getCipherColumn(column).ifPresent(item -> 
            result.add(new SubstitutableColumnNameToken(startIndex, stopIndex, getColumnProjections(item, column.getIdentifier().getValue()))));
        return result;
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

    private SubstitutableColumnNameToken generateSQLToken(final ColumnProjectionSegment segment, final String tableName, final Optional<String> alias, final SubqueryEnum subqueryEnum,
                                                          final SubqueryTableContext subqueryTableContext) {
        String encryptColumnName = getEncryptColumnName(tableName, segment.getColumn().getIdentifier().getValue());
        Collection<ColumnProjection> projections = getColumnProjections(segment, tableName, alias, encryptColumnName, subqueryEnum,
                subqueryTableContext);
        return segment.getColumn().getOwner().isPresent()
                ? new SubstitutableColumnNameToken(segment.getColumn().getOwner().get().getStopIndex() + 2, segment.getStopIndex(), projections)
                : new SubstitutableColumnNameToken(segment.getStartIndex(), segment.getStopIndex(), projections);
    }
    
    private SubstitutableColumnNameToken generateSQLToken(final ShorthandProjectionSegment segment, final ShorthandProjection shorthandProjection, final String tableName,
                                                          final EncryptTable encryptTable, final DatabaseType databaseType, final SubqueryEnum subqueryEnum) {
        List<ColumnProjection> projections = new LinkedList<>();
        for (ColumnProjection each : shorthandProjection.getActualColumns().values()) {
            if (encryptTable.getLogicColumns().contains(each.getName())) {
                projections.addAll(getShorthandProjectionForSubquery(each, tableName, subqueryEnum));
            } else {
                projections.add(columnProjection(each, each.getName(), null));
            }
        }
        previousSQLTokens.removeIf(each -> each.getStartIndex() == segment.getStartIndex());
        return new SubstitutableColumnNameToken(segment.getStartIndex(), segment.getStopIndex(), projections, databaseType.getQuoteCharacter());
    }

    private Collection<ColumnProjection> getColumnProjections(final ColumnProjectionSegment segment, final String tableName, final Optional<String> alias, final String encryptColumnName,
                                                              final SubqueryEnum subqueryEnum, final SubqueryTableContext subqueryTableContext) {
        Collection<ColumnProjection> result = new LinkedList<>();
        if (SubqueryEnum.INSERT_SELECT.equals(subqueryEnum)) {
            result.addAll(getColumnProjectionsForInsertSelect(segment, tableName, encryptColumnName));
        }
        if (SubqueryEnum.PROJECTION.equals(subqueryEnum)) {
            result.add(columnProjection(null, encryptColumnName, null));
        }
        if (SubqueryEnum.NESTED_PROJECTION_TABLE_SEGMENT.equals(subqueryEnum)) {
            result.add(columnProjection(null, encryptColumnName, null));
            result.addAll(getColumnProjectionsForSubqueryProjectionOrTabSegment(segment, tableName, alias, subqueryTableContext));
        }
        if (SubqueryEnum.EXPRESSION.equals(subqueryEnum)) {
            result.addAll(getColumnProjectionsForInExpression(segment, tableName));
        }
        if (SubqueryEnum.NONE.equals(subqueryEnum)) {
            result.add(columnProjection(null, encryptColumnName, segment.getAlias().orElse(segment.getColumn().getIdentifier().getValue())));
        }
        return result;
    }

    private Collection<ColumnProjection> getColumnProjections(final String columnName, final String alias) {
        return Collections.singletonList(new ColumnProjection(null, columnName, alias));
    }

    private Collection<ColumnProjection> getColumnProjectionsForInsertSelect(final ColumnProjectionSegment segment, final String tableName, final String encryptColumnName) {
        Collection<ColumnProjection> result = new LinkedList<>();
        result.add(columnProjection(null, encryptColumnName, null));
        assistedQueryColumnProjection(segment, tableName).ifPresent(result::add);
        plainColumnProjection(segment, tableName).ifPresent(result::add);
        return result;
    }

    private Collection<ColumnProjection> getColumnProjectionsForInExpression(final ColumnProjectionSegment segment, final String tableName) {
        Collection<ColumnProjection> result = new LinkedList<>();
        assistedQueryColumnProjection(segment, tableName).ifPresent(result::add);
        return result;
    }

    private Collection<ColumnProjection> getColumnProjectionsForSubqueryProjectionOrTabSegment(final ColumnProjectionSegment segment, final String tableName, final Optional<String> alias, 
                                                                                               final SubqueryTableContext subqueryTableContext) {
        Collection<ColumnProjection> result = new LinkedList<>();
        String plainColumn = segment.getColumn().getIdentifier().getValue();
        Optional<String> assistedQueryColumn = findAssistedQueryColumn(tableName, plainColumn);
        assistedQueryColumn.ifPresent(each -> result.add(new ColumnProjection(null, assistedQueryColumn.get(), null)));
        subqueryTableContext.put(alias, plainColumn, getEncryptColumnName(tableName, plainColumn), assistedQueryColumn);
        return result;
    }

    private Collection<ColumnProjection> getShorthandProjectionForSubquery(final ColumnProjection each, final String tableName, final SubqueryEnum subqueryKind) {
        Collection<ColumnProjection> result = new LinkedList<>();
        if (SubqueryEnum.PROJECTION.equals(subqueryKind)) {
            result.add(columnProjection(each, getEncryptColumnName(tableName, each.getName()), null));
            result.add(columnProjection(each, findAssistedQueryColumn(tableName, each.getName()).get(), null));
        } else {
            result.add(columnProjection(each, getEncryptColumnName(tableName, each.getName()), each.getName()));
        }
        return result;
    }

    private Optional<ColumnProjection> assistedQueryColumnProjection(final ColumnProjectionSegment segment, final String tableName) {
        Optional<String> assistedQueryColumn = findAssistedQueryColumn(tableName, segment.getColumn().getIdentifier().getValue());
        return assistedQueryColumn.isPresent() ? Optional.of(new ColumnProjection(null, assistedQueryColumn.get(), null)) : Optional.empty();
    }

    private Optional<ColumnProjection> plainColumnProjection(final ColumnProjectionSegment segment, final String tableName) {
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, segment.getColumn().getIdentifier().getValue());
        return plainColumn.isPresent() ? Optional.of(new ColumnProjection(null, plainColumn.get(), null)) : Optional.empty();
    }

    private ColumnProjection columnProjection(final ColumnProjection each, final String columnName, final String alias) {
        return new ColumnProjection((each == null || null == each.getOwner()) ? null : each.getOwner(), columnName, alias);
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
