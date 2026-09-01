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
import org.apache.shardingsphere.infra.algorithm.cryptographic.spi.CryptographicAlgorithm;
import org.apache.shardingsphere.infra.algorithm.cryptographic.spi.CryptographicContext;

import java.util.Base64;

/**
 * Cryptographic algorithm engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CryptographicAlgorithmEngine {
    
    private static final String BASE64 = "BASE64";
    
    /**
     * Encrypt.
     *
     * @param cryptographicAlgorithm cryptographic algorithm
     * @param plainValue plain value
     * @param cryptographicContext cryptographic context
     * @param encoder encoder
     * @param preferBinaryType prefer binary type
     * @return cipher value
     */
    public static Object encrypt(final CryptographicAlgorithm cryptographicAlgorithm, final Object plainValue, final CryptographicContext cryptographicContext,
                                 final String encoder, final boolean preferBinaryType) {
        byte[] result = cryptographicAlgorithm.encrypt(plainValue, cryptographicContext);
        return null == result || preferBinaryType ? result : encode(result, encoder);
    }
    
    /**
     * Decrypt.
     *
     * @param cryptographicAlgorithm cryptographic algorithm
     * @param cipherValue cipher value
     * @param cryptographicContext cryptographic context
     * @param encoder encoder
     * @return plain value
     */
    public static Object decrypt(final CryptographicAlgorithm cryptographicAlgorithm, final Object cipherValue, final CryptographicContext cryptographicContext, final String encoder) {
        return null == cipherValue ? null : cryptographicAlgorithm.decrypt(cipherValue instanceof byte[] ? (byte[]) cipherValue : decode(cipherValue.toString(), encoder), cryptographicContext);
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
    
    private static byte[] decode(final String value, final String encoder) {
        return BASE64.equalsIgnoreCase(encoder) ? CryptographicAlgorithmValueUtils.decodeBase64(value) : decodeHex(value.trim());
    }
    
    private static String encodeHex(final byte[] value) {
        StringBuilder result = new StringBuilder(value.length * 2);
        for (byte each : value) {
            result.append(Character.toUpperCase(Character.forDigit(each >> 4 & 0xF, 16)));
            result.append(Character.toUpperCase(Character.forDigit(each & 0xF, 16)));
        }
        return result.toString();
    }
    
    private static byte[] decodeHex(final String value) {
        byte[] result = new byte[value.length() / 2];
        for (int i = 0; i < value.length(); i += 2) {
            result[i / 2] = (byte) Integer.parseInt(value.substring(i, i + 2), 16);
        }
        return result;
    }
}
