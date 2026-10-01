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
 * Slice response packet for Firebird.
 *
 * <p>Success response to a get slice request, carrying the requested slice of a blob.</p>
 *
 * <p>The slice length is written twice, once as the standalone slice length and once as the length prefix of the slice data
 * buffer, which is padded to a 4-byte boundary.</p>
 *
 * <p>The wire protocol documentation marks this section as possibly not reflecting the actual encoding. The layout here follows
 * the Firebird 5.0.3 implementation, where op_slice encodes the slice length and then a slice of that length and data.</p>
 *
 * @see <a href="https://firebirdsql.org/file/documentation/html/en/firebirddocs/wireprotocol/firebird-wire-protocol.html#wireprotocol-responses-slice">Firebird wire protocol - slice response</a>
 */
@RequiredArgsConstructor
@Getter
public final class FirebirdSliceResponsePacket extends FirebirdPacket {
    
    private final int sliceLength;
    
    private final byte[] sliceData;
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        payload.writeInt4(FirebirdCommandPacketType.SLICE.getValue());
        payload.writeInt4(sliceLength);
        payload.writeBuffer(sliceData);
    }
}
