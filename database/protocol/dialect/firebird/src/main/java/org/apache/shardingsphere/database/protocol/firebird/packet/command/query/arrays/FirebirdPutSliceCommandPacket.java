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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.arrays;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

/**
 * Firebird put slice command packet.
 */
public final class FirebirdPutSliceCommandPacket extends FirebirdCommandPacket {
    
    private final int transactionHandle;
    
    private final long arrayHandle;
    
    private final int sliceLength;
    
    private final ByteBuf sliceDescriptor;
    
    private final ByteBuf sliceParameters;
    
    private final ByteBuf sliceData;
    
    public FirebirdPutSliceCommandPacket(final FirebirdPacketPayload payload) {
        payload.skipReserved(4);
        transactionHandle = payload.readInt4();
        arrayHandle = payload.readInt8();
        sliceLength = payload.readInt4();
        sliceDescriptor = payload.readBuffer();
        sliceParameters = payload.readBuffer();
        sliceData = payload.readBuffer();
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
    }
    
    /**
     * Get the request length, including the opcode and slice data.
     *
     * @param payload Firebird packet payload
     * @return request length in bytes
     */
    public static int getLength(final FirebirdPacketPayload payload) {
        int length = 4 + 4 + 8 + 4;
        int startIndex = payload.getByteBuf().readerIndex();
        length += payload.getBufferLength(startIndex + length);
        length += payload.getBufferLength(startIndex + length);
        length += payload.getBufferLength(startIndex + length);
        return length;
    }
}
