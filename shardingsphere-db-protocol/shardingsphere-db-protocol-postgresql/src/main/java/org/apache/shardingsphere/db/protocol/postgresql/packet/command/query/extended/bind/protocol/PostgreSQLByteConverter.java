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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Refer to
 * <a href="https://github.com/pgjdbc/pgjdbc/blob/REL42.3.2/pgjdbc/src/main/java/org/postgresql/util/ByteConverter.java" >
 *      org.postgresql.util.ByteConverter
 * </a>.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLByteConverter {
    
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
        if (sign == NUMERIC_NAN) {
            return Double.NaN;
        }
        short len = readShort2(bytes, pos);
        if (len == 0) {
            return new BigDecimal(BigInteger.ZERO, scale);
        }
        short weight = readShort2(bytes, pos + 2);
        if (weight < 0) {
            ++weight;
            return initBigDecimalNoneWeight(bytes, pos, len, weight, sign, scale);
        } else if (scale == 0) {
            return initBigDecimalNoneScale(bytes, pos, len, weight, sign);
        } else {
            return initBigDecimal(bytes, pos, len, weight, sign, scale);
        }
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
                if (unscaledBI == null) {
                    unscaledInt *= 10000;
                } else {
                    unscaledBI = unscaledBI.multiply(BI_TEN_THOUSAND);
                }
            } else if (effectiveScale >= 4) {
                effectiveScale -= 4;
                if (unscaledBI == null) {
                    unscaledInt *= 10000;
                } else {
                    unscaledBI = unscaledBI.multiply(BI_TEN_THOUSAND);
                }
            } else {
                if (unscaledBI == null) {
                    unscaledInt *= INT_TEN_POWERS[effectiveScale];
                } else {
                    unscaledBI = unscaledBI.multiply(tenPower(effectiveScale));
                }
                d = (short) (d / INT_TEN_POWERS[4 - effectiveScale]);
                effectiveScale = 0;
            }
            if (unscaledBI == null) {
                unscaledInt += d;
            } else {
                if (d != 0) {
                    unscaledBI = unscaledBI.add(BigInteger.valueOf(d));
                }
            }
        }
        if (unscaledBI == null) {
            unscaledBI = BigInteger.valueOf(unscaledInt);
        }
        if (effectiveWeight > 0) {
            unscaledBI = unscaledBI.multiply(tenPower(effectiveWeight * 4));
        }
        if (effectiveScale > 0) {
            unscaledBI = unscaledBI.multiply(tenPower(effectiveScale));
        }
        if (sign == NUMERIC_NEG) {
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
            if (unscaledBI == null) {
                unscaledInt *= 10000;
                unscaledInt += d;
            } else {
                unscaledBI = unscaledBI.multiply(BI_TEN_THOUSAND);
                if (d != 0) {
                    unscaledBI = unscaledBI.add(BigInteger.valueOf(d));
                }
            }
        }
        if (unscaledBI == null) {
            unscaledBI = BigInteger.valueOf(unscaledInt);
        }
        if (sign == NUMERIC_NEG) {
            unscaledBI = unscaledBI.negate();
        }
        final int bigDecScale = (len - (weight + 1)) * 4;
        return bigDecScale == 0 ? new BigDecimal(unscaledBI) : new BigDecimal(unscaledBI, bigDecScale);
    }
    
    private static Number initBigDecimalNoneWeight(final byte[] bytes, final int pos, final short len, final short weight, final short sign, final short scale) {
        int idx = pos + 8;
        short d = readShort2(bytes, idx);
        assert scale > 0;
        int effectiveScale = scale;
        if (weight < 0) {
            effectiveScale += 4 * weight;
        }
        for (int i = 1; i < len && d == 0; ++i) {
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
                if (unscaledBI == null) {
                    unscaledInt *= 10000;
                } else {
                    unscaledBI = unscaledBI.multiply(BI_TEN_THOUSAND);
                }
                effectiveScale -= 4;
            } else {
                if (unscaledBI == null) {
                    unscaledInt *= INT_TEN_POWERS[effectiveScale];
                } else {
                    unscaledBI = unscaledBI.multiply(tenPower(effectiveScale));
                }
                d = (short) (d / INT_TEN_POWERS[4 - effectiveScale]);
                effectiveScale = 0;
            }
            if (unscaledBI == null) {
                unscaledInt += d;
            } else {
                if (d != 0) {
                    unscaledBI = unscaledBI.add(BigInteger.valueOf(d));
                }
            }
        }
        if (unscaledBI == null) {
            unscaledBI = BigInteger.valueOf(unscaledInt);
        }
        if (effectiveScale > 0) {
            unscaledBI = unscaledBI.multiply(tenPower(effectiveScale));
        }
        if (sign == NUMERIC_NEG) {
            unscaledBI = unscaledBI.negate();
        }
        return new BigDecimal(unscaledBI, scale);
    }
    
    private static void validator(final short sign, final short scale) {
        if (!(sign == 0x0000 || NUMERIC_NEG == sign || NUMERIC_NAN == sign)) {
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
}
