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

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperFactory;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class EncryptRuleConfigurationYamlSwapperTest {
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlEncryptRuleConfiguration actual = getSwapper().swapToYamlConfiguration(createEncryptRuleConfiguration());
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getEncryptors().size(), is(1));
    }
    
    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        Collection<EncryptTableRuleConfiguration> tables = Collections.singletonList(new EncryptTableRuleConfiguration("tbl", Collections.emptyList(), null));
        Map<String, ShardingSphereAlgorithmConfiguration> encryptors = Collections.singletonMap("myEncryptor", new ShardingSphereAlgorithmConfiguration("FIXTURE", new Properties()));
        return new EncryptRuleConfiguration(tables, encryptors);
    }
    
    @Test
    public void assertSwapToObject() {
        EncryptRuleConfiguration actual = getSwapper().swapToObject(createYamlEncryptRuleConfiguration());
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getEncryptors().size(), is(1));
    }
    
    private YamlEncryptRuleConfiguration createYamlEncryptRuleConfiguration() {
        YamlEncryptRuleConfiguration result = new YamlEncryptRuleConfiguration();
        YamlEncryptTableRuleConfiguration tableRuleConfig = new YamlEncryptTableRuleConfiguration();
        tableRuleConfig.setName("t_encrypt");
        result.getTables().put("t_encrypt", tableRuleConfig);
        YamlShardingSphereAlgorithmConfiguration algorithmConfig = new YamlShardingSphereAlgorithmConfiguration();
        algorithmConfig.setType("CORE.FIXTURE");
        result.getEncryptors().put("fixture_encryptor", algorithmConfig);
        return result;
    }
    
    private EncryptRuleConfigurationYamlSwapper getSwapper() {
        EncryptRuleConfiguration ruleConfig = mock(EncryptRuleConfiguration.class);
        return (EncryptRuleConfigurationYamlSwapper) YamlRuleConfigurationSwapperFactory.getInstanceMapByRuleConfigurations(Collections.singletonList(ruleConfig)).get(ruleConfig);
    }
    
    @Test
    public void assertDataConvertersSwap() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/encrypt-dataConverters.yaml");
        assertNotNull(url);
        YamlEncryptRuleConfiguration yamlConfig = YamlEngine.unmarshal(new File(url.getFile()), YamlEncryptRuleConfiguration.class);
        EncryptRuleConfigurationYamlSwapper swapper = new EncryptRuleConfigurationYamlSwapper();
        EncryptRuleConfiguration actualConfig = swapper.swapToObject(yamlConfig);
        YamlEncryptRuleConfiguration actualYamlConfig = swapper.swapToYamlConfiguration(actualConfig);
        assertThat(YamlEngine.marshal(actualYamlConfig), is(YamlEngine.marshal(yamlConfig)));
    }
}
