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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.fixture.FixtureGlobalRule;
import org.apache.shardingsphere.infra.rule.builder.fixture.FixtureGlobalRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class GlobalRulesBuilderTest {
    
    @Test
    void assertBuildRules() {
        Collection<ShardingSphereRule> shardingSphereRules = GlobalRulesBuilder
                .buildRules(Collections.singletonList(new FixtureGlobalRuleConfiguration()), Collections.singletonMap("logic_db", buildDatabase()), mock(ConfigurationProperties.class));
        assertThat(shardingSphereRules.size(), is(1));
    }
    
    @Test
    void assertBuildRulesClassType() {
        Collection<ShardingSphereRule> shardingSphereRules = GlobalRulesBuilder
                .buildRules(Collections.singletonList(new FixtureGlobalRuleConfiguration()), Collections.singletonMap("logic_db", buildDatabase()), mock(ConfigurationProperties.class));
        assertTrue(shardingSphereRules.toArray()[0] instanceof FixtureGlobalRule);
    }
    
    private ShardingSphereDatabase buildDatabase() {
        return ShardingSphereDatabase.create("logic_db", new MySQLDatabaseType(), new ConfigurationProperties(new Properties()));
    }
}
