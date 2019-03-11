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

package org.apache.shardingsphere.core.optimizer.engine.sharding.insert;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.keygen.GeneratedKey;
import org.apache.shardingsphere.core.optimizer.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimizer.result.InsertColumnValues;
import org.apache.shardingsphere.core.optimizer.result.InsertColumnValues.InsertColumnValue;
import org.apache.shardingsphere.core.optimizer.result.OptimizeResult;
import org.apache.shardingsphere.core.optimizer.result.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimizer.result.condition.ShardingConditions;
import org.apache.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.GeneratedKeyCondition;
import org.apache.shardingsphere.core.parsing.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.routing.value.ListRouteValue;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.spi.algorithm.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.spi.algorithm.encrypt.ShardingQueryAssistedEncryptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert optimize engine.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class InsertOptimizeEngine implements OptimizeEngine {
    
    private final ShardingRule shardingRule;
    
    private final InsertStatement insertStatement;
    
    private final List<Object> parameters;
    
    private final GeneratedKey generatedKey;
    
    @Override
    public OptimizeResult optimize() {
        List<AndCondition> andConditions = insertStatement.getRouteConditions().getOrCondition().getAndConditions();
        Iterator<Comparable<?>> generatedKeys = createGeneratedKeys();
        List<ShardingCondition> shardingConditions = new ArrayList<>(andConditions.size());
        InsertColumnValues insertColumnValues = createInsertColumnValues();
        int parametersCount = 0;
        for (int i = 0; i < andConditions.size(); i++) {
            InsertValue insertValue = insertStatement.getInsertValues().getInsertValues().get(i);
            List<Object> currentParameters = new ArrayList<>(insertValue.getParametersCount() + 1);
            if (0 != insertValue.getParametersCount()) {
                currentParameters = getCurrentParameters(parametersCount, insertValue.getParametersCount());
                parametersCount = parametersCount + insertValue.getParametersCount();
            }
            ShardingCondition shardingCondition = createShardingCondition(andConditions.get(i));
            insertColumnValues.addInsertColumnValue(insertValue.getColumnValues(), currentParameters);
            if (isNeededToAppendGeneratedKey()) {
                Comparable<?> currentGeneratedKey = generatedKeys.next();
                fillInsertColumnValuestWithGeneratedKeyName(insertColumnValues);
                fillInsertColumnValueWithColumnValue(insertColumnValues.getColumnValues().get(i), currentGeneratedKey);
                fillShardingCondition(shardingCondition, currentGeneratedKey);
            }
            if (isNeededToEncrypt()) {
                encryptInsertColumnValues(insertColumnValues, i);
            }
            shardingConditions.add(shardingCondition);
        }
        return new OptimizeResult(new ShardingConditions(shardingConditions), insertColumnValues);
    }
    
    private InsertColumnValues createInsertColumnValues() {
        InsertColumnValues result = new InsertColumnValues(insertStatement.getInsertValuesToken().getType());
        result.getColumnNames().addAll(insertStatement.getInsertColumnNames());
        return result;
    }
    
    private Iterator<Comparable<?>> createGeneratedKeys() {
        return isNeededToAppendGeneratedKey() ? generatedKey.getGeneratedKeys().iterator() : null;
    }
    
    private List<Object> getCurrentParameters(final int beginCount, final int increment) {
        List<Object> result = new ArrayList<>(increment + 1);
        result.addAll(parameters.subList(beginCount, beginCount + increment));
        return result;
    }
    
    private ShardingCondition createShardingCondition(final AndCondition andCondition) {
        ShardingCondition result = new ShardingCondition();
        result.getShardingValues().addAll(getShardingValues(andCondition));
        return result;
    }
    
    private Collection<ListRouteValue> getShardingValues(final AndCondition andCondition) {
        Collection<ListRouteValue> result = new LinkedList<>();
        for (Condition each : andCondition.getConditions()) {
            result.add(new ListRouteValue<>(each.getColumn(), each.getConditionValues(parameters)));
        }
        return result;
    }
    
    private boolean isNeededToAppendGeneratedKey() {
        Optional<Column> generateKeyColumn = shardingRule.findGenerateKeyColumn(insertStatement.getTables().getSingleTableName());
        return generateKeyColumn.isPresent() && !insertStatement.getColumns().contains(generateKeyColumn.get());
    }
    
    private void fillInsertColumnValuestWithGeneratedKeyName(final InsertColumnValues insertColumnValues) {
        String generatedKeyName = shardingRule.findGenerateKeyColumn(insertStatement.getTables().getSingleTableName()).get().getName();
        insertColumnValues.getColumnNames().add(generatedKeyName);
    }
    
    private boolean isNeededToEncrypt() {
        return shardingRule.getShardingEncryptorEngine().isHasShardingEncryptorStrategy(insertStatement.getTables().getSingleTableName());
    }
    
    private void encryptInsertColumnValues(final InsertColumnValues insertColumnValues, final int insertColumnValueIndex) {
        for (int i = 0; i < insertColumnValues.getColumnNames().size(); i++) {
            Optional<ShardingEncryptor> shardingEncryptor = 
                    shardingRule.getShardingEncryptorEngine().getShardingEncryptor(insertStatement.getTables().getSingleTableName(), insertColumnValues.getColumnName(i));
            if (shardingEncryptor.isPresent()) {
                handleEncryptColumnsAndValues(insertColumnValues, insertColumnValueIndex, i, shardingEncryptor.get());
            }
        }
    }
    
    private void handleEncryptColumnsAndValues(final InsertColumnValues insertColumnValues, final int insertColumnValueIndex, final int columnIndex, final ShardingEncryptor shardingEncryptor) {
        InsertColumnValue insertColumnValue = insertColumnValues.getColumnValues().get(insertColumnValueIndex);
        if (shardingEncryptor instanceof ShardingQueryAssistedEncryptor) {
            String columnName = insertColumnValues.getColumnName(columnIndex);
            String assistedColumnName = shardingRule.getShardingEncryptorEngine().getAssistedQueryColumn(insertStatement.getTables().getSingleTableName(), columnName).get();
            insertColumnValues.getColumnNames().add(assistedColumnName);
            fillInsertColumnValueWithColumnValue(
                    insertColumnValue, ((ShardingQueryAssistedEncryptor) shardingEncryptor).queryAssistedEncrypt(insertColumnValue.getColumnValue(columnIndex).toString()));
        }
        insertColumnValue.setColumnValue(columnIndex, shardingEncryptor.encrypt(insertColumnValue.getColumnValue(columnIndex)));
    }
    
    private void fillInsertColumnValueWithColumnValue(final InsertColumnValue insertColumnValue, final Comparable<?> columnValue) {
        if (!parameters.isEmpty()) {
            insertColumnValue.getValues().add(new SQLPlaceholderExpression(parameters.size() - 1));
            insertColumnValue.getParameters().add(columnValue);
        } else if (columnValue.getClass() == String.class) {
            insertColumnValue.getValues().add(new SQLTextExpression(columnValue.toString()));
        } else {
            insertColumnValue.getValues().add(new SQLNumberExpression((Number) columnValue));
        }
    }
    
    private void fillShardingCondition(final ShardingCondition shardingCondition, final Comparable<?> currentGeneratedKey) {
        Column generateKeyColumn = shardingRule.findGenerateKeyColumn(insertStatement.getTables().getSingleTableName()).get();
        if (shardingRule.isShardingColumn(generateKeyColumn)) {
            shardingCondition.getShardingValues().add(new ListRouteValue<>(generateKeyColumn, new GeneratedKeyCondition(generateKeyColumn, -1, currentGeneratedKey).getConditionValues(parameters)));
        }
    }
}
