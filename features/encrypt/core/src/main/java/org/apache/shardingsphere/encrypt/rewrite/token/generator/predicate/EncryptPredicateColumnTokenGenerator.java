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

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.encrypt.enums.EncryptDerivedColumnSuffix;
import org.apache.shardingsphere.encrypt.exception.metadata.MissingMatchedEncryptQueryAlgorithmException;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.available.WhereContextAvailable;
import org.apache.shardingsphere.infra.binder.context.extractor.SQLStatementContextExtractor;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
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
        return sqlStatementContext instanceof WhereContextAvailable;
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<SelectStatementContext> allSubqueryContexts = SQLStatementContextExtractor.getAllSubqueryContexts(sqlStatementContext);
        Collection<WhereSegment> whereSegments = SQLStatementContextExtractor.getWhereSegments((WhereContextAvailable) sqlStatementContext, allSubqueryContexts);
        Collection<ExpressionSegment> expressions = getAllExpressions(whereSegments);
        return generateSQLTokens(expressions, sqlStatementContext.getSqlStatement());
    }
    
    private Collection<SQLToken> generateSQLTokens(final Collection<ExpressionSegment> expressions, final SQLStatement sqlStatement) {
        Collection<SQLToken> result = new LinkedList<>();
        for (ExpressionSegment each : expressions) {
            result.addAll(generateSQLTokens(sqlStatement, each));
        }
        return result;
    }
    
    private Collection<SQLToken> generateSQLTokens(final SQLStatement sqlStatement, final ExpressionSegment expression) {
        Collection<SQLToken> result = new LinkedList<>();
        for (ColumnSegment each : ColumnExtractor.extract(expression)) {
            Optional<EncryptTable> encryptTable = rule.findEncryptTable(each.getColumnBoundInfo().getOriginalTable().getValue());
            if (encryptTable.isPresent() && encryptTable.get().isEncryptColumn(each.getColumnBoundInfo().getOriginalColumn().getValue())) {
                EncryptColumn encryptColumn = encryptTable.get().getEncryptColumn(each.getColumnBoundInfo().getOriginalColumn().getValue());
                result.addAll(buildSubstitutableColumnNameTokens(encryptColumn, each, expression, sqlStatement.getDatabaseType(), encryptTable.get().getTable()));
            }
        }
        return result;
    }
    
    private Collection<ExpressionSegment> getAllExpressions(final Collection<WhereSegment> whereSegments) {
        if (1 == whereSegments.size()) {
            return ExpressionExtractor.extractAllExpressions(whereSegments.iterator().next().getExpr());
        }
        Collection<ExpressionSegment> result = new LinkedList<>();
        for (WhereSegment each : whereSegments) {
            Collection<ExpressionSegment> expressions = ExpressionExtractor.extractAllExpressions(each.getExpr());
            result.addAll(expressions);
        }
        return result;
    }
    
    private Collection<SQLToken> buildSubstitutableColumnNameTokens(final EncryptColumn encryptColumn, final ColumnSegment columnSegment,
                                                                    final ExpressionSegment expression, final DatabaseType databaseType, final String tableName) {
        int startIndex = columnSegment.getOwner().isPresent() ? columnSegment.getOwner().get().getStopIndex() + 2 : columnSegment.getStartIndex();
        int stopIndex = columnSegment.getStopIndex();
        if (isIncludeLike(expression)) {
            return generateEncryptLikeTokens(encryptColumn, columnSegment, databaseType, tableName, startIndex, stopIndex);
        }
        Collection<Projection> columnProjections = encryptColumn.getAssistedQuery()
                .map(optional -> createColumnProjections(optional.getName(), columnSegment, EncryptDerivedColumnSuffix.ASSISTED_QUERY, databaseType))
                .orElseGet(() -> createColumnProjections(encryptColumn.getCipher().getName(), columnSegment, EncryptDerivedColumnSuffix.CIPHER, databaseType));
        return Collections.singleton(new SubstitutableColumnNameToken(startIndex, stopIndex, columnProjections, databaseType));
    }
    
    private Collection<SQLToken> generateEncryptLikeTokens(final EncryptColumn encryptColumn, final ColumnSegment columnSegment, final DatabaseType databaseType,
                                                           final String tableName, final int startIndex, final int stopIndex) {
        ShardingSpherePreconditions.checkState(encryptColumn.getLikeQuery().isPresent() || encryptColumn.getCipher().getEncryptor().getMetaData().isSupportLike(),
                () -> new MissingMatchedEncryptQueryAlgorithmException(tableName, columnSegment.getIdentifier().getValue(), "LIKE"));
        return Collections.singleton(new SubstitutableColumnNameToken(startIndex, stopIndex, createColumnProjections(encryptColumn.getLikeQuery().map(LikeQueryColumnItem::getName)
                .orElseGet(() -> encryptColumn.getCipher().getName()), columnSegment, EncryptDerivedColumnSuffix.LIKE_QUERY, databaseType), databaseType));
    }
    
    private boolean isIncludeLike(final ExpressionSegment expression) {
        return expression instanceof BinaryOperationExpression && ("LIKE".equalsIgnoreCase(((BinaryOperationExpression) expression).getOperator())
                || "NOT LIKE".equalsIgnoreCase(((BinaryOperationExpression) expression).getOperator()));
    }
    
    private Collection<Projection> createColumnProjections(final String actualColumnName, final ColumnSegment columnSegment, final EncryptDerivedColumnSuffix derivedColumnSuffix,
                                                           final DatabaseType databaseType) {
        String columnName = TableSourceType.TEMPORARY_TABLE == columnSegment.getColumnBoundInfo().getTableSourceType()
                ? derivedColumnSuffix.getDerivedColumnName(columnSegment.getIdentifier().getValue(), databaseType)
                : actualColumnName;
        QuoteCharacter quoteCharacter = TableSourceType.TEMPORARY_TABLE == columnSegment.getColumnBoundInfo().getTableSourceType()
                ? columnSegment.getIdentifier().getQuoteCharacter()
                : new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getQuoteCharacter();
        ColumnProjection columnProjection = new ColumnProjection(null, new IdentifierValue(columnName, quoteCharacter), null, databaseType);
        return Collections.singleton(columnProjection);
    }
}
