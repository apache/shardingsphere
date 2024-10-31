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

package org.apache.shardingsphere.encrypt.rule.changed;

import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.dispatch.rule.alter.AlterNamedRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.mode.spi.RuleItemConfigurationChangedProcessor;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import static org.apache.shardingsphere.test.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptorChangedProcessorTest {
    
    @SuppressWarnings("unchecked")
    private final RuleItemConfigurationChangedProcessor<EncryptRuleConfiguration, AlgorithmConfiguration> processor = TypedSPILoader.getService(
            RuleItemConfigurationChangedProcessor.class, "encrypt.encryptors");
    
    @Test
    void assertSwapRuleItemConfiguration() {
        AlgorithmConfiguration actual = processor.swapRuleItemConfiguration(new AlterNamedRuleItemEvent("", "foo_tbl", "", "", ""), createYAMLContent());
        assertThat(actual, deepEqual(new AlgorithmConfiguration("foo_algo", new Properties())));
    }
    
    private String createYAMLContent() {
        YamlAlgorithmConfiguration yamlConfig = new YamlAlgorithmConfiguration();
        yamlConfig.setType("foo_algo");
        return YamlEngine.marshal(yamlConfig);
    }
    
    @Test
    void assertFindRuleConfiguration() {
        EncryptRuleConfiguration ruleConfig = mock(EncryptRuleConfiguration.class);
        assertThat(processor.findRuleConfiguration(mockDatabase(ruleConfig)), is(ruleConfig));
    }
    
    private ShardingSphereDatabase mockDatabase(final EncryptRuleConfiguration ruleConfig) {
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
    
    @Test
    void assertChangeRuleItemConfiguration() {
        EncryptRuleConfiguration currentRuleConfig = new EncryptRuleConfiguration(Collections.emptyList(), new HashMap<>(Collections.singletonMap("foo_algo", mock(AlgorithmConfiguration.class))));
        AlgorithmConfiguration toBeChangedItemConfig = new AlgorithmConfiguration("FIXTURE", new Properties());
        processor.changeRuleItemConfiguration(
                new AlterNamedRuleItemEvent("foo_db", "foo_algo", "", "", ""), currentRuleConfig, toBeChangedItemConfig);
        assertThat(currentRuleConfig.getEncryptors().size(), is(1));
        assertThat(currentRuleConfig.getEncryptors().get("foo_algo").getType(), is("FIXTURE"));
    }
    
    @Test
    void assertDropRuleItemConfiguration() {
        EncryptRuleConfiguration currentRuleConfig = new EncryptRuleConfiguration(Collections.emptyList(), new HashMap<>(Collections.singletonMap("foo_algo", mock(AlgorithmConfiguration.class))));
        processor.dropRuleItemConfiguration(new DropNamedRuleItemEvent("", "foo_algo", ""), currentRuleConfig);
        assertTrue(currentRuleConfig.getEncryptors().isEmpty());
    }
}
