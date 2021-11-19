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
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
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
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;

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
public final class EncryptProjectionTokenGenerator extends BaseEncryptSQLTokenGenerator 
        implements CollectionSQLTokenGenerator<SQLStatementContext>, QueryWithCipherColumnAware, PreviousSQLTokensAware, SchemaMetaDataAware {
    
    private boolean queryWithCipherColumn;
    
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
        TablesContext tablesContext = selectStatementContext.getTablesContext();
        if (projection instanceof ColumnProjectionSegment) {
            ColumnProjectionSegment columnSegment = (ColumnProjectionSegment) projection;
            ColumnProjection columnProjection = buildColumnProjection(columnSegment);
            String tableName = columnTableNames.get(columnProjection.getExpression());
            if (getEncryptRule().findEncryptor(tableName, columnProjection.getName()).isPresent() 
                    && isOwnerSameWithTableNameOrAlias(tableName, columnProjection.getOwner(), tablesContext)) {
                result.add(generateSQLTokens(columnSegment, tableName, subqueryType));
            }
            Optional<String> subqueryTableName = tablesContext.findTableNameFromSubquery(columnProjection.getName(), columnProjection.getOwner());
            subqueryTableName.ifPresent(optional -> result.add(generateSQLTokens(columnSegment, optional, subqueryType)));
        }
        if (projection instanceof ShorthandProjectionSegment) {
            ShorthandProjectionSegment shorthandSegment = (ShorthandProjectionSegment) projection;
            ShorthandProjection shorthandProjection = getShorthandProjection(shorthandSegment, selectStatementContext.getProjectionsContext());
            if (!shorthandProjection.getActualColumns().isEmpty()) {
                result.add(generateSQLTokens(shorthandSegment, shorthandProjection, selectStatementContext.getDatabaseType(), subqueryType, tablesContext, columnTableNames));
            }
        }
        return result;
    }
    
    private ColumnProjection buildColumnProjection(final ColumnProjectionSegment segment) {
        String owner = segment.getColumn().getOwner().map(optional -> optional.getIdentifier().getValue()).orElse(null);
        return new ColumnProjection(owner, segment.getColumn().getIdentifier().getValue(), segment.getAlias().orElse(null));
    }
    
    private SubstitutableColumnNameToken generateSQLTokens(final ColumnProjectionSegment segment, final String tableName, final SubqueryType subqueryType) {
        String columnName = segment.getColumn().getIdentifier().getValue();
        String alias = segment.getAlias().orElseGet(() -> segment.getColumn().getIdentifier().getValue());
        Collection<ColumnProjection> projections = generateProjections(tableName, columnName, alias, null, subqueryType);
        int startIndex = segment.getColumn().getOwner().isPresent() ? segment.getColumn().getOwner().get().getStopIndex() + 2 : segment.getColumn().getStartIndex();
        int stopIndex = segment.getStopIndex();
        return new SubstitutableColumnNameToken(startIndex, stopIndex, projections);
    }
    
    private SubstitutableColumnNameToken generateSQLTokens(final ShorthandProjectionSegment segment, final ShorthandProjection shorthandProjection, final DatabaseType databaseType, 
                                                           final SubqueryType subqueryType, final TablesContext tablesContext, final Map<String, String> columnTableNames) {
        List<ColumnProjection> projections = new LinkedList<>();
        for (ColumnProjection each : shorthandProjection.getActualColumns().values()) {
            String tableName = columnTableNames.get(each.getExpression());
            Optional<EncryptAlgorithm> encryptor = getEncryptRule().findEncryptor(tableName, each.getName());
            if (encryptor.isPresent()) {
                String owner = null == each.getOwner() ? null : each.getOwner();
                projections.addAll(generateProjections(tableName, each.getName(), each.getName(), owner, subqueryType));
            } else {
                projections.add(new ColumnProjection(each.getOwner(), each.getName(), each.getAlias().orElse(null)));
            }
        }
        previousSQLTokens.removeIf(each -> each.getStartIndex() == segment.getStartIndex());
        return new SubstitutableColumnNameToken(segment.getStartIndex(), segment.getStopIndex(), projections, databaseType.getQuoteCharacter());
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
    
    private boolean isOwnerSameWithTableNameOrAlias(final String tableName, final String owner, final TablesContext tablesContext) {
        if (Strings.isNullOrEmpty(owner)) {
            return true;
        }
        return tablesContext.findTableNameFromSQL(owner).filter(optional -> optional.equals(tableName)).isPresent();
    }
    
    private Collection<ColumnProjection> generateProjections(final String tableName, final String columnName, final String alias, final String owner, final SubqueryType subqueryType) {
        Collection<ColumnProjection> result = new LinkedList<>();
        if (SubqueryType.PREDICATE_SUBQUERY.equals(subqueryType)) {
            result.add(generatePredicateSubqueryProjection(tableName, columnName, owner));
        } else if (SubqueryType.TABLE_SUBQUERY.equals(subqueryType)) {
            result.addAll(generateTableSubqueryProjections(tableName, columnName, alias, owner));
        } else {
            result.add(generateCommonProjection(tableName, columnName, alias, owner));
        }
        return result;
    }
    
    private ColumnProjection generatePredicateSubqueryProjection(final String tableName, final String columnName, final String owner) {
        Boolean queryWithCipherColumn = getEncryptRule().findEncryptTable(tableName).map(EncryptTable::getQueryWithCipherColumn).orElse(null);
        if (Boolean.FALSE.equals(queryWithCipherColumn) || !this.queryWithCipherColumn) {
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
    
    @Override
    public void setSchema(final ShardingSphereSchema schema) {
        this.schema = schema;
    }
}
