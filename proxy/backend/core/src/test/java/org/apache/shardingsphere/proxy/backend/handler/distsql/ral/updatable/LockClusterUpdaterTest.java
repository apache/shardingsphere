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

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.LockClusterStatement;
import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class LockClusterUpdaterTest {
    
    @Test
    void assertExecuteWithNotClusterMode() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        LockClusterUpdater updater = new LockClusterUpdater();
        assertThrows(UnsupportedSQLOperationException.class, () -> updater.executeUpdate("foo", new LockClusterStatement(new AlgorithmSegment("FOO", new Properties()))));
    }
    
    @Test
    void assertExecuteWithLockedCluster() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getInstanceContext().isCluster()).thenReturn(true);
        when(contextManager.getClusterStateContext().getCurrentState()).thenReturn(ClusterState.UNAVAILABLE);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        LockClusterUpdater updater = new LockClusterUpdater();
        assertThrows(IllegalStateException.class, () -> updater.executeUpdate("foo", new LockClusterStatement(new AlgorithmSegment("FOO", new Properties()))));
    }
    
    @Test
    void assertExecuteWithWrongAlgorithm() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getInstanceContext().isCluster()).thenReturn(true);
        when(contextManager.getClusterStateContext().getCurrentState()).thenReturn(ClusterState.OK);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        LockClusterUpdater updater = new LockClusterUpdater();
        assertThrows(ServiceProviderNotFoundException.class, () -> updater.executeUpdate("foo", new LockClusterStatement(new AlgorithmSegment("FOO", new Properties()))));
    }
}
