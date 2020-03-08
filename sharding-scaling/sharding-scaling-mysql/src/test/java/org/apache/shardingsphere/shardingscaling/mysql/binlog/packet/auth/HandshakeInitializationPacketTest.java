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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.auth;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class HandshakeInitializationPacketTest {
    
    @Test
    public void assertFromByteBufWithoutClientPluginAuth() {
        byte[] handshakeBytes = ByteBufUtil.decodeHexDump("0a352e352e322d6d32000b00000064764840492d434a00fff7080200000000000000000000000000002a34647c635a776b345e5d3a00");
        ByteBuf handshakeByteBuf = Unpooled.buffer(handshakeBytes.length);
        handshakeByteBuf.writeBytes(handshakeBytes);
        HandshakeInitializationPacket actual = new HandshakeInitializationPacket();
        actual.fromByteBuf(handshakeByteBuf);
        assertThat(actual.getProtocolVersion(), is((short) 0x0a));
        assertThat(actual.getServerVersion(), is("5.5.2-m2"));
        assertThat(actual.getThreadId(), is(11L));
        assertThat(actual.getAuthPluginDataPart1(), is(ByteBufUtil.decodeHexDump("64764840492d434a")));
        assertThat(actual.getServerCapabilities(), is(63487));
        assertThat(actual.getServerCharsetSet(), is((short) 8));
        assertThat(actual.getServerStatus(), is(2));
        assertThat(actual.getServerCapabilities2(), is(0));
        assertThat(actual.getAuthPluginDataPart2(), is(ByteBufUtil.decodeHexDump("2a34647c635a776b345e5d3a")));
        assertNull(actual.getAuthPluginName());
    }
    
    @Test
    public void assertFromByteBufWithClientPluginAuth() {
        byte[] handshakeBytes = ByteBufUtil.decodeHexDump(
            "0a352e362e342d6d372d6c6f6700560a0000524233767a26477200ffff0802000fc015000000000000000000002b7944262f5a5a3330355a47006d7973716c5f6e61746976655f70617373776f726400");
        ByteBuf handshakeByteBuf = Unpooled.buffer(handshakeBytes.length);
        handshakeByteBuf.writeBytes(handshakeBytes);
        HandshakeInitializationPacket actual = new HandshakeInitializationPacket();
        actual.fromByteBuf(handshakeByteBuf);
        assertThat(actual.getProtocolVersion(), is((short) 0x0a));
        assertThat(actual.getServerVersion(), is("5.6.4-m7-log"));
        assertThat(actual.getThreadId(), is(2646L));
        assertThat(actual.getAuthPluginDataPart1(), is(ByteBufUtil.decodeHexDump("524233767a264772")));
        assertThat(actual.getServerCapabilities(), is(65535));
        assertThat(actual.getServerCharsetSet(), is((short) 8));
        assertThat(actual.getServerStatus(), is(2));
        assertThat(actual.getServerCapabilities2(), is(49167));
        assertThat(actual.getAuthPluginDataPart2(), is(ByteBufUtil.decodeHexDump("2b7944262f5a5a3330355a47")));
        assertThat(actual.getAuthPluginName(), is("mysql_native_password"));
    }
}
