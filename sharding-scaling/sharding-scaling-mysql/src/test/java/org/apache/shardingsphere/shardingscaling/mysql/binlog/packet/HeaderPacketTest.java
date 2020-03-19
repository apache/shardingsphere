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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class HeaderPacketTest {
    
    private static final int PACKET_LENGTH = 0x00101010;
    
    @Test
    public void assertToByteBuf() {
        HeaderPacket headerPacket = new HeaderPacket();
        headerPacket.setPacketSequenceNumber((byte) 1);
        headerPacket.setPacketBodyLength(PACKET_LENGTH);
        assertThat(headerPacket.toByteBuf(), is(getHeaderPacketByteBuf()));
    }
    
    @Test
    public void assertFromByteBuf() {
        HeaderPacket actual = new HeaderPacket();
        actual.fromByteBuf(getHeaderPacketByteBuf());
        assertThat(actual.getPacketSequenceNumber(), is((byte) 1));
        assertThat(actual.getPacketBodyLength(), is(PACKET_LENGTH));
    }
    
    private ByteBuf getHeaderPacketByteBuf() {
        ByteBuf result = ByteBufAllocator.DEFAULT.heapBuffer(4);
        result.writeByte((byte) PACKET_LENGTH);
        result.writeByte((byte) (PACKET_LENGTH >>> 8));
        result.writeByte((byte) (PACKET_LENGTH >>> 16));
        result.writeByte(1);
        return result;
    }
}
