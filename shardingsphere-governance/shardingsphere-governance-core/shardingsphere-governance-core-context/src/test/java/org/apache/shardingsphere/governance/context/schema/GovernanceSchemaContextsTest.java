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

import lombok.SneakyThrows;
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
import org.apache.shardingsphere.infra.context.schema.SchemaContext;
import org.apache.shardingsphere.infra.context.schema.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.context.schema.runtime.RuntimeContext;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.rule.event.RuleChangedEvent;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.jdbc.test.MockedDataSource;
import org.apache.shardingsphere.rdl.parser.engine.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.replication.primaryreplica.rule.PrimaryReplicaReplicationRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
    private SchemaContext schemaContext;
    
    @Mock
    private PrimaryReplicaReplicationRule primaryReplicaReplicationRule;
    
    private GovernanceSchemaContexts governanceSchemaContexts;
    
    @Before
    public void setUp() {
        when(databaseType.getName()).thenReturn("H2");
        when(databaseType.getDataSourceMetaData(any(), any())).thenReturn(mock(DataSourceMetaData.class));
        when(governanceFacade.getRegistryCenter()).thenReturn(registryCenter);
        when(governanceFacade.getConfigCenter()).thenReturn(configCenter);
        when(registryCenter.loadDisabledDataSources("schema")).thenReturn(Collections.singletonList("schema.ds_1"));
        governanceSchemaContexts = new GovernanceSchemaContexts(
                new StandardSchemaContexts(createSchemaContextMap(), mock(ShardingSphereSQLParserEngine.class), mock(ExecutorKernel.class), authentication, props, databaseType), governanceFacade);
    }
    
    @SneakyThrows
    private Map<String, SchemaContext> createSchemaContextMap() {
        ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class);
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class);
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        when(schemaContext.getSchema()).thenReturn(shardingSphereSchema);
        when(schemaContext.getSchema().getName()).thenReturn("schema");
        when(schemaContext.getRuntimeContext()).thenReturn(runtimeContext);
        when(shardingSphereSchema.getMetaData()).thenReturn(shardingSphereMetaData);
        when(shardingSphereSchema.getRules()).thenReturn(Collections.singletonList(primaryReplicaReplicationRule));
        return Collections.singletonMap("schema", schemaContext);
    }
    
    @Test
    public void assertGetDatabaseType() {
        assertThat(governanceSchemaContexts.getDatabaseType().getName(), is("H2"));
    }
    
    @Test
    public void assertGetSchemaContexts() {
        assertThat(governanceSchemaContexts.getSchemaContextMap().get("schema"), is(schemaContext));
    }
    
    @Test
    public void assertGetDefaultSchemaContext() {
        assertNull(governanceSchemaContexts.getDefaultSchemaContext());
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
    @SneakyThrows
    public void assertSchemaAdd() {
        SchemaAddedEvent event = new SchemaAddedEvent("schema_add", new HashMap<>(), new LinkedList<>());
        when(configCenter.loadDataSourceConfigurations("schema_add")).thenReturn(getDataSourceConfigurations());
        governanceSchemaContexts.renew(event);
        assertNotNull(governanceSchemaContexts.getSchemaContextMap().get("schema_add"));
        assertNotNull(governanceSchemaContexts.getSchemaContextMap().get("schema_add").getSchema().getDataSources());
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
        assertNull(governanceSchemaContexts.getSchemaContextMap().get("schema"));
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
        MetaDataChangedEvent event = new MetaDataChangedEvent(Collections.singletonList("schema_changed"), mock(RuleSchemaMetaData.class));
        governanceSchemaContexts.renew(event);
        assertTrue(governanceSchemaContexts.getSchemaContextMap().containsKey("schema"));
        assertFalse(governanceSchemaContexts.getSchemaContextMap().containsKey("schema_changed"));
    }
    
    @Test
    public void assertMetaDataChangedWithExistSchema() {
        MetaDataChangedEvent event = new MetaDataChangedEvent(Collections.singletonList("schema"), mock(RuleSchemaMetaData.class));
        governanceSchemaContexts.renew(event);
        assertThat(governanceSchemaContexts.getSchemaContextMap().get("schema"), not(schemaContext));
    }
    
    @Test
    @SneakyThrows
    public void assertRuleConfigurationsChanged() {
        assertThat(governanceSchemaContexts.getSchemaContextMap().get("schema"), is(schemaContext));
        RuleConfigurationsChangedEvent event = new RuleConfigurationsChangedEvent("schema", new LinkedList<>());
        governanceSchemaContexts.renew(event);
        assertThat(governanceSchemaContexts.getSchemaContextMap().get("schema"), not(schemaContext));
    }
    
    @Test
    public void assertDisableStateChanged() {
        DisabledStateChangedEvent event = new DisabledStateChangedEvent(new GovernanceSchema("schema.ds_0"), true);
        governanceSchemaContexts.renew(event);
        verify(primaryReplicaReplicationRule, times(2)).updateRuleStatus(any(RuleChangedEvent.class));
    }
    
    @Test
    @SneakyThrows
    public void assertDataSourceChanged() {
        DataSourceChangedEvent event = new DataSourceChangedEvent("schema", getChangedDataSourceConfigurations());
        governanceSchemaContexts.renew(event);
        assertTrue(governanceSchemaContexts.getSchemaContextMap().get("schema").getSchema().getDataSources().containsKey("ds_2"));
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
