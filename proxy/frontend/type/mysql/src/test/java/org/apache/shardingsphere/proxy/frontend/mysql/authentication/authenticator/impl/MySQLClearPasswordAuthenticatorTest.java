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

package org.apache.shardingsphere.proxy.frontend.mysql.authentication.authenticator.impl;

import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLClearPasswordAuthenticatorTest {
    
    @Test
    void assertAuthenticationMethodName() {
        assertThat(new MySQLClearPasswordAuthenticator().getAuthenticationMethodName(), is("mysql_clear_password"));
    }
    
    @Test
    void assertAuthenticate() {
        ShardingSphereUser user = new ShardingSphereUser("foo", "password", "%");
        byte[] password = "password".getBytes();
        byte[] authInfo = new byte[password.length + 1];
        System.arraycopy(password, 0, authInfo, 0, password.length);
        assertTrue(new MySQLClearPasswordAuthenticator().authenticate(user, new Object[]{authInfo}));
    }
    
    @Test
    void assertAuthenticateFailed() {
        ShardingSphereUser user = new ShardingSphereUser("foo", "password", "%");
        byte[] password = "wrong".getBytes();
        assertFalse(new MySQLClearPasswordAuthenticator().authenticate(user, new Object[]{password}));
    }
}
