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

import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.fixture.CoreComplexKeysShardingAlgorithmFixture;
import org.apache.shardingsphere.sharding.fixture.CoreHintShardingAlgorithmFixture;
import org.apache.shardingsphere.sharding.fixture.CoreStandardShardingAlgorithmFixture;
import org.apache.shardingsphere.sharding.route.strategy.type.complex.ComplexShardingStrategy;
import org.apache.shardingsphere.sharding.route.strategy.type.hint.HintShardingStrategy;
import org.apache.shardingsphere.sharding.route.strategy.type.none.NoneShardingStrategy;
import org.apache.shardingsphere.sharding.route.strategy.type.standard.StandardShardingStrategy;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingStrategyFactoryTest {
    
    @Test
    void assertNewInstanceForStandardShardingStrategy() {
        StandardShardingStrategyConfiguration standardShardingStrategyConfig = mock(StandardShardingStrategyConfiguration.class);
        when(standardShardingStrategyConfig.getShardingColumn()).thenReturn("standard_sharding_column");
        assertThat(ShardingStrategyFactory.newInstance(standardShardingStrategyConfig, mock(CoreStandardShardingAlgorithmFixture.class), null), isA(StandardShardingStrategy.class));
    }
    
    @Test
    void assertNewInstanceForStandardShardingStrategyWithDefaultColumnStrategy() {
        ShardingStrategy actual = ShardingStrategyFactory.newInstance(mock(StandardShardingStrategyConfiguration.class), mock(CoreStandardShardingAlgorithmFixture.class), "order_id");
        assertTrue(actual.getShardingColumns().contains("order_id"));
    }
    
    @Test
    void assertNewInstanceForComplexShardingStrategy() {
        ComplexShardingStrategyConfiguration complexShardingStrategyConfig = mock(ComplexShardingStrategyConfiguration.class);
        when(complexShardingStrategyConfig.getShardingColumns()).thenReturn("complex_sharding_column");
        assertThat(ShardingStrategyFactory.newInstance(complexShardingStrategyConfig, mock(CoreComplexKeysShardingAlgorithmFixture.class), null), isA(ComplexShardingStrategy.class));
    }
    
    @Test
    void assertNewInstanceForHintShardingStrategy() {
        assertThat(ShardingStrategyFactory.newInstance(mock(HintShardingStrategyConfiguration.class), mock(CoreHintShardingAlgorithmFixture.class), null), isA(HintShardingStrategy.class));
    }
    
    @Test
    void assertNewInstanceForNoneShardingStrategy() {
        assertThat(ShardingStrategyFactory.newInstance(null, null, null), isA(NoneShardingStrategy.class));
    }
}
