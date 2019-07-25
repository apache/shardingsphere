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

package org.apache.shardingsphere.core.rewrite.token.generator;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertQueryAndPlainNamesToken;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Insert assisted and plain names token generator.
 *
 * @author panjuan
 */
public final class InsertQueryAndPlainNamesTokenGenerator implements OptionalSQLTokenGenerator<EncryptRule> {
    
    private EncryptRule encryptRule;
    
    private InsertOptimizedStatement insertOptimizedStatement;
    
    private InsertColumnsSegment insertColumnsSegment;
    
    private String tableName;
    
    @Override
    public Optional<InsertQueryAndPlainNamesToken> generateSQLToken(
            final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final boolean isQueryWithCipherColumn) {
        if (!isNeedToGenerateSQLToken(optimizedStatement)) {
            return Optional.absent();
        }
        initParameters(encryptRule, optimizedStatement);
        return createInsertAssistedColumnsToken();
    }
    
    private boolean isNeedToGenerateSQLToken(final OptimizedStatement optimizedStatement) {
        Optional<InsertColumnsSegment> insertColumnsSegment = optimizedStatement.getSQLStatement().findSQLSegment(InsertColumnsSegment.class);
        return optimizedStatement instanceof InsertOptimizedStatement && insertColumnsSegment.isPresent();
    }
    
    private void initParameters(final EncryptRule encryptRule, final OptimizedStatement optimizedStatement) {
        this.encryptRule = encryptRule;
        insertOptimizedStatement = (InsertOptimizedStatement) optimizedStatement;
        insertColumnsSegment = optimizedStatement.getSQLStatement().findSQLSegment(InsertColumnsSegment.class).get();
        tableName = insertOptimizedStatement.getTables().getSingleTableName();
    }
    
    private Optional<InsertQueryAndPlainNamesToken> createInsertAssistedColumnsToken() {
        return 0 == encryptRule.getAssistedQueryAndPlainColumnCount(tableName) ? Optional.<InsertQueryAndPlainNamesToken>absent()
                : Optional.of(new InsertQueryAndPlainNamesToken(insertColumnsSegment.getStopIndex(), getAssistedQueryAndPlainColumns(), insertColumnsSegment.getColumns().isEmpty()));
    }
    
    private Collection<String> getAssistedQueryAndPlainColumns() {
        Collection<String> result = new LinkedList<>();
        result.addAll(getAssistedQueryColumns());
        result.addAll(getPlainColumns());
        return result;
    }
    
    private Collection<String> getAssistedQueryColumns() {
        Collection<String> result = new LinkedList<>();
        for (String each : insertOptimizedStatement.getInsertColumns().getRegularColumnNames()) {
            Optional<String> assistedQueryColumn = encryptRule.getAssistedQueryColumn(tableName, each);
            if (assistedQueryColumn.isPresent()) {
                result.add(assistedQueryColumn.get());
            }
        }
        return result;
    }
    
    private Collection<String> getPlainColumns() {
        Collection<String> result = new LinkedList<>();
        for (String each : insertOptimizedStatement.getInsertColumns().getRegularColumnNames()) {
            Optional<String> plainColumn = encryptRule.getPlainColumn(tableName, each);
            if (plainColumn.isPresent()) {
                result.add(plainColumn.get());
            }
        }
        return result;
    }
}
