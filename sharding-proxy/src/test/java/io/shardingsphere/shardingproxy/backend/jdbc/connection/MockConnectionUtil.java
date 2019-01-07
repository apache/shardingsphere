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
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Mock backend connection Util.
 *
 * @author zhaojun
 */
class MockConnectionUtil {
    
    /**
     * Mock set cached connections.
     *
     * @param backendConnection backend connection
     * @param dsName datasource name
     * @param connectionSize connection size
     */
    @SneakyThrows
    static void setCachedConnections(final BackendConnection backendConnection, final String dsName, final int connectionSize) {
        Multimap<String, Connection> cachedConnections = HashMultimap.create();
        cachedConnections.putAll(dsName, mockNewConnections(connectionSize));
        Field field = backendConnection.getClass().getDeclaredField("cachedConnections");
        field.setAccessible(true);
        field.set(backendConnection, cachedConnections);
    }
    
    /**
     * Mock new connections.
     *
     * @param connectionSize connection size
     * @return list of connection
     */
    static List<Connection> mockNewConnections(final int connectionSize) {
        List<Connection> result = new ArrayList<>();
        for (int i = 0; i < connectionSize; i++) {
            result.add(mock(Connection.class));
        }
        return result;
    }
    
    static void mockThrowException(final Collection<Connection> connections) throws SQLException {
        for (Connection each : connections) {
            doThrow(SQLException.class).when(each).commit();
            doThrow(SQLException.class).when(each).rollback();
            doThrow(SQLException.class).when(each).close();
        }
    }
}
