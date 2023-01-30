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
import org.apache.shardingsphere.authority.checker.AuthorityChecker;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLPasswordMessagePacket;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.dialect.postgresql.exception.authority.InvalidPasswordException;
import org.apache.shardingsphere.dialect.postgresql.exception.authority.PrivilegeNotGrantedException;
import org.apache.shardingsphere.dialect.postgresql.exception.authority.UnknownUsernameException;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.opengauss.authentication.authenticator.OpenGaussAuthenticator;
import org.apache.shardingsphere.proxy.frontend.opengauss.authentication.authenticator.OpenGaussSCRAMSha256PasswordAuthenticator;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Locale;
import java.util.Optional;

/**
 * Authentication handler for openGauss.
 * 
 * @see <a href="https://opengauss.org/zh/blogs/blogs.html?post/douxin/sm3_for_opengauss/">SM3 for openGauss</a>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenGaussAuthenticationHandler {
    
    private static final String PBKDF2_WITH_HMAC_SHA1_ALGORITHM = "PBKDF2WithHmacSHA1";
    
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    
    private static final String SERVER_KEY = "Server Key";
    
    /**
     * Calculate server signature.
     *
     * @param password password
     * @param salt salt in hex string
     * @param nonce nonce in hex string
     * @param serverIteration server iteration
     * @return server signature
     */
    public static String calculateServerSignature(final String password, final String salt, final String nonce, final int serverIteration) {
        byte[] k = generateKFromPBKDF2(password, salt, serverIteration);
        byte[] serverKey = getKeyFromHmac(k, SERVER_KEY.getBytes(StandardCharsets.UTF_8));
        byte[] result = getKeyFromHmac(serverKey, hexStringToBytes(nonce));
        return bytesToHexString(result);
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
            String hex = Integer.toHexString(each & 255);
            if (hex.length() < 2) {
                result.append(0);
            }
            result.append(hex);
        }
        return result.toString();
    }
    
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
        ShardingSpherePreconditions.checkState(Strings.isNullOrEmpty(databaseName) || ProxyContext.getInstance().databaseExists(databaseName), () -> new UnknownDatabaseException(databaseName));
        AuthorityRule authorityRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        Grantee grantee = new Grantee(username, "%");
        Optional<ShardingSphereUser> user = authorityRule.findUser(grantee);
        ShardingSpherePreconditions.checkState(user.isPresent(), () -> new UnknownUsernameException(username));
        ShardingSpherePreconditions.checkState(getAuthenticator(grantee).authenticate(user.get(), new Object[]{passwordMessagePacket.getDigest(), salt, nonce, serverIteration}),
                () -> new InvalidPasswordException(username));
        ShardingSpherePreconditions.checkState(null == databaseName || new AuthorityChecker(authorityRule, grantee).isAuthorized(databaseName),
                () -> new PrivilegeNotGrantedException(username, databaseName));
    }
    
    private static OpenGaussAuthenticator getAuthenticator(final Grantee grantee) {
        // TODO get authenticator by username and hostname
        return new OpenGaussSCRAMSha256PasswordAuthenticator();
    }
}
