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

package org.apache.shardingsphere.sharding.route.strategy.type.hint;

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingValue;
import org.apache.shardingsphere.sharding.exception.metadata.MissingRequiredShardingConfigurationException;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.route.strategy.ShardingStrategy;

import java.util.Collection;

/**
 * Hint sharding strategy.
 */
@Getter
public final class HintShardingStrategy implements ShardingStrategy {
    
    private final Collection<String> shardingColumns;
    
    private final HintShardingAlgorithm<?> shardingAlgorithm;
    
    public HintShardingStrategy(final HintShardingAlgorithm<?> shardingAlgorithm) {
        ShardingSpherePreconditions.checkNotNull(shardingAlgorithm, () -> new MissingRequiredShardingConfigurationException("Hint sharding algorithm"));
        shardingColumns = new CaseInsensitiveSet<>();
        this.shardingAlgorithm = shardingAlgorithm;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final Collection<ShardingConditionValue> shardingConditionValues,
                                         final DataNodeInfo dataNodeInfo, final ConfigurationProperties props) {
        ListShardingConditionValue<?> shardingValue = (ListShardingConditionValue) shardingConditionValues.iterator().next();
        Collection<String> shardingResult = shardingAlgorithm.doSharding(availableTargetNames,
                new HintShardingValue(shardingValue.getTableName(), shardingValue.getColumnName(), shardingValue.getValues()));
        return new CaseInsensitiveSet<>(shardingResult);
    }
}
