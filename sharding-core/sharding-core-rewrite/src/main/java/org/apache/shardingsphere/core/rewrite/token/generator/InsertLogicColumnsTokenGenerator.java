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
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * Insert columns token generator.
 *
 * @author panjuan
 */
public final class InsertLogicColumnsTokenGenerator implements CollectionSQLTokenGenerator<EncryptRule> {
    
    private EncryptRule encryptRule;
    
    private InsertOptimizedStatement insertOptimizedStatement;
    
    private String tableName;
    
    @Override
    public Collection<InsertColumnsToken> generateSQLToken(final OptimizedStatement optimizedStatement, 
                                                         final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final boolean isQueryWithCipherColumn) {
        if (!isNeedToGenerateSQLToken(optimizedStatement)) {
            return Collections.emptyList();
        }
        initParameters(optimizedStatement, encryptRule);
        return createInsertColumnsToken();
    }
    
    private boolean isNeedToGenerateSQLToken(final OptimizedStatement optimizedStatement) {
        Optional<InsertColumnsSegment> insertColumnsSegment = optimizedStatement.getSQLStatement().findSQLSegment(InsertColumnsSegment.class);
        return optimizedStatement instanceof InsertOptimizedStatement && insertColumnsSegment.isPresent() && insertColumnsSegment.get().getColumns().isEmpty();
    }
    
    private void initParameters(final OptimizedStatement optimizedStatement, final EncryptRule encryptRule) {
        this.encryptRule = encryptRule;
        this.insertOptimizedStatement = (InsertOptimizedStatement) optimizedStatement;
        tableName = insertOptimizedStatement.getTables().getSingleTableName();
    }
    
    private Optional<InsertColumnsToken> createInsertColumnsToken() {
        InsertColumnsSegment segment = insertOptimizedStatement.getSQLStatement().findSQLSegment(InsertColumnsSegment.class).get();
        if (!segment.getColumns().isEmpty()) {
            return Optional.absent();
        }
        InsertColumnsToken result = new InsertColumnsToken(segment.getStopIndex(), getActualInsertColumns(), !isNeedToAppendColumns());
        return Optional.of(result);
    }
    
    private Collection<String> getActualInsertColumns() {
        Collection<String> result = new LinkedList<>();
        Map<String, String> logicAndCipherColumns = encryptRule.getEncryptEngine().getLogicAndCipherColumns(tableName);
        for (String each : insertOptimizedStatement.getInsertColumns().getRegularColumnNames()) {
            result.add(getCipherColumn(each, logicAndCipherColumns));
        }
        return result;
    }
    
    private String getCipherColumn(final String column, final Map<String, String> logicAndCipherColumns) {
        return logicAndCipherColumns.keySet().contains(column) ? logicAndCipherColumns.get(column) : column;
    }
    
    private boolean isNeedToAppendColumns() {
        return encryptRule instanceof ShardingRule ? isNeedToAppendColumns((ShardingRule) encryptRule) : isNeedToAppendAssistedQueryAndPlainColumns((EncryptRule) encryptRule);
    }
    
    private boolean isNeedToAppendColumns(final ShardingRule shardingRule) {
        return isNeedToAppendGeneratedKey(shardingRule) || isNeedToAppendAssistedQueryAndPlainColumns(shardingRule.getEncryptRule());
    }
    
    private boolean isNeedToAppendGeneratedKey(final ShardingRule shardingRule) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(tableName);
        return generateKeyColumnName.isPresent() && !insertOptimizedStatement.getInsertColumns().getRegularColumnNames().contains(generateKeyColumnName.get());
    }
    
    private boolean isNeedToAppendAssistedQueryAndPlainColumns(final EncryptRule encryptRule) {
        return encryptRule.getEncryptEngine().getAssistedQueryAndPlainColumnCount(tableName) > 0;
    }
}
