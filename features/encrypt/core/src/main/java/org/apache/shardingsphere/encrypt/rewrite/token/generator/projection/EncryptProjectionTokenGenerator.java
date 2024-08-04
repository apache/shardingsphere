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
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParenthesesSegment;
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
        for (ProjectionSegment each : selectStatementContext.getSqlStatement().getProjections().getProjections()) {
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
            Collection<Projection> projections = generateProjections(encryptColumn, columnProjection, selectStatementContext.getSubqueryType());
            int startIndex = getStartIndex(columnSegment);
            int stopIndex = getStopIndex(columnSegment);
            previousSQLTokens.removeIf(each -> each.getStartIndex() == startIndex);
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
                    projections.addAll(generateProjections(encryptColumn, columnProjection, subqueryType));
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
        ColumnProjection result = new ColumnProjection(owner, segment.getColumn().getIdentifier(), segment.getAliasName().isPresent() ? segment.getAlias().orElse(null) : null, databaseType,
                segment.getColumn().getLeftParentheses().orElse(null), segment.getColumn().getRightParentheses().orElse(null));
        result.setOriginalColumn(segment.getColumn().getColumnBoundInfo().getOriginalColumn());
        result.setOriginalTable(segment.getColumn().getColumnBoundInfo().getOriginalTable());
        return result;
    }
    
    private Collection<Projection> generateProjections(final EncryptColumn encryptColumn, final ColumnProjection columnProjection,
                                                       final SubqueryType subqueryType) {
        if (null == subqueryType || SubqueryType.PROJECTION == subqueryType) {
            return Collections.singleton(generateProjection(encryptColumn, columnProjection));
        }
        if (SubqueryType.TABLE == subqueryType || SubqueryType.JOIN == subqueryType) {
            return generateProjectionsInTableSegmentSubquery(encryptColumn, columnProjection, subqueryType);
        }
        if (SubqueryType.PREDICATE == subqueryType) {
            return Collections.singleton(generateProjectionInPredicateSubquery(encryptColumn, columnProjection));
        }
        if (SubqueryType.INSERT_SELECT == subqueryType) {
            return generateProjectionsInInsertSelectSubquery(encryptColumn, columnProjection);
        }
        throw new UnsupportedSQLOperationException(
                "Projections not in simple select, table subquery, join subquery, predicate subquery and insert select subquery are not supported in encrypt feature.");
    }
    
    private ColumnProjection generateProjection(final EncryptColumn encryptColumn, final ColumnProjection columnProjection) {
        IdentifierValue cipherColumnName = new IdentifierValue(encryptColumn.getCipher().getName(), columnProjection.getName().getQuoteCharacter());
        return new ColumnProjection(columnProjection.getOwner().orElse(null), cipherColumnName, columnProjection.getAlias().orElse(columnProjection.getName()), databaseType,
                columnProjection.getLeftParentheses().orElse(null), columnProjection.getRightParentheses().orElse(null));
    }
    
    private Collection<Projection> generateProjectionsInTableSegmentSubquery(final EncryptColumn encryptColumn, final ColumnProjection columnProjection, final SubqueryType subqueryType) {
        Collection<Projection> result = new LinkedList<>();
        QuoteCharacter quoteCharacter = columnProjection.getName().getQuoteCharacter();
        IdentifierValue cipherColumnName = new IdentifierValue(encryptColumn.getCipher().getName(), quoteCharacter);
        IdentifierValue alias = SubqueryType.JOIN == subqueryType ? null : columnProjection.getAlias().orElse(columnProjection.getName());
        ParenthesesSegment leftParentheses = columnProjection.getLeftParentheses().orElse(null);
        ParenthesesSegment rightParentheses = columnProjection.getRightParentheses().orElse(null);
        result.add(new ColumnProjection(columnProjection.getOwner().orElse(null), cipherColumnName, alias, databaseType, leftParentheses, rightParentheses));
        IdentifierValue assistedColumOwner = columnProjection.getOwner().orElse(null);
        encryptColumn.getAssistedQuery().ifPresent(
                optional -> result.add(new ColumnProjection(assistedColumOwner, new IdentifierValue(optional.getName(), quoteCharacter), null, databaseType, leftParentheses, rightParentheses)));
        encryptColumn.getLikeQuery().ifPresent(
                optional -> result.add(new ColumnProjection(assistedColumOwner, new IdentifierValue(optional.getName(), quoteCharacter), null, databaseType, leftParentheses, rightParentheses)));
        return result;
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
        IdentifierValue assistedColumOwner = columnProjection.getOwner().orElse(null);
        encryptColumn.getAssistedQuery().ifPresent(
                optional -> result.add(new ColumnProjection(assistedColumOwner, new IdentifierValue(optional.getName(), quoteCharacter), null, databaseType, leftParentheses, rightParentheses)));
        encryptColumn.getLikeQuery().ifPresent(
                optional -> result.add(new ColumnProjection(assistedColumOwner, new IdentifierValue(optional.getName(), quoteCharacter), null, databaseType, leftParentheses, rightParentheses)));
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
