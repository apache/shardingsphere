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

package org.apache.shardingsphere.core.optimizer.engine.encrypt;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.optimizer.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimizer.result.InsertColumnValues;
import org.apache.shardingsphere.core.optimizer.result.InsertColumnValues.InsertColumnValue;
import org.apache.shardingsphere.core.optimizer.result.OptimizeResult;
import org.apache.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parsing.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.spi.algorithm.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.spi.algorithm.encrypt.ShardingQueryAssistedEncryptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Encrypt insert optimize engine.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
public final class EncryptInsertOptimizeEngine implements OptimizeEngine {
    
    private final EncryptRule encryptRule;
    
    private final InsertStatement insertStatement;
    
    private final List<Object> parameters;
    
    @Override
    public OptimizeResult optimize() {
        List<AndCondition> andConditions = insertStatement.getRouteConditions().getOrCondition().getAndConditions();
        List<InsertValue> insertValues = insertStatement.getInsertValues().getInsertValues();
        InsertColumnValues insertColumnValues = createInsertColumnValues();
        int parametersCount = 0;
        for (int i = 0; i < andConditions.size(); i++) {
            InsertValue insertValue = insertValues.get(i);
            List<Object> currentParameters = new ArrayList<>(insertValue.getParametersCount() + 1);
            if (0 != insertValue.getParametersCount()) {
                currentParameters = getCurrentParameters(parametersCount, insertValue.getParametersCount());
                parametersCount = parametersCount + insertValue.getParametersCount();
            }
            insertColumnValues.addInsertColumnValue(insertValue.getColumnValues(), currentParameters);
            if (isNeededToEncrypt()) {
                encryptInsertColumnValues(insertColumnValues, i);
            }
        }
        return new OptimizeResult(insertColumnValues);
    }
    
    private InsertColumnValues createInsertColumnValues() {
        InsertColumnValues result = new InsertColumnValues(insertStatement.getColumnClauseStartIndex(), insertStatement.getInsertValuesToken().getType());
        result.getColumnNames().addAll(insertStatement.getInsertColumnNames());
        return result;
    }
    
    private List<Object> getCurrentParameters(final int beginCount, final int increment) {
        List<Object> result = new ArrayList<>(increment + 1);
        result.addAll(parameters.subList(beginCount, beginCount + increment));
        return result;
    }
        
    private boolean isNeededToEncrypt() {
        return encryptRule.getEncryptorEngine().isHasShardingEncryptorStrategy(insertStatement.getTables().getSingleTableName());
    }
    
    private void encryptInsertColumnValues(final InsertColumnValues insertColumnValues, final int insertColumnValueIndex) {
        for (int i = 0; i < insertColumnValues.getColumnNames().size(); i++) {
            Optional<ShardingEncryptor> shardingEncryptor = encryptRule.getEncryptorEngine().getShardingEncryptor(
                    insertStatement.getTables().getSingleTableName(), insertColumnValues.getColumnName(i));
            if (shardingEncryptor.isPresent()) {
                handleEncryptColumnsAndValues(insertColumnValues, insertColumnValueIndex, i, shardingEncryptor.get());
            }
        }
    }
    
    private void handleEncryptColumnsAndValues(final InsertColumnValues insertColumnValues, final int insertColumnValueIndex, final int columnIndex, final ShardingEncryptor shardingEncryptor) {
        InsertColumnValue insertColumnValue = insertColumnValues.getColumnValues().get(insertColumnValueIndex);
        if (shardingEncryptor instanceof ShardingQueryAssistedEncryptor) {
            String columnName = insertColumnValues.getColumnName(columnIndex);
            String assistedColumnName = encryptRule.getEncryptorEngine().getAssistedQueryColumn(insertStatement.getTables().getSingleTableName(), columnName).get();
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
}
