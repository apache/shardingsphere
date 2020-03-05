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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.response;

import org.apache.shardingsphere.shardingscaling.mysql.binlog.codec.DataTypesCodec;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

/**
 * MySQL OK packet.
 *
 * <p>
 *     MySQL Internals Manual  /  MySQL Client/Server Protocol  /  Overview  /  Generic Response Packets  /  OK_Packet
 *     https://dev.mysql.com/doc/internals/en/packet-OK_Packet.html
 * </p>
 */
@Getter
public final class OkPacket extends AbstractPacket {
    
    private short fieldCount;
    
    private long affectedRows;
    
    private long insertId;
    
    private int serverStatus;
    
    private int warningCount;
    
    private String message;
    
    @Override
    public void fromByteBuf(final ByteBuf data) {
        this.fieldCount = DataTypesCodec.readUnsignedInt1(data);
        this.affectedRows = DataTypesCodec.readLengthCodedIntLE(data);
        this.insertId = DataTypesCodec.readLengthCodedIntLE(data);
        this.serverStatus = DataTypesCodec.readUnsignedInt2LE(data);
        this.warningCount = DataTypesCodec.readUnsignedInt2LE(data);
        this.message = new String(DataTypesCodec.readBytes(data.readableBytes(), data));
    }
}
