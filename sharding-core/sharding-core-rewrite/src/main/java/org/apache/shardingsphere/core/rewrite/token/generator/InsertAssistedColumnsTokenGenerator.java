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
    
    @Override
    public Optional<InsertAssistedColumnsToken> generateSQLToken(
            final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final boolean isQueryWithCipherColumn) {
        Optional<InsertColumnsSegment> insertColumnsSegment = optimizedStatement.getSQLStatement().findSQLSegment(InsertColumnsSegment.class);
        if (!(optimizedStatement instanceof InsertOptimizedStatement && insertColumnsSegment.isPresent())) {
            return Optional.absent();
        }
        return createInsertAssistedColumnsToken((InsertOptimizedStatement) optimizedStatement, insertColumnsSegment.get(), encryptRule);
    }
    
    private Optional<InsertAssistedColumnsToken> createInsertAssistedColumnsToken(
            final InsertOptimizedStatement optimizedStatement, final InsertColumnsSegment segment, final EncryptRule encryptRule) {
        return encryptRule.getEncryptEngine().getAssistedQueryColumns(optimizedStatement.getTables().getSingleTableName()).isEmpty() ? Optional.<InsertAssistedColumnsToken>absent()
                : Optional.of(new InsertAssistedColumnsToken(segment.getStopIndex(), getQueryAssistedColumns(optimizedStatement, encryptRule), segment.getColumns().isEmpty()));
    }
    
    private Collection<String> getQueryAssistedColumns(final InsertOptimizedStatement optimizedStatement, final EncryptRule encryptRule) {
        Collection<String> result = new LinkedList<>();
        for (String each : optimizedStatement.getInsertColumns().getRegularColumnNames()) {
            Optional<String> assistedColumnName = encryptRule.getEncryptEngine().getAssistedQueryColumn(optimizedStatement.getTables().getSingleTableName(), each);
            if (assistedColumnName.isPresent()) {
                result.add(assistedColumnName.get());
            }
        }
        return result;
    }
}
