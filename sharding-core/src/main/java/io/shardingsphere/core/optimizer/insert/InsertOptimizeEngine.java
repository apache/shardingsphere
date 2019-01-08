/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.optimizer.insert;

import io.shardingsphere.api.algorithm.sharding.ListShardingValue;
import io.shardingsphere.core.optimizer.OptimizeEngine;
import io.shardingsphere.core.optimizer.condition.ShardingCondition;
import io.shardingsphere.core.optimizer.condition.ShardingConditions;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.parsing.parser.context.condition.Condition;
import io.shardingsphere.core.parsing.parser.context.condition.GeneratedKeyCondition;
import io.shardingsphere.core.parsing.parser.context.insertvalue.InsertValue;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.routing.router.sharding.GeneratedKey;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.RequiredArgsConstructor;

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
        int parametersCount = 0;
        if (!isNeededToAppendGeneratedKey()) {
            for (int i = 0; i < andConditions.size(); i++) {
                InsertValue insertValue = insertValues.get(i);
                List<Object> currentParameters = new ArrayList<>(insertValue.getParametersCount() + 1);
                if (0 != insertValue.getParametersCount()) {
                    currentParameters = getCurrentParameters(parametersCount, insertValue.getParametersCount());
                    parametersCount = parametersCount + insertValue.getParametersCount();
                }
                InsertShardingCondition insertShardingCondition = new InsertShardingCondition(insertValue.getExpression(), currentParameters);
                insertShardingCondition.getShardingValues().addAll(getShardingCondition(andConditions.get(i)));
                result.add(insertShardingCondition);
            }
        } else {
            Iterator<Comparable<?>> generatedKeys = generatedKey.getGeneratedKeys().iterator();
            for (int i = 0; i < andConditions.size(); i++) {
                InsertValue insertValue = insertValues.get(i);
                List<Object> currentParameters = new ArrayList<>(insertValue.getParametersCount() + 1);
                if (0 != insertValue.getParametersCount()) {
                    currentParameters = getCurrentParameters(parametersCount, insertValue.getParametersCount());
                    parametersCount = parametersCount + insertValue.getParametersCount();
                }
                InsertShardingCondition insertShardingCondition = getInsertShardingCondition(generatedKeys.next(), insertValue, currentParameters);
                insertShardingCondition.getShardingValues().addAll(getShardingCondition(andConditions.get(i)));
                result.add(insertShardingCondition);
            }
        }
        return new ShardingConditions(result);
    }
    
    private List<Object> getCurrentParameters(final int beginCount, final int increment) {
        List<Object> result = new ArrayList<>(increment + 1);
        result.addAll(parameters.subList(beginCount, beginCount + increment));
        return result;
    }
    
    private InsertShardingCondition getInsertShardingCondition(final Comparable<?> currentGeneratedKey, final InsertValue insertValue, final List<Object> currentParameters) {
        Column generateKeyColumn = shardingRule.getGenerateKeyColumn(insertStatement.getTables().getSingleTableName()).get();
        String expression = getExpression(insertValue, currentGeneratedKey, generateKeyColumn, currentParameters);
        InsertShardingCondition result = new InsertShardingCondition(expression, currentParameters);
        result.getShardingValues().add(getShardingCondition(generateKeyColumn, currentGeneratedKey));
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
    
    private String getExpressionWithoutValues(final InsertValue insertValue, final Comparable<?> currentGeneratedKey, final Column generateKeyColumn, final List<Object> currentParameters) {
        currentParameters.add(0, currentGeneratedKey);
        return generateKeyColumn.getName() + " = ?, " + insertValue.getExpression();
    }
    
    private String getExpressionWithValues(final InsertValue insertValue, final Comparable<?> currentGeneratedKey, final List<Object> currentParameters) {
        currentParameters.add(currentGeneratedKey);
        return insertValue.getExpression().substring(0, insertValue.getExpression().lastIndexOf(")")) + ", ?)";
    }
    
    private String getExpressionWithoutValues(final InsertValue insertValue, final Comparable<?> currentGeneratedKey, final Column generateKeyColumn, final boolean isStringTypeOfGeneratedKey) {
        return isStringTypeOfGeneratedKey ? generateKeyColumn.getName() + " = " + '"' + currentGeneratedKey + '"' + ", " + insertValue.getExpression() 
                : generateKeyColumn.getName() + " = " + currentGeneratedKey + ", " + insertValue.getExpression();
    }
    
    private String getExpressionWithValues(final InsertValue insertValue, final Comparable<?> currentGeneratedKey, final boolean isStringTypeOfGeneratedKey) {
        return isStringTypeOfGeneratedKey ? insertValue.getExpression().substring(0, insertValue.getExpression().lastIndexOf(")")) + ", " + '"' + currentGeneratedKey + '"' + ")" 
                : insertValue.getExpression().substring(0, insertValue.getExpression().lastIndexOf(")")) + ", " + currentGeneratedKey + ")";
    }

    private boolean isNeededToAppendGeneratedKey() {
        return -1 == insertStatement.getGenerateKeyColumnIndex() && shardingRule.getGenerateKeyColumn(insertStatement.getTables().getSingleTableName()).isPresent();
    }
    
    private ListShardingValue getShardingCondition(final Column column, final Comparable<?> value) {
        return new ListShardingValue<>(column.getTableName(), column.getName(),
                new GeneratedKeyCondition(column, -1, value).getConditionValues(parameters));
    }
    
    private Collection<ListShardingValue> getShardingCondition(final AndCondition andCondition) {
        Collection<ListShardingValue> result = new LinkedList<>();
        for (Condition each : andCondition.getConditions()) {
            result.add(new ListShardingValue<>(each.getColumn().getTableName(), each.getColumn().getName(), each.getConditionValues(parameters)));
        }
        return result;
    }
}
