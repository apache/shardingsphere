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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.close;

import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.close.PostgreSQLCloseCompletePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.close.PostgreSQLComClosePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PortalContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLComCloseExecutorTest {
    
    @Mock
    private PortalContext portalContext;
    
    @Mock
    private PostgreSQLComClosePacket packet;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Test
    void assertExecuteClosePreparedStatement() throws SQLException {
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        when(packet.getType()).thenReturn(PostgreSQLComClosePacket.Type.PREPARED_STATEMENT);
        when(packet.getName()).thenReturn("S_1");
        PostgreSQLComCloseExecutor closeExecutor = new PostgreSQLComCloseExecutor(portalContext, packet, connectionSession);
        Collection<DatabasePacket> actual = closeExecutor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(isA(PostgreSQLCloseCompletePacket.class)));
    }
    
    @Test
    void assertExecuteClosePortal() throws SQLException {
        when(packet.getType()).thenReturn(PostgreSQLComClosePacket.Type.PORTAL);
        String portalName = "C_1";
        when(packet.getName()).thenReturn(portalName);
        PostgreSQLComCloseExecutor closeExecutor = new PostgreSQLComCloseExecutor(portalContext, packet, connectionSession);
        Collection<DatabasePacket> actual = closeExecutor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(isA(PostgreSQLCloseCompletePacket.class)));
        verify(portalContext).close(portalName);
    }
}
