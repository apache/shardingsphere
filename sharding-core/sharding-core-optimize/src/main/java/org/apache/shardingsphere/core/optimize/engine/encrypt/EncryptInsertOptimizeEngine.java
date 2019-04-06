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

package org.apache.shardingsphere.core.optimize.engine.encrypt;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.optimize.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.result.InsertColumnValues;
import org.apache.shardingsphere.core.optimize.result.InsertColumnValues.InsertColumnValue;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.LinkedList;
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
        List<InsertValue> insertValues = insertStatement.getInsertValues().getValues();
        InsertColumnValues insertColumnValues = createInsertColumnValues();
        int parametersCount = 0;
        for (int i = 0; i < insertValues.size(); i++) {
            InsertValue insertValue = insertValues.get(i);
            SQLExpression[] currentColumnValues = createCurrentColumnValues(insertValue);
            Object[] currentParameters = createCurrentParameters(parametersCount, insertValue);
            parametersCount = parametersCount + insertValue.getParametersCount();
            insertColumnValues.addInsertColumnValue(currentColumnValues, currentParameters);
            if (isNeededToAppendQueryAssistedColumn()) {
                fillWithQueryAssistedColumn(insertColumnValues, i);
            }
        }
        return new OptimizeResult(insertColumnValues);
    }
    
    private InsertColumnValues createInsertColumnValues() {
        return new InsertColumnValues(insertStatement.getInsertValuesToken().getType(), insertStatement.getInsertColumnNames());
    }
    
    private SQLExpression[] createCurrentColumnValues(final InsertValue insertValue) {
        SQLExpression[] result = new SQLExpression[insertValue.getColumnValues().size() + getIncrement()];
        insertValue.getColumnValues().toArray(result);
        return result;
    }
    
    private Object[] createCurrentParameters(final int beginIndex, final InsertValue insertValue) {
        if (0 == insertValue.getParametersCount()) {
            return new Object[0];
        }
        Object[] result = new Object[insertValue.getParametersCount() + getIncrement()];
        parameters.subList(beginIndex, beginIndex + insertValue.getParametersCount()).toArray(result);
        return result;
    }
    
    private int getIncrement() {
        int result = 0;
        if (isNeededToAppendQueryAssistedColumn()) {
            result += encryptRule.getEncryptorEngine().getAssistedQueryColumnCount(insertStatement.getTables().getSingleTableName()).get();
        }
        return result;
    }
    
    private boolean isNeededToAppendQueryAssistedColumn() {
        return encryptRule.getEncryptorEngine().isHasShardingQueryAssistedEncryptor(insertStatement.getTables().getSingleTableName());
    }
    
    private void fillWithQueryAssistedColumn(final InsertColumnValues insertColumnValues, final int insertColumnValueIndex) {
        Collection<String> assistedColumnNames = new LinkedList<>();
        for (String each : insertColumnValues.getColumnNames()) {
            InsertColumnValue insertColumnValue = insertColumnValues.getColumnValues().get(insertColumnValueIndex);
            Optional<String> assistedColumnName = encryptRule.getEncryptorEngine().getAssistedQueryColumn(insertStatement.getTables().getSingleTableName(), each);
            if (assistedColumnName.isPresent()) {
                assistedColumnNames.add(assistedColumnName.get());
                fillWithColumnValue(insertColumnValue, (Comparable<?>) insertColumnValue.getColumnValue(each));
            }
        }
        if (!assistedColumnNames.isEmpty()) {
            insertColumnValues.getColumnNames().addAll(assistedColumnNames);
        }
    }
    
    private void fillWithColumnValue(final InsertColumnValue insertColumnValue, final Comparable<?> columnValue) {
        if (!parameters.isEmpty()) {
            insertColumnValue.addColumnValue(new SQLPlaceholderExpression(parameters.size() - 1));
            insertColumnValue.addColumnParameter(columnValue);
        } else if (columnValue.getClass() == String.class) {
            insertColumnValue.addColumnValue(new SQLTextExpression(columnValue.toString()));
        } else {
            insertColumnValue.addColumnValue(new SQLNumberExpression((Number) columnValue));
        }
    }
}
