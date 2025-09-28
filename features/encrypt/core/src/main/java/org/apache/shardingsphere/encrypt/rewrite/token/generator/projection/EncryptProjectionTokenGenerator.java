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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.projection;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.enums.EncryptDerivedColumnSuffix;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.DerivedColumn;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParenthesesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Projection token generator for encrypt.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public final class EncryptProjectionTokenGenerator {
    
    private final List<SQLToken> previousSQLTokens;
    
    private final DatabaseType databaseType;
    
    private final EncryptRule rule;
    
    private final DialectDatabaseMetaData dialectDatabaseMetaData;
    
    public EncryptProjectionTokenGenerator(final List<SQLToken> previousSQLTokens, final DatabaseType databaseType, final EncryptRule rule) {
        this.previousSQLTokens = previousSQLTokens;
        this.databaseType = databaseType;
        this.rule = rule;
        dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
    }
    
    /**
     * Generate SQL tokens.
     *
     * @param selectStatementContext select statement context
     * @return generated SQL tokens
     */
    public Collection<SQLToken> generateSQLTokens(final SelectStatementContext selectStatementContext) {
        Collection<SQLToken> result = new LinkedList<>();
        selectStatementContext.getSubqueryContexts().values().forEach(each -> result.addAll(generateSQLTokens(each)));
        result.addAll(generateSelectSQLTokens(selectStatementContext));
        return result;
    }
    
    private Collection<SQLToken> generateSelectSQLTokens(final SelectStatementContext selectStatementContext) {
        Collection<SQLToken> result = new LinkedList<>();
        for (ProjectionSegment each : selectStatementContext.getSqlStatement().getProjections().getProjections()) {
            if (each instanceof ColumnProjectionSegment) {
                generateSQLToken(selectStatementContext, (ColumnProjectionSegment) each).ifPresent(result::add);
            }
            if (each instanceof ShorthandProjectionSegment) {
                ShorthandProjectionSegment shorthandSegment = (ShorthandProjectionSegment) each;
                Collection<Projection> actualColumns = getShorthandProjection(shorthandSegment, selectStatementContext.getProjectionsContext()).getActualColumns();
                if (!actualColumns.isEmpty()) {
                    result.add(generateSQLToken(shorthandSegment, actualColumns, selectStatementContext.getSqlStatement().getDatabaseType(), selectStatementContext.getSubqueryType()));
                }
            }
        }
        return result;
    }
    
    private Optional<SubstitutableColumnNameToken> generateSQLToken(final SelectStatementContext selectStatementContext, final ColumnProjectionSegment columnSegment) {
        ColumnProjection columnProjection = buildColumnProjection(columnSegment);
        String columnName = columnProjection.getOriginalColumn().getValue();
        Optional<EncryptTable> encryptTable = rule.findEncryptTable(columnProjection.getOriginalTable().getValue());
        if (encryptTable.isPresent() && encryptTable.get().isEncryptColumn(columnName)) {
            EncryptColumn encryptColumn = encryptTable.get().getEncryptColumn(columnName);
            Collection<Projection> projections = generateProjections(encryptColumn, columnProjection, selectStatementContext.getSubqueryType());
            int startIndex = getStartIndex(columnSegment);
            int stopIndex = getStopIndex(columnSegment);
            previousSQLTokens.removeIf(each -> each.getStartIndex() == startIndex);
            return Optional.of(new SubstitutableColumnNameToken(startIndex, stopIndex, projections, databaseType));
        }
        return Optional.empty();
    }
    
    private SubstitutableColumnNameToken generateSQLToken(final ShorthandProjectionSegment segment, final Collection<Projection> actualColumns, final DatabaseType databaseType,
                                                          final SubqueryType subqueryType) {
        Collection<Projection> projections = new LinkedList<>();
        for (Projection each : actualColumns) {
            if (each instanceof ColumnProjection) {
                ColumnProjection columnProjection = (ColumnProjection) each;
                Optional<EncryptTable> encryptTable = rule.findEncryptTable(columnProjection.getOriginalTable().getValue());
                if (encryptTable.isPresent() && encryptTable.get().isEncryptColumn(columnProjection.getOriginalColumn().getValue())) {
                    EncryptColumn encryptColumn = encryptTable.get().getEncryptColumn(columnProjection.getOriginalColumn().getValue());
                    projections.addAll(generateProjections(encryptColumn, columnProjection, subqueryType));
                    continue;
                }
            }
            projections.add(each.getAlias().filter(alias -> !DerivedColumn.isDerivedColumnName(alias.getValue()))
                    .map(optional -> (Projection) new ColumnProjection(null, optional, null, databaseType)).orElse(each));
        }
        int startIndex = segment.getOwner().isPresent() ? segment.getOwner().get().getStartIndex() : segment.getStartIndex();
        previousSQLTokens.removeIf(each -> each.getStartIndex() == startIndex);
        return new SubstitutableColumnNameToken(startIndex, segment.getStopIndex(), projections, databaseType);
    }
    
    private int getStartIndex(final ColumnProjectionSegment columnSegment) {
        if (columnSegment.getColumn().getLeftParentheses().isPresent()) {
            return columnSegment.getColumn().getLeftParentheses().get().getStartIndex();
        }
        return columnSegment.getColumn().getOwner().isPresent() ? columnSegment.getColumn().getOwner().get().getStartIndex() : columnSegment.getColumn().getStartIndex();
    }
    
    private int getStopIndex(final ColumnProjectionSegment columnSegment) {
        if (columnSegment.getAliasSegment().isPresent()) {
            return columnSegment.getAliasSegment().get().getStopIndex();
        }
        return columnSegment.getColumn().getRightParentheses().isPresent() ? columnSegment.getColumn().getRightParentheses().get().getStopIndex() : columnSegment.getColumn().getStopIndex();
    }
    
    private ColumnProjection buildColumnProjection(final ColumnProjectionSegment segment) {
        IdentifierValue owner = segment.getColumn().getOwner().map(OwnerSegment::getIdentifier).orElse(null);
        return new ColumnProjection(owner, segment.getColumn().getIdentifier(), segment.getAliasName().isPresent() ? segment.getAlias().orElse(null) : null, databaseType,
                segment.getColumn().getLeftParentheses().orElse(null), segment.getColumn().getRightParentheses().orElse(null), segment.getColumn().getColumnBoundInfo());
    }
    
    private Collection<Projection> generateProjections(final EncryptColumn encryptColumn, final ColumnProjection columnProjection, final SubqueryType subqueryType) {
        if (null == subqueryType || SubqueryType.PROJECTION == subqueryType) {
            return Collections.singleton(generateProjection(encryptColumn, columnProjection));
        }
        if (SubqueryType.TABLE == subqueryType || SubqueryType.JOIN == subqueryType || SubqueryType.WITH == subqueryType) {
            return generateProjectionsInTableSegmentSubquery(encryptColumn, columnProjection);
        }
        if (SubqueryType.PREDICATE == subqueryType) {
            return Collections.singleton(generateProjectionInPredicateSubquery(encryptColumn, columnProjection));
        }
        if (SubqueryType.INSERT_SELECT == subqueryType || SubqueryType.VIEW_DEFINITION == subqueryType) {
            return generateProjectionsInInsertSelectSubquery(encryptColumn, columnProjection);
        }
        throw new UnsupportedSQLOperationException(
                "Projections not in simple select, table subquery, join subquery, predicate subquery and insert select subquery are not supported in encrypt feature.");
    }
    
    private ColumnProjection generateProjection(final EncryptColumn encryptColumn, final ColumnProjection columnProjection) {
        String encryptColumnName = getEncryptColumnName(columnProjection, encryptColumn);
        QuoteCharacter quoteCharacter = getQuoteCharacter(columnProjection);
        IdentifierValue cipherColumnName = new IdentifierValue(encryptColumnName, quoteCharacter);
        IdentifierValue cipherColumnAlias = columnProjection.getAlias().orElse(columnProjection.getName());
        return new ColumnProjection(columnProjection.getOwner().orElse(null), cipherColumnName, cipherColumnAlias,
                databaseType, columnProjection.getLeftParentheses().orElse(null), columnProjection.getRightParentheses().orElse(null));
    }
    
    private QuoteCharacter getQuoteCharacter(final ColumnProjection columnProjection) {
        return TableSourceType.PHYSICAL_TABLE == columnProjection.getColumnBoundInfo().getTableSourceType()
                ? dialectDatabaseMetaData.getQuoteCharacter()
                : columnProjection.getName().getQuoteCharacter();
    }
    
    private String getEncryptColumnName(final ColumnProjection columnProjection, final EncryptColumn encryptColumn) {
        IdentifierValue columnName = columnProjection.getName();
        return TableSourceType.TEMPORARY_TABLE == columnProjection.getColumnBoundInfo().getTableSourceType()
                ? EncryptDerivedColumnSuffix.CIPHER.getDerivedColumnName(columnName.getValue(), databaseType)
                : encryptColumn.getCipher().getName();
    }
    
    private Collection<Projection> generateProjectionsInTableSegmentSubquery(final EncryptColumn encryptColumn, final ColumnProjection columnProjection) {
        return generateCipherProjectionsInTableSegmentSubquery(encryptColumn, columnProjection);
    }
    
    private Collection<Projection> generateCipherProjectionsInTableSegmentSubquery(final EncryptColumn encryptColumn, final ColumnProjection columnProjection) {
        Collection<Projection> result = new LinkedList<>();
        IdentifierValue cipherColumnName = TableSourceType.TEMPORARY_TABLE == columnProjection.getColumnBoundInfo().getTableSourceType()
                ? new IdentifierValue(EncryptDerivedColumnSuffix.CIPHER.getDerivedColumnName(columnProjection.getName().getValue(), databaseType),
                        columnProjection.getName().getQuoteCharacter())
                : new IdentifierValue(encryptColumn.getCipher().getName(), columnProjection.getName().getQuoteCharacter());
        IdentifierValue columnAlias = columnProjection.getAlias().orElse(columnProjection.getName());
        IdentifierValue cipherColumnAlias = getEncryptColumnAliasInTableSegmentSubquery(columnProjection, columnAlias, EncryptDerivedColumnSuffix.CIPHER);
        result.add(new ColumnProjection(columnProjection.getOwner().orElse(null), cipherColumnName, cipherColumnAlias, databaseType));
        encryptColumn.getAssistedQuery().ifPresent(optional -> addAssistedQueryColumn(columnProjection, optional, columnAlias, result));
        encryptColumn.getLikeQuery().ifPresent(optional -> addLikeQueryColumn(columnProjection, optional, columnAlias, result));
        return result;
    }
    
    private IdentifierValue getEncryptColumnAliasInTableSegmentSubquery(final ColumnProjection columnProjection, final IdentifierValue columnAlias, final EncryptDerivedColumnSuffix suffix) {
        if (TableSourceType.TEMPORARY_TABLE == columnProjection.getColumnBoundInfo().getTableSourceType()) {
            return columnProjection.getAlias().map(optional -> new IdentifierValue(suffix.getDerivedColumnName(optional.getValue(), databaseType), optional.getQuoteCharacter())).orElse(null);
        }
        return new IdentifierValue(suffix.getDerivedColumnName(columnAlias.getValue(), databaseType), columnAlias.getQuoteCharacter());
    }
    
    private void addAssistedQueryColumn(final ColumnProjection columnProjection, final AssistedQueryColumnItem assistedQueryColumnItem, final IdentifierValue columnAlias,
                                        final Collection<Projection> result) {
        IdentifierValue assistedQueryName = TableSourceType.TEMPORARY_TABLE == columnProjection.getColumnBoundInfo().getTableSourceType()
                ? new IdentifierValue(EncryptDerivedColumnSuffix.ASSISTED_QUERY.getDerivedColumnName(columnProjection.getName().getValue(), databaseType),
                        columnProjection.getName().getQuoteCharacter())
                : new IdentifierValue(assistedQueryColumnItem.getName(), columnProjection.getName().getQuoteCharacter());
        IdentifierValue assistedQueryAlias = getEncryptColumnAliasInTableSegmentSubquery(columnProjection, columnAlias, EncryptDerivedColumnSuffix.ASSISTED_QUERY);
        result.add(new ColumnProjection(columnProjection.getOwner().orElse(null), assistedQueryName, assistedQueryAlias, databaseType, columnProjection.getLeftParentheses().orElse(null),
                columnProjection.getRightParentheses().orElse(null)));
    }
    
    private void addLikeQueryColumn(final ColumnProjection columnProjection, final LikeQueryColumnItem likeQueryColumnItem, final IdentifierValue columnAlias, final Collection<Projection> result) {
        IdentifierValue likeQueryName = TableSourceType.TEMPORARY_TABLE == columnProjection.getColumnBoundInfo().getTableSourceType()
                ? new IdentifierValue(EncryptDerivedColumnSuffix.LIKE_QUERY.getDerivedColumnName(columnProjection.getName().getValue(), databaseType),
                        columnProjection.getName().getQuoteCharacter())
                : new IdentifierValue(likeQueryColumnItem.getName(), columnProjection.getName().getQuoteCharacter());
        IdentifierValue likeQueryAlias = getEncryptColumnAliasInTableSegmentSubquery(columnProjection, columnAlias, EncryptDerivedColumnSuffix.LIKE_QUERY);
        result.add(new ColumnProjection(columnProjection.getOwner().orElse(null), likeQueryName, likeQueryAlias, databaseType, columnProjection.getLeftParentheses().orElse(null),
                columnProjection.getRightParentheses().orElse(null)));
    }
    
    private ColumnProjection generateProjectionInPredicateSubquery(final EncryptColumn encryptColumn, final ColumnProjection columnProjection) {
        QuoteCharacter quoteCharacter = columnProjection.getName().getQuoteCharacter();
        ParenthesesSegment leftParentheses = columnProjection.getLeftParentheses().orElse(null);
        ParenthesesSegment rightParentheses = columnProjection.getRightParentheses().orElse(null);
        IdentifierValue owner = columnProjection.getOwner().orElse(null);
        return encryptColumn.getAssistedQuery()
                .map(optional -> new ColumnProjection(owner, new IdentifierValue(optional.getName(), quoteCharacter), null, databaseType, leftParentheses, rightParentheses))
                .orElseGet(() -> new ColumnProjection(owner, new IdentifierValue(encryptColumn.getCipher().getName(), quoteCharacter), columnProjection.getAlias().orElse(columnProjection.getName()),
                        databaseType, leftParentheses, rightParentheses));
    }
    
    private Collection<Projection> generateProjectionsInInsertSelectSubquery(final EncryptColumn encryptColumn, final ColumnProjection columnProjection) {
        QuoteCharacter quoteCharacter = columnProjection.getName().getQuoteCharacter();
        IdentifierValue columnName = new IdentifierValue(encryptColumn.getCipher().getName(), quoteCharacter);
        Collection<Projection> result = new LinkedList<>();
        ParenthesesSegment leftParentheses = columnProjection.getLeftParentheses().orElse(null);
        ParenthesesSegment rightParentheses = columnProjection.getRightParentheses().orElse(null);
        result.add(new ColumnProjection(columnProjection.getOwner().orElse(null), columnName, null, databaseType, leftParentheses, rightParentheses));
        IdentifierValue columOwner = columnProjection.getOwner().orElse(null);
        encryptColumn.getAssistedQuery()
                .ifPresent(optional -> result.add(new ColumnProjection(columOwner, new IdentifierValue(optional.getName(), quoteCharacter), null, databaseType, leftParentheses, rightParentheses)));
        encryptColumn.getLikeQuery()
                .ifPresent(optional -> result.add(new ColumnProjection(columOwner, new IdentifierValue(optional.getName(), quoteCharacter), null, databaseType, leftParentheses, rightParentheses)));
        return result;
    }
    
    private ShorthandProjection getShorthandProjection(final ShorthandProjectionSegment segment, final ProjectionsContext projectionsContext) {
        Optional<String> owner = segment.getOwner().isPresent() ? Optional.of(segment.getOwner().get().getIdentifier().getValue()) : Optional.empty();
        for (Projection each : projectionsContext.getProjections()) {
            if (each instanceof ShorthandProjection) {
                if (!owner.isPresent() && !((ShorthandProjection) each).getOwner().isPresent()) {
                    return (ShorthandProjection) each;
                }
                if (owner.isPresent() && owner.get().equals(((ShorthandProjection) each).getOwner().map(IdentifierValue::getValue).orElse(null))) {
                    return (ShorthandProjection) each;
                }
            }
        }
        throw new IllegalStateException(String.format("Can not find shorthand projection segment, owner is `%s`", owner.orElse(null)));
    }
}
