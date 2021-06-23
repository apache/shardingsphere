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

package org.apache.shardingsphere.sharding.route.strategy.type.standard;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.route.strategy.ShardingStrategy;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeSet;

/**
 * Standard sharding strategy.
 */
public final class StandardShardingStrategy implements ShardingStrategy {
    
    private final String shardingColumn;
    
    @Getter
    private final StandardShardingAlgorithm<?> shardingAlgorithm;
    
    public StandardShardingStrategy(final String shardingColumn, final StandardShardingAlgorithm<?> shardingAlgorithm) {
        Preconditions.checkNotNull(shardingColumn, "Sharding column cannot be null.");
        Preconditions.checkNotNull(shardingAlgorithm, "sharding algorithm cannot be null.");
        this.shardingColumn = shardingColumn;
        this.shardingAlgorithm = shardingAlgorithm;
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<ShardingConditionValue> shardingConditionValues, final ConfigurationProperties props) {
        ShardingConditionValue shardingConditionValue = shardingConditionValues.iterator().next();
        Collection<String> shardingResult = shardingConditionValue instanceof ListShardingConditionValue
                ? doSharding(availableTargetNames, (ListShardingConditionValue) shardingConditionValue) : doSharding(availableTargetNames, (RangeShardingConditionValue) shardingConditionValue);
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.addAll(shardingResult);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Collection<String> doSharding(final Collection<String> availableTargetNames, final ListShardingConditionValue<?> shardingValue) {
        Collection<String> result = new LinkedList<>();
        for (Comparable<?> each : shardingValue.getValues()) {
            String target;
            target = shardingAlgorithm.doSharding(availableTargetNames, new PreciseShardingValue(shardingValue.getTableName(), shardingValue.getColumnName(), each));
            if (null != target && availableTargetNames.contains(target)) {
                result.add(target);
            } else if (null != target && !availableTargetNames.contains(target)) {
                throw new ShardingSphereException(String.format("Route table %s does not exist, available actual table: %s", target, availableTargetNames));
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingConditionValue<?> shardingValue) {
        return shardingAlgorithm.doSharding(availableTargetNames,
                new RangeShardingValue(shardingValue.getTableName(), shardingValue.getColumnName(), shardingValue.getValueRange()));
    }
    
    @Override
    public Collection<String> getShardingColumns() {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(shardingColumn);
        return result;
    }
}
