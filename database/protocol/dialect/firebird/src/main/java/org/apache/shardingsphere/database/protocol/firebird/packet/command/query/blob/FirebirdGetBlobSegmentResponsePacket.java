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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

/**
 * Firebird get blob segment response packet.
 *
 * <p>Each segment is written as a 2-byte little-endian length followed by the segment data</p>
 *
 * @see <a href="https://firebirdsql.org/file/documentation/html/en/firebirddocs/wireprotocol/firebird-wire-protocol.html#wireprotocol-blobs-create-v11">Firebird wire protocol - blobs</a>
 */
@RequiredArgsConstructor
public final class FirebirdGetBlobSegmentResponsePacket extends FirebirdPacket {
    
    private final byte[] segment;
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        payload.writeInt2LE(segment.length);
        if (segment.length > 0) {
            payload.writeBytes(segment);
        }
    }
}
