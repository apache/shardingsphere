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

package org.apache.shardingsphere.infra.context.metadata;

import org.apache.shardingsphere.infra.auth.builtin.DefaultAuthentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.fixture.FixtureRule;
import org.apache.shardingsphere.infra.context.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.jdbc.test.MockedDataSource;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class MetaDataContextsBuilderTest {
    
    @Test
    public void assertBuildWithoutConfiguration() throws SQLException {
        DatabaseType databaseType = DatabaseTypeRegistry.getActualDatabaseType("FixtureDB");
        MetaDataContexts actual = new MetaDataContextsBuilder(Collections.singletonMap(DefaultSchema.LOGIC_NAME, databaseType), Collections.emptyMap(), Collections.emptyMap(), null).build();
        assertThat(actual.getDatabaseType(DefaultSchema.LOGIC_NAME), CoreMatchers.is(databaseType));
        assertTrue(actual.getMetaDataMap().isEmpty());
        assertTrue(((DefaultAuthentication) actual.getAuthentication()).getUsers().isEmpty());
        assertTrue(actual.getProps().getProps().isEmpty());
    }
    
    @Test
    public void assertBuildWithConfigurationsButWithoutDataSource() throws SQLException {
        DatabaseType databaseType = DatabaseTypeRegistry.getActualDatabaseType("FixtureDB");
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.EXECUTOR_SIZE.getKey(), "1");
        MetaDataContexts actual = new MetaDataContextsBuilder(Collections.singletonMap(DefaultSchema.LOGIC_NAME, databaseType), Collections.singletonMap("logic_db", Collections.emptyMap()), 
                Collections.singletonMap("logic_db", Collections.singleton(new FixtureRuleConfiguration())), props).build();
        assertThat(actual.getDatabaseType(DefaultSchema.LOGIC_NAME), CoreMatchers.is(databaseType));
        assertRules(actual);
        assertTrue(actual.getMetaDataMap().get("logic_db").getResource().getDataSources().isEmpty());
        assertTrue(((DefaultAuthentication) actual.getAuthentication()).getUsers().isEmpty());
        assertThat(actual.getProps().getProps().size(), CoreMatchers.is(1));
        assertThat(actual.getProps().getValue(ConfigurationPropertyKey.EXECUTOR_SIZE), CoreMatchers.is(1));
    }
    
    @Test
    public void assertBuildWithConfigurationsAndDataSources() throws SQLException {
        DatabaseType databaseType = DatabaseTypeRegistry.getActualDatabaseType("FixtureDB");
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.EXECUTOR_SIZE.getKey(), "1");
        MetaDataContexts actual = new MetaDataContextsBuilder(
                Collections.singletonMap(DefaultSchema.LOGIC_NAME, databaseType), Collections.singletonMap("logic_db", Collections.singletonMap("ds", new MockedDataSource())),
                Collections.singletonMap("logic_db", Collections.singleton(new FixtureRuleConfiguration())), props).build();
        assertThat(actual.getDatabaseType(DefaultSchema.LOGIC_NAME), CoreMatchers.is(databaseType));
        assertRules(actual);
        assertDataSources(actual);
        assertTrue(((DefaultAuthentication) actual.getAuthentication()).getUsers().isEmpty());
        assertThat(actual.getProps().getProps().size(), CoreMatchers.is(1));
        assertThat(actual.getProps().getValue(ConfigurationPropertyKey.EXECUTOR_SIZE), CoreMatchers.is(1));
    }
    
    private void assertRules(final MetaDataContexts actual) {
        assertThat(actual.getMetaDataMap().get("logic_db").getRuleMetaData().getRules().size(), CoreMatchers.is(1));
        assertThat(actual.getMetaDataMap().get("logic_db").getRuleMetaData().getRules().iterator().next(), CoreMatchers.instanceOf(FixtureRule.class));
    }
    
    private void assertDataSources(final MetaDataContexts actual) {
        assertThat(actual.getMetaDataMap().get("logic_db").getResource().getDataSources().size(), CoreMatchers.is(1));
        assertThat(actual.getMetaDataMap().get("logic_db").getResource().getDataSources().get("ds"), CoreMatchers.instanceOf(MockedDataSource.class));
    }
}
