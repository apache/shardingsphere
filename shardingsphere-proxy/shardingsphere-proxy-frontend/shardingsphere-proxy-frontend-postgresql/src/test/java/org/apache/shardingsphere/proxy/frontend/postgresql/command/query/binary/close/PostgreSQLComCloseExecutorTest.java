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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.close;

import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.close.PostgreSQLCloseCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.close.PostgreSQLComClosePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Random;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLComCloseExecutorTest {
    
    private static final int CONNECTION_ID = new Random().nextInt() & Integer.MAX_VALUE;
    
    @Mock
    private PostgreSQLComClosePacket packet;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Before
    public void setUp() {
        when(backendConnection.getConnectionId()).thenReturn(CONNECTION_ID);
        PostgreSQLBinaryStatementRegistry.getInstance().register(CONNECTION_ID);
    }
    
    @Test
    public void assertExecuteClosePreparedStatement() throws SQLException {
        when(packet.getType()).thenReturn(PostgreSQLComClosePacket.Type.PREPARED_STATEMENT);
        when(packet.getName()).thenReturn("S_1");
        PostgreSQLComCloseExecutor closeExecutor = new PostgreSQLComCloseExecutor(packet, backendConnection);
        Collection<DatabasePacket<?>> actual = closeExecutor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(instanceOf(PostgreSQLCloseCompletePacket.class)));
    }
    
    @Test
    public void assertExecuteClosePortal() throws SQLException {
        when(packet.getType()).thenReturn(PostgreSQLComClosePacket.Type.PORTAL);
        PostgreSQLComCloseExecutor closeExecutor = new PostgreSQLComCloseExecutor(packet, backendConnection);
        Collection<DatabasePacket<?>> actual = closeExecutor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(instanceOf(PostgreSQLErrorResponsePacket.class)));
    }
}
