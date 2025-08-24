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

package org.apache.shardingsphere.proxy.frontend.firebird.authentication.authenticator.impl;

import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdSRPAuthenticationData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.firebirdsql.util.ByteArrayHelper;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdSRPAuthenticatorTest {
    
    private final FirebirdSRPAuthenticator authenticator = new FirebirdSRPAuthenticator();
    
    @Test
    void assertAuthenticationMethodName() {
        assertThat(authenticator.getAuthenticationMethodName(), is("Srp"));
    }
    
    @Test
    void assertAuthenticateSuccess() {
        ShardingSphereUser user = new ShardingSphereUser("foo", "password", "");
        FirebirdSRPAuthenticationData authData = new FirebirdSRPAuthenticationData("SHA-1", user.getGrantee().getUsername(), user.getPassword(), "4");
        String clientProof = ByteArrayHelper.toHexString(authData.serverProof(user.getGrantee().getUsername()));
        assertTrue(authenticator.authenticate(user, new Object[]{null, authData, clientProof}));
    }
    
    @Test
    void assertAuthenticateFailure() {
        ShardingSphereUser user = new ShardingSphereUser("foo", "password", "");
        FirebirdSRPAuthenticationData authData = new FirebirdSRPAuthenticationData("SHA-1", user.getGrantee().getUsername(), user.getPassword(), "4");
        assertFalse(authenticator.authenticate(user, new Object[]{null, authData, "wrong"}));
    }
}
