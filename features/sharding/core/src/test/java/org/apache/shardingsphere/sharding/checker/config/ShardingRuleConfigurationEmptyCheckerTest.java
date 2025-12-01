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

package org.apache.shardingsphere.sharding.checker.config;

import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationEmptyChecker;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ShardingRuleConfigurationEmptyCheckerTest {
    
    private ShardingRuleConfigurationEmptyChecker checker;
    
    @BeforeEach
    void setUp() {
        checker = (ShardingRuleConfigurationEmptyChecker) TypedSPILoader.getService(DatabaseRuleConfigurationEmptyChecker.class, ShardingRuleConfiguration.class);
    }
    
    @Test
    void assertIsNotEmptyWithTables() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getTables().add(mock(ShardingTableRuleConfiguration.class));
        assertFalse(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsNotEmptyWithAutoTables() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getAutoTables().add(mock(ShardingAutoTableRuleConfiguration.class));
        assertFalse(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsNotEmptyWithGetDefaultDatabaseShardingStrategy() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.setDefaultDatabaseShardingStrategy(mock(ShardingStrategyConfiguration.class));
        assertFalse(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsNotEmptyWithGetDefaultTableShardingStrategy() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.setDefaultTableShardingStrategy(mock(ShardingStrategyConfiguration.class));
        assertFalse(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsEmpty() {
        assertTrue(checker.isEmpty(new ShardingRuleConfiguration()));
    }
}
