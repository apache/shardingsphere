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
import org.apache.shardingsphere.encrypt.rewrite.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.constant.SubqueryType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;

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
public final class EncryptProjectionTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext>, PreviousSQLTokensAware, SchemaMetaDataAware {
    
    private List<SQLToken> previousSQLTokens;
    
    private ShardingSphereSchema schema;
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && !((SelectStatementContext) sqlStatementContext).getAllTables().isEmpty();    
    }
    
    @Override
    public Collection<SubstitutableColumnNameToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Preconditions.checkState(sqlStatementContext instanceof SelectStatementContext);
        Collection<SubstitutableColumnNameToken> result = new LinkedHashSet<>();
        for (SelectStatementContext each : getSelectStatementContexts((SelectStatementContext) sqlStatementContext)) {
            Map<String, String> columnTableNames = getColumnTableNames(each);
            for (ProjectionSegment projection : each.getSqlStatement().getProjections().getProjections()) {
                result.addAll(generateSQLTokens(each, projection, columnTableNames));
            }
        }
        return result;
    }
    
    private Collection<SubstitutableColumnNameToken> generateSQLTokens(final SelectStatementContext selectStatementContext, 
                                                                       final ProjectionSegment projection, final Map<String, String> columnTableNames) {
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        SubqueryType subqueryType = selectStatementContext.getSubqueryType();
        if (projection instanceof ColumnProjectionSegment) {
            ColumnProjectionSegment columnSegment = (ColumnProjectionSegment) projection;
            ColumnProjection columnProjection = buildColumnProjection(columnSegment);
            String tableName = columnTableNames.get(columnProjection.getExpression());
            if (null != tableName && getEncryptRule().findEncryptor(tableName, columnProjection.getName()).isPresent()) {
                result.add(generateSQLTokens(tableName, columnSegment, columnProjection, subqueryType));
            }
        }
        if (projection instanceof ShorthandProjectionSegment) {
            ShorthandProjectionSegment shorthandSegment = (ShorthandProjectionSegment) projection;
            Collection<ColumnProjection> actualColumns = getShorthandProjection(shorthandSegment, selectStatementContext.getProjectionsContext()).getActualColumns().values();
            if (!actualColumns.isEmpty()) {
                result.add(generateSQLTokens(shorthandSegment, actualColumns, selectStatementContext.getDatabaseType(), subqueryType, columnTableNames));
            }
        }
        return result;
    }
    
    private SubstitutableColumnNameToken generateSQLTokens(final String tableName, final ColumnProjectionSegment columnSegment, 
                                                           final ColumnProjection columnProjection, final SubqueryType subqueryType) {
        Collection<ColumnProjection> projections = generateProjections(tableName, columnProjection, subqueryType, false);
        int startIndex = columnSegment.getColumn().getOwner().isPresent() ? columnSegment.getColumn().getOwner().get().getStopIndex() + 2 : columnSegment.getColumn().getStartIndex();
        int stopIndex = columnSegment.getStopIndex();
        return new SubstitutableColumnNameToken(startIndex, stopIndex, projections);
    }
    
    private SubstitutableColumnNameToken generateSQLTokens(final ShorthandProjectionSegment segment, final Collection<ColumnProjection> actualColumns, 
                                                           final DatabaseType databaseType, final SubqueryType subqueryType, final Map<String, String> columnTableNames) {
        List<ColumnProjection> projections = new LinkedList<>();
        for (ColumnProjection each : actualColumns) {
            String tableName = columnTableNames.get(each.getExpression());
            if (null != tableName && getEncryptRule().findEncryptor(tableName, each.getName()).isPresent()) {
                projections.addAll(generateProjections(tableName, each, subqueryType, true));
            } else {
                projections.add(new ColumnProjection(each.getOwner(), each.getName(), each.getAlias().orElse(null)));
            }
        }
        previousSQLTokens.removeIf(each -> each.getStartIndex() == segment.getStartIndex());
        return new SubstitutableColumnNameToken(segment.getStartIndex(), segment.getStopIndex(), projections, databaseType.getQuoteCharacter());
    }
    
    private ColumnProjection buildColumnProjection(final ColumnProjectionSegment segment) {
        String owner = segment.getColumn().getOwner().map(optional -> optional.getIdentifier().getValue()).orElse(null);
        return new ColumnProjection(owner, segment.getColumn().getIdentifier().getValue(), segment.getAlias().orElse(null));
    }
    
    private Map<String, String> getColumnTableNames(final SelectStatementContext selectStatementContext) {
        Collection<ColumnProjection> columns = new LinkedList<>();
        for (Projection projection : selectStatementContext.getProjectionsContext().getProjections()) {
            if (projection instanceof ColumnProjection) {
                columns.add((ColumnProjection) projection);
            }
            if (projection instanceof ShorthandProjection) {
                columns.addAll(((ShorthandProjection) projection).getActualColumns().values());
            }
        }
        return selectStatementContext.getTablesContext().findTableName(columns, schema);
    }
    
    private Collection<SelectStatementContext> getSelectStatementContexts(final SelectStatementContext selectStatementContext) {
        Collection<SelectStatementContext> result = new LinkedList<>();
        result.add(selectStatementContext);
        result.addAll(selectStatementContext.getSubqueryContexts().values());
        return result;
    }
    
    private Collection<ColumnProjection> generateProjections(final String tableName, final ColumnProjection column, final SubqueryType subqueryType, final boolean shorthand) {
        Collection<ColumnProjection> result = new LinkedList<>();
        if (SubqueryType.PREDICATE_SUBQUERY.equals(subqueryType)) {
            result.add(generatePredicateSubqueryProjection(tableName, column));
        } else if (SubqueryType.TABLE_SUBQUERY.equals(subqueryType)) {
            result.addAll(generateTableSubqueryProjections(tableName, column));
        } else {
            result.add(generateCommonProjection(tableName, column, shorthand));
        }
        return result;
    }
    
    private ColumnProjection generatePredicateSubqueryProjection(final String tableName, final ColumnProjection column) {
        boolean queryWithCipherColumn = getEncryptRule().isQueryWithCipherColumn(tableName);
        if (!queryWithCipherColumn) {
            Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, column.getName());
            if (plainColumn.isPresent()) {
                return new ColumnProjection(column.getOwner(), plainColumn.get(), null);
            }
        }
        Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(tableName, column.getName());
        if (assistedQueryColumn.isPresent()) {
            return new ColumnProjection(column.getOwner(), assistedQueryColumn.get(), null);
        }
        String cipherColumn = getEncryptRule().getCipherColumn(tableName, column.getName());
        return new ColumnProjection(column.getOwner(), cipherColumn, null);
    }
    
    private Collection<ColumnProjection> generateTableSubqueryProjections(final String tableName, final ColumnProjection column) {
        Collection<ColumnProjection> result = new LinkedList<>();
        result.add(new ColumnProjection(column.getOwner(), getEncryptRule().getCipherColumn(tableName, column.getName()), column.getAlias().orElse(column.getName())));
        Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(tableName, column.getName());
        assistedQueryColumn.ifPresent(optional -> result.add(new ColumnProjection(column.getOwner(), optional, null)));
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, column.getName());
        plainColumn.ifPresent(optional -> result.add(new ColumnProjection(column.getOwner(), optional, null)));
        return result;
    }
    
    private ColumnProjection generateCommonProjection(final String tableName, final ColumnProjection column, final boolean shorthand) {
        String encryptColumnName = getEncryptColumnName(tableName, column.getName());
        String owner = shorthand ? column.getOwner() : null;
        return new ColumnProjection(owner, encryptColumnName, column.getAlias().orElse(column.getName()));
    }
    
    private String getEncryptColumnName(final String tableName, final String logicEncryptColumnName) {
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, logicEncryptColumnName);
        boolean queryWithCipherColumn = getEncryptRule().isQueryWithCipherColumn(tableName);
        if (!queryWithCipherColumn) {
            return plainColumn.orElseGet(() -> getEncryptRule().getCipherColumn(tableName, logicEncryptColumnName));
        }
        return getEncryptRule().getCipherColumn(tableName, logicEncryptColumnName);
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
    
    @Override
    public void setSchema(final ShardingSphereSchema schema) {
        this.schema = schema;
    }
}
