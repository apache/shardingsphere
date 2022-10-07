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

package org.apache.shardingsphere.proxy.frontend.postgresql.authentication.authenticator;

import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLAuthenticationMethod;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class PostgreSQLPasswordAuthenticatorTest {
    
    private final PostgreSQLPasswordAuthenticator authenticator = new PostgreSQLPasswordAuthenticator();
    
    private final String username = "root";
    
    private final String password = "password";
    
    @Test
    public void assertGetAuthenticationMethodName() {
        assertThat(authenticator.getAuthenticationMethodName(), is(PostgreSQLAuthenticationMethod.PASSWORD.getMethodName()));
    }
    
    @Test
    public void assertAuthenticate() {
        ShardingSphereUser user = new ShardingSphereUser(username, password, "");
        assertTrue(authenticator.authenticate(user, new Object[]{password, null}));
        assertFalse(authenticator.authenticate(user, new Object[]{"wrong", null}));
    }
}
