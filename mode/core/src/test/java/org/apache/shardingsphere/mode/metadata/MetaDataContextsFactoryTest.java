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
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.metadata.factory.ExternalMetaDataFactory;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.config.database.rule.DatabaseRulePersistService;
import org.apache.shardingsphere.metadata.persist.service.config.global.GlobalRulePersistService;
import org.apache.shardingsphere.metadata.persist.service.config.global.PropertiesPersistService;
import org.apache.shardingsphere.metadata.persist.service.database.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.test.fixture.infra.rule.MockedRule;
import org.apache.shardingsphere.test.fixture.infra.rule.MockedRuleConfiguration;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ExternalMetaDataFactory.class, GlobalRulesBuilder.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class MetaDataContextsFactoryTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataPersistService metaDataPersistService;
    
    @Mock
    private DatabaseMetaDataPersistService databaseMetaDataPersistService;
    
    @BeforeEach
    void setUp() throws SQLException {
        when(metaDataPersistService.getEffectiveDataSources(eq("foo_db"), anyMap())).thenReturn(Collections.emptyMap());
        DatabaseRulePersistService databaseRulePersistService = mockDatabaseRulePersistService();
        when(metaDataPersistService.getDatabaseRulePersistService()).thenReturn(databaseRulePersistService);
        GlobalRulePersistService globalRulePersistService = mockGlobalRulePersistService();
        when(metaDataPersistService.getGlobalRuleService()).thenReturn(globalRulePersistService);
        PropertiesPersistService propertiesPersistService = mock(PropertiesPersistService.class);
        when(propertiesPersistService.load()).thenReturn(new Properties());
        when(metaDataPersistService.getPropsService()).thenReturn(propertiesPersistService);
        when(metaDataPersistService.getDatabaseMetaDataService()).thenReturn(databaseMetaDataPersistService);
        when(ExternalMetaDataFactory.create(anyMap(), any(), any())).thenReturn(new HashMap<>(Collections.singletonMap("foo_db", mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS))));
        when(GlobalRulesBuilder.buildRules(anyCollection(), anyMap(), any(ConfigurationProperties.class))).thenReturn(Collections.singleton(new MockedRule()));
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
    void assertCreateWithJDBCInstanceMetaData() throws SQLException {
        InstanceContext instanceContext = mock(InstanceContext.class, RETURNS_DEEP_STUBS);
        when(instanceContext.getInstance().getMetaData()).thenReturn(mock(JDBCInstanceMetaData.class));
        try (MetaDataContexts actual = MetaDataContextsFactory.create(metaDataPersistService, createContextManagerBuilderParameter(), instanceContext)) {
            assertThat(actual.getMetaData().getGlobalRuleMetaData().getRules().size(), is(1));
            assertThat(actual.getMetaData().getGlobalRuleMetaData().getRules().iterator().next(), instanceOf(MockedRule.class));
            assertTrue(actual.getMetaData().getDatabases().containsKey("foo_db"));
            assertThat(actual.getMetaData().getDatabases().size(), is(1));
        }
    }
    
    @Test
    void assertCreateWithProxyInstanceMetaData() throws SQLException {
        when(databaseMetaDataPersistService.loadAllDatabaseNames()).thenReturn(Collections.singletonList("foo_db"));
        when(metaDataPersistService.getDatabaseMetaDataService()).thenReturn(databaseMetaDataPersistService);
        try (MetaDataContexts actual = MetaDataContextsFactory.create(metaDataPersistService, createContextManagerBuilderParameter(), mock(InstanceContext.class, RETURNS_DEEP_STUBS))) {
            assertThat(actual.getPersistService(), is(metaDataPersistService));
            assertThat(actual.getMetaData().getGlobalRuleMetaData().getRules().size(), is(1));
            assertThat(actual.getMetaData().getGlobalRuleMetaData().getRules().iterator().next(), instanceOf(MockedRule.class));
            assertTrue(actual.getMetaData().getDatabases().containsKey("foo_db"));
            assertThat(actual.getMetaData().getDatabases().size(), is(1));
        }
    }
    
    private ContextManagerBuilderParameter createContextManagerBuilderParameter() {
        return new ContextManagerBuilderParameter(null,
                Collections.singletonMap("foo_db", mock(DataSourceGeneratedDatabaseConfiguration.class)), Collections.emptyMap(),
                Collections.emptyList(), new Properties(), Collections.emptyList(), null, false);
    }
}
