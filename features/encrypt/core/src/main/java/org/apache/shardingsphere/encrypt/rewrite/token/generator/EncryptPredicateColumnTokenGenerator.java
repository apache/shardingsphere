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
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.aware.EncryptRuleAware;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

/**
 * Predicate column token generator for encrypt.
 */
@Setter
public final class EncryptPredicateColumnTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext<?>>, SchemaMetaDataAware, EncryptRuleAware {
    
    private String databaseName;
    
    private Map<String, ShardingSphereSchema> schemas;
    
    private EncryptRule encryptRule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext<?> sqlStatementContext) {
        return sqlStatementContext instanceof WhereAvailable && !((WhereAvailable) sqlStatementContext).getWhereSegments().isEmpty();
    }
    
    @Override
    public Collection<SubstitutableColumnNameToken> generateSQLTokens(final SQLStatementContext<?> sqlStatementContext) {
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
    
    private Collection<SubstitutableColumnNameToken> generateSQLTokens(
            final Collection<ColumnSegment> columnSegments,
            final Map<String, String> columnExpressionTableNames,
            final Collection<WhereSegment> whereSegments) {
        Collection<SubstitutableColumnNameToken> result = new LinkedHashSet<>();
        for (ColumnSegment each : columnSegments) {
            String tableName = Optional.ofNullable(columnExpressionTableNames.get(each.getExpression())).orElse("");
            Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
            if (!encryptTable.isPresent() || !encryptTable.get().findEncryptColumn(each.getIdentifier().getValue()).isPresent()) {
                continue;
            }
            int startIndex = each.getOwner().isPresent() ? each.getOwner().get().getStopIndex() + 2 : each.getStartIndex();
            int stopIndex = each.getStopIndex();
            boolean queryWithCipherColumn = encryptRule.isQueryWithCipherColumn(tableName, each.getIdentifier().getValue());
            if (!queryWithCipherColumn) {
                Optional<String> plainColumn = encryptTable.get().findPlainColumn(each.getIdentifier().getValue());
                if (plainColumn.isPresent()) {
                    result.add(new SubstitutableColumnNameToken(startIndex, stopIndex, createColumnProjections(plainColumn.get())));
                    continue;
                }
            }
            BinaryOperationExpression boe = findBinaryOperationExpression(whereSegments, each);
            if (boe != null && boe.getOperator().equalsIgnoreCase("LIKE")) {
                Optional<String> fuzzyQueryColumn = encryptTable.get()
                        .findFuzzyQueryColumn(each.getIdentifier().getValue());
                if (!fuzzyQueryColumn.isPresent()) {
                    throw new UnsupportedEncryptSQLException("LIKE");
                } else {
                    result.add(new SubstitutableColumnNameToken(startIndex,
                            stopIndex, createColumnProjections(fuzzyQueryColumn.get())));
                    continue;
                }
            }
            
            Optional<String> assistedQueryColumn = encryptTable.get().findAssistedQueryColumn(each.getIdentifier().getValue());
            SubstitutableColumnNameToken encryptColumnNameToken = assistedQueryColumn.map(columnName -> new SubstitutableColumnNameToken(startIndex, stopIndex, createColumnProjections(columnName)))
                    .orElseGet(() -> new SubstitutableColumnNameToken(startIndex, stopIndex, createColumnProjections(encryptTable.get().getCipherColumn(each.getIdentifier().getValue()))));
            result.add(encryptColumnNameToken);
        }
        return result;
    }
    
    private BinaryOperationExpression findBinaryOperationExpression(
            final Collection<WhereSegment> whereSegments,
            final ColumnSegment columnSegment) {
        for (WhereSegment whereSegment : whereSegments) {
            Collection<AndPredicate> andPredicates =
                    ExpressionExtractUtil.getAndPredicates(whereSegment.getExpr());
            for (AndPredicate andPredicate : andPredicates) {
                for (ExpressionSegment predicate : andPredicate.getPredicates()) {
                    if (predicate instanceof BinaryOperationExpression) {
                        BinaryOperationExpression boe = (BinaryOperationExpression) predicate;
                        if (columnMatch(columnSegment, boe.getLeft())) {
                            return boe;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private boolean columnMatch(final ColumnSegment columnSegment, final ExpressionSegment left) {
        if (left instanceof ColumnSegment) {
            ColumnSegment leftCol = (ColumnSegment) left;
            if (leftCol.getStartIndex() == columnSegment.getStartIndex() && left.getStopIndex() == columnSegment.getStopIndex()) {
                return true;
            }
        }
        return false;
    }
    
    private Collection<ColumnProjection> createColumnProjections(final String columnName) {
        return Collections.singletonList(new ColumnProjection(null, columnName, null));
    }
}
