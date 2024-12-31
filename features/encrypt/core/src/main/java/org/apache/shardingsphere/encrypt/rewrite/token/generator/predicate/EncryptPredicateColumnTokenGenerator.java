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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.predicate;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.extractor.SQLStatementContextExtractor;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.WhereAvailable;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Predicate column token generator for encrypt.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
@Setter
public final class EncryptPredicateColumnTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext> {
    
    private final EncryptRule rule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof WhereAvailable && !((WhereAvailable) sqlStatementContext).getWhereSegments().isEmpty();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<SelectStatementContext> allSubqueryContexts = SQLStatementContextExtractor.getAllSubqueryContexts(sqlStatementContext);
        Collection<WhereSegment> whereSegments = SQLStatementContextExtractor.getWhereSegments((WhereAvailable) sqlStatementContext, allSubqueryContexts);
        Collection<ColumnSegment> columnSegments = SQLStatementContextExtractor.getColumnSegments((WhereAvailable) sqlStatementContext, allSubqueryContexts);
        return generateSQLTokens(columnSegments, whereSegments, sqlStatementContext.getDatabaseType());
    }
    
    private Collection<SQLToken> generateSQLTokens(final Collection<ColumnSegment> columnSegments, final Collection<WhereSegment> whereSegments, final DatabaseType databaseType) {
        Collection<SQLToken> result = new LinkedList<>();
        for (ColumnSegment each : columnSegments) {
            Optional<EncryptTable> encryptTable = rule.findEncryptTable(each.getColumnBoundInfo().getOriginalTable().getValue());
            if (encryptTable.isPresent() && encryptTable.get().isEncryptColumn(each.getColumnBoundInfo().getOriginalColumn().getValue())) {
                EncryptColumn encryptColumn = encryptTable.get().getEncryptColumn(each.getColumnBoundInfo().getOriginalColumn().getValue());
                result.add(buildSubstitutableColumnNameToken(encryptColumn, each, whereSegments, databaseType));
            }
        }
        return result;
    }
    
    private SubstitutableColumnNameToken buildSubstitutableColumnNameToken(final EncryptColumn encryptColumn,
                                                                           final ColumnSegment columnSegment, final Collection<WhereSegment> whereSegments, final DatabaseType databaseType) {
        int startIndex = columnSegment.getOwner().isPresent() ? columnSegment.getOwner().get().getStopIndex() + 2 : columnSegment.getStartIndex();
        int stopIndex = columnSegment.getStopIndex();
        if (includesLike(whereSegments, columnSegment)) {
            Optional<LikeQueryColumnItem> likeQueryColumnItem = encryptColumn.getLikeQuery();
            Preconditions.checkState(likeQueryColumnItem.isPresent());
            return new SubstitutableColumnNameToken(
                    startIndex, stopIndex, createColumnProjections(likeQueryColumnItem.get().getName(), columnSegment.getIdentifier().getQuoteCharacter(), databaseType), databaseType);
        }
        Collection<Projection> columnProjections =
                encryptColumn.getAssistedQuery().map(optional -> createColumnProjections(optional.getName(), columnSegment.getIdentifier().getQuoteCharacter(), databaseType))
                        .orElseGet(() -> createColumnProjections(encryptColumn.getCipher().getName(), columnSegment.getIdentifier().getQuoteCharacter(), databaseType));
        return new SubstitutableColumnNameToken(startIndex, stopIndex, columnProjections, databaseType);
    }
    
    private boolean includesLike(final Collection<WhereSegment> whereSegments, final ColumnSegment targetColumnSegment) {
        for (WhereSegment each : whereSegments) {
            Collection<AndPredicate> andPredicates = ExpressionExtractor.extractAndPredicates(each.getExpr());
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
    
    private Collection<Projection> createColumnProjections(final String columnName, final QuoteCharacter quoteCharacter, final DatabaseType databaseType) {
        return Collections.singleton(new ColumnProjection(null, new IdentifierValue(columnName, quoteCharacter), null, databaseType));
    }
}
