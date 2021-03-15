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

package org.apache.shardingsphere.ha.yaml.swapper;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.ha.api.config.HARuleConfiguration;
import org.apache.shardingsphere.ha.api.config.rule.HADataSourceRuleConfiguration;
import org.apache.shardingsphere.ha.constant.HAOrder;
import org.apache.shardingsphere.ha.yaml.config.YamlHARuleConfiguration;
import org.apache.shardingsphere.ha.yaml.config.rule.YamlHADataSourceRuleConfiguration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class HARuleConfigurationYamlSwapperTest {
    
    private final Collection<YamlRuleConfigurationSwapper> collection = ShardingSphereServiceLoader.newServiceInstances(YamlRuleConfigurationSwapper.class);
    
    static {
        ShardingSphereServiceLoader.register(YamlRuleConfigurationSwapper.class);
    }
    
    @Test
    public void assertSwapToYamlWithLoadBalanceAlgorithm() {
        HADataSourceRuleConfiguration dataSourceConfig =
                new HADataSourceRuleConfiguration("ds", Collections.singletonList("dataSourceName"), "haTypeName");
        YamlHARuleConfiguration actual = getHARuleConfigurationYamlSwapper().swapToYamlConfiguration(new HARuleConfiguration(Collections.singleton(dataSourceConfig),
                ImmutableMap.of("mgr", new ShardingSphereAlgorithmConfiguration("MGR", new Properties()))));
        assertThat(actual.getDataSources().get("ds").getName(), is("ds"));
        assertThat(actual.getDataSources().get("ds").getDataSourceNames(), is(Collections.singletonList("dataSourceName")));
    }
    
    @Test
    public void assertSwapToYamlWithoutLoadBalanceAlgorithm() {
        HADataSourceRuleConfiguration dataSourceConfig = new HADataSourceRuleConfiguration("ds", Collections.singletonList("dataSourceName"), "haTypeName");
        YamlHARuleConfiguration actual = getHARuleConfigurationYamlSwapper().swapToYamlConfiguration(
                new HARuleConfiguration(Collections.singleton(dataSourceConfig), Collections.emptyMap()));
        assertThat(actual.getDataSources().get("ds").getName(), is("ds"));
        assertThat(actual.getDataSources().get("ds").getDataSourceNames(), is(Collections.singletonList("dataSourceName")));
    }
    
    @Test
    public void assertSwapToObjectWithLoadBalanceAlgorithmType() {
        YamlHARuleConfiguration yamlConfig = createYamlHARuleConfiguration();
        HARuleConfiguration actual = getHARuleConfigurationYamlSwapper().swapToObject(yamlConfig);
        assertHARuleConfiguration(actual);
    }
    
    @Test
    public void assertSwapToObjectWithoutLoadBalanceAlgorithm() {
        YamlHARuleConfiguration yamlConfig = createYamlHARuleConfiguration();
        HARuleConfiguration actual = getHARuleConfigurationYamlSwapper().swapToObject(yamlConfig);
        assertHARuleConfiguration(actual);
    }
    
    private YamlHARuleConfiguration createYamlHARuleConfiguration() {
        YamlHARuleConfiguration result = new YamlHARuleConfiguration();
        result.getDataSources().put("ha_ds", new YamlHADataSourceRuleConfiguration());
        result.getDataSources().get("ha_ds").setName("ha_ds");
        result.getDataSources().get("ha_ds").setDataSourceNames(Arrays.asList("ds_0", "ds_1"));
        return result;
    }
    
    private void assertHARuleConfiguration(final HARuleConfiguration actual) {
        HADataSourceRuleConfiguration group = actual.getDataSources().iterator().next();
        assertThat(group.getName(), is("ha_ds"));
        assertThat(group.getDataSourceNames(), is(Arrays.asList("ds_0", "ds_1")));
    }
    
    @Test
    public void assertGetTypeClass() {
        HARuleConfigurationYamlSwapper swapper = getHARuleConfigurationYamlSwapper();
        Class<HARuleConfiguration> actual = swapper.getTypeClass();
        assertTrue(actual.isAssignableFrom(HARuleConfiguration.class));
    }
    
    @Test
    public void assertGetOrder() {
        HARuleConfigurationYamlSwapper swapper = getHARuleConfigurationYamlSwapper();
        int actual = swapper.getOrder();
        assertThat(actual, is(HAOrder.ORDER));
    }
    
    private HARuleConfigurationYamlSwapper getHARuleConfigurationYamlSwapper() {
        Optional<HARuleConfigurationYamlSwapper> optional = collection.stream()
                .filter(swapper -> swapper instanceof HARuleConfigurationYamlSwapper)
                .map(swapper -> (HARuleConfigurationYamlSwapper) swapper)
                .findFirst();
        assertTrue(optional.isPresent());
        return optional.get();
    }
}
