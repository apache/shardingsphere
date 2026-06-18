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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.decimal;

import lombok.Getter;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.MySQLBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * NEWDECIMAL type value of MySQL binlog protocol.
 *
 * @see <a href="https://github.com/mysql/mysql-server/blob/5.7/strings/decimal.c">bin2decimal</a>
 */
public final class MySQLDecimalBinlogProtocolValue implements MySQLBinlogProtocolValue {
    
    private static final int DEC_BYTE_SIZE = 4;
    
    private static final int DIG_PER_DEC = 9;
    
    private static final int[] DIG_TO_BYTES = {0, 1, 1, 2, 2, 3, 3, 4, 4, 4};
    
    @Override
    public Serializable read(final MySQLBinlogColumnDef columnDef, final MySQLPacketPayload payload) {
        DecimalMetaData decimalMetaData = new DecimalMetaData(columnDef.getColumnMeta());
        return toDecimal(decimalMetaData, payload.readStringFixByBytes(decimalMetaData.getTotalByteLength()));
    }
    
    private static BigDecimal toDecimal(final DecimalMetaData metaData, final byte[] value) {
        boolean positive = 0x80 == (value[0] & 0x80);
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
        while (offset < metaData.getIntegerByteLength()) {
            int i = readFixedLengthIntBE(value, offset, DEC_BYTE_SIZE);
            result = result.movePointRight(DIG_PER_DEC).add(BigDecimal.valueOf(i));
            offset += DEC_BYTE_SIZE;
        }
        return result;
    }
    
    private static BigDecimal decodeScaleValue(final DecimalMetaData metaData, final byte[] value) {
        BigDecimal result = BigDecimal.ZERO;
        int shift = 0;
        int offset = metaData.getIntegerByteLength();
        int scale = metaData.getScale();
        while (shift + DIG_PER_DEC <= scale) {
            result = result.add(BigDecimal.valueOf(readFixedLengthIntBE(value, offset, DEC_BYTE_SIZE)).movePointLeft(shift + DIG_PER_DEC));
            shift += DIG_PER_DEC;
            offset += DEC_BYTE_SIZE;
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
