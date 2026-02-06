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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol;

import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.codec.decoder.PgBinaryObj;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.codec.encoder.NumericArrayEncoder;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.postgresql.util.ByteConverter;

import java.math.BigDecimal;

/**
 * Binary protocol value for numeric for PostgreSQL.
 */
public final class PostgreSQLNumericBinaryProtocolValue implements PostgreSQLBinaryProtocolValue {
    
    public static final short NUMERIC_NAN = (short) 0xC000;
    
    public static final short NUMERIC_PINF = (short) 0xD000;
    
    public static final short NUMERIC_NINF = (short) 0xF000;
    
    @Override
    public int getColumnLength(final PostgreSQLPacketPayload payload, final Object value) {
        if (value instanceof BigDecimal) {
            return ByteConverter.numeric((BigDecimal) value).length;
        }
        if (value instanceof Double && (Double.isNaN((Double) value) || Double.isInfinite((Double) value))) {
            return 8;
        }
        throw new UnsupportedSQLOperationException("PostgreSQLNumericBinaryProtocolValue.getColumnLength()");
    }
    
    @Override
    public Object read(final PostgreSQLPacketPayload payload, final int parameterValueLength) {
        byte[] bytes = new byte[parameterValueLength];
        payload.getByteBuf().readBytes(bytes);
        Object result = ByteConverter.numeric(bytes);
        if (result instanceof Double) {
            return parseDouble2Binary((Double) result);
        }
        return result;
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload, final Object value) {
        if (value instanceof BigDecimal) {
            payload.writeBytes(ByteConverter.numeric((BigDecimal) value));
            return;
        }
        if (value instanceof Double) {
            double doubleValue = (Double) value;
            if (Double.isNaN(doubleValue)) {
                writeSpecialNumeric(payload, NUMERIC_NAN);
                return;
            }
            if (Double.isInfinite(doubleValue)) {
                writeSpecialNumeric(payload, doubleValue > 0 ? NUMERIC_PINF : NUMERIC_NINF);
                return;
            }
        }
        throw new UnsupportedSQLOperationException("PostgreSQLNumericBinaryProtocolValue.getColumnLength()");
    }
    
    /**
     * writeSpecialNumeric.
     *
     * @param payload payload
     * @param sign sign
     */
    private void writeSpecialNumeric(final PostgreSQLPacketPayload payload, final int sign) {
        // len and weight
        payload.writeInt4(0);
        // sign
        payload.writeInt2(sign);
        // scale
        payload.writeInt2(0);
    }
    
    /**
     * parseDouble2Binary.
     *
     * @param doubleValue doubleValue
     * @return PgBinaryObj
     */
    private static PgBinaryObj parseDouble2Binary(final Double doubleValue) {
        byte[] specialNumericBytes = NumericArrayEncoder.buildSpecialNumericBytes(doubleValue);
        PgBinaryObj result = new PgBinaryObj(specialNumericBytes);
        result.setType("numeric");
        return result;
    }
}
