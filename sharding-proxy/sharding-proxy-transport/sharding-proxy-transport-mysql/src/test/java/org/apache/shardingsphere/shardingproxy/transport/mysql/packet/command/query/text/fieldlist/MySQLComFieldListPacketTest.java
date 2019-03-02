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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.fieldlist;

import lombok.SneakyThrows;
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryData;
import org.apache.shardingsphere.shardingproxy.context.GlobalContext;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.payload.MySQLPacketPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLComFieldListPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        setMaxConnectionsSizePerQuery();
    }
    
    private void setMaxConnectionsSizePerQuery() throws ReflectiveOperationException {
        Field field = GlobalContext.getInstance().getClass().getDeclaredField("shardingProperties");
        field.setAccessible(true);
        Properties props = new Properties();
        props.setProperty(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY.getKey(), String.valueOf(1));
        field.set(GlobalContext.getInstance(), new ShardingProperties(props));
    }
    
    @Test
    public void assertWrite() {
        when(payload.readStringNul()).thenReturn("tbl");
        when(payload.readStringEOF()).thenReturn("-");
        MySQLComFieldListPacket actual = new MySQLComFieldListPacket(payload, backendConnection);
        actual.write(payload);
        verify(payload).writeInt1(MySQLCommandPacketType.COM_FIELD_LIST.getValue());
        verify(payload).writeStringNul("tbl");
        verify(payload).writeStringEOF("-");
    }
    
    @Test
    public void assertExecuteWhenSuccess() throws SQLException {
        when(payload.readStringNul()).thenReturn("tbl");
        when(payload.readStringEOF()).thenReturn("-");
        when(databaseCommunicationEngine.next()).thenReturn(true, false);
        when(databaseCommunicationEngine.getQueryData()).thenReturn(new QueryData(Collections.singletonList(Types.VARCHAR), Collections.<Object>singletonList("id")));
        BackendResponse backendResponse = mock(BackendResponse.class);
        when(databaseCommunicationEngine.execute()).thenReturn(backendResponse);
        MySQLComFieldListPacket packet = new MySQLComFieldListPacket(payload, backendConnection);
        setBackendHandler(packet);
        Collection<MySQLPacket> actual = packet.execute();
        assertThat(actual.size(), is(2));
        Iterator<MySQLPacket> databasePackets = actual.iterator();
        assertColumnDefinition41Packet((MySQLColumnDefinition41Packet) databasePackets.next());
        assertEofPacket((MySQLEofPacket) databasePackets.next());
    }
    
    private void assertColumnDefinition41Packet(final MySQLColumnDefinition41Packet actual) {
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getName(), is("id"));
        assertThat(actual.getMySQLColumnType(), is(MySQLColumnType.MYSQL_TYPE_VARCHAR));
    }
    
    private void assertEofPacket(final MySQLEofPacket actual) {
        assertThat(actual.getSequenceId(), is(2));
    }
    
    @Test
    public void assertExecuteWhenFailure() throws SQLException {
        when(payload.readStringNul()).thenReturn("tbl");
        when(payload.readStringEOF()).thenReturn("-");
        BackendResponse expected = new ErrorResponse(new RuntimeException("unknown"));
        when(databaseCommunicationEngine.execute()).thenReturn(expected);
        MySQLComFieldListPacket packet = new MySQLComFieldListPacket(payload, backendConnection);
        setBackendHandler(packet);
        Collection<MySQLPacket> actual = packet.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), instanceOf(MySQLErrPacket.class));
    }
    
    @SneakyThrows
    private void setBackendHandler(final MySQLComFieldListPacket packet) {
        Field field = MySQLComFieldListPacket.class.getDeclaredField("databaseCommunicationEngine");
        field.setAccessible(true);
        field.set(packet, databaseCommunicationEngine);
    }
}
