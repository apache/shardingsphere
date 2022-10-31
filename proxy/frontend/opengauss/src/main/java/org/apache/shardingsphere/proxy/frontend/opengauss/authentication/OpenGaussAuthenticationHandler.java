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

package org.apache.shardingsphere.proxy.frontend.opengauss.authentication;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLPasswordMessagePacket;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.dialect.postgresql.exception.authority.InvalidPasswordException;
import org.apache.shardingsphere.dialect.postgresql.exception.authority.PrivilegeNotGrantedException;
import org.apache.shardingsphere.dialect.postgresql.exception.authority.UnknownUsernameException;
import org.apache.shardingsphere.infra.executor.check.SQLCheckEngine;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Authentication handler for openGauss.
 * 
 * @see <a href="https://opengauss.org/zh/blogs/blogs.html?post/douxin/sm3_for_opengauss/">SM3 for openGauss</a>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenGaussAuthenticationHandler {
    
    private static final String PBKDF2_WITH_HMAC_SHA1_ALGORITHM = "PBKDF2WithHmacSHA1";
    
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    
    private static final String SHA256_ALGORITHM = "SHA-256";
    
    private static final String CLIENT_KEY = "Client Key";
    
    /**
     * Login with SCRAM SHA-256 password.
     *
     * @param username username
     * @param databaseName database name
     * @param salt salt in hex string
     * @param nonce nonce in hex string
     * @param serverIteration server iteration
     * @param passwordMessagePacket password message packet
     */
    public static void loginWithSCRAMSha256Password(final String username, final String databaseName, final String salt, final String nonce, final int serverIteration,
                                                    final PostgreSQLPasswordMessagePacket passwordMessagePacket) {
        String clientDigest = passwordMessagePacket.getDigest();
        Grantee grantee = new Grantee(username, "%");
        if (!Strings.isNullOrEmpty(databaseName) && !ProxyContext.getInstance().databaseExists(databaseName)) {
            throw new UnknownDatabaseException(databaseName);
        }
        if (!SQLCheckEngine.check(grantee, getRules(databaseName))) {
            throw new UnknownUsernameException(username);
        }
        if (!SQLCheckEngine.check(grantee, (a, b) -> isPasswordRight((ShardingSphereUser) a, (Object[]) b), new Object[]{clientDigest, salt, nonce, serverIteration}, getRules(databaseName))) {
            throw new InvalidPasswordException(username);
        }
        if (null != databaseName && !SQLCheckEngine.check(databaseName, getRules(databaseName), grantee)) {
            throw new PrivilegeNotGrantedException(username, databaseName);
        }
    }
    
    private static Collection<ShardingSphereRule> getRules(final String databaseName) {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        if (!Strings.isNullOrEmpty(databaseName) && ProxyContext.getInstance().databaseExists(databaseName)) {
            result.addAll(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getRuleMetaData().getRules());
        }
        result.addAll(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules());
        return result;
    }
    
    private static boolean isPasswordRight(final ShardingSphereUser user, final Object[] args) {
        String h3HexString = (String) args[0];
        String salt = (String) args[1];
        String nonce = (String) args[2];
        int serverIteration = (int) args[3];
        byte[] serverStoredKey = calculatedStoredKey(user.getPassword(), salt, serverIteration);
        byte[] h3 = hexStringToBytes(h3HexString);
        byte[] h2 = calculateH2(user.getPassword(), salt, nonce, serverIteration);
        byte[] clientCalculatedStoredKey = sha256(xor(h3, h2));
        return Arrays.equals(clientCalculatedStoredKey, serverStoredKey);
    }
    
    private static byte[] calculatedStoredKey(final String password, final String salt, final int serverIteration) {
        byte[] k = generateKFromPBKDF2(password, salt, serverIteration);
        byte[] clientKey = getKeyFromHmac(k, CLIENT_KEY.getBytes());
        return sha256(clientKey);
    }
    
    private static byte[] calculateH2(final String password, final String salt, final String nonce, final int serverIteration) {
        byte[] k = generateKFromPBKDF2(password, salt, serverIteration);
        byte[] clientKey = getKeyFromHmac(k, CLIENT_KEY.getBytes());
        byte[] storedKey = sha256(clientKey);
        return getKeyFromHmac(storedKey, hexStringToBytes(nonce));
    }
    
    @SneakyThrows({NoSuchAlgorithmException.class, InvalidKeySpecException.class})
    private static byte[] generateKFromPBKDF2(final String password, final String saltString, final int serverIteration) {
        char[] chars = password.toCharArray();
        byte[] salt = hexStringToBytes(saltString);
        PBEKeySpec spec = new PBEKeySpec(chars, salt, serverIteration, 32 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_WITH_HMAC_SHA1_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }
    
    private static byte[] hexStringToBytes(final String rawHexString) {
        if (null == rawHexString || rawHexString.isEmpty()) {
            return new byte[0];
        }
        String hexString = rawHexString.toUpperCase(Locale.ENGLISH);
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
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
    
    @SneakyThrows(NoSuchAlgorithmException.class)
    private static byte[] sha256(final byte[] str) {
        MessageDigest md = MessageDigest.getInstance(SHA256_ALGORITHM);
        md.update(str);
        return md.digest();
    }
    
    @SneakyThrows({NoSuchAlgorithmException.class, InvalidKeyException.class})
    private static byte[] getKeyFromHmac(final byte[] key, final byte[] data) {
        SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA256_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(signingKey);
        return mac.doFinal(data);
    }
    
    private static byte[] xor(final byte[] password1, final byte[] password2) {
        Preconditions.checkArgument(password1.length == password2.length, "Xor values with different length");
        int length = password1.length;
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (password1[i] ^ password2[i]);
        }
        return result;
    }
}
