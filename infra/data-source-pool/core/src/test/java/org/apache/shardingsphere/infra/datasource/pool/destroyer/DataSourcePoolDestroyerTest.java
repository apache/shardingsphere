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

package org.apache.shardingsphere.infra.datasource.pool.destroyer;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class DataSourcePoolDestroyerTest {
    
    @Test
    void assertAsyncDestroyWithoutAutoCloseableDataSource() {
        DataSource dataSource = mock(DataSource.class);
        assertDoesNotThrow(() -> new DataSourcePoolDestroyer(dataSource).asyncDestroy());
        verifyNoInteractions(dataSource);
    }
    
    @Test
    void assertAsyncDestroyWithAutoCloseableDataSourceWithoutActiveDetector() throws Exception {
        DataSource dataSource = mock(DataSource.class, withSettings().extraInterfaces(AutoCloseable.class));
        AutoCloseable closeable = (AutoCloseable) dataSource;
        ExecutorService executor = mockSynchronousExecutor();
        try (
                MockedStatic<Executors> mockedExecutors = mockStatic(Executors.class);
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class)) {
            mockedExecutors.when(Executors::newSingleThreadExecutor).thenReturn(executor);
            typedSPILoader.when(() -> TypedSPILoader.findService(DataSourcePoolActiveDetector.class, dataSource.getClass().getName())).thenReturn(Optional.empty());
            new DataSourcePoolDestroyer(dataSource).asyncDestroy();
            verify(executor).execute(any(Runnable.class));
            verify(executor).shutdown();
            verify(closeable).close();
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("asyncDestroyWithActiveDetectorArguments")
    void assertAsyncDestroyWithActiveDetector(final String name, final boolean interruptedBeforeDestroy, final int expectedContainsActiveConnectionCount) throws Exception {
        DataSource dataSource = mock(DataSource.class, withSettings().extraInterfaces(AutoCloseable.class));
        AutoCloseable closeable = (AutoCloseable) dataSource;
        DataSourcePoolActiveDetector activeDetector = mock(DataSourcePoolActiveDetector.class);
        ExecutorService executor = mockSynchronousExecutor();
        if (1 == expectedContainsActiveConnectionCount) {
            when(activeDetector.containsActiveConnection(dataSource)).thenReturn(false);
        } else {
            when(activeDetector.containsActiveConnection(dataSource)).thenReturn(true, false);
        }
        try {
            if (interruptedBeforeDestroy) {
                Thread.currentThread().interrupt();
            }
            try (
                    MockedStatic<Executors> mockedExecutors = mockStatic(Executors.class);
                    MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class)) {
                mockedExecutors.when(Executors::newSingleThreadExecutor).thenReturn(executor);
                typedSPILoader.when(() -> TypedSPILoader.findService(DataSourcePoolActiveDetector.class, dataSource.getClass().getName())).thenReturn(Optional.of(activeDetector));
                new DataSourcePoolDestroyer(dataSource).asyncDestroy();
            }
            verify(executor).execute(any(Runnable.class));
            verify(executor).shutdown();
            verify(activeDetector, times(expectedContainsActiveConnectionCount)).containsActiveConnection(dataSource);
            verify(closeable).close();
            MatcherAssert.assertThat(Thread.currentThread().isInterrupted(), CoreMatchers.is(interruptedBeforeDestroy));
        } finally {
            Thread.interrupted();
        }
    }
    
    private static ExecutorService mockSynchronousExecutor() {
        ExecutorService result = mock(ExecutorService.class);
        doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).when(result).execute(any(Runnable.class));
        return result;
    }
    
    private static Stream<Arguments> asyncDestroyWithActiveDetectorArguments() {
        return Stream.of(
                Arguments.of("with inactive connection detector", false, 1),
                Arguments.of("with active connection detector", false, 2),
                Arguments.of("with interrupted waiting thread", true, 2));
    }
}
