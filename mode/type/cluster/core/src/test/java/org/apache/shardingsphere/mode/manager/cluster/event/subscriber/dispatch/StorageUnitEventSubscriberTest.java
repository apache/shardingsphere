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

package org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch;

import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.mode.event.dispatch.datasource.unit.AlterStorageUnitEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.unit.RegisterStorageUnitEvent;
import org.apache.shardingsphere.mode.event.dispatch.datasource.unit.UnregisterStorageUnitEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageUnitEventSubscriberTest {
    
    private StorageUnitEventSubscriber subscriber;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        subscriber = new StorageUnitEventSubscriber(contextManager);
    }
    
    @Test
    void assertRenewWithRegisterStorageUnitEvent() {
        when(contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService().getActiveVersionByFullPath("key")).thenReturn("value");
        when(contextManager.getPersistServiceFacade().getMetaDataPersistService().getDataSourceUnitService().load("foo_db", "foo_unit")).thenReturn(mock(DataSourcePoolProperties.class));
        subscriber.renew(new RegisterStorageUnitEvent("foo_db", "foo_unit", "key", "value"));
        verify(contextManager.getMetaDataContextManager().getStorageUnitManager()).registerStorageUnit(eq("foo_db"), any());
    }
    
    @Test
    void assertRenewWithAlterStorageUnitEvent() {
        when(contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService().getActiveVersionByFullPath("key")).thenReturn("value");
        when(contextManager.getPersistServiceFacade().getMetaDataPersistService().getDataSourceUnitService().load("foo_db", "foo_unit")).thenReturn(mock(DataSourcePoolProperties.class));
        subscriber.renew(new AlterStorageUnitEvent("foo_db", "foo_unit", "key", "value"));
        verify(contextManager.getMetaDataContextManager().getStorageUnitManager()).alterStorageUnit(eq("foo_db"), any());
    }
    
    @Test
    void assertRenewWithUnregisterStorageUnitEvent() {
        when(contextManager.getMetaDataContexts().getMetaData().containsDatabase("foo_db")).thenReturn(true);
        subscriber.renew(new UnregisterStorageUnitEvent("foo_db", "foo_unit"));
        verify(contextManager.getMetaDataContextManager().getStorageUnitManager()).unregisterStorageUnit("foo_db", "foo_unit");
    }
}
