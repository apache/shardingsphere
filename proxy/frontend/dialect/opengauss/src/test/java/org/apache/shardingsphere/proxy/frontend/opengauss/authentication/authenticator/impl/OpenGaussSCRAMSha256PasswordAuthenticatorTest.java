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

package org.apache.shardingsphere.proxy.frontend.opengauss.authentication.authenticator.impl;

import org.apache.shardingsphere.database.protocol.opengauss.packet.authentication.OpenGaussMacCalculator;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(OpenGaussMacCalculator.class)
class OpenGaussSCRAMSha256PasswordAuthenticatorTest {
    
    private final OpenGaussSCRAMSha256PasswordAuthenticator authenticator = new OpenGaussSCRAMSha256PasswordAuthenticator();
    
    @Test
    void assertAuthenticate() {
        ShardingSphereUser user = new ShardingSphereUser("user", "pass", "localhost");
        byte[] storedKey = new byte[]{1, 2, 3};
        when(OpenGaussMacCalculator.requestClientMac("pass", "salt", 4096)).thenReturn(storedKey);
        when(OpenGaussMacCalculator.calculateClientMac("h3", "nonce", storedKey)).thenReturn(storedKey);
        Object[] authInfo = {"h3", "salt", "nonce", 4096};
        assertTrue(authenticator.authenticate(user, authInfo));
    }
    
    @Test
    void assertGetAuthenticationMethodName() {
        assertThat(authenticator.getAuthenticationMethodName(), is("scram-sha-256"));
    }
}
