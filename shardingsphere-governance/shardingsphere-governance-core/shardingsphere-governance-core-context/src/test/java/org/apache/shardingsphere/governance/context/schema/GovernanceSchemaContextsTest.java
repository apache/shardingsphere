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

package org.apache.shardingsphere.governance.context.schema;

import org.apache.shardingsphere.governance.core.config.ConfigCenter;
import org.apache.shardingsphere.governance.core.event.model.auth.AuthenticationChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.metadata.MetaDataChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.props.PropertiesChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.schema.SchemaAddedEvent;
import org.apache.shardingsphere.governance.core.event.model.schema.SchemaDeletedEvent;
import org.apache.shardingsphere.governance.core.facade.GovernanceFacade;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.core.registry.event.CircuitStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.schema.GovernanceSchema;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.schema.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.rule.event.RuleChangedEvent;
import org.apache.shardingsphere.infra.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.schema.model.schema.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.jdbc.test.MockedDataSource;
import org.apache.shardingsphere.replicaquery.rule.ReplicaQueryRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public final class GovernanceSchemaContextsTest {
    
    private final Authentication authentication = new Authentication();
    
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
    private ShardingSphereMetaData metaData;
    
    @Mock
    private ReplicaQueryRule replicaQueryRule;
    
    private GovernanceSchemaContexts governanceSchemaContexts;
    
    @Before
    public void setUp() {
        when(databaseType.getName()).thenReturn("H2");
        when(databaseType.getDataSourceMetaData(any(), any())).thenReturn(mock(DataSourceMetaData.class));
        when(governanceFacade.getRegistryCenter()).thenReturn(registryCenter);
        when(governanceFacade.getConfigCenter()).thenReturn(configCenter);
        when(registryCenter.loadDisabledDataSources("schema")).thenReturn(Collections.singletonList("schema.ds_1"));
        governanceSchemaContexts = new GovernanceSchemaContexts(new StandardSchemaContexts(createMetaDataMap(), mock(ExecutorKernel.class), authentication, props, databaseType), governanceFacade);
    }
    
    private Map<String, ShardingSphereMetaData> createMetaDataMap() {
        when(metaData.getName()).thenReturn("schema");
        when(metaData.getResource()).thenReturn(mock(ShardingSphereResource.class));
        when(metaData.getSchema()).thenReturn(mock(ShardingSphereSchema.class));
        when(metaData.getRuleMetaData().getRules()).thenReturn(Collections.singletonList(replicaQueryRule));
        return Collections.singletonMap("schema", metaData);
    }
    
    @Test
    public void assertGetDatabaseType() {
        assertThat(governanceSchemaContexts.getDatabaseType().getName(), is("H2"));
    }
    
    @Test
    public void assertGetSchemas() {
        assertThat(governanceSchemaContexts.getMetaDataMap().get("schema"), is(metaData));
    }
    
    @Test
    public void assertGetDefaultSchema() {
        assertNull(governanceSchemaContexts.getDefaultMetaData());
    }
    
    @Test
    public void assertGetAuthentication() {
        assertThat(governanceSchemaContexts.getAuthentication(), is(authentication));
    }
    
    @Test
    public void assertGetProps() {
        assertThat(governanceSchemaContexts.getProps(), is(props));
    }
    
    @Test
    public void assertIsCircuitBreak() {
        assertFalse(governanceSchemaContexts.isCircuitBreak());
    }
    
    @Test
    public void assertSchemaAdd() throws SQLException {
        SchemaAddedEvent event = new SchemaAddedEvent("schema_add", new HashMap<>(), new LinkedList<>());
        when(configCenter.loadDataSourceConfigurations("schema_add")).thenReturn(getDataSourceConfigurations());
        governanceSchemaContexts.renew(event);
        assertNotNull(governanceSchemaContexts.getMetaDataMap().get("schema_add"));
        assertNotNull(governanceSchemaContexts.getMetaDataMap().get("schema_add").getResource().getDataSources());
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
        SchemaDeletedEvent event = new SchemaDeletedEvent("schema");
        governanceSchemaContexts.renew(event);
        assertNull(governanceSchemaContexts.getMetaDataMap().get("schema"));
    }
    
    @Test
    public void assertPropertiesChanged() {
        Properties properties = new Properties();
        properties.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true");
        PropertiesChangedEvent event = new PropertiesChangedEvent(properties);
        governanceSchemaContexts.renew(event);
        assertThat(governanceSchemaContexts.getProps().getProps().getProperty(ConfigurationPropertyKey.SQL_SHOW.getKey()), is("true"));
    }
    
    @Test
    public void assertAuthenticationChanged() {
        Authentication authentication = new Authentication();
        AuthenticationChangedEvent event = new AuthenticationChangedEvent(authentication);
        governanceSchemaContexts.renew(event);
        assertThat(governanceSchemaContexts.getAuthentication(), is(authentication));
    }
    
    @Test
    public void assertMetaDataChanged() {
        MetaDataChangedEvent event = new MetaDataChangedEvent("schema_changed", mock(PhysicalSchemaMetaData.class));
        governanceSchemaContexts.renew(event);
        assertTrue(governanceSchemaContexts.getMetaDataMap().containsKey("schema"));
        assertFalse(governanceSchemaContexts.getMetaDataMap().containsKey("schema_changed"));
    }
    
    @Test
    public void assertMetaDataChangedWithExistSchema() {
        MetaDataChangedEvent event = new MetaDataChangedEvent("schema", mock(PhysicalSchemaMetaData.class));
        governanceSchemaContexts.renew(event);
        assertThat(governanceSchemaContexts.getMetaDataMap().get("schema"), not(metaData));
    }
    
    @Test
    public void assertRuleConfigurationsChanged() throws SQLException {
        assertThat(governanceSchemaContexts.getMetaDataMap().get("schema"), is(metaData));
        RuleConfigurationsChangedEvent event = new RuleConfigurationsChangedEvent("schema", new LinkedList<>());
        governanceSchemaContexts.renew(event);
        assertThat(governanceSchemaContexts.getMetaDataMap().get("schema"), not(metaData));
    }
    
    @Test
    public void assertDisableStateChanged() {
        DisabledStateChangedEvent event = new DisabledStateChangedEvent(new GovernanceSchema("schema.ds_0"), true);
        governanceSchemaContexts.renew(event);
        verify(replicaQueryRule, times(2)).updateRuleStatus(any(RuleChangedEvent.class));
    }
    
    @Test
    public void assertDataSourceChanged() throws SQLException {
        DataSourceChangedEvent event = new DataSourceChangedEvent("schema", getChangedDataSourceConfigurations());
        governanceSchemaContexts.renew(event);
        assertTrue(governanceSchemaContexts.getMetaDataMap().get("schema").getResource().getDataSources().containsKey("ds_2"));
    }
    
    private Map<String, DataSourceConfiguration> getChangedDataSourceConfigurations() {
        MockedDataSource dataSource = new MockedDataSource();
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(3, 1);
        result.put("primary_ds", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        result.put("ds_1", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        result.put("ds_2", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        return result;
    }
    
    @Test
    public void assertCircuitStateChanged() {
        CircuitStateChangedEvent event = new CircuitStateChangedEvent(true);
        governanceSchemaContexts.renew(event);
        assertTrue(governanceSchemaContexts.isCircuitBreak());
    }
}
