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

import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.withSettings;

class DataSourcePoolDestroyerTest {
    
    @Test
    void assertAsyncDestroyWithoutAutoCloseableDataSource() {
        DataSource dataSource = mock(DataSource.class);
        assertDoesNotThrow(() -> new DataSourcePoolDestroyer(dataSource).asyncDestroy());
        verifyNoInteractions(dataSource);
    }
    
    @Test
    void assertAsyncDestroyWithAutoCloseableDataSourceWithoutActiveDetector() {
        DataSource dataSource = mock(DataSource.class, withSettings().extraInterfaces(AutoCloseable.class));
        AutoCloseable closeable = (AutoCloseable) dataSource;
        new DataSourcePoolDestroyer(dataSource).asyncDestroy();
        Awaitility.await().atMost(1L, TimeUnit.SECONDS).pollInterval(10L, TimeUnit.MILLISECONDS).untilAsserted(() -> verify(closeable).close());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("asyncDestroyWithActiveDetectorArguments")
    void assertAsyncDestroyWithActiveDetector(final String name, final boolean holdConnection, final boolean interruptWaitingThread) throws SQLException {
        MockedDataSource dataSource = new MockedDataSource();
        Connection connection = null;
        if (holdConnection) {
            connection = dataSource.getConnection();
        }
        try {
            Collection<Long> threadIdsBefore = Thread.getAllStackTraces().keySet().stream().map(Thread::getId).collect(Collectors.toSet());
            new DataSourcePoolDestroyer(dataSource).asyncDestroy();
            if (holdConnection) {
                Thread destroyThread = awaitDestroyThread(threadIdsBefore);
                Awaitility.await().atMost(1L, TimeUnit.SECONDS).pollInterval(10L, TimeUnit.MILLISECONDS).until(() -> Thread.State.TIMED_WAITING == destroyThread.getState());
                if (interruptWaitingThread) {
                    destroyThread.interrupt();
                }
                connection.close();
            }
            Awaitility.await().atMost(1L, TimeUnit.SECONDS).pollInterval(10L, TimeUnit.MILLISECONDS).until(dataSource::isClosed);
            assertTrue(dataSource.isClosed());
        } finally {
            if (null != connection) {
                connection.close();
            }
        }
    }
    
    private Thread awaitDestroyThread(final Collection<Long> threadIdsBefore) {
        Awaitility.await().atMost(1L, TimeUnit.SECONDS).pollInterval(10L, TimeUnit.MILLISECONDS).until(() -> null != findDestroyThread(threadIdsBefore));
        return findDestroyThread(threadIdsBefore);
    }
    
    private Thread findDestroyThread(final Collection<Long> threadIdsBefore) {
        return Thread.getAllStackTraces().keySet().stream().filter(each -> !threadIdsBefore.contains(each.getId()) && each.isAlive() && each.getName().startsWith("pool-")).findFirst().orElse(null);
    }
    
    private static Stream<Arguments> asyncDestroyWithActiveDetectorArguments() {
        return Stream.of(
                Arguments.of("without active connection", false, false),
                Arguments.of("with active connection", true, false),
                Arguments.of("with interrupted waiting thread", true, true));
    }
}
