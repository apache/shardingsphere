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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.metadata;

import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MetaDataChangedHandlerTest {
    
    private MetaDataChangedHandler handler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        when(contextManager.getMetaDataContexts().getMetaData().getTemporaryProps().getValue(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_ENABLED)).thenReturn(false);
        when(contextManager.getComputeNodeInstanceContext().getModeConfiguration().isCluster()).thenReturn(true);
        when(contextManager.getPersistServiceFacade().getRepository().query(any())).thenReturn("0");
        handler = new MetaDataChangedHandler(contextManager);
    }
    
    @Test
    void assertHandleSchemaCreated() {
        when(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()).thenReturn(InstanceType.PROXY);
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema", "", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getSchemaMetaDataManager()).addSchema("foo_db", "foo_schema");
    }
    
    @Test
    void assertHandleSchemaDropped() {
        when(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()).thenReturn(InstanceType.PROXY);
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema", "", Type.DELETED));
        verify(contextManager.getMetaDataContextManager().getSchemaMetaDataManager()).dropSchema("foo_db", "foo_schema");
    }
    
    @Test
    void assertHandleTableCreated() {
        when(contextManager.getPersistServiceFacade().getRepository().query("key")).thenReturn("value");
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataFacade().getTable().load("foo_db", "foo_schema", "foo_tbl"))
                .thenReturn(table);
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version/0", "0", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getSchemaMetaDataManager()).alterSchema("foo_db", "foo_schema", table, null);
    }
    
    @Test
    void assertHandleTableAltered() {
        when(contextManager.getPersistServiceFacade().getRepository().query("key")).thenReturn("value");
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataFacade().getTable().load("foo_db", "foo_schema", "foo_tbl"))
                .thenReturn(table);
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version/0", "0", Type.UPDATED));
        verify(contextManager.getMetaDataContextManager().getSchemaMetaDataManager()).alterSchema("foo_db", "foo_schema", table, null);
    }
    
    @Test
    void assertHandleTableDropped() {
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.DELETED));
        verify(contextManager.getMetaDataContextManager().getSchemaMetaDataManager()).alterSchema("foo_db", "foo_schema", "foo_tbl", null);
    }
    
    @Test
    void assertHandleViewCreated() {
        when(contextManager.getPersistServiceFacade().getRepository().query("key")).thenReturn("value");
        ShardingSphereView view = mock(ShardingSphereView.class);
        when(contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataFacade().getView().load("foo_db", "foo_schema", "foo_view"))
                .thenReturn(view);
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/views/foo_view/active_version/0", "0", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getSchemaMetaDataManager()).alterSchema("foo_db", "foo_schema", null, view);
    }
    
    @Test
    void assertHandleViewAltered() {
        when(contextManager.getPersistServiceFacade().getRepository().query("key")).thenReturn("value");
        ShardingSphereView view = mock(ShardingSphereView.class);
        when(contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataFacade().getView().load("foo_db", "foo_schema", "foo_view"))
                .thenReturn(view);
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/views/foo_view/active_version/0", "0", Type.UPDATED));
        verify(contextManager.getMetaDataContextManager().getSchemaMetaDataManager()).alterSchema("foo_db", "foo_schema", null, view);
    }
    
    @Test
    void assertHandleViewDropped() {
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/views/foo_view", "", Type.DELETED));
        verify(contextManager.getMetaDataContextManager().getSchemaMetaDataManager()).alterSchema("foo_db", "foo_schema", null, "foo_view");
    }
    
    @Test
    void assertHandleStorageUnitRegistered() {
        when(contextManager.getPersistServiceFacade().getRepository().query("key")).thenReturn("value");
        when(contextManager.getPersistServiceFacade().getMetaDataPersistService().getDataSourceUnitService().load("foo_db", "foo_unit")).thenReturn(mock(DataSourcePoolProperties.class));
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/data_sources/units/foo_unit/active_version/0", "0", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getStorageUnitManager()).registerStorageUnit(eq("foo_db"), any());
    }
    
    @Test
    void assertHandleStorageUnitAltered() {
        when(contextManager.getPersistServiceFacade().getRepository().query("key")).thenReturn("value");
        when(contextManager.getPersistServiceFacade().getMetaDataPersistService().getDataSourceUnitService().load("foo_db", "foo_unit")).thenReturn(mock(DataSourcePoolProperties.class));
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/data_sources/units/foo_unit/active_version/0", "0", Type.UPDATED));
        verify(contextManager.getMetaDataContextManager().getStorageUnitManager()).alterStorageUnit(eq("foo_db"), any());
    }
    
    @Test
    void assertHandleStorageUnitUnregistered() {
        when(contextManager.getMetaDataContexts().getMetaData().containsDatabase("foo_db")).thenReturn(true);
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/data_sources/units/foo_unit", "", Type.DELETED));
        verify(contextManager.getMetaDataContextManager().getStorageUnitManager()).unregisterStorageUnit("foo_db", "foo_unit");
    }
}
