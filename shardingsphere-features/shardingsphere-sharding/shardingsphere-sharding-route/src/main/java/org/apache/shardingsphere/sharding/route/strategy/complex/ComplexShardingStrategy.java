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

package org.apache.shardingsphere.sharding.route.strategy.complex;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Range;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;
import org.apache.shardingsphere.sharding.route.strategy.ShardingStrategy;
import org.apache.shardingsphere.sharding.route.strategy.value.ListRouteValue;
import org.apache.shardingsphere.sharding.route.strategy.value.RangeRouteValue;
import org.apache.shardingsphere.sharding.route.strategy.value.RouteValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Complex sharding strategy.
 */
@Getter
public final class ComplexShardingStrategy implements ShardingStrategy {
    
    private final Collection<String> shardingColumns;
    
    private final ComplexKeysShardingAlgorithm<?> shardingAlgorithm;
    
    public ComplexShardingStrategy(final String shardingColumns, final ComplexKeysShardingAlgorithm<?> shardingAlgorithm) {
        Preconditions.checkNotNull(shardingColumns, "Sharding columns cannot be null.");
        Preconditions.checkNotNull(shardingAlgorithm, "Sharding algorithm cannot be null.");
        this.shardingColumns = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.shardingColumns.addAll(Splitter.on(",").trimResults().splitToList(shardingColumns));
        this.shardingAlgorithm = shardingAlgorithm;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<RouteValue> shardingValues, final ConfigurationProperties props) {
        Map<String, Collection<Comparable<?>>> columnShardingValues = new HashMap<>(shardingValues.size(), 1);
        Map<String, Range<Comparable<?>>> columnRangeValues = new HashMap<>(shardingValues.size(), 1);
        String logicTableName = "";
        for (RouteValue each : shardingValues) {
            if (each instanceof ListRouteValue) {
                columnShardingValues.put(each.getColumnName(), ((ListRouteValue) each).getValues());
            } else if (each instanceof RangeRouteValue) {
                columnRangeValues.put(each.getColumnName(), ((RangeRouteValue) each).getValueRange());
            }
            logicTableName = each.getTableName();
        }
        Collection<String> shardingResult = shardingAlgorithm.doSharding(availableTargetNames, new ComplexKeysShardingValue(logicTableName, columnShardingValues, columnRangeValues));
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(shardingResult);
        return result;
    }
}
