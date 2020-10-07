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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.route.strategy.complex.ComplexShardingStrategy;
import org.apache.shardingsphere.sharding.route.strategy.hint.HintShardingStrategy;
import org.apache.shardingsphere.sharding.route.strategy.none.NoneShardingStrategy;
import org.apache.shardingsphere.sharding.route.strategy.standard.StandardShardingStrategy;

/**
 * Sharding strategy factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingStrategyFactory {
    
    /**
     * Create sharding strategy.
     * 
     * @param shardingStrategyConfig sharding strategy configuration
     * @param shardingAlgorithm sharding algorithm
     * @return sharding strategy instance
     */
    public static ShardingStrategy newInstance(final ShardingStrategyConfiguration shardingStrategyConfig, final ShardingAlgorithm shardingAlgorithm) {
        if (shardingAlgorithm instanceof StandardShardingAlgorithm) {
            return new StandardShardingStrategy(((StandardShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumn(), (StandardShardingAlgorithm) shardingAlgorithm);
        }
        if (shardingAlgorithm instanceof ComplexKeysShardingAlgorithm) {
            return new ComplexShardingStrategy(((ComplexShardingStrategyConfiguration) shardingStrategyConfig).getShardingColumns(), (ComplexKeysShardingAlgorithm) shardingAlgorithm);
        }
        if (shardingAlgorithm instanceof HintShardingAlgorithm) {
            return new HintShardingStrategy((HintShardingAlgorithm) shardingAlgorithm);
        }
        return new NoneShardingStrategy();
    }
}
