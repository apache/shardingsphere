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
import org.apache.shardingsphere.encrypt.rewrite.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Projection token generator for encrypt.
 */
@Setter
public final class EncryptProjectionTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext>, PreviousSQLTokensAware, SchemaMetaDataAware, EncryptRuleAware {
    
    private List<SQLToken> previousSQLTokens;
    
    private EncryptRule encryptRule;
    
    private String databaseName;
    
    private Map<String, ShardingSphereSchema> schemas;
    
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
        Map<String, String> columnTableNames = getColumnTableNames(selectStatementContext);
        for (ProjectionSegment each : selectStatementContext.getSqlStatement().getProjections().getProjections()) {
            SubqueryType subqueryType = selectStatementContext.getSubqueryType();
            if (each instanceof ColumnProjectionSegment) {
                ColumnProjectionSegment columnSegment = (ColumnProjectionSegment) each;
                ColumnProjection columnProjection = buildColumnProjection(columnSegment);
                String tableName = columnTableNames.get(columnProjection.getColumnName());
                if (null == tableName) {
                    continue;
                }
                Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
                if (encryptTable.isPresent() && encryptTable.get().isEncryptColumn(columnProjection.getName().getValue()) && !containsTableSubquery(selectStatementContext)) {
                    sqlTokens.add(generateSQLToken(encryptTable.get().getEncryptColumn(columnProjection.getName().getValue()), columnSegment, columnProjection, subqueryType));
                }
            }
            if (each instanceof ShorthandProjectionSegment) {
                ShorthandProjectionSegment shorthandSegment = (ShorthandProjectionSegment) each;
                Collection<Projection> actualColumns = getShorthandProjection(shorthandSegment, selectStatementContext.getProjectionsContext()).getActualColumns();
                if (!actualColumns.isEmpty()) {
                    sqlTokens.add(generateSQLToken(shorthandSegment, actualColumns, selectStatementContext, subqueryType, columnTableNames));
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
                                                          final SelectStatementContext selectStatementContext, final SubqueryType subqueryType, final Map<String, String> columnTableNames) {
        List<Projection> projections = new LinkedList<>();
        for (Projection each : actualColumns) {
            String tableName = columnTableNames.get(each.getColumnName());
            Optional<EncryptTable> encryptTable = null == tableName ? Optional.empty() : encryptRule.findEncryptTable(tableName);
            if (!encryptTable.isPresent() || !encryptTable.get().isEncryptColumn(each.getColumnLabel()) || containsTableSubquery(selectStatementContext)) {
                projections.add(each.getAlias().map(optional -> (Projection) new ColumnProjection(null, optional, null)).orElse(each));
            } else if (each instanceof ColumnProjection) {
                projections.addAll(generateProjections(encryptTable.get().getEncryptColumn(((ColumnProjection) each).getName().getValue()), (ColumnProjection) each, subqueryType, true, segment));
            }
        }
        int startIndex = segment.getOwner().isPresent() ? segment.getOwner().get().getStartIndex() : segment.getStartIndex();
        previousSQLTokens.removeIf(each -> each.getStartIndex() == startIndex);
        return new SubstitutableColumnNameToken(startIndex, segment.getStopIndex(), projections, selectStatementContext.getDatabaseType().getQuoteCharacter());
    }
    
    private boolean containsTableSubquery(final SelectStatementContext selectStatementContext) {
        if (selectStatementContext.getSqlStatement().getFrom() instanceof SubqueryTableSegment) {
            return true;
        } else if (selectStatementContext.getSqlStatement().getFrom() instanceof JoinTableSegment) {
            JoinTableSegment joinTableSegment = (JoinTableSegment) selectStatementContext.getSqlStatement().getFrom();
            return joinTableSegment.getLeft() instanceof SubqueryTableSegment || joinTableSegment.getRight() instanceof SubqueryTableSegment;
        }
        return false;
    }
    
    private ColumnProjection buildColumnProjection(final ColumnProjectionSegment segment) {
        IdentifierValue owner = segment.getColumn().getOwner().map(OwnerSegment::getIdentifier).orElse(null);
        return new ColumnProjection(owner, segment.getColumn().getIdentifier(), segment.getAliasName().isPresent() ? segment.getAlias().orElse(null) : null);
    }
    
    private Map<String, String> getColumnTableNames(final SelectStatementContext selectStatementContext) {
        Collection<ColumnProjection> columns = new LinkedList<>();
        for (Projection projection : selectStatementContext.getProjectionsContext().getProjections()) {
            if (projection instanceof ColumnProjection) {
                columns.add((ColumnProjection) projection);
            }
            if (projection instanceof ShorthandProjection) {
                columns.addAll(((ShorthandProjection) projection).getColumnProjections());
            }
        }
        String defaultSchema = DatabaseTypeEngine.getDefaultSchemaName(selectStatementContext.getDatabaseType(), databaseName);
        ShardingSphereSchema schema = selectStatementContext.getTablesContext().getSchemaName().map(schemas::get).orElseGet(() -> schemas.get(defaultSchema));
        return selectStatementContext.getTablesContext().findTableNamesByColumnProjection(columns, schema);
    }
    
    private Collection<Projection> generateProjections(final EncryptColumn encryptColumn, final ColumnProjection column,
                                                       final SubqueryType subqueryType, final boolean shorthand, final ShorthandProjectionSegment segment) {
        Collection<Projection> result = new LinkedList<>();
        if (SubqueryType.PREDICATE_SUBQUERY == subqueryType) {
            result.add(distinctOwner(generatePredicateSubqueryProjection(encryptColumn, column), shorthand));
        } else if (SubqueryType.TABLE_SUBQUERY == subqueryType) {
            result.addAll(generateTableSubqueryProjections(encryptColumn, column, shorthand));
        } else if (SubqueryType.EXISTS_SUBQUERY == subqueryType) {
            result.addAll(generateExistsSubqueryProjections(encryptColumn, column, shorthand));
        } else {
            result.add(distinctOwner(generateCommonProjection(encryptColumn, column, segment), shorthand));
        }
        return result;
    }
    
    private ColumnProjection distinctOwner(final ColumnProjection column, final boolean shorthand) {
        if (shorthand || !column.getOwner().isPresent()) {
            return column;
        }
        return new ColumnProjection(null, column.getName(), column.getAlias().isPresent() ? column.getAlias().get() : null);
    }
    
    private ColumnProjection generatePredicateSubqueryProjection(final EncryptColumn encryptColumn, final ColumnProjection column) {
        Optional<AssistedQueryColumnItem> assistedQueryColumn = encryptColumn.getAssistedQuery();
        if (assistedQueryColumn.isPresent()) {
            return new ColumnProjection(column.getOwner().orElse(null), new IdentifierValue(assistedQueryColumn.get().getName(), column.getName().getQuoteCharacter()), null);
        }
        String cipherColumn = encryptColumn.getCipher().getName();
        return new ColumnProjection(column.getOwner().orElse(null), new IdentifierValue(cipherColumn, column.getName().getQuoteCharacter()), null);
    }
    
    private Collection<ColumnProjection> generateTableSubqueryProjections(final EncryptColumn encryptColumn, final ColumnProjection column, final boolean shorthand) {
        Collection<ColumnProjection> result = new LinkedList<>();
        result.add(distinctOwner(new ColumnProjection(column.getOwner().orElse(null), new IdentifierValue(encryptColumn.getCipher().getName(),
                column.getName().getQuoteCharacter()), column.getAlias().orElse(column.getName())), shorthand));
        encryptColumn.getAssistedQuery().ifPresent(optional -> result.add(
                new ColumnProjection(column.getOwner().orElse(null), new IdentifierValue(optional.getName(), column.getName().getQuoteCharacter()), null)));
        return result;
    }
    
    private Collection<ColumnProjection> generateExistsSubqueryProjections(final EncryptColumn encryptColumn, final ColumnProjection column, final boolean shorthand) {
        Collection<ColumnProjection> result = new LinkedList<>();
        result.add(distinctOwner(new ColumnProjection(column.getOwner().orElse(null), new IdentifierValue(encryptColumn.getCipher().getName(),
                column.getName().getQuoteCharacter()), null), shorthand));
        return result;
    }
    
    private ColumnProjection generateCommonProjection(final EncryptColumn encryptColumn, final ColumnProjection column, final ShorthandProjectionSegment segment) {
        String queryColumnName = encryptColumn.getCipher().getName();
        IdentifierValue owner = (null == segment || !segment.getOwner().isPresent()) ? column.getOwner().orElse(null) : segment.getOwner().get().getIdentifier();
        return new ColumnProjection(owner, new IdentifierValue(queryColumnName, column.getName().getQuoteCharacter()), column.getAlias().isPresent()
                ? column.getAlias().get()
                : column.getName());
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
