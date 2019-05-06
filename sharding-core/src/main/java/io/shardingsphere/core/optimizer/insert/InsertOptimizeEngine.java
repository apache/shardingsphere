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

import com.google.common.base.Optional;
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
        Iterator<Number> generatedKeys = null;
        int count = 0;
        int parametersCount = 0;
        for (AndCondition each : andConditions) {
            InsertValue insertValue = insertValues.get(count);
            List<Object> currentParameters = new ArrayList<>(insertValue.getParametersCount() + 1);
            if (insertValue.getParametersCount() > 0) {
                currentParameters.addAll(parameters.subList(parametersCount, parametersCount += insertValue.getParametersCount()));
            }
            String logicTableName = insertStatement.getTables().getSingleTableName();
            Optional<Column> generateKeyColumn = shardingRule.getGenerateKeyColumn(logicTableName);
            InsertShardingCondition insertShardingCondition;
            if (-1 != insertStatement.getGenerateKeyColumnIndex() || !generateKeyColumn.isPresent()) {
                insertShardingCondition = new InsertShardingCondition(insertValue.getExpression(), currentParameters);
            } else {
                if (null == generatedKeys) {
                    generatedKeys = generatedKey.getGeneratedKeys().iterator();
                }
                String expression;
                Number currentGeneratedKey = generatedKeys.next();
                if (parameters.isEmpty()) {
                    if (DefaultKeyword.VALUES.equals(insertValue.getType())) {
                        expression = insertValue.getExpression().substring(0, insertValue.getExpression().lastIndexOf(")")) + ", " + currentGeneratedKey.toString() + ")";
                    } else {
                        expression = generateKeyColumn.get().getName() + " = " + currentGeneratedKey + ", " + insertValue.getExpression();
                    }
                } else {
                    if (DefaultKeyword.VALUES.equals(insertValue.getType())) {
                        expression = insertValue.getExpression().substring(0, insertValue.getExpression().lastIndexOf(")")) + ", ?)";
                        currentParameters.add(currentGeneratedKey);
                    } else {
                        expression = generateKeyColumn.get().getName() + " = ?, " + insertValue.getExpression();
                        currentParameters.add(0, currentGeneratedKey);
                    }
                }
                insertShardingCondition = new InsertShardingCondition(expression, currentParameters);
                insertShardingCondition.getShardingValues().add(getShardingCondition(generateKeyColumn.get(), currentGeneratedKey));
            }
            insertShardingCondition.getShardingValues().addAll(getShardingCondition(each));
            result.add(insertShardingCondition);
            count++;
        }
        return new ShardingConditions(result);
    }
    
    private ListShardingValue getShardingCondition(final Column column, final Number value) {
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
