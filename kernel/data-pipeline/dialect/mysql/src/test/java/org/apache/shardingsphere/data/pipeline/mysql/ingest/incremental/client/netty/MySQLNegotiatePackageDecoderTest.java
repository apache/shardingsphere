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
import io.netty.channel.ChannelPipeline;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLAuthMoreDataPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLAuthSwitchRequestPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLHandshakePacket;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLNegotiatePackageDecoderTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext channelHandlerContext;
    
    @BeforeEach
    void setup() {
        when(channelHandlerContext.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
    }
    
    @Test
    void assertDecodeUnsupportedResponseHeader() throws ReflectiveOperationException {
        MySQLNegotiatePackageDecoder negotiatePackageDecoder = new MySQLNegotiatePackageDecoder();
        Plugins.getMemberAccessor().set(MySQLNegotiatePackageDecoder.class.getDeclaredField("handshakeReceived"), negotiatePackageDecoder, true);
        List<Object> actual = new LinkedList<>();
        assertThrows(UnsupportedSQLOperationException.class, () -> negotiatePackageDecoder.decode(channelHandlerContext, unsupportedHeaderPacket(), actual));
        assertTrue(actual.isEmpty());
    }
    
    private ByteBuf unsupportedHeaderPacket() {
        ByteBuf result = Unpooled.buffer();
        result.writeByte(0x10);
        return result;
    }
    
    @Test
    void assertDecodeHandshakePacket() {
        MySQLNegotiatePackageDecoder commandPacketDecoder = new MySQLNegotiatePackageDecoder();
        List<Object> actual = new LinkedList<>();
        commandPacketDecoder.decode(channelHandlerContext, handshakePacket(), actual);
        assertHandshakePacket(actual);
    }
    
    private ByteBuf handshakePacket() {
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
        ByteBuf result = Unpooled.buffer();
        result.writeByte(MySQLAuthSwitchRequestPacket.HEADER);
        result.writeBytes("mysql_native_password".getBytes(StandardCharsets.UTF_8));
        result.writeByte(0);
        result.writeBytes("foo_auth_plugin_data_1234".getBytes(StandardCharsets.UTF_8));
        result.writeByte(0);
        return result;
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
        ByteBuf result = Unpooled.buffer();
        result.writeByte(MySQLAuthMoreDataPacket.HEADER);
        result.writeBytes(new byte[]{1, 2, 3});
        return result;
    }
    
    @Test
    void assertDecodeErrPacket() throws ReflectiveOperationException {
        MySQLNegotiatePackageDecoder negotiatePackageDecoder = new MySQLNegotiatePackageDecoder();
        Plugins.getMemberAccessor().set(MySQLNegotiatePackageDecoder.class.getDeclaredField("handshakeReceived"), negotiatePackageDecoder, true);
        List<Object> actual = new LinkedList<>();
        negotiatePackageDecoder.decode(channelHandlerContext, errPacket(), actual);
        assertPacketByType(actual, MySQLErrPacket.class);
        MySQLErrPacket actualPacket = (MySQLErrPacket) actual.get(0);
        assertThat(actualPacket.getErrorCode(), is(1234));
        assertThat(actualPacket.getSqlState(), is("HY000"));
        assertThat(actualPacket.getErrorMessage(), is("foo_error"));
    }
    
    private ByteBuf errPacket() {
        ByteBuf result = Unpooled.buffer();
        result.writeByte(MySQLErrPacket.HEADER);
        result.writeShortLE(1234);
        result.writeByte('#');
        result.writeBytes("HY000".getBytes(StandardCharsets.UTF_8));
        result.writeBytes("foo_error".getBytes(StandardCharsets.UTF_8));
        return result;
    }
    
    @Test
    void assertDecodeOkPacketAndRemoveDecoder() throws ReflectiveOperationException {
        MySQLNegotiatePackageDecoder negotiatePackageDecoder = new MySQLNegotiatePackageDecoder();
        Plugins.getMemberAccessor().set(MySQLNegotiatePackageDecoder.class.getDeclaredField("handshakeReceived"), negotiatePackageDecoder, true);
        List<Object> actual = new LinkedList<>();
        negotiatePackageDecoder.decode(channelHandlerContext, okPacket(), actual);
        assertPacketByType(actual, MySQLOKPacket.class);
        ChannelPipeline actualPipeline = channelHandlerContext.channel().pipeline();
        verify(actualPipeline).remove(negotiatePackageDecoder);
    }
    
    private ByteBuf okPacket() {
        ByteBuf result = Unpooled.buffer();
        result.writeByte(MySQLOKPacket.HEADER);
        result.writeByte(0);
        result.writeByte(0);
        result.writeShortLE(2);
        result.writeShortLE(0);
        return result;
    }
    
    private void assertPacketByType(final List<Object> actual, final Class<?> clazz) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), isA(clazz));
    }
}
