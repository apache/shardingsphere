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
import org.apache.shardingsphere.db.protocol.opengauss.packet.authentication.OpenGaussAuthenticationHexData;
import org.apache.shardingsphere.db.protocol.opengauss.packet.authentication.OpenGaussAuthenticationSCRAMSha256Packet;
import org.apache.shardingsphere.proxy.frontend.opengauss.authentication.authenticator.impl.OpenGaussSCRAMSha256PasswordAuthenticator;
import org.mockito.internal.configuration.plugins.Plugins;

import java.nio.charset.StandardCharsets;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenGaussAuthenticationAlgorithm {
    
    /**
     * Generate client digest.
     * 
     * @param packet password
     * @param password password
     * @return client digest
     */
    public static byte[] doRFC5802Algorithm(final OpenGaussAuthenticationSCRAMSha256Packet packet, final String password) {
        byte[] k = generateKFromPBKDF2(packet, password);
        byte[] clientKey = getKeyFromHmac(packet, k, "Client Key".getBytes(StandardCharsets.UTF_8));
        byte[] storedKey = sha256(clientKey);
        byte[] tokenBytes = toHexBytes(packet, getAuthHexData(packet).getNonce());
        byte[] hmacResult = getKeyFromHmac(packet, storedKey, tokenBytes);
        byte[] h = xor(hmacResult, clientKey);
        byte[] result = new byte[h.length * 2];
        bytesToHex(h, result, 0, h.length);
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static OpenGaussAuthenticationHexData getAuthHexData(final OpenGaussAuthenticationSCRAMSha256Packet packet) {
        return (OpenGaussAuthenticationHexData) Plugins.getMemberAccessor().get(OpenGaussAuthenticationSCRAMSha256Packet.class.getDeclaredField("authHexData"), packet);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static byte[] generateKFromPBKDF2(final OpenGaussAuthenticationSCRAMSha256Packet packet, final String password) {
        return (byte[]) Plugins.getMemberAccessor().invoke(OpenGaussAuthenticationSCRAMSha256Packet.class.getDeclaredMethod("generateKFromPBKDF2", String.class), packet, password);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static byte[] getKeyFromHmac(final OpenGaussAuthenticationSCRAMSha256Packet packet, final byte[] key, final byte[] data) {
        return (byte[]) Plugins.getMemberAccessor().invoke(
                OpenGaussAuthenticationSCRAMSha256Packet.class.getDeclaredMethod("getKeyFromHmac", byte[].class, byte[].class), packet, key, data);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static byte[] sha256(final byte[] str) {
        return (byte[]) Plugins.getMemberAccessor().invoke(
                OpenGaussSCRAMSha256PasswordAuthenticator.class.getDeclaredMethod("sha256", byte[].class), OpenGaussAuthenticationSCRAMSha256Packet.class, new Object[]{str});
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static byte[] toHexBytes(final OpenGaussAuthenticationSCRAMSha256Packet packet, final String rawHexString) {
        return (byte[]) Plugins.getMemberAccessor().invoke(OpenGaussAuthenticationSCRAMSha256Packet.class.getDeclaredMethod("toHexBytes", String.class), packet, rawHexString);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static byte[] xor(final byte[] value1, final byte[] value2) {
        return (byte[]) Plugins.getMemberAccessor().invoke(
                OpenGaussSCRAMSha256PasswordAuthenticator.class.getDeclaredMethod("xor", byte[].class, byte[].class), OpenGaussAuthenticationSCRAMSha256Packet.class, value1, value2);
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
