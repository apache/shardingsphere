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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.fieldlist;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.shardingproxy.backend.BackendHandler;
import io.shardingsphere.shardingproxy.backend.ResultPacket;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ServerErrorCode;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandPacketType;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.ColumnDefinition41Packet;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.FieldCountPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ComFieldListPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private BackendHandler backendHandler;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        setMaxConnectionsSizePerQuery();
    }
    
    private void setMaxConnectionsSizePerQuery() throws ReflectiveOperationException {
        Field field = GlobalRegistry.getInstance().getClass().getDeclaredField("shardingProperties");
        field.setAccessible(true);
        Properties props = new Properties();
        props.setProperty(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY.getKey(), String.valueOf(1));
        field.set(GlobalRegistry.getInstance(), new ShardingProperties(props));
    }
    
    @Test
    public void assertWrite() {
        when(payload.readStringNul()).thenReturn("tbl");
        when(payload.readStringEOF()).thenReturn("-");
        ComFieldListPacket actual = new ComFieldListPacket(1, 1000, ShardingConstant.LOGIC_SCHEMA_NAME, payload, backendConnection);
        assertThat(actual.getSequenceId(), is(1));
        actual.write(payload);
        verify(payload).writeInt1(CommandPacketType.COM_FIELD_LIST.getValue());
        verify(payload).writeStringNul("tbl");
        verify(payload).writeStringEOF("-");
    }
    
    @Test
    public void assertExecuteWhenSuccess() throws SQLException {
        when(payload.readStringNul()).thenReturn("tbl");
        when(payload.readStringEOF()).thenReturn("-");
        when(backendHandler.next()).thenReturn(true, false);
        when(backendHandler.getResultValue()).thenReturn(new ResultPacket(1, Collections.<Object>singletonList("id"), 1, Collections.singletonList(ColumnType.MYSQL_TYPE_VARCHAR)));
        when(backendHandler.execute()).thenReturn(new CommandResponsePackets(new FieldCountPacket(1, 1)));
        ComFieldListPacket packet = new ComFieldListPacket(1, 1000, ShardingConstant.LOGIC_SCHEMA_NAME, payload, backendConnection);
        setBackendHandler(packet);
        Optional<CommandResponsePackets> actual = packet.execute();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getPackets().size(), is(2));
        Iterator<DatabasePacket> databasePackets = actual.get().getPackets().iterator();
        assertColumnDefinition41Packet((ColumnDefinition41Packet) databasePackets.next());
        assertEofPacket((EofPacket) databasePackets.next());
    }
    
    private void assertColumnDefinition41Packet(final ColumnDefinition41Packet actual) {
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getName(), is("id"));
        assertThat(actual.getColumnType(), is(ColumnType.MYSQL_TYPE_VARCHAR));
    }
    
    private void assertEofPacket(final EofPacket actual) {
        assertThat(actual.getSequenceId(), is(2));
    }
    
    @Test
    public void assertExecuteWhenFailure() throws SQLException {
        when(payload.readStringNul()).thenReturn("tbl");
        when(payload.readStringEOF()).thenReturn("-");
        CommandResponsePackets expected = new CommandResponsePackets(new ErrPacket(1, ServerErrorCode.ER_STD_UNKNOWN_EXCEPTION, "unknown"));
        when(backendHandler.execute()).thenReturn(expected);
        ComFieldListPacket packet = new ComFieldListPacket(1, 1000, ShardingConstant.LOGIC_SCHEMA_NAME, payload, backendConnection);
        setBackendHandler(packet);
        Optional<CommandResponsePackets> actual = packet.execute();
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(expected));
    }
    
    @SneakyThrows
    private void setBackendHandler(final ComFieldListPacket packet) {
        Field field = ComFieldListPacket.class.getDeclaredField("backendHandler");
        field.setAccessible(true);
        field.set(packet, backendHandler);
    }
}
