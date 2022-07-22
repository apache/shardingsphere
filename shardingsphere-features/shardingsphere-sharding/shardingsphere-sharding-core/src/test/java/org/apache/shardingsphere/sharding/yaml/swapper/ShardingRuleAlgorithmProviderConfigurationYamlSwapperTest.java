
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
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ShardingRuleAlgorithmProviderConfigurationYamlSwapperTest {

    private AlgorithmProvidedShardingRuleConfiguration ruleConfig;

    @Before
    public void setUp() {
        ruleConfig = mock(AlgorithmProvidedShardingRuleConfiguration.class);
    }

    @Test
    public void assertSwapToYamlConfiguration() {
        YamlShardingRuleConfiguration actualResult = getSwapper().swapToYamlConfiguration(createAlgorithmProvidedShardingRuleConfiguration());
        assertNotNull(actualResult);
        assertThat(actualResult.getDefaultShardingColumn(), equalTo("foo_column"));
        assertThat(actualResult.getScalingName(), equalTo("foo_scale_name"));
    }

    @Test
    public void assertSwapToObject() {
        AlgorithmProvidedShardingRuleConfiguration algorithmProvidedShardingRuleConfiguration = getSwapper().swapToObject(createYamlShardingRuleConfiguration());
        assertThat(algorithmProvidedShardingRuleConfiguration.getDefaultShardingColumn(), equalTo("foo_column"));
    }

    @Test
    public void assertGetRuleTagName() {
        assertThat(getSwapper().getRuleTagName(), equalTo("SHARDING"));
    }

    private ShardingRuleAlgorithmProviderConfigurationYamlSwapper getSwapper() {
        return (ShardingRuleAlgorithmProviderConfigurationYamlSwapper) YamlRuleConfigurationSwapperFactory.getInstanceMapByRuleConfigurations(Collections.singletonList(ruleConfig)).get(ruleConfig);
    }

    private AlgorithmProvidedShardingRuleConfiguration createAlgorithmProvidedShardingRuleConfiguration() {
        AlgorithmProvidedShardingRuleConfiguration algorithmProvidedShardingRuleConfiguration = new AlgorithmProvidedShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfiguration = new ShardingTableRuleConfiguration("foo_db");
        algorithmProvidedShardingRuleConfiguration.setTables(Collections.singletonList(shardingTableRuleConfiguration));
        ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfiguration = new ShardingAutoTableRuleConfiguration("foo_db");
        algorithmProvidedShardingRuleConfiguration.setAutoTables(Collections.singletonList(shardingAutoTableRuleConfiguration));
        algorithmProvidedShardingRuleConfiguration.setBindingTableGroups(Collections.singletonList("foo_bind_tb"));
        algorithmProvidedShardingRuleConfiguration.setDefaultShardingColumn("foo_column");
        algorithmProvidedShardingRuleConfiguration.setScalingName("foo_scale_name");
        return algorithmProvidedShardingRuleConfiguration;
    }

    private YamlShardingRuleConfiguration createYamlShardingRuleConfiguration() {
        YamlShardingRuleConfiguration yamlShardingRuleConfiguration = new YamlShardingRuleConfiguration();
        Map<String, YamlTableRuleConfiguration> yamlTableRuleConfigurationMap = new HashMap<>();
        YamlTableRuleConfiguration yamlTableRuleConfiguration = new YamlTableRuleConfiguration();
        yamlTableRuleConfiguration.setLogicTable("foo_tbl");
        yamlTableRuleConfigurationMap.put("foo_key", yamlTableRuleConfiguration);
        yamlShardingRuleConfiguration.setTables(yamlTableRuleConfigurationMap);
        yamlShardingRuleConfiguration.setDefaultShardingColumn("foo_column");
        return yamlShardingRuleConfiguration;
    }
}
