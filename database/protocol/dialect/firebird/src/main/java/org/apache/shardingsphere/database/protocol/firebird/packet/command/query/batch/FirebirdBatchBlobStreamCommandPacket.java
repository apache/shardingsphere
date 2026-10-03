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
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBpbVersionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidClumpletStructureException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

/**
 * Firebird batch BLOB stream command packet.
 */
@Getter
public final class FirebirdBatchBlobStreamCommandPacket extends FirebirdCommandPacket {
    
    private final int statementHandle;
    
    private final long length;
    
    private final ByteBuf data;
    
    public FirebirdBatchBlobStreamCommandPacket(final FirebirdPacketPayload payload) {
        payload.skipReserved(4);
        statementHandle = payload.readInt4();
        length = payload.readInt4Unsigned();
        data = payload.getByteBuf().readSlice(payload.getByteBuf().readableBytes());
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
    }
    
    /**
     * Get length of packet.
     *
     * <p>The declared stream length differs from the length of the packet, so the stream is read with the BLOB stream state left by the previous packets.</p>
     *
     * @param payload Firebird packet payload
     * @param blobStream BLOB stream state of the batch, advanced to the end of the packet
     * @return length of packet
     * @throws IndexOutOfBoundsException when packet is incomplete
     * @throws DatabaseProtocolException when stream portion is invalid
     * @throws InvalidClumpletStructureException when BLOB parameter buffer of stream portion has invalid structure
     * @throws InvalidBpbVersionException when BLOB parameter buffer of stream portion has wrong version
     */
    public static int getLength(final FirebirdPacketPayload payload, final FirebirdBatchBlobStream blobStream) {
        int startReaderIndex = payload.getByteBuf().readerIndex();
        payload.skipReserved(8);
        blobStream.skip(payload.getByteBuf(), payload.readInt4Unsigned());
        return payload.getByteBuf().readerIndex() - startReaderIndex;
    }
}
