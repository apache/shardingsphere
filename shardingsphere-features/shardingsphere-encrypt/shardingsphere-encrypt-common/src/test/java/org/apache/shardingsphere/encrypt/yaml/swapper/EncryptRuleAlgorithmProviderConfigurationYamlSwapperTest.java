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
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
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

@RunWith(MockitoJUnitRunner.class)
public final class EncryptRuleAlgorithmProviderConfigurationYamlSwapperTest {
    
    static {
        ShardingSphereServiceLoader.register(YamlRuleConfigurationSwapper.class);
    }
    
    @Mock
    private AlgorithmProvidedEncryptRuleConfiguration ruleConfig;
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlEncryptRuleConfiguration actual = getSwapper().swapToYamlConfiguration(createAlgorithmProvidedEncryptRuleConfiguration());
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getEncryptors().size(), is(0));
    }
    
    private AlgorithmProvidedEncryptRuleConfiguration createAlgorithmProvidedEncryptRuleConfiguration() {
        Collection<EncryptTableRuleConfiguration> tables = Collections.singletonList(new EncryptTableRuleConfiguration("tbl", Collections.emptyList()));
        Map<String, EncryptAlgorithm> encryptors = new LinkedHashMap<>();
        return new AlgorithmProvidedEncryptRuleConfiguration(tables, encryptors);
    }
    
    @Test
    public void assertSwapToObject() {
        AlgorithmProvidedEncryptRuleConfiguration actual = getSwapper().swapToObject(createYamlEncryptRuleConfiguration());
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getEncryptors().size(), is(0));
    }
    
    private YamlEncryptRuleConfiguration createYamlEncryptRuleConfiguration() {
        YamlEncryptRuleConfiguration result = new YamlEncryptRuleConfiguration();
        YamlEncryptTableRuleConfiguration tableRuleConfig = new YamlEncryptTableRuleConfiguration();
        tableRuleConfig.setName("t_encrypt");
        result.getTables().put("t_encrypt", tableRuleConfig);
        YamlShardingSphereAlgorithmConfiguration algorithmConfig = new YamlShardingSphereAlgorithmConfiguration();
        algorithmConfig.setType("TEST");
        result.getEncryptors().put("test", algorithmConfig);
        return result;
    }
    
    private EncryptRuleAlgorithmProviderConfigurationYamlSwapper getSwapper() {
        return (EncryptRuleAlgorithmProviderConfigurationYamlSwapper)
                    OrderedSPIRegistry.getRegisteredServices(Collections.singletonList(ruleConfig), YamlRuleConfigurationSwapper.class).get(ruleConfig);
    }
}
