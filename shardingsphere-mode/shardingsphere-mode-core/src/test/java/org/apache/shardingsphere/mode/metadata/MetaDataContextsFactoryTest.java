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
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.config.database.DatabaseRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.config.global.GlobalRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.config.global.PropertiesPersistService;
import org.apache.shardingsphere.test.fixture.rule.MockedRuleConfiguration;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MetaDataContextsFactoryTest {
    
    @Mock
    private MetaDataPersistService metaDataPersistService;
    
    @Mock
    private DatabaseMetaDataPersistService databaseMetaDataPersistService;
    
    @Before
    public void setUp() {
        when(metaDataPersistService.getEffectiveDataSources(eq("foo_db"), Mockito.anyMap())).thenReturn(Collections.singletonMap("foo_ds", new MockedDataSource()));
        DatabaseRulePersistService databaseRulePersistService = mockDatabaseRulePersistService();
        when(metaDataPersistService.getDatabaseRulePersistService()).thenReturn(databaseRulePersistService);
        GlobalRulePersistService globalRulePersistService = mockGlobalRulePersistService();
        when(metaDataPersistService.getGlobalRuleService()).thenReturn(globalRulePersistService);
        PropertiesPersistService propertiesPersistService = mock(PropertiesPersistService.class);
        when(propertiesPersistService.load()).thenReturn(new Properties());
        when(metaDataPersistService.getPropsService()).thenReturn(propertiesPersistService);
        when(metaDataPersistService.getDatabaseMetaDataService()).thenReturn(databaseMetaDataPersistService);
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
    public void createFactoryWithJDBCInstanceMetadata() throws SQLException {
        MetaDataContexts actual = MetaDataContextsFactory.create(metaDataPersistService, createContextManagerBuilderParameter(), mock(InstanceContext.class, RETURNS_DEEP_STUBS));
        assertThat(actual.getPersistService(), is(metaDataPersistService));
        // TODO assert metaData
    }
    
    @Test
    public void createFactoryWithProxyInstanceMetadata() throws SQLException {
        when(databaseMetaDataPersistService.loadAllDatabaseNames()).thenReturn(Collections.singletonList("foo_db"));
        when(metaDataPersistService.getDatabaseMetaDataService()).thenReturn(databaseMetaDataPersistService);
        MetaDataContexts actual = MetaDataContextsFactory.create(metaDataPersistService, createContextManagerBuilderParameter(), mock(InstanceContext.class, RETURNS_DEEP_STUBS));
        assertThat(actual.getPersistService(), is(metaDataPersistService));
        // TODO assert metaData
    }
    
    private ContextManagerBuilderParameter createContextManagerBuilderParameter() {
        return new ContextManagerBuilderParameter(null,
                Collections.singletonMap("foo_db", mock(DataSourceGeneratedDatabaseConfiguration.class)), Collections.emptyList(), new Properties(), Collections.emptyList(), null);
    }
}
