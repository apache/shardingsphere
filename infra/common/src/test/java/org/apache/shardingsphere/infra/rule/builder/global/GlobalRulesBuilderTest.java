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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabaseFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.fixture.FixtureGlobalRule;
import org.apache.shardingsphere.infra.rule.builder.fixture.FixtureGlobalRuleConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;

class GlobalRulesBuilderTest {
    
    @Test
    void assertBuildRules() {
        Collection<ShardingSphereRule> rules = GlobalRulesBuilder.buildRules(Collections.singletonList(new FixtureGlobalRuleConfiguration()), Collections.singleton(buildDatabase()), mock());
        assertThat(rules.size(), is(1));
        assertThat(rules.iterator().next(), isA(FixtureGlobalRule.class));
    }
    
    @Test
    void assertBuildSingleRules() {
        Collection<ShardingSphereRule> rules = GlobalRulesBuilder.buildSingleRules(new FixtureGlobalRuleConfiguration(), Collections.singleton(buildDatabase()), mock());
        assertThat(rules.size(), is(1));
    }
    
    private ShardingSphereDatabase buildDatabase() {
        return ShardingSphereDatabaseFactory.create("foo_db", TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), new ConfigurationProperties(new Properties()));
    }
}
