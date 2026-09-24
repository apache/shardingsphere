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

import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class YamlShadowRuleConfigurationSwapperTest {
    
    private final YamlShadowRuleConfigurationSwapper swapper = new YamlShadowRuleConfigurationSwapper();
    
    @Test
    void assertSwapToObjectWithDefaultShadowAlgorithmName() {
        YamlShadowRuleConfiguration yamlConfig = createYamlRuleConfiguration(Collections.emptyList());
        yamlConfig.setDefaultShadowAlgorithmName("foo_algo");
        ShadowRuleConfiguration actual = swapper.swapToObject(yamlConfig);
        assertThat(actual.getDefaultShadowAlgorithmName(), is("foo_algo"));
        assertThat(actual.getTables().get("foo_tbl").getShadowAlgorithmNames(), is(Collections.singletonList("foo_algo")));
    }
    
    @Test
    void assertSwapToObjectWithoutDefaultShadowAlgorithmName() {
        ShadowRuleConfiguration actual = swapper.swapToObject(createYamlRuleConfiguration(Collections.emptyList()));
        assertThat(actual.getTables().get("foo_tbl").getShadowAlgorithmNames(), is(Collections.emptyList()));
    }
    
    @Test
    void assertSwapToObjectWithTableShadowAlgorithmNames() {
        YamlShadowRuleConfiguration yamlConfig = createYamlRuleConfiguration(Collections.singletonList("bar_algo"));
        yamlConfig.setDefaultShadowAlgorithmName("foo_algo");
        ShadowRuleConfiguration actual = swapper.swapToObject(yamlConfig);
        assertThat(actual.getTables().get("foo_tbl").getShadowAlgorithmNames(), is(Collections.singletonList("bar_algo")));
    }
    
    private YamlShadowRuleConfiguration createYamlRuleConfiguration(final Collection<String> shadowAlgorithmNames) {
        YamlShadowTableConfiguration yamlTableConfig = new YamlShadowTableConfiguration();
        yamlTableConfig.setShadowAlgorithmNames(new LinkedList<>(shadowAlgorithmNames));
        YamlShadowRuleConfiguration result = new YamlShadowRuleConfiguration();
        result.getTables().put("foo_tbl", yamlTableConfig);
        return result;
    }
}
