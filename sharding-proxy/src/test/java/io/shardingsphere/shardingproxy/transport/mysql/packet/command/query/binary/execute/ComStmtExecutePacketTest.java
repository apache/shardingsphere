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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute;

import com.google.common.base.Optional;
import io.shardingsphere.shardingproxy.backend.BackendHandler;
import io.shardingsphere.shardingproxy.backend.ResultPacket;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.BinaryStatementRegistry;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.fixture.BinaryStatementRegistryUtil;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
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
public final class ComStmtExecutePacketTest {
    
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
        BinaryStatementRegistry.getInstance().register("SELECT id FROM tbl WHERE id=?", 1);
        when(payload.readInt4()).thenReturn(1);
        when(payload.readInt1()).thenReturn(0, 1);
        ComStmtExecutePacket actual = new ComStmtExecutePacket(1, payload, backendConnection);
        assertThat(actual.getSequenceId(), is(1));
        actual.write(payload);
        verify(payload, times(2)).writeInt4(1);
        verify(payload, times(4)).writeInt1(1);
        verify(payload).writeInt1(0);
        verify(payload).writeStringLenenc("");
    }
    
    @Test
    public void assertExecute() throws SQLException {
        BinaryStatementRegistry.getInstance().register("SELECT id FROM tbl WHERE id=?", 1);
        BackendHandler backendHandler = mock(BackendHandler.class);
        when(payload.readInt4()).thenReturn(1);
        when(payload.readInt1()).thenReturn(0, 1);
        CommandResponsePackets expectedCommandResponsePackets = new CommandResponsePackets();
        when(backendHandler.execute()).thenReturn(expectedCommandResponsePackets);
        when(backendHandler.next()).thenReturn(true, false);
        when(backendHandler.getResultValue()).thenReturn(new ResultPacket(2, Collections.<Object>singletonList(99999L), 1, Collections.singletonList(ColumnType.MYSQL_TYPE_LONG)));
        ComStmtExecutePacket packet = new ComStmtExecutePacket(1, payload, backendConnection);
        setBackendHandler(packet, backendHandler);
        Optional<CommandResponsePackets> actualCommandResponsePackets = packet.execute();
        assertTrue(actualCommandResponsePackets.isPresent());
        assertThat(actualCommandResponsePackets.get(), is(expectedCommandResponsePackets));
        assertTrue(packet.next());
        DatabasePacket actualResultValue = packet.getResultValue();
        assertThat(actualResultValue.getSequenceId(), is(2));
        assertThat(((BinaryResultSetRowPacket) actualResultValue).getData(), is(Collections.<Object>singletonList(99999L)));
        assertFalse(packet.next());
    }
    
    @SneakyThrows
    private void setBackendHandler(final ComStmtExecutePacket packet, final BackendHandler backendHandler) {
        Field field = ComStmtExecutePacket.class.getDeclaredField("backendHandler");
        field.setAccessible(true);
        field.set(packet, backendHandler);
    }
}
