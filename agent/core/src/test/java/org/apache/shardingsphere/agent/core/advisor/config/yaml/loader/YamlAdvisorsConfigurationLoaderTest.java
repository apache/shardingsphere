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

package org.apache.shardingsphere.agent.core.advisor.config.yaml.loader;

import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlAdvisorsConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlPointcutConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlPointcutParameterConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.fixture.YamlAdviceFixture;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.fixture.YamlTargetObjectFixture;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlAdvisorsConfigurationLoaderTest {
    
    @Test
    void assertLoad() {
        YamlAdvisorsConfiguration actual = YamlAdvisorsConfigurationLoader.load(getClass().getResourceAsStream("/META-INF/conf/advisors.yaml"));
        assertThat(actual.getAdvisors().size(), is(1));
        assertYamlAdvisorConfiguration(actual.getAdvisors().iterator().next());
    }
    
    private void assertYamlAdvisorConfiguration(final YamlAdvisorConfiguration actual) {
        assertThat(actual.getTarget(), is(YamlTargetObjectFixture.class.getName()));
        assertThat(actual.getAdvice(), is(YamlAdviceFixture.class.getName()));
        assertThat(actual.getPointcuts().size(), is(8));
        List<YamlPointcutConfiguration> actualYamlPointcutConfigs = new ArrayList<>(actual.getPointcuts());
        assertYamlPointcutConfiguration(actualYamlPointcutConfigs.get(0), null, "constructor", Collections.emptyList());
        assertYamlPointcutConfiguration(actualYamlPointcutConfigs.get(1), null, "constructor", Collections.singletonList(createYamlPointcutParameterConfiguration(0, "java.lang.String")));
        assertYamlPointcutConfiguration(actualYamlPointcutConfigs.get(2), "call", "method", Collections.emptyList());
        assertYamlPointcutConfiguration(actualYamlPointcutConfigs.get(3), "call", "method", Collections.singletonList(createYamlPointcutParameterConfiguration(0, "java.lang.String")));
        assertYamlPointcutConfiguration(actualYamlPointcutConfigs.get(4), "call", "method",
                Arrays.asList(createYamlPointcutParameterConfiguration(0, "java.lang.String"), createYamlPointcutParameterConfiguration(1, "java.lang.String")));
        assertYamlPointcutConfiguration(actualYamlPointcutConfigs.get(5), "staticCall", "method", Collections.emptyList());
        assertYamlPointcutConfiguration(actualYamlPointcutConfigs.get(6), "staticCall", "method", Collections.singletonList(createYamlPointcutParameterConfiguration(0, "java.lang.String")));
        assertYamlPointcutConfiguration(actualYamlPointcutConfigs.get(7), "staticCall", "method",
                Arrays.asList(createYamlPointcutParameterConfiguration(0, "java.lang.String"), createYamlPointcutParameterConfiguration(1, "java.lang.String")));
    }
    
    private void assertYamlPointcutConfiguration(final YamlPointcutConfiguration actual,
                                                 final String expectedName, final String expectedType, final List<YamlPointcutParameterConfiguration> expectedParams) {
        assertThat(actual.getName(), is(expectedName));
        assertThat(actual.getType(), is(expectedType));
        assertThat(actual.getParams().size(), is(expectedParams.size()));
        int count = 0;
        for (YamlPointcutParameterConfiguration each : actual.getParams()) {
            assertYamlPointcutParameterConfiguration(each, expectedParams.get(count));
            count++;
        }
    }
    
    private void assertYamlPointcutParameterConfiguration(final YamlPointcutParameterConfiguration actual, final YamlPointcutParameterConfiguration expected) {
        assertThat(actual.getIndex(), is(expected.getIndex()));
        assertThat(actual.getType(), is(expected.getType()));
    }
    
    private YamlPointcutParameterConfiguration createYamlPointcutParameterConfiguration(final int index, final String type) {
        YamlPointcutParameterConfiguration result = new YamlPointcutParameterConfiguration();
        result.setIndex(index);
        result.setType(type);
        return result;
    }
    
    @Test
    void assertLoadEmptyFile() {
        YamlAdvisorsConfiguration actual = YamlAdvisorsConfigurationLoader.load(getClass().getResourceAsStream("/META-INF/conf/empty-advisors.yaml"));
        assertTrue(actual.getAdvisors().isEmpty());
    }
}
