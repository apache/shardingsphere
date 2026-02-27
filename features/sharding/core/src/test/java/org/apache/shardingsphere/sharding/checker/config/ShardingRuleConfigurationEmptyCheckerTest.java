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
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheOptionsConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingRuleConfigurationEmptyCheckerTest {
    
    private ShardingRuleConfigurationEmptyChecker checker;
    
    @BeforeEach
    void setUp() {
        checker = (ShardingRuleConfigurationEmptyChecker) TypedSPILoader.getService(DatabaseRuleConfigurationEmptyChecker.class, ShardingRuleConfiguration.class);
    }
    
    @Test
    void assertIsNotEmptyWithTables() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds_0.t_order"));
        assertFalse(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsNotEmptyWithAutoTables() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getAutoTables().add(new ShardingAutoTableRuleConfiguration("t_order", "ds_0"));
        assertFalse(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsNotEmptyWithBindingTableGroups() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("binding_group", "t_order,t_order_item"));
        assertFalse(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsNotEmptyWithGetDefaultDatabaseShardingStrategy() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.setDefaultDatabaseShardingStrategy(new NoneShardingStrategyConfiguration());
        assertFalse(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsNotEmptyWithGetDefaultTableShardingStrategy() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.setDefaultTableShardingStrategy(new NoneShardingStrategyConfiguration());
        assertFalse(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsNotEmptyWithDefaultKeyGenerateStrategy() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.setDefaultKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
        assertFalse(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsNotEmptyWithDefaultAuditStrategy() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.setDefaultAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton("foo_auditor"), false));
        assertFalse(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsNotEmptyWithDefaultShardingColumn() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.setDefaultShardingColumn("user_id");
        assertFalse(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsNotEmptyWithShardingAlgorithms() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getShardingAlgorithms().put("foo_algorithm", null);
        assertFalse(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsNotEmptyWithKeyGenerators() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getKeyGenerators().put("foo_key_generator", null);
        assertFalse(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsNotEmptyWithAuditors() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getAuditors().put("foo_auditor", null);
        assertFalse(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsNotEmptyWithShardingCache() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.setShardingCache(new ShardingCacheConfiguration(1, new ShardingCacheOptionsConfiguration(false, 1, 1)));
        assertFalse(checker.isEmpty(ruleConfig));
    }
    
    @Test
    void assertIsEmpty() {
        assertTrue(checker.isEmpty(new ShardingRuleConfiguration()));
    }
}
