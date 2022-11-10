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

package org.apache.shardingsphere.infra.rule.builder.global;

import org.apache.shardingsphere.infra.rule.builder.fixture.FixtureGlobalRuleBuilder;
import org.apache.shardingsphere.infra.rule.builder.fixture.FixtureGlobalRuleConfiguration;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class GlobalRuleBuilderFactoryTest {
    
    @Test
    public void assertGetInstanceMap() {
        FixtureGlobalRuleConfiguration ruleConfig = new FixtureGlobalRuleConfiguration();
        assertThat(GlobalRuleBuilderFactory.getInstanceMap(Collections.singletonList(ruleConfig)).get(ruleConfig), instanceOf(FixtureGlobalRuleBuilder.class));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetInstancesWithoutAssignedGlobalRuleBuilderClasses() {
        Collection<GlobalRuleBuilder> actual = GlobalRuleBuilderFactory.getInstances(Collections.emptyList());
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), instanceOf(FixtureGlobalRuleBuilder.class));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void assertGetInstancesWithAssignedGlobalRuleBuilderClasses() {
        Collection<GlobalRuleBuilder> builders = Collections.singleton(mock(FixtureGlobalRuleBuilder.class));
        assertTrue(GlobalRuleBuilderFactory.getInstances(builders.stream().map(each -> (Class<GlobalRuleBuilder>) each.getClass()).collect(Collectors.toSet())).isEmpty());
    }
}
