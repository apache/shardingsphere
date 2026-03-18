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

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.exception.metadata.MissingRequiredShardingConfigurationException;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.route.strategy.ShardingStrategy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Standard sharding strategy.
 */
@Getter
public final class StandardShardingStrategy implements ShardingStrategy {
    
    private final Collection<String> shardingColumns;
    
    private final StandardShardingAlgorithm<?> shardingAlgorithm;
    
    public StandardShardingStrategy(final String shardingColumn, final StandardShardingAlgorithm<?> shardingAlgorithm) {
        ShardingSpherePreconditions.checkNotNull(shardingColumn, () -> new MissingRequiredShardingConfigurationException("Standard sharding column"));
        ShardingSpherePreconditions.checkNotNull(shardingAlgorithm, () -> new MissingRequiredShardingConfigurationException("Standard sharding algorithm"));
        Collection<String> shardingColumns = new CaseInsensitiveSet<>();
        shardingColumns.add(shardingColumn);
        this.shardingColumns = Collections.unmodifiableCollection(shardingColumns);
        this.shardingAlgorithm = shardingAlgorithm;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<ShardingConditionValue> shardingConditionValues,
                                         final DataNodeInfo dataNodeInfo, final ConfigurationProperties props) {
        ShardingConditionValue shardingConditionValue = shardingConditionValues.iterator().next();
        Collection<String> shardingResult = shardingConditionValue instanceof ListShardingConditionValue
                ? doSharding(availableTargetNames, (ListShardingConditionValue) shardingConditionValue, dataNodeInfo)
                : doSharding(availableTargetNames, (RangeShardingConditionValue) shardingConditionValue, dataNodeInfo);
        return new CaseInsensitiveSet<>(shardingResult);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection<String> doSharding(final Collection<String> availableTargetNames, final ListShardingConditionValue<?> shardingValue, final DataNodeInfo dataNodeInfo) {
        Collection<String> result = new LinkedList<>();
        for (Object each : shardingValue.getValues()) {
            String target = shardingAlgorithm.doSharding(availableTargetNames,
                    new PreciseShardingValue(shardingValue.getTableName(), shardingValue.getColumnName(), dataNodeInfo, each));
            if (null != target && availableTargetNames.contains(target)) {
                result.add(target);
            }
            // TODO add ShardingRouteAlgorithmException check when autoTables support config actualDataNodes in #33364
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingConditionValue<?> shardingValue, final DataNodeInfo dataNodeInfo) {
        return shardingAlgorithm.doSharding(availableTargetNames,
                new RangeShardingValue(shardingValue.getTableName(), shardingValue.getColumnName(), dataNodeInfo, shardingValue.getValueRange()));
    }
}
