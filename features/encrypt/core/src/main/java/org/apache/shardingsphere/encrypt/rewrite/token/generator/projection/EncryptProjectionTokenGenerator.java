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
import org.apache.shardingsphere.encrypt.rewrite.token.generator.projection.checker.EncryptProjectionRewriteSupportedChecker;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.DerivedColumn;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
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
    
    private final EncryptRule encryptRule;
    
    private final DatabaseType databaseType;
    
    /**
     * Generate SQL tokens.
     *
     * @param selectStatementContext select statement context
     * @return generated SQL tokens
     */
    public Collection<SQLToken> generateSQLTokens(final SelectStatementContext selectStatementContext) {
        Collection<SQLToken> result = new LinkedHashSet<>(generateSelectSQLTokens(selectStatementContext));
        selectStatementContext.getSubqueryContexts().values().stream().map(this::generateSelectSQLTokens).forEach(result::addAll);
        return result;
    }
    
    private Collection<SQLToken> generateSelectSQLTokens(final SelectStatementContext selectStatementContext) {
        Collection<SQLToken> result = new LinkedList<>();
        EncryptProjectionRewriteSupportedChecker.checkNotContainEncryptProjectionInCombineSegment(encryptRule, selectStatementContext);
        for (ProjectionSegment each : selectStatementContext.getSqlStatement().getProjections().getProjections()) {
            EncryptProjectionRewriteSupportedChecker.checkNotContainEncryptShorthandExpandWithSubqueryStatement(selectStatementContext, each);
            if (each instanceof ColumnProjectionSegment) {
                generateSQLToken(selectStatementContext, (ColumnProjectionSegment) each).ifPresent(result::add);
            }
            if (each instanceof ShorthandProjectionSegment) {
                ShorthandProjectionSegment shorthandSegment = (ShorthandProjectionSegment) each;
                Collection<Projection> actualColumns = getShorthandProjection(shorthandSegment, selectStatementContext.getProjectionsContext()).getActualColumns();
                if (!actualColumns.isEmpty()) {
                    result.add(generateSQLToken(shorthandSegment, actualColumns, selectStatementContext, selectStatementContext.getSubqueryType()));
                }
            }
        }
        return result;
    }
    
    private Optional<SubstitutableColumnNameToken> generateSQLToken(final SelectStatementContext selectStatementContext, final ColumnProjectionSegment columnSegment) {
        ColumnProjection columnProjection = buildColumnProjection(columnSegment);
        String columnName = columnProjection.getOriginalColumn().getValue();
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(columnProjection.getOriginalTable().getValue());
        if (encryptTable.isPresent() && encryptTable.get().isEncryptColumn(columnName) && !selectStatementContext.containsTableSubquery()) {
            EncryptColumn encryptColumn = encryptTable.get().getEncryptColumn(columnName);
            Collection<Projection> projections = generateProjections(encryptColumn, columnProjection, selectStatementContext.getSubqueryType(), false);
            int startIndex = columnSegment.getColumn().getOwner().isPresent() ? columnSegment.getColumn().getOwner().get().getStopIndex() + 2 : columnSegment.getColumn().getStartIndex();
            int stopIndex = columnSegment.getStopIndex();
            return Optional.of(new SubstitutableColumnNameToken(startIndex, stopIndex, projections, databaseType));
        }
        return Optional.empty();
    }
    
    private SubstitutableColumnNameToken generateSQLToken(final ShorthandProjectionSegment segment, final Collection<Projection> actualColumns,
                                                          final SelectStatementContext selectStatementContext, final SubqueryType subqueryType) {
        Collection<Projection> projections = new LinkedList<>();
        for (Projection each : actualColumns) {
            if (each instanceof ColumnProjection) {
                ColumnProjection columnProjection = (ColumnProjection) each;
                Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(columnProjection.getOriginalTable().getValue());
                if (encryptTable.isPresent() && encryptTable.get().isEncryptColumn(columnProjection.getOriginalColumn().getValue()) && !selectStatementContext.containsTableSubquery()) {
                    EncryptColumn encryptColumn = encryptTable.get().getEncryptColumn(columnProjection.getOriginalColumn().getValue());
                    projections.addAll(generateProjections(encryptColumn, columnProjection, subqueryType, true));
                    continue;
                }
            }
            projections.add(each.getAlias().filter(alias -> !DerivedColumn.isDerivedColumnName(alias.getValue()))
                    .map(optional -> (Projection) new ColumnProjection(null, optional, null, databaseType)).orElse(each));
        }
        int startIndex = segment.getOwner().isPresent() ? segment.getOwner().get().getStartIndex() : segment.getStartIndex();
        previousSQLTokens.removeIf(each -> each.getStartIndex() == startIndex);
        return new SubstitutableColumnNameToken(startIndex, segment.getStopIndex(), projections, selectStatementContext.getDatabaseType());
    }
    
    private ColumnProjection buildColumnProjection(final ColumnProjectionSegment segment) {
        IdentifierValue owner = segment.getColumn().getOwner().map(OwnerSegment::getIdentifier).orElse(null);
        ColumnProjection result = new ColumnProjection(owner, segment.getColumn().getIdentifier(), segment.getAliasName().isPresent() ? segment.getAlias().orElse(null) : null, databaseType);
        result.setOriginalColumn(segment.getColumn().getColumnBoundInfo().getOriginalColumn());
        result.setOriginalTable(segment.getColumn().getColumnBoundInfo().getOriginalTable());
        return result;
    }
    
    private Collection<Projection> generateProjections(final EncryptColumn encryptColumn, final ColumnProjection columnProjection,
                                                       final SubqueryType subqueryType, final boolean shorthandProjection) {
        if (null == subqueryType || SubqueryType.PROJECTION == subqueryType) {
            return Collections.singleton(generateProjection(encryptColumn, columnProjection, shorthandProjection));
        }
        if (SubqueryType.TABLE == subqueryType || SubqueryType.JOIN == subqueryType) {
            return generateProjectionsInTableSegmentSubquery(encryptColumn, columnProjection, shorthandProjection, subqueryType);
        }
        if (SubqueryType.PREDICATE == subqueryType) {
            return Collections.singleton(generateProjectionInPredicateSubquery(encryptColumn, columnProjection, shorthandProjection));
        }
        if (SubqueryType.INSERT_SELECT == subqueryType) {
            return generateProjectionsInInsertSelectSubquery(encryptColumn, columnProjection, shorthandProjection);
        }
        throw new UnsupportedSQLOperationException(
                "Projections not in simple select, table subquery, join subquery, predicate subquery and insert select subquery are not supported in encrypt feature.");
    }
    
    private ColumnProjection generateProjection(final EncryptColumn encryptColumn, final ColumnProjection columnProjection, final boolean shorthandProjection) {
        IdentifierValue encryptColumnOwner = shorthandProjection ? columnProjection.getOwner().orElse(null) : null;
        String encryptColumnName = encryptColumn.getCipher().getName();
        return new ColumnProjection(encryptColumnOwner, new IdentifierValue(encryptColumnName, columnProjection.getName().getQuoteCharacter()),
                columnProjection.getAlias().orElse(columnProjection.getName()), databaseType);
    }
    
    private Collection<Projection> generateProjectionsInTableSegmentSubquery(final EncryptColumn encryptColumn, final ColumnProjection columnProjection,
                                                                             final boolean shorthandProjection, final SubqueryType subqueryType) {
        Collection<Projection> result = new LinkedList<>();
        IdentifierValue encryptColumnOwner = shorthandProjection ? columnProjection.getOwner().orElse(null) : null;
        QuoteCharacter quoteCharacter = columnProjection.getName().getQuoteCharacter();
        IdentifierValue columnName = new IdentifierValue(encryptColumn.getCipher().getName(), quoteCharacter);
        IdentifierValue alias = SubqueryType.JOIN == subqueryType ? null : columnProjection.getAlias().orElse(columnProjection.getName());
        result.add(new ColumnProjection(encryptColumnOwner, columnName, alias, databaseType));
        IdentifierValue assistedColumOwner = columnProjection.getOwner().orElse(null);
        encryptColumn.getAssistedQuery().ifPresent(optional -> result.add(new ColumnProjection(assistedColumOwner, new IdentifierValue(optional.getName(), quoteCharacter), null, databaseType)));
        encryptColumn.getLikeQuery().ifPresent(optional -> result.add(new ColumnProjection(assistedColumOwner, new IdentifierValue(optional.getName(), quoteCharacter), null, databaseType)));
        return result;
    }
    
    private ColumnProjection generateProjectionInPredicateSubquery(final EncryptColumn encryptColumn, final ColumnProjection columnProjection, final boolean shorthandProjection) {
        IdentifierValue owner = shorthandProjection ? columnProjection.getOwner().orElse(null) : null;
        QuoteCharacter quoteCharacter = columnProjection.getName().getQuoteCharacter();
        return encryptColumn.getAssistedQuery().map(optional -> new ColumnProjection(owner, new IdentifierValue(optional.getName(), quoteCharacter), null, databaseType))
                .orElseGet(() -> new ColumnProjection(owner, new IdentifierValue(encryptColumn.getCipher().getName(), quoteCharacter), columnProjection.getAlias().orElse(columnProjection.getName()),
                        databaseType));
    }
    
    private Collection<Projection> generateProjectionsInInsertSelectSubquery(final EncryptColumn encryptColumn, final ColumnProjection columnProjection, final boolean shorthandProjection) {
        QuoteCharacter quoteCharacter = columnProjection.getName().getQuoteCharacter();
        IdentifierValue columnName = new IdentifierValue(encryptColumn.getCipher().getName(), quoteCharacter);
        Collection<Projection> result = new LinkedList<>();
        IdentifierValue encryptColumnOwner = shorthandProjection ? columnProjection.getOwner().orElse(null) : null;
        result.add(new ColumnProjection(encryptColumnOwner, columnName, null, databaseType));
        IdentifierValue assistedColumOwner = columnProjection.getOwner().orElse(null);
        encryptColumn.getAssistedQuery().ifPresent(optional -> result.add(new ColumnProjection(assistedColumOwner, new IdentifierValue(optional.getName(), quoteCharacter), null, databaseType)));
        encryptColumn.getLikeQuery().ifPresent(optional -> result.add(new ColumnProjection(assistedColumOwner, new IdentifierValue(optional.getName(), quoteCharacter), null, databaseType)));
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
