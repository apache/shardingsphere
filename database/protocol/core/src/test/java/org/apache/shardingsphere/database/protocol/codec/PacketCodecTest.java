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

package org.apache.shardingsphere.database.protocol.codec;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.test.infra.framework.extension.log.LogCaptureAssertion;
import org.apache.shardingsphere.test.infra.framework.extension.log.LogCaptureExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, LogCaptureExtension.class})
class PacketCodecTest {
    
    @Mock
    private DatabasePacketCodecEngine databasePacketCodecEngine;
    
    @Mock
    private ChannelHandlerContext context;
    
    @Mock
    private Channel channel;
    
    @Mock
    private ChannelId channelId;
    
    private PacketCodec packetCodec;
    
    private Logger packetCodecLogger;
    
    private Level originalLevel;
    
    @BeforeEach
    void setUp() {
        packetCodec = new PacketCodec(databasePacketCodecEngine);
        packetCodecLogger = (Logger) LoggerFactory.getLogger(PacketCodec.class);
        originalLevel = packetCodecLogger.getLevel();
    }
    
    @AfterEach
    void tearDown() {
        packetCodecLogger.setLevel(originalLevel);
    }
    
    @Test
    void assertDecodeWithInvalidHeader(final LogCaptureAssertion logCaptureAssertion) {
        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{1, 2, 3});
        LinkedList<Object> actualOut = new LinkedList<>();
        packetCodecLogger.setLevel(Level.INFO);
        when(databasePacketCodecEngine.isValidHeader(input.readableBytes())).thenReturn(false);
        packetCodec.decode(context, input, actualOut);
        verify(databasePacketCodecEngine).isValidHeader(input.readableBytes());
        logCaptureAssertion.assertLogCount(0);
    }
    
    @Test
    void assertDecodeWithDebugDisabled(final LogCaptureAssertion logCaptureAssertion) {
        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{1, 2, 3});
        LinkedList<Object> actualOut = new LinkedList<>();
        packetCodecLogger.setLevel(Level.INFO);
        when(databasePacketCodecEngine.isValidHeader(input.readableBytes())).thenReturn(true);
        packetCodec.decode(context, input, actualOut);
        verify(databasePacketCodecEngine).isValidHeader(input.readableBytes());
        verify(databasePacketCodecEngine).decode(context, input, actualOut);
        logCaptureAssertion.assertLogCount(0);
    }
    
    @Test
    void assertDecodeWithDebugEnabled(final LogCaptureAssertion logCaptureAssertion) {
        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{1, 2, 3});
        packetCodecLogger.setLevel(Level.DEBUG);
        mockChannelId();
        when(databasePacketCodecEngine.isValidHeader(input.readableBytes())).thenReturn(true);
        LinkedList<Object> actualOut = new LinkedList<>();
        packetCodec.decode(context, input, actualOut);
        verify(databasePacketCodecEngine).isValidHeader(input.readableBytes());
        verify(databasePacketCodecEngine).decode(context, input, actualOut);
        logCaptureAssertion.assertLogCount(1);
        logCaptureAssertion.assertLogContent(0, Level.DEBUG, "Read from client {} :\n{}", false);
    }
    
    @Test
    void assertEncodeWithDebugDisabled(final LogCaptureAssertion logCaptureAssertion) {
        DatabasePacket databasePacket = mock(DatabasePacket.class);
        ByteBuf actualOut = Unpooled.buffer();
        packetCodecLogger.setLevel(Level.INFO);
        packetCodec.encode(context, databasePacket, actualOut);
        verify(databasePacketCodecEngine).encode(context, databasePacket, actualOut);
        logCaptureAssertion.assertLogCount(0);
    }
    
    @Test
    void assertEncodeWithDebugEnabled(final LogCaptureAssertion logCaptureAssertion) {
        DatabasePacket databasePacket = mock(DatabasePacket.class);
        ByteBuf actualOut = Unpooled.buffer();
        packetCodecLogger.setLevel(Level.DEBUG);
        mockChannelId();
        packetCodec.encode(context, databasePacket, actualOut);
        verify(databasePacketCodecEngine).encode(context, databasePacket, actualOut);
        logCaptureAssertion.assertLogCount(1);
        logCaptureAssertion.assertLogContent(0, Level.DEBUG, "Write to client {} :\n{}", false);
    }
    
    private void mockChannelId() {
        when(context.channel()).thenReturn(channel);
        when(channel.id()).thenReturn(channelId);
        when(channelId.asShortText()).thenReturn("foo_id");
    }
}
