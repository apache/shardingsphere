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
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.api.segment.InsertValue;
import org.apache.shardingsphere.core.optimize.sharding.engnie.ShardingOptimizeEngine;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.engine.InsertClauseShardingConditionEngine;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.GeneratedKey;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.ShardingInsertColumns;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingInsertOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
                                                     final TableMetas tableMetas, final String sql, final List<Object> parameters, final InsertStatement sqlStatement) {
        InsertClauseShardingConditionEngine shardingConditionEngine = new InsertClauseShardingConditionEngine(shardingRule);
        ShardingInsertColumns insertColumns = new ShardingInsertColumns(shardingRule, tableMetas, sqlStatement);
        Optional<GeneratedKey> generatedKey = GeneratedKey.getGenerateKey(shardingRule, parameters, sqlStatement, insertColumns);
        boolean isGeneratedValue = generatedKey.isPresent() && generatedKey.get().isGenerated();
        Iterator<Comparable<?>> generatedValues = isGeneratedValue ? generatedKey.get().getGeneratedValues().iterator() : null;
        String tableName = sqlStatement.getTable().getTableName();
        Collection<String> allColumnNames = new LinkedHashSet<>(insertColumns.getRegularColumnNames());
        if (generatedKey.isPresent()) {
            allColumnNames.add(generatedKey.get().getColumnName());
        }
        allColumnNames.addAll(shardingRule.getEncryptRule().getAssistedQueryAndPlainColumns(sqlStatement.getTable().getTableName()));
        int derivedColumnsCount = getDerivedColumnsCount(shardingRule, tableName, isGeneratedValue);
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(sqlStatement, parameters, allColumnNames, generatedKey.orNull());
        ShardingInsertOptimizedStatement result = new ShardingInsertOptimizedStatement(sqlStatement, shardingConditions, insertColumns, generatedKey.orNull());
        checkDuplicateKeyForShardingKey(shardingRule, sqlStatement, tableName);
        int parametersCount = 0;
        for (Collection<ExpressionSegment> each : sqlStatement.getAllValueExpressions()) {
            String generateKeyColumnName = shardingRule.findGenerateKeyColumnName(sqlStatement.getTable().getTableName()).orNull();
            Collection<String> encryptDerivedColumnNames = shardingRule.getEncryptRule().getAssistedQueryAndPlainColumns(sqlStatement.getTable().getTableName());
            InsertValue insertValue = result.createInsertValue(
                    generateKeyColumnName, encryptDerivedColumnNames, each, derivedColumnsCount, parameters, parametersCount);
            result.addInsertValue(insertValue);
            Object[] currentParameters = insertValue.getParameters();
            if (isGeneratedValue) {
                insertValue.appendValue(generatedValues.next(), Arrays.asList(currentParameters));
            }
            if (shardingRule.getEncryptRule().containsQueryAssistedColumn(tableName)) {
                fillAssistedQueryInsertValue(shardingRule, tableName, insertColumns.getRegularColumnNames(), insertValue, Arrays.asList(currentParameters));
            }
            if (shardingRule.getEncryptRule().containsPlainColumn(tableName)) {
                fillPlainInsertValue(shardingRule, tableName, insertColumns.getRegularColumnNames(), insertValue, Arrays.asList(currentParameters));
            }
            parametersCount += insertValue.getParametersCount();
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
        int assistedQueryAndPlainColumnsCount = shardingRule.getEncryptRule().getAssistedQueryAndPlainColumnCount(tableName);
        return isGeneratedValue ? assistedQueryAndPlainColumnsCount + 1 : assistedQueryAndPlainColumnsCount;
    }
    
    private void fillAssistedQueryInsertValue(final ShardingRule shardingRule,
                                              final String tableName, final Collection<String> columnNames, final InsertValue insertValue, final List<Object> parameters) {
        for (String each : columnNames) {
            if (shardingRule.getEncryptRule().getAssistedQueryColumn(tableName, each).isPresent()) {
                insertValue.appendValue((Comparable<?>) insertValue.getValue(each), parameters);
            }
        }
    }
    
    private void fillPlainInsertValue(final ShardingRule shardingRule,
                                      final String tableName, final Collection<String> columnNames, final InsertValue insertValue, final List<Object> parameters) {
        for (String each : columnNames) {
            if (shardingRule.getEncryptRule().getPlainColumn(tableName, each).isPresent()) {
                insertValue.appendValue((Comparable<?>) insertValue.getValue(each), parameters);
            }
        }
    }
}
