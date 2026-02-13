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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.lock;

import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.UnlockClusterStatement;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.exclusive.ExclusiveOperatorEngine;
import org.apache.shardingsphere.mode.exclusive.callback.ExclusiveOperationVoidCallback;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.lock.exception.NotLockedClusterException;
import org.apache.shardingsphere.mode.state.ShardingSphereState;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UnlockClusterExecutorTest {
    
    private final UnlockClusterExecutor executor = (UnlockClusterExecutor) TypedSPILoader.getService(DistSQLUpdateExecutor.class, UnlockClusterStatement.class);
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideExecuteUpdateScenarios")
    void assertExecuteUpdate(final String name, final UnlockClusterStatement sqlStatement, final ShardingSphereState initialState, final ShardingSphereState callbackState,
                             final Class<? extends Throwable> expectedException, final long expectedTimeoutMillis,
                             final boolean expectedOperateInvoked, final boolean expectedStateUpdated) throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ExclusiveOperatorEngine exclusiveOperatorEngine = mock(ExclusiveOperatorEngine.class);
        when(contextManager.getExclusiveOperatorEngine()).thenReturn(exclusiveOperatorEngine);
        when(contextManager.getStateContext().getState()).thenReturn(initialState, callbackState);
        if (expectedOperateInvoked) {
            doAnswer(invocation -> {
                ExclusiveOperationVoidCallback callback = invocation.getArgument(2);
                callback.execute();
                return null;
            }).when(exclusiveOperatorEngine).operate(any(), anyLong(), any(ExclusiveOperationVoidCallback.class));
        }
        if (null == expectedException) {
            assertDoesNotThrow(() -> executor.executeUpdate(sqlStatement, contextManager));
        } else {
            assertThrows(expectedException, () -> executor.executeUpdate(sqlStatement, contextManager));
        }
        if (expectedOperateInvoked) {
            verify(exclusiveOperatorEngine).operate(any(), eq(expectedTimeoutMillis), any(ExclusiveOperationVoidCallback.class));
        } else {
            verify(exclusiveOperatorEngine, never()).operate(any(), anyLong(), any(ExclusiveOperationVoidCallback.class));
        }
        if (expectedStateUpdated) {
            verify(contextManager.getPersistServiceFacade().getStateService()).update(ShardingSphereState.OK);
        } else {
            verify(contextManager.getPersistServiceFacade().getStateService(), never()).update(ShardingSphereState.OK);
        }
    }
    
    private static Stream<Arguments> provideExecuteUpdateScenarios() {
        return Stream.of(
                Arguments.of("throw when cluster is not locked", new UnlockClusterStatement(), ShardingSphereState.OK, ShardingSphereState.OK,
                        NotLockedClusterException.class, 3000L, false, false),
                Arguments.of("unlock with explicit timeout", new UnlockClusterStatement(2000L), ShardingSphereState.UNAVAILABLE, ShardingSphereState.UNAVAILABLE,
                        null, 2000L, true, true),
                Arguments.of("unlock with default timeout", new UnlockClusterStatement(), ShardingSphereState.UNAVAILABLE, ShardingSphereState.UNAVAILABLE,
                        null, 3000L, true, true),
                Arguments.of("throw when callback state changes", new UnlockClusterStatement(2000L), ShardingSphereState.UNAVAILABLE, ShardingSphereState.OK,
                        NotLockedClusterException.class, 2000L, true, false));
    }
}
