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

package org.apache.shardingsphere.distsql.handler.executor.lock;

import org.apache.shardingsphere.distsql.handler.executor.spi.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.LockClusterStatement;
import org.apache.shardingsphere.infra.algorithm.core.exception.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.exclusive.ExclusiveOperatorEngine;
import org.apache.shardingsphere.mode.exclusive.callback.ExclusiveOperationVoidCallback;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.lock.exception.LockedClusterException;
import org.apache.shardingsphere.mode.state.ShardingSphereState;
import org.apache.shardingsphere.mode.state.StatePersistService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LockClusterExecutorTest {
    
    private final LockClusterExecutor executor = (LockClusterExecutor) TypedSPILoader.getService(DistSQLUpdateExecutor.class, LockClusterStatement.class);
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideFailureScenarios")
    void assertExecuteUpdateWithFailureScenarios(final String name, final LockClusterStatement sqlStatement,
                                                 final ShardingSphereState state, final Class<? extends Throwable> expectedException) throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getStateContext().getState()).thenReturn(state);
        assertThrows(expectedException, () -> executor.executeUpdate(sqlStatement, contextManager));
        verify(contextManager.getExclusiveOperatorEngine(), never()).operate(any(), anyLong(), any());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideOperationScenarios")
    void assertExecuteUpdateWithOperationScenarios(final String name, final LockClusterStatement sqlStatement,
                                                   final long expectedTimeoutMillis, final ShardingSphereState callbackState,
                                                   final Class<? extends Throwable> expectedException, final ShardingSphereState expectedState) throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ExclusiveOperatorEngine exclusiveOperatorEngine = mock(ExclusiveOperatorEngine.class);
        when(contextManager.getExclusiveOperatorEngine()).thenReturn(exclusiveOperatorEngine);
        when(contextManager.getStateContext().getState()).thenReturn(ShardingSphereState.OK, callbackState);
        StatePersistService stateService = contextManager.getPersistServiceFacade().getStateService();
        doAnswer(invocation -> {
            ExclusiveOperationVoidCallback callback = invocation.getArgument(2);
            callback.execute();
            return null;
        }).when(exclusiveOperatorEngine).operate(any(), anyLong(), any(ExclusiveOperationVoidCallback.class));
        if (null == expectedException) {
            assertDoesNotThrow(() -> executor.executeUpdate(sqlStatement, contextManager));
        } else {
            assertThrows(expectedException, () -> executor.executeUpdate(sqlStatement, contextManager));
        }
        verify(exclusiveOperatorEngine).operate(any(), eq(expectedTimeoutMillis), any(ExclusiveOperationVoidCallback.class));
        if (null == expectedState) {
            verify(stateService, never()).update(any());
        } else {
            verify(stateService).update(expectedState);
        }
    }
    
    private static Stream<Arguments> provideFailureScenarios() {
        return Stream.of(
                Arguments.of("cluster state is unavailable", new LockClusterStatement(new AlgorithmSegment("WRITE", new Properties()), 2000L),
                        ShardingSphereState.UNAVAILABLE, LockedClusterException.class),
                Arguments.of("cluster state is read only", new LockClusterStatement(new AlgorithmSegment("WRITE", new Properties()), 2000L),
                        ShardingSphereState.READ_ONLY, LockedClusterException.class),
                Arguments.of("lock strategy is required", new LockClusterStatement(null, 2000L), ShardingSphereState.OK, MissingRequiredAlgorithmException.class),
                Arguments.of("lock strategy is unsupported", new LockClusterStatement(new AlgorithmSegment("FOO", new Properties()), 2000L),
                        ShardingSphereState.OK, ServiceProviderNotFoundException.class));
    }
    
    private static Stream<Arguments> provideOperationScenarios() {
        return Stream.of(
                Arguments.of("use explicit timeout", new LockClusterStatement(new AlgorithmSegment("WRITE", new Properties()), 2000L),
                        2000L, ShardingSphereState.OK, null, ShardingSphereState.READ_ONLY),
                Arguments.of("use default timeout when absent", new LockClusterStatement(new AlgorithmSegment("WRITE", new Properties())),
                        3000L, ShardingSphereState.OK, null, ShardingSphereState.READ_ONLY),
                Arguments.of("lock reads and writes", new LockClusterStatement(new AlgorithmSegment("READ_WRITE", new Properties()), 2000L),
                        2000L, ShardingSphereState.OK, null, ShardingSphereState.UNAVAILABLE),
                Arguments.of("throw exception when callback state changes", new LockClusterStatement(new AlgorithmSegment("WRITE", new Properties()), 2000L),
                        2000L, ShardingSphereState.UNAVAILABLE, LockedClusterException.class, null),
                Arguments.of("throw exception when callback state becomes read only", new LockClusterStatement(new AlgorithmSegment("WRITE", new Properties()), 2000L),
                        2000L, ShardingSphereState.READ_ONLY, LockedClusterException.class, null));
    }
}
