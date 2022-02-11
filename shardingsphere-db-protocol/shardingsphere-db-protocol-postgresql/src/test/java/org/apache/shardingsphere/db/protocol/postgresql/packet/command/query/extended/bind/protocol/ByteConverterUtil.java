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

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public final class ByteConverterUtil {
    
    private static final short NUMERIC_POS = 0x0000;
    
    private static final short NUMERIC_NEG = 0x4000;
    
    private static final BigInteger[] BI_TEN_POWERS = new BigInteger[32];
    
    private static final BigInteger BI_TEN_THOUSAND = BigInteger.valueOf(10000);
    
    private static final BigInteger BI_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
    
    static {
        for (int i = 0; i < BI_TEN_POWERS.length; ++i) {
            BI_TEN_POWERS[i] = BigInteger.TEN.pow(i);
        }
    }
    
    /**
     * Converts a non-null {@link BigDecimal} to binary format for.
     *
     * @param bigDecimal The instance to represent in binary.
     * @return The binary representation of <i>bigDecimal</i>.
     */
    public static byte[] numeric(final BigDecimal bigDecimal) {
        final PositiveShorts shorts = new PositiveShorts();
        BigInteger unscaled = bigDecimal.unscaledValue().abs();
        int scale = bigDecimal.scale();
        if (unscaled.equals(BigInteger.ZERO)) {
            byte[] bytes = new byte[] {0, 0, -1, -1, 0, 0, 0, 0};
            int2(bytes, 6, Math.max(0, scale));
            return bytes;
        }
        int weight = -1;
        if (scale < 0) {
            scale = Math.abs(scale);
            weight += scale / 4;
            int mod = scale % 4;
            unscaled = unscaled.multiply(tenPower(mod));
            scale = 0;
        }
        if (scale == 0) {
            while (unscaled.compareTo(BI_MAX_LONG) > 0) {
                final BigInteger[] pair = unscaled.divideAndRemainder(BI_TEN_THOUSAND);
                unscaled = pair[0];
                final short shortValue = pair[1].shortValue();
                if (shortValue != 0 || !shorts.isEmpty()) {
                    shorts.push(shortValue);
                }
                ++weight;
            }
            long unscaledLong = unscaled.longValueExact();
            do {
                final short shortValue = (short) (unscaledLong % 10000);
                if (shortValue != 0 || !shorts.isEmpty()) {
                    shorts.push(shortValue);
                }
                unscaledLong = unscaledLong / 10000L;
                ++weight;
            } while (unscaledLong != 0);
            return initBytes(bigDecimal, shorts, weight, scale);
        }
        
        final BigInteger[] split = unscaled.divideAndRemainder(tenPower(scale));
        BigInteger decimal = split[1];
        BigInteger wholes = split[0];
        weight = -1;
        if (!BigInteger.ZERO.equals(decimal)) {
            int mod = scale % 4;
            int segments = scale / 4;
            if (mod != 0) {
                decimal = decimal.multiply(tenPower(4 - mod));
                ++segments;
            }
            do {
                final BigInteger[] pair = decimal.divideAndRemainder(BI_TEN_THOUSAND);
                decimal = pair[0];
                final short shortValue = pair[1].shortValue();
                if (shortValue != 0 || !shorts.isEmpty()) {
                    shorts.push(shortValue);
                }
                --segments;
            } while (!BigInteger.ZERO.equals(decimal));
            if (BigInteger.ZERO.equals(wholes)) {
                weight -= segments;
            } else {
                for (int i = 0; i < segments; ++i) {
                    shorts.push((short) 0);
                }
            }
        }
        while (!BigInteger.ZERO.equals(wholes)) {
            ++weight;
            final BigInteger[] pair = wholes.divideAndRemainder(BI_TEN_THOUSAND);
            wholes = pair[0];
            final short shortValue = pair[1].shortValue();
            if (shortValue != 0 || !shorts.isEmpty()) {
                shorts.push(shortValue);
            }
        }
        
        return initBytes(bigDecimal, shorts, weight, scale);
    }
    
    private static byte[] initBytes(final BigDecimal bigDecimal, final PositiveShorts shorts, final int weight, final int scale) {
        final byte[] bytes = new byte[8 + (2 * shorts.size())];
        int idx = 0;
        int2(bytes, idx, shorts.size());
        idx += 2;
        int2(bytes, idx, weight);
        idx += 2;
        int2(bytes, idx, bigDecimal.signum() == -1 ? NUMERIC_NEG : NUMERIC_POS);
        idx += 2;
        int2(bytes, idx, Math.max(0, scale));
        idx += 2;
        short s;
        while ((s = shorts.pop()) != -1) {
            int2(bytes, idx, s);
            idx += 2;
        }
        return bytes;
    }
    
    private static BigInteger tenPower(final int exponent) {
        return BI_TEN_POWERS.length > exponent ? BI_TEN_POWERS[exponent] : BigInteger.TEN.pow(exponent);
    }
    
    /**
     * Encodes a int value to the byte array.
     *
     * @param target The byte array to encode to.
     * @param idx The starting index in the byte array.
     * @param value The value to encode.
     */
    public static void int2(final byte[] target, final int idx, final int value) {
        target[idx] = (byte) (value >>> 8);
        target[idx + 1] = (byte) value;
    }
    
    /**
     * Simple stack structure for non-negative {@code short} values.
     */
    @RequiredArgsConstructor
    private static final class PositiveShorts {
        
        private short[] shorts = new short[8];
        
        private int idx;
        
        public void push(final short s) {
            if (s < 0) {
                throw new IllegalArgumentException("only non-negative values accepted: " + s);
            }
            if (idx == shorts.length) {
                grow();
            }
            shorts[idx++] = s;
        }
        
        public int size() {
            return idx;
        }
        
        public boolean isEmpty() {
            return idx == 0;
        }
        
        public short pop() {
            return idx > 0 ? shorts[--idx] : -1;
        }
        
        private void grow() {
            final int newSize = shorts.length <= 1024 ? shorts.length << 1 : (int) (shorts.length * 1.5);
            shorts = Arrays.copyOf(shorts, newSize);
        }
    }
}
