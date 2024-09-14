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

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.datasource.YamlShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class YamlShadowRuleConfigurationSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlShadowRuleConfiguration actual = getSwapper().swapToYamlConfiguration(createShadowRuleConfiguration());
        assertThat(actual.getDataSources().size(), is(1));
        assertThat(actual.getDataSources().get("foo_ds").getProductionDataSourceName(), is("prod_ds"));
        assertThat(actual.getDataSources().get("foo_ds").getShadowDataSourceName(), is("shadow_ds"));
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getTables().get("tbl").getDataSourceNames(), is(Collections.singletonList("ds")));
        assertThat(actual.getTables().get("tbl").getShadowAlgorithmNames(), is(Collections.singletonList("shadow_algo")));
        assertThat(actual.getShadowAlgorithms().size(), is(1));
        assertThat(actual.getShadowAlgorithms().get("shadow_algo").getType(), is("FIXTURE"));
        assertThat(actual.getShadowAlgorithms().get("shadow_algo").getProps(), is(new Properties()));
        assertThat(actual.getDefaultShadowAlgorithmName(), is("default_shadow_algorithm_name"));
    }
    
    private ShadowRuleConfiguration createShadowRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.getDataSources().add(new ShadowDataSourceConfiguration("foo_ds", "prod_ds", "shadow_ds"));
        result.getTables().put("tbl", new ShadowTableConfiguration(Collections.singleton("ds"), Collections.singleton("shadow_algo")));
        result.getShadowAlgorithms().put("shadow_algo", new AlgorithmConfiguration("FIXTURE", new Properties()));
        result.setDefaultShadowAlgorithmName("default_shadow_algorithm_name");
        return result;
    }
    
    @Test
    void assertSwapToObject() {
        ShadowRuleConfiguration actual = getSwapper().swapToObject(createYamlShadowRuleConfiguration());
        assertThat(actual.getDataSources().size(), is(1));
        assertShadowDataSourceConfiguration(actual.getDataSources().iterator().next());
        assertThat(actual.getTables().size(), is(1));
        assertShadowTableConfiguration(actual.getTables().get("tbl"));
        assertAlgorithmConfiguration(actual.getShadowAlgorithms().get("shadow_algo"));
    }
    
    private YamlShadowRuleConfiguration createYamlShadowRuleConfiguration() {
        YamlShadowRuleConfiguration result = new YamlShadowRuleConfiguration();
        result.getDataSources().put("foo_ds", createYamlShadowDataSourceConfiguration());
        result.getTables().put("tbl", createYamlShadowTableConfiguration());
        result.getShadowAlgorithms().put("shadow_algo", getYamlAlgorithmConfiguration());
        result.setDefaultShadowAlgorithmName("default_shadow_algorithm_name");
        return result;
    }
    
    private YamlShadowDataSourceConfiguration createYamlShadowDataSourceConfiguration() {
        YamlShadowDataSourceConfiguration result = new YamlShadowDataSourceConfiguration();
        result.setProductionDataSourceName("prod_ds");
        result.setShadowDataSourceName("shadow_ds");
        return result;
    }
    
    private YamlShadowTableConfiguration createYamlShadowTableConfiguration() {
        YamlShadowTableConfiguration result = new YamlShadowTableConfiguration();
        result.setDataSourceNames(Collections.singleton("ds"));
        result.setShadowAlgorithmNames(Collections.singleton("shadow_algo"));
        return result;
    }
    
    private YamlAlgorithmConfiguration getYamlAlgorithmConfiguration() {
        YamlAlgorithmConfiguration result = new YamlAlgorithmConfiguration();
        result.setType("FIXTURE");
        return result;
    }
    
    private void assertShadowDataSourceConfiguration(final ShadowDataSourceConfiguration actual) {
        assertThat(actual.getName(), is("foo_ds"));
        assertThat(actual.getProductionDataSourceName(), is("prod_ds"));
        assertThat(actual.getShadowDataSourceName(), is("shadow_ds"));
    }
    
    private void assertShadowTableConfiguration(final ShadowTableConfiguration actual) {
        assertThat(actual.getDataSourceNames(), is(Collections.singleton("ds")));
        assertThat(actual.getShadowAlgorithmNames(), is(Collections.singletonList("shadow_algo")));
    }
    
    private void assertAlgorithmConfiguration(final AlgorithmConfiguration actual) {
        assertThat(actual.getType(), is("FIXTURE"));
        assertThat(actual.getProps(), is(new Properties()));
    }
    
    private YamlShadowRuleConfigurationSwapper getSwapper() {
        ShadowRuleConfiguration ruleConfig = mock(ShadowRuleConfiguration.class);
        return (YamlShadowRuleConfigurationSwapper) OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class, Collections.singleton(ruleConfig)).get(ruleConfig);
    }
}
