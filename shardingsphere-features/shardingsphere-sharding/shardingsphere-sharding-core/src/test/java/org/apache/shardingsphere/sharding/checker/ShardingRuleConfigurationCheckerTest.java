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

package org.apache.shardingsphere.sharding.checker;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.config.checker.RuleConfigurationCheckerFactory;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingRuleConfigurationCheckerTest {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void assertCheckPass() {
        ShardingRuleConfiguration configuration = createConfiguration();
        ShardingAuditStrategyConfiguration shardingAuditStrategyConfiguration
                = createShardingAuditStrategyConfiguration();
        ShardingStrategyConfiguration shardingStrategyConfiguration = createShardingStrategyConfiguration();
        configuration.setTables(Collections.singleton(
                createShardingTableRuleConfiguration(configuration.getDefaultKeyGenerateStrategy(), shardingAuditStrategyConfiguration, shardingStrategyConfiguration)));
        configuration.setAutoTables(Collections.singleton(
                createShardingAutoTableRuleConfiguration(configuration.getDefaultKeyGenerateStrategy(), shardingAuditStrategyConfiguration, shardingStrategyConfiguration)));

        RuleConfigurationChecker checker = getChecker(configuration);
        checker.check("foo_db", configuration);
    }

    @Test(expected = IllegalStateException.class)
    public void assertCheckTableConfigurationInitFail() {
        ShardingRuleConfiguration configuration = createConfiguration();
        getChecker(configuration).check("foo_db", configuration);
    }

    @Test(expected = IllegalStateException.class)
    public void assertCheckTableConfigurationFail() {
        ShardingRuleConfiguration configuration = createConfiguration();
        configuration.setTables(Collections.singletonList(createShardingTableRuleConfiguration(null, null, null)));
        configuration.setAutoTables(Collections.singleton(createShardingAutoTableRuleConfiguration(null, null, null)));
        getChecker(configuration).check("foo_db", configuration);
    }

    private ShardingRuleConfiguration createConfiguration() {
        ShardingRuleConfiguration configuration = new ShardingRuleConfiguration();

        KeyGenerateStrategyConfiguration keyGenerateStrategyConfiguration = new KeyGenerateStrategyConfiguration("foo_column",
                "foo_key");
        configuration.setDefaultKeyGenerateStrategy(keyGenerateStrategyConfiguration);
        configuration.getKeyGenerators().put("foo_key", mock(ShardingSphereAlgorithmConfiguration.class));
        configuration.getAuditors().put("foo_audit", mock(ShardingSphereAlgorithmConfiguration.class));
        configuration.getShardingAlgorithms().put("foo_algorithm", mock(ShardingSphereAlgorithmConfiguration.class));
        return configuration;
    }

    private ShardingAuditStrategyConfiguration createShardingAuditStrategyConfiguration() {
        ShardingAuditStrategyConfiguration shardingAuditStrategyConfiguration = new ShardingAuditStrategyConfiguration(Collections.singletonList("foo_audit"), false);
        return shardingAuditStrategyConfiguration;
    }

    private ShardingStrategyConfiguration createShardingStrategyConfiguration() {
        ShardingStrategyConfiguration shardingStrategyConfiguration = mock(ShardingStrategyConfiguration.class);
        when(shardingStrategyConfiguration.getShardingAlgorithmName()).thenReturn("foo_algorithm");
        return shardingStrategyConfiguration;
    }

    private ShardingTableRuleConfiguration createShardingTableRuleConfiguration(final KeyGenerateStrategyConfiguration keyGenerateStrategyConfiguration,
                                                                                final ShardingAuditStrategyConfiguration shardingAuditStrategyConfiguration,
                                                                                final ShardingStrategyConfiguration shardingStrategyConfiguration) {
        ShardingTableRuleConfiguration shardingTableRuleConfiguration = new ShardingTableRuleConfiguration("foo_ltb");
        shardingTableRuleConfiguration.setKeyGenerateStrategy(keyGenerateStrategyConfiguration == null ? mock(KeyGenerateStrategyConfiguration.class) : keyGenerateStrategyConfiguration);
        shardingTableRuleConfiguration.setAuditStrategy(shardingAuditStrategyConfiguration == null ? mock(ShardingAuditStrategyConfiguration.class) : shardingAuditStrategyConfiguration);
        shardingTableRuleConfiguration.setDatabaseShardingStrategy(shardingStrategyConfiguration == null ? mock(ShardingStrategyConfiguration.class) : shardingStrategyConfiguration);
        shardingTableRuleConfiguration.setTableShardingStrategy(shardingStrategyConfiguration == null ? mock(ShardingStrategyConfiguration.class) : shardingStrategyConfiguration);
        return shardingTableRuleConfiguration;
    }

    private ShardingAutoTableRuleConfiguration createShardingAutoTableRuleConfiguration(final KeyGenerateStrategyConfiguration keyGenerateStrategyConfiguration,
                                                                                        final ShardingAuditStrategyConfiguration shardingAuditStrategyConfiguration,
                                                                                        final ShardingStrategyConfiguration shardingStrategyConfiguration) {
        ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfiguration = mock(ShardingAutoTableRuleConfiguration.class);
        when(shardingAutoTableRuleConfiguration.getKeyGenerateStrategy())
                .thenReturn(keyGenerateStrategyConfiguration == null ? mock(KeyGenerateStrategyConfiguration.class) : keyGenerateStrategyConfiguration);
        when(shardingAutoTableRuleConfiguration.getAuditStrategy())
                .thenReturn(shardingAuditStrategyConfiguration == null ? mock(ShardingAuditStrategyConfiguration.class) : shardingAuditStrategyConfiguration);
        when(shardingAutoTableRuleConfiguration.getShardingStrategy())
                .thenReturn(shardingStrategyConfiguration == null ? mock(ShardingStrategyConfiguration.class) : shardingStrategyConfiguration);
        return shardingAutoTableRuleConfiguration;
    }

    private RuleConfigurationChecker getChecker(final ShardingRuleConfiguration configuration) {
        Optional<RuleConfigurationChecker> checkerOptional = RuleConfigurationCheckerFactory.findInstance(configuration);
        return checkerOptional.get();
    }
}
