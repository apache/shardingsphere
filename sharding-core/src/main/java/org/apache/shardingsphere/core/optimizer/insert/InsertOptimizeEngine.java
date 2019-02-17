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

package org.apache.shardingsphere.core.optimizer.insert;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.keygen.GeneratedKey;
import org.apache.shardingsphere.core.optimizer.OptimizeEngine;
import org.apache.shardingsphere.core.optimizer.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimizer.condition.ShardingConditions;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.GeneratedKeyCondition;
import org.apache.shardingsphere.core.parsing.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.parsing.parser.token.InsertValuesToken;
import org.apache.shardingsphere.core.parsing.parser.token.InsertValuesToken.InsertColumnValue;
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
    public ShardingConditions optimize() {
        List<AndCondition> andConditions = insertStatement.getRouteConditions().getOrCondition().getAndConditions();
        List<InsertValue> insertValues = insertStatement.getInsertValues().getInsertValues();
        InsertValuesToken insertValuesToken = insertStatement.getInsertValuesToken();
        insertValuesToken.getColumnNames().addAll(insertStatement.getInsertColumnNames());
        Iterator<Comparable<?>> generatedKeys = createGeneratedKeys();
        List<ShardingCondition> result = new ArrayList<>(andConditions.size());
        int parametersCount = 0;
        for (int i = 0; i < andConditions.size(); i++) {
            InsertValue insertValue = insertValues.get(i);
            List<Object> currentParameters = new ArrayList<>(insertValue.getParametersCount() + 1);
            if (0 != insertValue.getParametersCount()) {
                currentParameters = getCurrentParameters(parametersCount, insertValue.getParametersCount());
                parametersCount = parametersCount + insertValue.getParametersCount();
            }
            insertValuesToken.addInsertColumnValue(insertValue.getColumnValues(), currentParameters);
            if (isNeededToAppendGeneratedKey()) {
                insertValuesToken.getColumnNames().add(shardingRule.findGenerateKeyColumn(insertStatement.getTables().getSingleTableName()).get().getName());
                fillInsertValuesTokenWithColumnValue(insertValuesToken.getColumnValues().get(i), generatedKeys.next());
            }
            if (isNeededToEncrypt()) {
            }
            
            
            
            
            
            InsertShardingCondition insertShardingCondition = isNeededToAppendGeneratedKey() ? getInsertShardingCondition(generatedKeys.next(), insertValue, currentParameters) 
                    : new InsertShardingCondition(insertValue.getExpression(), currentParameters);
            insertShardingCondition.getShardingValues().addAll(getShardingValues(andConditions.get(i)));
            result.add(insertShardingCondition);
        }
        return new ShardingConditions(result);
    }
    
    private boolean isNeededToAppendGeneratedKey() {
        return -1 == insertStatement.getGenerateKeyColumnIndex() && shardingRule.findGenerateKeyColumn(insertStatement.getTables().getSingleTableName()).isPresent();
    }
    
    private Iterator<Comparable<?>> createGeneratedKeys() {
        return isNeededToAppendGeneratedKey() ? generatedKey.getGeneratedKeys().iterator() : null;
    }
    
    private List<Object> getCurrentParameters(final int beginCount, final int increment) {
        List<Object> result = new ArrayList<>(increment + 1);
        result.addAll(parameters.subList(beginCount, beginCount + increment));
        return result;
    }
    
    private void fillInsertValuesTokenWithColumnValue(final InsertColumnValue insertColumnValue, final Comparable<?> columnValue) {
        if (!parameters.isEmpty()) {
            insertColumnValue.getValues().add(new SQLPlaceholderExpression(parameters.size() - 1));
            insertColumnValue.getParameters().add(columnValue);
        } else if (columnValue.getClass() == String.class) {
            insertColumnValue.getValues().add(new SQLTextExpression(columnValue.toString()));
        } else {
            insertColumnValue.getValues().add(new SQLNumberExpression((Number) columnValue));
        }
    }
    
    private boolean isNeededToEncrypt() {
        return shardingRule.getShardingEncryptorEngine().isHasShardingEncryptorStrategy(insertStatement.getTables().getSingleTableName());
    }
    
    private void encryptColumnValues(final InsertValuesToken insertValuesToken, final int currentIndex) {
//        String logicTableName = insertStatement.getTables().getSingleTableName()
        for (int i = 0; i < insertValuesToken.getColumnNames().size(); i++) {
            Optional<ShardingEncryptor> shardingEncryptor = shardingRule.getShardingEncryptorEngine().getShardingEncryptor(insertStatement.getTables().getSingleTableName(), insertValuesToken.getColumnNames().get(i));
            if (shardingEncryptor.isPresent()) {
                reviseInsertValuesToken(insertValuesToken, currentIndex, i, shardingEncryptor.get());
            }
        }
    }
    
    private void reviseInsertValuesToken(final InsertValuesToken insertValuesToken, final int insertColumnValueIndex, final int columnIndex, final ShardingEncryptor shardingEncryptor) {
        InsertColumnValue insertColumnValue = insertValuesToken.getColumnValues().get(insertColumnValueIndex);
        insertColumnValue.setColumnValue(columnIndex, shardingEncryptor.encrypt(insertColumnValue.getColumnValue(columnIndex)).toString());
        if (shardingEncryptor instanceof ShardingQueryAssistedEncryptor) {
            insertValuesToken.getColumnNames().add(shardingRule.getTableRule(insertStatement.getTables().getSingleTableName()).getShardingEncryptorStrategy().getAssistedQueryColumn(insertValuesToken.getColumnNames().get(columnIndex)).get());
            fillInsertValuesTokenWithColumnValue(insertColumnValue, insertColumnValue.getColumnValue(columnIndex));
        }
    }
    
    private InsertShardingCondition getInsertShardingCondition(final Comparable<?> currentGeneratedKey, final InsertValue insertValue, final List<Object> currentParameters) {
        Column generateKeyColumn = shardingRule.findGenerateKeyColumn(insertStatement.getTables().getSingleTableName()).get();
        String expression = getExpression(insertValue, currentGeneratedKey, generateKeyColumn, currentParameters);
        InsertShardingCondition result = new InsertShardingCondition(expression, currentParameters);
        result.getShardingValues().add(getShardingValue(generateKeyColumn, currentGeneratedKey));
        insertStatement.setContainGenerateKey(true);
        return result;
    }
    
    private String getExpression(final InsertValue insertValue, final Comparable<?> currentGeneratedKey, final Column generateKeyColumn, final List<Object> currentParameters) {
        boolean isStringTypeOfGeneratedKey = currentGeneratedKey.getClass() == String.class;
        return parameters.isEmpty() ? getExpressionWithoutPlaceHolders(insertValue, currentGeneratedKey, generateKeyColumn, isStringTypeOfGeneratedKey) 
                : getExpressionWithPlaceHolders(insertValue, currentGeneratedKey, generateKeyColumn, currentParameters);
    }
    
    private String getExpressionWithPlaceHolders(final InsertValue insertValue, final Comparable<?> currentGeneratedKey, final Column generateKeyColumn, final List<Object> currentParameters) {
        return DefaultKeyword.VALUES.equals(insertValue.getType()) ? getExpressionWithValues(insertValue, currentGeneratedKey, currentParameters) 
                : getExpressionWithoutValues(insertValue, currentGeneratedKey, generateKeyColumn, currentParameters);
    }
    
    private String getExpressionWithoutPlaceHolders(final InsertValue insertValue, final Comparable<?> currentGeneratedKey, final Column generateKeyColumn, final boolean isStringTypeOfGeneratedKey) {
        return DefaultKeyword.VALUES.equals(insertValue.getType()) ? getExpressionWithValues(insertValue, currentGeneratedKey, isStringTypeOfGeneratedKey)
                : getExpressionWithoutValues(insertValue, currentGeneratedKey, generateKeyColumn, isStringTypeOfGeneratedKey);
    }
    
    private String getExpressionWithoutValues(final InsertValue insertValue, final Comparable<?> currentGeneratedKey, final Column generateKeyColumn, final boolean isStringTypeOfGeneratedKey) {
        return isStringTypeOfGeneratedKey ? generateKeyColumn.getName() + " = " + '"' + currentGeneratedKey + '"' + ", " + insertValue.getExpression()
                : generateKeyColumn.getName() + " = " + currentGeneratedKey + ", " + insertValue.getExpression();
    }
    
    private String getExpressionWithoutValues(final InsertValue insertValue, final Comparable<?> currentGeneratedKey, final Column generateKeyColumn, final List<Object> currentParameters) {
        currentParameters.add(0, currentGeneratedKey);
        return generateKeyColumn.getName() + " = ?, " + insertValue.getExpression();
    }
    
    private String getExpressionWithValues(final InsertValue insertValue, final Comparable<?> currentGeneratedKey, final boolean isStringTypeOfGeneratedKey) {
        return isStringTypeOfGeneratedKey ? insertValue.getExpression().substring(0, insertValue.getExpression().lastIndexOf(")")) + ", " + '"' + currentGeneratedKey + '"' + ")"
                : insertValue.getExpression().substring(0, insertValue.getExpression().lastIndexOf(")")) + ", " + currentGeneratedKey + ")";
    }
    
    private String getExpressionWithValues(final InsertValue insertValue, final Comparable<?> currentGeneratedKey, final List<Object> currentParameters) {
        currentParameters.add(currentGeneratedKey);
        return insertValue.getExpression().substring(0, insertValue.getExpression().lastIndexOf(")")) + ", ?)";
    }
    
    private ListRouteValue getShardingValue(final Column column, final Comparable<?> value) {
        return new ListRouteValue<>(column, new GeneratedKeyCondition(column, -1, value).getConditionValues(parameters));
    }
    
    private Collection<ListRouteValue> getShardingValues(final AndCondition andCondition) {
        Collection<ListRouteValue> result = new LinkedList<>();
        for (Condition each : andCondition.getConditions()) {
            result.add(new ListRouteValue<>(each.getColumn(), each.getConditionValues(parameters)));
        }
        return result;
    }
}
