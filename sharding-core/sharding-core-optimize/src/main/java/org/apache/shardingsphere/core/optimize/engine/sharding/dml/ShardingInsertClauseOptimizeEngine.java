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

package org.apache.shardingsphere.core.optimize.engine.sharding.dml;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.keygen.GeneratedKey;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResultUnit;
import org.apache.shardingsphere.core.parse.sql.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.sql.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Insert clause optimize engine for sharding.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
public final class ShardingInsertClauseOptimizeEngine implements OptimizeEngine {
    
    private final ShardingRule shardingRule;
    
    private final InsertStatement insertStatement;
    
    private final List<Object> parameters;
    
    private final InsertClauseShardingConditionEngine shardingConditionEngine;
    
    public ShardingInsertClauseOptimizeEngine(final ShardingRule shardingRule, final InsertStatement insertStatement, final List<Object> parameters) {
        this.shardingRule = shardingRule;
        this.insertStatement = insertStatement;
        this.parameters = parameters;
        shardingConditionEngine = new InsertClauseShardingConditionEngine(shardingRule);
    }
    
    @Override
    public OptimizeResult optimize() {
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(insertStatement.getColumnNames());
        List<AndCondition> andConditions = insertStatement.getShardingConditions().getOrConditions();
        Optional<GeneratedKey> generatedKey = GeneratedKey.getGenerateKey(shardingRule, parameters, insertStatement);
        boolean isGeneratedValue = generatedKey.isPresent() && generatedKey.get().isGenerated();
        Iterator<Comparable<?>> generatedValues = isGeneratedValue ? generatedKey.get().getGeneratedValues().iterator() : null;
        if (generatedKey.isPresent()) {
            appendGeneratedKeyColumn(generatedKey.get(), insertOptimizeResult);
        }
        appendAssistedQueryColumns(insertOptimizeResult);
        int derivedColumnsCount = getDerivedColumnsCount(isGeneratedValue);
        int parametersCount = 0;
        for (int i = 0; i < andConditions.size(); i++) {
            InsertValue insertValue = insertStatement.getValues().get(i);
            InsertOptimizeResultUnit unit = insertOptimizeResult.addUnit(
                    insertValue.getValues(derivedColumnsCount), insertValue.getParameters(parameters, parametersCount, derivedColumnsCount), insertValue.getParametersCount());
            if (isGeneratedValue) {
                unit.addInsertValue(generatedValues.next(), parameters);
            }
            if (shardingRule.getEncryptRule().getEncryptorEngine().isHasShardingQueryAssistedEncryptor(insertStatement.getTables().getSingleTableName())) {
                fillAssistedQueryUnit(insertOptimizeResult.getColumnNames(), unit);
            }
            parametersCount += insertValue.getParametersCount();
        }
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatement, parameters, generatedKey.orNull());
        OptimizeResult result = new OptimizeResult(new ShardingConditions(shardingConditions), insertOptimizeResult);
        result.setGeneratedKey(generatedKey.orNull());
        return result;
    }
    
    private void appendGeneratedKeyColumn(final GeneratedKey generatedKey, final InsertOptimizeResult insertOptimizeResult) {
        if (generatedKey.isGenerated()) {
            insertOptimizeResult.getColumnNames().add(generatedKey.getColumnName());
        }
    }
    
    private void appendAssistedQueryColumns(final InsertOptimizeResult insertOptimizeResult) {
        for (String each : shardingRule.getEncryptRule().getEncryptorEngine().getAssistedQueryColumns(insertStatement.getTables().getSingleTableName())) {
            insertOptimizeResult.getColumnNames().add(each);
        }
    }
    
    private int getDerivedColumnsCount(final boolean isGeneratedValue) {
        int assistedQueryColumnsCount = shardingRule.getEncryptRule().getEncryptorEngine().getAssistedQueryColumnCount(insertStatement.getTables().getSingleTableName());
        return isGeneratedValue ? assistedQueryColumnsCount + 1 : assistedQueryColumnsCount;
    }
    
    private void fillAssistedQueryUnit(final Collection<String> columnNames, final InsertOptimizeResultUnit unit) {
        for (String each : columnNames) {
            if (shardingRule.getEncryptRule().getEncryptorEngine().getAssistedQueryColumn(insertStatement.getTables().getSingleTableName(), each).isPresent()) {
                unit.addInsertValue((Comparable<?>) unit.getColumnValue(each), parameters);
            }
        }
    }
}
