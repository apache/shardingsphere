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

package org.apache.shardingsphere.encrypt.algorithm.engine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.cryptographic.core.CryptographicAlgorithmValueUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

/**
 * Encrypt algorithm engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptAlgorithmEngine {
    
    private static final String BASE64 = "BASE64";
    
    private static final String ENCODER_SUFFIX = "-encoder";
    
    /**
     * Encrypt.
     *
     * @param encryptAlgorithm encrypt algorithm
     * @param plainValue plain value
     * @param algorithmSQLContext algorithm SQL context
     * @param encoder encoder
     * @param preferBinaryType prefer binary type
     * @return cipher value
     */
    public static Object encrypt(final EncryptAlgorithm encryptAlgorithm, final Object plainValue, final AlgorithmSQLContext algorithmSQLContext,
                                 final String encoder, final boolean preferBinaryType) {
        Object result = encryptAlgorithm.encrypt(plainValue, algorithmSQLContext);
        return result instanceof byte[] && !preferBinaryType ? encode(encryptAlgorithm, (byte[]) result, encoder) : result;
    }
    
    /**
     * Decrypt.
     *
     * @param encryptAlgorithm encrypt algorithm
     * @param cipherValue cipher value
     * @param algorithmSQLContext algorithm SQL context
     * @param encoder encoder
     * @return plain value
     */
    public static Object decrypt(final EncryptAlgorithm encryptAlgorithm, final Object cipherValue, final AlgorithmSQLContext algorithmSQLContext, final String encoder) {
        if (null == cipherValue) {
            return null;
        }
        return encryptAlgorithm.decrypt(getActualCipherValue(encryptAlgorithm, cipherValue, encoder), algorithmSQLContext);
    }
    
    private static Object getActualCipherValue(final EncryptAlgorithm encryptAlgorithm, final Object cipherValue, final String encoder) {
        if (cipherValue instanceof byte[] || !isByteArrayCipherValue(encryptAlgorithm)) {
            return cipherValue;
        }
        return decode(cipherValue.toString(), encoder);
    }
    
    private static boolean isByteArrayCipherValue(final EncryptAlgorithm encryptAlgorithm) {
        return byte[].class == encryptAlgorithm.getMetaData().getCipherValueType();
    }
    
    /**
     * Find encoder.
     *
     * @param encryptAlgorithm encrypt algorithm
     * @return encoder
     */
    public static Optional<String> findEncoder(final EncryptAlgorithm encryptAlgorithm) {
        Optional<String> result = encryptAlgorithm.getEncoder();
        return result.isPresent() ? result : findEncoder(encryptAlgorithm.toConfiguration());
    }
    
    /**
     * Find encoder.
     *
     * @param config algorithm configuration
     * @return encoder
     */
    public static Optional<String> findEncoder(final AlgorithmConfiguration config) {
        return Optional.ofNullable(config.getProps().getProperty(getEncoderKey(config.getType())));
    }
    
    private static String getEncoderKey(final String type) {
        return type.trim().toLowerCase(Locale.ENGLISH) + ENCODER_SUFFIX;
    }
    
    private static String encode(final EncryptAlgorithm encryptAlgorithm, final byte[] value, final String encoder) {
        if (null == encoder) {
            return new String(value, StandardCharsets.UTF_8);
        }
        return BASE64.equalsIgnoreCase(encoder) ? Base64.getEncoder().encodeToString(value) : encodeHex(value, isUseLowerHexEncode(encryptAlgorithm));
    }
    
    private static boolean isUseLowerHexEncode(final EncryptAlgorithm encryptAlgorithm) {
        return "MD5".equals(encryptAlgorithm.getType());
    }
    
    private static byte[] decode(final String value, final String encoder) {
        if (null == encoder) {
            return value.getBytes(StandardCharsets.UTF_8);
        }
        return BASE64.equalsIgnoreCase(encoder) ? CryptographicAlgorithmValueUtils.decodeBase64(value) : decodeHex(value.trim());
    }
    
    private static String encodeHex(final byte[] value, final boolean lowerCase) {
        return Hex.encodeHexString(value, lowerCase);
    }
    
    private static byte[] decodeHex(final String value) {
        try {
            return Hex.decodeHex(value);
        } catch (final DecoderException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
