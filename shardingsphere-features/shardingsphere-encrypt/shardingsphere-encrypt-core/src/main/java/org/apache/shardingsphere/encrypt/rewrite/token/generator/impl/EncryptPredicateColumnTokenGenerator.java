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

import lombok.Setter;
import org.apache.shardingsphere.encrypt.rewrite.aware.QueryWithCipherColumnAware;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SubqueryTableContext;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtil;
import org.apache.shardingsphere.sql.parser.sql.common.util.WhereExtractUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Predicate column token generator for encrypt.
 */
@Setter
public final class EncryptPredicateColumnTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator, SchemaMetaDataAware, QueryWithCipherColumnAware {
    
    private ShardingSphereSchema schema;
    
    private boolean queryWithCipherColumn;
    
    @SuppressWarnings("rawtypes")
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        boolean containsJoinQueryOrSubquery = sqlStatementContext instanceof SelectStatementContext 
                && (((SelectStatementContext) sqlStatementContext).isContainsJoinQuery() || ((SelectStatementContext) sqlStatementContext).isContainsSubquery());
        boolean containsInsertSelectContext = (sqlStatementContext instanceof InsertStatementContext) && null != ((InsertStatementContext) sqlStatementContext).getInsertSelectContext();
        return containsJoinQueryOrSubquery || containsInsertSelectContext || (sqlStatementContext instanceof WhereAvailable && ((WhereAvailable) sqlStatementContext).getWhere().isPresent());
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public Collection<SubstitutableColumnNameToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<SubstitutableColumnNameToken> result = new LinkedHashSet<>();
        for (WhereSegment each : getWhereSegments(sqlStatementContext)) {
            Collection<AndPredicate> andPredicates = ExpressionExtractUtil.getAndPredicates(each.getExpr());
            Map<String, String> columnTableNames = getColumnTableNames(sqlStatementContext, andPredicates);
            for (AndPredicate predicate : andPredicates) {
                result.addAll(generateSQLTokens(sqlStatementContext, predicate.getPredicates(), columnTableNames));
            }
        }
        return result;
    }

    private Collection<SubstitutableColumnNameToken> generateSQLTokens(final SQLStatementContext sqlStatementContext, final Collection<ExpressionSegment> predicates,
                                                                       final Map<String, String> columnTableNames) {
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        SubqueryTableContext subqueryTableContext = new SubqueryTableContext();
        if (sqlStatementContext instanceof SelectStatementContext) {
            subqueryTableContext = ((SelectStatementContext) sqlStatementContext).getSubqueryTableContext();
        }
        for (ExpressionSegment each : predicates) {
            for (ColumnSegment column : ColumnExtractor.extract(each)) {
                int startIndex = column.getOwner().isPresent() ? column.getOwner().get().getStopIndex() + 2 : column.getStartIndex();
                int stopIndex = column.getStopIndex();
                subqueryTableContext.getAssistedQueryColumn(column).ifPresent(item -> result.add(new SubstitutableColumnNameToken(startIndex, stopIndex, getColumnProjections(item))));
                Optional<EncryptTable> encryptTable = findEncryptTable(columnTableNames, column);
                if (!encryptTable.isPresent() || !encryptTable.get().findEncryptorName(column.getIdentifier().getValue()).isPresent()) {
                    continue;
                }
                EncryptTable table = encryptTable.get();
                if (Boolean.FALSE.equals(table.getQueryWithCipherColumn()) || !queryWithCipherColumn) {
                    Optional<String> plainColumn = encryptTable.get().findPlainColumn(column.getIdentifier().getValue());
                    if (plainColumn.isPresent()) {
                        result.add(new SubstitutableColumnNameToken(startIndex, stopIndex, getColumnProjections(plainColumn.get())));
                        continue;
                    }
                }
                Optional<String> assistedQueryColumn = encryptTable.get().findAssistedQueryColumn(column.getIdentifier().getValue());
                SubstitutableColumnNameToken encryptColumnNameToken = assistedQueryColumn.map(columnName
                    -> new SubstitutableColumnNameToken(startIndex, stopIndex, getColumnProjections(columnName))).orElseGet(()
                        -> new SubstitutableColumnNameToken(startIndex, stopIndex, getColumnProjections(encryptTable.get().getCipherColumn(column.getIdentifier().getValue()))));
                result.add(encryptColumnNameToken);
            }
        }
        return result;
    }
    
    private Collection<WhereSegment> getWhereSegments(final SQLStatementContext<?> sqlStatementContext) {
        Collection<WhereSegment> result = new LinkedList<>();
        if (sqlStatementContext instanceof WhereAvailable) {
            ((WhereAvailable) sqlStatementContext).getWhere().ifPresent(result::add);
        }
        if (sqlStatementContext instanceof SelectStatementContext) {
            result.addAll(WhereExtractUtil.getSubqueryWhereSegments((SelectStatement) sqlStatementContext.getSqlStatement()));
            result.addAll(WhereExtractUtil.getJoinWhereSegments((SelectStatement) sqlStatementContext.getSqlStatement()));
        }
        if (sqlStatementContext instanceof InsertStatementContext && null != ((InsertStatementContext) sqlStatementContext).getInsertSelectContext()) {
            result.addAll(getWhereSegments(((InsertStatementContext) sqlStatementContext).getInsertSelectContext().getSelectStatementContext()));
        }
        return result;
    }
    
    private Map<String, String> getColumnTableNames(final SQLStatementContext<?> sqlStatementContext, final Collection<AndPredicate> andPredicates) {
        Collection<ColumnSegment> columns = andPredicates.stream().flatMap(each -> each.getPredicates().stream())
                .flatMap(each -> ColumnExtractor.extract(each).stream()).filter(Objects::nonNull).collect(Collectors.toList());
        return sqlStatementContext.getTablesContext().findTableName(columns, schema);
    }
    
    private Optional<EncryptTable> findEncryptTable(final Map<String, String> columnTableNames, final ColumnSegment column) {
        return Optional.ofNullable(columnTableNames.get(column.getQualifiedName())).flatMap(tableName -> getEncryptRule().findEncryptTable(tableName));
    }
    
    private Collection<ColumnProjection> getColumnProjections(final String columnName) {
        return Collections.singletonList(new ColumnProjection(null, columnName, null));
    }
}
