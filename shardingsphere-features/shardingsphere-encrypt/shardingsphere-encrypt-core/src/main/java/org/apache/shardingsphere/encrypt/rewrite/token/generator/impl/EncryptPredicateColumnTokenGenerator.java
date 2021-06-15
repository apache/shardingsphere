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
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionBuilder;
import org.apache.shardingsphere.sql.parser.sql.common.util.ColumnExtractor;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;

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
        ExpressionBuilder expressionBuilder = new ExpressionBuilder(expression);
        Collection<AndPredicate> andPredicates = new LinkedList<>(expressionBuilder.extractAndPredicates().getAndPredicates());
        for (AndPredicate each : andPredicates) {
            result.addAll(generateSQLTokens(sqlStatementContext, each));
        }
        return result;
    }
    
    private Collection<SubstitutableColumnNameToken> generateSQLTokens(final SQLStatementContext sqlStatementContext, final AndPredicate andPredicate) {
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        for (ExpressionSegment each : andPredicate.getPredicates()) {
            Optional<ColumnSegment> column = ColumnExtractor.extract(each);
            if (!column.isPresent()) {
                continue;
            }
            Optional<EncryptTable> encryptTable = findEncryptTable(sqlStatementContext, column.get());
            if (!encryptTable.isPresent() || !encryptTable.get().findEncryptorName(column.get().getIdentifier().getValue()).isPresent()) {
                continue;
            }
            int startIndex = column.get().getOwner().isPresent() ? column.get().getOwner().get().getStopIndex() + 2 : column.get().getStartIndex();
            int stopIndex = column.get().getStopIndex();
            if (!queryWithCipherColumn) {
                Optional<String> plainColumn = encryptTable.get().findPlainColumn(column.get().getIdentifier().getValue());
                if (plainColumn.isPresent()) {
                    result.add(new SubstitutableColumnNameToken(startIndex, stopIndex, plainColumn.get()));
                    continue;
                }
            }
            Optional<String> assistedQueryColumn = encryptTable.get().findAssistedQueryColumn(column.get().getIdentifier().getValue());
            SubstitutableColumnNameToken encryptColumnNameToken = assistedQueryColumn.map(columnName -> new SubstitutableColumnNameToken(startIndex, stopIndex, columnName))
                    .orElseGet(() -> new SubstitutableColumnNameToken(startIndex, stopIndex, encryptTable.get().getCipherColumn(column.get().getIdentifier().getValue())));
            result.add(encryptColumnNameToken);
        }
        return result;
    }
    
    private Optional<EncryptTable> findEncryptTable(final SQLStatementContext sqlStatementContext, final ColumnSegment column) {
        return sqlStatementContext.getTablesContext().findTableName(column, schema).flatMap(tableName -> getEncryptRule().findEncryptTable(tableName));
    }
}
