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

package org.apache.shardingsphere.infra.yaml.config.swapper.rule;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.fixture.YamlRuleConfigurationSwapperFixture;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlRuleConfigurationSwapperFactoryTest {
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetInstanceMapByRuleConfigurations() {
        FixtureRuleConfiguration ruleConfig = new FixtureRuleConfiguration();
        Map<RuleConfiguration, YamlRuleConfigurationSwapper> actual = YamlRuleConfigurationSwapperFactory.getInstanceMapByRuleConfigurations(Collections.singletonList(ruleConfig));
        assertThat(actual.get(ruleConfig), instanceOf(YamlRuleConfigurationSwapperFixture.class));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetInstanceMapByRuleConfigurationClasses() {
        Map<Class<?>, YamlRuleConfigurationSwapper> actual = YamlRuleConfigurationSwapperFactory.getInstanceMapByRuleConfigurationClasses(Collections.singletonList(FixtureRuleConfiguration.class));
        assertThat(actual.get(FixtureRuleConfiguration.class), instanceOf(YamlRuleConfigurationSwapperFixture.class));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetAllInstances() {
        Collection<YamlRuleConfigurationSwapper> actual = YamlRuleConfigurationSwapperFactory.getAllInstances();
        assertThat(actual.size(), is(1));
        assertTrue(actual.stream().findFirst().isPresent());
        assertThat(actual.stream().findFirst().get(), instanceOf(YamlRuleConfigurationSwapperFixture.class));
    }
}
