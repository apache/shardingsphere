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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import org.apache.shardingsphere.encrypt.rewrite.aware.QueryWithCipherColumnAware;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
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
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtil;
import org.apache.shardingsphere.sql.parser.sql.common.util.WhereExtractUtil;

import lombok.Setter;

/**
 * Predicate column token generator for encrypt.
 */
@Setter
public final class EncryptPredicateColumnTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator, SchemaMetaDataAware, QueryWithCipherColumnAware {
    
    private ShardingSphereSchema schema;
    
    private boolean queryWithCipherColumn;
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return isGenerateSQLTokenForEncryptOnWhereAvailable(sqlStatementContext) || isGenerateSQLTokenForEncryptOnJoinSegments(sqlStatementContext);
    }
    
    private boolean isGenerateSQLTokenForEncryptOnWhereAvailable(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof WhereAvailable && ((WhereAvailable) sqlStatementContext).getWhere().isPresent();
    }
    
    private boolean isGenerateSQLTokenForEncryptOnJoinSegments(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && !WhereExtractUtil.getJoinWhereSegments((SelectStatement) sqlStatementContext.getSqlStatement()).isEmpty();
    }
    
    @Override
    public Collection<SubstitutableColumnNameToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<SubstitutableColumnNameToken> result = new LinkedHashSet<>();
        Collection<AndPredicate> andPredicates = new LinkedHashSet<>();
        if (isGenerateSQLTokenForEncryptOnWhereAvailable(sqlStatementContext)) {
            ExpressionSegment expression = ((WhereAvailable) sqlStatementContext).getWhere().get().getExpr();
            andPredicates.addAll(ExpressionExtractUtil.getAndPredicates(expression));
        }
        Optional<Collection<WhereSegment>> whereSegments = Optional.empty();
        if (sqlStatementContext instanceof SelectStatementContext) {
            whereSegments = Optional.of(WhereExtractUtil.getJoinWhereSegments((SelectStatement) sqlStatementContext.getSqlStatement()));
            whereSegments.get().forEach(each -> andPredicates.addAll(ExpressionExtractUtil.getAndPredicates(each.getExpr())));
        }
        Map<String, String> columnTableNames = getColumnTableNames(sqlStatementContext, andPredicates, whereSegments);
        andPredicates.forEach(each -> result.addAll(generateSQLTokens(each.getPredicates(), columnTableNames)));
        return result;
    }
    
    private Collection<SubstitutableColumnNameToken> generateSQLTokens(final Collection<ExpressionSegment> predicates, final Map<String, String> columnTableNames) {
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        predicates.forEach(each -> result.addAll(generateSQLTokensOnColumnSegments(ColumnExtractor.extractAll(each), columnTableNames)));
        return result;
    }
    
    private Collection<SubstitutableColumnNameToken> generateSQLTokensOnColumnSegments(final Collection<ColumnSegment> columnSegments, final Map<String, String> columnTableNames) {
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        for (ColumnSegment each : columnSegments) {
            Optional<EncryptTable> encryptTable = findEncryptTable(columnTableNames, each);
            if (!encryptTable.isPresent() || !encryptTable.get().findEncryptorName(each.getIdentifier().getValue()).isPresent()) {
                continue;
            }
            int startIndex = each.getOwner().isPresent() ? each.getOwner().get().getStopIndex() + 2 : each.getStartIndex();
            int stopIndex = each.getStopIndex();
            if (!queryWithCipherColumn) {
                Optional<String> plainColumn = encryptTable.get().findPlainColumn(each.getIdentifier().getValue());
                if (plainColumn.isPresent()) {
                    result.add(new SubstitutableColumnNameToken(startIndex, stopIndex, getColumnProjections(plainColumn.get())));
                    continue;
                }
            }
            Optional<String> assistedQueryColumn = encryptTable.get().findAssistedQueryColumn(each.getIdentifier().getValue());
            SubstitutableColumnNameToken encryptColumnNameToken = assistedQueryColumn.map(columnName 
                -> new SubstitutableColumnNameToken(startIndex, stopIndex, getColumnProjections(columnName))).orElseGet(() 
                    -> new SubstitutableColumnNameToken(startIndex, stopIndex, getColumnProjections(encryptTable.get().getCipherColumn(each.getIdentifier().getValue()))));
            result.add(encryptColumnNameToken);
        }
        return result;
    }
    
    private Map<String, String> getColumnTableNames(final SQLStatementContext sqlStatementContext, final Collection<AndPredicate> andPredicates, 
            final Optional<Collection<WhereSegment>> whereSegments) {
        Collection<ColumnSegment> columns = new ArrayList<ColumnSegment>();
        andPredicates.forEach(each -> columns.addAll(generateColumnSegments(each.getPredicates())));
        if (whereSegments.isPresent()) {
            whereSegments.get().forEach(each -> columns.addAll(ColumnExtractor.extractAll(each.getExpr())));
        }
        return sqlStatementContext.getTablesContext().findTableName(columns, schema);
    }
    
    private Collection<ColumnSegment> generateColumnSegments(final Collection<ExpressionSegment> expressionSegments) {
        Collection<ColumnSegment> result = new ArrayList<ColumnSegment>();
        expressionSegments.forEach(each -> result.addAll(ColumnExtractor.extractAll(each)));
        return result;
    }
    
    private Optional<EncryptTable> findEncryptTable(final Map<String, String> columnTableNames, final ColumnSegment column) {
        return Optional.ofNullable(columnTableNames.get(column.getQualifiedName())).flatMap(tableName -> getEncryptRule().findEncryptTable(tableName));
    }
    
    private Collection<ColumnProjection> getColumnProjections(final String columnName) {
        return Collections.singletonList(new ColumnProjection(null, columnName, null));
    }
}
