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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.datasource;

import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
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
class StorageUnitChangedHandlerTest {
    
    private StorageUnitChangedHandler handler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        when(contextManager.getPersistServiceFacade().getRepository().query(any())).thenReturn("0");
        handler = new StorageUnitChangedHandler(contextManager);
    }
    
    @Test
    void assertHandleStorageUnitRegistered() {
        when(contextManager.getPersistServiceFacade().getMetaDataFacade().getDataSourceUnitService().load("foo_db", "foo_unit")).thenReturn(mock(DataSourcePoolProperties.class));
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/data_sources/units/foo_unit/active_version", "0", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getStorageUnitManager()).register(eq("foo_db"), any());
    }
    
    @Test
    void assertHandleStorageUnitAltered() {
        when(contextManager.getPersistServiceFacade().getMetaDataFacade().getDataSourceUnitService().load("foo_db", "foo_unit")).thenReturn(mock(DataSourcePoolProperties.class));
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/data_sources/units/foo_unit/active_version", "0", Type.UPDATED));
        verify(contextManager.getMetaDataContextManager().getStorageUnitManager()).alter(eq("foo_db"), any());
    }
    
    @Test
    void assertHandleStorageUnitUnregistered() {
        when(contextManager.getMetaDataContexts().getMetaData().containsDatabase("foo_db")).thenReturn(true);
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/data_sources/units/foo_unit", "", Type.DELETED));
        verify(contextManager.getMetaDataContextManager().getStorageUnitManager()).unregister("foo_db", "foo_unit");
    }
}
