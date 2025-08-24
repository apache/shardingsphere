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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLAuthMoreDataPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLAuthSwitchRequestPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLHandshakePacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLNegotiatePackageDecoderTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private ByteBuf byteBuf;
    
    @BeforeEach
    void setup() {
        when(channelHandlerContext.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
    }
    
    @Test
    void assertDecodeUnsupportedProtocolVersion() {
        MySQLNegotiatePackageDecoder commandPacketDecoder = new MySQLNegotiatePackageDecoder();
        assertThrows(IllegalArgumentException.class, () -> commandPacketDecoder.decode(channelHandlerContext, byteBuf, null));
    }
    
    @Test
    void assertDecodeHandshakePacket() {
        MySQLNegotiatePackageDecoder commandPacketDecoder = new MySQLNegotiatePackageDecoder();
        List<Object> actual = new LinkedList<>();
        commandPacketDecoder.decode(channelHandlerContext, mockHandshakePacket(), actual);
        assertHandshakePacket(actual);
    }
    
    private ByteBuf mockHandshakePacket() {
        String handshakePacket = "0a352e372e32312d6c6f6700090000004a592a1f725a0d0900fff7210200ff8115000000000000000000001a437b30323a4d2b514b5870006d"
                + "7973716c5f6e61746976655f70617373776f72640000000002000000";
        byte[] handshakePacketBytes = ByteBufUtil.decodeHexDump(handshakePacket);
        ByteBuf result = Unpooled.buffer(handshakePacketBytes.length);
        result.writeBytes(handshakePacketBytes);
        return result;
    }
    
    private void assertHandshakePacket(final List<Object> actual) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), isA(MySQLHandshakePacket.class));
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
    void assertDecodeAuthSwitchRequestPacket() throws ReflectiveOperationException {
        MySQLNegotiatePackageDecoder negotiatePackageDecoder = new MySQLNegotiatePackageDecoder();
        Plugins.getMemberAccessor().set(MySQLNegotiatePackageDecoder.class.getDeclaredField("handshakeReceived"), negotiatePackageDecoder, true);
        List<Object> actual = new LinkedList<>();
        negotiatePackageDecoder.decode(channelHandlerContext, authSwitchRequestPacket(), actual);
        assertPacketByType(actual, MySQLAuthSwitchRequestPacket.class);
    }
    
    private ByteBuf authSwitchRequestPacket() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) MySQLAuthSwitchRequestPacket.HEADER);
        when(byteBuf.getByte(0)).thenReturn((byte) MySQLAuthSwitchRequestPacket.HEADER);
        when(byteBuf.bytesBefore((byte) 0)).thenReturn(20);
        return byteBuf;
    }
    
    @Test
    void assertDecodeAuthMoreDataPacket() throws ReflectiveOperationException {
        MySQLNegotiatePackageDecoder negotiatePackageDecoder = new MySQLNegotiatePackageDecoder();
        Plugins.getMemberAccessor().set(MySQLNegotiatePackageDecoder.class.getDeclaredField("handshakeReceived"), negotiatePackageDecoder, true);
        List<Object> actual = new LinkedList<>();
        negotiatePackageDecoder.decode(channelHandlerContext, authMoreDataPacket(), actual);
        assertPacketByType(actual, MySQLAuthMoreDataPacket.class);
    }
    
    private ByteBuf authMoreDataPacket() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) MySQLAuthMoreDataPacket.HEADER);
        when(byteBuf.getByte(0)).thenReturn((byte) MySQLAuthMoreDataPacket.HEADER);
        return byteBuf;
    }
    
    private void assertPacketByType(final List<Object> actual, final Class<?> clazz) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), isA(clazz));
    }
}
