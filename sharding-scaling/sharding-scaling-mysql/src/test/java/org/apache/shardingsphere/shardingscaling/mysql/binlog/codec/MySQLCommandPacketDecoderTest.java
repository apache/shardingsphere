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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.codec;

import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.auth.HandshakeInitializationPacket;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.response.ErrorPacket;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.response.InternalResultSet;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.response.OkPacket;
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
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertDecodeUnsupportedProtocolVersion() {
        MySQLCommandPacketDecoder commandPacketDecoder = new MySQLCommandPacketDecoder();
        commandPacketDecoder.decode(null, byteBuf, null);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertDecodeUnsupportedAuthenticationMethod() {
        MySQLCommandPacketDecoder commandPacketDecoder = new MySQLCommandPacketDecoder();
        when(byteBuf.readUnsignedByte()).thenReturn((short) PacketConstants.PROTOCOL_VERSION);
        commandPacketDecoder.decode(null, byteBuf, null);
    }
    
    @Test
    public void assertDecode() {
        MySQLCommandPacketDecoder commandPacketDecoder = new MySQLCommandPacketDecoder();
        List<Object> actual = new ArrayList<>();
        commandPacketDecoder.decode(null, mockHandshakePacket(), actual);
        assertInitial(actual);
        actual.clear();
        commandPacketDecoder.decode(null, mockOkPacket(), actual);
        assertPacketByType(actual, OkPacket.class);
        actual.clear();
        commandPacketDecoder.decode(null, mockErrPacket(), actual);
        assertPacketByType(actual, ErrorPacket.class);
        actual.clear();
        commandPacketDecoder.decode(null, mockResultSetPacket(), actual);
        commandPacketDecoder.decode(null, mockResultSetPacket(), actual);
        commandPacketDecoder.decode(null, mockEofPacket(), actual);
        commandPacketDecoder.decode(null, mockResultSetPacket(), actual);
        commandPacketDecoder.decode(null, mockEofPacket(), actual);
        assertPacketByType(actual, InternalResultSet.class);
    }
    
    private ByteBuf mockHandshakePacket() {
        String handshakePacket = "0a352e372e32312d6c6f6700090000004a592a1f725a0d0900fff7210200ff8115000000000000000000001a437b30323a4d2b514b5870006d"
            + "7973716c5f6e61746976655f70617373776f72640000000002000000";
        byte[] handshakePacketBytes = ByteBufUtil.decodeHexDump(handshakePacket);
        ByteBuf result = Unpooled.buffer(handshakePacketBytes.length);
        result.writeBytes(handshakePacketBytes);
        return result;
    }
    
    private void assertInitial(final List<Object> actual) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), instanceOf(HandshakeInitializationPacket.class));
        HandshakeInitializationPacket actualPacket = (HandshakeInitializationPacket) actual.get(0);
        assertThat(actualPacket.getProtocolVersion(), is((short) 0x0a));
        assertThat(actualPacket.getServerVersion(), is("5.7.21-log"));
        assertThat(actualPacket.getThreadId(), is(9L));
        assertThat(actualPacket.getServerCharsetSet(), is((short) 33));
        assertThat(actualPacket.getServerStatus(), is(2));
        assertThat(actualPacket.getServerCapabilities(), is(63487));
        assertThat(actualPacket.getServerCapabilities2(), is(33279));
        assertThat(actualPacket.getAuthPluginName(), is("mysql_native_password"));
    }
    
    private ByteBuf mockOkPacket() {
        when(byteBuf.getByte(0)).thenReturn(PacketConstants.OK_PACKET_MARK);
        return byteBuf;
    }
    
    private ByteBuf mockErrPacket() {
        when(byteBuf.getByte(0)).thenReturn(PacketConstants.ERR_PACKET_MARK);
        return byteBuf;
    }
    
    private ByteBuf mockResultSetPacket() {
        when(byteBuf.getByte(0)).thenReturn((byte) 1);
        return byteBuf;
    }
    
    private ByteBuf mockEofPacket() {
        when(byteBuf.getByte(0)).thenReturn(PacketConstants.EOF_PACKET_MARK);
        return byteBuf;
    }
    
    private void assertPacketByType(final List<Object> actual, final Class clazz) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), instanceOf(clazz));
    }
}
