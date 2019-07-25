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
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertRegularNamesToken;
import org.apache.shardingsphere.core.rule.BaseRule;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Insert regular column names token generator.
 *
 * @author panjuan
 */
public final class InsertRegularNamesTokenGenerator implements OptionalSQLTokenGenerator<BaseRule> {
    
    private BaseRule baseRule;
    
    private InsertOptimizedStatement insertOptimizedStatement;
    
    private String tableName;
    
    @Override
    public Optional<InsertRegularNamesToken> generateSQLToken(final OptimizedStatement optimizedStatement,
                                                              final ParameterBuilder parameterBuilder, final BaseRule baseRule, final boolean isQueryWithCipherColumn) {
        if (!isNeedToGenerateSQLToken(baseRule, optimizedStatement)) {
            return Optional.absent();
        }
        initParameters(optimizedStatement, baseRule);
        return createInsertColumnsToken();
    }
    
    private boolean isNeedToGenerateSQLToken(final BaseRule baseRule, final OptimizedStatement optimizedStatement) {
        Optional<InsertColumnsSegment> insertColumnsSegment = optimizedStatement.getSQLStatement().findSQLSegment(InsertColumnsSegment.class);
        return !(baseRule instanceof MasterSlaveRule) 
                && optimizedStatement instanceof InsertOptimizedStatement && insertColumnsSegment.isPresent() && insertColumnsSegment.get().getColumns().isEmpty();
    }
    
    private void initParameters(final OptimizedStatement optimizedStatement, final BaseRule baseRule) {
        this.baseRule = baseRule;
        this.insertOptimizedStatement = (InsertOptimizedStatement) optimizedStatement;
        tableName = insertOptimizedStatement.getTables().getSingleTableName();
    }
    
    private Optional<InsertRegularNamesToken> createInsertColumnsToken() {
        InsertColumnsSegment segment = insertOptimizedStatement.getSQLStatement().findSQLSegment(InsertColumnsSegment.class).get();
        InsertRegularNamesToken result = new InsertRegularNamesToken(segment.getStopIndex(), getActualInsertColumns(), !isNeedToAppendColumns());
        return Optional.of(result);
    }
    
    private Collection<String> getActualInsertColumns() {
        Collection<String> result = new LinkedList<>();
        Map<String, String> logicAndCipherColumns = getEncryptRule().getLogicAndCipherColumns(tableName);
        for (String each : insertOptimizedStatement.getInsertColumns().getRegularColumnNames()) {
            result.add(getCipherColumn(each, logicAndCipherColumns));
        }
        return result;
    }
    
    private EncryptRule getEncryptRule() {
        return baseRule instanceof ShardingRule ? ((ShardingRule) baseRule).getEncryptRule() : (EncryptRule) baseRule;
    }
    
    private String getCipherColumn(final String column, final Map<String, String> logicAndCipherColumns) {
        return logicAndCipherColumns.keySet().contains(column) ? logicAndCipherColumns.get(column) : column;
    }
    
    private boolean isNeedToAppendColumns() {
        return baseRule instanceof ShardingRule ? isNeedToAppendColumns((ShardingRule) baseRule) : isNeedToAppendAssistedQueryAndPlainColumns((EncryptRule) baseRule);
    }
    
    private boolean isNeedToAppendColumns(final ShardingRule shardingRule) {
        return isNeedToAppendGeneratedKey(shardingRule) || isNeedToAppendAssistedQueryAndPlainColumns(shardingRule.getEncryptRule());
    }
    
    private boolean isNeedToAppendGeneratedKey(final ShardingRule shardingRule) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(tableName);
        return generateKeyColumnName.isPresent() && !insertOptimizedStatement.getInsertColumns().getRegularColumnNames().contains(generateKeyColumnName.get());
    }
    
    private boolean isNeedToAppendAssistedQueryAndPlainColumns(final EncryptRule encryptRule) {
        return encryptRule.getAssistedQueryAndPlainColumnCount(tableName) > 0;
    }
}
