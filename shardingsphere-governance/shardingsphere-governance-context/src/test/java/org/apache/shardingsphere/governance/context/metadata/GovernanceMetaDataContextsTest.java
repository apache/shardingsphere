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

package org.apache.shardingsphere.governance.context.metadata;

import org.apache.shardingsphere.governance.core.config.ConfigCenter;
import org.apache.shardingsphere.governance.core.event.model.auth.AuthenticationChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.metadata.MetaDataAddedEvent;
import org.apache.shardingsphere.governance.core.event.model.metadata.MetaDataDeletedEvent;
import org.apache.shardingsphere.governance.core.event.model.props.PropertiesChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.schema.SchemaChangedEvent;
import org.apache.shardingsphere.governance.core.facade.GovernanceFacade;
import org.apache.shardingsphere.governance.core.lock.LockCenter;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.core.registry.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.schema.GovernanceSchema;
import org.apache.shardingsphere.infra.auth.builtin.DefaultAuthentication;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.event.RuleChangedEvent;
import org.apache.shardingsphere.jdbc.test.MockedDataSource;
import org.apache.shardingsphere.replicaquery.rule.ReplicaQueryRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class GovernanceMetaDataContextsTest {
    
    private final DefaultAuthentication authentication = new DefaultAuthentication();
    
    private final ConfigurationProperties props = new ConfigurationProperties(new Properties());
    
    @Mock
    private DatabaseType databaseType;
    
    @Mock
    private GovernanceFacade governanceFacade;
    
    @Mock
    private RegistryCenter registryCenter;
    
    @Mock
    private ConfigCenter configCenter;
    
    @Mock
    private LockCenter lockCenter;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData metaData;
    
    @Mock
    private ReplicaQueryRule replicaQueryRule;
    
    private GovernanceMetaDataContexts governanceMetaDataContexts;
    
    @Before
    public void setUp() {
        when(databaseType.getName()).thenReturn("H2");
        when(databaseType.getDataSourceMetaData(any(), any())).thenReturn(mock(DataSourceMetaData.class));
        when(governanceFacade.getRegistryCenter()).thenReturn(registryCenter);
        when(governanceFacade.getConfigCenter()).thenReturn(configCenter);
        when(governanceFacade.getLockCenter()).thenReturn(lockCenter);
        when(registryCenter.loadDisabledDataSources("schema")).thenReturn(Collections.singletonList("schema.ds_1"));
        governanceMetaDataContexts = new GovernanceMetaDataContexts(
                new StandardMetaDataContexts(createMetaDataMap(), mock(ExecutorEngine.class), authentication, props, Collections.singletonMap("schema", databaseType)), governanceFacade);
    }
    
    private Map<String, ShardingSphereMetaData> createMetaDataMap() {
        when(metaData.getName()).thenReturn("schema");
        when(metaData.getResource()).thenReturn(mock(ShardingSphereResource.class));
        when(metaData.getSchema()).thenReturn(mock(ShardingSphereSchema.class));
        when(metaData.getRuleMetaData().getRules()).thenReturn(Collections.singletonList(replicaQueryRule));
        return Collections.singletonMap("schema", metaData);
    }
    
    @Test
    public void assertGetMetaDataMap() {
        assertThat(governanceMetaDataContexts.getMetaDataMap().get("schema"), is(metaData));
    }
    
    @Test
    public void assertGetDefaultMetaData() {
        assertNull(governanceMetaDataContexts.getDefaultMetaData());
    }
    
    @Test
    public void assertGetAuthentication() {
        assertThat(governanceMetaDataContexts.getAuthentication(), is(authentication));
    }
    
    @Test
    public void assertGetProps() {
        assertThat(governanceMetaDataContexts.getProps(), is(props));
    }
    
    @Test
    public void assertSchemaAdd() throws SQLException {
        MetaDataAddedEvent event = new MetaDataAddedEvent("schema_add", new HashMap<>(), new LinkedList<>());
        when(configCenter.loadDataSourceConfigurations("schema_add")).thenReturn(getDataSourceConfigurations());
        governanceMetaDataContexts.renew(event);
        assertNotNull(governanceMetaDataContexts.getMetaDataMap().get("schema_add"));
        assertNotNull(governanceMetaDataContexts.getMetaDataMap().get("schema_add").getResource().getDataSources());
    }
    
    private Map<String, DataSourceConfiguration> getDataSourceConfigurations() {
        MockedDataSource dataSource = new MockedDataSource();
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(3, 1);
        result.put("primary_ds", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        result.put("ds_0", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        result.put("ds_1", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        return result;
    }
    
    @Test
    public void assertSchemaDelete() {
        MetaDataDeletedEvent event = new MetaDataDeletedEvent("schema");
        governanceMetaDataContexts.renew(event);
        assertNull(governanceMetaDataContexts.getMetaDataMap().get("schema"));
    }
    
    @Test
    public void assertPropertiesChanged() {
        Properties properties = new Properties();
        properties.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true");
        PropertiesChangedEvent event = new PropertiesChangedEvent(properties);
        governanceMetaDataContexts.renew(event);
        assertThat(governanceMetaDataContexts.getProps().getProps().getProperty(ConfigurationPropertyKey.SQL_SHOW.getKey()), is("true"));
    }
    
    @Test
    public void assertAuthenticationChanged() {
        DefaultAuthentication authentication = new DefaultAuthentication();
        AuthenticationChangedEvent event = new AuthenticationChangedEvent(authentication);
        governanceMetaDataContexts.renew(event);
        assertThat(governanceMetaDataContexts.getAuthentication(), is(authentication));
    }
    
    @Test
    public void assertSchemaChanged() {
        SchemaChangedEvent event = new SchemaChangedEvent("schema_changed", mock(ShardingSphereSchema.class));
        governanceMetaDataContexts.renew(event);
        assertTrue(governanceMetaDataContexts.getMetaDataMap().containsKey("schema"));
        assertFalse(governanceMetaDataContexts.getMetaDataMap().containsKey("schema_changed"));
    }
    
    @Test
    public void assertSchemaChangedWithExistSchema() {
        SchemaChangedEvent event = new SchemaChangedEvent("schema", mock(ShardingSphereSchema.class));
        governanceMetaDataContexts.renew(event);
        assertThat(governanceMetaDataContexts.getMetaDataMap().get("schema"), not(metaData));
    }
    
    @Test
    public void assertRuleConfigurationsChanged() throws SQLException {
        assertThat(governanceMetaDataContexts.getMetaDataMap().get("schema"), is(metaData));
        RuleConfigurationsChangedEvent event = new RuleConfigurationsChangedEvent("schema", new LinkedList<>());
        governanceMetaDataContexts.renew(event);
        assertThat(governanceMetaDataContexts.getMetaDataMap().get("schema"), not(metaData));
    }
    
    @Test
    public void assertDisableStateChanged() {
        DisabledStateChangedEvent event = new DisabledStateChangedEvent(new GovernanceSchema("schema.ds_0"), true);
        governanceMetaDataContexts.renew(event);
        verify(replicaQueryRule, times(2)).updateRuleStatus(any(RuleChangedEvent.class));
    }
    
    @Test
    public void assertDataSourceChanged() throws SQLException {
        DataSourceChangedEvent event = new DataSourceChangedEvent("schema", getChangedDataSourceConfigurations());
        governanceMetaDataContexts.renew(event);
        assertTrue(governanceMetaDataContexts.getMetaDataMap().get("schema").getResource().getDataSources().containsKey("ds_2"));
    }
    
    private Map<String, DataSourceConfiguration> getChangedDataSourceConfigurations() {
        MockedDataSource dataSource = new MockedDataSource();
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(3, 1);
        result.put("primary_ds", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        result.put("ds_1", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        result.put("ds_2", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        return result;
    }
}
