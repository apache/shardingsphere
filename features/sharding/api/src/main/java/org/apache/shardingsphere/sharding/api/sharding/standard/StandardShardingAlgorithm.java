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

package org.apache.shardingsphere.sharding.api.sharding.standard;

import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import java.util.Collection;

/**
 * Standard sharding algorithm.
 * 
 * @param <T> class type of sharding value
 */
public interface StandardShardingAlgorithm<T extends Comparable<?>> extends ShardingAlgorithm {
    
    /**
     * Sharding.
     * 
     * @param availableTargetNames available data sources or table names
     * @param shardingValue sharding value
     * @return sharding result for data source or table name
     */
    String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<T> shardingValue);
    
    /**
     * Sharding.
     *
     * @param availableTargetNames available data sources or table names
     * @param shardingValue sharding value
     * @return sharding results for data sources or table names
     */
    Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<T> shardingValue);
}
