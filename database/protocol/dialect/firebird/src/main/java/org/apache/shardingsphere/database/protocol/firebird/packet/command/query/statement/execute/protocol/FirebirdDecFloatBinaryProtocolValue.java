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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.extern.decimal.Decimal128;
import org.firebirdsql.extern.decimal.Decimal64;

import java.math.BigDecimal;

/**
 * Binary protocol value for decfloat for Firebird.
 *
 * <p>Firebird writes a {@code dtype_dec64} datum with {@code xdr_dec64} and a {@code dtype_dec128} datum with
 * {@code xdr_dec128}, both of which transport the IEEE 754 decimal bit pattern in longwords, see the {@code dtype_dec64}
 * and {@code dtype_dec128} cases of src/common/xdr.cpp of the Firebird server. A DECFLOAT(16) value therefore occupies
 * 8 bytes and a DECFLOAT(34) value 16 bytes on the wire, whatever the descriptor storage length of the value is.</p>
 *
 * <p>The bit pattern is produced by the decNumber implementation shipped with the Firebird JDBC driver, whose byte
 * encoding is documented as network byte order and matches the longword order of {@code xdr_decfloat_hyper}.</p>
 */
@RequiredArgsConstructor
public final class FirebirdDecFloatBinaryProtocolValue implements FirebirdBinaryProtocolValue {
    
    private static final int DEC16_BYTE_LENGTH = 8;
    
    private static final int DEC34_BYTE_LENGTH = 16;
    
    private final FirebirdBinaryColumnType columnType;
    
    @Override
    public Object read(final FirebirdPacketPayload payload) {
        byte[] bytes = new byte[getByteLength()];
        payload.getByteBuf().readBytes(bytes);
        return FirebirdBinaryColumnType.DEC16 == columnType ? Decimal64.parseBytes(bytes) : Decimal128.parseBytes(bytes);
    }
    
    private int getByteLength() {
        return FirebirdBinaryColumnType.DEC16 == columnType ? DEC16_BYTE_LENGTH : DEC34_BYTE_LENGTH;
    }
    
    @Override
    public void write(final FirebirdPacketPayload payload, final Object value) {
        BigDecimal decimal = toBigDecimal(value);
        payload.writeBytes(FirebirdBinaryColumnType.DEC16 == columnType ? Decimal64.valueOf(decimal).toBytes() : Decimal128.valueOf(decimal).toBytes());
    }
    
    private BigDecimal toBigDecimal(final Object value) {
        Preconditions.checkArgument(value instanceof Number, "Can not encode DECFLOAT value `%s`", value);
        return value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(value.toString());
    }
    @Override
    public int getLength(final FirebirdPacketPayload payload) {
        return getByteLength();
    }
    
}
