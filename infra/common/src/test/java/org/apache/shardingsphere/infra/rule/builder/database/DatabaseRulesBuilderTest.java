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

import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.fixture.FixtureRule;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.fixture.FixtureDatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.rule.builder.fixture.ToggleFixtureDatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.rule.builder.fixture.ToggleFixtureRule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseRulesBuilderTest {
    
    private static final ResourceMetaData EMPTY_RESOURCE_META_DATA = new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap());
    
    @Test
    void assertBuildMultipleRules() {
        List<ShardingSphereRule> actual = new ArrayList<>(DatabaseRulesBuilder.build("foo_db", null,
                new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.singleton(new FixtureRuleConfiguration())), null, EMPTY_RESOURCE_META_DATA));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), isA(FixtureRule.class));
    }
    
    @Test
    void assertBuildSingleRule() {
        ShardingSphereRule actual = DatabaseRulesBuilder.build("foo_db", null, Collections.emptyList(), new FixtureDatabaseRuleConfiguration(), null, EMPTY_RESOURCE_META_DATA);
        assertThat(actual, isA(FixtureRule.class));
    }
    
    @Test
    void assertBuildWithEmptyDatabaseRuleConfiguration() {
        List<ShardingSphereRule> actual = new ArrayList<>(DatabaseRulesBuilder.build("foo_db", null,
                new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.singleton(new ToggleFixtureDatabaseRuleConfiguration(true))), null, EMPTY_RESOURCE_META_DATA));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), isA(FixtureRule.class));
    }
    
    @Test
    void assertBuildWithNonEmptyDatabaseRuleConfiguration() {
        List<ShardingSphereRule> actual = new ArrayList<>(DatabaseRulesBuilder.build("foo_db", null,
                new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.singleton(new ToggleFixtureDatabaseRuleConfiguration(false))), null, EMPTY_RESOURCE_META_DATA));
        assertThat(actual.size(), is(2));
        assertTrue(actual.stream().anyMatch(ToggleFixtureRule.class::isInstance));
    }
}
