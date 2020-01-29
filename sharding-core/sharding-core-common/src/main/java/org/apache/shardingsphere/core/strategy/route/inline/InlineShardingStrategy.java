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

package org.apache.shardingsphere.core.strategy.route.inline;

import com.google.common.base.Preconditions;
import groovy.lang.Closure;
import groovy.util.Expando;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.core.strategy.route.ShardingStrategy;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RangeRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;
import org.apache.shardingsphere.underlying.common.config.inline.InlineExpressionParser;
import org.apache.shardingsphere.underlying.common.constant.properties.PropertiesConstant;
import org.apache.shardingsphere.underlying.common.constant.properties.ShardingSphereProperties;

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
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<RouteValue> shardingValues, final ShardingSphereProperties properties) {
        RouteValue shardingValue = shardingValues.iterator().next();
        if (properties.<Boolean>getValue(PropertiesConstant.ALLOW_RANGE_QUERY_WITH_INLINE_SHARDING) && shardingValue instanceof RangeRouteValue) {
            return availableTargetNames;
        }
        Preconditions.checkState(shardingValue instanceof ListRouteValue, "Inline strategy cannot support this type sharding:" + shardingValue.toString());
        Collection<String> shardingResult = doSharding((ListRouteValue) shardingValue);
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String each : shardingResult) {
            if (availableTargetNames.contains(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<String> doSharding(final ListRouteValue shardingValue) {
        Collection<String> result = new LinkedList<>();
        for (PreciseShardingValue<?> each : transferToPreciseShardingValues(shardingValue)) {
            result.add(execute(each));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private List<PreciseShardingValue> transferToPreciseShardingValues(final ListRouteValue<?> shardingValue) {
        List<PreciseShardingValue> result = new ArrayList<>(shardingValue.getValues().size());
        for (Comparable<?> each : shardingValue.getValues()) {
            result.add(new PreciseShardingValue(shardingValue.getTableName(), shardingValue.getColumnName(), each));
        }
        return result;
    }
    
    private String execute(final PreciseShardingValue shardingValue) {
        Closure<?> result = closure.rehydrate(new Expando(), null, null);
        result.setResolveStrategy(Closure.DELEGATE_ONLY);
        result.setProperty(shardingColumn, shardingValue.getValue());
        return result.call().toString();
    }
    
    @Override
    public Collection<String> getShardingColumns() {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(shardingColumn);
        return result;
    }
}
