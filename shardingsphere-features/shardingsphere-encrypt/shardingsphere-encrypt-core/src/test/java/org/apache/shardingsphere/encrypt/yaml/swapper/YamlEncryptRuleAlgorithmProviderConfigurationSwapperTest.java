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

package org.apache.shardingsphere.encrypt.yaml.swapper;

import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class YamlEncryptRuleAlgorithmProviderConfigurationSwapperTest {
    
    @Mock
    private AlgorithmProvidedEncryptRuleConfiguration ruleConfig;
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlEncryptRuleConfiguration actual = getSwapper().swapToYamlConfiguration(createAlgorithmProvidedEncryptRuleConfiguration());
        assertThat(actual.getTables().size(), is(1));
        assertTrue(actual.getEncryptors().isEmpty());
    }
    
    private AlgorithmProvidedEncryptRuleConfiguration createAlgorithmProvidedEncryptRuleConfiguration() {
        Collection<EncryptTableRuleConfiguration> tables = Collections.singletonList(new EncryptTableRuleConfiguration("tbl", Collections.emptyList(), null));
        Map<String, EncryptAlgorithm<?, ?>> encryptors = new LinkedHashMap<>();
        return new AlgorithmProvidedEncryptRuleConfiguration(tables, encryptors, true);
    }
    
    @Test
    public void assertSwapToObject() {
        AlgorithmProvidedEncryptRuleConfiguration actual = getSwapper().swapToObject(createYamlEncryptRuleConfiguration());
        assertThat(actual.getTables().size(), is(1));
        assertTrue(actual.getEncryptors().isEmpty());
    }
    
    private YamlEncryptRuleConfiguration createYamlEncryptRuleConfiguration() {
        YamlEncryptRuleConfiguration result = new YamlEncryptRuleConfiguration();
        YamlEncryptTableRuleConfiguration tableRuleConfig = new YamlEncryptTableRuleConfiguration();
        tableRuleConfig.setName("t_encrypt");
        result.getTables().put("t_encrypt", tableRuleConfig);
        YamlAlgorithmConfiguration algorithmConfig = new YamlAlgorithmConfiguration();
        algorithmConfig.setType("CORE.FIXTURE");
        result.getEncryptors().put("fixture_encryptor", algorithmConfig);
        return result;
    }
    
    private YamlEncryptRuleAlgorithmProviderConfigurationSwapper getSwapper() {
        return (YamlEncryptRuleAlgorithmProviderConfigurationSwapper) YamlRuleConfigurationSwapperFactory.getInstanceMapByRuleConfigurations(Collections.singletonList(ruleConfig)).get(ruleConfig);
    }
}
