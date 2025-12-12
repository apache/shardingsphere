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
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.InternalResultSet;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLCommandPacketDecoderTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private ByteBuf byteBuf;
    
    @BeforeEach
    void setup() {
        when(channelHandlerContext.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
    }
    
    @Test
    void assertDecodeOkPacket() {
        MySQLCommandPacketDecoder commandPacketDecoder = new MySQLCommandPacketDecoder();
        List<Object> actual = new LinkedList<>();
        commandPacketDecoder.decode(channelHandlerContext, mockOkPacket(), actual);
        assertPacketByType(actual, MySQLOKPacket.class);
    }
    
    private ByteBuf mockOkPacket() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, (short) MySQLOKPacket.HEADER);
        when(byteBuf.getByte(0)).thenReturn((byte) MySQLOKPacket.HEADER);
        return byteBuf;
    }
    
    @Test
    void assertDecodeErrPacket() {
        MySQLCommandPacketDecoder commandPacketDecoder = new MySQLCommandPacketDecoder();
        List<Object> actual = new LinkedList<>();
        commandPacketDecoder.decode(channelHandlerContext, mockErrPacket(), actual);
        assertPacketByType(actual, MySQLErrPacket.class);
    }
    
    private ByteBuf mockErrPacket() {
        when(byteBuf.getByte(0)).thenReturn((byte) MySQLErrPacket.HEADER);
        when(byteBuf.readUnsignedByte()).thenReturn((short) MySQLErrPacket.HEADER);
        return byteBuf;
    }
    
    @Test
    void assertDecodeQueryCommPacket() {
        MySQLCommandPacketDecoder commandPacketDecoder = new MySQLCommandPacketDecoder();
        List<Object> actual = new LinkedList<>();
        commandPacketDecoder.decode(channelHandlerContext, mockEmptyResultSetPacket(), actual);
        commandPacketDecoder.decode(channelHandlerContext, mockFieldDefinition41Packet(), actual);
        commandPacketDecoder.decode(channelHandlerContext, mockEofPacket(), actual);
        commandPacketDecoder.decode(channelHandlerContext, mockEmptyResultSetPacket(), actual);
        commandPacketDecoder.decode(channelHandlerContext, mockEofPacket(), actual);
        assertPacketByType(actual, InternalResultSet.class);
    }
    
    private ByteBuf mockEmptyResultSetPacket() {
        when(byteBuf.getByte(0)).thenReturn((byte) 3);
        return byteBuf;
    }
    
    private ByteBuf mockFieldDefinition41Packet() {
        when(byteBuf.getByte(0)).thenReturn((byte) 3);
        when(byteBuf.readUnsignedByte()).thenReturn((short) 3, (short) 0x0c);
        when(byteBuf.readBytes(new byte[3])).then(invocationOnMock -> {
            byte[] input = invocationOnMock.getArgument(0);
            System.arraycopy("def".getBytes(), 0, input, 0, input.length);
            return byteBuf;
        });
        return byteBuf;
    }
    
    private ByteBuf mockEofPacket() {
        when(byteBuf.getByte(0)).thenReturn((byte) MySQLEofPacket.HEADER);
        when(byteBuf.readUnsignedByte()).thenReturn((short) MySQLEofPacket.HEADER);
        return byteBuf;
    }
    
    private void assertPacketByType(final List<Object> actual, final Class<?> clazz) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), isA(clazz));
    }
}
