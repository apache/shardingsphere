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
import org.apache.shardingsphere.core.optimize.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.statement.sharding.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.statement.sharding.insert.GeneratedKey;
import org.apache.shardingsphere.core.optimize.statement.sharding.insert.InsertClauseOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.insert.InsertOptimizeResultUnit;
import org.apache.shardingsphere.core.parse.exception.SQLParsingException;
import org.apache.shardingsphere.core.parse.sql.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
    public InsertClauseOptimizedStatement optimize() {
        Optional<OnDuplicateKeyColumnsSegment> onDuplicateKeyColumnsSegment = insertStatement.findSQLSegment(OnDuplicateKeyColumnsSegment.class);
        if (onDuplicateKeyColumnsSegment.isPresent() && isUpdateShardingKey(onDuplicateKeyColumnsSegment.get(), insertStatement.getTables().getSingleTableName())) {
            throw new SQLParsingException("INSERT INTO .... ON DUPLICATE KEY UPDATE can not support update for sharding column.");
        }
        Optional<GeneratedKey> generatedKey = GeneratedKey.getGenerateKey(shardingRule, parameters, insertStatement);
        boolean isGeneratedValue = generatedKey.isPresent() && generatedKey.get().isGenerated();
        Iterator<Comparable<?>> generatedValues = isGeneratedValue ? generatedKey.get().getGeneratedValues().iterator() : null;
        Collection<String> columnNames = getColumnNames(generatedKey.orNull());
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatement, parameters, generatedKey.orNull());
        InsertClauseOptimizedStatement result = new InsertClauseOptimizedStatement(insertStatement, shardingConditions, columnNames);
        result.setGeneratedKey(generatedKey.orNull());
        int derivedColumnsCount = getDerivedColumnsCount(isGeneratedValue);
        int parametersCount = 0;
        for (InsertValue each : insertStatement.getValues()) {
            InsertOptimizeResultUnit unit = result.addUnit(
                    each.getValues(derivedColumnsCount), each.getParameters(parameters, parametersCount, derivedColumnsCount), each.getParametersCount());
            if (isGeneratedValue) {
                unit.addInsertValue(generatedValues.next(), parameters);
            }
            if (shardingRule.getEncryptRule().getEncryptorEngine().isHasShardingQueryAssistedEncryptor(insertStatement.getTables().getSingleTableName())) {
                fillAssistedQueryUnit(columnNames, unit);
            }
            parametersCount += each.getParametersCount();
        }
        return result;
    }
    
    private boolean isUpdateShardingKey(final OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment, final String tableName) {
        for (ColumnSegment each : onDuplicateKeyColumnsSegment.getColumns()) {
            if (shardingRule.isShardingColumn(each.getName(), tableName)) {
                return true;
            }
        }
        return false;
    }
    
    private Collection<String> getColumnNames(final GeneratedKey generatedKey) {
        Collection<String> result = new LinkedHashSet<>(insertStatement.getColumnNames());
        if (null != generatedKey && generatedKey.isGenerated()) {
            result.add(generatedKey.getColumnName());
        }
        result.addAll(shardingRule.getEncryptRule().getEncryptorEngine().getAssistedQueryColumns(insertStatement.getTables().getSingleTableName()));
        return result;
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
