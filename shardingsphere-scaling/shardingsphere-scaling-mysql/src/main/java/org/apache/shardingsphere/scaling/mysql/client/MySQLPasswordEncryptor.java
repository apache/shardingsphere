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

package org.apache.shardingsphere.scaling.mysql.client;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MySQL Password Encryptor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLPasswordEncryptor {
    
    /**
     * Encrypt password with MySQL protocol 41.
     *
     * <p>
     *     MySQL Internals Manual  /  MySQL Client/Server Protocol  /  Authentication Method  /  Secure Password Authentication
     *     https://dev.mysql.com/doc/internals/en/secure-password-authentication.html
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
        return xorPassword(passwordSha1, concatSeed);
    }
    
    private static byte[] concatSeed(final MessageDigest messageDigest, final byte[] seed, final byte[] passwordSha1) {
        messageDigest.update(seed);
        messageDigest.update(passwordSha1);
        return messageDigest.digest();
    }
    
    private static byte[] xorPassword(final byte[] passwordSha1, final byte[] concatSeed) {
        byte[] result = new byte[concatSeed.length];
        for (int i = 0; i < concatSeed.length; i++) {
            result[i] = (byte) (concatSeed[i] ^ passwordSha1[i]);
        }
        return result;
    }
}
