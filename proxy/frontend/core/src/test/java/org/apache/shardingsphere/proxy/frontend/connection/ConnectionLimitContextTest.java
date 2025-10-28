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

package org.apache.shardingsphere.proxy.frontend.connection;

import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class ConnectionLimitContextTest {
    
    @Test
    void assertConnectionsLimited() {
        ContextManager contextManager = mockContextManager(2);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertTrue(ConnectionLimitContext.getInstance().connectionAllowed());
        assertTrue(ConnectionLimitContext.getInstance().connectionAllowed());
        assertFalse(ConnectionLimitContext.getInstance().connectionAllowed());
        ConnectionLimitContext.getInstance().connectionInactive();
        ConnectionLimitContext.getInstance().connectionInactive();
        assertTrue(ConnectionLimitContext.getInstance().connectionAllowed());
        ConnectionLimitContext.getInstance().connectionInactive();
        ConnectionLimitContext.getInstance().connectionInactive();
        ConnectionLimitContext.getInstance().connectionInactive();
    }
    
    @Test
    void assertConnectionsUnlimited() {
        ContextManager contextManager = mockContextManager(0);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertFalse(ConnectionLimitContext.getInstance().limitsMaxConnections());
    }
    
    private ContextManager mockContextManager(final int maxConnections) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_MAX_CONNECTIONS)).thenReturn(maxConnections);
        return result;
    }
}
