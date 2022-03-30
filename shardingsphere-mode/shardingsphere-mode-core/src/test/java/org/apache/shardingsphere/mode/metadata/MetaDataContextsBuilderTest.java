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

package org.apache.shardingsphere.mode.metadata;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.schema.impl.DataSourceProvidedSchemaConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.metadata.fixture.FixtureRule;
import org.apache.shardingsphere.mode.metadata.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class MetaDataContextsBuilderTest {
    
    @Test
    public void assertBuildWithAuthorityRuleConfigurations() throws SQLException {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE.getKey(), "1");
        ShardingSphereUser user = new ShardingSphereUser("root", "root", "");
        AuthorityRuleConfiguration authorityRuleConfig = new AuthorityRuleConfiguration(Collections.singleton(user),
                new ShardingSphereAlgorithmConfiguration("ALL_PRIVILEGES_PERMITTED", new Properties()));
        MetaDataContextsBuilder builder = new MetaDataContextsBuilder(Collections.singleton(authorityRuleConfig), props);
        builder.addSchema("logic_db", DatabaseTypeRegistry.getDefaultDatabaseType(), 
                new DataSourceProvidedSchemaConfiguration(Collections.emptyMap(), Collections.singletonList(new FixtureRuleConfiguration())), props);
        MetaDataContexts actual = builder.build(mock(MetaDataPersistService.class));
        assertRules(actual);
        assertTrue(actual.getMetaData("logic_db").getResource().getDataSources().isEmpty());
        assertThat(actual.getProps().getProps().size(), is(1));
        assertThat(actual.getProps().getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE), is(1));
    }
    
    @Test
    public void assertBuildWithoutGlobalRuleConfigurations() throws SQLException {
        MetaDataContexts actual = new MetaDataContextsBuilder(Collections.emptyList(), new Properties()).build(mock(MetaDataPersistService.class));
        assertThat(actual.getGlobalRuleMetaData().getRules().size(), is(3));
        assertThat(actual.getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof AuthorityRule).count(), is(1L));
        assertThat(actual.getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof TransactionRule).count(), is(1L));
        assertThat(actual.getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof SQLParserRule).count(), is(1L));
    }
    
    @Test
    public void assertBuildWithEmptyRuleConfigurations() throws SQLException {
        MetaDataContextsBuilder builder = new MetaDataContextsBuilder(Collections.emptyList(), new Properties());
        builder.addSystemSchemas(new MySQLDatabaseType());
        MetaDataContexts actual = builder.build(mock(MetaDataPersistService.class));
        assertThat(actual.getMetaDataMap().size(), is(4));
        assertTrue(actual.getMetaDataMap().containsKey("information_schema"));
        assertTrue(actual.getMetaDataMap().containsKey("performance_schema"));
        assertTrue(actual.getMetaDataMap().containsKey("mysql"));
        assertTrue(actual.getMetaDataMap().containsKey("sys"));
        assertThat(actual.getMetaDataMap().get("information_schema").getRuleMetaData().getRules(), instanceOf(LinkedList.class));
        assertThat(actual.getMetaDataMap().get("performance_schema").getRuleMetaData().getRules(), instanceOf(LinkedList.class));
        assertThat(actual.getMetaDataMap().get("mysql").getRuleMetaData().getRules(), instanceOf(LinkedList.class));
        assertThat(actual.getMetaDataMap().get("sys").getRuleMetaData().getRules(), instanceOf(LinkedList.class));
    }
    
    private void assertRules(final MetaDataContexts actual) {
        Collection<ShardingSphereRule> rules = actual.getMetaData("logic_db").getRuleMetaData().getRules();
        assertThat(rules.size(), is(1));
        assertThat(rules.iterator().next(), instanceOf(FixtureRule.class));
    }
}
