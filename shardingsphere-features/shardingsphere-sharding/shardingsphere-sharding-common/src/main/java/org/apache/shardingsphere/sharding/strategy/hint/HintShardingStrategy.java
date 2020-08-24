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

package org.apache.shardingsphere.sharding.strategy.hint;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingValue;
import org.apache.shardingsphere.sharding.strategy.ShardingStrategy;
import org.apache.shardingsphere.sharding.strategy.value.ListRouteValue;
import org.apache.shardingsphere.sharding.strategy.value.RouteValue;

import java.util.Collection;
import java.util.TreeSet;

/**
 * Hint sharding strategy.
 */
@Getter
public final class HintShardingStrategy implements ShardingStrategy {
    
    private final Collection<String> shardingColumns;
    
    private final HintShardingAlgorithm shardingAlgorithm;
    
    public HintShardingStrategy(final HintShardingAlgorithm shardingAlgorithm) {
        Preconditions.checkNotNull(shardingAlgorithm, "Sharding algorithm cannot be null.");
        shardingColumns = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.shardingAlgorithm = shardingAlgorithm;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<RouteValue> shardingValues, final ConfigurationProperties props) {
        ListRouteValue shardingValue = (ListRouteValue) shardingValues.iterator().next();
        Collection<String> shardingResult = shardingAlgorithm.doSharding(availableTargetNames, 
                new HintShardingValue(shardingValue.getTableName(), shardingValue.getColumnName(), shardingValue.getValues()));
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(shardingResult);
        return result;
    }
}
