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

import java.util.Collection;

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
 * <p>The server walks the elements of a slice with {@code xdr_slice} and writes every one of them with {@code xdr_datum}, see
 * src/remote/protocol.cpp of the Firebird server, so each element keeps the wire form of its own data type. The wire length of an
 * element is decided by that data type and not by the descriptor storage length, for instance a TIME_TZ element occupies 8 bytes and
 * a DEC34 element 16 bytes.</p>
 *
 * <p>A text array element is the exception spelled out by {@code xdr_datum}: it is written as fixed length opaque data of its
 * storage length, without the leading length field that a variable length value carries, so {@code elementLength} is what decides how
 * many bytes each text element occupies. Its bytes are the raw value when the element carries them, for example a JDBC
 * {@code Types.BINARY} element, and are encoded with the connection charset otherwise. Varying text keeps its own shape and is still
 * written through the binary protocol values.</p>
 *
 * @see <a href="https://firebirdsql.org/file/documentation/html/en/firebirddocs/wireprotocol/firebird-wire-protocol.html#wireprotocol-responses-slice">Firebird wire protocol - slice response</a>
 */
@RequiredArgsConstructor
@Getter
public final class FirebirdSliceResponsePacket extends FirebirdPacket {
    
    private final int sliceLength;
    
    private final FirebirdBinaryColumnType columnType;
    
    private final Collection<Object> elements;
    
    private final int elementLength;
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        payload.writeInt4(FirebirdCommandPacketType.SLICE.getValue());
        payload.writeInt4(sliceLength);
        payload.writeInt4(sliceLength);
        writeSliceData(payload);
    }
    
    private void writeSliceData(final FirebirdPacketPayload payload) {
        if (FirebirdBinaryColumnType.TEXT == columnType || FirebirdBinaryColumnType.LEGACY_TEXT == columnType) {
            for (Object each : elements) {
                writeTextElement(payload, each);
            }
            return;
        }
        FirebirdBinaryProtocolValue protocolValue = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(columnType);
        for (Object each : elements) {
            protocolValue.write(payload, each);
        }
    }
    
    private void writeTextElement(final FirebirdPacketPayload payload, final Object element) {
        if (element instanceof byte[]) {
            payload.writeOpaque((byte[]) element, elementLength);
            return;
        }
        payload.writeOpaque(element.toString(), elementLength);
    }
}
