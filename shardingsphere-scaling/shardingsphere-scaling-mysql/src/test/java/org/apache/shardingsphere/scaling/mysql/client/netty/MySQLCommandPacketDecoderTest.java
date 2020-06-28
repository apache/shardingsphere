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

package org.apache.shardingsphere.scaling.mysql.client.netty;

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLStatusFlag;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLHandshakePacket;
import org.apache.shardingsphere.scaling.mysql.client.InternalResultSet;
import org.apache.shardingsphere.scaling.utils.ReflectionUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLCommandPacketDecoderTest {
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test(expected = IllegalArgumentException.class)
    public void assertDecodeUnsupportedProtocolVersion() {
        MySQLCommandPacketDecoder commandPacketDecoder = new MySQLCommandPacketDecoder();
        commandPacketDecoder.decode(null, byteBuf, null);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertDecodeUnsupportedAuthenticationMethod() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, (short) MySQLServerInfo.PROTOCOL_VERSION);
        when(byteBuf.readUnsignedShortLE()).thenReturn(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue());
        MySQLCommandPacketDecoder commandPacketDecoder = new MySQLCommandPacketDecoder();
        commandPacketDecoder.decode(null, byteBuf, null);
    }
    
    @Test
    public void assertDecodeHandshakePacket() {
        MySQLCommandPacketDecoder commandPacketDecoder = new MySQLCommandPacketDecoder();
        List<Object> actual = new ArrayList<>();
        commandPacketDecoder.decode(null, mockHandshakePacket(), actual);
        assertHandshakePacket(actual);
    }
    
    private ByteBuf mockHandshakePacket() {
        String handshakePacket = "000a352e372e32312d6c6f6700090000004a592a1f725a0d0900fff7210200ff8115000000000000000000001a437b30323a4d2b514b5870006d"
            + "7973716c5f6e61746976655f70617373776f72640000000002000000";
        byte[] handshakePacketBytes = ByteBufUtil.decodeHexDump(handshakePacket);
        ByteBuf result = Unpooled.buffer(handshakePacketBytes.length);
        result.writeBytes(handshakePacketBytes);
        return result;
    }
    
    private void assertHandshakePacket(final List<Object> actual) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), instanceOf(MySQLHandshakePacket.class));
        MySQLHandshakePacket actualPacket = (MySQLHandshakePacket) actual.get(0);
        assertThat(actualPacket.getProtocolVersion(), is(0x0a));
        assertThat(actualPacket.getServerVersion(), is("5.7.21-log"));
        assertThat(actualPacket.getConnectionId(), is(9));
        assertThat(actualPacket.getCharacterSet(), is(33));
        assertThat(actualPacket.getStatusFlag().getValue(), is(2));
        assertThat(actualPacket.getCapabilityFlagsLower(), is(63487));
        assertThat(actualPacket.getCapabilityFlagsUpper(), is(33279));
        assertThat(actualPacket.getAuthPluginName(), is("mysql_native_password"));
    }
    
    @Test
    public void assertDecodeOkPacket() throws NoSuchFieldException, IllegalAccessException {
        MySQLCommandPacketDecoder commandPacketDecoder = new MySQLCommandPacketDecoder();
        List<Object> actual = new ArrayList<>();
        ReflectionUtil.setFieldValueToClass(commandPacketDecoder, "auth", true);
        commandPacketDecoder.decode(null, mockOkPacket(), actual);
        assertPacketByType(actual, MySQLOKPacket.class);
    }
    
    private ByteBuf mockOkPacket() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, (short) MySQLOKPacket.HEADER);
        when(byteBuf.getByte(1)).thenReturn((byte) MySQLOKPacket.HEADER);
        return byteBuf;
    }
    
    @Test
    public void assertDecodeErrPacket() throws NoSuchFieldException, IllegalAccessException {
        MySQLCommandPacketDecoder commandPacketDecoder = new MySQLCommandPacketDecoder();
        List<Object> actual = new ArrayList<>();
        ReflectionUtil.setFieldValueToClass(commandPacketDecoder, "auth", true);
        commandPacketDecoder.decode(null, mockErrPacket(), actual);
        assertPacketByType(actual, MySQLErrPacket.class);
    }
    
    private ByteBuf mockErrPacket() {
        when(byteBuf.getByte(1)).thenReturn((byte) MySQLErrPacket.HEADER);
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, (short) MySQLErrPacket.HEADER);
        return byteBuf;
    }
    
    @Test
    public void assertDecodeQueryCommPacket() throws NoSuchFieldException, IllegalAccessException {
        MySQLCommandPacketDecoder commandPacketDecoder = new MySQLCommandPacketDecoder();
        List<Object> actual = new ArrayList<>();
        ReflectionUtil.setFieldValueToClass(commandPacketDecoder, "auth", true);
        commandPacketDecoder.decode(null, mockEmptyResultSetPacket(), actual);
        commandPacketDecoder.decode(null, mockFieldDefinition41Packet(), actual);
        commandPacketDecoder.decode(null, mockEofPacket(), actual);
        commandPacketDecoder.decode(null, mockEmptyResultSetPacket(), actual);
        commandPacketDecoder.decode(null, mockEofPacket(), actual);
        assertPacketByType(actual, InternalResultSet.class);
    }
    
    private ByteBuf mockEmptyResultSetPacket() {
        when(byteBuf.getByte(1)).thenReturn((byte) 3);
        return byteBuf;
    }
    
    private ByteBuf mockFieldDefinition41Packet() {
        when(byteBuf.getByte(1)).thenReturn((byte) 3);
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, (short) 3, (short) 0x0c);
        when(byteBuf.readBytes(new byte[3])).then(invocationOnMock -> {
            byte[] input = invocationOnMock.getArgument(0);
            System.arraycopy("def".getBytes(), 0, input, 0, input.length);
            return byteBuf;
        });
        return byteBuf;
    }
    
    private ByteBuf mockEofPacket() {
        when(byteBuf.getByte(1)).thenReturn((byte) MySQLEofPacket.HEADER);
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, (short) MySQLEofPacket.HEADER);
        return byteBuf;
    }
    
    private void assertPacketByType(final List<Object> actual, final Class clazz) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), instanceOf(clazz));
    }
}
