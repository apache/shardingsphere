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

package org.apache.shardingsphere.infra.metadata.database;

import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.fixture.FixtureDatabaseRule;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class ShardingSphereDatabasesFactoryTest {
    
    @Test
    public void assertCreateSingleDatabase() throws SQLException {
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.singleton(new FixtureRuleConfiguration()));
        ShardingSphereDatabase actual = ShardingSphereDatabasesFactory.create("foo_db", databaseConfig, new ConfigurationProperties(new Properties()), mock(InstanceContext.class));
        assertThat(actual.getName(), is("foo_db"));
        assertThat(actual.getRuleMetaData().getRules().iterator().next(), instanceOf(FixtureDatabaseRule.class));
        assertTrue(actual.getResource().getDataSources().isEmpty());
    }
    
    @Test
    public void assertCreateDatabaseMap() throws SQLException {
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.singleton(new FixtureRuleConfiguration()));
        Map<String, ShardingSphereDatabase> actual = ShardingSphereDatabasesFactory.create(
                Collections.singletonMap("foo_db", databaseConfig), new ConfigurationProperties(new Properties()), mock(InstanceContext.class));
        Collection<ShardingSphereRule> rules = actual.get("foo_db").getRuleMetaData().getRules();
        assertThat(rules.size(), is(1));
        assertThat(rules.iterator().next(), instanceOf(FixtureDatabaseRule.class));
        assertTrue(actual.get("foo_db").getResource().getDataSources().isEmpty());
    }
    
    @Test
    public void assertCreateDatabaseMapWhenConfigUppercaseDatabaseName() throws SQLException {
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.singleton(new FixtureRuleConfiguration()));
        Map<String, ShardingSphereDatabase> actual = ShardingSphereDatabasesFactory.create(
                Collections.singletonMap("FOO_DB", databaseConfig), new ConfigurationProperties(new Properties()), mock(InstanceContext.class));
        Collection<ShardingSphereRule> rules = actual.get("foo_db").getRuleMetaData().getRules();
        assertThat(rules.size(), is(1));
        assertThat(rules.iterator().next(), instanceOf(FixtureDatabaseRule.class));
        assertTrue(actual.get("foo_db").getResource().getDataSources().isEmpty());
    }
}
