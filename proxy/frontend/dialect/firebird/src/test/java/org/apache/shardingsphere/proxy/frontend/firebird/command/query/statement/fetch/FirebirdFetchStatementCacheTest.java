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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch;

import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FirebirdFetchStatementCacheTest {
    
    private final FirebirdFetchStatementCache cache = FirebirdFetchStatementCache.getInstance();
    
    private Map<Integer, Map<Integer, ?>> statementRegistry;
    
    private final ConnectionSession connectionSession = mock(ConnectionSession.class);
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        statementRegistry = (Map<Integer, Map<Integer, ?>>) Plugins.getMemberAccessor().get(FirebirdFetchStatementCache.class.getDeclaredField("statementRegistry"), cache);
        statementRegistry.clear();
    }
    
    @AfterEach
    void tearDown() {
        cache.unregisterConnection(1);
    }
    
    @Test
    void assertGetInstance() {
        assertThat(FirebirdFetchStatementCache.getInstance(), is(cache));
    }
    
    @Test
    void assertRegisterConnection() {
        cache.registerConnection(1);
        assertTrue(statementRegistry.containsKey(1));
    }
    
    @Test
    void assertRegisterStatement() {
        ProxyBackendHandler handler = mock(ProxyBackendHandler.class);
        cache.registerConnection(1);
        cache.registerStatement(1, 10, handler, connectionSession);
        assertThat(cache.getFetchBackendHandler(1, 10), sameInstance(handler));
        assertThat(cache.getConnectionSession(1, 10), sameInstance(connectionSession));
    }
    
    @Test
    void assertGetFetchBackendHandler() {
        ProxyBackendHandler handler = mock(ProxyBackendHandler.class);
        cache.registerConnection(1);
        cache.registerStatement(1, 10, handler, connectionSession);
        assertThat(cache.getFetchBackendHandler(1, 10), is(handler));
        assertNull(cache.getFetchBackendHandler(1, 11));
    }
    
    @Test
    void assertGetFetchBackendHandlerWithoutConnection() {
        assertNull(cache.getFetchBackendHandler(1, 10));
    }
    
    @Test
    void assertUnregisterStatement() {
        cache.registerConnection(1);
        cache.registerStatement(1, 10, mock(ProxyBackendHandler.class), connectionSession);
        cache.unregisterStatement(1, 10);
        assertNull(cache.getFetchBackendHandler(1, 10));
    }
    
    @Test
    void assertUnregisterStatementWithoutConnection() {
        cache.unregisterStatement(1, 10);
        assertFalse(statementRegistry.containsKey(1));
    }
    
    @Test
    void assertUnregisterConnection() throws Exception {
        ProxyBackendHandler handler = mock(ProxyBackendHandler.class);
        cache.registerConnection(1);
        cache.registerStatement(1, 10, handler, connectionSession);
        cache.unregisterConnection(1);
        verify(handler).close();
        assertFalse(statementRegistry.containsKey(1));
    }
    
    @Test
    void assertUnregisterTransaction() {
        ConnectionSession otherSession = mock(ConnectionSession.class);
        ProxyBackendHandler otherHandler = mock(ProxyBackendHandler.class);
        cache.registerConnection(1);
        cache.registerStatement(1, 10, mock(ProxyBackendHandler.class), connectionSession);
        cache.registerStatement(1, 11, otherHandler, otherSession);
        cache.unregisterTransaction(1, connectionSession);
        assertNull(cache.getFetchBackendHandler(1, 10));
        assertNull(cache.getConnectionSession(1, 10));
        assertThat(cache.getFetchBackendHandler(1, 11), sameInstance(otherHandler));
        assertThat(cache.getConnectionSession(1, 11), sameInstance(otherSession));
    }
}
