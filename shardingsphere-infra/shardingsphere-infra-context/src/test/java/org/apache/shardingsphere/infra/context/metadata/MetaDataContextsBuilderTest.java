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

import com.google.common.collect.Lists;
import org.apache.shardingsphere.authority.api.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.fixture.FixtureRule;
import org.apache.shardingsphere.infra.context.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.single.SingleTableRule;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class MetaDataContextsBuilderTest {
    
    @Test
    public void assertBuildWithoutConfiguration() throws SQLException {
        MetaDataContexts actual = new MetaDataContextsBuilder(Collections.emptyMap(), Collections.emptyMap(), null).build();
        assertTrue(actual.getAllSchemaNames().isEmpty());
        assertTrue(actual.getProps().getProps().isEmpty());
    }
    
    @Test
    public void assertBuildWithConfigurationsButWithoutDataSource() throws SQLException {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.EXECUTOR_SIZE.getKey(), "1");
        MetaDataContexts actual = new MetaDataContextsBuilder(
                Collections.singletonMap("logic_db", Collections.emptyMap()), Collections.singletonMap("logic_db", Lists.newArrayList(new FixtureRuleConfiguration())), props).build();
        assertRules(actual);
        assertTrue(actual.getMetaData("logic_db").getResource().getDataSources().isEmpty());
        assertThat(actual.getProps().getProps().size(), is(1));
        assertThat(actual.getProps().getValue(ConfigurationPropertyKey.EXECUTOR_SIZE), is(1));
    }
    
    @Test
    public void assertBuildWithConfigurationsAndDataSources() throws SQLException {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.EXECUTOR_SIZE.getKey(), "1");
        MetaDataContexts actual = new MetaDataContextsBuilder(Collections.singletonMap("logic_db", Collections.singletonMap("ds", new MockedDataSource())),
                Collections.singletonMap("logic_db", Lists.newArrayList(new FixtureRuleConfiguration())), props).build();
        assertRules(actual);
        assertDataSources(actual);
        assertThat(actual.getProps().getProps().size(), is(1));
        assertThat(actual.getProps().getValue(ConfigurationPropertyKey.EXECUTOR_SIZE), is(1));
    }

    @Test
    public void assertBuildWithAuthorityRuleConfigurations() throws SQLException {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.EXECUTOR_SIZE.getKey(), "1");
        ShardingSphereUser user = new ShardingSphereUser("root", "root", "");
        AuthorityRuleConfiguration authorityRuleConfig = new AuthorityRuleConfiguration(Collections.singleton(user), null);

        MetaDataContexts actual = new MetaDataContextsBuilder(
                Collections.singletonMap("logic_db", Collections.emptyMap()), Collections.singletonMap("logic_db",
                Lists.newArrayList(new FixtureRuleConfiguration())), Collections.singleton(authorityRuleConfig), props).build();
        assertRules(actual);
        assertTrue(actual.getMetaData("logic_db").getResource().getDataSources().isEmpty());
        assertThat(actual.getProps().getProps().size(), is(1));
        assertThat(actual.getProps().getValue(ConfigurationPropertyKey.EXECUTOR_SIZE), is(1));
    }
    
    private void assertRules(final MetaDataContexts actual) {
        Collection<ShardingSphereRule> rules = actual.getMetaData("logic_db").getRuleMetaData().getRules();
        assertThat(rules.size(), is(2));
        Iterator<ShardingSphereRule> iterator = rules.iterator();
        assertThat(iterator.next(), instanceOf(FixtureRule.class));
        assertThat(iterator.next(), instanceOf(SingleTableRule.class));
    }
    
    private void assertDataSources(final MetaDataContexts actual) {
        assertThat(actual.getMetaData("logic_db").getResource().getDataSources().size(), is(1));
        assertThat(actual.getMetaData("logic_db").getResource().getDataSources().get("ds"), instanceOf(MockedDataSource.class));
    }
}
