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

package org.apache.shardingsphere.sharding.distsql.update;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.UnusedAlgorithmFinder;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnusedAlgorithmFinderTest {
    
    private static final String USED_TABLE_SHARDING_ALGORITHM = "used_table_sharding_algorithm";
    
    private static final String USED_TABLE_SHARDING_DEFAULT_ALGORITHM = "used_table_sharding_default_algorithm";
    
    private static final String USED_DATABASE_SHARDING_ALGORITHM = "used_database_sharding_algorithm";
    
    private static final String USED_DATABASE_SHARDING_DEFAULT_ALGORITHM = "used_database_sharding_default_algorithm";
    
    private static final String UNUSED_ALGORITHM = "unused_algorithm";
    
    @Test
    void assertFind() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = getShardingTableRuleConfiguration();
        ruleConfig.getTables().add(shardingTableRuleConfig);
        ruleConfig.getShardingAlgorithms().putAll(getAlgorithms());
        ruleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", USED_DATABASE_SHARDING_DEFAULT_ALGORITHM));
        ruleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", USED_TABLE_SHARDING_DEFAULT_ALGORITHM));
        Collection<String> actual = UnusedAlgorithmFinder.findUnusedShardingAlgorithm(ruleConfig);
        assertNotNull(actual);
        assertThat(actual.size(), is(1));
        assertTrue(actual.contains(UNUSED_ALGORITHM));
    }
    
    private ShardingTableRuleConfiguration getShardingTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", null);
        result.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", USED_TABLE_SHARDING_ALGORITHM));
        result.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", USED_DATABASE_SHARDING_ALGORITHM));
        return result;
    }
    
    private Map<String, AlgorithmConfiguration> getAlgorithms() {
        return ImmutableMap.of(
                USED_DATABASE_SHARDING_ALGORITHM, new AlgorithmConfiguration("INLINE", new Properties()),
                USED_DATABASE_SHARDING_DEFAULT_ALGORITHM, new AlgorithmConfiguration("INLINE", new Properties()),
                USED_TABLE_SHARDING_ALGORITHM, new AlgorithmConfiguration("INLINE", new Properties()),
                USED_TABLE_SHARDING_DEFAULT_ALGORITHM, new AlgorithmConfiguration("INLINE", new Properties()),
                UNUSED_ALGORITHM, new AlgorithmConfiguration("INLINE", new Properties()));
    }
}
