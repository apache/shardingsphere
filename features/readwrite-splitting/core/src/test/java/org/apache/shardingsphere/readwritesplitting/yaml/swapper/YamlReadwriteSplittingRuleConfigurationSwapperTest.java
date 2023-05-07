/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.apache.shardingsphere.readwritesplitting.yaml.swapper;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class YamlReadwriteSplittingRuleConfigurationSwapperTest {

    @Test
    void assertSwapToYamlConfiguration() {
        YamlReadwriteSplittingRuleConfiguration actual = getSwapper().swapToYamlConfiguration(creatReadwriteSplittingRuleConfiguration());
        assertThat(actual.getDataSources().size(), is(1));
        assertThat(actual.getLoadBalancers().size(), is(1));
    }

    private ReadwriteSplittingRuleConfiguration creatReadwriteSplittingRuleConfiguration() {
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> dataSources = Collections.singletonList(new ReadwriteSplittingDataSourceRuleConfiguration("readwrite", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), "random"));
        Map<String, AlgorithmConfiguration> loadBalancers = Collections.singletonMap("myLoadBalancer", new AlgorithmConfiguration("RANDOM", new Properties()));
        return new ReadwriteSplittingRuleConfiguration(dataSources, loadBalancers);
    }

    @Test
    void assertSwapToObject() {
        ReadwriteSplittingRuleConfiguration actual = getSwapper().swapToObject(creatYamlReadwriteSplittingRuleConfiguration());
        assertThat(actual.getDataSources().size(), is(1));
        assertThat(actual.getLoadBalancers().size(), is(1));
    }

    private YamlReadwriteSplittingRuleConfiguration creatYamlReadwriteSplittingRuleConfiguration() {
        YamlReadwriteSplittingRuleConfiguration result = new YamlReadwriteSplittingRuleConfiguration();
        YamlReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig = new YamlReadwriteSplittingDataSourceRuleConfiguration();
        dataSourceRuleConfig.setReadDataSourceNames(Arrays.asList("read_ds_0", "read_ds_1"));
        dataSourceRuleConfig.setWriteDataSourceName("write_ds");
        result.getDataSources().put("t_readwrite", dataSourceRuleConfig);
        YamlAlgorithmConfiguration algorithmConfig = new YamlAlgorithmConfiguration();
        algorithmConfig.setType("RANDOM");
        result.getLoadBalancers().put("random_loadbalancer", algorithmConfig);
        return result;
    }

    private YamlReadwriteSplittingRuleConfigurationSwapper getSwapper() {
        ReadwriteSplittingRuleConfiguration ruleConfig = mock(ReadwriteSplittingRuleConfiguration.class);
        return (YamlReadwriteSplittingRuleConfigurationSwapper) OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class, Collections.singleton(ruleConfig)).get(ruleConfig);
    }
}
