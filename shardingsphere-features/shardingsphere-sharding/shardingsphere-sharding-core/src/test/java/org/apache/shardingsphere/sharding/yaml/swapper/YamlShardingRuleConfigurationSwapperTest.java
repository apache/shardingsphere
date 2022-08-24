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

import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.audit.YamlShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlStandardShardingStrategyConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class YamlShardingRuleConfigurationSwapperTest {
    
    private final YamlShardingRuleConfigurationSwapper swapper = new YamlShardingRuleConfigurationSwapper();
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlShardingRuleConfiguration actual = swapper.swapToYamlConfiguration(createShardingRuleConfiguration());
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getBindingTables().size(), is(1));
        assertThat(actual.getBindingTables().iterator().next(), is("tbl, sub_tbl"));
        assertThat(actual.getBroadcastTables().size(), is(1));
        assertThat(actual.getBroadcastTables().iterator().next(), is("dict"));
        assertNotNull(actual.getDefaultDatabaseStrategy());
        assertNotNull(actual.getDefaultTableStrategy());
        assertNotNull(actual.getDefaultKeyGenerateStrategy());
        assertNotNull(actual.getDefaultAuditStrategy());
        assertThat(actual.getDefaultShardingColumn(), is("user_id"));
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(mock(ShardingTableRuleConfiguration.class));
        result.getBindingTableGroups().add("tbl, sub_tbl");
        result.getBroadcastTables().add("dict");
        result.setDefaultDatabaseShardingStrategy(mock(ShardingStrategyConfiguration.class));
        result.setDefaultTableShardingStrategy(mock(ShardingStrategyConfiguration.class));
        result.setDefaultTableShardingStrategy(mock(ShardingStrategyConfiguration.class));
        result.setDefaultKeyGenerateStrategy(mock(KeyGenerateStrategyConfiguration.class));
        result.setDefaultAuditStrategy(mock(ShardingAuditStrategyConfiguration.class));
        result.setDefaultShardingColumn("user_id");
        return result;
    }
    
    @Test
    public void assertSwapToObject() {
        ShardingRuleConfiguration actual = swapper.swapToObject(createYamlShardingRuleConfiguration());
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getBindingTableGroups().size(), is(1));
        assertThat(actual.getBindingTableGroups().iterator().next(), is("tbl, sub_tbl"));
        assertThat(actual.getBroadcastTables().size(), is(1));
        assertThat(actual.getBroadcastTables().iterator().next(), is("dict"));
        assertNotNull(actual.getDefaultDatabaseShardingStrategy());
        assertNotNull(actual.getDefaultTableShardingStrategy());
        assertNotNull(actual.getDefaultKeyGenerateStrategy());
        assertNotNull(actual.getDefaultAuditStrategy());
        assertThat(actual.getDefaultShardingColumn(), is("user_id"));
    }
    
    private YamlShardingRuleConfiguration createYamlShardingRuleConfiguration() {
        YamlShardingRuleConfiguration result = new YamlShardingRuleConfiguration();
        YamlTableRuleConfiguration yamlTableRuleConfig = new YamlTableRuleConfiguration();
        yamlTableRuleConfig.setLogicTable("tbl");
        result.getTables().put("tbl", yamlTableRuleConfig);
        result.getBindingTables().add("tbl, sub_tbl");
        result.getBroadcastTables().add("dict");
        YamlShardingStrategyConfiguration yamlShardingStrategyConfig = new YamlShardingStrategyConfiguration();
        YamlStandardShardingStrategyConfiguration yamlStandardShardingStrategyConfig = new YamlStandardShardingStrategyConfiguration();
        yamlStandardShardingStrategyConfig.setShardingAlgorithmName("foo_sharding_algo");
        yamlShardingStrategyConfig.setStandard(yamlStandardShardingStrategyConfig);
        result.setDefaultDatabaseStrategy(yamlShardingStrategyConfig);
        result.setDefaultTableStrategy(yamlShardingStrategyConfig);
        YamlKeyGenerateStrategyConfiguration yamlKeyGenerateStrategyConfig = new YamlKeyGenerateStrategyConfiguration();
        yamlKeyGenerateStrategyConfig.setColumn("col");
        result.setDefaultKeyGenerateStrategy(yamlKeyGenerateStrategyConfig);
        result.setDefaultAuditStrategy(mock(YamlShardingAuditStrategyConfiguration.class));
        result.setDefaultShardingColumn("user_id");
        return result;
    }
}
