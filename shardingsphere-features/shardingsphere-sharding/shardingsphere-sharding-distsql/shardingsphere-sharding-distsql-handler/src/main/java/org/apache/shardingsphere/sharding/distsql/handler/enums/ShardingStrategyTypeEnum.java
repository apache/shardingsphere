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

package org.apache.shardingsphere.sharding.distsql.handler.enums;

import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Sharding strategy type enum.
 */
public enum ShardingStrategyTypeEnum {
    
    STANDARD {
        @Override
        public ShardingStrategyConfiguration createConfiguration(final String shardingAlgorithmName, final String shardingColumn) {
            return new StandardShardingStrategyConfiguration(shardingColumn, shardingAlgorithmName);
        }
        
        @Override
        public Class<? extends ShardingStrategyConfiguration> getImplementedClass() {
            return StandardShardingStrategyConfiguration.class;
        }
        
        @Override
        public Collection<String> getConfigurationContents(final ShardingStrategyConfiguration strategyConfiguration) {
            return Arrays.asList(((StandardShardingStrategyConfiguration) strategyConfiguration).getShardingColumn(), strategyConfiguration.getShardingAlgorithmName());
        }
    }, NONE {
        @Override
        public ShardingStrategyConfiguration createConfiguration(final String shardingAlgorithmName, final String shardingColumn) {
            return new NoneShardingStrategyConfiguration();
        }
        
        @Override
        public Class<? extends ShardingStrategyConfiguration> getImplementedClass() {
            return NoneShardingStrategyConfiguration.class;
        }
        
        @Override
        public Collection<String> getConfigurationContents(final ShardingStrategyConfiguration strategyConfiguration) {
            return Arrays.asList("", strategyConfiguration.getShardingAlgorithmName());
        }
    }, HINT {
        @Override
        public ShardingStrategyConfiguration createConfiguration(final String shardingAlgorithmName, final String shardingColumn) {
            return new HintShardingStrategyConfiguration(shardingAlgorithmName);
        }
        
        @Override
        public Class<? extends ShardingStrategyConfiguration> getImplementedClass() {
            return HintShardingStrategyConfiguration.class;
        }
        
        @Override
        public Collection<String> getConfigurationContents(final ShardingStrategyConfiguration strategyConfiguration) {
            return Arrays.asList("", strategyConfiguration.getShardingAlgorithmName());
        }
    }, COMPLEX {
        @Override
        public ShardingStrategyConfiguration createConfiguration(final String shardingAlgorithmName, final String shardingColumn) {
            return new ComplexShardingStrategyConfiguration(shardingColumn, shardingAlgorithmName);
        }
        
        @Override
        public Class<? extends ShardingStrategyConfiguration> getImplementedClass() {
            return ComplexShardingStrategyConfiguration.class;
        }
        
        @Override
        public Collection<String> getConfigurationContents(final ShardingStrategyConfiguration strategyConfiguration) {
            return Arrays.asList(((ComplexShardingStrategyConfiguration) strategyConfiguration).getShardingColumns(), strategyConfiguration.getShardingAlgorithmName());
        }
    };
    
    /**
     * Get the sharding strategy configuration.
     *
     * @param shardingAlgorithmName sharding algorithm name
     * @param shardingColumn sharding column
     * @return sharding strategy configuration
     */
    public abstract ShardingStrategyConfiguration createConfiguration(String shardingAlgorithmName, String shardingColumn);
    
    /**
     * Get the class that implements the strategy.
     *
     * @return Class implementing the strategy
     */
    public abstract Class<? extends ShardingStrategyConfiguration> getImplementedClass();
    
    /**
     * Get the content in the configuration.
     * @param strategyConfiguration sharding strategy configuration.
     * 
     * @return Content list
     */
    public abstract Collection<String> getConfigurationContents(ShardingStrategyConfiguration strategyConfiguration);
    
    /**
     * Returns the sharding strategy type.
     *
     * @param name name
     * @return sharding strategy type
     */
    public static ShardingStrategyTypeEnum getValueOf(final String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException(String.format("unsupported strategy type %s", name));
        }
    }
    
    /**
     * Returns the sharding strategy type.
     *
     * @param shardingStrategyConfiguration Implementation class of sharding strategy configuration
     * @return sharding strategy type
     */
    public static ShardingStrategyTypeEnum getValueOf(final ShardingStrategyConfiguration shardingStrategyConfiguration) {
        Optional<ShardingStrategyTypeEnum> type = Arrays.stream(values())
                .filter(each -> shardingStrategyConfiguration.getClass().getCanonicalName().equals(each.getImplementedClass().getCanonicalName())).findFirst();
        type.orElseThrow(() -> new UnsupportedOperationException(String.format("unsupported strategy type %s", shardingStrategyConfiguration.getClass().getName())));
        return type.get();
    }
    
    /**
     * Determine whether the specified type is included.
     *
     * @param name name
     * @return have data or not
     */
    public static boolean contain(final String name) {
        return Arrays.stream(values()).map(Enum::name).anyMatch(each -> each.equalsIgnoreCase(name));
    }
}
