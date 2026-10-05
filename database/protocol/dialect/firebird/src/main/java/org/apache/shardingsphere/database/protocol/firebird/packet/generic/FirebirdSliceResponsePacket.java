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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValueFactory;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.List;

/**
 * Slice response packet for Firebird.
 *
 * <p>Success response to a get slice request, carrying the requested slice of an array.</p>
 *
 * <p>The total logical byte length of the slice is written twice: once as the slice response length and once as the slice data
 * length, followed by the slice elements. Both lengths are logical lengths, independent of the encoded size of the elements: the
 * elements are written per the XDR binary protocol values, so a SMALLINT element occupies 4 bytes on the wire although its logical
 * size is 2 bytes. The Firebird server likewise writes the same total slice length to both fields.</p>
 *
 * @see <a href="https://firebirdsql.org/file/documentation/html/en/firebirddocs/wireprotocol/firebird-wire-protocol.html#wireprotocol-responses-slice">Firebird wire protocol - slice response</a>
 */
@RequiredArgsConstructor
@Getter
public final class FirebirdSliceResponsePacket extends FirebirdPacket {
    
    private final int sliceLength;
    
    private final FirebirdBinaryColumnType columnType;
    
    private final List<Object> elements;
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        payload.writeInt4(FirebirdCommandPacketType.SLICE.getValue());
        payload.writeInt4(sliceLength);
        payload.writeInt4(sliceLength);
        writeSliceData(payload);
    }
    
    private void writeSliceData(final FirebirdPacketPayload payload) {
        FirebirdBinaryProtocolValue protocolValue = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(columnType);
        for (Object element : elements) {
            protocolValue.write(payload, element);
        }
    }
}
