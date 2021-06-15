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

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.scaling.mysql.client.InternalResultSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLCommandPacketDecoderTest {
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    public void assertDecodeOkPacket() throws NoSuchFieldException, IllegalAccessException {
        MySQLCommandPacketDecoder commandPacketDecoder = new MySQLCommandPacketDecoder();
        List<Object> actual = new LinkedList<>();
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
        List<Object> actual = new LinkedList<>();
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
        List<Object> actual = new LinkedList<>();
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
    
    private void assertPacketByType(final List<Object> actual, final Class<?> clazz) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), instanceOf(clazz));
    }
}
