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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute;

import com.google.common.base.Optional;
import lombok.SneakyThrows;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.TransportResponse;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.MySQLBinaryStatementRegistry;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.fixture.BinaryStatementRegistryUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLComStmtExecutePacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Before
    @After
    public void reset() {
        BinaryStatementRegistryUtil.reset();
    }
    
    @Test
    public void assertWrite() throws SQLException {
        MySQLBinaryStatementRegistry.getInstance().register("SELECT id FROM tbl WHERE id=?", 1);
        when(payload.readInt4()).thenReturn(1);
        when(payload.readInt1()).thenReturn(0, 1);
        MySQLQueryComStmtExecutePacket actual = new MySQLQueryComStmtExecutePacket(1, payload, backendConnection);
        assertThat(actual.getSequenceId(), is(1));
        actual.write(payload);
        verify(payload, times(2)).writeInt4(1);
        verify(payload, times(4)).writeInt1(1);
        verify(payload).writeInt1(0);
        verify(payload).writeStringLenenc("");
    }
    
    @Test
    public void assertExecute() throws SQLException {
        MySQLBinaryStatementRegistry.getInstance().register("SELECT id FROM tbl WHERE id=?", 1);
        DatabaseCommunicationEngine databaseCommunicationEngine = mock(DatabaseCommunicationEngine.class);
        when(payload.readInt4()).thenReturn(1);
        when(payload.readInt1()).thenReturn(0, 1);
        when(databaseCommunicationEngine.execute()).thenReturn(mock(QueryResponse.class));
        when(databaseCommunicationEngine.next()).thenReturn(true, false);
        when(databaseCommunicationEngine.getQueryData()).thenReturn(new QueryData(Collections.singletonList(Types.BIGINT), Collections.<Object>singletonList(99999L)));
        MySQLQueryComStmtExecutePacket packet = new MySQLQueryComStmtExecutePacket(1, payload, backendConnection);
        setBackendHandler(packet, databaseCommunicationEngine);
        Optional<TransportResponse> actualCommandResponsePackets = packet.execute();
        assertTrue(actualCommandResponsePackets.isPresent());
        assertTrue(packet.next());
        MySQLPacket actual = packet.getQueryData();
        assertThat(actual.getSequenceId(), is(3));
        assertFalse(packet.next());
    }
    
    @SneakyThrows
    private void setBackendHandler(final MySQLQueryComStmtExecutePacket packet, final DatabaseCommunicationEngine databaseCommunicationEngine) {
        Field field = MySQLQueryComStmtExecutePacket.class.getDeclaredField("databaseCommunicationEngine");
        field.setAccessible(true);
        field.set(packet, databaseCommunicationEngine);
    }
}
