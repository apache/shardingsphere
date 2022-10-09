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

package org.apache.shardingsphere.proxy.frontend.opengauss.authentication.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.proxy.frontend.opengauss.authentication.OpenGaussAuthenticationHandler;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenGaussAuthenticationAlgorithm {
    
    /**
     * Generate client digest.
     * 
     * @param password password
     * @param random64code random 64 code (salt)
     * @param token token (nonce)
     * @param serverIteration server iteration
     * @return client digest
     */
    public static byte[] doRFC5802Algorithm(final String password, final String random64code, final String token, final int serverIteration) {
        byte[] k = generateKFromPBKDF2(password, random64code, serverIteration);
        byte[] clientKey = getKeyFromHmac(k, "Client Key".getBytes(StandardCharsets.UTF_8));
        byte[] storedKey = sha256(clientKey);
        byte[] tokenBytes = hexStringToBytes(token);
        byte[] hmacResult = getKeyFromHmac(storedKey, tokenBytes);
        byte[] h = xor(hmacResult, clientKey);
        byte[] result = new byte[h.length * 2];
        bytesToHex(h, result, 0, h.length);
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static byte[] generateKFromPBKDF2(final String password, final String saltString, final int serverIteration) {
        Method generateKFromPBKDF2Method = OpenGaussAuthenticationHandler.class.getDeclaredMethod("generateKFromPBKDF2", String.class, String.class, int.class);
        generateKFromPBKDF2Method.setAccessible(true);
        return (byte[]) generateKFromPBKDF2Method.invoke(null, password, saltString, serverIteration);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static byte[] getKeyFromHmac(final byte[] key, final byte[] data) {
        Method getKeyFromHmacMethod = OpenGaussAuthenticationHandler.class.getDeclaredMethod("getKeyFromHmac", byte[].class, byte[].class);
        getKeyFromHmacMethod.setAccessible(true);
        return (byte[]) getKeyFromHmacMethod.invoke(null, key, data);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static byte[] sha256(final byte[] str) {
        Method sha256Method = OpenGaussAuthenticationHandler.class.getDeclaredMethod("sha256", byte[].class);
        sha256Method.setAccessible(true);
        return (byte[]) sha256Method.invoke(null, str);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static byte[] hexStringToBytes(final String rawHexString) {
        Method hexStringToBytesMethod = OpenGaussAuthenticationHandler.class.getDeclaredMethod("hexStringToBytes", String.class);
        hexStringToBytesMethod.setAccessible(true);
        return (byte[]) hexStringToBytesMethod.invoke(null, rawHexString);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static byte[] calculateH2(final String password, final String random64code, final String token, final int serverIteration) {
        Method calculateH2Method = OpenGaussAuthenticationHandler.class.getDeclaredMethod("calculateH2", String.class, String.class, String.class, int.class);
        calculateH2Method.setAccessible(true);
        return (byte[]) calculateH2Method.invoke(null, password, random64code, token, serverIteration);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static byte[] xor(final byte[] value1, final byte[] value2) {
        Method xorMethod = OpenGaussAuthenticationHandler.class.getDeclaredMethod("xor", byte[].class, byte[].class);
        xorMethod.setAccessible(true);
        return (byte[]) xorMethod.invoke(null, value1, value2);
    }
    
    private static void bytesToHex(final byte[] bytes, final byte[] hex, final int offset, final int length) {
        final char[] lookup = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int pos = offset;
        for (int i = 0; i < length; i++) {
            int c = bytes[i] & 0xFF;
            int j = c >> 4;
            hex[pos++] = (byte) lookup[j];
            j = c & 0xF;
            hex[pos++] = (byte) lookup[j];
        }
    }
}
