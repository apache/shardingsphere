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

package org.apache.shardingsphere.infra.yaml.config.swapper;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.fixture.RuleConfigurationFixture;
import org.apache.shardingsphere.infra.yaml.config.swapper.fixture.YamlRuleConfigurationSwapperFixture;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlRuleConfigurationSwapperFactoryTest {
    
    @Test
    public void newInstanceMapByRuleConfigurations() {
        RuleConfigurationFixture ruleConfigurationFixture = new RuleConfigurationFixture();
        Map<RuleConfiguration, YamlRuleConfigurationSwapper> yamlRuleConfigurationSwapperMap = YamlRuleConfigurationSwapperFactory
                .getInstanceMapByRuleConfigurations(Collections.singletonList(ruleConfigurationFixture));
        assertTrue(yamlRuleConfigurationSwapperMap.get(ruleConfigurationFixture) instanceof YamlRuleConfigurationSwapperFixture);
    }
    
    @Test
    public void newInstanceMapByRuleConfigurationClasses() {
        Map<Class<?>, YamlRuleConfigurationSwapper> yamlRuleConfigurationSwapperMap = YamlRuleConfigurationSwapperFactory
                .getInstanceMapByRuleConfigurationClasses(Collections.singletonList(RuleConfigurationFixture.class));
        assertTrue(yamlRuleConfigurationSwapperMap.get(RuleConfigurationFixture.class) instanceof YamlRuleConfigurationSwapperFixture);
    }
    
    @Test
    public void newInstances() {
        Collection<YamlRuleConfigurationSwapper> yamlRuleConfigurationSwappers = YamlRuleConfigurationSwapperFactory.getAllInstances();
        assertThat(yamlRuleConfigurationSwappers.size(), is(1));
        assertTrue(yamlRuleConfigurationSwappers.stream().findFirst().isPresent());
        assertTrue(yamlRuleConfigurationSwappers.stream().findFirst().get() instanceof YamlRuleConfigurationSwapperFixture);
    }
}
