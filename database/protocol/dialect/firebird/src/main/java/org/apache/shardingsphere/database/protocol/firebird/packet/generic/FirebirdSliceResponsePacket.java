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

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBinaryProtocolValueFactory;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdDecFloatBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdDoubleBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdInt8BinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdTimeTZBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
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
 * <p>The encoder of an element is selected by this packet rather than by {@code FirebirdBinaryProtocolValueFactory}.
 * That factory also encodes the row values of a query result, so changing a registration there would change row value encoding too.
 * Row value encoding is a different path, so the pre-existing factory defects for DEC16, DEC34, INT128 and TIME_TZ stay untouched here.</p>
 *
 * <p>DEC16, DEC34, INT128 and TIME_TZ are encoded here per the XDR contract of their data types, namely 8, 16, 16 and 8 bytes.
 * Every other element type still uses the value returned by the factory.</p>
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
    
    private static final int INT128_BYTE_LENGTH = 16;
    
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
        if (FirebirdBinaryColumnType.INT128 == columnType) {
            for (Object each : elements) {
                payload.getByteBuf().writeBytes(toInt128Bytes(each));
            }
            return;
        }
        FirebirdBinaryProtocolValue protocolValue = getProtocolValue();
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
    
    private byte[] toInt128Bytes(final Object element) {
        BigInteger value = toBigInteger(element);
        Preconditions.checkArgument(value.bitLength() <= 127, "INT128 slice element `%s` requires more than 16 bytes of storage", value);
        byte[] minimumBytes = value.toByteArray();
        int offset = Math.max(0, minimumBytes.length - INT128_BYTE_LENGTH);
        byte[] result = new byte[INT128_BYTE_LENGTH];
        if (value.signum() < 0) {
            Arrays.fill(result, (byte) -1);
        }
        System.arraycopy(minimumBytes, offset, result, INT128_BYTE_LENGTH + offset - minimumBytes.length, minimumBytes.length - offset);
        return result;
    }
    
    private BigInteger toBigInteger(final Object element) {
        if (element instanceof BigInteger) {
            return (BigInteger) element;
        }
        if (element instanceof BigDecimal) {
            return ((BigDecimal) element).toBigInteger();
        }
        Preconditions.checkArgument(element instanceof Integer || element instanceof Long, "Can not encode INT128 slice element `%s`", element);
        return BigInteger.valueOf(((Number) element).longValue());
    }
    
    private FirebirdBinaryProtocolValue getProtocolValue() {
        if (FirebirdBinaryColumnType.DEC16 == columnType) {
            return new FirebirdDecFloatBinaryProtocolValue(FirebirdBinaryColumnType.DEC16);
        }
        if (FirebirdBinaryColumnType.DEC34 == columnType) {
            return new FirebirdDecFloatBinaryProtocolValue(FirebirdBinaryColumnType.DEC34);
        }
        if (FirebirdBinaryColumnType.TIME_TZ == columnType) {
            return new FirebirdTimeTZBinaryProtocolValue();
        }
        if (FirebirdBinaryColumnType.D_FLOAT == columnType) {
            return new FirebirdDoubleBinaryProtocolValue();
        }
        if (FirebirdBinaryColumnType.QUAD == columnType) {
            return new FirebirdInt8BinaryProtocolValue();
        }
        return FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(columnType);
    }
}
