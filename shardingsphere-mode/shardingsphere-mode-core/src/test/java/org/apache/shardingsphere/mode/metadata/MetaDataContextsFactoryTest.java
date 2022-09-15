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

import org.apache.shardingsphere.infra.config.database.impl.DataSourceGeneratedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.config.database.DatabaseRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.config.global.GlobalRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.config.global.PropertiesPersistService;
import org.apache.shardingsphere.test.fixture.rule.MockedRule;
import org.apache.shardingsphere.test.fixture.rule.MockedRuleConfiguration;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MetaDataContextsFactoryTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataPersistService metaDataPersistService;
    
    @Mock
    private DatabaseMetaDataPersistService databaseMetaDataPersistService;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private JDBCInstanceMetaData jdbcInstanceMetaData;
    
    private final Collection<ShardingSphereRule> rules = new LinkedList<>();
    
    private final Map<String, ShardingSphereDatabase> databases = new HashMap<>();
    
    private MockedStatic<ShardingSphereDatabasesFactory> databasesFactory;
    
    private MockedStatic<GlobalRulesBuilder> globalRulesBuilder;
    
    @Before
    public void setUp() {
        rules.add(new MockedRule());
        databases.put("foo_db", database);
        when(metaDataPersistService.getEffectiveDataSources(eq("foo_db"), Mockito.anyMap())).thenReturn(Collections.singletonMap("foo_ds", new MockedDataSource()));
        DatabaseRulePersistService databaseRulePersistService = mockDatabaseRulePersistService();
        when(metaDataPersistService.getDatabaseRulePersistService()).thenReturn(databaseRulePersistService);
        GlobalRulePersistService globalRulePersistService = mockGlobalRulePersistService();
        when(metaDataPersistService.getGlobalRuleService()).thenReturn(globalRulePersistService);
        PropertiesPersistService propertiesPersistService = mock(PropertiesPersistService.class);
        when(propertiesPersistService.load()).thenReturn(new Properties());
        when(metaDataPersistService.getPropsService()).thenReturn(propertiesPersistService);
        when(metaDataPersistService.getDatabaseMetaDataService()).thenReturn(databaseMetaDataPersistService);
        mockDatabasesFactory();
        mockGlobalRulesBuilder();
    }
    
    private void mockDatabasesFactory() {
        databasesFactory = mockStatic(ShardingSphereDatabasesFactory.class);
        databasesFactory.when(() -> ShardingSphereDatabasesFactory.create(anyMap(), any(), any())).thenReturn(databases);
    }
    
    private void mockGlobalRulesBuilder() {
        globalRulesBuilder = mockStatic(GlobalRulesBuilder.class);
        globalRulesBuilder.when(() -> GlobalRulesBuilder.buildRules(anyCollection(), anyMap(), any(InstanceContext.class), any(ConfigurationProperties.class))).thenReturn(rules);
    }
    
    private DatabaseRulePersistService mockDatabaseRulePersistService() {
        DatabaseRulePersistService result = mock(DatabaseRulePersistService.class);
        when(result.load("foo_db")).thenReturn(Collections.singleton(new MockedRuleConfiguration("database_name")));
        return result;
    }
    
    private GlobalRulePersistService mockGlobalRulePersistService() {
        GlobalRulePersistService result = mock(GlobalRulePersistService.class);
        when(result.load()).thenReturn(Collections.singleton(new MockedRuleConfiguration("global_name")));
        return result;
    }
    
    @Test
    public void assertCreateWithJDBCInstanceMetadata() throws SQLException {
        InstanceContext instanceContext = mock(InstanceContext.class, RETURNS_DEEP_STUBS);
        when(instanceContext.getInstance().getMetaData()).thenReturn(jdbcInstanceMetaData);
        try (MetaDataContexts actual = MetaDataContextsFactory.create(metaDataPersistService, createContextManagerBuilderParameter(), instanceContext);) {
            assertThat(actual.getMetaData().getGlobalRuleMetaData().getRules().size(), is(1));
            assertThat(actual.getMetaData().getGlobalRuleMetaData().getRules().iterator().next(), instanceOf(MockedRule.class));
            assertTrue(actual.getMetaData().getDatabases().containsKey("foo_db"));
            assertThat(actual.getMetaData().getDatabases().size(), is(1));
        }
    }
    
    @Test
    public void assertCreateWithProxyInstanceMetadata() throws SQLException {
        when(databaseMetaDataPersistService.loadAllDatabaseNames()).thenReturn(Collections.singletonList("foo_db"));
        when(metaDataPersistService.getDatabaseMetaDataService()).thenReturn(databaseMetaDataPersistService);
        try (MetaDataContexts actual = MetaDataContextsFactory.create(metaDataPersistService, createContextManagerBuilderParameter(), mock(InstanceContext.class, RETURNS_DEEP_STUBS));) {
            assertThat(actual.getPersistService(), is(metaDataPersistService));
            assertThat(actual.getMetaData().getGlobalRuleMetaData().getRules().size(), is(1));
            assertThat(actual.getMetaData().getGlobalRuleMetaData().getRules().iterator().next(), instanceOf(MockedRule.class));
            assertTrue(actual.getMetaData().getDatabases().containsKey("foo_db"));
            assertThat(actual.getMetaData().getDatabases().size(), is(1));
        }
    }
    
    private ContextManagerBuilderParameter createContextManagerBuilderParameter() {
        return new ContextManagerBuilderParameter(null,
                Collections.singletonMap("foo_db", mock(DataSourceGeneratedDatabaseConfiguration.class)), Collections.emptyList(), new Properties(), Collections.emptyList(), null, false);
    }
    
    @After
    public void tearDown() {
        databasesFactory.close();
        globalRulesBuilder.close();
    }
}
