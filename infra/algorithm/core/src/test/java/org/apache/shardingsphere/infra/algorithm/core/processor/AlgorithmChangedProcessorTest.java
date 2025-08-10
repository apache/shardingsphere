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

package org.apache.shardingsphere.infra.algorithm.core.processor;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.processor.fixture.AlgorithmChangedProcessorFixtureRule;
import org.apache.shardingsphere.infra.algorithm.core.processor.fixture.AlgorithmChangedProcessorFixtureRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlgorithmChangedProcessorTest {
    
    @SuppressWarnings("unchecked")
    private final RuleItemConfigurationChangedProcessor<AlgorithmChangedProcessorFixtureRuleConfiguration, AlgorithmConfiguration> processor = TypedSPILoader.getService(
            RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("fixture", "algorithm"));
    
    @Test
    void assertSwapRuleItemConfiguration() {
        AlgorithmConfiguration actual = processor.swapRuleItemConfiguration(null, createYAMLContent());
        assertThat(actual, deepEqual(new AlgorithmConfiguration("foo_algo", new Properties())));
    }
    
    private String createYAMLContent() {
        YamlAlgorithmConfiguration yamlConfig = new YamlAlgorithmConfiguration();
        yamlConfig.setType("foo_algo");
        return YamlEngine.marshal(yamlConfig);
    }
    
    @Test
    void assertFindRuleConfiguration() {
        AlgorithmChangedProcessorFixtureRuleConfiguration ruleConfig = new AlgorithmChangedProcessorFixtureRuleConfiguration();
        assertThat(processor.findRuleConfiguration(mockDatabase(ruleConfig)), is(ruleConfig));
    }
    
    private ShardingSphereDatabase mockDatabase(final AlgorithmChangedProcessorFixtureRuleConfiguration ruleConfig) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(new AlgorithmChangedProcessorFixtureRule(ruleConfig))));
        return result;
    }
    
    @Test
    void assertChangeRuleItemConfiguration() {
        AlgorithmChangedProcessorFixtureRuleConfiguration currentRuleConfig = new AlgorithmChangedProcessorFixtureRuleConfiguration();
        currentRuleConfig.getAlgorithmConfigurations().put("foo_algo", new AlgorithmConfiguration("FOO_FIXTURE", new Properties()));
        AlgorithmConfiguration toBeChangedItemConfig = new AlgorithmConfiguration("BAR_FIXTURE", new Properties());
        processor.changeRuleItemConfiguration("bar_algo", currentRuleConfig, toBeChangedItemConfig);
        assertThat(currentRuleConfig.getAlgorithmConfigurations().size(), is(2));
        assertThat(currentRuleConfig.getAlgorithmConfigurations().get("foo_algo").getType(), is("FOO_FIXTURE"));
        assertThat(currentRuleConfig.getAlgorithmConfigurations().get("bar_algo").getType(), is("BAR_FIXTURE"));
    }
    
    @Test
    void assertDropRuleItemConfiguration() {
        AlgorithmChangedProcessorFixtureRuleConfiguration currentRuleConfig = new AlgorithmChangedProcessorFixtureRuleConfiguration();
        currentRuleConfig.getAlgorithmConfigurations().put("foo_algo", new AlgorithmConfiguration("FOO_FIXTURE", new Properties()));
        processor.dropRuleItemConfiguration("foo_algo", currentRuleConfig);
        assertTrue(currentRuleConfig.getAlgorithmConfigurations().isEmpty());
    }
}
