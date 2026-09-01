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

package org.apache.shardingsphere.infra.algorithm.messagedigest.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.algorithm.messagedigest.spi.MessageDigestAlgorithm;

import java.util.Base64;

/**
 * Message digest algorithm engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageDigestAlgorithmEngine {
    
    private static final String BASE64 = "BASE64";
    
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
    
    /**
     * Digest.
     *
     * @param digestAlgorithm digest algorithm
     * @param plainValue plain value
     * @param encoder encoder
     * @param preferBinaryType prefer binary type
     * @return digest value
     */
    public static Object digest(final MessageDigestAlgorithm digestAlgorithm, final Object plainValue, final String encoder, final boolean preferBinaryType) {
        byte[] result = digestAlgorithm.digest(plainValue);
        return null == result || preferBinaryType ? result : encode(result, encoder);
    }
    
    /**
     * Calculate encoded length.
     *
     * @param byteLength byte length
     * @param encoder encoder
     * @return encoded length
     */
    public static int calculateEncodedLength(final int byteLength, final String encoder) {
        return BASE64.equalsIgnoreCase(encoder) ? 4 * ((byteLength + 2) / 3) : byteLength << 1;
    }
    
    private static String encode(final byte[] value, final String encoder) {
        return BASE64.equalsIgnoreCase(encoder) ? Base64.getEncoder().encodeToString(value) : encodeHex(value);
    }
    
    private static String encodeHex(final byte[] value) {
        StringBuilder result = new StringBuilder(value.length * 2);
        for (byte each : value) {
            result.append(HEX_DIGITS[each >> 4 & 0xF]);
            result.append(HEX_DIGITS[each & 0xF]);
        }
        return result.toString();
    }
}
