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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.codec.encoder;

import lombok.SneakyThrows;
import org.postgresql.core.Oid;
import org.postgresql.util.ByteConverter;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.PostgreSQLNumericBinaryProtocolValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;

/**
 * process BigDecimal.
 * NaN and   ±Infinity cant convert to  BigDecimal
 * in pg jdbc it will convert to double
 * so we use Number
 */
public final class NumericArrayEncoder extends AbstractArrayEncoder<Number> {
    
    public static final NumericArrayEncoder INSTANCE = new NumericArrayEncoder();
    
    private NumericArrayEncoder() {
        super(Oid.NUMERIC);
    }
    
    @SneakyThrows(IOException.class)
    @Override
    public void write(final Number item, final ByteArrayOutputStream bout, final Charset charset) {
        byte[] numericBytes;
        if (item instanceof BigDecimal) {
            numericBytes = ByteConverter.numeric((BigDecimal) item);
        } else if (item instanceof Double) {
            double d = (Double) item;
            numericBytes = buildSpecialNumericBytes(d);
        } else {
            throw new UnsupportedOperationException("Unsupported Number type: " + item.getClass());
        }
        int length = numericBytes.length;
        bout.write((byte) (length >>> 24));
        bout.write((byte) (length >>> 16));
        bout.write((byte) (length >>> 8));
        bout.write(length);
        bout.write(numericBytes);
    }
    
    @Override
    public String toString(final Number item) {
        if (item == null) {
            return "NULL";
        }
        return item.toString();
    }
    
    /**
     * buildSpecialNumericBytes.
     *
     * @param d NaN or Infinite double value
     * @return binary result
     * @throws IllegalArgumentException if d is not special numeric
     */
    public static byte[] buildSpecialNumericBytes(final double d) {
        if (Double.isNaN(d)) {
            return buildSpecialNumericBytes(PostgreSQLNumericBinaryProtocolValue.NUMERIC_NAN);
        } else if (Double.isInfinite(d)) {
            return buildSpecialNumericBytes(d > 0 ? PostgreSQLNumericBinaryProtocolValue.NUMERIC_PINF : PostgreSQLNumericBinaryProtocolValue.NUMERIC_NINF);
        }
        throw new IllegalArgumentException(d + " is not special numeric");
    }
    
    /**
     * buildSpecialNumericBytes.
     *
     * @param sign sign
     * @return binary value
     */
    private static byte[] buildSpecialNumericBytes(final int sign) {
        byte[] result = new byte[8];
        ByteConverter.int2(result, 4, sign);
        return result;
    }
}
