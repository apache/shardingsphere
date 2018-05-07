/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.optimizer.insert;

import com.google.common.base.Optional;
import io.shardingjdbc.core.api.algorithm.sharding.ListShardingValue;
import io.shardingjdbc.core.optimizer.OptimizeEngine;
import io.shardingjdbc.core.optimizer.condition.ShardingCondition;
import io.shardingjdbc.core.optimizer.condition.ShardingConditions;
import io.shardingjdbc.core.parsing.parser.context.condition.AndCondition;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.context.condition.Condition;
import io.shardingjdbc.core.parsing.parser.context.condition.GeneratedKeyCondition;
import io.shardingjdbc.core.parsing.parser.context.insertvalue.InsertValue;
import io.shardingjdbc.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingjdbc.core.routing.router.sharding.GeneratedKey;
import io.shardingjdbc.core.rule.ShardingRule;
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
        for (AndCondition each : andConditions) {
            InsertValue insertValue = insertValues.get(count);
            List<Object> currentParameters = new ArrayList<>(insertValue.getParametersCount() + 1);
            currentParameters.addAll(parameters.subList(count * insertValue.getParametersCount(), (count + 1) * insertValue.getParametersCount()));
            
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
                if (0 == parameters.size()) {
                    expression = insertValue.getExpression().substring(0, insertValue.getExpression().length() - 1) + ", " + currentGeneratedKey.toString() + ")";
                } else {
                    expression = insertValue.getExpression().substring(0, insertValue.getExpression().length() - 1) + ", ?)";
                    currentParameters.add(currentGeneratedKey);
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
