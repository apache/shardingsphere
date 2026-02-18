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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Firebird batch message command packet.
 */
@Getter
public final class FirebirdBatchSendMessageCommandPacket extends FirebirdCommandPacket {
    
    private final int statementHandle;
    
    private final long batchMessageCount;
    
    private final byte[] batchData;
    
    private final List<Object> parameterValues = new ArrayList<>();
    
    public FirebirdBatchSendMessageCommandPacket(final FirebirdPacketPayload payload) {
        payload.skipReserved(4);
        statementHandle = payload.readInt4();
        batchMessageCount = payload.readInt4Unsigned();
        ByteBuf buf = payload.getByteBuf();
        int remaining = buf.readableBytes();
        batchData = new byte[remaining];
        buf.readBytes(batchData);
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
    }
    
    /**
     * Get length of packet.
     *
     * @param payload Firebird packet payload
     * @return length of packet
     */
    public static int getLength(final FirebirdPacketPayload payload) {
        // TODO Do not rely on fixed header subtraction. Implement proper packet length calculation by parsing BATCH_MSG fields.
        int readable = payload.getByteBuf().readableBytes();
        return readable > 12 ? readable - 12 : -1;
    }
}
