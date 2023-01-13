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

import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.core.advisor.config.AdvisorConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.MethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlAdvisorsConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.fixture.YamlAdviceFixture;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.fixture.YamlTargetObjectFixture;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class YamlAdvisorsConfigurationSwapperTest {
    
    @Test
    public void assertSwapToObject() {
        Collection<AdvisorConfiguration> actual = YamlAdvisorsConfigurationSwapper.swap(
                new Yaml().loadAs(getClass().getResourceAsStream("/META-INF/conf/advisors.yaml"), YamlAdvisorsConfiguration.class));
        assertThat(actual.size(), is(1));
        assertAdvisorConfiguration(actual.iterator().next());
    }
    
    private void assertAdvisorConfiguration(final AdvisorConfiguration actual) {
        assertThat(actual.getTargetClassName(), is(YamlTargetObjectFixture.class.getName()));
        assertThat(actual.getAdvisors().size(), is(8));
        for (MethodAdvisorConfiguration each : actual.getAdvisors()) {
            assertThat(each.getAdviceClassName(), is(YamlAdviceFixture.class.getName()));
        }
        List<MethodAdvisorConfiguration> actualAdvisorConfig = new ArrayList<>(actual.getAdvisors());
        assertThat(actualAdvisorConfig.get(0).getPointcut(), is(ElementMatchers.isConstructor()));
        assertThat(actualAdvisorConfig.get(1).getPointcut(), is(ElementMatchers.isConstructor().and(ElementMatchers.takesArgument(0, ElementMatchers.named("java.lang.String")))));
        assertThat(actualAdvisorConfig.get(2).getPointcut(), is(ElementMatchers.named("call")));
        assertThat(actualAdvisorConfig.get(3).getPointcut(), is(ElementMatchers.named("call").and(ElementMatchers.takesArgument(0, ElementMatchers.named("java.lang.String")))));
        assertThat(actualAdvisorConfig.get(4).getPointcut(), is(ElementMatchers.named("call")
                .and(ElementMatchers.takesArgument(0, ElementMatchers.named("java.lang.String"))).and(ElementMatchers.takesArgument(1, ElementMatchers.named("java.lang.String")))));
        assertThat(actualAdvisorConfig.get(5).getPointcut(), is(ElementMatchers.named("staticCall")));
        assertThat(actualAdvisorConfig.get(6).getPointcut(), is(ElementMatchers.named("staticCall").and(ElementMatchers.takesArgument(0, ElementMatchers.named("java.lang.String")))));
        assertThat(actualAdvisorConfig.get(7).getPointcut(), is(ElementMatchers.named("staticCall")
                .and(ElementMatchers.takesArgument(0, ElementMatchers.named("java.lang.String"))).and(ElementMatchers.takesArgument(1, ElementMatchers.named("java.lang.String")))));
        
    }
}
