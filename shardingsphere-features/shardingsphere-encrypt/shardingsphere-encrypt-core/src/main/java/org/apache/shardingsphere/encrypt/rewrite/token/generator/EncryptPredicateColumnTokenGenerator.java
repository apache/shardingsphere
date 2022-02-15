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

import lombok.Setter;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.aware.EncryptRuleAware;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Predicate column token generator for encrypt.
 */
@Setter
public final class EncryptPredicateColumnTokenGenerator implements CollectionSQLTokenGenerator, SchemaMetaDataAware, EncryptRuleAware {
    
    private ShardingSphereSchema schema;
    
    private EncryptRule encryptRule;
    
    @SuppressWarnings("rawtypes")
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        boolean containsJoinQuery = sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isContainsJoinQuery();
        boolean containsSubquery = sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isContainsSubquery();
        return containsJoinQuery || containsSubquery || (sqlStatementContext instanceof WhereAvailable && !((WhereAvailable) sqlStatementContext).getWhereSegments().isEmpty());
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public Collection<SubstitutableColumnNameToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<SubstitutableColumnNameToken> result = new LinkedHashSet<>();
        Collection<WhereSegment> whereSegments = sqlStatementContext instanceof WhereAvailable 
                ? ((WhereAvailable) sqlStatementContext).getWhereSegments() : Collections.emptyList();
        for (WhereSegment each : whereSegments) {
            Collection<AndPredicate> andPredicates = ExpressionExtractUtil.getAndPredicates(each.getExpr());
            Map<String, String> columnTableNames = getColumnTableNames(sqlStatementContext, andPredicates);
            for (AndPredicate predicate : andPredicates) {
                result.addAll(generateSQLTokens(predicate.getPredicates(), columnTableNames));
            }
        }
        return result;
    }
    
    private Collection<SubstitutableColumnNameToken> generateSQLTokens(final Collection<ExpressionSegment> predicates, final Map<String, String> columnTableNames) {
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        for (ExpressionSegment each : predicates) {
            for (ColumnSegment column : ColumnExtractor.extract(each)) {
                Optional<String> tableName = findTableName(columnTableNames, buildColumnProjection(column));
                Optional<EncryptTable> encryptTable = tableName.flatMap(optional -> encryptRule.findEncryptTable(optional));
                if (!encryptTable.isPresent() || !encryptTable.get().findEncryptorName(column.getIdentifier().getValue()).isPresent()) {
                    continue;
                }
                int startIndex = column.getOwner().isPresent() ? column.getOwner().get().getStopIndex() + 2 : column.getStartIndex();
                int stopIndex = column.getStopIndex();
                boolean queryWithCipherColumn = encryptRule.isQueryWithCipherColumn(tableName.orElse(""));
                if (!queryWithCipherColumn) {
                    Optional<String> plainColumn = encryptTable.get().findPlainColumn(column.getIdentifier().getValue());
                    if (plainColumn.isPresent()) {
                        result.add(new SubstitutableColumnNameToken(startIndex, stopIndex, buildColumnProjections(plainColumn.get())));
                        continue;
                    }
                }
                Optional<String> assistedQueryColumn = encryptTable.get().findAssistedQueryColumn(column.getIdentifier().getValue());
                SubstitutableColumnNameToken encryptColumnNameToken = assistedQueryColumn.map(columnName
                    -> new SubstitutableColumnNameToken(startIndex, stopIndex, buildColumnProjections(columnName))).orElseGet(()
                        -> new SubstitutableColumnNameToken(startIndex, stopIndex, buildColumnProjections(encryptTable.get().getCipherColumn(column.getIdentifier().getValue()))));
                result.add(encryptColumnNameToken);
            }
        }
        return result;
    }
    
    private Map<String, String> getColumnTableNames(final SQLStatementContext<?> sqlStatementContext, final Collection<AndPredicate> andPredicates) {
        Collection<ColumnProjection> columns = new LinkedList<>();
        for (AndPredicate each : andPredicates) {
            columns.addAll(getColumnProjections(each));
        }
        return sqlStatementContext.getTablesContext().findTableName(columns, schema);
    }
    
    private Collection<ColumnProjection> getColumnProjections(final AndPredicate predicate) {
        Collection<ColumnProjection> result = new LinkedList<>();
        for (ExpressionSegment each : predicate.getPredicates()) {
            for (ColumnSegment columnSegment : ColumnExtractor.extract(each)) {
                result.add(buildColumnProjection(columnSegment));
            }
        }
        return result;
    }
    
    private ColumnProjection buildColumnProjection(final ColumnSegment segment) {
        String owner = segment.getOwner().map(optional -> optional.getIdentifier().getValue()).orElse(null);
        return new ColumnProjection(owner, segment.getIdentifier().getValue(), null);
    }
    
    private Collection<ColumnProjection> buildColumnProjections(final String columnName) {
        return Collections.singletonList(new ColumnProjection(null, columnName, null));
    }
    
    private Optional<String> findTableName(final Map<String, String> columnTableNames, final ColumnProjection column) {
        return Optional.ofNullable(columnTableNames.get(column.getExpression()));
    }
}
