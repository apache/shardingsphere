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

import org.apache.shardingsphere.authority.api.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.governance.context.authority.listener.event.AuthorityChangedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.metadata.MetaDataDeletedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.metadata.MetaDataPersistedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.GlobalRuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.schema.SchemaChangedEvent;
import org.apache.shardingsphere.governance.core.facade.GovernanceFacade;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.core.registry.listener.event.readwritesplitting.DisabledStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.schema.GovernanceSchema;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUsers;
import org.apache.shardingsphere.infra.rule.event.RuleChangedEvent;
import org.apache.shardingsphere.readwritesplitting.common.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
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
    
    private final ConfigurationProperties props = new ConfigurationProperties(new Properties());
    
    @Mock
    private GovernanceFacade governanceFacade;
    
    @Mock
    private RegistryCenter registryCenter;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData metaData;
    
    @Mock
    private ReadwriteSplittingRule readwriteSplittingRule;
    
    private GovernanceMetaDataContexts governanceMetaDataContexts;
    
    @Mock
    private ShardingSphereRuleMetaData globalRuleMetaData;
    
    @Before
    public void setUp() {
        when(governanceFacade.getRegistryCenter()).thenReturn(registryCenter);
        when(registryCenter.loadDisabledDataSources("schema")).thenReturn(Collections.singletonList("schema.ds_1"));
        governanceMetaDataContexts = new GovernanceMetaDataContexts(new StandardMetaDataContexts(
                createMetaDataMap(), globalRuleMetaData, mock(ExecutorEngine.class), new ShardingSphereUsers(Collections.emptyList()), props), governanceFacade);
    }
    
    private Map<String, ShardingSphereMetaData> createMetaDataMap() {
        when(metaData.getName()).thenReturn("schema");
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        when(resource.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(metaData.getResource()).thenReturn(resource);
        when(metaData.getSchema()).thenReturn(mock(ShardingSphereSchema.class));
        when(metaData.getRuleMetaData().getRules()).thenReturn(Collections.singletonList(readwriteSplittingRule));
        return Collections.singletonMap("schema", metaData);
    }
    
    @Test
    public void assertGetMetaData() {
        assertThat(governanceMetaDataContexts.getMetaData("schema"), is(metaData));
    }
    
    @Test
    public void assertGetDefaultMetaData() {
        assertNull(governanceMetaDataContexts.getDefaultMetaData());
    }
    
    @Test
    public void assertGetProps() {
        assertThat(governanceMetaDataContexts.getProps(), is(props));
    }
    
    @Test
    public void assertSchemaAdd() throws SQLException {
        MetaDataPersistedEvent event = new MetaDataPersistedEvent("schema_add");
        when(registryCenter.loadDataSourceConfigurations("schema_add")).thenReturn(getDataSourceConfigurations());
        governanceMetaDataContexts.renew(event);
        assertNotNull(governanceMetaDataContexts.getMetaData("schema_add"));
        assertNotNull(governanceMetaDataContexts.getMetaData("schema_add").getResource().getDataSources());
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
        assertNull(governanceMetaDataContexts.getMetaData("schema"));
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
    public void assertAuthorityChanged() {
        AuthorityChangedEvent event = new AuthorityChangedEvent(Collections.singleton(mock(ShardingSphereUser.class)));
        governanceMetaDataContexts.renew(event);
        assertThat(governanceMetaDataContexts.getUsers().getUsers().size(), is(1));
    }
    
    @Test
    public void assertSchemaChanged() {
        SchemaChangedEvent event = new SchemaChangedEvent("schema_changed", mock(ShardingSphereSchema.class));
        governanceMetaDataContexts.renew(event);
        assertTrue(governanceMetaDataContexts.getAllSchemaNames().contains("schema"));
        assertFalse(governanceMetaDataContexts.getAllSchemaNames().contains("schema_changed"));
    }
    
    @Test
    public void assertSchemaChangedWithExistSchema() {
        SchemaChangedEvent event = new SchemaChangedEvent("schema", mock(ShardingSphereSchema.class));
        governanceMetaDataContexts.renew(event);
        assertThat(governanceMetaDataContexts.getMetaData("schema"), not(metaData));
    }
    
    @Test
    public void assertRuleConfigurationsChanged() throws SQLException {
        assertThat(governanceMetaDataContexts.getMetaData("schema"), is(metaData));
        RuleConfigurationsChangedEvent event = new RuleConfigurationsChangedEvent("schema", new LinkedList<>());
        governanceMetaDataContexts.renew(event);
        assertThat(governanceMetaDataContexts.getMetaData("schema"), not(metaData));
    }
    
    @Test
    public void assertDisableStateChanged() {
        DisabledStateChangedEvent event = new DisabledStateChangedEvent(new GovernanceSchema("schema.ds_0"), true);
        governanceMetaDataContexts.renew(event);
        verify(readwriteSplittingRule, times(2)).updateRuleStatus(any(RuleChangedEvent.class));
    }
    
    @Test
    public void assertDataSourceChanged() throws SQLException {
        DataSourceChangedEvent event = new DataSourceChangedEvent("schema", getChangedDataSourceConfigurations());
        governanceMetaDataContexts.renew(event);
        assertTrue(governanceMetaDataContexts.getMetaData("schema").getResource().getDataSources().containsKey("ds_2"));
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
    public void assertGlobalRuleConfigurationsChanged() {
        GlobalRuleConfigurationsChangedEvent event = new GlobalRuleConfigurationsChangedEvent("", getChangedGlobalRuleConfigurations());
        governanceMetaDataContexts.renew(event);
        assertThat(governanceMetaDataContexts.getGlobalRuleMetaData(), not(globalRuleMetaData));
    }
    
    private Collection<RuleConfiguration> getChangedGlobalRuleConfigurations() {
        Collection<ShardingSphereUser> users = new LinkedList<>();
        users.add(new ShardingSphereUser("root", "root", "%"));
        users.add(new ShardingSphereUser("sharding", "sharding", "localhost"));
        RuleConfiguration authorityRuleConfig = new AuthorityRuleConfiguration(users, new ShardingSphereAlgorithmConfiguration("NATIVE", new Properties()));
        return Collections.singleton(authorityRuleConfig);
    }
}
