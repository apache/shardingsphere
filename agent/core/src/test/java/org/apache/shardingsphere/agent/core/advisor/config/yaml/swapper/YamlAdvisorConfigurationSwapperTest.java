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

package org.apache.shardingsphere.agent.core.advisor.config.yaml.swapper;

import net.bytebuddy.description.method.MethodDescription;
import org.apache.shardingsphere.agent.core.advisor.config.AdvisorConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.MethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlPointcutConfiguration;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class YamlAdvisorConfigurationSwapperTest {
    
    @Test
    void assertSwapWithPointcut() throws NoSuchMethodException {
        YamlAdvisorConfiguration yamlConfig = new YamlAdvisorConfiguration();
        yamlConfig.setTarget(getClass().getName());
        Object adviceMock = mock(Object.class);
        yamlConfig.setAdvice(adviceMock.getClass().getName());
        YamlPointcutConfiguration pointcutConfig = new YamlPointcutConfiguration();
        pointcutConfig.setType("method");
        pointcutConfig.setName("call");
        pointcutConfig.setParamLength(1);
        yamlConfig.getPointcuts().add(pointcutConfig);
        AdvisorConfiguration actual = YamlAdvisorConfigurationSwapper.swap(yamlConfig, "PLUGIN");
        assertThat(actual.getTargetClassName(), is(getClass().getName()));
        assertThat(actual.getAdvisors().size(), is(1));
        MethodAdvisorConfiguration advisorConfig = actual.getAdvisors().iterator().next();
        assertTrue(advisorConfig.getPointcut().matches(new MethodDescription.ForLoadedMethod(YamlAdvisorConfigurationSwapperTest.class.getDeclaredMethod("call", String.class))));
        assertThat(advisorConfig.getAdviceClassName(), is(adviceMock.getClass().getName()));
        assertThat(advisorConfig.getPluginType(), is("PLUGIN"));
    }
    
    @SuppressWarnings("unused")
    private String call(final String input) {
        return input;
    }
}
