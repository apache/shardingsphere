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

package org.apache.shardingsphere.shadow.yaml.swapper;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.datasource.YamlShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlShadowRuleConfigurationSwapperTest {
    
    private final YamlShadowRuleConfigurationSwapper swapper = new YamlShadowRuleConfigurationSwapper();
    
    @Test
    void assertSwapToYamlConfigurationWithEmptyTableLists() {
        ShadowRuleConfiguration ruleConfig = createRuleConfiguration();
        YamlShadowRuleConfiguration actual = swapper.swapToYamlConfiguration(ruleConfig);
        assertTrue(actual.getTables().get("foo_tbl").getDataSourceNames().isEmpty());
        assertTrue(actual.getTables().get("foo_tbl").getShadowAlgorithmNames().isEmpty());
        assertTrue(ruleConfig.getTables().get("foo_tbl").getDataSourceNames().isEmpty());
        assertTrue(ruleConfig.getTables().get("foo_tbl").getShadowAlgorithmNames().isEmpty());
    }
    
    private ShadowRuleConfiguration createRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.getDataSources().add(new ShadowDataSourceConfiguration("foo_ds", "prod_ds", "shadow_ds"));
        result.getTables().put("foo_tbl", new ShadowTableConfiguration(new LinkedList<>(), new LinkedList<>()));
        result.getShadowAlgorithms().put("foo_algo", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        result.setDefaultShadowAlgorithmName("foo_algo");
        return result;
    }
    
    @Test
    void assertSwapToYamlConfigurationRepeatedly() {
        ShadowRuleConfiguration ruleConfig = createRuleConfiguration();
        YamlShadowRuleConfiguration actualFirst = swapper.swapToYamlConfiguration(ruleConfig);
        YamlShadowRuleConfiguration actualSecond = swapper.swapToYamlConfiguration(ruleConfig);
        assertThat(actualSecond, deepEqual(actualFirst));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithExplicitTableConfiguration() {
        ShadowRuleConfiguration ruleConfig = createRuleConfiguration();
        ruleConfig.getTables().put("foo_tbl", new ShadowTableConfiguration(Collections.singletonList("foo_ds"), Collections.singletonList("bar_algo")));
        ruleConfig.getShadowAlgorithms().put("bar_algo", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        YamlShadowRuleConfiguration actual = swapper.swapToYamlConfiguration(ruleConfig);
        assertThat(actual.getDataSources().get("foo_ds").getProductionDataSourceName(), is("prod_ds"));
        assertThat(actual.getDataSources().get("foo_ds").getShadowDataSourceName(), is("shadow_ds"));
        assertThat(actual.getTables().get("foo_tbl").getDataSourceNames(), is(Collections.singletonList("foo_ds")));
        assertThat(actual.getTables().get("foo_tbl").getShadowAlgorithmNames(), is(Collections.singletonList("bar_algo")));
        assertThat(actual.getDefaultShadowAlgorithmName(), is("foo_algo"));
        assertThat(actual.getShadowAlgorithms().get("foo_algo").getType(), is("SQL_HINT"));
        assertThat(actual.getShadowAlgorithms().get("bar_algo").getType(), is("SQL_HINT"));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithRuntimeRule() {
        ShadowRule rule = new ShadowRule(createRuleConfiguration());
        assertTrue(rule.getShadowDataSourceMappings("foo_tbl").isEmpty());
        swapper.swapToYamlConfiguration(rule.getConfiguration());
        assertTrue(rule.getShadowDataSourceMappings("foo_tbl").isEmpty());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("swapToObjectArguments")
    void assertSwapToObject(final String name, final YamlShadowRuleConfiguration yamlConfig, final Collection<String> expectedShadowAlgorithmNames) {
        ShadowRuleConfiguration expected = new ShadowRuleConfiguration();
        expected.setDefaultShadowAlgorithmName(yamlConfig.getDefaultShadowAlgorithmName());
        expected.getTables().put("foo_tbl", new ShadowTableConfiguration(Collections.emptyList(), expectedShadowAlgorithmNames));
        ShadowRuleConfiguration actual = swapper.swapToObject(yamlConfig);
        assertThat(name, actual, deepEqual(expected));
    }
    
    private static Collection<Arguments> swapToObjectArguments() {
        YamlShadowRuleConfiguration withDefaultAlgorithm = createYamlRuleConfiguration(Collections.emptyList());
        withDefaultAlgorithm.setDefaultShadowAlgorithmName("foo_algo");
        YamlShadowRuleConfiguration withTableAlgorithm = createYamlRuleConfiguration(Collections.singletonList("bar_algo"));
        withTableAlgorithm.setDefaultShadowAlgorithmName("foo_algo");
        return Arrays.asList(
                Arguments.of("default algorithm", withDefaultAlgorithm, Collections.singletonList("foo_algo")),
                Arguments.of("no default algorithm", createYamlRuleConfiguration(Collections.emptyList()), Collections.emptyList()),
                Arguments.of("explicit table algorithm", withTableAlgorithm, Collections.singletonList("bar_algo")));
    }
    
    private static YamlShadowRuleConfiguration createYamlRuleConfiguration(final Collection<String> shadowAlgorithmNames) {
        YamlShadowTableConfiguration yamlTableConfig = new YamlShadowTableConfiguration();
        yamlTableConfig.setShadowAlgorithmNames(new LinkedList<>(shadowAlgorithmNames));
        YamlShadowRuleConfiguration result = new YamlShadowRuleConfiguration();
        result.getTables().put("foo_tbl", yamlTableConfig);
        return result;
    }
    
    @Test
    void assertSwapToObjectWithSingleDataSource() {
        YamlShadowRuleConfiguration yamlConfig = createYamlRuleConfiguration(Collections.emptyList());
        YamlShadowDataSourceConfiguration yamlDataSourceConfig = new YamlShadowDataSourceConfiguration();
        yamlDataSourceConfig.setProductionDataSourceName("prod_ds");
        yamlDataSourceConfig.setShadowDataSourceName("shadow_ds");
        yamlConfig.getDataSources().put("foo_ds", yamlDataSourceConfig);
        ShadowRuleConfiguration actual = swapper.swapToObject(yamlConfig);
        assertThat(actual.getTables().get("foo_tbl").getDataSourceNames(), is(Collections.singletonList("foo_ds")));
        assertThat(actual.getDataSources(), deepEqual(Collections.singletonList(new ShadowDataSourceConfiguration("foo_ds", "prod_ds", "shadow_ds"))));
    }
    
    @Test
    void assertSwapToDataSource() {
        YamlShadowDataSourceConfiguration yamlConfig = new YamlShadowDataSourceConfiguration();
        yamlConfig.setProductionDataSourceName("prod_ds");
        yamlConfig.setShadowDataSourceName("shadow_ds");
        ShadowDataSourceConfiguration actual = swapper.swapToDataSource("foo_ds", yamlConfig);
        assertThat(actual, deepEqual(new ShadowDataSourceConfiguration("foo_ds", "prod_ds", "shadow_ds")));
    }
}
