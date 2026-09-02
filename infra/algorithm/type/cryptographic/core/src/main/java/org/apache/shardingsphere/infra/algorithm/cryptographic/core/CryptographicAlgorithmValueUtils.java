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

package org.apache.shardingsphere.infra.algorithm.cryptographic.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.algorithm.core.util.AlgorithmValueUtils;
import org.apache.shardingsphere.infra.algorithm.cryptographic.spi.CryptographicContext;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Cryptographic algorithm value utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CryptographicAlgorithmValueUtils {
    
    /**
     * Convert value to bytes.
     *
     * @param value value to be converted
     * @param context cryptographic context
     * @return converted bytes
     */
    public static byte[] convertToBytes(final Object value, final CryptographicContext context) {
        return AlgorithmValueUtils.convertToBytes(value, getPlainCharset(context));
    }
    
    /**
     * Convert bytes to plain value.
     *
     * @param value value to be converted
     * @param context cryptographic context
     * @return converted plain value
     */
    public static Object convertToPlainValue(final byte[] value, final CryptographicContext context) {
        return AlgorithmValueUtils.convertToPlainValue(value, getPlainCharset(context), isPlainValueBinary(context));
    }
    
    /**
     * Decode Base64 value while keeping legacy boundary trim compatibility and ignoring SP, HT, CR and LF inside value.
     *
     * @param value Base64 value
     * @return decoded bytes
     */
    @HighFrequencyInvocation
    public static byte[] decodeBase64(final String value) {
        int startIndex = getBase64StartIndex(value);
        int stopIndex = getBase64StopIndex(value, startIndex);
        StringBuilder normalized = null;
        for (int i = startIndex; i < stopIndex; i++) {
            char each = value.charAt(i);
            if (isBase64Whitespace(each)) {
                if (null == normalized) {
                    normalized = new StringBuilder(stopIndex - startIndex);
                    normalized.append(value, startIndex, i);
                }
            } else if (null != normalized) {
                normalized.append(each);
            }
        }
        if (null != normalized) {
            return Base64.getDecoder().decode(normalized.toString());
        }
        return 0 == startIndex && value.length() == stopIndex ? Base64.getDecoder().decode(value) : Base64.getDecoder().decode(value.substring(startIndex, stopIndex));
    }
    
    private static int getBase64StartIndex(final String value) {
        int result = 0;
        while (result < value.length() && isBase64BoundaryWhitespace(value.charAt(result))) {
            result++;
        }
        return result;
    }
    
    private static int getBase64StopIndex(final String value, final int startIndex) {
        int result = value.length();
        while (result > startIndex && isBase64BoundaryWhitespace(value.charAt(result - 1))) {
            result--;
        }
        return result;
    }
    
    private static boolean isBase64Whitespace(final char value) {
        return ' ' == value || '\t' == value || '\r' == value || '\n' == value;
    }
    
    private static boolean isBase64BoundaryWhitespace(final char value) {
        return value <= ' ';
    }
    
    private static Charset getPlainCharset(final CryptographicContext context) {
        return null == context ? null : context.getPlainCharset();
    }
    
    private static boolean isPlainValueBinary(final CryptographicContext context) {
        return null != context && context.isPlainValueBinary();
    }
}
