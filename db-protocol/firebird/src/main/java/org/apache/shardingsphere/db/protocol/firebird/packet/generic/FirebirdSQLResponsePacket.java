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

package org.apache.shardingsphere.db.protocol.firebird.packet.generic;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.apache.shardingsphere.db.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;

/**
 * SQL response packet for Firebird.
 */
@Getter
public final class FirebirdSQLResponsePacket extends FirebirdPacket {

    private final int messageCount;
    private final ByteBuf data;
    
    public FirebirdSQLResponsePacket() {
        messageCount = 0;
        data = null;
    }
    
    public FirebirdSQLResponsePacket(ByteBuf data) {
        messageCount = 1;
        this.data = data;
    }
    
    @Override
    protected void write(FirebirdPacketPayload payload) {
        payload.writeInt4(FirebirdCommandPacketType.SQL_RESPONSE.getValue());
        payload.writeInt4(messageCount);
        if (data != null) {
            payload.getByteBuf().writeBytes(data);
        }
    }
}