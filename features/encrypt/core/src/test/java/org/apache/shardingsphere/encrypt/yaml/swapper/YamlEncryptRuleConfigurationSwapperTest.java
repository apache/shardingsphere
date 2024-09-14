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

import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class YamlEncryptRuleConfigurationSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlEncryptRuleConfiguration actual = getSwapper().swapToYamlConfiguration(createEncryptRuleConfiguration());
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getTables().get("tbl").getName(), is("tbl"));
        assertTrue(actual.getTables().get("tbl").getColumns().isEmpty());
        assertThat(actual.getEncryptors().size(), is(1));
        assertThat(actual.getEncryptors().get("foo_encryptor").getType(), is("CORE.FIXTURE"));
        assertThat(actual.getEncryptors().get("foo_encryptor").getProps(), is(new Properties()));
    }
    
    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        Collection<EncryptTableRuleConfiguration> tables = Collections.singleton(new EncryptTableRuleConfiguration("tbl", Collections.emptyList()));
        Map<String, AlgorithmConfiguration> encryptors = Collections.singletonMap("foo_encryptor", new AlgorithmConfiguration("CORE.FIXTURE", new Properties()));
        return new EncryptRuleConfiguration(tables, encryptors);
    }
    
    @Test
    void assertSwapToObject() {
        EncryptRuleConfiguration actual = getSwapper().swapToObject(createYamlEncryptRuleConfiguration());
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getTables().iterator().next().getName(), is("tbl"));
        assertTrue(actual.getTables().iterator().next().getColumns().isEmpty());
        assertThat(actual.getEncryptors().size(), is(1));
        assertThat(actual.getEncryptors().get("foo_encryptor").getType(), is("CORE.FIXTURE"));
        assertThat(actual.getEncryptors().get("foo_encryptor").getProps(), is(new Properties()));
    }
    
    private YamlEncryptRuleConfiguration createYamlEncryptRuleConfiguration() {
        YamlEncryptRuleConfiguration result = new YamlEncryptRuleConfiguration();
        YamlEncryptTableRuleConfiguration tableRuleConfig = new YamlEncryptTableRuleConfiguration();
        tableRuleConfig.setName("tbl");
        result.getTables().put("tbl", tableRuleConfig);
        YamlAlgorithmConfiguration algorithmConfig = new YamlAlgorithmConfiguration();
        algorithmConfig.setType("CORE.FIXTURE");
        result.getEncryptors().put("foo_encryptor", algorithmConfig);
        return result;
    }
    
    private YamlEncryptRuleConfigurationSwapper getSwapper() {
        EncryptRuleConfiguration ruleConfig = mock(EncryptRuleConfiguration.class);
        return (YamlEncryptRuleConfigurationSwapper) OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class, Collections.singleton(ruleConfig)).get(ruleConfig);
    }
}
