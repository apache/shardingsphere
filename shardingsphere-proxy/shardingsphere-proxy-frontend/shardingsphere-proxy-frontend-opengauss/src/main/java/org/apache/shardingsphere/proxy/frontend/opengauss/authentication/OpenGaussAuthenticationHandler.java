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

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLErrorCode;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLPasswordMessagePacket;
import org.apache.shardingsphere.infra.executor.check.SQLCheckEngine;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.PostgreSQLLoginResult;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Authentication handler for openGauss.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenGaussAuthenticationHandler {
    
    private static final String PBKDF2_WITH_HMAC_SHA1_ALGORITHM = "PBKDF2WithHmacSHA1";
    
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    
    private static final String SHA256_ALGORITHM = "SHA-256";
    
    /**
     * Login with sha256 password.
     *
     * @param username username
     * @param databaseName database name
     * @param random64Code random 64 code
     * @param token token
     * @param serverIteration server iteration
     * @param passwordMessagePacket password message packet
     * @return openGauss(PostgreSQL) login result
     */
    public static PostgreSQLLoginResult loginWithSha256Password(final String username, final String databaseName, final String random64Code, final String token, final int serverIteration,
                                                                final PostgreSQLPasswordMessagePacket passwordMessagePacket) {
        String clientDigest = passwordMessagePacket.getDigest();
        Grantee grantee = new Grantee(username, "%");
        if (!Strings.isNullOrEmpty(databaseName) && !ProxyContext.getInstance().schemaExists(databaseName)) {
            return new PostgreSQLLoginResult(PostgreSQLErrorCode.INVALID_CATALOG_NAME, String.format("database \"%s\" does not exist", databaseName));
        }
        if (!SQLCheckEngine.check(grantee, getRules(databaseName))) {
            return new PostgreSQLLoginResult(PostgreSQLErrorCode.INVALID_AUTHORIZATION_SPECIFICATION, String.format("unknown username: %s", username));
        }
        if (!SQLCheckEngine.check(grantee, (a, b) -> isPasswordRight((ShardingSphereUser) a, (Object[]) b), new Object[]{clientDigest, random64Code, token, serverIteration}, getRules(databaseName))) {
            return new PostgreSQLLoginResult(PostgreSQLErrorCode.INVALID_PASSWORD, String.format("password authentication failed for user \"%s\"", username));
        }
        return null == databaseName || SQLCheckEngine.check(databaseName, getRules(databaseName), grantee)
                ? new PostgreSQLLoginResult(PostgreSQLErrorCode.SUCCESSFUL_COMPLETION, null)
                : new PostgreSQLLoginResult(PostgreSQLErrorCode.PRIVILEGE_NOT_GRANTED, String.format("Access denied for user '%s' to database '%s'", username, databaseName));
    }
    
    private static boolean isPasswordRight(final ShardingSphereUser user, final Object[] args) {
        String clientDigest = (String) args[0];
        String random64Code = (String) args[1];
        String token = (String) args[2];
        int serverIteration = (int) args[3];
        String expectedDigest = new String(doRFC5802Algorithm(user.getPassword(), random64Code, token, serverIteration));
        return expectedDigest.equals(clientDigest);
    }
    
    private static Collection<ShardingSphereRule> getRules(final String databaseName) {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        if (!Strings.isNullOrEmpty(databaseName) && ProxyContext.getInstance().schemaExists(databaseName)) {
            result.addAll(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(databaseName).getRuleMetaData().getRules());
        }
        result.addAll(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getGlobalRuleMetaData().getRules());
        return result;
    }
    
    private static byte[] doRFC5802Algorithm(final String password, final String random64code, final String token, final int serverIteration) {
        byte[] k = generateKFromPBKDF2(password, random64code, serverIteration);
        byte[] clientKey = getKeyFromHmac(k, "Client Key".getBytes(StandardCharsets.UTF_8));
        byte[] storedKey = sha256(clientKey);
        byte[] tokenBytes = hexStringToBytes(token);
        byte[] hmacResult = getKeyFromHmac(storedKey, tokenBytes);
        byte[] h = xorBetweenPassword(hmacResult, clientKey, clientKey.length);
        byte[] result = new byte[h.length * 2];
        bytesToHex(h, result, 0, h.length);
        return result;
    }
    
    @SneakyThrows({NoSuchAlgorithmException.class, InvalidKeySpecException.class})
    private static byte[] generateKFromPBKDF2(final String password, final String random64code, final int serverIteration) {
        char[] chars = password.toCharArray();
        byte[] random32code = hexStringToBytes(random64code);
        PBEKeySpec spec = new PBEKeySpec(chars, random32code, serverIteration, 32 * 8);
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
    
    private static String bytesToHexString(final byte[] src) {
        StringBuilder result = new StringBuilder();
        for (byte each : src) {
            int v = each & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                result.append(0);
            }
            result.append(hv);
        }
        return result.toString();
    }
    
    private static byte[] xorBetweenPassword(final byte[] password1, final byte[] password2, final int length) {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (password1[i] ^ password2[i]);
        }
        return result;
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
