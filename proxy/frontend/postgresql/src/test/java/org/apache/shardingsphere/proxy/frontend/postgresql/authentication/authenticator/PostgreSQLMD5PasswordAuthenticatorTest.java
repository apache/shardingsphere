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

import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLAuthenticationMethod;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLRandomGenerator;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class PostgreSQLMD5PasswordAuthenticatorTest {
    
    private final PostgreSQLMD5PasswordAuthenticator authenticator = new PostgreSQLMD5PasswordAuthenticator();
    
    private final String username = "root";
    
    private final String password = "password";
    
    @Test
    public void assertGetAuthenticationMethodName() {
        assertThat(authenticator.getAuthenticationMethodName(), is(PostgreSQLAuthenticationMethod.MD5.getMethodName()));
    }
    
    @Test
    public void assertAuthenticate() {
        ShardingSphereUser user = new ShardingSphereUser(username, password, "");
        byte[] md5Salt = PostgreSQLRandomGenerator.getInstance().generateRandomBytes(4);
        String md5Digest = md5Encode(username, password, md5Salt);
        assertTrue(authenticator.authenticate(user, new Object[]{md5Digest, md5Salt}));
        assertFalse(authenticator.authenticate(user, new Object[]{"wrong", md5Salt}));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private String md5Encode(final String username, final String password, final byte[] md5Salt) {
        Method method = PostgreSQLMD5PasswordAuthenticator.class.getDeclaredMethod("md5Encode", String.class, String.class, byte[].class);
        method.setAccessible(true);
        return (String) method.invoke(new PostgreSQLMD5PasswordAuthenticator(), username, password, md5Salt);
    }
}
