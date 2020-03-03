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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.codec;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Decimal Value decoder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DecimalValueDecoder {
    
    private static final int DEC_BYTE_SIZE = 4;
    
    private static final int DIG_PER_DEC = 9;
    
    private static final int[] DIG_TO_BYTES = {0, 1, 1, 2, 2, 3, 3, 4, 4, 4};
    
    /**
     * decode new Decimal.
     * <p>
     *     see comment of strings/decimal.c/decimal2bin and strings/decimal.c/bin2decimal
     *     https://github.com/mysql/mysql-server/blob/5.7/strings/decimal.c
     * </p>
     *
     * @param meta meta
     * @param in byte buffer
     * @return decimal value
     */
    public static Serializable decodeNewDecimal(final int meta, final ByteBuf in) {
        DecimalMetaData metaData = new DecimalMetaData(meta);
        return toDecimal(metaData, DataTypesCodec.readBytes(metaData.getTotalByteLength(), in));
    }

    private static BigDecimal toDecimal(final DecimalMetaData metaData, final byte[] value) {
        boolean positive = (value[0] & 0x80) == 0x80;
        value[0] ^= 0x80;
        if (!positive) {
            for (int i = 0; i < value.length; i++) {
                value[i] ^= 0xFF;
            }
        }
        BigDecimal integerValue = decodeIntegerValue(metaData, value);
        BigDecimal scaleValue = decodeScaleValue(metaData, value);
        BigDecimal result = integerValue.add(scaleValue);
        return positive ? result : result.negate();
    }

    private static BigDecimal decodeIntegerValue(final DecimalMetaData metaData, final byte[] value) {
        int offset = DIG_TO_BYTES[metaData.getExtraIntegerSize()];
        BigDecimal result = offset > 0 ? BigDecimal.valueOf(readFixedLengthIntBE(value, 0, offset)) : BigDecimal.ZERO;
        for (; offset < metaData.getIntegerByteLength(); offset += DEC_BYTE_SIZE) {
            int i = readFixedLengthIntBE(value, offset, DEC_BYTE_SIZE);
            result = result.movePointRight(DIG_PER_DEC).add(BigDecimal.valueOf(i));
        }
        return result;
    }
    
    private static BigDecimal decodeScaleValue(final DecimalMetaData metaData, final byte[] value) {
        BigDecimal result = BigDecimal.ZERO;
        int shift = 0;
        int offset = metaData.getIntegerByteLength();
        int scale = metaData.getScale();
        for (; shift + DIG_PER_DEC <= scale; shift += DIG_PER_DEC, offset += DEC_BYTE_SIZE) {
            result = result.add(BigDecimal.valueOf(readFixedLengthIntBE(value, offset, DEC_BYTE_SIZE)).movePointLeft(shift + DIG_PER_DEC));
        }
        if (shift < scale) {
            result = result.add(BigDecimal.valueOf(readFixedLengthIntBE(value, offset, DIG_TO_BYTES[scale - shift])).movePointLeft(scale));
        }
        return result;
    }
    
    private static int readFixedLengthIntBE(final byte[] bytes, final int offset, final int length) {
        int result = 0;
        for (int i = offset; i < (offset + length); i++) {
            result = (result << 8) | (short) (0xff & bytes[i]);
        }
        return result;
    }
    
    @Getter
    private static final class DecimalMetaData {
        
        private final int scale;
        
        private final int extraIntegerSize;
        
        private final int integerByteLength;
        
        private final int totalByteLength;
        
        private DecimalMetaData(final int metaData) {
            scale = metaData & 0xFF;
            int precision = metaData >> 8;
            int integer = precision - scale;
            int fullIntegerSize = integer / DIG_PER_DEC;
            extraIntegerSize = integer - fullIntegerSize * DIG_PER_DEC;
            integerByteLength = (fullIntegerSize << 2) + DIG_TO_BYTES[extraIntegerSize];
            int fullScaleSize = scale / DIG_PER_DEC;
            int extraScaleSize = scale - fullScaleSize * DIG_PER_DEC;
            totalByteLength = integerByteLength + (fullScaleSize << 2) + DIG_TO_BYTES[extraScaleSize];
        }
    }
}
