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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.client;

import com.google.common.primitives.Bytes;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * MySQL Password Encryption.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PasswordEncryption {
    
    /**
     * Encrypt password with MySQL protocol 41.
     *
     * <p>
     * MySQL Internals Manual  /  MySQL Client/Server Protocol  /  Authentication Method  /  Secure Password Authentication
     * <a href="https://dev.mysql.com/doc/internals/en/secure-password-authentication.html">secure-password-authentication</a>
     * </p>
     *
     * @param password password
     * @param seed 20-bytes random data from server
     * @return encrypted password
     * @throws NoSuchAlgorithmException no such algorithm exception
     */
    public static byte[] encryptWithMySQL41(final byte[] password, final byte[] seed) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        byte[] passwordSha1 = messageDigest.digest(password);
        byte[] concatSeed = concatSeed(messageDigest, seed, messageDigest.digest(passwordSha1));
        return xor(passwordSha1, concatSeed, concatSeed.length);
    }
    
    /**
     * Encrypt password with sha2.
     *
     * @param password password
     * @param seed 20-bytes random data from server
     * @return encrypted password
     * @throws NoSuchAlgorithmException no such algorithm exception
     */
    public static byte[] encryptWithSha2(final byte[] password, final byte[] seed) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] s1 = messageDigest.digest(password);
        byte[] s2 = messageDigest.digest(s1);
        messageDigest.reset();
        messageDigest.update(s2);
        messageDigest.update(seed);
        byte[] s3 = messageDigest.digest();
        messageDigest.reset();
        return xor(s1, s3, s3.length);
    }
    
    /**
     * Encrypt password with rsa public key.
     *
     * @param password password
     * @param seed 20-bytes random data from server
     * @param transformation transformation
     * @param publicKey public key
     * @return encrypted password
     */
    public static byte[] encryptWithRSAPublicKey(final String password, final byte[] seed, final String transformation, final String publicKey) {
        byte[] formattedPassword = password != null ? Bytes.concat(password.getBytes(), new byte[]{0}) : new byte[]{0};
        return encryptWithRSAPublicKey(xor(formattedPassword, seed, formattedPassword.length), parseRSAPublicKey(publicKey), transformation);
    }
    
    @SneakyThrows
    private static byte[] encryptWithRSAPublicKey(final byte[] source, final RSAPublicKey key, final String transformation) {
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(source);
    }
    
    @SneakyThrows
    private static RSAPublicKey parseRSAPublicKey(final String key) {
        byte[] certificateData = Base64.getDecoder().decode(formatKey(key));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(certificateData);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(spec);
    }
    
    private static byte[] formatKey(final String key) {
        int start = key.indexOf("\n") + 1;
        int end = key.lastIndexOf("\n");
        return key.substring(start, end).replace("\n", "").getBytes();
    }
    
    private static byte[] concatSeed(final MessageDigest messageDigest, final byte[] seed, final byte[] passwordSha1) {
        messageDigest.update(seed);
        messageDigest.update(passwordSha1);
        return messageDigest.digest();
    }
    
    private static byte[] xor(final byte[] data, final byte[] seed, final int length) {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (seed[i] ^ data[i]);
        }
        return result;
    }
}
