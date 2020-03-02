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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.binlog;

import org.apache.shardingsphere.shardingscaling.mysql.binlog.codec.DataTypesCodec;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

/**
 * Binlog event header.
 *
 * <p>
 *     https://github.com/mysql/mysql-server/blob/5.7/sql/log_event.h
 *     +---------+---------+---------+------------+-----------+-------+
 *     |timestamp|type code|server_id|event_length|end_log_pos|flags  |
 *     |4 bytes  |1 byte   |4 bytes  |4 bytes     |4 bytes    |2 bytes|
 *     +---------+---------+---------+------------+-----------+-------+
 * </p>
 */
@Setter
@Getter
public final class BinlogEventHeader {
    
    private long timeStamp;
    
    private short typeCode;
    
    private long serverId;
    
    private long eventLength;
    
    private long endLogPos;
    
    private int flags;
    
    /**
     * Init values from {@code ByteBuf}.
     *
     * @param data byte buffer
     */
    public void fromBytes(final ByteBuf data) {
        timeStamp = DataTypesCodec.readUnsignedInt4LE(data);
        typeCode = DataTypesCodec.readUnsignedInt1(data);
        serverId = DataTypesCodec.readUnsignedInt4LE(data);
        eventLength = DataTypesCodec.readUnsignedInt4LE(data);
        endLogPos = DataTypesCodec.readUnsignedInt4LE(data);
        flags = DataTypesCodec.readUnsignedInt2LE(data);
    }
}
