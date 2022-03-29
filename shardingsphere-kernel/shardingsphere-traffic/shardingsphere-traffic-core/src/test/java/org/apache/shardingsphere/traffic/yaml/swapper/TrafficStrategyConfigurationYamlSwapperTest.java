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

package org.apache.shardingsphere.traffic.yaml.swapper;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.yaml.config.YamlTrafficStrategyConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class TrafficStrategyConfigurationYamlSwapperTest {

    private static final String NAME = "testName";

    private static final String TEST_LABEL_ONE = "testLabelOne";

    private static final String TEST_LABEL_TWO = "testLabelTwo";

    private static final String ALGORITHM_NAME = "algorithmName";

    private static final String LOAD_BALANCER_NAME = "testLoadBalancerName";

    private static final List<String> LABELS = Lists.newArrayList(TEST_LABEL_ONE, TEST_LABEL_TWO);

    private final TrafficStrategyConfigurationYamlSwapper yamlSwapper = new TrafficStrategyConfigurationYamlSwapper();

    @Test
    public void swapToYamlConfiguration() {
        YamlTrafficStrategyConfiguration yamlTrafficStrategyConfiguration = yamlSwapper.swapToYamlConfiguration(createTrafficStrategyConfiguration());
        assertThat(yamlTrafficStrategyConfiguration.getName(), is(NAME));
        assertThat(yamlTrafficStrategyConfiguration.getLabels(), is(LABELS));
        assertThat(yamlTrafficStrategyConfiguration.getAlgorithmName(), is(ALGORITHM_NAME));
        assertThat(yamlTrafficStrategyConfiguration.getLoadBalancerName(), is(LOAD_BALANCER_NAME));
    }

    private TrafficStrategyConfiguration createTrafficStrategyConfiguration() {
        return new TrafficStrategyConfiguration(NAME, LABELS, ALGORITHM_NAME, LOAD_BALANCER_NAME);
    }

    @Test
    public void swapToObject() {
        TrafficStrategyConfiguration trafficStrategyConfiguration = yamlSwapper.swapToObject(createYamlTrafficStrategyConfiguration());
        assertThat(trafficStrategyConfiguration.getName(), is(NAME));
        assertThat(trafficStrategyConfiguration.getLabels(), is(LABELS));
        assertThat(trafficStrategyConfiguration.getAlgorithmName(), is(ALGORITHM_NAME));
        assertThat(trafficStrategyConfiguration.getLoadBalancerName(), is(LOAD_BALANCER_NAME));
    }

    private YamlTrafficStrategyConfiguration createYamlTrafficStrategyConfiguration() {
        YamlTrafficStrategyConfiguration result = new YamlTrafficStrategyConfiguration();
        result.setName(NAME);
        result.setLabels(LABELS);
        result.setAlgorithmName(ALGORITHM_NAME);
        result.setLoadBalancerName(LOAD_BALANCER_NAME);
        return result;
    }
}
