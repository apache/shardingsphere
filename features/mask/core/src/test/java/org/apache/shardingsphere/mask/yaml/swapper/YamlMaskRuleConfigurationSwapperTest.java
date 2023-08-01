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

package org.apache.shardingsphere.mask.yaml.swapper;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.config.YamlMaskRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.config.rule.YamlMaskTableRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class YamlMaskRuleConfigurationSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlMaskRuleConfiguration actual = getSwapper().swapToYamlConfiguration(createMaskRuleConfiguration());
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getMaskAlgorithms().size(), is(1));
    }
    
    private MaskRuleConfiguration createMaskRuleConfiguration() {
        Collection<MaskTableRuleConfiguration> tables = Collections.singletonList(new MaskTableRuleConfiguration("tbl", Collections.emptyList()));
        Map<String, AlgorithmConfiguration> encryptors = Collections.singletonMap("myMaskAlgorithm", new AlgorithmConfiguration("MD5", new Properties()));
        return new MaskRuleConfiguration(tables, encryptors);
    }
    
    @Test
    void assertSwapToObject() {
        MaskRuleConfiguration actual = getSwapper().swapToObject(createYamlMaskRuleConfiguration());
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getMaskAlgorithms().size(), is(1));
    }
    
    private YamlMaskRuleConfiguration createYamlMaskRuleConfiguration() {
        YamlMaskRuleConfiguration result = new YamlMaskRuleConfiguration();
        YamlMaskTableRuleConfiguration tableRuleConfig = new YamlMaskTableRuleConfiguration();
        tableRuleConfig.setName("t_mask");
        result.getTables().put("t_mask", tableRuleConfig);
        YamlAlgorithmConfiguration algorithmConfig = new YamlAlgorithmConfiguration();
        algorithmConfig.setType("MD5");
        result.getMaskAlgorithms().put("md5_mask", algorithmConfig);
        return result;
    }
    
    private YamlMaskRuleConfigurationSwapper getSwapper() {
        MaskRuleConfiguration ruleConfig = mock(MaskRuleConfiguration.class);
        return (YamlMaskRuleConfigurationSwapper) OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class, Collections.singleton(ruleConfig)).get(ruleConfig);
    }
}
