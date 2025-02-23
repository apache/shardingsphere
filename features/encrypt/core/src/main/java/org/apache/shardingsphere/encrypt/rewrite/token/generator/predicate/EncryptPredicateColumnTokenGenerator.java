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
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ColumnExtractor;
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
        return sqlStatementContext instanceof WhereAvailable;
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<SelectStatementContext> allSubqueryContexts = SQLStatementContextExtractor.getAllSubqueryContexts(sqlStatementContext);
        Collection<WhereSegment> whereSegments = SQLStatementContextExtractor.getWhereSegments((WhereAvailable) sqlStatementContext, allSubqueryContexts);
        Collection<AndPredicate> andPredicates = getAndPredicates(whereSegments);
        return generateSQLTokens(andPredicates, sqlStatementContext);
    }
    
    private Collection<SQLToken> generateSQLTokens(final Collection<AndPredicate> andPredicates, final SQLStatementContext sqlStatementContext) {
        Collection<SQLToken> result = new LinkedList<>();
        for (AndPredicate each : andPredicates) {
            for (ExpressionSegment expression : each.getPredicates()) {
                result.addAll(generateSQLTokens(sqlStatementContext, expression));
            }
        }
        return result;
    }
    
    private Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext, final ExpressionSegment expression) {
        Collection<SQLToken> result = new LinkedList<>();
        for (ColumnSegment each : ColumnExtractor.extract(expression)) {
            Optional<EncryptTable> encryptTable = rule.findEncryptTable(each.getColumnBoundInfo().getOriginalTable().getValue());
            if (encryptTable.isPresent() && encryptTable.get().isEncryptColumn(each.getColumnBoundInfo().getOriginalColumn().getValue())) {
                EncryptColumn encryptColumn = encryptTable.get().getEncryptColumn(each.getColumnBoundInfo().getOriginalColumn().getValue());
                result.addAll(buildSubstitutableColumnNameTokens(encryptColumn, each, expression, sqlStatementContext.getDatabaseType()));
            }
        }
        return result;
    }
    
    private Collection<AndPredicate> getAndPredicates(final Collection<WhereSegment> whereSegments) {
        Collection<AndPredicate> result = new LinkedList<>();
        for (WhereSegment each : whereSegments) {
            result.addAll(ExpressionExtractor.extractAndPredicates(each.getExpr()));
        }
        return result;
    }
    
    private Collection<SQLToken> buildSubstitutableColumnNameTokens(final EncryptColumn encryptColumn, final ColumnSegment columnSegment,
                                                                    final ExpressionSegment expression, final DatabaseType databaseType) {
        int startIndex = columnSegment.getOwner().isPresent() ? columnSegment.getOwner().get().getStopIndex() + 2 : columnSegment.getStartIndex();
        int stopIndex = columnSegment.getStopIndex();
        if (isIncludeLike(expression)) {
            Optional<LikeQueryColumnItem> likeQueryColumnItem = encryptColumn.getLikeQuery();
            Preconditions.checkState(likeQueryColumnItem.isPresent());
            Collection<Projection> columnProjections = createColumnProjections(likeQueryColumnItem.get().getName(), columnSegment.getIdentifier().getQuoteCharacter(), databaseType);
            return Collections.singleton(new SubstitutableColumnNameToken(startIndex, stopIndex, columnProjections, databaseType));
        }
        Collection<Projection> columnProjections = encryptColumn.getAssistedQuery()
                .map(optional -> createColumnProjections(optional.getName(), columnSegment.getIdentifier().getQuoteCharacter(), databaseType))
                .orElseGet(() -> createColumnProjections(encryptColumn.getCipher().getName(), columnSegment.getIdentifier().getQuoteCharacter(), databaseType));
        return Collections.singleton(new SubstitutableColumnNameToken(startIndex, stopIndex, columnProjections, databaseType));
    }
    
    private boolean isIncludeLike(final ExpressionSegment expression) {
        return expression instanceof BinaryOperationExpression && "LIKE".equalsIgnoreCase(((BinaryOperationExpression) expression).getOperator());
    }
    
    private Collection<Projection> createColumnProjections(final String columnName, final QuoteCharacter quoteCharacter, final DatabaseType databaseType) {
        ColumnProjection columnProjection = new ColumnProjection(null, new IdentifierValue(columnName, quoteCharacter), null, databaseType);
        return Collections.singleton(columnProjection);
    }
}
