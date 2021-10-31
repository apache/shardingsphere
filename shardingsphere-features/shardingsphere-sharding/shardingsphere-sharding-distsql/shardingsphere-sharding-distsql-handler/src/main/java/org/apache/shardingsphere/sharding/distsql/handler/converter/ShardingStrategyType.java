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

package org.apache.shardingsphere.sharding.distsql.handler.converter;

import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;

import java.util.Arrays;

/**
 * Sharding strategy type.
 */
public enum ShardingStrategyType {
    
    STANDARD {
        @Override
        public ShardingStrategyConfiguration getConfiguration(final String shardingAlgorithmName, final String shardingColumn) {
            return new StandardShardingStrategyConfiguration(shardingColumn, shardingAlgorithmName);
        }
        
    }, NONE {
        @Override
        public ShardingStrategyConfiguration getConfiguration(final String shardingAlgorithmName, final String shardingColumn) {
            return new NoneShardingStrategyConfiguration();
        }
    }, HINT {
        @Override
        public ShardingStrategyConfiguration getConfiguration(final String shardingAlgorithmName, final String shardingColumn) {
            return new HintShardingStrategyConfiguration(shardingAlgorithmName);
        }
    }, COMPLEX {
        @Override
        public ShardingStrategyConfiguration getConfiguration(final String shardingAlgorithmName, final String shardingColumn) {
            return new ComplexShardingStrategyConfiguration(shardingColumn, shardingAlgorithmName);
        }
    };
    
    /**
     * Get the sharding strategy configuration.
     *
     * @param shardingAlgorithmName sharding algorithm name
     * @param shardingColumn sharding column
     * @return sharding strategy configuration
     */
    public abstract ShardingStrategyConfiguration getConfiguration(String shardingAlgorithmName, String shardingColumn);
    
    /**
     * Returns the sharding strategy type.
     *
     * @param name name
     * @return sharding strategy type
     */
    public static ShardingStrategyType getValueOf(final String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException(String.format("unsupported strategy type %s", name));
        }
    }
    
    /**
     * Determine whether the specified type is included.
     * @param name name
     * @return have data or not
     */
    public static boolean contain(final String name) {
        return Arrays.stream(values()).map(Enum::name).anyMatch(each -> each.equalsIgnoreCase(name));
    }
}
