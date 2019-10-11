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

package info.avalon566.shardingscaling.sync.mysql.binlog.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;

/**
 * MySQL packet header.
 *
 * <p>
 *     MySQL Internals Manual  /  MySQL Client/Server Protocol  /  Overview  /  MySQL Packets
 *     https://dev.mysql.com/doc/internals/en/mysql-packet.html
 * </p>
 *
 * @author avalon566
 * @author yangyi
 */
@Data
public final class HeaderPacket implements Packet {

    private int packetBodyLength;
    
    private byte packetSequenceNumber;

    @Override
    public ByteBuf toByteBuf() {
        ByteBuf result = ByteBufAllocator.DEFAULT.heapBuffer(4);
        result.writeByte((byte) (packetBodyLength & 0xFF));
        result.writeByte((byte) (packetBodyLength >>> 8));
        result.writeByte((byte) (packetBodyLength >>> 16));
        result.writeByte(getPacketSequenceNumber());
        return result;
    }

    @Override
    public void fromByteBuf(final ByteBuf data) {
        byte[] buffer = new byte[4];
        buffer[0] = data.readByte();
        buffer[1] = data.readByte();
        buffer[2] = data.readByte();
        buffer[3] = data.readByte();
        this.packetBodyLength = (buffer[0] & 0xFF) | ((buffer[1] & 0xFF) << 8) | ((buffer[2] & 0xFF) << 16);
        this.setPacketSequenceNumber(buffer[3]);
    }
}
