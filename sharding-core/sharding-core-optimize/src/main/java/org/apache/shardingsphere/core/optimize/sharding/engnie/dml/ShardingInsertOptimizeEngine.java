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

package org.apache.shardingsphere.core.optimize.sharding.engnie.dml;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.api.segment.InsertValue;
import org.apache.shardingsphere.core.optimize.sharding.engnie.ShardingOptimizeEngine;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.engine.InsertClauseShardingConditionEngine;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.GeneratedKey;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.InsertOptimizeResultUnit;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.ShardingInsertColumns;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.value.InsertValueEngine;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingInsertOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Insert optimize engine for sharding.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
public final class ShardingInsertOptimizeEngine implements ShardingOptimizeEngine<InsertStatement> {
    
    @Override
    public ShardingInsertOptimizedStatement optimize(final ShardingRule shardingRule,
                                                     final ShardingTableMetaData shardingTableMetaData, final String sql, final List<Object> parameters, final InsertStatement sqlStatement) {
        InsertClauseShardingConditionEngine shardingConditionEngine = new InsertClauseShardingConditionEngine(shardingRule);
        ShardingInsertColumns insertColumns = new ShardingInsertColumns(shardingRule, shardingTableMetaData, sqlStatement);
        InsertValueEngine insertValueEngine = new InsertValueEngine();
        Collection<InsertValue> insertValues = insertValueEngine.createInsertValues(sqlStatement);
        Optional<GeneratedKey> generatedKey = GeneratedKey.getGenerateKey(shardingRule, parameters, sqlStatement, insertColumns, insertValues);
        boolean isGeneratedValue = generatedKey.isPresent() && generatedKey.get().isGenerated();
        Iterator<Comparable<?>> generatedValues = isGeneratedValue ? generatedKey.get().getGeneratedValues().iterator() : null;
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(
                sqlStatement, parameters, insertColumns.getAllColumnNames(), insertValues, generatedKey.orNull());
        ShardingInsertOptimizedStatement result = new ShardingInsertOptimizedStatement(
                sqlStatement, shardingConditions, insertColumns, insertValueEngine.createInsertValues(sqlStatement), generatedKey.orNull());
        String tableName = sqlStatement.getTable().getTableName();
        checkDuplicateKeyForShardingKey(shardingRule, sqlStatement, tableName);
        int derivedColumnsCount = getDerivedColumnsCount(shardingRule, tableName, isGeneratedValue);
        int parametersCount = 0;
        for (InsertValue each : result.getValues()) {
            InsertOptimizeResultUnit unit = result.createUnit(each.getValues(derivedColumnsCount), each.getParameters(parameters, parametersCount, derivedColumnsCount), each.getParametersCount());
            result.addUnit(unit);
            if (isGeneratedValue) {
                unit.addInsertValue(generatedValues.next(), parameters);
            }
            if (shardingRule.getEncryptRule().getEncryptEngine().isHasShardingQueryAssistedEncryptor(tableName)) {
                fillAssistedQueryUnit(shardingRule, tableName, insertColumns.getRegularColumnNames(), unit, parameters);
            }
            parametersCount += each.getParametersCount();
        }
        return result;
    }
    
    private void checkDuplicateKeyForShardingKey(final ShardingRule shardingRule, final InsertStatement sqlStatement, final String tableName) {
        Optional<OnDuplicateKeyColumnsSegment> onDuplicateKeyColumnsSegment = sqlStatement.findSQLSegment(OnDuplicateKeyColumnsSegment.class);
        if (onDuplicateKeyColumnsSegment.isPresent() && isUpdateShardingKey(shardingRule, onDuplicateKeyColumnsSegment.get(), tableName)) {
            throw new ShardingException("INSERT INTO .... ON DUPLICATE KEY UPDATE can not support update for sharding column.");
        }
    }
    
    private boolean isUpdateShardingKey(final ShardingRule shardingRule, final OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment, final String tableName) {
        for (ColumnSegment each : onDuplicateKeyColumnsSegment.getColumns()) {
            if (shardingRule.isShardingColumn(each.getName(), tableName)) {
                return true;
            }
        }
        return false;
    }
    
    private int getDerivedColumnsCount(final ShardingRule shardingRule, final String tableName, final boolean isGeneratedValue) {
        int assistedQueryColumnsCount = shardingRule.getEncryptRule().getEncryptEngine().getAssistedQueryColumnCount(tableName);
        return isGeneratedValue ? assistedQueryColumnsCount + 1 : assistedQueryColumnsCount;
    }
    
    private void fillAssistedQueryUnit(final ShardingRule shardingRule, 
                                       final String tableName, final Collection<String> columnNames, final InsertOptimizeResultUnit unit, final List<Object> parameters) {
        for (String each : columnNames) {
            if (shardingRule.getEncryptRule().getEncryptEngine().getAssistedQueryColumn(tableName, each).isPresent()) {
                unit.addInsertValue((Comparable<?>) unit.getColumnValue(each), parameters);
            }
        }
    }
}
