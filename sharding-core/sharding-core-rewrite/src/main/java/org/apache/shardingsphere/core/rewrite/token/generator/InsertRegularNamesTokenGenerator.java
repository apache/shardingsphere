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
import org.apache.shardingsphere.core.optimize.encrypt.statement.EncryptInsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingInsertOptimizedStatement;
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
    
    @Override
    public Optional<InsertRegularNamesToken> generateSQLToken(final OptimizedStatement optimizedStatement,
                                                              final ParameterBuilder parameterBuilder, final BaseRule baseRule, final boolean isQueryWithCipherColumn) {
        if (!isNeedToGenerateSQLToken(optimizedStatement, baseRule)) {
            return Optional.absent();
        }
        return createInsertColumnsToken(optimizedStatement, baseRule);
    }
    
    private boolean isNeedToGenerateSQLToken(final OptimizedStatement optimizedStatement, final BaseRule baseRule) {
        Optional<InsertColumnsSegment> insertColumnsSegment = optimizedStatement.getSQLStatement().findSQLSegment(InsertColumnsSegment.class);
        return !(baseRule instanceof MasterSlaveRule) 
                && optimizedStatement instanceof InsertOptimizedStatement && insertColumnsSegment.isPresent() && insertColumnsSegment.get().getColumns().isEmpty();
    }
    
    private Optional<InsertRegularNamesToken> createInsertColumnsToken(final OptimizedStatement optimizedStatement, final BaseRule baseRule) {
        Optional<InsertColumnsSegment> insertColumnsSegment = optimizedStatement.getSQLStatement().findSQLSegment(InsertColumnsSegment.class);
        return insertColumnsSegment.isPresent()
                ? Optional.of(new InsertRegularNamesToken(insertColumnsSegment.get().getStopIndex(), 
                getActualInsertColumns((InsertOptimizedStatement) optimizedStatement, baseRule), !isNeedToAppendColumns((InsertOptimizedStatement) optimizedStatement, baseRule)))
                : Optional.<InsertRegularNamesToken>absent();
    }
    
    private Collection<String> getActualInsertColumns(final InsertOptimizedStatement optimizedStatement, final BaseRule baseRule) {
        Collection<String> result = new LinkedList<>();
        Map<String, String> logicAndCipherColumns = getEncryptRule(baseRule).getLogicAndCipherColumns(optimizedStatement.getTables().getSingleTableName());
        Collection<String> columnNames = optimizedStatement instanceof ShardingInsertOptimizedStatement
                ? ((ShardingInsertOptimizedStatement) optimizedStatement).getInsertColumns().getRegularColumnNames()
                : ((EncryptInsertOptimizedStatement) optimizedStatement).getColumnNames();
        for (String each : columnNames) {
            result.add(getCipherColumn(each, logicAndCipherColumns));
        }
        return result;
    }
    
    private EncryptRule getEncryptRule(final BaseRule baseRule) {
        return baseRule instanceof ShardingRule ? ((ShardingRule) baseRule).getEncryptRule() : (EncryptRule) baseRule;
    }
    
    private String getCipherColumn(final String column, final Map<String, String> logicAndCipherColumns) {
        return logicAndCipherColumns.keySet().contains(column) ? logicAndCipherColumns.get(column) : column;
    }
    
    private boolean isNeedToAppendColumns(final InsertOptimizedStatement optimizedStatement, final BaseRule baseRule) {
        return baseRule instanceof ShardingRule
                ? isNeedToAppendColumns(optimizedStatement, (ShardingRule) baseRule) : isNeedToAppendAssistedQueryAndPlainColumns(optimizedStatement, (EncryptRule) baseRule);
    }
    
    private boolean isNeedToAppendColumns(final InsertOptimizedStatement optimizedStatement, final ShardingRule shardingRule) {
        return isNeedToAppendGeneratedKey(optimizedStatement, shardingRule) || isNeedToAppendAssistedQueryAndPlainColumns(optimizedStatement, shardingRule.getEncryptRule());
    }
    
    private boolean isNeedToAppendGeneratedKey(final InsertOptimizedStatement optimizedStatement, final ShardingRule shardingRule) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(optimizedStatement.getTables().getSingleTableName());
        Collection<String> columnNames = optimizedStatement instanceof ShardingInsertOptimizedStatement
                ? ((ShardingInsertOptimizedStatement) optimizedStatement).getInsertColumns().getRegularColumnNames()
                : ((EncryptInsertOptimizedStatement) optimizedStatement).getColumnNames();
        return generateKeyColumnName.isPresent() && !columnNames.contains(generateKeyColumnName.get());
    }
    
    private boolean isNeedToAppendAssistedQueryAndPlainColumns(final OptimizedStatement optimizedStatement, final EncryptRule encryptRule) {
        return encryptRule.getAssistedQueryAndPlainColumnCount(optimizedStatement.getTables().getSingleTableName()) > 0;
    }
}
