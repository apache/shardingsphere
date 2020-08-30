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

package org.apache.shardingsphere.governance.core.schema;

import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.governance.core.common.event.auth.AuthenticationChangedEvent;
import org.apache.shardingsphere.governance.core.common.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.governance.core.common.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.governance.core.common.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.core.common.event.schema.SchemaAddedEvent;
import org.apache.shardingsphere.governance.core.common.event.schema.SchemaDeletedEvent;
import org.apache.shardingsphere.governance.core.facade.GovernanceFacade;
import org.apache.shardingsphere.governance.core.metadata.MetaDataCenter;
import org.apache.shardingsphere.governance.core.metadata.event.MetaDataChangedEvent;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.core.registry.event.CircuitStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.schema.GovernanceSchema;
import org.apache.shardingsphere.governance.core.schema.fixture.TestGovernanceSchemaContexts;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.SchemaContext;
import org.apache.shardingsphere.infra.context.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.context.runtime.RuntimeContext;
import org.apache.shardingsphere.infra.context.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.rule.event.RuleChangedEvent;
import org.apache.shardingsphere.masterslave.rule.MasterSlaveRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
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
    
    @Mock
    private GovernanceFacade governanceFacade;
    
    @Mock
    private RegistryCenter registryCenter;
    
    @Mock
    private MetaDataCenter metaDataCenter;
    
    @Mock
    private SchemaContext schemaContext;
    
    @Mock
    private MasterSlaveRule masterSlaveRule;
    
    private Authentication authentication = new Authentication();
    
    private ConfigurationProperties configurationProperties = new ConfigurationProperties(new Properties());
    
    private TestGovernanceSchemaContexts governanceSchemaContexts;
    
    @Before
    public void setUp() {
        when(governanceFacade.getRegistryCenter()).thenReturn(registryCenter);
        when(registryCenter.loadDisabledDataSources()).thenReturn(Collections.singletonList("schema.ds_1"));
        when(governanceFacade.getMetaDataCenter()).thenReturn(metaDataCenter);
        governanceSchemaContexts = new TestGovernanceSchemaContexts(new StandardSchemaContexts(getSchemaContextMap(),
                authentication, configurationProperties, new H2DatabaseType()), governanceFacade);
    }
    
    @SneakyThrows(Exception.class)
    private Map<String, SchemaContext> getSchemaContextMap() {
        ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class);
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class);
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        when(schemaContext.getName()).thenReturn("schema");
        when(schemaContext.getSchema()).thenReturn(shardingSphereSchema);
        when(schemaContext.getRuntimeContext()).thenReturn(runtimeContext);
        when(shardingSphereSchema.getMetaData()).thenReturn(shardingSphereMetaData);
        when(shardingSphereSchema.getRules()).thenReturn(Collections.singletonList(masterSlaveRule));
        return Collections.singletonMap("schema", schemaContext);
    }
    
    @Test
    public void assertGetDatabaseType() {
        assertThat(governanceSchemaContexts.getDatabaseType().getName(), is("H2"));
    }
    
    @Test
    public void assertGetSchemaContexts() {
        assertThat(governanceSchemaContexts.getSchemaContexts().get("schema"), is(schemaContext));
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
        assertThat(governanceSchemaContexts.getProps(), is(configurationProperties));
    }
    
    @Test
    public void assertIsCircuitBreak() {
        assertFalse(governanceSchemaContexts.isCircuitBreak());
    }
    
    @Test
    @SneakyThrows(Exception.class)
    public void assertSchemaAdd() {
        SchemaAddedEvent event = new SchemaAddedEvent("schema_add", getDataSourceConfigurations(), new ArrayList<>());
        governanceSchemaContexts.renew(event);
        assertNotNull(governanceSchemaContexts.getSchemaContexts().get("schema_add"));
    }
    
    private Map<String, DataSourceConfiguration> getDataSourceConfigurations() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>();
        result.put("ds_m", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        result.put("ds_0", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        result.put("ds_1", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
        return result;
    }
    
    @Test
    public void assertSchemaDelete() {
        SchemaDeletedEvent event = new SchemaDeletedEvent("schema");
        governanceSchemaContexts.renew(event);
        assertNull(governanceSchemaContexts.getSchemaContexts().get("schema"));
    }
    
    @Test
    public void assertPropertiesChanged() {
        Properties properties = new Properties();
        properties.setProperty("sql.show", "true");
        PropertiesChangedEvent event = new PropertiesChangedEvent(properties);
        governanceSchemaContexts.renew(event);
        assertThat(governanceSchemaContexts.getProps().getProps().getProperty("sql.show"), is("true"));
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
        MetaDataChangedEvent event = new MetaDataChangedEvent(Arrays.asList("schema_changed"), mock(RuleSchemaMetaData.class));
        governanceSchemaContexts.renew(event);
        assertTrue(governanceSchemaContexts.getSchemaContexts().keySet().contains("schema"));
        assertFalse(governanceSchemaContexts.getSchemaContexts().keySet().contains("schema_changed"));
    }
    
    @Test
    public void assertMetaDataChangedWithExistSchema() {
        MetaDataChangedEvent event = new MetaDataChangedEvent(Arrays.asList("schema"), mock(RuleSchemaMetaData.class));
        governanceSchemaContexts.renew(event);
        assertThat(governanceSchemaContexts.getSchemaContexts().get("schema"), not(schemaContext));
    }
    
    @Test
    @SneakyThrows(Exception.class)
    public void assertRuleConfigurationsChanged() {
        assertThat(governanceSchemaContexts.getSchemaContexts().get("schema"), is(schemaContext));
        RuleConfigurationsChangedEvent event = new RuleConfigurationsChangedEvent("schema", new ArrayList<>());
        governanceSchemaContexts.renew(event);
        assertThat(governanceSchemaContexts.getSchemaContexts().get("schema"), not(schemaContext));
    }
    
    @Test
    public void assertDisableStateChanged() {
        DisabledStateChangedEvent event = new DisabledStateChangedEvent(new GovernanceSchema("schema.ds_0"), true);
        governanceSchemaContexts.renew(event);
        verify(masterSlaveRule, times(2)).updateRuleStatus(any(RuleChangedEvent.class));
    }
    
    @Test
    @SneakyThrows(Exception.class)
    public void assertDataSourceChanged() {
        DataSourceChangedEvent event = new DataSourceChangedEvent("schema", getChangedDataSourceConfigurations());
        governanceSchemaContexts.renew(event);
        assertTrue(governanceSchemaContexts.getSchemaContexts().get("schema").getSchema().getDataSources().keySet().contains("ds_2"));
    }
    
    private Map<String, DataSourceConfiguration> getChangedDataSourceConfigurations() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>();
        result.put("ds_m", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
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
