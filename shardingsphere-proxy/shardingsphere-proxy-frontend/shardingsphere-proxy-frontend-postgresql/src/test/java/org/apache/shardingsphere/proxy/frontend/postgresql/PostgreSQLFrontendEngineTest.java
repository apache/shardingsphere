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

package org.apache.shardingsphere.proxy.frontend.postgresql;

import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.BinaryStatementRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PostgreSQLFrontendEngineTest {
    
    @Test
    public void assertGetDatabaseType() {
        String actual = new PostgreSQLFrontendEngine().getDatabaseType();
        assertThat(actual, is(new PostgreSQLDatabaseType().getName()));
    }
    
    @Test
    public void assertRelease() {
        BackendConnection backendConnection = mock(BackendConnection.class);
        int connectionId = 1;
        when(backendConnection.getConnectionId()).thenReturn(connectionId);
        BinaryStatementRegistry registry = BinaryStatementRegistry.getInstance();
        registry.register(connectionId);
        assertNotNull(registry.get(connectionId));
        PostgreSQLFrontendEngine frontendEngine = new PostgreSQLFrontendEngine();
        frontendEngine.release(backendConnection);
        assertNull(registry.get(connectionId));
    }
}
