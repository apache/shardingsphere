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

import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperFactory;
import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ShardingRuleAlgorithmProviderConfigurationYamlSwapperTest {
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlShardingRuleConfiguration actual = getSwapper().swapToYamlConfiguration(createAlgorithmProvidedShardingRuleConfiguration());
        assertThat(actual.getDefaultShardingColumn(), is("foo_column"));
        assertThat(actual.getScalingName(), is("foo_scale_name"));
    }
    
    private AlgorithmProvidedShardingRuleConfiguration createAlgorithmProvidedShardingRuleConfiguration() {
        AlgorithmProvidedShardingRuleConfiguration result = new AlgorithmProvidedShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("foo_db"));
        result.getAutoTables().add(new ShardingAutoTableRuleConfiguration("foo_db"));
        result.getBindingTableGroups().add("foo_bind_tb");
        result.setDefaultShardingColumn("foo_column");
        result.setScalingName("foo_scale_name");
        return result;
    }
    
    @Test
    public void assertSwapToObject() {
        assertThat(getSwapper().swapToObject(createYamlShardingRuleConfiguration()).getDefaultShardingColumn(), is("foo_column"));
    }
    
    private YamlShardingRuleConfiguration createYamlShardingRuleConfiguration() {
        YamlShardingRuleConfiguration result = new YamlShardingRuleConfiguration();
        YamlTableRuleConfiguration yamlTableRuleConfig = new YamlTableRuleConfiguration();
        yamlTableRuleConfig.setLogicTable("foo_tbl");
        result.getTables().put("foo_key", yamlTableRuleConfig);
        result.setDefaultShardingColumn("foo_column");
        return result;
    }
    
    private ShardingRuleAlgorithmProviderConfigurationYamlSwapper getSwapper() {
        AlgorithmProvidedShardingRuleConfiguration ruleConfig = mock(AlgorithmProvidedShardingRuleConfiguration.class);
        return (ShardingRuleAlgorithmProviderConfigurationYamlSwapper) YamlRuleConfigurationSwapperFactory.getInstanceMapByRuleConfigurations(Collections.singleton(ruleConfig)).get(ruleConfig);
    }
}
