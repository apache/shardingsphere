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
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingInsertOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertGeneratedKeyNameToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Insert generated key name token generator.
 *
 * @author panjuan
 */
public final class InsertGeneratedKeyNameTokenGenerator implements OptionalSQLTokenGenerator<ShardingRule> {
    
    @Override
    public Optional<InsertGeneratedKeyNameToken> generateSQLToken(
            final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final ShardingRule shardingRule, final boolean isQueryWithCipherColumn) {
        Optional<InsertColumnsSegment> insertColumnsSegment = optimizedStatement.getSQLStatement().findSQLSegment(InsertColumnsSegment.class);
        if (!(optimizedStatement instanceof ShardingInsertOptimizedStatement && insertColumnsSegment.isPresent())) {
            return Optional.absent();
        }
        return createInsertGeneratedKeyToken((ShardingInsertOptimizedStatement) optimizedStatement, insertColumnsSegment.get(), shardingRule);
    }
    
    private Optional<InsertGeneratedKeyNameToken> createInsertGeneratedKeyToken(
            final ShardingInsertOptimizedStatement optimizedStatement, final InsertColumnsSegment segment, final ShardingRule shardingRule) {
        String tableName = optimizedStatement.getTables().getSingleTableName();
        Optional<String> generatedKeyColumnName = shardingRule.findGenerateKeyColumnName(tableName);
        return generatedKeyColumnName.isPresent() && !optimizedStatement.getInsertColumns().getRegularColumnNames().contains(generatedKeyColumnName.get())
                ? Optional.of(new InsertGeneratedKeyNameToken(segment.getStopIndex(), generatedKeyColumnName.get(), isToAddCloseParenthesis(tableName, segment, shardingRule)))
                : Optional.<InsertGeneratedKeyNameToken>absent();
    }
    
    private boolean isToAddCloseParenthesis(final String tableName, final InsertColumnsSegment segment, final ShardingRule shardingRule) {
        return segment.getColumns().isEmpty() && 0 == shardingRule.getEncryptRule().getAssistedQueryAndPlainColumnCount(tableName);
    }
    
}
