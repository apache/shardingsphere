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

import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Sharding strategy type.
 */
public enum ShardingStrategyType {
    
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
        public Collection<String> getConfigurationContents(final ShardingStrategyConfiguration strategyConfig) {
            return Arrays.asList(((StandardShardingStrategyConfiguration) strategyConfig).getShardingColumn(), strategyConfig.getShardingAlgorithmName());
        }
        
        @Override
        public boolean isValid(final String shardingColumn) {
            return null != shardingColumn && !shardingColumn.contains(",");
        }
    },
    NONE {
        
        @Override
        public ShardingStrategyConfiguration createConfiguration(final String shardingAlgorithmName, final String shardingColumn) {
            return new NoneShardingStrategyConfiguration();
        }
        
        @Override
        public Class<? extends ShardingStrategyConfiguration> getImplementedClass() {
            return NoneShardingStrategyConfiguration.class;
        }
        
        @Override
        public Collection<String> getConfigurationContents(final ShardingStrategyConfiguration strategyConfig) {
            return Arrays.asList("", strategyConfig.getShardingAlgorithmName());
        }
        
        @Override
        public boolean isValid(final String shardingColumn) {
            return true;
        }
    },
    HINT {
        
        @Override
        public ShardingStrategyConfiguration createConfiguration(final String shardingAlgorithmName, final String shardingColumn) {
            return new HintShardingStrategyConfiguration(shardingAlgorithmName);
        }
        
        @Override
        public Class<? extends ShardingStrategyConfiguration> getImplementedClass() {
            return HintShardingStrategyConfiguration.class;
        }
        
        @Override
        public Collection<String> getConfigurationContents(final ShardingStrategyConfiguration strategyConfig) {
            return Arrays.asList("", strategyConfig.getShardingAlgorithmName());
        }
        
        @Override
        public boolean isValid(final String shardingColumn) {
            return null == shardingColumn;
        }
    },
    COMPLEX {
        
        @Override
        public ShardingStrategyConfiguration createConfiguration(final String shardingAlgorithmName, final String shardingColumn) {
            return new ComplexShardingStrategyConfiguration(shardingColumn, shardingAlgorithmName);
        }
        
        @Override
        public Class<? extends ShardingStrategyConfiguration> getImplementedClass() {
            return ComplexShardingStrategyConfiguration.class;
        }
        
        @Override
        public Collection<String> getConfigurationContents(final ShardingStrategyConfiguration strategyConfig) {
            return Arrays.asList(((ComplexShardingStrategyConfiguration) strategyConfig).getShardingColumns(), strategyConfig.getShardingAlgorithmName());
        }
        
        @Override
        public boolean isValid(final String shardingColumn) {
            return null != shardingColumn && shardingColumn.split(",").length > 1;
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
     *
     * @param strategyConfig sharding strategy configuration.
     * @return contents
     */
    public abstract Collection<String> getConfigurationContents(ShardingStrategyConfiguration strategyConfig);
    
    /**
     * Check whether the configuration is valid.
     *
     * @param shardingColumn sharding column
     * @return valid or invalid
     */
    public abstract boolean isValid(String shardingColumn);
    
    /**
     * Returns the sharding strategy type.
     *
     * @param name name
     * @return sharding strategy type
     * @throws UnsupportedSQLOperationException unsupported SQL operation exception
     */
    public static ShardingStrategyType getValueOf(final String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (final IllegalArgumentException ignored) {
            throw new UnsupportedSQLOperationException(String.format("unsupported strategy type `%s`", name));
        }
    }
    
    /**
     * Returns the sharding strategy type.
     *
     * @param config sharding strategy configuration
     * @return sharding strategy type
     */
    public static ShardingStrategyType getValueOf(final ShardingStrategyConfiguration config) {
        Optional<ShardingStrategyType> result = Arrays.stream(values()).filter(each -> config.getClass().isAssignableFrom(each.getImplementedClass())).findFirst();
        ShardingSpherePreconditions.checkState(result.isPresent(), () -> new UnsupportedOperationException(String.format("unsupported strategy type: `%s`.", config.getClass().getName())));
        return result.get();
    }
    
    /**
     * Determine whether contains type.
     *
     * @param type type
     * @return contains or not
     */
    public static boolean contains(final String type) {
        return Arrays.stream(values()).map(Enum::name).anyMatch(each -> each.equalsIgnoreCase(type));
    }
}
