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

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import java.util.Collection;

/**
 * Sharding strategy.
 */
public interface ShardingStrategy {
    
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
     * @param props configuration properties
     * @return sharding results for data source or table names
     */
    Collection<String> doSharding(Collection<String> availableTargetNames, Collection<ShardingConditionValue> shardingConditionValues, ConfigurationProperties props);
}
