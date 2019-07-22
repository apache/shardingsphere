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
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertColumnsToken;
import org.apache.shardingsphere.core.rule.BaseRule;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Insert columns token generator.
 *
 * @author panjuan
 */
public final class InsertColumnsTokenGenerator implements OptionalSQLTokenGenerator<BaseRule> {
    
    @Override
    public Optional<InsertColumnsToken> generateSQLToken(
            final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final BaseRule rule, final boolean isQueryWithCipherColumn) {
        Optional<InsertColumnsSegment> insertColumnsSegment = optimizedStatement.getSQLStatement().findSQLSegment(InsertColumnsSegment.class);
        if (!(optimizedStatement instanceof InsertOptimizedStatement && insertColumnsSegment.isPresent())) {
            return Optional.absent();
        }
        return createInsertColumnsToken((InsertOptimizedStatement) optimizedStatement, rule, insertColumnsSegment.get());
    }
    
    private Optional<InsertColumnsToken> createInsertColumnsToken(final InsertOptimizedStatement optimizedStatement, final BaseRule rule, final InsertColumnsSegment segment) {
        if (!segment.getColumns().isEmpty()) {
            return Optional.absent();
        }
        InsertColumnsToken result = new InsertColumnsToken(
                segment.getStopIndex(), new LinkedList<>(optimizedStatement.getInsertColumns().getRegularColumnNames()), !isNeededToAppendColumns(optimizedStatement, rule));
        return Optional.of(result);
    }
    
    private boolean isNeededToAppendColumns(final InsertOptimizedStatement optimizedStatement, final BaseRule rule) {
        if (rule instanceof ShardingRule) {
            return isNeededToAppendColumns(optimizedStatement, (ShardingRule) rule);
        }
        return rule instanceof EncryptRule && isNeededToAppendColumns(optimizedStatement.getTables().getSingleTableName(), (EncryptRule) rule);
    }
    
    private boolean isNeededToAppendColumns(final InsertOptimizedStatement optimizedStatement, final ShardingRule shardingRule) {
        String tableName = optimizedStatement.getTables().getSingleTableName();
        return isNeededToAppendGeneratedKey(
                tableName, optimizedStatement.getInsertColumns().getRegularColumnNames(), shardingRule) || isNeededToAppendColumns(tableName, shardingRule.getEncryptRule());
    }
    
    private boolean isNeededToAppendColumns(final String tableName, final EncryptRule encryptRule) {
        return !encryptRule.getEncryptEngine().getAssistedQueryColumns(tableName).isEmpty();
    }
    
    private boolean isNeededToAppendGeneratedKey(final String tableName, final Collection<String> columnNames, final ShardingRule shardingRule) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(tableName);
        return generateKeyColumnName.isPresent() && !columnNames.contains(generateKeyColumnName.get());
    }
}
