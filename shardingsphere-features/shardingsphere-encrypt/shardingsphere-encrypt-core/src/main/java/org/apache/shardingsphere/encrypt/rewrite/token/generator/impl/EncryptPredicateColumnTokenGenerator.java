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
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.util.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtil;

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
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof WhereAvailable && ((WhereAvailable) sqlStatementContext).getWhere().isPresent();
    }
    
    @Override
    public Collection<SubstitutableColumnNameToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Preconditions.checkState(((WhereAvailable) sqlStatementContext).getWhere().isPresent());
        Collection<SubstitutableColumnNameToken> result = new LinkedHashSet<>();
        ExpressionSegment expression = ((WhereAvailable) sqlStatementContext).getWhere().get().getExpr();
        Collection<AndPredicate> andPredicates = ExpressionExtractUtil.getAndPredicates(expression);
        Map<String, String> columnTableNames = getColumnTableNames(sqlStatementContext, andPredicates);
        for (AndPredicate each : andPredicates) {
            result.addAll(generateSQLTokens(each.getPredicates(), columnTableNames));
        }
        return result;
    }
    
    private Collection<SubstitutableColumnNameToken> generateSQLTokens(final Collection<ExpressionSegment> predicates, final Map<String, String> columnTableNames) {
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        for (ExpressionSegment each : predicates) {
            Optional<ColumnSegment> column = ColumnExtractor.extract(each);
            if (!column.isPresent()) {
                continue;
            }
            Optional<EncryptTable> encryptTable = findEncryptTable(columnTableNames, column.get());
            if (!encryptTable.isPresent() || !encryptTable.get().findEncryptorName(column.get().getIdentifier().getValue()).isPresent()) {
                continue;
            }
            int startIndex = column.get().getOwner().isPresent() ? column.get().getOwner().get().getStopIndex() + 2 : column.get().getStartIndex();
            int stopIndex = column.get().getStopIndex();
            if (!queryWithCipherColumn) {
                Optional<String> plainColumn = encryptTable.get().findPlainColumn(column.get().getIdentifier().getValue());
                if (plainColumn.isPresent()) {
                    result.add(new SubstitutableColumnNameToken(startIndex, stopIndex, getColumnProjections(plainColumn.get())));
                    continue;
                }
            }
            Optional<String> assistedQueryColumn = encryptTable.get().findAssistedQueryColumn(column.get().getIdentifier().getValue());
            SubstitutableColumnNameToken encryptColumnNameToken = assistedQueryColumn.map(columnName 
                -> new SubstitutableColumnNameToken(startIndex, stopIndex, getColumnProjections(columnName))).orElseGet(() 
                    -> new SubstitutableColumnNameToken(startIndex, stopIndex, getColumnProjections(encryptTable.get().getCipherColumn(column.get().getIdentifier().getValue()))));
            result.add(encryptColumnNameToken);
        }
        return result;
    }
    
    private Map<String, String> getColumnTableNames(final SQLStatementContext sqlStatementContext, final Collection<AndPredicate> andPredicates) {
        Collection<ColumnSegment> columns = andPredicates.stream().flatMap(each -> each.getPredicates().stream())
                .map(each -> ColumnExtractor.extract(each).orElse(null)).filter(Objects::nonNull).collect(Collectors.toList());
        return sqlStatementContext.getTablesContext().findTableName(columns, schema);
    }
    
    private Optional<EncryptTable> findEncryptTable(final Map<String, String> columnTableNames, final ColumnSegment column) {
        return Optional.ofNullable(columnTableNames.get(column.getQualifiedName())).flatMap(tableName -> getEncryptRule().findEncryptTable(tableName));
    }
    
    private Collection<ColumnProjection> getColumnProjections(final String columnName) {
        return Collections.singletonList(new ColumnProjection(null, columnName, null));
    }
}
