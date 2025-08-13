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

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationChecker;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.exception.metadata.MissingRequiredShardingConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingRuleConfigurationCheckerTest {
    
    private ShardingRuleConfigurationChecker checker;
    
    @BeforeEach
    void setUp() {
        checker = (ShardingRuleConfigurationChecker) OrderedSPILoader.getServicesByClass(
                DatabaseRuleConfigurationChecker.class, Collections.singleton(ShardingRuleConfiguration.class)).get(ShardingRuleConfiguration.class);
    }
    
    @Test
    void assertCheckSuccess() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ShardingAuditStrategyConfiguration shardingAuditStrategyConfig = new ShardingAuditStrategyConfiguration(Collections.singleton("foo_audit"), false);
        ShardingStrategyConfiguration shardingStrategyConfig = createShardingStrategyConfiguration();
        ruleConfig.setTables(Collections.singleton(createShardingTableRuleConfiguration(shardingStrategyConfig, shardingAuditStrategyConfig, ruleConfig.getDefaultKeyGenerateStrategy())));
        ruleConfig.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(shardingStrategyConfig, shardingAuditStrategyConfig, ruleConfig.getDefaultKeyGenerateStrategy())));
        checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList());
    }
    
    @Test
    void assertCheckTableConfigurationFailed() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ruleConfig.setTables(Collections.singleton(createShardingTableRuleConfiguration(null, null, null)));
        ruleConfig.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(null, null, null)));
        assertThrows(MissingRequiredShardingConfigurationException.class, () -> checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
    }
    
    @Test
    void assertCheckKeyGenerateStrategyFailed() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ShardingAuditStrategyConfiguration shardingAuditStrategyConfig = new ShardingAuditStrategyConfiguration(Collections.singleton("foo_audit"), false);
        ShardingStrategyConfiguration shardingStrategyConfig = createShardingStrategyConfiguration();
        ruleConfig.setTables(Collections.singleton(createShardingTableRuleConfiguration(shardingStrategyConfig, shardingAuditStrategyConfig, ruleConfig.getDefaultKeyGenerateStrategy())));
        ruleConfig.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(null, null,
                new KeyGenerateStrategyConfiguration("foo_col", "bar_keygen"))));
        assertThrows(UnregisteredAlgorithmException.class, () -> checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
    }
    
    @Test
    void assertCheckAuditStrategyFailed() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ShardingStrategyConfiguration shardingStrategyConfig = createShardingStrategyConfiguration();
        ruleConfig.setTables(Collections.singleton(createShardingTableRuleConfiguration(shardingStrategyConfig, null, ruleConfig.getDefaultKeyGenerateStrategy())));
        ruleConfig.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(shardingStrategyConfig,
                new ShardingAuditStrategyConfiguration(Collections.singleton("bar_audit"), false), ruleConfig.getDefaultKeyGenerateStrategy())));
        assertThrows(UnregisteredAlgorithmException.class, () -> checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
    }
    
    @Test
    void assertCheckShardingStrategyFailedWithComplexShardingStrategy() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ShardingAuditStrategyConfiguration shardingAuditStrategyConfig = new ShardingAuditStrategyConfiguration(Collections.singleton("foo_audit"), false);
        ruleConfig.setTables(Collections.singleton(
                createShardingTableRuleConfiguration(new NoneShardingStrategyConfiguration(), shardingAuditStrategyConfig, ruleConfig.getDefaultKeyGenerateStrategy())));
        ruleConfig.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(
                new ComplexShardingStrategyConfiguration("", "foo_algorithm"), shardingAuditStrategyConfig, ruleConfig.getDefaultKeyGenerateStrategy())));
        assertThrows(MissingRequiredShardingConfigurationException.class, () -> checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
    }
    
    @Test
    void assertCheckShardingStrategyFailedWithUnregisteredAlgorithm() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ShardingAuditStrategyConfiguration shardingAuditStrategyConfig = new ShardingAuditStrategyConfiguration(Collections.singleton("foo_audit"), false);
        ruleConfig.setTables(Collections.singleton(
                createShardingTableRuleConfiguration(new NoneShardingStrategyConfiguration(), shardingAuditStrategyConfig, ruleConfig.getDefaultKeyGenerateStrategy())));
        ruleConfig.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(
                new StandardShardingStrategyConfiguration("foo_col", "bar_algorithm"), shardingAuditStrategyConfig, ruleConfig.getDefaultKeyGenerateStrategy())));
        assertThrows(UnregisteredAlgorithmException.class, () -> checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
    }
    
    @Test
    void assertGetRequiredDataSourceNames() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ShardingAuditStrategyConfiguration shardingAuditStrategyConfig = new ShardingAuditStrategyConfiguration(Collections.singleton("foo_audit"), false);
        ShardingStrategyConfiguration shardingStrategyConfig = createShardingStrategyConfiguration();
        ruleConfig.setTables(Collections.singleton(createShardingTableRuleConfiguration(shardingStrategyConfig, shardingAuditStrategyConfig, ruleConfig.getDefaultKeyGenerateStrategy())));
        ruleConfig.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(shardingStrategyConfig, shardingAuditStrategyConfig, ruleConfig.getDefaultKeyGenerateStrategy())));
        assertTrue(checker.getRequiredDataSourceNames(ruleConfig).isEmpty());
    }
    
    @Test
    void assertGetTableNames() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ShardingAuditStrategyConfiguration shardingAuditStrategyConfig = new ShardingAuditStrategyConfiguration(Collections.singleton("foo_audit"), false);
        ShardingStrategyConfiguration shardingStrategyConfig = createShardingStrategyConfiguration();
        ruleConfig.setTables(Collections.singleton(createShardingTableRuleConfiguration(shardingStrategyConfig, shardingAuditStrategyConfig, ruleConfig.getDefaultKeyGenerateStrategy())));
        ruleConfig.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(shardingStrategyConfig, shardingAuditStrategyConfig, ruleConfig.getDefaultKeyGenerateStrategy())));
        assertThat(checker.getTableNames(ruleConfig), is(Arrays.asList("foo_tbl", "bar_tbl")));
    }
    
    private ShardingRuleConfiguration createRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getShardingAlgorithms().put("foo_algorithm", new AlgorithmConfiguration("MOD", PropertiesBuilder.build(new Property("sharding-count", "1"))));
        result.getAuditors().put("foo_audit", new AlgorithmConfiguration("foo", new Properties()));
        result.getKeyGenerators().put("foo_keygen", new AlgorithmConfiguration("UUID", new Properties()));
        result.setDefaultKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("foo_col", "foo_keygen"));
        return result;
    }
    
    private ShardingStrategyConfiguration createShardingStrategyConfiguration() {
        ShardingStrategyConfiguration result = mock(ShardingStrategyConfiguration.class);
        when(result.getShardingAlgorithmName()).thenReturn("foo_algorithm");
        return result;
    }
    
    private ShardingTableRuleConfiguration createShardingTableRuleConfiguration(final ShardingStrategyConfiguration shardingStrategyConfig,
                                                                                final ShardingAuditStrategyConfiguration shardingAuditStrategyConfig,
                                                                                final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig) {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("foo_tbl", null);
        result.setDatabaseShardingStrategy(null == shardingStrategyConfig ? mock(ShardingStrategyConfiguration.class) : shardingStrategyConfig);
        result.setTableShardingStrategy(null == shardingStrategyConfig ? mock(ShardingStrategyConfiguration.class) : shardingStrategyConfig);
        result.setAuditStrategy(shardingAuditStrategyConfig);
        result.setKeyGenerateStrategy(keyGenerateStrategyConfig);
        return result;
    }
    
    private ShardingAutoTableRuleConfiguration createShardingAutoTableRuleConfiguration(final ShardingStrategyConfiguration shardingStrategyConfig,
                                                                                        final ShardingAuditStrategyConfiguration shardingAuditStrategyConfig,
                                                                                        final KeyGenerateStrategyConfiguration keyGenerateStrategyConfig) {
        ShardingAutoTableRuleConfiguration result = mock(ShardingAutoTableRuleConfiguration.class);
        when(result.getLogicTable()).thenReturn("bar_tbl");
        when(result.getShardingStrategy()).thenReturn(null == shardingStrategyConfig ? mock(ShardingStrategyConfiguration.class) : shardingStrategyConfig);
        when(result.getAuditStrategy()).thenReturn(shardingAuditStrategyConfig);
        when(result.getKeyGenerateStrategy()).thenReturn(keyGenerateStrategyConfig);
        return result;
    }
}
