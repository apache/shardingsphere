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

package org.apache.shardingsphere.orchestration.core.schema;

import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.rule.event.RuleChangedEvent;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.impl.StandardSchemaContexts;
import org.apache.shardingsphere.kernel.context.runtime.RuntimeContext;
import org.apache.shardingsphere.kernel.context.schema.ShardingSphereSchema;
import org.apache.shardingsphere.masterslave.rule.MasterSlaveRule;
import org.apache.shardingsphere.orchestration.core.common.event.auth.AuthenticationChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.schema.SchemaAddedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.schema.SchemaDeletedEvent;
import org.apache.shardingsphere.orchestration.core.facade.OrchestrationFacade;
import org.apache.shardingsphere.orchestration.core.metadata.MetaDataCenter;
import org.apache.shardingsphere.orchestration.core.metadata.event.MetaDataChangedEvent;
import org.apache.shardingsphere.orchestration.core.registry.RegistryCenter;
import org.apache.shardingsphere.orchestration.core.registry.event.CircuitStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registry.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registry.schema.OrchestrationSchema;
import org.apache.shardingsphere.orchestration.core.schema.fixture.TestOrchestrationSchemaContexts;
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
public final class OrchestrationSchemaContextsTest {
    
    @Mock
    private OrchestrationFacade orchestrationFacade;
    
    @Mock
    private RegistryCenter registryCenter;
    
    @Mock
    private MetaDataCenter metaDataCenter;
    
    @Mock
    private SchemaContext schemaContext;
    
    @Mock
    private MasterSlaveRule masterSlaveRule;
    
    private final Authentication authentication = new Authentication();
    
    private final ConfigurationProperties configurationProperties = new ConfigurationProperties(new Properties());
    
    private TestOrchestrationSchemaContexts orchestrationSchemaContexts;
    
    @Before
    public void setUp() {
        when(orchestrationFacade.getRegistryCenter()).thenReturn(registryCenter);
        when(registryCenter.loadDisabledDataSources()).thenReturn(Arrays.asList("schema.ds_1"));
        when(orchestrationFacade.getMetaDataCenter()).thenReturn(metaDataCenter);
        orchestrationSchemaContexts = new TestOrchestrationSchemaContexts(new StandardSchemaContexts(getSchemaContextMap(),
                authentication, configurationProperties, new H2DatabaseType()), orchestrationFacade);
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
        assertThat(orchestrationSchemaContexts.getDatabaseType().getName(), is("H2"));
    }
    
    @Test
    public void assertGetSchemaContexts() {
        assertThat(orchestrationSchemaContexts.getSchemaContexts().get("schema"), is(schemaContext));
    }
    
    @Test
    public void assertGetDefaultSchemaContext() {
        assertNull(orchestrationSchemaContexts.getDefaultSchemaContext());
    }
    
    @Test
    public void assertGetAuthentication() {
        assertThat(orchestrationSchemaContexts.getAuthentication(), is(authentication));
    }
    
    @Test
    public void assertGetProps() {
        assertThat(orchestrationSchemaContexts.getProps(), is(configurationProperties));
    }
    
    @Test
    public void assertIsCircuitBreak() {
        assertFalse(orchestrationSchemaContexts.isCircuitBreak());
    }
    
    @Test
    @SneakyThrows(Exception.class)
    public void assertSchemaAdd() {
        SchemaAddedEvent event = new SchemaAddedEvent("schema_add", getDataSourceConfigurations(), new ArrayList<>());
        orchestrationSchemaContexts.renew(event);
        assertNotNull(orchestrationSchemaContexts.getSchemaContexts().get("schema_add"));
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
        orchestrationSchemaContexts.renew(event);
        assertNull(orchestrationSchemaContexts.getSchemaContexts().get("schema"));
    }
    
    @Test
    public void assertPropertiesChanged() {
        Properties properties = new Properties();
        properties.setProperty("sql.show", "true");
        PropertiesChangedEvent event = new PropertiesChangedEvent(properties);
        orchestrationSchemaContexts.renew(event);
        assertThat(orchestrationSchemaContexts.getProps().getProps().getProperty("sql.show"), is("true"));
    }
    
    @Test
    public void assertAuthenticationChanged() {
        Authentication authentication = new Authentication();
        AuthenticationChangedEvent event = new AuthenticationChangedEvent(authentication);
        orchestrationSchemaContexts.renew(event);
        assertThat(orchestrationSchemaContexts.getAuthentication(), is(authentication));
    }
    
    @Test
    public void assertMetaDataChanged() {
        MetaDataChangedEvent event = new MetaDataChangedEvent(Collections.singletonList("schema_changed"), mock(RuleSchemaMetaData.class));
        orchestrationSchemaContexts.renew(event);
        assertTrue(orchestrationSchemaContexts.getSchemaContexts().containsKey("schema"));
        assertFalse(orchestrationSchemaContexts.getSchemaContexts().containsKey("schema_changed"));
    }
    
    @Test
    public void assertMetaDataChangedWithExistSchema() {
        MetaDataChangedEvent event = new MetaDataChangedEvent(Collections.singletonList("schema"), mock(RuleSchemaMetaData.class));
        orchestrationSchemaContexts.renew(event);
        assertThat(orchestrationSchemaContexts.getSchemaContexts().get("schema"), not(schemaContext));
    }
    
    @Test
    @SneakyThrows(Exception.class)
    public void assertRuleConfigurationsChanged() {
        assertThat(orchestrationSchemaContexts.getSchemaContexts().get("schema"), is(schemaContext));
        RuleConfigurationsChangedEvent event = new RuleConfigurationsChangedEvent("schema", new ArrayList<>());
        orchestrationSchemaContexts.renew(event);
        assertThat(orchestrationSchemaContexts.getSchemaContexts().get("schema"), not(schemaContext));
    }
    
    @Test
    public void assertDisableStateChanged() {
        DisabledStateChangedEvent event = new DisabledStateChangedEvent(new OrchestrationSchema("schema.ds_0"), true);
        orchestrationSchemaContexts.renew(event);
        verify(masterSlaveRule, times(2)).updateRuleStatus(any(RuleChangedEvent.class));
    }
    
    @Test
    @SneakyThrows(Exception.class)
    public void assertDataSourceChanged() {
        DataSourceChangedEvent event = new DataSourceChangedEvent("schema", getChangedDataSourceConfigurations());
        orchestrationSchemaContexts.renew(event);
        assertTrue(orchestrationSchemaContexts.getSchemaContexts().get("schema").getSchema().getDataSources().containsKey("ds_2"));
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
        orchestrationSchemaContexts.renew(event);
        assertTrue(orchestrationSchemaContexts.isCircuitBreak());
    }
}
