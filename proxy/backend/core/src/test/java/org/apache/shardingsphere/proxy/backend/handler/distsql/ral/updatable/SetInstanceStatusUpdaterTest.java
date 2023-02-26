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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetInstanceStatusStatement;
import org.apache.shardingsphere.infra.state.StateType;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.junit.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public final class SetInstanceStatusUpdaterTest {
    
    @Test(expected = UnsupportedSQLOperationException.class)
    public void assertExecuteWithNotNotClusterMode() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getInstanceContext().isCluster()).thenReturn(false);
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            SetInstanceStatusUpdater updater = new SetInstanceStatusUpdater();
            updater.executeUpdate("foo", new SetInstanceStatusStatement("ENABLE", "instanceID"));
        }
    }
    
    @Test(expected = UnsupportedSQLOperationException.class)
    public void assertExecuteWithNotExistsInstanceID() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getInstanceContext().isCluster()).thenReturn(true);
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            SetInstanceStatusUpdater updater = new SetInstanceStatusUpdater();
            updater.executeUpdate("foo", new SetInstanceStatusStatement("ENABLE", "instanceID"));
        }
    }
    
    @Test(expected = UnsupportedSQLOperationException.class)
    public void assertExecuteWithCurrentUsingInstance() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getInstanceContext().isCluster()).thenReturn(true);
        when(contextManager.getInstanceContext().getInstance().getCurrentInstanceId()).thenReturn("instanceID");
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            SetInstanceStatusUpdater updater = new SetInstanceStatusUpdater();
            updater.executeUpdate("foo", new SetInstanceStatusStatement("DISABLE", "instanceID"));
        }
    }
    
    @Test(expected = UnsupportedSQLOperationException.class)
    public void assertExecuteWithAlreadyDisableInstance() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getInstanceContext().isCluster()).thenReturn(true);
        when(contextManager.getInstanceContext().getInstance().getCurrentInstanceId()).thenReturn("currentInstance");
        when(contextManager.getInstanceContext().getComputeNodeInstanceById("instanceID").isPresent()).thenReturn(true);
        when(contextManager.getInstanceContext().getComputeNodeInstanceById("instanceID").get().getState().getCurrentState()).thenReturn(StateType.CIRCUIT_BREAK);
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            SetInstanceStatusUpdater updater = new SetInstanceStatusUpdater();
            updater.executeUpdate("foo", new SetInstanceStatusStatement("DISABLE", "instanceID"));
        }
    }
}
