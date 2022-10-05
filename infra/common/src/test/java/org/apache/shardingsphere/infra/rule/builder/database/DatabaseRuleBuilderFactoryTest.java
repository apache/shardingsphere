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

package org.apache.shardingsphere.infra.rule.builder.database;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.infra.rule.builder.fixture.FixtureDatabaseRuleBuilder;
import org.apache.shardingsphere.infra.rule.builder.fixture.FixtureDatabaseRuleConfiguration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;

public final class DatabaseRuleBuilderFactoryTest {
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetInstances() {
        Iterator<DatabaseRuleBuilder> actual = DatabaseRuleBuilderFactory.getInstances().iterator();
        assertThat(actual.next(), instanceOf(FixtureDatabaseRuleBuilder.class));
        assertFalse(actual.hasNext());
    }
    
    @Test
    public void assertGetInstanceMap() {
        FixtureDatabaseRuleConfiguration ruleConfig = new FixtureDatabaseRuleConfiguration();
        assertThat(DatabaseRuleBuilderFactory.getInstanceMap(Collections.singleton(ruleConfig)).get(ruleConfig), instanceOf(FixtureDatabaseRuleBuilder.class));
    }
    
    @Test
    public void assertGetInstanceMapWithComparator() {
        Iterator<RuleConfiguration> actual = DatabaseRuleBuilderFactory.getInstanceMap(
                Arrays.asList(new FixtureDatabaseRuleConfiguration(), new FixtureRuleConfiguration()), Comparator.naturalOrder()).keySet().iterator();
        assertThat(actual.next(), instanceOf(FixtureDatabaseRuleConfiguration.class));
        assertFalse(actual.hasNext());
    }
}
