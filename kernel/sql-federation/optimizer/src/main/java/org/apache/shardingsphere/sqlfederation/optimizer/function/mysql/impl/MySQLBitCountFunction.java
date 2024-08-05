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

package org.apache.shardingsphere.sqlfederation.optimizer.function.mysql.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.util.BitString;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;

/**
 * MySQL bit count function.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLBitCountFunction {
    
    /**
     * Bit count.
     *
     * @param value value
     * @return bit count
     */
    @SuppressWarnings("unused")
    public static Object bitCount(final Object value) {
        if (null == value) {
            return 0;
        }
        if (value instanceof byte[]) {
            return bitCount((byte[]) value);
        }
        if (value instanceof String) {
            return StringUtils.isNumeric((String) value) ? Long.bitCount(Long.parseLong((String) value)) : 0;
        }
        if (value instanceof BigInteger) {
            return ((BigInteger) value).bitCount();
        }
        if (value instanceof Integer) {
            return Integer.bitCount((Integer) value);
        }
        if (value instanceof Long) {
            return Long.bitCount((Long) value);
        }
        return 0;
    }
    
    private static long bitCount(final byte[] byteValue) {
        long result = 0;
        for (char each : BitString.createFromBytes(byteValue).toBitString().toCharArray()) {
            if ('1' == each) {
                result++;
            }
        }
        return result;
    }
}
