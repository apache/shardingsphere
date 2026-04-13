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
import org.apache.shardingsphere.infra.config.keygen.impl.ColumnKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.infra.config.keygen.KeyGenerateStrategiesConfiguration;
import org.apache.shardingsphere.infra.config.keygen.impl.SequenceKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.exception.metadata.MissingRequiredShardingConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        ruleConfig.setTables(Collections.singleton(createShardingTableRuleConfiguration(shardingStrategyConfig, shardingAuditStrategyConfig)));
        ruleConfig.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(shardingStrategyConfig, shardingAuditStrategyConfig)));
        ruleConfig.getKeyGenerateStrategies().put("foo_column_strategy", createColumnKeyGenerateStrategyRuleConfiguration("foo_keygen"));
        ruleConfig.getKeyGenerateStrategies().put("foo_sequence_strategy", createSequenceKeyGenerateStrategyRuleConfiguration("foo_keygen"));
        checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList());
    }
    
    @Test
    void assertCheckKeyGenerateStrategiesFailed() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ruleConfig.getKeyGenerateStrategies().put("foo_sequence_strategy", createSequenceKeyGenerateStrategyRuleConfiguration("bar_keygen"));
        assertThrows(UnregisteredAlgorithmException.class, () -> checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
    }
    
    @Test
    void assertCheckTableConfigurationFailed() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ruleConfig.setTables(Collections.singleton(createShardingTableRuleConfiguration(null, null)));
        ruleConfig.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(null, null)));
        assertThrows(MissingRequiredShardingConfigurationException.class, () -> checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
    }
    
    @Test
    void assertCheckKeyGenerateStrategiesWithUnregisteredKeyGeneratorFailed() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ruleConfig.getKeyGenerateStrategies().put("bar_tbl_foo_col", new ColumnKeyGenerateStrategiesRuleConfiguration("bar_keygen", "bar_tbl", "foo_col"));
        assertThrows(UnregisteredAlgorithmException.class, () -> checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
    }
    
    @Test
    void assertCheckAuditStrategyFailed() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ShardingStrategyConfiguration shardingStrategyConfig = createShardingStrategyConfiguration();
        ruleConfig.setTables(Collections.singleton(createShardingTableRuleConfiguration(shardingStrategyConfig, null)));
        ruleConfig.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(shardingStrategyConfig,
                new ShardingAuditStrategyConfiguration(Collections.singleton("bar_audit"), false))));
        assertThrows(UnregisteredAlgorithmException.class, () -> checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
    }
    
    @Test
    void assertCheckShardingStrategyFailedWithComplexShardingStrategy() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ShardingAuditStrategyConfiguration shardingAuditStrategyConfig = new ShardingAuditStrategyConfiguration(Collections.singleton("foo_audit"), false);
        ruleConfig.setTables(Collections.singleton(
                createShardingTableRuleConfiguration(new NoneShardingStrategyConfiguration(), shardingAuditStrategyConfig)));
        ruleConfig.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(
                new ComplexShardingStrategyConfiguration("", "foo_algorithm"), shardingAuditStrategyConfig)));
        assertThrows(MissingRequiredShardingConfigurationException.class, () -> checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
    }
    
    @Test
    void assertCheckShardingStrategyFailedWithUnregisteredAlgorithm() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ShardingAuditStrategyConfiguration shardingAuditStrategyConfig = new ShardingAuditStrategyConfiguration(Collections.singleton("foo_audit"), false);
        ruleConfig.setTables(Collections.singleton(
                createShardingTableRuleConfiguration(new NoneShardingStrategyConfiguration(), shardingAuditStrategyConfig)));
        ruleConfig.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(
                new StandardShardingStrategyConfiguration("foo_col", "bar_algorithm"), shardingAuditStrategyConfig)));
        assertThrows(UnregisteredAlgorithmException.class, () -> checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
    }
    
    @Test
    void assertGetRequiredDataSourceNames() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ShardingAuditStrategyConfiguration shardingAuditStrategyConfig = new ShardingAuditStrategyConfiguration(Collections.singleton("foo_audit"), false);
        ShardingStrategyConfiguration shardingStrategyConfig = createShardingStrategyConfiguration();
        ruleConfig.setTables(Collections.singleton(createShardingTableRuleConfiguration(shardingStrategyConfig, shardingAuditStrategyConfig)));
        ruleConfig.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(shardingStrategyConfig, shardingAuditStrategyConfig)));
        assertThat(checker.getRequiredDataSourceNames(ruleConfig), is(new LinkedHashSet<>(Arrays.asList("ds_0", "ds_1"))));
    }
    
    @Test
    void assertGetTableNames() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ShardingAuditStrategyConfiguration shardingAuditStrategyConfig = new ShardingAuditStrategyConfiguration(Collections.singleton("foo_audit"), false);
        ShardingStrategyConfiguration shardingStrategyConfig = createShardingStrategyConfiguration();
        ruleConfig.setTables(Collections.singleton(createShardingTableRuleConfiguration(shardingStrategyConfig, shardingAuditStrategyConfig)));
        ruleConfig.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(shardingStrategyConfig, shardingAuditStrategyConfig)));
        assertThat(checker.getTableNames(ruleConfig), is(Arrays.asList("foo_tbl", "bar_tbl")));
    }
    
    private ShardingRuleConfiguration createRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getShardingAlgorithms().put("foo_algorithm", new AlgorithmConfiguration("MOD", PropertiesBuilder.build(new Property("sharding-count", "1"))));
        result.getAuditors().put("foo_audit", new AlgorithmConfiguration("foo", new Properties()));
        result.getKeyGenerators().put("foo_keygen", new AlgorithmConfiguration("UUID", new Properties()));
        return result;
    }
    
    private ShardingStrategyConfiguration createShardingStrategyConfiguration() {
        return new StandardShardingStrategyConfiguration("foo_col", "foo_algorithm");
    }
    
    private ShardingTableRuleConfiguration createShardingTableRuleConfiguration(final ShardingStrategyConfiguration shardingStrategyConfig,
                                                                                final ShardingAuditStrategyConfiguration shardingAuditStrategyConfig) {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("foo_tbl", "ds_0.foo_tbl");
        result.setDatabaseShardingStrategy(null == shardingStrategyConfig ? createInvalidShardingStrategyConfiguration() : shardingStrategyConfig);
        result.setTableShardingStrategy(null == shardingStrategyConfig ? createInvalidShardingStrategyConfiguration() : shardingStrategyConfig);
        result.setAuditStrategy(shardingAuditStrategyConfig);
        return result;
    }
    
    private ShardingAutoTableRuleConfiguration createShardingAutoTableRuleConfiguration(final ShardingStrategyConfiguration shardingStrategyConfig,
                                                                                        final ShardingAuditStrategyConfiguration shardingAuditStrategyConfig) {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration("bar_tbl", "ds_1");
        result.setShardingStrategy(null == shardingStrategyConfig ? createInvalidShardingStrategyConfiguration() : shardingStrategyConfig);
        result.setAuditStrategy(shardingAuditStrategyConfig);
        return result;
    }
    
    private ShardingStrategyConfiguration createInvalidShardingStrategyConfiguration() {
        return new StandardShardingStrategyConfiguration("foo_col", null);
    }
    
    private KeyGenerateStrategiesConfiguration createColumnKeyGenerateStrategyRuleConfiguration(final String keyGeneratorName) {
        return new ColumnKeyGenerateStrategiesRuleConfiguration(keyGeneratorName, "foo_tbl", "foo_col");
    }
    
    private KeyGenerateStrategiesConfiguration createSequenceKeyGenerateStrategyRuleConfiguration(final String keyGeneratorName) {
        return new SequenceKeyGenerateStrategiesRuleConfiguration(keyGeneratorName, "foo_sequence");
    }
}
