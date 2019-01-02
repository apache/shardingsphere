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

package io.shardingsphere.core.routing.strategy.inline;

import com.google.common.base.Preconditions;
import groovy.lang.Closure;
import groovy.util.Expando;
import io.shardingsphere.api.algorithm.sharding.ListShardingValue;
import io.shardingsphere.api.algorithm.sharding.PreciseShardingValue;
import io.shardingsphere.api.algorithm.sharding.ShardingValue;
import io.shardingsphere.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingsphere.core.routing.strategy.ShardingStrategy;
import io.shardingsphere.core.util.InlineExpressionParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * Standard sharding strategy.
 * 
 * @author zhangliang
 */
public final class InlineShardingStrategy implements ShardingStrategy {
    
    private final String shardingColumn;
    
    private final Closure<?> closure;
    
    public InlineShardingStrategy(final InlineShardingStrategyConfiguration inlineShardingStrategyConfig) {
        Preconditions.checkNotNull(inlineShardingStrategyConfig.getShardingColumn(), "Sharding column cannot be null.");
        Preconditions.checkNotNull(inlineShardingStrategyConfig.getAlgorithmExpression(), "Sharding algorithm expression cannot be null.");
        shardingColumn = inlineShardingStrategyConfig.getShardingColumn();
        String algorithmExpression = InlineExpressionParser.handlePlaceHolder(inlineShardingStrategyConfig.getAlgorithmExpression().trim());
        closure = new InlineExpressionParser(algorithmExpression).evaluateClosure();
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<ShardingValue> shardingValues) {
        ShardingValue shardingValue = shardingValues.iterator().next();
        Preconditions.checkState(shardingValue instanceof ListShardingValue, "Inline strategy cannot support range sharding.");
        Collection<String> shardingResult = doSharding((ListShardingValue) shardingValue);
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(shardingResult);
        return result;
    }
    
    private Collection<String> doSharding(final ListShardingValue shardingValue) {
        Collection<String> result = new LinkedList<>();
        for (PreciseShardingValue<?> each : transferToPreciseShardingValues(shardingValue)) {
            result.add(execute(each));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private List<PreciseShardingValue> transferToPreciseShardingValues(final ListShardingValue<?> shardingValue) {
        List<PreciseShardingValue> result = new ArrayList<>(shardingValue.getValues().size());
        for (Comparable<?> each : shardingValue.getValues()) {
            result.add(new PreciseShardingValue(shardingValue.getLogicTableName(), shardingValue.getColumnName(), each));
        }
        return result;
    }
    
    private String execute(final PreciseShardingValue shardingValue) {
        Closure<?> result = closure.rehydrate(new Expando(), null, null);
        result.setResolveStrategy(Closure.DELEGATE_ONLY);
        result.setProperty(shardingValue.getColumnName(), shardingValue.getValue());
        return result.call().toString();
    }
    
    @Override
    public Collection<String> getShardingColumns() {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(shardingColumn);
        return result;
    }
}
