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

package org.apache.shardingsphere.database.protocol.opengauss.packet.authentication;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.util.string.HexStringUtils;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Locale;

/**
 * MAC calculator for openGauss.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenGaussMacCalculator {
    
    private static final String MAC_REQUEST_ALGORITHM = "HmacSHA256";
    
    private static final String SECRET_KEY_REQUEST_ALGORITHM = "PBKDF2WithHmacSHA1";
    
    private static final String SHA256_ALGORITHM = "SHA-256";
    
    /**
     * Request server MAC.
     *
     * @param password password
     * @param authHexData authentication hex data
     * @param serverIteration server iteration
     * @return MAC result
     */
    public static String requestServerMac(final String password, final OpenGaussAuthenticationHexData authHexData, final int serverIteration) {
        byte[] serverKey = getMacResult(generateSecretKey(password, authHexData.getSalt(), serverIteration), MacType.SERVER.data.getBytes(StandardCharsets.UTF_8));
        byte[] result = getMacResult(serverKey, toHexBytes(authHexData.getNonce()));
        return HexStringUtils.toHexString(result);
    }
    
    /**
     * Request client MAC.
     *
     * @param password password
     * @param salt salt
     * @param serverIteration server iteration
     * @return MAC result
     */
    public static byte[] requestClientMac(final String password, final String salt, final int serverIteration) {
        return sha256(getMacResult(generateSecretKey(password, salt, serverIteration), MacType.CLIENT.data.getBytes()));
    }
    
    /**
     * Calculate client MAC.
     *
     * @param h3HexString h3 hex string value
     * @param nonce nonce
     * @param serverStoredKey server stored key
     * @return MAC result
     */
    public static byte[] calculateClientMac(final String h3HexString, final String nonce, final byte[] serverStoredKey) {
        byte[] h3 = toHexBytes(h3HexString);
        byte[] h2 = getMacResult(serverStoredKey, toHexBytes(nonce));
        return sha256(xor(h3, h2));
    }
    
    @SneakyThrows({NoSuchAlgorithmException.class, InvalidKeyException.class})
    private static byte[] getMacResult(final byte[] key, final byte[] data) {
        Mac mac = Mac.getInstance(MAC_REQUEST_ALGORITHM);
        mac.init(new SecretKeySpec(key, MAC_REQUEST_ALGORITHM));
        return mac.doFinal(data);
    }
    
    @SneakyThrows({NoSuchAlgorithmException.class, InvalidKeySpecException.class})
    private static byte[] generateSecretKey(final String password, final String salt, final int serverIteration) {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), toHexBytes(salt), serverIteration, 32 * 8);
        return SecretKeyFactory.getInstance(SECRET_KEY_REQUEST_ALGORITHM).generateSecret(keySpec).getEncoded();
    }
    
    private static byte[] toHexBytes(final String hexString) {
        if (Strings.isNullOrEmpty(hexString)) {
            return new byte[0];
        }
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toUpperCase(Locale.ENGLISH).toCharArray();
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            result[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return result;
    }
    
    private static byte charToByte(final char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    
    private static byte[] xor(final byte[] password1, final byte[] password2) {
        Preconditions.checkArgument(password1.length == password2.length, "Xor values with different length.");
        int length = password1.length;
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (password1[i] ^ password2[i]);
        }
        return result;
    }
    
    @SneakyThrows(NoSuchAlgorithmException.class)
    private static byte[] sha256(final byte[] data) {
        MessageDigest messageDigest = MessageDigest.getInstance(SHA256_ALGORITHM);
        messageDigest.update(data);
        return messageDigest.digest();
    }
    
    @RequiredArgsConstructor
    private enum MacType {
        
        SERVER("Server Key"),
        CLIENT("Client Key");
        
        private final String data;
    }
}
