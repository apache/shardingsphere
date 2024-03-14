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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.subscriber;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.ClusterContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResourceMetaDataChangedSubscriberTest {
    
    private ResourceMetaDataChangedSubscriber subscriber;
    
    private ContextManager contextManager;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataPersistService persistService;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() throws SQLException {
        contextManager = new ClusterContextManagerBuilder().build(createContextManagerBuilderParameter());
        contextManager.renewMetaDataContexts(new MetaDataContexts(contextManager.getMetaDataContexts().getPersistService(), new ShardingSphereMetaData(createDatabases(),
                contextManager.getMetaDataContexts().getMetaData().getGlobalResourceMetaData(), contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData(),
                new ConfigurationProperties(new Properties()))));
        subscriber = new ResourceMetaDataChangedSubscriber(contextManager);
    }
    
    private ContextManagerBuilderParameter createContextManagerBuilderParameter() {
        ModeConfiguration modeConfig = new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("FIXTURE", "", "", new Properties()));
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        return new ContextManagerBuilderParameter(modeConfig, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), new Properties(), Collections.emptyList(), instanceMetaData,
                false);
    }
    
    private Map<String, ShardingSphereDatabase> createDatabases() {
        when(database.getName()).thenReturn("db");
        when(database.getSchemas()).thenReturn(Collections.singletonMap("foo_schema", new ShardingSphereSchema()));
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(database.getSchema("foo_schema")).thenReturn(mock(ShardingSphereSchema.class));
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        return Collections.singletonMap("db", database);
    }
    
    @Test
    void assertRenewForDatabaseAdded() {
        when(persistService.getDataSourceUnitService().load("db_added")).thenReturn(createDataSourcePoolPropertiesMap());
        when(persistService.getDatabaseRulePersistService().load("db_added")).thenReturn(Collections.emptyList());
        subscriber.renew(new DatabaseAddedEvent("db_added"));
        assertNotNull(contextManager.getMetaDataContexts().getMetaData().getDatabase("db_added").getResourceMetaData().getStorageUnits());
    }
    
    private Map<String, DataSourcePoolProperties> createDataSourcePoolPropertiesMap() {
        MockedDataSource dataSource = new MockedDataSource();
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>(3, 1F);
        result.put("primary_ds", DataSourcePoolPropertiesCreator.create(dataSource));
        result.put("replica_ds_0", DataSourcePoolPropertiesCreator.create(dataSource));
        result.put("replica_ds_1", DataSourcePoolPropertiesCreator.create(dataSource));
        return result;
    }
    
    @Test
    void assertRenewForDatabaseDeleted() {
        subscriber.renew(new DatabaseDeletedEvent("db"));
        assertNull(contextManager.getMetaDataContexts().getMetaData().getDatabase("db"));
    }
    
    @Test
    void assertRenewForSchemaAdded() {
        subscriber.renew(new SchemaAddedEvent("db", "foo_schema"));
        verify(contextManager.getMetaDataContexts().getMetaData().getDatabase("db")).addSchema(argThat(argument -> argument.equals("foo_schema")), any(ShardingSphereSchema.class));
    }
    
    @Test
    void assertRenewForSchemaDeleted() {
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("db").containsSchema("foo_schema")).thenReturn(true);
        subscriber.renew(new SchemaDeletedEvent("db", "foo_schema"));
        verify(contextManager.getMetaDataContexts().getMetaData().getDatabase("db")).dropSchema("foo_schema");
    }
}
