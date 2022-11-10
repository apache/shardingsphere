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

package org.apache.shardingsphere.sharding.yaml.swapper;

import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperFactory;
import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.audit.YamlShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public final class YamlShardingRuleAlgorithmProviderConfigurationSwapperTest {
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlShardingRuleConfiguration actual = getSwapper().swapToYamlConfiguration(createAlgorithmProvidedShardingRuleConfiguration());
        assertFalse(actual.getBindingTables().isEmpty());
        assertFalse(actual.getBroadcastTables().isEmpty());
        assertThat(actual.getDefaultShardingColumn(), is("foo_column"));
    }
    
    private AlgorithmProvidedShardingRuleConfiguration createAlgorithmProvidedShardingRuleConfiguration() {
        AlgorithmProvidedShardingRuleConfiguration result = new AlgorithmProvidedShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("foo_db"));
        result.getAutoTables().add(new ShardingAutoTableRuleConfiguration("foo_db", null));
        result.getBindingTableGroups().add("foo_bind_tb");
        result.setBroadcastTables(Collections.singleton("foo_broad_cast_tb"));
        result.setDefaultDatabaseShardingStrategy(mock(ShardingStrategyConfiguration.class));
        result.setDefaultTableShardingStrategy(mock(ShardingStrategyConfiguration.class));
        result.setDefaultKeyGenerateStrategy(mock(KeyGenerateStrategyConfiguration.class));
        result.setDefaultAuditStrategy(mock(ShardingAuditStrategyConfiguration.class));
        result.setDefaultShardingColumn("foo_column");
        return result;
    }
    
    @Test
    public void assertSwapToObject() {
        AlgorithmProvidedShardingRuleConfiguration actual = getSwapper().swapToObject(createYamlShardingRuleConfiguration());
        assertFalse(actual.getTables().isEmpty());
        assertFalse(actual.getAutoTables().isEmpty());
        assertFalse(actual.getBindingTableGroups().isEmpty());
        assertFalse(actual.getBroadcastTables().isEmpty());
        assertThat(actual.getDefaultShardingColumn(), is("foo_column"));
    }
    
    private YamlShardingRuleConfiguration createYamlShardingRuleConfiguration() {
        YamlShardingRuleConfiguration result = new YamlShardingRuleConfiguration();
        YamlTableRuleConfiguration yamlTableRuleConfig = new YamlTableRuleConfiguration();
        yamlTableRuleConfig.setLogicTable("foo_tbl");
        result.getTables().put("foo_key", yamlTableRuleConfig);
        YamlShardingAutoTableRuleConfiguration yamlShardingAutoTableRuleConfiguration = new YamlShardingAutoTableRuleConfiguration();
        yamlShardingAutoTableRuleConfiguration.setLogicTable("foo_auto_tbl");
        result.getAutoTables().put("foo_auto_key", yamlShardingAutoTableRuleConfiguration);
        result.setBindingTables(Collections.singleton("foo_btb"));
        result.setBroadcastTables(Collections.singleton("foo_ctb"));
        result.setDefaultDatabaseStrategy(mock(YamlShardingStrategyConfiguration.class));
        result.setDefaultTableStrategy(mock(YamlShardingStrategyConfiguration.class));
        YamlKeyGenerateStrategyConfiguration yamlKeyGenerateStrategyConfiguration = new YamlKeyGenerateStrategyConfiguration();
        yamlKeyGenerateStrategyConfiguration.setColumn("foo_column");
        result.setDefaultKeyGenerateStrategy(yamlKeyGenerateStrategyConfiguration);
        result.setDefaultAuditStrategy(mock(YamlShardingAuditStrategyConfiguration.class));
        result.setDefaultShardingColumn("foo_column");
        return result;
    }
    
    private YamlShardingRuleAlgorithmProviderConfigurationSwapper getSwapper() {
        AlgorithmProvidedShardingRuleConfiguration ruleConfig = mock(AlgorithmProvidedShardingRuleConfiguration.class);
        return (YamlShardingRuleAlgorithmProviderConfigurationSwapper) YamlRuleConfigurationSwapperFactory.getInstanceMapByRuleConfigurations(Collections.singleton(ruleConfig)).get(ruleConfig);
    }
}
