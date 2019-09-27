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

package info.avalon566.shardingscaling.sync.mysql.binlog.codec;

import io.netty.buffer.ByteBuf;
import lombok.var;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Decimal Value decoder.
 *
 * @author avalon566
 */
public class DecimalValueDecoder {

    private static final int DIG_PER_DEC = 9;

    private static final int[] DIG_TO_BYTES = {0, 1, 1, 2, 2, 3, 3, 4, 4, 4};
    
    /**
     * decode new Decimal.
     *
     * @param meta meta
     * @param in byte buffer
     * @return decimal value
     */
    public static Serializable decodeNewDecimal(final int meta, final ByteBuf in) {
        var precision = meta >> 8;
        var scale = meta & 0xFF;
        var x = precision - scale;
        var ipd = x / DIG_PER_DEC;
        var fpd = scale / DIG_PER_DEC;
        var decimalLength = (ipd << 2) + DIG_TO_BYTES[x - ipd * DIG_PER_DEC] + (fpd << 2) + DIG_TO_BYTES[scale - fpd * DIG_PER_DEC];
        return toDecimal(precision, scale, DataTypesCodec.readBytes(decimalLength, in));
    }

    private static BigDecimal toDecimal(final int precision, final int scale, final byte[] value) {
        var positive = (value[0] & 0x80) == 0x80;
        value[0] ^= 0x80;
        if (!positive) {
            for (int i = 0; i < value.length; i++) {
                value[i] ^= 0xFF;
            }
        }
        var x = precision - scale;
        var ipDigits = x / DIG_PER_DEC;
        var ipDigitsX = x - ipDigits * DIG_PER_DEC;
        var ipSize = (ipDigits << 2) + DIG_TO_BYTES[ipDigitsX];
        var offset = DIG_TO_BYTES[ipDigitsX];
        var ip = offset > 0 ? BigDecimal.valueOf(readFixedLengthIntBE(value, 0, offset)) : BigDecimal.ZERO;
        for (; offset < ipSize; offset += 4) {
            int i = readFixedLengthIntBE(value, offset, 4);
            ip = ip.movePointRight(DIG_PER_DEC).add(BigDecimal.valueOf(i));
        }
        var shift = 0;
        var fp = BigDecimal.ZERO;
        for (; shift + DIG_PER_DEC <= scale; shift += DIG_PER_DEC, offset += 4) {
            var i = readFixedLengthIntBE(value, offset, 4);
            fp = fp.add(BigDecimal.valueOf(i).movePointLeft(shift + DIG_PER_DEC));
        }
        if (shift < scale) {
            var i = readFixedLengthIntBE(value, offset, DIG_TO_BYTES[scale - shift]);
            fp = fp.add(BigDecimal.valueOf(i).movePointLeft(scale));
        }
        var result = ip.add(fp);
        return positive ? result : result.negate();
    }

    private static int readFixedLengthIntBE(final byte[] bytes, final int offset, final int length) {
        var result = 0;
        for (var i = offset; i < (offset + length); i++) {
            result = (result << 8) | (short) (0xff & bytes[i]);
        }
        return result;
    }
}
