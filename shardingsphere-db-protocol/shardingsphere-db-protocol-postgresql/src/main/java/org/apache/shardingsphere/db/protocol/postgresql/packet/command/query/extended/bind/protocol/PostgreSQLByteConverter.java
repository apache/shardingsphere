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

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Refer to
 * <a href="https://github.com/pgjdbc/pgjdbc/blob/REL42.3.2/pgjdbc/src/main/java/org/postgresql/util/ByteConverter.java" >
 *      org.postgresql.util.ByteConverter
 * </a>.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLByteConverter {
    
    private static final short NUMERIC_POS = 0x0000;
    
    private static final short NUMERIC_NEG = 0x4000;
    
    private static final short NUMERIC_NAN = (short) 0xC000;
    
    private static final int[] INT_TEN_POWERS = new int[6];
    
    private static final BigInteger[] BI_TEN_POWERS = new BigInteger[32];
    
    private static final BigInteger BI_TEN_THOUSAND = BigInteger.valueOf(10000);
    
    static {
        for (int i = 0; i < INT_TEN_POWERS.length; ++i) {
            INT_TEN_POWERS[i] = (int) Math.pow(10, i);
        }
        for (int i = 0; i < BI_TEN_POWERS.length; ++i) {
            BI_TEN_POWERS[i] = BigInteger.TEN.pow(i);
        }
    }
    
    /**
     * Convert a variable length array of bytes to an number.
     *
     * @param bytes array of bytes that can be decoded as an integer
     * @return number
     */
    public static Number numeric(final byte[] bytes) {
        return numeric(bytes, 0);
    }
    
    /**
     * Convert a variable length array of bytes to an number.
     *
     * @param bytes array of bytes that can be decoded as an integer
     * @param pos index of the start position of the bytes array for number
     * @return number
     */
    public static Number numeric(final byte[] bytes, final int pos) {
        short sign = readShort2(bytes, pos + 4);
        short scale = readShort2(bytes, pos + 6);
        validator(sign, scale);
        if (NUMERIC_NAN == sign) {
            return Double.NaN;
        }
        short len = readShort2(bytes, pos);
        if (0 == len) {
            return new BigDecimal(BigInteger.ZERO, scale);
        }
        short weight = readShort2(bytes, pos + 2);
        if (weight < 0) {
            ++weight;
            return initBigDecimalNoneWeight(bytes, pos, len, weight, sign, scale);
        }
        return 0 == scale ? initBigDecimalNoneScale(bytes, pos, len, weight, sign) : initBigDecimal(bytes, pos, len, weight, sign, scale);
    }
    
    /**
     * Converts a non-null {@link BigDecimal} to binary.
     *
     * @param number number to represent in binary.
     * @return The binary representation of <i>input</i>.
     */
    public static byte[] numeric(final BigDecimal number) {
        BigInteger unscaled = number.unscaledValue().abs();
        int scale = number.scale();
        if (BigInteger.ZERO.equals(unscaled)) {
            return initBytesZeroCase(scale);
        }
        final PositiveShortStack shortStacks = new PositiveShortStack();
        int weight = -1;
        if (scale < 0) {
            scale = Math.abs(scale);
            weight += scale / 4;
            int mod = scale % 4;
            unscaled = unscaled.multiply(tenPower(mod));
            scale = 0;
        }
        if (0 == scale) {
            weight = initShortValuesNoneScaled(shortStacks, unscaled, weight);
        } else {
            weight = initShortValuesScaled(shortStacks, unscaled, scale);
        }
        return initBytes(number, shortStacks, scale, weight);
    }
    
    private static byte[] initBytesZeroCase(final int scale) {
        final byte[] result = new byte[]{0, 0, -1, -1, 0, 0, 0, 0};
        int2(result, 6, Math.max(0, scale));
        return result;
    }
    
    private static int initShortValuesNoneScaled(final PositiveShortStack shortStacks, final BigInteger unscaled, final int weight) {
        int result = weight;
        BigInteger tempUnscaled = unscaled;
        BigInteger maxInteger = BigInteger.valueOf(Long.MAX_VALUE);
        while (unscaled.compareTo(maxInteger) > 0) {
            BigInteger[] pair = unscaled.divideAndRemainder(BI_TEN_THOUSAND);
            tempUnscaled = pair[0];
            shortStacks.push(pair[1].shortValue());
            ++result;
        }
        long unscaledLong = tempUnscaled.longValueExact();
        do {
            shortStacks.push((short) (unscaledLong % 10000));
            unscaledLong = unscaledLong / 10000L;
            ++result;
        } while (0 != unscaledLong);
        return result;
    }
    
    private static int initShortValuesScaled(final PositiveShortStack shortStacks, final BigInteger unscaled, final int scale) {
        int result = -1;
        final BigInteger[] split = unscaled.divideAndRemainder(tenPower(scale));
        BigInteger decimal = split[1];
        BigInteger wholes = split[0];
        if (!BigInteger.ZERO.equals(decimal)) {
            int mod = scale % 4;
            int segments = scale / 4;
            if (0 != mod) {
                decimal = decimal.multiply(tenPower(4 - mod));
                ++segments;
            }
            do {
                final BigInteger[] pair = decimal.divideAndRemainder(BI_TEN_THOUSAND);
                decimal = pair[0];
                shortStacks.push(pair[1].shortValue());
                --segments;
            } while (!BigInteger.ZERO.equals(decimal));
            if (BigInteger.ZERO.equals(wholes)) {
                result -= segments;
            } else {
                for (int i = 0; i < segments; ++i) {
                    shortStacks.push((short) 0);
                }
            }
        }
        while (!BigInteger.ZERO.equals(wholes)) {
            ++result;
            final BigInteger[] pair = wholes.divideAndRemainder(BI_TEN_THOUSAND);
            wholes = pair[0];
            shortStacks.push(pair[1].shortValue());
        }
        return result;
    }
    
    private static byte[] initBytes(final BigDecimal number, final PositiveShortStack shortStacks, final int scale, final int weight) {
        final byte[] result = new byte[8 + (2 * shortStacks.size())];
        int idx = 0;
        int2(result, idx, shortStacks.size());
        idx += 2;
        int2(result, idx, weight);
        idx += 2;
        int2(result, idx, number.signum() == -1 ? NUMERIC_NEG : NUMERIC_POS);
        idx += 2;
        int2(result, idx, Math.max(0, scale));
        idx += 2;
        short s;
        while ((s = shortStacks.pop()) != -1) {
            int2(result, idx, s);
            idx += 2;
        }
        return result;
    }
    
    private static void int2(final byte[] target, final int idx, final int value) {
        target[idx] = (byte) (value >>> 8);
        target[idx + 1] = (byte) value;
    }
    
    private static Number initBigDecimalNoneWeight(final byte[] bytes, final int pos, final short len, final short weight, final short sign, final short scale) {
        int idx = pos + 8;
        short d = readShort2(bytes, idx);
        assert scale > 0;
        int effectiveScale = scale;
        if (weight < 0) {
            effectiveScale += 4 * weight;
        }
        for (int i = 1; i < len && 0 == d; ++i) {
            effectiveScale -= 4;
            idx += 2;
            d = readShort2(bytes, idx);
        }
        assert effectiveScale > 0;
        if (effectiveScale >= 4) {
            effectiveScale -= 4;
        } else {
            d = (short) (d / INT_TEN_POWERS[4 - effectiveScale]);
            effectiveScale = 0;
        }
        BigInteger unscaledBI = null;
        long unscaledInt = d;
        for (int i = 1; i < len; ++i) {
            if (i == 4 && effectiveScale > 2) {
                unscaledBI = BigInteger.valueOf(unscaledInt);
            }
            idx += 2;
            d = readShort2(bytes, idx);
            if (effectiveScale >= 4) {
                if (null == unscaledBI) {
                    unscaledInt *= 10000;
                } else {
                    unscaledBI = unscaledBI.multiply(BI_TEN_THOUSAND);
                }
                effectiveScale -= 4;
            } else {
                if (null == unscaledBI) {
                    unscaledInt *= INT_TEN_POWERS[effectiveScale];
                } else {
                    unscaledBI = unscaledBI.multiply(tenPower(effectiveScale));
                }
                d = (short) (d / INT_TEN_POWERS[4 - effectiveScale]);
                effectiveScale = 0;
            }
            if (null == unscaledBI) {
                unscaledInt += d;
            } else {
                if (0 != d) {
                    unscaledBI = unscaledBI.add(BigInteger.valueOf(d));
                }
            }
        }
        if (null == unscaledBI) {
            unscaledBI = BigInteger.valueOf(unscaledInt);
        }
        if (effectiveScale > 0) {
            unscaledBI = unscaledBI.multiply(tenPower(effectiveScale));
        }
        if (NUMERIC_NEG == sign) {
            unscaledBI = unscaledBI.negate();
        }
        return new BigDecimal(unscaledBI, scale);
    }
    
    private static Number initBigDecimalNoneScale(final byte[] bytes, final int pos, final short len, final short weight, final short sign) {
        int idx = pos + 8;
        short d = readShort2(bytes, idx);
        BigInteger unscaledBI = null;
        long unscaledInt = d;
        for (int i = 1; i < len; ++i) {
            if (i == 4) {
                unscaledBI = BigInteger.valueOf(unscaledInt);
            }
            idx += 2;
            d = readShort2(bytes, idx);
            if (null == unscaledBI) {
                unscaledInt *= 10000;
                unscaledInt += d;
            } else {
                unscaledBI = unscaledBI.multiply(BI_TEN_THOUSAND);
                if (0 != d) {
                    unscaledBI = unscaledBI.add(BigInteger.valueOf(d));
                }
            }
        }
        if (null == unscaledBI) {
            unscaledBI = BigInteger.valueOf(unscaledInt);
        }
        if (NUMERIC_NEG == sign) {
            unscaledBI = unscaledBI.negate();
        }
        final int bigDecScale = (len - (weight + 1)) * 4;
        return 0 == bigDecScale ? new BigDecimal(unscaledBI) : new BigDecimal(unscaledBI, bigDecScale);
    }
    
    private static Number initBigDecimal(final byte[] bytes, final int pos, final short len, final short weight, final short sign, final short scale) {
        int idx = pos + 8;
        short d = readShort2(bytes, idx);
        BigInteger unscaledBI = null;
        long unscaledInt = d;
        int effectiveWeight = weight;
        int effectiveScale = scale;
        for (int i = 1; i < len; ++i) {
            if (i == 4) {
                unscaledBI = BigInteger.valueOf(unscaledInt);
            }
            idx += 2;
            d = readShort2(bytes, idx);
            if (effectiveWeight > 0) {
                --effectiveWeight;
                if (null == unscaledBI) {
                    unscaledInt *= 10000;
                } else {
                    unscaledBI = unscaledBI.multiply(BI_TEN_THOUSAND);
                }
            } else if (effectiveScale >= 4) {
                effectiveScale -= 4;
                if (null == unscaledBI) {
                    unscaledInt *= 10000;
                } else {
                    unscaledBI = unscaledBI.multiply(BI_TEN_THOUSAND);
                }
            } else {
                if (null == unscaledBI) {
                    unscaledInt *= INT_TEN_POWERS[effectiveScale];
                } else {
                    unscaledBI = unscaledBI.multiply(tenPower(effectiveScale));
                }
                d = (short) (d / INT_TEN_POWERS[4 - effectiveScale]);
                effectiveScale = 0;
            }
            if (null == unscaledBI) {
                unscaledInt += d;
            } else {
                if (0 != d) {
                    unscaledBI = unscaledBI.add(BigInteger.valueOf(d));
                }
            }
        }
        if (null == unscaledBI) {
            unscaledBI = BigInteger.valueOf(unscaledInt);
        }
        if (effectiveWeight > 0) {
            unscaledBI = unscaledBI.multiply(tenPower(effectiveWeight * 4));
        }
        if (effectiveScale > 0) {
            unscaledBI = unscaledBI.multiply(tenPower(effectiveScale));
        }
        if (NUMERIC_NEG == sign) {
            unscaledBI = unscaledBI.negate();
        }
        return new BigDecimal(unscaledBI, scale);
    }
    
    private static void validator(final short sign, final short scale) {
        if (!(0x0000 == sign || NUMERIC_NEG == sign || NUMERIC_NAN == sign)) {
            throw new IllegalArgumentException("invalid sign in \"numeric\" value");
        }
        if ((scale & 0x00003FFF) != scale) {
            throw new IllegalArgumentException("invalid scale in \"numeric\" value");
        }
    }
    
    private static BigInteger tenPower(final int exponent) {
        return BI_TEN_POWERS.length > exponent ? BI_TEN_POWERS[exponent] : BigInteger.TEN.pow(exponent);
    }
    
    private static short readShort2(final byte[] bytes, final int index) {
        return (short) (((bytes[index] & 255) << 8) + ((bytes[index + 1] & 255)));
    }
    
    /**
     * Simple stack structure for non-negative {@code short} values.
     */
    private static final class PositiveShortStack {
        
        private short[] shorts = new short[8];
        
        private int index;
        
        public void push(final short value) {
            if (0 != value || 0 != index) {
                Preconditions.checkArgument(value >= 0, "only non-negative values accepted: %s", value);
                if (index == shorts.length) {
                    grow();
                }
                shorts[index++] = value;
            }
        }
        
        public int size() {
            return index;
        }
        
        public boolean isEmpty() {
            return 0 == index;
        }
        
        public short pop() {
            return index > 0 ? shorts[--index] : -1;
        }
        
        private void grow() {
            final int newSize = shorts.length <= 1024 ? shorts.length << 1 : (int) (shorts.length * 1.5);
            shorts = Arrays.copyOf(shorts, newSize);
        }
    }
}
