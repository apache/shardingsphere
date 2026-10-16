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
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Binary protocol value for int16 for Firebird.
 *
 * <p>Firebird writes an INT128 datum with {@code xdr_int128}, which emits the two 64-bit halves of the value as
 * {@code xdr_hyper} longwords, the high half first (see {@code xdr_int128} and {@code xdr_hyper} of
 * src/common/xdr.cpp of the Firebird server). The wire form is therefore a 16 byte big-endian two's complement value,
 * sign extended, not a zero padded one.</p>
 */
public final class FirebirdInt16BinaryProtocolValue implements FirebirdBinaryProtocolValue {
    
    private static final int INT128_BYTE_LENGTH = 16;
    
    @Override
    public Object read(final FirebirdPacketPayload payload) {
        return payload.getByteBuf().readSlice(16);
    }
    
    @Override
    public void write(final FirebirdPacketPayload payload, final Object value) {
        payload.getByteBuf().writeBytes(toInt128Bytes(toBigInteger(value)));
    }
    
    private BigInteger toBigInteger(final Object value) {
        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toBigInteger();
        }
        Preconditions.checkArgument(value instanceof Integer || value instanceof Long, "Can not encode INT128 value `%s`", value);
        return BigInteger.valueOf(((Number) value).longValue());
    }
    
    private byte[] toInt128Bytes(final BigInteger value) {
        Preconditions.checkArgument(value.bitLength() <= 127, "INT128 value `%s` requires more than 16 bytes of storage", value);
        byte[] minimumBytes = value.toByteArray();
        int offset = Math.max(0, minimumBytes.length - INT128_BYTE_LENGTH);
        byte[] result = new byte[INT128_BYTE_LENGTH];
        if (value.signum() < 0) {
            Arrays.fill(result, (byte) -1);
        }
        System.arraycopy(minimumBytes, offset, result, INT128_BYTE_LENGTH + offset - minimumBytes.length, minimumBytes.length - offset);
        return result;
    }
    
    @Override
    public int getLength(final FirebirdPacketPayload payload) {
        return 16;
    }
}
