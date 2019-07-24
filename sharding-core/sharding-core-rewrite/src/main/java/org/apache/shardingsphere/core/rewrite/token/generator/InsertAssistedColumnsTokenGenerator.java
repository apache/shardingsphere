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
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertAssistedColumnsToken;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Insert assisted columns token generator.
 *
 * @author panjuan
 */
public final class InsertAssistedColumnsTokenGenerator implements OptionalSQLTokenGenerator<EncryptRule> {
    
    private EncryptRule encryptRule;
    
    private InsertOptimizedStatement insertOptimizedStatement;
    
    private InsertColumnsSegment insertColumnsSegment;
    
    private String tableName;
    
    @Override
    public Optional<InsertAssistedColumnsToken> generateSQLToken(
            final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final boolean isQueryWithCipherColumn) {
        if (isNotNeedToGenerateSQLToken(optimizedStatement)) {
            return Optional.absent();
        }
        initParameters(encryptRule, optimizedStatement);
        return createInsertAssistedColumnsToken();
    }
    
    private boolean isNotNeedToGenerateSQLToken(final OptimizedStatement optimizedStatement) {
        Optional<InsertColumnsSegment> insertColumnsSegment = optimizedStatement.getSQLStatement().findSQLSegment(InsertColumnsSegment.class);
        return !(optimizedStatement instanceof InsertOptimizedStatement && insertColumnsSegment.isPresent());
    }
    
    private void initParameters(final EncryptRule encryptRule, final OptimizedStatement optimizedStatement) {
        this.encryptRule = encryptRule;
        insertOptimizedStatement = (InsertOptimizedStatement) optimizedStatement;
        insertColumnsSegment = optimizedStatement.getSQLStatement().findSQLSegment(InsertColumnsSegment.class).get();
        tableName = insertOptimizedStatement.getTables().getSingleTableName();
    }
    
    private Optional<InsertAssistedColumnsToken> createInsertAssistedColumnsToken() {
        return encryptRule.getEncryptEngine().getAssistedQueryColumns(tableName).isEmpty() ? Optional.<InsertAssistedColumnsToken>absent()
                : Optional.of(new InsertAssistedColumnsToken(insertColumnsSegment.getStopIndex(), getQueryAssistedColumns(), insertColumnsSegment.getColumns().isEmpty()));
    }
    
    private Collection<String> getQueryAssistedColumns() {
        Collection<String> result = new LinkedList<>();
        for (String each : insertOptimizedStatement.getInsertColumns().getRegularColumnNames()) {
            Optional<String> assistedColumnName = encryptRule.getEncryptEngine().getAssistedQueryColumn(tableName, each);
            if (assistedColumnName.isPresent()) {
                result.add(assistedColumnName.get());
            }
        }
        return result;
    }
}
