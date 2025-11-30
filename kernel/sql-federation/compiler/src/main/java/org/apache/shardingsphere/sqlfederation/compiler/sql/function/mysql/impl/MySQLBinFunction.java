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

package org.apache.shardingsphere.sqlfederation.compiler.sql.function.mysql.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

/**
 * MySQL bin function.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLBinFunction {
    
    /**
     * Bin.
     *
     * @param value value
     * @return binary string
     */
    @SuppressWarnings("unused")
    public static String bin(final Object value) {
        if (null == value) {
            return null;
        }
        if (value instanceof Number && !(value instanceof BigInteger)) {
            return Long.toBinaryString(((Number) value).longValue());
        }
        if (value instanceof String && ((String) value).isEmpty()) {
            return null;
        }
        BigInteger bigIntegerValue = value instanceof BigInteger ? (BigInteger) value : new BigInteger(getFirstNumbers(String.valueOf(value)));
        return -1 == bigIntegerValue.signum() ? bigIntegerValue.add(BigInteger.ONE.shiftLeft(Long.SIZE).subtract(BigInteger.ONE)).add(BigInteger.ONE).toString(2) : bigIntegerValue.toString(2);
    }
    
    private static String getFirstNumbers(final String value) {
        boolean isNegative = '-' == value.charAt(0);
        StringBuilder result = new StringBuilder();
        if (isNegative) {
            result.append('-');
        }
        for (int i = isNegative ? 1 : 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                result.append(value.charAt(i));
            } else {
                break;
            }
        }
        if (0 == result.length() || isNegative && 1 == result.length()) {
            return "0";
        }
        return result.toString();
    }
}
