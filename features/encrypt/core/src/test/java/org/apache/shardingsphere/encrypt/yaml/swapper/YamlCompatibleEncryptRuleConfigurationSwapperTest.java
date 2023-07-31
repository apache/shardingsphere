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

import org.apache.shardingsphere.encrypt.api.config.CompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlCompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlCompatibleEncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

@Deprecated
class YamlCompatibleEncryptRuleConfigurationSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlCompatibleEncryptRuleConfiguration actual = getSwapper().swapToYamlConfiguration(createEncryptRuleConfiguration());
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getEncryptors().size(), is(1));
    }
    
    private CompatibleEncryptRuleConfiguration createEncryptRuleConfiguration() {
        Collection<EncryptTableRuleConfiguration> tables = Collections.singletonList(new EncryptTableRuleConfiguration("tbl", Collections.emptyList()));
        Map<String, AlgorithmConfiguration> encryptors = Collections.singletonMap("myEncryptor", new AlgorithmConfiguration("FIXTURE", new Properties()));
        return new CompatibleEncryptRuleConfiguration(tables, encryptors);
    }
    
    @Test
    void assertSwapToObject() {
        CompatibleEncryptRuleConfiguration actual = getSwapper().swapToObject(createYamlEncryptRuleConfiguration());
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getEncryptors().size(), is(1));
    }
    
    private YamlCompatibleEncryptRuleConfiguration createYamlEncryptRuleConfiguration() {
        YamlCompatibleEncryptRuleConfiguration result = new YamlCompatibleEncryptRuleConfiguration();
        YamlCompatibleEncryptTableRuleConfiguration tableRuleConfig = new YamlCompatibleEncryptTableRuleConfiguration();
        tableRuleConfig.setName("t_encrypt");
        result.getTables().put("t_encrypt", tableRuleConfig);
        YamlAlgorithmConfiguration algorithmConfig = new YamlAlgorithmConfiguration();
        algorithmConfig.setType("CORE.FIXTURE");
        result.getEncryptors().put("fixture_encryptor", algorithmConfig);
        return result;
    }
    
    private YamlCompatibleEncryptRuleConfigurationSwapper getSwapper() {
        CompatibleEncryptRuleConfiguration ruleConfig = mock(CompatibleEncryptRuleConfiguration.class);
        return (YamlCompatibleEncryptRuleConfigurationSwapper) OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class, Collections.singleton(ruleConfig)).get(ruleConfig);
    }
}
