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
import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rewrite.aware.EncryptRuleAware;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sql.parser.sql.common.enums.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtils;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

/**
 * Predicate column token generator for encrypt.
 */
@Setter
public final class EncryptPredicateColumnTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext>, SchemaMetaDataAware, EncryptRuleAware {
    
    private String databaseName;
    
    private Map<String, ShardingSphereSchema> schemas;
    
    private EncryptRule encryptRule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof WhereAvailable && !((WhereAvailable) sqlStatementContext).getWhereSegments().isEmpty();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<ColumnSegment> columnSegments = Collections.emptyList();
        Collection<WhereSegment> whereSegments = Collections.emptyList();
        if (sqlStatementContext instanceof WhereAvailable) {
            columnSegments = ((WhereAvailable) sqlStatementContext).getColumnSegments();
            whereSegments = ((WhereAvailable) sqlStatementContext).getWhereSegments();
        }
        String defaultSchema = DatabaseTypeEngine.getDefaultSchemaName(sqlStatementContext.getDatabaseType(), databaseName);
        ShardingSphereSchema schema = sqlStatementContext.getTablesContext().getSchemaName().map(schemas::get).orElseGet(() -> schemas.get(defaultSchema));
        Map<String, String> columnExpressionTableNames = sqlStatementContext.getTablesContext().findTableNamesByColumnSegment(columnSegments, schema);
        return generateSQLTokens(columnSegments, columnExpressionTableNames, whereSegments);
    }
    
    private Collection<SQLToken> generateSQLTokens(final Collection<ColumnSegment> columnSegments,
                                                   final Map<String, String> columnExpressionTableNames, final Collection<WhereSegment> whereSegments) {
        Collection<SQLToken> result = new LinkedHashSet<>();
        for (ColumnSegment each : columnSegments) {
            String tableName = Optional.ofNullable(columnExpressionTableNames.get(each.getExpression())).orElse("");
            Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
            if (encryptTable.isPresent() && encryptTable.get().isEncryptColumn(each.getIdentifier().getValue())) {
                result.add(buildSubstitutableColumnNameToken(encryptTable.get().getEncryptColumn(each.getIdentifier().getValue()), each, whereSegments));
            }
        }
        return result;
    }
    
    private SubstitutableColumnNameToken buildSubstitutableColumnNameToken(final EncryptColumn encryptColumn, final ColumnSegment columnSegment, final Collection<WhereSegment> whereSegments) {
        int startIndex = columnSegment.getOwner().isPresent() ? columnSegment.getOwner().get().getStopIndex() + 2 : columnSegment.getStartIndex();
        int stopIndex = columnSegment.getStopIndex();
        if (includesLike(whereSegments, columnSegment)) {
            ShardingSpherePreconditions.checkState(encryptColumn.getLikeQuery().isPresent(), () -> new UnsupportedEncryptSQLException("LIKE"));
            return new SubstitutableColumnNameToken(startIndex, stopIndex, createColumnProjections(encryptColumn.getLikeQuery().get().getName(), columnSegment.getIdentifier().getQuoteCharacter()));
        }
        Collection<Projection> columnProjections =
                encryptColumn.getAssistedQuery().map(optional -> createColumnProjections(optional.getName(), columnSegment.getIdentifier().getQuoteCharacter()))
                        .orElseGet(() -> createColumnProjections(encryptColumn.getCipher().getName(), columnSegment.getIdentifier().getQuoteCharacter()));
        return new SubstitutableColumnNameToken(startIndex, stopIndex, columnProjections);
    }
    
    private boolean includesLike(final Collection<WhereSegment> whereSegments, final ColumnSegment targetColumnSegment) {
        for (WhereSegment each : whereSegments) {
            Collection<AndPredicate> andPredicates = ExpressionExtractUtils.getAndPredicates(each.getExpr());
            for (AndPredicate andPredicate : andPredicates) {
                if (isLikeColumnSegment(andPredicate, targetColumnSegment)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isLikeColumnSegment(final AndPredicate andPredicate, final ColumnSegment targetColumnSegment) {
        for (ExpressionSegment each : andPredicate.getPredicates()) {
            if (each instanceof BinaryOperationExpression
                    && "LIKE".equalsIgnoreCase(((BinaryOperationExpression) each).getOperator()) && isSameColumnSegment(((BinaryOperationExpression) each).getLeft(), targetColumnSegment)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isSameColumnSegment(final ExpressionSegment columnSegment, final ColumnSegment targetColumnSegment) {
        return columnSegment instanceof ColumnSegment && columnSegment.getStartIndex() == targetColumnSegment.getStartIndex() && columnSegment.getStopIndex() == targetColumnSegment.getStopIndex();
    }
    
    private Collection<Projection> createColumnProjections(final String columnName, final QuoteCharacter quoteCharacter) {
        return Collections.singletonList(new ColumnProjection(null, new IdentifierValue(columnName, quoteCharacter), null));
    }
}
