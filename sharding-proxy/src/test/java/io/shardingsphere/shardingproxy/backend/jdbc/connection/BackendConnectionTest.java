/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend.jdbc.connection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.shardingproxy.backend.jdbc.datasource.JDBCBackendDataSource;
import io.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BackendConnectionTest {
    
    @Mock
    private LogicSchema logicSchema;
    
    @Mock
    private JDBCBackendDataSource backendDataSource;
    
    private BackendConnection backendConnection = new BackendConnection();
    
    @Before
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public void setup() {
        when(logicSchema.getBackendDataSource()).thenReturn(backendDataSource);
        backendConnection.setLogicSchema(logicSchema);
    }
    
    @Test
    public void assertGetConnectionCacheIsEmpty() throws SQLException {
        when(backendDataSource.getConnections((ConnectionMode) any(), anyString(), eq(2))).thenReturn(mockNewConnections(2));
        List<Connection> actualConnections = backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 2);
        assertThat(actualConnections.size(), is(2));
        assertThat(backendConnection.getConnectionSize(), is(2));
        assertThat(backendConnection.getStatus(), is(ConnectionStatus.RUNNING));
    }
    
    @Test
    public void assertGetConnectionSizeLessThanCache() throws SQLException {
        setCachedConnections("ds1", 10);
        List<Connection> actualConnections = backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 2);
        assertThat(actualConnections.size(), is(2));
        assertThat(backendConnection.getConnectionSize(), is(10));
        assertThat(backendConnection.getStatus(), is(ConnectionStatus.RUNNING));
    }
    
    @Test
    public void assertGetConnectionSizeGreaterThanCache() throws SQLException {
        setCachedConnections("ds1", 10);
        when(backendDataSource.getConnections((ConnectionMode) any(), anyString(), eq(2))).thenReturn(mockNewConnections(2));
        List<Connection> actualConnections = backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 12);
        assertThat(actualConnections.size(), is(12));
        assertThat(backendConnection.getConnectionSize(), is(12));
        assertThat(backendConnection.getStatus(), is(ConnectionStatus.RUNNING));
    }
    
    @Test
    @SneakyThrows
    public void assertMultiThreadGetConnection() {
        setCachedConnections("ds1", 10);
        when(backendDataSource.getConnections((ConnectionMode) any(), anyString(), eq(2))).thenReturn(mockNewConnections(2));
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                assertOneThreadResult();
            }
        });
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                assertOneThreadResult();
            }
        });
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
    }
    
    @SneakyThrows
    private void assertOneThreadResult() {
        List<Connection> actualConnections = backendConnection.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds1", 12);
        assertThat(actualConnections.size(), is(12));
        assertThat(backendConnection.getConnectionSize(), is(12));
        assertThat(backendConnection.getStatus(), is(ConnectionStatus.RUNNING));
    }
    
    private List<Connection> mockNewConnections(final int connectionSize) {
        List<Connection> result = new ArrayList<>();
        for (int i = 0; i < connectionSize; i++) {
            Connection connection = mock(Connection.class);
            result.add(connection);
        }
        return result;
    }
    
    @SneakyThrows
    private void setCachedConnections(final String dsName, final int connectionSize) {
        Multimap<String, Connection> cachedConnections = HashMultimap.create();
        cachedConnections.putAll(dsName, mockNewConnections(connectionSize));
        Field field = backendConnection.getClass().getDeclaredField("cachedConnections");
        field.setAccessible(true);
        field.set(backendConnection, cachedConnections);
    }
}
