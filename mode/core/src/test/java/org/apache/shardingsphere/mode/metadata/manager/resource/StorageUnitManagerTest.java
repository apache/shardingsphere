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

package org.apache.shardingsphere.mode.metadata.manager.resource;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.factory.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(AutoMockExtension.class)
class StorageUnitManagerTest {
    
    private static final String DATABASE_NAME = "foo_db";
    
    @Test
    void assertRegisterSuccess() {
        MetaDataContexts metaDataContexts = mockMetaDataContexts();
        ResourceSwitchManager resourceSwitchManager = mock(ResourceSwitchManager.class);
        SwitchingResource switchingResource = new SwitchingResource(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), Collections.emptyMap());
        when(resourceSwitchManager.switchByRegisterStorageUnit(any(ResourceMetaData.class), any(Map.class))).thenReturn(switchingResource);
        ShardingSphereDatabase reloadDatabase = mock(ShardingSphereDatabase.class);
        when(reloadDatabase.getAllSchemas()).thenReturn(Collections.singleton(new ShardingSphereSchema("foo_schema")));
        MetaDataContexts reloadMetaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(reloadMetaDataContexts.getMetaData().getDatabase(DATABASE_NAME)).thenReturn(reloadDatabase);
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createBySwitchResource(eq(DATABASE_NAME), eq(true), eq(switchingResource), eq(metaDataContexts))).thenReturn(reloadMetaDataContexts))) {
            createManager(metaDataContexts, resourceSwitchManager).register(DATABASE_NAME, Collections.emptyMap());
        }
        verify(metaDataContexts).update(any(MetaDataContexts.class));
        verify(metaDataContexts.getMetaData()).putDatabase(any(ShardingSphereDatabase.class));
        verifyClosableRuleInvoked(metaDataContexts);
    }
    
    @Test
    void assertRegisterLogsErrorWhenSQLException() {
        MetaDataContexts metaDataContexts = mockMetaDataContexts();
        ResourceSwitchManager resourceSwitchManager = mock(ResourceSwitchManager.class);
        doAnswer(invocation -> {
            throw new SQLException("register error");
        }).when(resourceSwitchManager).switchByRegisterStorageUnit(any(ResourceMetaData.class), any(Map.class));
        assertDoesNotThrow(() -> createManager(metaDataContexts, resourceSwitchManager).register(DATABASE_NAME, Collections.emptyMap()));
        verify(metaDataContexts, never()).update(any(MetaDataContexts.class));
        verify(metaDataContexts.getMetaData(), never()).putDatabase(any(ShardingSphereDatabase.class));
    }
    
    @Test
    void assertAlterSuccess() {
        MetaDataContexts metaDataContexts = mockMetaDataContexts();
        ResourceSwitchManager resourceSwitchManager = mock(ResourceSwitchManager.class);
        SwitchingResource switchingResource = new SwitchingResource(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), Collections.emptyMap());
        when(resourceSwitchManager.switchByAlterStorageUnit(any(ResourceMetaData.class), any(Map.class))).thenReturn(switchingResource);
        ShardingSphereDatabase reloadDatabase = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        MetaDataContexts reloadMetaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(reloadMetaDataContexts.getMetaData().getDatabase(DATABASE_NAME)).thenReturn(reloadDatabase);
        when(reloadDatabase.getAllSchemas()).thenReturn(Collections.singleton(new ShardingSphereSchema("foo_schema")));
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createBySwitchResource(eq(DATABASE_NAME), eq(true), eq(switchingResource), eq(metaDataContexts))).thenReturn(reloadMetaDataContexts))) {
            createManager(metaDataContexts, resourceSwitchManager).alter(DATABASE_NAME, Collections.emptyMap());
        }
        verify(metaDataContexts).update(any(MetaDataContexts.class));
        verifyClosableRuleInvoked(metaDataContexts);
    }
    
    @Test
    void assertAlterLogsErrorWhenSQLException() {
        MetaDataContexts metaDataContexts = mockMetaDataContexts();
        ResourceSwitchManager resourceSwitchManager = mock(ResourceSwitchManager.class);
        doAnswer(invocation -> {
            throw new SQLException("alter error");
        }).when(resourceSwitchManager).switchByAlterStorageUnit(any(ResourceMetaData.class), any(Map.class));
        assertDoesNotThrow(() -> createManager(metaDataContexts, resourceSwitchManager).alter(DATABASE_NAME, Collections.emptyMap()));
        verify(metaDataContexts, never()).update(any(MetaDataContexts.class));
        verify(metaDataContexts.getMetaData(), never()).putDatabase(any(ShardingSphereDatabase.class));
    }
    
    @Test
    void assertUnregisterSuccess() {
        MetaDataContexts metaDataContexts = mockMetaDataContexts();
        ResourceSwitchManager resourceSwitchManager = mock(ResourceSwitchManager.class);
        SwitchingResource switchingResource = new SwitchingResource(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), Collections.emptyMap());
        when(resourceSwitchManager.switchByUnregisterStorageUnit(any(ResourceMetaData.class), any(Collection.class))).thenReturn(switchingResource);
        ShardingSphereDatabase reloadDatabase = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        MetaDataContexts reloadMetaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(reloadMetaDataContexts.getMetaData().getDatabase(DATABASE_NAME)).thenReturn(reloadDatabase);
        when(reloadDatabase.getAllSchemas()).thenReturn(Collections.singleton(new ShardingSphereSchema("foo_schema")));
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createBySwitchResource(eq(DATABASE_NAME), eq(false), eq(switchingResource), eq(metaDataContexts))).thenReturn(reloadMetaDataContexts))) {
            createManager(metaDataContexts, resourceSwitchManager).unregister(DATABASE_NAME, "ds_0");
        }
        verify(metaDataContexts).update(any(MetaDataContexts.class));
        verifyClosableRuleInvoked(metaDataContexts);
    }
    
    @Test
    void assertUnregisterLogsErrorWhenSQLException() {
        MetaDataContexts metaDataContexts = mockMetaDataContexts();
        ResourceSwitchManager resourceSwitchManager = mock(ResourceSwitchManager.class);
        doAnswer(invocation -> {
            throw new SQLException("unregister error");
        }).when(resourceSwitchManager).switchByUnregisterStorageUnit(any(ResourceMetaData.class), any(Collection.class));
        assertDoesNotThrow(() -> createManager(metaDataContexts, resourceSwitchManager).unregister(DATABASE_NAME, "ds_0"));
        verify(metaDataContexts, never()).update(any(MetaDataContexts.class));
        verify(metaDataContexts.getMetaData(), never()).putDatabase(any(ShardingSphereDatabase.class));
    }
    
    private MetaDataContexts mockMetaDataContexts() {
        MetaDataContexts result = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData()).thenReturn(new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()));
        when(database.getRuleMetaData().getRules()).thenReturn(Arrays.asList(mock(ShardingSphereRule.class, withSettings().extraInterfaces(AutoCloseable.class)), mock(ShardingSphereRule.class)));
        when(result.getMetaData().getDatabase(DATABASE_NAME)).thenReturn(database);
        return result;
    }
    
    private StorageUnitManager createManager(final MetaDataContexts metaDataContexts, final ResourceSwitchManager resourceSwitchManager) {
        MetaDataPersistFacade metaDataPersistFacade = mock(MetaDataPersistFacade.class, RETURNS_DEEP_STUBS);
        lenient().when(metaDataPersistFacade.getDatabaseMetaDataFacade().getView().load(anyString(), anyString())).thenReturn(Collections.emptyList());
        return new StorageUnitManager(metaDataContexts, mock(ComputeNodeInstanceContext.class), resourceSwitchManager, metaDataPersistFacade);
    }
    
    @SneakyThrows
    private void verifyClosableRuleInvoked(final MetaDataContexts metaDataContexts) {
        verify((AutoCloseable) metaDataContexts.getMetaData().getDatabase(DATABASE_NAME).getRuleMetaData().getRules().iterator().next()).close();
    }
}
