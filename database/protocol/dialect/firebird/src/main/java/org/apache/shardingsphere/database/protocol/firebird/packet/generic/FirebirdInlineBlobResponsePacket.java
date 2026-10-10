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

package org.apache.shardingsphere.database.protocol.firebird.packet.generic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

/**
 * Inline blob response packet for Firebird.
 *
 * <p>The server sends the blob of a row inline, within the fetch response or SQL response, when it fits within the requested
 * p_sqldata_inline_blob_size including segment lengths. It is up to the client to decide whether to cache the inline blob or to discard it.</p>
 *
 * <p>Both the blob information and the blob data are length prefixed buffers. The blob information uses the same encoding as the
 * response data of a blob information request carrying all blob information items, and the blob data uses the same encoding as the
 * response data of a get segment response.</p>
 *
 * @see <a href="https://firebirdsql.org/file/documentation/html/en/firebirddocs/wireprotocol/firebird-wire-protocol.html#wireprotocol-responses-inline-blob">Firebird wire protocol - inline blob</a>
 */
@RequiredArgsConstructor
@Getter
public final class FirebirdInlineBlobResponsePacket extends FirebirdPacket {
    
    private final int transactionId;
    
    private final long blobId;
    
    private final FirebirdPacket blobInfo;
    
    private final FirebirdPacket blobData;
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        payload.writeInt4(FirebirdCommandPacketType.INLINE_BLOB.getValue());
        payload.writeInt4(transactionId);
        payload.writeInt8(blobId);
        writeBuffer(payload, blobInfo);
        writeBuffer(payload, blobData);
    }
    
    private static void writeBuffer(final FirebirdPacketPayload payload, final FirebirdPacket packet) {
        int lengthIndex = payload.getByteBuf().writerIndex();
        payload.getByteBuf().writeInt(0);
        packet.write(payload);
        int length = payload.getByteBuf().writerIndex() - lengthIndex - Integer.BYTES;
        payload.getByteBuf().setInt(lengthIndex, length);
        payload.getByteBuf().writeZero(payload.getPadding(length));
    }
}
