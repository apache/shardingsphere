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

package io.shardingsphere.shardingproxy.backend.jdbc.datasource;

import io.shardingsphere.core.constant.ConnectionMode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class JDBCBackendDataSourceTest {
    
    private JDBCBackendDataSource jdbcBackendDataSource = new JDBCBackendDataSource();
    
    @Before
    public void setUp() {
        setDataSource();
    }
    
    @SneakyThrows
    private void setDataSource() {
        Field field = jdbcBackendDataSource.getClass().getDeclaredField("dataSources");
        field.setAccessible(true);
        field.set(jdbcBackendDataSource, mockDataSources(2));
    }
    
    private Map<String, DataSource> mockDataSources(final int size) {
        Map<String, DataSource> result = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            result.put("ds_" + i, new MockDataSource());
        }
        return result;
    }
    
    @Test
    public void assertGetConnectionFixedOne() throws SQLException {
        Connection actual = jdbcBackendDataSource.getConnection("ds_1");
        assertThat(actual, instanceOf(Connection.class));
    }
    
    @Test
    public void assertGetConnectionsSucceed() throws SQLException {
        List<Connection> actual = jdbcBackendDataSource.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds_1", 5);
        assertEquals(5, actual.size());
    }
    
    @Test(expected = SQLException.class)
    public void assertGetConnectionsFailed() throws SQLException {
        jdbcBackendDataSource.getConnections(ConnectionMode.MEMORY_STRICTLY, "ds_1", 6);
    }
    
    @Test
    public void assertGetConnectionsByMultiThread() {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        List<Future<List<Connection>>> futures = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            futures.add(executorService.submit(new CallableTask(ConnectionMode.MEMORY_STRICTLY, "ds_1", 6)));
        }
        List<Connection> actual = new ArrayList<>();
        for (Future<List<Connection>> each : futures) {
            try {
                actual.addAll(each.get());
            } catch (final Exception ex) {
                assertThat(ex.getMessage(), containsString("Could't get 6 connections one time, partition succeed connection(5) have released!"));
            }
        }
        assertTrue(actual.isEmpty());
        executorService.shutdown();
    }
    
    @RequiredArgsConstructor
    private class CallableTask implements Callable<List<Connection>> {
        
        private final ConnectionMode connectionMode;
        
        private final String datasourceName;
        
        private final int connectionSize;
    
        @Override
        public List<Connection> call() throws SQLException {
            return jdbcBackendDataSource.getConnections(connectionMode, datasourceName, connectionSize);
        }
    }
}
