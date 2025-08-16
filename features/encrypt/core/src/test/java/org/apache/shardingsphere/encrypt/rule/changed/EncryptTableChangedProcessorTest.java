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
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptTableChangedProcessorTest {
    
    @SuppressWarnings("unchecked")
    private final RuleItemConfigurationChangedProcessor<EncryptRuleConfiguration, EncryptTableRuleConfiguration> processor = TypedSPILoader.getService(
            RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("encrypt", "tables"));
    
    @Test
    void assertSwapRuleItemConfiguration() {
        EncryptTableRuleConfiguration actual = processor.swapRuleItemConfiguration(null, createYAMLContent());
        assertThat(actual, deepEqual(new EncryptTableRuleConfiguration("foo_tbl",
                Collections.singletonList(new EncryptColumnRuleConfiguration("foo_col", new EncryptColumnItemRuleConfiguration("foo_col_cipher", "foo_algo"))))));
    }
    
    private String createYAMLContent() {
        YamlEncryptTableRuleConfiguration yamlConfig = new YamlEncryptTableRuleConfiguration();
        yamlConfig.setName("foo_tbl");
        YamlEncryptColumnRuleConfiguration yamlColumnRuleConfig = new YamlEncryptColumnRuleConfiguration();
        yamlColumnRuleConfig.setName("foo_col");
        YamlEncryptColumnItemRuleConfiguration yamlCipherColumn = new YamlEncryptColumnItemRuleConfiguration();
        yamlCipherColumn.setName("foo_col_cipher");
        yamlCipherColumn.setEncryptorName("foo_algo");
        yamlColumnRuleConfig.setCipher(yamlCipherColumn);
        yamlConfig.setColumns(Collections.singletonMap("foo_col", yamlColumnRuleConfig));
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
        EncryptRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        EncryptTableRuleConfiguration toBeChangedItemConfig = new EncryptTableRuleConfiguration("foo_tbl",
                Collections.singleton(new EncryptColumnRuleConfiguration("foo_col", new EncryptColumnItemRuleConfiguration("bar_col_cipher", "bar_algo"))));
        processor.changeRuleItemConfiguration(null, currentRuleConfig, toBeChangedItemConfig);
        assertThat(currentRuleConfig.getTables().size(), is(1));
        assertThat(new ArrayList<>(currentRuleConfig.getTables()).get(0).getColumns().size(), is(1));
        assertThat(new ArrayList<>(new ArrayList<>(currentRuleConfig.getTables()).get(0).getColumns()).get(0).getCipher().getName(), is("bar_col_cipher"));
        assertThat(new ArrayList<>(new ArrayList<>(currentRuleConfig.getTables()).get(0).getColumns()).get(0).getCipher().getEncryptorName(), is("bar_algo"));
    }
    
    @Test
    void assertDropRuleItemConfiguration() {
        EncryptRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        processor.dropRuleItemConfiguration("foo_tbl", currentRuleConfig);
        assertTrue(currentRuleConfig.getTables().isEmpty());
    }
    
    private EncryptRuleConfiguration createCurrentRuleConfiguration() {
        EncryptTableRuleConfiguration toBeChangedItemConfig = new EncryptTableRuleConfiguration("foo_tbl",
                Collections.singleton(new EncryptColumnRuleConfiguration("foo_col", new EncryptColumnItemRuleConfiguration("foo_col_cipher", "foo_algo"))));
        return new EncryptRuleConfiguration(new LinkedList<>(Collections.singleton(toBeChangedItemConfig)), Collections.emptyMap());
    }
}
