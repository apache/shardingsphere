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

import lombok.Getter;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.db.protocol.opengauss.packet.authentication.OpenGaussAuthenticationSCRAMSha256Packet;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLPasswordMessagePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLRandomGenerator;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.authentication.PostgreSQLMD5PasswordAuthenticationPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.PostgreSQLAuthenticationHandler;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.PostgreSQLLoginResult;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Authentication helper for openGauss.
 */
public final class OpenGaussAuthenticationHelper {
    
    private static final String MD5 = "md5";
    
    @Getter
    private final String saltHexString;
    
    @Getter
    private final String nonceHexString;
    
    @Getter
    private final int serverIteration;
    
    @Getter
    private byte[] md5Salt;
    
    @Getter
    private final boolean md5AuthorityType;
    
    public OpenGaussAuthenticationHelper(final String user) {
        if (isMd5User(user)) {
            md5AuthorityType = true;
            saltHexString = null;
            nonceHexString = null;
        } else {
            md5AuthorityType = false;
            saltHexString = generateRandomHexString(64);
            nonceHexString = generateRandomHexString(8);
        }
        serverIteration = 1000;
    }
    
    /**
     * Select the algorithm according to the configuration.
     *
     * @return the identifier packet
     */
    public PostgreSQLIdentifierPacket createIdentifierPacket() {
        if (md5AuthorityType) {
            md5Salt = PostgreSQLRandomGenerator.getInstance().generateRandomBytes(4);
            return new PostgreSQLMD5PasswordAuthenticationPacket(md5Salt);
        } else {
            return new OpenGaussAuthenticationSCRAMSha256Packet(saltHexString.getBytes(), nonceHexString.getBytes(), serverIteration);
        }
    }
    
    /**
     * Process password authention in openGauss connecting.
     * @param username the user name
     * @param databaseName database name
     * @param passwordMessagePacket receive password package
     * @return the login result
     */
    public PostgreSQLLoginResult processPassword(final String username, final String databaseName, final PostgreSQLPasswordMessagePacket passwordMessagePacket) {
        if (md5AuthorityType) {
            return new PostgreSQLAuthenticationHandler().login(username, databaseName, md5Salt, passwordMessagePacket);
        } else {
            return OpenGaussAuthenticationHandler.loginWithSCRAMSha256Password(username,
                    databaseName, saltHexString, nonceHexString, serverIteration, passwordMessagePacket);
        }
    }
    
    private String generateRandomHexString(final int length) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < result.capacity(); i++) {
            result.append(Integer.toString(random.nextInt(0x10), 0x10));
        }
        return result.toString();
    }
    
    private static boolean isMd5User(final String user) {
        Optional<AuthorityRule> rule;
        try {
            rule = ProxyContext.getInstance().getContextManager()
                    .getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findSingleRule(AuthorityRule.class);
            if (!rule.isPresent()) {
                return false;
            }
            Grantee grantee = new Grantee(user, "%");
            Optional<ShardingSphereUser> shardingSphereUser = rule.get().findUser(grantee);
            if (!shardingSphereUser.isPresent()) {
                return false;
            }
            return MD5.equalsIgnoreCase(shardingSphereUser.get().getAuthType());
        } catch (NullPointerException npe) {
            // in test case, maybe proxy context not inited.
            return false;
        }
    }
}
