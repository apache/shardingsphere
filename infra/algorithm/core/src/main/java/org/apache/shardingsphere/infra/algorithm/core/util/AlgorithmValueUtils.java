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

package org.apache.shardingsphere.infra.algorithm.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Algorithm value utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlgorithmValueUtils {
    
    /**
     * Convert value to bytes.
     *
     * @param value value to be converted
     * @param charset charset for non-byte array values, null means UTF-8
     * @return converted bytes
     */
    public static byte[] convertToBytes(final Object value, final Charset charset) {
        return value instanceof byte[] ? (byte[]) value : String.valueOf(value).getBytes(getCharset(charset));
    }
    
    /**
     * Convert value to text.
     *
     * @param value value to be converted
     * @param charset charset for byte array values, null means UTF-8
     * @return converted text
     */
    public static String convertToText(final Object value, final Charset charset) {
        return value instanceof byte[] ? new String((byte[]) value, getCharset(charset)) : String.valueOf(value);
    }
    
    /**
     * Convert bytes to plain value.
     *
     * @param value value to be converted
     * @param charset charset for text values, null means UTF-8
     * @param plainValueBinary whether plain value is binary
     * @return converted plain value
     */
    public static Object convertToPlainValue(final byte[] value, final Charset charset, final boolean plainValueBinary) {
        return plainValueBinary ? value : convertToText(value, charset);
    }
    
    /**
     * Convert value to UTF-8 text.
     *
     * @param value value to be converted
     * @return converted text
     */
    public static String convertToUTF8Text(final Object value) {
        return convertToText(value, StandardCharsets.UTF_8);
    }
    
    private static Charset getCharset(final Charset charset) {
        return null == charset ? StandardCharsets.UTF_8 : charset;
    }
}
