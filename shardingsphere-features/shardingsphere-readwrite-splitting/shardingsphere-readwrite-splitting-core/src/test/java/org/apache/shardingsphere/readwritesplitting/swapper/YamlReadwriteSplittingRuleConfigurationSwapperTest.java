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

package org.apache.shardingsphere.readwritesplitting.swapper;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperFactory;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.strategy.YamlStaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.swapper.YamlReadwriteSplittingRuleConfigurationSwapper;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlReadwriteSplittingRuleConfigurationSwapperTest {
    
    @Test
    public void assertSwapToYamlWithLoadBalanceAlgorithm() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("ds",
                        new StaticReadwriteSplittingStrategyConfiguration("write", Collections.singletonList("read")), null, "roundRobin");
        YamlReadwriteSplittingRuleConfiguration actual = getYamlReadwriteSplittingRuleConfigurationSwapper().swapToYamlConfiguration(new ReadwriteSplittingRuleConfiguration(
                Collections.singleton(dataSourceConfig), Collections.singletonMap("roundRobin", new AlgorithmConfiguration("ROUND_ROBIN", new Properties()))));
        assertNotNull(actual.getDataSources().get("ds").getStaticStrategy());
        assertThat((actual.getDataSources().get("ds").getStaticStrategy()).getWriteDataSourceName(), is("write"));
        assertThat((actual.getDataSources().get("ds").getStaticStrategy()).getReadDataSourceNames(), is(Collections.singletonList("read")));
        assertThat(actual.getDataSources().get("ds").getLoadBalancerName(), is("roundRobin"));
    }
    
    @Test
    public void assertSwapToYamlWithoutLoadBalanceAlgorithm() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig = new ReadwriteSplittingDataSourceRuleConfiguration("ds",
                new StaticReadwriteSplittingStrategyConfiguration("write", Collections.singletonList("read")), null, null);
        YamlReadwriteSplittingRuleConfiguration actual = getYamlReadwriteSplittingRuleConfigurationSwapper().swapToYamlConfiguration(
                new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceConfig), Collections.emptyMap()));
        assertNotNull(actual.getDataSources().get("ds").getStaticStrategy());
        assertThat(actual.getDataSources().get("ds").getStaticStrategy().getWriteDataSourceName(), is("write"));
        assertThat(actual.getDataSources().get("ds").getStaticStrategy().getReadDataSourceNames(), is(Collections.singletonList("read")));
        assertNull(actual.getDataSources().get("ds").getLoadBalancerName());
    }
    
    @Test
    public void assertSwapToObjectWithLoadBalanceAlgorithmType() {
        YamlReadwriteSplittingRuleConfiguration yamlConfig = createYamlReadwriteSplittingRuleConfiguration();
        yamlConfig.getDataSources().get("read_query_ds").setLoadBalancerName("RANDOM");
        ReadwriteSplittingRuleConfiguration actual = getYamlReadwriteSplittingRuleConfigurationSwapper().swapToObject(yamlConfig);
        assertReadwriteSplittingRuleConfiguration(actual);
        assertThat(actual.getDataSources().iterator().next().getLoadBalancerName(), is("RANDOM"));
    }
    
    @Test
    public void assertSwapToObjectWithoutLoadBalanceAlgorithm() {
        YamlReadwriteSplittingRuleConfiguration yamlConfig = createYamlReadwriteSplittingRuleConfiguration();
        ReadwriteSplittingRuleConfiguration actual = getYamlReadwriteSplittingRuleConfigurationSwapper().swapToObject(yamlConfig);
        assertReadwriteSplittingRuleConfiguration(actual);
        assertNull(actual.getDataSources().iterator().next().getLoadBalancerName());
    }
    
    private YamlReadwriteSplittingRuleConfiguration createYamlReadwriteSplittingRuleConfiguration() {
        YamlReadwriteSplittingRuleConfiguration result = new YamlReadwriteSplittingRuleConfiguration();
        result.getDataSources().put("read_query_ds", new YamlReadwriteSplittingDataSourceRuleConfiguration());
        YamlStaticReadwriteSplittingStrategyConfiguration staticConfig = new YamlStaticReadwriteSplittingStrategyConfiguration();
        staticConfig.setWriteDataSourceName("write");
        staticConfig.setReadDataSourceNames(Collections.singletonList("read"));
        result.getDataSources().get("read_query_ds").setStaticStrategy(staticConfig);
        return result;
    }
    
    private void assertReadwriteSplittingRuleConfiguration(final ReadwriteSplittingRuleConfiguration actual) {
        ReadwriteSplittingDataSourceRuleConfiguration group = actual.getDataSources().iterator().next();
        assertThat(group.getName(), is("read_query_ds"));
        assertNotNull(group.getStaticStrategy());
        assertThat(group.getStaticStrategy().getWriteDataSourceName(), is("write"));
        assertThat(group.getStaticStrategy().getReadDataSourceNames(), is(Collections.singletonList("read")));
    }
    
    private YamlReadwriteSplittingRuleConfigurationSwapper getYamlReadwriteSplittingRuleConfigurationSwapper() {
        Optional<YamlReadwriteSplittingRuleConfigurationSwapper> result = YamlRuleConfigurationSwapperFactory.getAllInstances().stream()
                .filter(each -> each instanceof YamlReadwriteSplittingRuleConfigurationSwapper)
                .map(each -> (YamlReadwriteSplittingRuleConfigurationSwapper) each)
                .findFirst();
        assertTrue(result.isPresent());
        return result.get();
    }
}
