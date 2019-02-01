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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.api.algorithm.sharding.ListShardingValue;
import org.apache.shardingsphere.core.optimizer.OptimizeEngine;
import org.apache.shardingsphere.core.optimizer.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimizer.condition.ShardingConditions;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.GeneratedKeyCondition;
import org.apache.shardingsphere.core.parsing.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.routing.router.sharding.GeneratedKey;
import org.apache.shardingsphere.core.rule.ShardingRule;

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
        List<AndCondition> andConditions = insertStatement.getConditions().getOrCondition().getAndConditions();
        List<InsertValue> insertValues = insertStatement.getInsertValues().getInsertValues();
        List<ShardingCondition> result = new ArrayList<>(andConditions.size());
        Iterator<Comparable<?>> generatedKeys = createGeneratedKeys();
        int parametersCount = 0;
        for (int i = 0; i < andConditions.size(); i++) {
            InsertValue insertValue = insertValues.get(i);
            List<Object> currentParameters = new ArrayList<>(insertValue.getParametersCount() + 1);
            if (0 != insertValue.getParametersCount()) {
                currentParameters = getCurrentParameters(parametersCount, insertValue.getParametersCount());
                parametersCount = parametersCount + insertValue.getParametersCount();
            }
            InsertShardingCondition insertShardingCondition = isNeededToAppendGeneratedKey() ? getInsertShardingCondition(generatedKeys.next(), insertValue, currentParameters) 
                    : new InsertShardingCondition(insertValue.getExpression(), currentParameters);
            insertShardingCondition.getShardingValues().addAll(getShardingValues(andConditions.get(i)));
            result.add(insertShardingCondition);
        }
        return new ShardingConditions(result);
    }
    
    private Iterator<Comparable<?>> createGeneratedKeys() {
        return isNeededToAppendGeneratedKey() ? generatedKey.getGeneratedKeys().iterator() : null;
    }
    
    private boolean isNeededToAppendGeneratedKey() {
        return -1 == insertStatement.getGenerateKeyColumnIndex() && shardingRule.findGenerateKeyColumn(insertStatement.getTables().getSingleTableName()).isPresent();
    }
    
    private List<Object> getCurrentParameters(final int beginCount, final int increment) {
        List<Object> result = new ArrayList<>(increment + 1);
        result.addAll(parameters.subList(beginCount, beginCount + increment));
        return result;
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
    
    private ListShardingValue getShardingValue(final Column column, final Comparable<?> value) {
        return new ListShardingValue<>(column.getTableName(), column.getName(),
                new GeneratedKeyCondition(column, -1, value).getConditionValues(parameters));
    }
    
    private Collection<ListShardingValue> getShardingValues(final AndCondition andCondition) {
        Collection<ListShardingValue> result = new LinkedList<>();
        for (Condition each : andCondition.getConditions()) {
            result.add(new ListShardingValue<>(each.getColumn().getTableName(), each.getColumn().getName(), each.getConditionValues(parameters)));
        }
        return result;
    }
}
