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

package org.apache.shardingsphere.proxy.frontend.firebird.command.admin;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.frontend.firebird.command.admin.FirebirdCancellationRegistry.Scope;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireOperations;
import org.firebirdsql.jdbc.FBConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirebirdCancellationRegistryTest {
    
    private static final int CONNECTION_ID = 100001;
    
    private final FirebirdCancellationRegistry registry = FirebirdCancellationRegistry.getInstance();
    
    @AfterEach
    void tearDown() {
        registry.unregister(CONNECTION_ID);
    }
    
    @Test
    void assertDisabledCancellationAndPendingCancellation() throws SQLException {
        ProxyDatabaseConnectionManager manager = mock(ProxyDatabaseConnectionManager.class);
        registry.beginCommand(CONNECTION_ID);
        registry.apply(CONNECTION_ID, ISCConstants.fb_cancel_disable);
        registry.apply(CONNECTION_ID, ISCConstants.fb_cancel_raise);
        registry.enter(CONNECTION_ID, manager).close();
        registry.apply(CONNECTION_ID, ISCConstants.fb_cancel_enable);
        registry.apply(CONNECTION_ID, ISCConstants.fb_cancel_raise);
        SQLException actual = assertThrows(SQLException.class, () -> registry.enter(CONNECTION_ID, manager));
        assertThat(actual.getErrorCode(), is(ISCConstants.isc_cancelled));
    }
    
    @Test
    void assertLateSignalDoesNotCancelNextCommand() throws SQLException {
        ProxyDatabaseConnectionManager manager = mock(ProxyDatabaseConnectionManager.class);
        registry.beginCommand(CONNECTION_ID);
        registry.apply(CONNECTION_ID, ISCConstants.fb_cancel_raise);
        registry.endCommand(CONNECTION_ID);
        registry.beginCommand(CONNECTION_ID);
        registry.enter(CONNECTION_ID, manager).close();
    }
    
    @Test
    void assertActiveCancellationTargetsTransactionConnections() throws Exception {
        ProxyDatabaseConnectionManager manager = mock(ProxyDatabaseConnectionManager.class);
        Connection connection = mock(Connection.class);
        FBConnection firebird = mock(FBConnection.class);
        FbDatabase database = mock(FbDatabase.class);
        Multimap<String, Connection> cached = LinkedHashMultimap.create();
        cached.put("foo_db.foo_ds", connection);
        when(manager.getCachedConnections()).thenReturn(cached);
        when(connection.unwrap(FBConnection.class)).thenReturn(firebird);
        when(firebird.getFbDatabase()).thenReturn(database);
        CountDownLatch cancelled = new CountDownLatch(1);
        doAnswer(invocation -> {
            cancelled.countDown();
            return null;
        }).when(database).cancelOperation(ISCConstants.fb_cancel_raise);
        registry.beginCommand(CONNECTION_ID);
        try (Scope ignored = registry.enter(CONNECTION_ID, manager)) {
            registry.apply(CONNECTION_ID, ISCConstants.fb_cancel_raise);
            assertTrue(cancelled.await(5, TimeUnit.SECONDS));
        }
    }
    
    @Test
    void assertSignalAndCommandEndDoNotWaitForBackendCancellation() throws Exception {
        ProxyDatabaseConnectionManager manager = mock(ProxyDatabaseConnectionManager.class);
        Connection connection = mock(Connection.class);
        FBConnection firebird = mock(FBConnection.class);
        FbDatabase database = mock(FbDatabase.class);
        Multimap<String, Connection> cached = LinkedHashMultimap.create();
        cached.put("foo_db.foo_ds", connection);
        when(manager.getCachedConnections()).thenReturn(cached);
        when(connection.unwrap(FBConnection.class)).thenReturn(firebird);
        when(firebird.getFbDatabase()).thenReturn(database);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            entered.countDown();
            release.await(5, TimeUnit.SECONDS);
            return null;
        }).when(database).cancelOperation(ISCConstants.fb_cancel_raise);
        registry.beginCommand(CONNECTION_ID);
        Scope actualScope = registry.enter(CONNECTION_ID, manager);
        ExecutorService actualExecutor = Executors.newSingleThreadExecutor();
        try {
            registry.apply(CONNECTION_ID, ISCConstants.fb_cancel_raise);
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            Future<?> actualSignal = actualExecutor.submit(() -> registry.apply(CONNECTION_ID, ISCConstants.fb_cancel_raise));
            actualSignal.get(1, TimeUnit.SECONDS);
            Future<?> actualEnd = actualExecutor.submit(() -> registry.endCommand(CONNECTION_ID));
            actualEnd.get(1, TimeUnit.SECONDS);
        } finally {
            release.countDown();
            actualScope.close();
            actualExecutor.shutdownNow();
        }
    }
    
    @Test
    void assertAbortClosesBackendWhenCancellationIsDisabled() throws Exception {
        ProxyDatabaseConnectionManager manager = mock(ProxyDatabaseConnectionManager.class);
        Connection connection = mock(Connection.class);
        FBConnection firebird = mock(FBConnection.class);
        FbWireDatabase database = mock(FbWireDatabase.class);
        FbWireOperations operations = mock(FbWireOperations.class);
        Multimap<String, Connection> cached = LinkedHashMultimap.create();
        cached.put("foo_db.foo_ds", connection);
        when(manager.getCachedConnections()).thenReturn(cached);
        when(connection.unwrap(FBConnection.class)).thenReturn(firebird);
        when(firebird.getFbDatabase()).thenReturn(database);
        when(database.getWireOperations()).thenReturn(operations);
        CountDownLatch aborted = new CountDownLatch(1);
        doAnswer(invocation -> {
            assertThat(invocation.<byte[]>getArgument(0), is(new byte[]{0, 0, 0, 91, 0, 0, 0, ISCConstants.fb_cancel_abort}));
            aborted.countDown();
            return null;
        }).when(operations).writeDirect(any(byte[].class));
        registry.beginCommand(CONNECTION_ID);
        registry.apply(CONNECTION_ID, ISCConstants.fb_cancel_disable);
        try (Scope ignored = registry.enter(CONNECTION_ID, manager)) {
            registry.apply(CONNECTION_ID, ISCConstants.fb_cancel_abort);
            assertTrue(aborted.await(5, TimeUnit.SECONDS));
        }
    }
}
