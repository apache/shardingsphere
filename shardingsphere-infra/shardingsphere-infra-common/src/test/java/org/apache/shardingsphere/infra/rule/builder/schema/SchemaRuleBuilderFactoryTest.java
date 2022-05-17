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

package org.apache.shardingsphere.infra.rule.builder.schema;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.fixture.TestRuleConfiguration;
import org.apache.shardingsphere.infra.fixture.TestShardingSphereRuleBuilder;
import org.apache.shardingsphere.infra.rule.builder.fixture.FixtureSchemaRuleBuilder;
import org.apache.shardingsphere.infra.rule.builder.fixture.FixtureSchemaRuleConfiguration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SchemaRuleBuilderFactoryTest {
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetInstances() {
        Collection<SchemaRuleBuilder> actual = SchemaRuleBuilderFactory.getInstances();
        assertThat(actual.size(), is(2));
        Iterator<SchemaRuleBuilder> iterator = actual.iterator();
        assertThat(iterator.next(), instanceOf(TestShardingSphereRuleBuilder.class));
        assertThat(iterator.next(), instanceOf(FixtureSchemaRuleBuilder.class));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetInstanceMap() {
        FixtureSchemaRuleConfiguration configuration = new FixtureSchemaRuleConfiguration();
        Map<RuleConfiguration, SchemaRuleBuilder> actual = SchemaRuleBuilderFactory.getInstanceMap(Collections.singleton(configuration));
        assertThat(actual.get(configuration), instanceOf(FixtureSchemaRuleBuilder.class));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetInstanceMapWithComparator() {
        Collection<RuleConfiguration> ruleConfigs = new ArrayList<>(2);
        ruleConfigs.add(new FixtureSchemaRuleConfiguration());
        ruleConfigs.add(new TestRuleConfiguration());
        Map<RuleConfiguration, SchemaRuleBuilder> actual = SchemaRuleBuilderFactory.getInstanceMap(ruleConfigs, Comparator.naturalOrder());
        Iterator<RuleConfiguration> iterator = actual.keySet().iterator();
        assertThat(iterator.next(), instanceOf(TestRuleConfiguration.class));
        assertThat(iterator.next(), instanceOf(FixtureSchemaRuleConfiguration.class));
    }
}
