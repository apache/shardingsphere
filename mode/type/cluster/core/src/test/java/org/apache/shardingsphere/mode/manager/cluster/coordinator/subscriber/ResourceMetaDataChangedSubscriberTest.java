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
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.ClusterContextManagerBuilder;
import org.apache.shardingsphere.mode.event.schema.TableMetaDataChangedEvent;
import org.apache.shardingsphere.mode.event.schema.ViewMetaDataChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
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
import java.util.LinkedList;
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
        when(database.getResourceMetaData().getDataSources()).thenReturn(new LinkedHashMap<>());
        when(database.getResourceMetaData().getStorageTypes()).thenReturn(Collections.singletonMap("ds_0", new MySQLDatabaseType()));
        when(database.getSchemas()).thenReturn(Collections.singletonMap("foo_schema", new ShardingSphereSchema()));
        when(database.getProtocolType()).thenReturn(new MySQLDatabaseType());
        when(database.getSchema("foo_schema")).thenReturn(mock(ShardingSphereSchema.class));
        when(database.getRuleMetaData().getRules()).thenReturn(new LinkedList<>());
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        when(database.getRuleMetaData().findRules(ResourceHeldRule.class)).thenReturn(Collections.emptyList());
        return Collections.singletonMap("db", database);
    }
    
    @Test
    void assertRenewForDatabaseAdded() {
        when(persistService.getDataSourceUnitService().load("db_added")).thenReturn(createDataSourcePropertiesMap());
        when(persistService.getDatabaseRulePersistService().load("db_added")).thenReturn(Collections.emptyList());
        subscriber.renew(new DatabaseAddedEvent("db_added"));
        assertNotNull(contextManager.getMetaDataContexts().getMetaData().getDatabase("db_added").getResourceMetaData().getDataSources());
    }
    
    private Map<String, DataSourceProperties> createDataSourcePropertiesMap() {
        MockedDataSource dataSource = new MockedDataSource();
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(3, 1F);
        result.put("primary_ds", DataSourcePropertiesCreator.create(dataSource));
        result.put("replica_ds_0", DataSourcePropertiesCreator.create(dataSource));
        result.put("replica_ds_1", DataSourcePropertiesCreator.create(dataSource));
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
        verify(contextManager.getMetaDataContexts().getMetaData().getDatabase("db")).putSchema(argThat(argument -> argument.equals("foo_schema")), any(ShardingSphereSchema.class));
    }
    
    @Test
    void assertRenewForSchemaDeleted() {
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("db").containsSchema("foo_schema")).thenReturn(true);
        subscriber.renew(new SchemaDeletedEvent("db", "foo_schema"));
        verify(contextManager.getMetaDataContexts().getMetaData().getDatabase("db")).removeSchema("foo_schema");
    }
    
    @Test
    void assertRenewForTableMetaDataChangedChanged() {
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("db").containsSchema("db")).thenReturn(true);
        ShardingSphereTable changedTableMetaData = new ShardingSphereTable("t_order", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        TableMetaDataChangedEvent event = new TableMetaDataChangedEvent("db", "db", changedTableMetaData, null);
        subscriber.renew(event);
        verify(contextManager.getMetaDataContexts().getMetaData().getDatabase("db").getSchema("db")).putTable("t_order", event.getChangedTableMetaData());
    }
    
    @Test
    void assertRenewForViewMetaDataChanged() {
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("db").containsSchema("db")).thenReturn(true);
        ShardingSphereView changedViewMetaData = new ShardingSphereView("t_order_view", "");
        ViewMetaDataChangedEvent event = new ViewMetaDataChangedEvent("db", "db", changedViewMetaData, null);
        subscriber.renew(event);
        verify(contextManager.getMetaDataContexts().getMetaData().getDatabase("db").getSchema("db")).putView("t_order_view", event.getChangedViewMetaData());
    }
}
