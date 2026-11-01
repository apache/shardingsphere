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

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.statistics.StatisticsRefreshEngine;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.ConstructionMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@ConstructionMockSettings(StatisticsRefreshEngine.class)
class TableChangedHandlerTest {
    
    private TableChangedHandler handler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    private StatisticsRefreshEngine statisticsRefreshEngine;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        handler = new TableChangedHandler(contextManager);
        statisticsRefreshEngine = (StatisticsRefreshEngine) Plugins.getMemberAccessor().get(TableChangedHandler.class.getDeclaredField("statisticsRefreshEngine"), handler);
    }
    
    @ParameterizedTest(name = "{0}: persisted table={1}, updates={2}")
    @CsvSource({"ADDED, true, 1", "UPDATED, true, 1", "ADDED, false, 0"})
    void assertHandleCreatedOrAltered(final Type eventType, final boolean persistedTableExists, final int expectedUpdates) {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        when(contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseMetaDataFacade().getTable().load("foo_db", "foo_schema", "foo_tbl")).thenAnswer(invocation -> {
            assertTrue(Thread.holdsLock(contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService()));
            assertTrue(Thread.holdsLock(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager()));
            return persistedTableExists ? Optional.of(table) : Optional.empty();
        });
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version", "0", eventType));
        verify(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager(), times(expectedUpdates)).alterTable("foo_db", "foo_schema", table);
        verify(statisticsRefreshEngine, times(expectedUpdates)).asyncRefresh();
    }
    
    @ParameterizedTest(name = "local table={0}, drops={2}")
    @MethodSource("getDroppedArguments")
    void assertHandleDropped(final boolean tableExists, final Collection<MutableDataNodeRuleAttribute> ruleAttributes, final int expectedDrops) {
        when(contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseMetaDataFacade().getTable().load("foo_db", "foo_schema", "foo_tbl")).thenAnswer(invocation -> {
            assertTrue(Thread.holdsLock(contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService()));
            assertTrue(Thread.holdsLock(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager()));
            return Optional.empty();
        });
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").containsSchema("foo_schema")).thenReturn(true);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getSchema("foo_schema").containsTable("foo_tbl")).thenReturn(tableExists);
        if (!tableExists) {
            when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class)).thenReturn(ruleAttributes);
        }
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.DELETED));
        verify(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager(), times(expectedDrops)).dropTable("foo_db", "foo_schema", "foo_tbl");
        verify(statisticsRefreshEngine).asyncRefresh();
    }
    
    private static Object[][] getDroppedArguments() {
        MutableDataNodeRuleAttribute ruleAttribute = mock(MutableDataNodeRuleAttribute.class);
        when(ruleAttribute.findTableDataNode("foo_schema", "foo_tbl")).thenReturn(Optional.of(mock()));
        return new Object[][]{
                {true, Collections.emptyList(), 1},
                {false, Collections.singleton(mock(MutableDataNodeRuleAttribute.class)), 0},
                {false, Collections.singleton(ruleAttribute), 1}
        };
    }
    
    @Test
    void assertHandleRecreatedTableDropped() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        when(contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseMetaDataFacade().getTable().load("foo_db", "foo_schema", "foo_tbl")).thenAnswer(invocation -> {
            assertTrue(Thread.holdsLock(contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService()));
            assertTrue(Thread.holdsLock(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager()));
            return Optional.of(table);
        });
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.DELETED));
        verifyNoInteractions(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager());
        verify(statisticsRefreshEngine).asyncRefresh();
    }
    
    @Test
    void assertHandleDroppedDuringCreatePreparation() throws InterruptedException {
        Object persistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        AtomicReference<Optional<ShardingSphereTable>> actualPersistedTable = new AtomicReference<>(Optional.empty());
        MutableDataNodeRuleAttribute ruleAttribute = mock(MutableDataNodeRuleAttribute.class);
        lenient().when(ruleAttribute.findTableDataNode("foo_schema", "foo_tbl")).thenReturn(Optional.of(mock()));
        lenient().when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").containsSchema("foo_schema")).thenReturn(true);
        lenient().when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class))
                .thenReturn(Collections.singleton(ruleAttribute));
        when(contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseMetaDataFacade().getTable().load("foo_db", "foo_schema", "foo_tbl")).thenAnswer(invocation -> {
            assertTrue(Thread.holdsLock(persistService));
            assertTrue(Thread.holdsLock(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager()));
            return actualPersistedTable.get();
        });
        CountDownLatch routePrepared = new CountDownLatch(1);
        CountDownLatch callbackAttempted = new CountDownLatch(1);
        AtomicReference<Throwable> actualFailure = new AtomicReference<>();
        Thread createThread = new Thread(() -> {
            synchronized (persistService) {
                routePrepared.countDown();
                assertTrue(assertDoesNotThrow(() -> callbackAttempted.await(5L, TimeUnit.SECONDS)));
                actualPersistedTable.set(Optional.of(table));
            }
        });
        Thread callbackThread = new Thread(() -> {
            assertTrue(assertDoesNotThrow(() -> routePrepared.await(5L, TimeUnit.SECONDS)));
            callbackAttempted.countDown();
            handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.DELETED));
        });
        createThread.setUncaughtExceptionHandler((thread, ex) -> actualFailure.set(ex));
        callbackThread.setUncaughtExceptionHandler((thread, ex) -> actualFailure.set(ex));
        try {
            createThread.start();
            callbackThread.start();
            createThread.join(5000L);
            callbackThread.join(5000L);
            assertFalse(createThread.isAlive());
            assertFalse(callbackThread.isAlive());
            assertNull(actualFailure.get());
            assertTrue(actualPersistedTable.get().isPresent());
            verify(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager(), never()).dropTable("foo_db", "foo_schema", "foo_tbl");
            verify(statisticsRefreshEngine).asyncRefresh();
        } finally {
            routePrepared.countDown();
            callbackAttempted.countDown();
            createThread.interrupt();
            callbackThread.interrupt();
            createThread.join(5000L);
            callbackThread.join(5000L);
        }
    }
    
    @Test
    void assertHandleMissingDatabaseDropped() {
        when(contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseMetaDataFacade().getTable().load("foo_db", "foo_schema", "foo_tbl")).thenAnswer(invocation -> {
            assertTrue(Thread.holdsLock(contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService()));
            assertTrue(Thread.holdsLock(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager()));
            return Optional.empty();
        });
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(null);
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.DELETED));
        verifyNoInteractions(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager());
        verify(statisticsRefreshEngine).asyncRefresh();
    }
    
    @Test
    void assertHandleMissingSchemaDropped() {
        when(contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseMetaDataFacade().getTable().load("foo_db", "foo_schema", "foo_tbl")).thenAnswer(invocation -> {
            assertTrue(Thread.holdsLock(contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService()));
            assertTrue(Thread.holdsLock(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager()));
            return Optional.empty();
        });
        handler.handle("foo_db", new DataChangedEvent("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.DELETED));
        verifyNoInteractions(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager());
        verify(statisticsRefreshEngine).asyncRefresh();
    }
}
