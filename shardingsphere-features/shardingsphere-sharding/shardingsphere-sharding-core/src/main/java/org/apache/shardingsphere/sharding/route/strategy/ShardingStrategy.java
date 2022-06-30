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

package org.apache.shardingsphere.sharding.route.strategy;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Sharding strategy.
 */
public interface ShardingStrategy extends Comparable {
    
    /**
     * Get sharding columns.
     * 
     * @return sharding columns
     */
    Collection<String> getShardingColumns();
    
    /**
     * Get sharding algorithm.
     *
     * @return sharding algorithm
     */
    ShardingAlgorithm getShardingAlgorithm();
    
    /**
     * Sharding.
     *
     * @param availableTargetNames available data source or table names
     * @param shardingConditionValues sharding condition values
     * @param dataNodeInfo data node info
     * @param props configuration properties
     * @return sharding results for data source or table names
     */
    Collection<String> doSharding(Collection<String> availableTargetNames, Collection<ShardingConditionValue> shardingConditionValues, DataNodeInfo dataNodeInfo, ConfigurationProperties props);
    
    @Override
    default int compareTo(final Object other) {
        if (!(other instanceof ShardingStrategy)) {
            return -1;
        }
        ShardingStrategy otherShardingStrategy = (ShardingStrategy) other;
        if (!new ArrayList(this.getShardingColumns()).equals(new ArrayList(otherShardingStrategy.getShardingColumns()))) {
            return -1;
        }
        if (null == this.getShardingAlgorithm() || null == otherShardingStrategy.getShardingAlgorithm()) {
            return -1;
        }
        if (0 != this.getShardingAlgorithm().compareTo(otherShardingStrategy.getShardingAlgorithm())) {
            return -1;
        }
        return 0;
    }
}
