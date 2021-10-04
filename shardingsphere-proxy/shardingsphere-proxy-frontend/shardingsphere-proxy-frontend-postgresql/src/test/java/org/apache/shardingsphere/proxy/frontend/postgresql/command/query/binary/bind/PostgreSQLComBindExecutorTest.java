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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.bind;

import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatement;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.PostgreSQLBindCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.PostgreSQLPortal;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLComBindExecutorTest {
    
    @Mock
    private PostgreSQLConnectionContext connectionContext;
    
    @Mock
    private PostgreSQLPortal portal;
    
    @Mock
    private PostgreSQLComBindPacket bindPacket;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws SQLException {
        PostgreSQLBinaryStatementRegistry.getInstance().register(1);
        PostgreSQLBinaryStatementRegistry.getInstance().register(1, "2", "", new EmptyStatement(), Collections.emptyList());
        when(bindPacket.getStatementId()).thenReturn("1");
        when(bindPacket.getPortal()).thenReturn("C_1");
        when(bindPacket.getParameters()).thenReturn(Collections.emptyList());
        when(bindPacket.getResultFormats()).thenReturn(Collections.emptyList());
        when(backendConnection.getConnectionId()).thenReturn(1);
        when(connectionContext.createPortal(anyString(), any(PostgreSQLBinaryStatement.class), any(List.class), any(List.class), eq(backendConnection))).thenReturn(portal);
    }
    
    @Test
    public void assertExecuteEmptyBindPacket() throws SQLException {
        PostgreSQLComBindExecutor executor = new PostgreSQLComBindExecutor(connectionContext, bindPacket, backendConnection);
        Collection<DatabasePacket<?>> actual = executor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(instanceOf(PostgreSQLBindCompletePacket.class)));
        verify(portal).execute();
    }
    
    @Test
    public void assertExecuteBindPacketWithQuerySQLAndReturnEmptyResult() throws SQLException {
        PostgreSQLComBindExecutor executor = new PostgreSQLComBindExecutor(connectionContext, bindPacket, backendConnection);
        Collection<DatabasePacket<?>> actual = executor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(instanceOf(PostgreSQLBindCompletePacket.class)));
        verify(portal).execute();
    }
    
    @Test
    public void assertExecuteBindPacketWithQuerySQL() throws SQLException {
        PostgreSQLComBindExecutor executor = new PostgreSQLComBindExecutor(connectionContext, bindPacket, backendConnection);
        Collection<DatabasePacket<?>> actual = executor.execute();
        assertThat(actual.size(), is(1));
        Iterator<DatabasePacket<?>> actualPackets = actual.iterator();
        assertThat(actualPackets.next(), is(instanceOf(PostgreSQLBindCompletePacket.class)));
        verify(portal).execute();
    }
    
    @Test
    public void assertExecuteBindPacketWithUpdateSQL() throws SQLException {
        PostgreSQLComBindExecutor executor = new PostgreSQLComBindExecutor(connectionContext, bindPacket, backendConnection);
        Collection<DatabasePacket<?>> actual = executor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(instanceOf(PostgreSQLBindCompletePacket.class)));
        verify(portal).execute();
    }
}
