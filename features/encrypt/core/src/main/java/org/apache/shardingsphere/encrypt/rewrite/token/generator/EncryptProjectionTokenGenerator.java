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

import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.rewrite.aware.DatabaseTypeAware;
import org.apache.shardingsphere.encrypt.rewrite.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.DerivedColumn;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Projection token generator for encrypt.
 */
@Setter
public final class EncryptProjectionTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext>, PreviousSQLTokensAware, EncryptRuleAware, DatabaseTypeAware {
    
    private List<SQLToken> previousSQLTokens;
    
    private EncryptRule encryptRule;
    
    private DatabaseType databaseType;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && !((SelectStatementContext) sqlStatementContext).getAllTables().isEmpty();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Preconditions.checkState(sqlStatementContext instanceof SelectStatementContext);
        Collection<SQLToken> result = new LinkedHashSet<>();
        SelectStatementContext selectStatementContext = (SelectStatementContext) sqlStatementContext;
        addGenerateSQLTokens(result, selectStatementContext);
        for (SelectStatementContext each : selectStatementContext.getSubqueryContexts().values()) {
            addGenerateSQLTokens(result, each);
        }
        return result;
    }
    
    private void addGenerateSQLTokens(final Collection<SQLToken> sqlTokens, final SelectStatementContext selectStatementContext) {
        for (ProjectionSegment each : selectStatementContext.getSqlStatement().getProjections().getProjections()) {
            SubqueryType subqueryType = selectStatementContext.getSubqueryType();
            if (each instanceof ColumnProjectionSegment) {
                ColumnProjectionSegment columnSegment = (ColumnProjectionSegment) each;
                ColumnProjection columnProjection = buildColumnProjection(columnSegment);
                String originalColumnName = columnProjection.getOriginalColumn().getValue();
                Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(columnProjection.getOriginalTable().getValue());
                if (encryptTable.isPresent() && encryptTable.get().isEncryptColumn(originalColumnName) && !selectStatementContext.containsTableSubquery()) {
                    sqlTokens.add(generateSQLToken(encryptTable.get().getEncryptColumn(originalColumnName), columnSegment, columnProjection, subqueryType));
                }
            }
            ShardingSpherePreconditions.checkState(!(each instanceof ShorthandProjectionSegment) || !selectStatementContext.containsTableSubquery(),
                    () -> new UnsupportedSQLOperationException("Can not support encrypt shorthand expand with subquery statement"));
            if (each instanceof ShorthandProjectionSegment) {
                ShorthandProjectionSegment shorthandSegment = (ShorthandProjectionSegment) each;
                Collection<Projection> actualColumns = getShorthandProjection(shorthandSegment, selectStatementContext.getProjectionsContext()).getActualColumns();
                if (!actualColumns.isEmpty()) {
                    sqlTokens.add(generateSQLToken(shorthandSegment, actualColumns, selectStatementContext, subqueryType));
                }
            }
        }
    }
    
    private SubstitutableColumnNameToken generateSQLToken(final EncryptColumn encryptColumn, final ColumnProjectionSegment columnSegment,
                                                          final ColumnProjection columnProjection, final SubqueryType subqueryType) {
        Collection<Projection> projections = generateProjections(encryptColumn, columnProjection, subqueryType, false, null);
        int startIndex = columnSegment.getColumn().getOwner().isPresent() ? columnSegment.getColumn().getOwner().get().getStopIndex() + 2 : columnSegment.getColumn().getStartIndex();
        int stopIndex = columnSegment.getStopIndex();
        return new SubstitutableColumnNameToken(startIndex, stopIndex, projections);
    }
    
    private SubstitutableColumnNameToken generateSQLToken(final ShorthandProjectionSegment segment, final Collection<Projection> actualColumns,
                                                          final SelectStatementContext selectStatementContext, final SubqueryType subqueryType) {
        List<Projection> projections = new LinkedList<>();
        for (Projection each : actualColumns) {
            if (each instanceof ColumnProjection) {
                ColumnProjection columnProjection = (ColumnProjection) each;
                Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(columnProjection.getOriginalTable().getValue());
                if (encryptTable.isPresent() && encryptTable.get().isEncryptColumn(columnProjection.getOriginalColumn().getValue()) && !selectStatementContext.containsTableSubquery()) {
                    EncryptColumn encryptColumn = encryptTable.get().getEncryptColumn(columnProjection.getOriginalColumn().getValue());
                    projections.addAll(generateProjections(encryptColumn, columnProjection, subqueryType, true, segment));
                    continue;
                }
            }
            projections.add(each.getAlias().filter(alias -> !DerivedColumn.isDerivedColumnName(alias.getValue()))
                    .map(optional -> (Projection) new ColumnProjection(null, optional, null, databaseType)).orElse(each));
        }
        int startIndex = segment.getOwner().isPresent() ? segment.getOwner().get().getStartIndex() : segment.getStartIndex();
        previousSQLTokens.removeIf(each -> each.getStartIndex() == startIndex);
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(selectStatementContext.getDatabaseType()).getDialectDatabaseMetaData();
        return new SubstitutableColumnNameToken(startIndex, segment.getStopIndex(), projections, dialectDatabaseMetaData.getQuoteCharacter());
    }
    
    private ColumnProjection buildColumnProjection(final ColumnProjectionSegment segment) {
        IdentifierValue owner = segment.getColumn().getOwner().map(OwnerSegment::getIdentifier).orElse(null);
        ColumnProjection result = new ColumnProjection(owner, segment.getColumn().getIdentifier(), segment.getAliasName().isPresent() ? segment.getAlias().orElse(null) : null, databaseType);
        result.setOriginalColumn(segment.getColumn().getOriginalColumn());
        result.setOriginalTable(segment.getColumn().getOriginalTable());
        return result;
    }
    
    private Collection<Projection> generateProjections(final EncryptColumn encryptColumn, final ColumnProjection columnProjection,
                                                       final SubqueryType subqueryType, final boolean shorthandProjection, final ShorthandProjectionSegment segment) {
        Collection<Projection> result = new LinkedList<>();
        if (SubqueryType.PREDICATE_SUBQUERY == subqueryType) {
            result.add(generatePredicateSubqueryProjection(encryptColumn, columnProjection, shorthandProjection));
        } else if (SubqueryType.TABLE_SUBQUERY == subqueryType || SubqueryType.JOIN_SUBQUERY == subqueryType) {
            result.addAll(generateTableSegmentSubqueryProjections(encryptColumn, columnProjection, shorthandProjection, subqueryType));
        } else if (SubqueryType.EXISTS_SUBQUERY == subqueryType) {
            result.addAll(generateExistsSubqueryProjections(encryptColumn, columnProjection, shorthandProjection));
        } else {
            result.add(generateCommonProjection(encryptColumn, columnProjection, segment, shorthandProjection));
        }
        return result;
    }
    
    private ColumnProjection generatePredicateSubqueryProjection(final EncryptColumn encryptColumn, final ColumnProjection columnProjection, final boolean shorthandProjection) {
        Optional<AssistedQueryColumnItem> assistedQueryColumn = encryptColumn.getAssistedQuery();
        IdentifierValue owner = columnProjection.getOwner().orElse(null);
        return assistedQueryColumn
                .map(optional -> new ColumnProjection(owner, new IdentifierValue(optional.getName(), columnProjection.getName().getQuoteCharacter()), null, databaseType))
                .orElseGet(() -> createCipherColumn(owner, new IdentifierValue(encryptColumn.getCipher().getName(), columnProjection.getName().getQuoteCharacter()),
                        columnProjection.getAlias().orElse(columnProjection.getName()), shorthandProjection));
    }
    
    private Collection<ColumnProjection> generateTableSegmentSubqueryProjections(final EncryptColumn encryptColumn, final ColumnProjection columnProjection, final boolean shorthandProjection,
                                                                                 final SubqueryType subqueryType) {
        Collection<ColumnProjection> result = new LinkedList<>();
        IdentifierValue owner = columnProjection.getOwner().orElse(null);
        QuoteCharacter quoteCharacter = columnProjection.getName().getQuoteCharacter();
        IdentifierValue columnName = new IdentifierValue(encryptColumn.getCipher().getName(), quoteCharacter);
        IdentifierValue alias = SubqueryType.JOIN_SUBQUERY == subqueryType ? null : columnProjection.getAlias().orElse(columnProjection.getName());
        result.add(createCipherColumn(owner, columnName, alias, shorthandProjection));
        encryptColumn.getAssistedQuery().ifPresent(optional -> result.add(new ColumnProjection(owner, new IdentifierValue(optional.getName(), quoteCharacter), null, databaseType)));
        encryptColumn.getLikeQuery().ifPresent(optional -> result.add(new ColumnProjection(owner, new IdentifierValue(optional.getName(), quoteCharacter), null, databaseType)));
        return result;
    }
    
    private ColumnProjection createCipherColumn(final IdentifierValue owner, final IdentifierValue columnName, final IdentifierValue alias, final boolean shorthandProjection) {
        return new ColumnProjection(shorthandProjection ? owner : null, columnName, alias, databaseType);
    }
    
    private Collection<ColumnProjection> generateExistsSubqueryProjections(final EncryptColumn encryptColumn, final ColumnProjection columnProjection, final boolean shorthandProjection) {
        Collection<ColumnProjection> result = new LinkedList<>();
        IdentifierValue columnName = new IdentifierValue(encryptColumn.getCipher().getName(), columnProjection.getName().getQuoteCharacter());
        result.add(createCipherColumn(columnProjection.getOwner().orElse(null), columnName, columnProjection.getAlias().orElse(columnProjection.getName()), shorthandProjection));
        return result;
    }
    
    private ColumnProjection generateCommonProjection(final EncryptColumn encryptColumn, final ColumnProjection columnProjection, final ShorthandProjectionSegment segment,
                                                      final boolean shorthandProjection) {
        IdentifierValue owner = (null == segment || !segment.getOwner().isPresent()) ? columnProjection.getOwner().orElse(null) : segment.getOwner().get().getIdentifier();
        IdentifierValue columnName = new IdentifierValue(encryptColumn.getCipher().getName(), columnProjection.getName().getQuoteCharacter());
        return createCipherColumn(owner, columnName, columnProjection.getAlias().orElse(columnProjection.getName()), shorthandProjection);
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
