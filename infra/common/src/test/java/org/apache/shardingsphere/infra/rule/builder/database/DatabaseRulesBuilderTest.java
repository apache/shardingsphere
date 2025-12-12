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
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.fixture.FixtureDatabaseRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;

class DatabaseRulesBuilderTest {
    
    @Test
    void assertBuildMultipleRules() {
        List<ShardingSphereRule> actual = new ArrayList<>(DatabaseRulesBuilder.build("foo_db", mock(),
                new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.singleton(new FixtureRuleConfiguration())), mock(), mock()));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), isA(FixtureRule.class));
    }
    
    @Test
    void assertBuildSingleRule() {
        ShardingSphereRule actual = DatabaseRulesBuilder.build("foo_db", mock(), Collections.emptyList(), new FixtureDatabaseRuleConfiguration(), mock(), mock());
        assertThat(actual, isA(FixtureRule.class));
    }
}
