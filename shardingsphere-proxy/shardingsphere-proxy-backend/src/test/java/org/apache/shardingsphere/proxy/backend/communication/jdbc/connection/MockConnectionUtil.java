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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.connection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * Mock backend connection utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MockConnectionUtil {
    
    /**
     * Mock set cached connections.
     *
     * @param backendConnection backend connection
     * @param dataSourceName datasource name
     * @param connectionSize connection size
     */
    @SneakyThrows(ReflectiveOperationException.class)
    static void setCachedConnections(final BackendConnection backendConnection, final String dataSourceName, final int connectionSize) {
        Multimap<String, Connection> cachedConnections = HashMultimap.create();
        cachedConnections.putAll(dataSourceName, mockNewConnections(connectionSize));
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
        List<Connection> result = new ArrayList<>(connectionSize);
        for (int i = 0; i < connectionSize; i++) {
            result.add(mock(Connection.class));
        }
        return result;
    }
}
