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

package org.apache.shardingsphere.mode.metadata.factory;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.fixture.rule.MockedRule;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ShardingSphereDatabasesFactory.class, GlobalRulesBuilder.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class MetaDataContextsFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataPersistFacade metaDataPersistFacade;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PersistRepository repository;
    
    @BeforeEach
    void setUp() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(databaseType);
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        when(ShardingSphereDatabasesFactory.create(anyMap(), anyMap(), any(), any())).thenReturn(Collections.singleton(database));
        when(GlobalRulesBuilder.buildRules(anyCollection(), anyCollection(), any(ConfigurationProperties.class))).thenReturn(Collections.singleton(new MockedRule()));
        when(metaDataPersistFacade.getRepository()).thenReturn(repository);
    }
    
    @Test
    void assertCreateWithJDBCInstanceMetaData() throws SQLException {
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(computeNodeInstanceContext.getInstance().getMetaData()).thenReturn(mock(JDBCInstanceMetaData.class));
        when(repository.getChildrenKeys("/metadata")).thenReturn(Collections.singletonList("foo_db"));
        when(repository.getChildrenKeys("/metadata/foo_db/data_sources/units")).thenReturn(Collections.emptyList());
        when(repository.getChildrenKeys("/rules")).thenReturn(Collections.singletonList("global_fixture"));
        when(repository.query("/rules/global_fixture/active_version")).thenReturn(String.valueOf(0));
        when(repository.query("/rules/global_fixture/versions/0")).thenReturn("name: global_name");
        when(repository.getChildrenKeys("/statistics/databases")).thenReturn(Collections.emptyList());
        MetaDataContexts actual = new MetaDataContextsFactory(metaDataPersistFacade, computeNodeInstanceContext).create(createContextManagerBuilderParameter());
        assertThat(actual.getMetaData().getGlobalRuleMetaData().getRules().size(), is(1));
        assertThat(actual.getMetaData().getGlobalRuleMetaData().getRules().iterator().next(), isA(MockedRule.class));
        assertTrue(actual.getMetaData().containsDatabase("foo_db"));
        assertThat(actual.getMetaData().getAllDatabases().size(), is(1));
    }
    
    @Test
    void assertCreateWithProxyInstanceMetaData() throws SQLException {
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(computeNodeInstanceContext.getInstance().getMetaData()).thenReturn(mock(ProxyInstanceMetaData.class));
        when(repository.getChildrenKeys("/metadata")).thenReturn(Collections.singletonList("foo_db"));
        when(repository.getChildrenKeys("/metadata/foo_db/data_sources/units")).thenReturn(Collections.emptyList());
        when(repository.getChildrenKeys("/rules")).thenReturn(Collections.singletonList("global_fixture"));
        when(repository.query("/rules/global_fixture/active_version")).thenReturn(String.valueOf(0));
        when(repository.query("/rules/global_fixture/versions/0")).thenReturn("name: global_name");
        when(repository.getChildrenKeys("/statistics/databases")).thenReturn(Collections.emptyList());
        MetaDataContexts actual = new MetaDataContextsFactory(metaDataPersistFacade, computeNodeInstanceContext).create(createContextManagerBuilderParameter());
        assertThat(actual.getMetaData().getGlobalRuleMetaData().getRules().size(), is(1));
        assertThat(actual.getMetaData().getGlobalRuleMetaData().getRules().iterator().next(), isA(MockedRule.class));
        assertTrue(actual.getMetaData().containsDatabase("foo_db"));
        assertThat(actual.getMetaData().getAllDatabases().size(), is(1));
    }
    
    private ContextManagerBuilderParameter createContextManagerBuilderParameter() {
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(Collections.singletonMap("foo", new MockedDataSource()), Collections.emptyList());
        return new ContextManagerBuilderParameter(null, Collections.singletonMap("foo_db", databaseConfig), Collections.emptyMap(),
                Collections.emptyList(), new Properties(), Collections.emptyList(), null);
    }
}
