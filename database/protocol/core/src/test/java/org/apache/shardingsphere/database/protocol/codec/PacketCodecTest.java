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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PacketCodecTest {
    
    @Mock
    private DatabasePacketCodecEngine databasePacketCodecEngine;
    
    @Mock
    private ChannelHandlerContext context;
    
    @Mock
    private ByteBuf byteBuf;
    
    private PacketCodec packetCodec;
    
    @BeforeEach
    void setUp() {
        packetCodec = new PacketCodec(databasePacketCodecEngine);
    }
    
    @Test
    void assertDecodeWithValidHeader() {
        when(byteBuf.readableBytes()).thenReturn(1);
        when(databasePacketCodecEngine.isValidHeader(1)).thenReturn(true);
        packetCodec.decode(context, byteBuf, Collections.emptyList());
        verify(databasePacketCodecEngine).decode(context, byteBuf, Collections.emptyList());
    }
    
    @Test
    void assertDecodeWithInvalidHeader() {
        when(byteBuf.readableBytes()).thenReturn(1);
        when(databasePacketCodecEngine.isValidHeader(1)).thenReturn(false);
        packetCodec.decode(context, byteBuf, Collections.emptyList());
        verify(databasePacketCodecEngine, never()).decode(context, byteBuf, Collections.emptyList());
    }
    
    @Test
    void assertEncode() {
        DatabasePacket databasePacket = mock(DatabasePacket.class);
        packetCodec.encode(context, databasePacket, byteBuf);
        verify(databasePacketCodecEngine).encode(context, databasePacket, byteBuf);
    }
}
