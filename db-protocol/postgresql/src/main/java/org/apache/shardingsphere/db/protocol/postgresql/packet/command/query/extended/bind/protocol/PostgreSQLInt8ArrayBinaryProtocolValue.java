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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.protocol;

import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;

/**
 * Binary protocol value for int8 array for PostgreSQL.
 */
public final class PostgreSQLInt8ArrayBinaryProtocolValue implements PostgreSQLBinaryProtocolValue {
    
    private static final PostgreSQLArrayParameterDecoder ARRAY_PARAMETER_DECODER = new PostgreSQLArrayParameterDecoder();
    private static final int DIMENSIONS = 1;
    private static final int FLAGS_NO_NULLS = 0;
    private static final int LOWER_BOUND = 1;
    private static final int INT8_LENGTH = 8;
    
    @Override
    public int getColumnLength(final PostgreSQLPacketPayload payload, final Object value) {
        throw new UnsupportedSQLOperationException("PostgreSQLInt8ArrayBinaryProtocolValue.getColumnLength()");
    }
    
    @Override
    public Object read(final PostgreSQLPacketPayload payload, final int parameterValueLength) {
        byte[] bytes = new byte[parameterValueLength];
        payload.getByteBuf().readBytes(bytes);
        return ARRAY_PARAMETER_DECODER.decodeInt8Array(bytes, '{' != bytes[0]);
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload, final Object value) {
        if (!(value instanceof Object[])) {
            throw new IllegalArgumentException("Expected Object[] for int8 array, but got: " + value.getClass().getSimpleName());
        }
        Object[] elements = (Object[]) value;
        final int DIMENSIONS = 1;
        final int FLAGS_NO_NULLS = 0;
        final int INT8_OID = 20;
        final int LOWER_BOUND = 1;
        final int INT8_LENGTH = 8;
        payload.writeInt4(DIMENSIONS);
        payload.writeInt4(FLAGS_NO_NULLS);
        payload.writeInt4(INT8_OID);
        payload.writeInt4(elements.length);
        payload.writeInt4(LOWER_BOUND);
        for (Object element : elements) {
            if (element == null) {
                payload.writeInt4(-1);
            } else if (element instanceof Number) {
                payload.writeInt4(INT8_LENGTH);
                payload.writeInt8(((Number) element).longValue());
            } else {
                throw new IllegalArgumentException("Invalid element type in int8 array: " + element);
            }
        }
    }
}
