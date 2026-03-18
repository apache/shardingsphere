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

package org.apache.shardingsphere.sharding.route.strategy.type.complex;

import com.cedarsoftware.util.CaseInsensitiveSet;
import com.google.common.base.Splitter;
import com.google.common.collect.Range;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;
import org.apache.shardingsphere.sharding.exception.metadata.MissingRequiredShardingConfigurationException;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.route.strategy.ShardingStrategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Complex sharding strategy.
 */
@Getter
public final class ComplexShardingStrategy implements ShardingStrategy {
    
    private final Collection<String> shardingColumns;
    
    private final ComplexKeysShardingAlgorithm<?> shardingAlgorithm;
    
    public ComplexShardingStrategy(final String shardingColumns, final ComplexKeysShardingAlgorithm<?> shardingAlgorithm) {
        ShardingSpherePreconditions.checkNotNull(shardingColumns, () -> new MissingRequiredShardingConfigurationException("Complex sharding columns"));
        ShardingSpherePreconditions.checkNotNull(shardingAlgorithm, () -> new MissingRequiredShardingConfigurationException("Complex sharding algorithm"));
        this.shardingColumns = new CaseInsensitiveSet<>();
        this.shardingColumns.addAll(Splitter.on(",").trimResults().splitToList(shardingColumns));
        this.shardingAlgorithm = shardingAlgorithm;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<ShardingConditionValue> shardingConditionValues,
                                         final DataNodeInfo dataNodeInfo, final ConfigurationProperties props) {
        Map<String, Collection<Comparable<?>>> columnShardingValues = new HashMap<>(shardingConditionValues.size(), 1F);
        Map<String, Range<Comparable<?>>> columnRangeValues = new HashMap<>(shardingConditionValues.size(), 1F);
        String logicTableName = "";
        for (ShardingConditionValue each : shardingConditionValues) {
            if (each instanceof ListShardingConditionValue) {
                columnShardingValues.put(each.getColumnName(), ((ListShardingConditionValue) each).getValues());
            } else if (each instanceof RangeShardingConditionValue) {
                columnRangeValues.put(each.getColumnName(), ((RangeShardingConditionValue) each).getValueRange());
            }
            logicTableName = each.getTableName();
        }
        Collection<String> shardingResult = shardingAlgorithm.doSharding(availableTargetNames, new ComplexKeysShardingValue(logicTableName, columnShardingValues, columnRangeValues));
        return new CaseInsensitiveSet<>(shardingResult);
    }
}
