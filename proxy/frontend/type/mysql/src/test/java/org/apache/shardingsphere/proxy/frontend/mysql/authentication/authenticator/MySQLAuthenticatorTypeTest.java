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

package org.apache.shardingsphere.proxy.frontend.mysql.authentication.authenticator;

import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.proxy.frontend.authentication.Authenticator;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticatorFactory;
import org.apache.shardingsphere.proxy.frontend.mysql.authentication.authenticator.impl.MySQLClearPasswordAuthenticator;
import org.apache.shardingsphere.proxy.frontend.mysql.authentication.authenticator.impl.MySQLNativePasswordAuthenticator;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MySQLAuthenticatorTypeTest {
    
    private final AuthorityRule rule = mock(AuthorityRule.class);
    
    @Test
    public void assertDefaultAuthenticatorType() {
        when(rule.getAuthenticatorType(any())).thenReturn("");
        Authenticator authenticator = new AuthenticatorFactory<>(MySQLAuthenticatorType.class, rule).newInstance(mock(ShardingSphereUser.class));
        assertThat(authenticator, instanceOf(MySQLNativePasswordAuthenticator.class));
        assertThat(authenticator.getAuthenticationMethod().getMethodName(), is("mysql_native_password"));
    }
    
    @Test
    public void assertAuthenticatorTypeWithErrorName() {
        when(rule.getAuthenticatorType(any())).thenReturn("error");
        Authenticator authenticator = new AuthenticatorFactory<>(MySQLAuthenticatorType.class, rule).newInstance(mock(ShardingSphereUser.class));
        assertThat(authenticator, instanceOf(MySQLNativePasswordAuthenticator.class));
        assertThat(authenticator.getAuthenticationMethod().getMethodName(), is("mysql_native_password"));
    }
    
    @Test
    public void assertAuthenticatorTypeWithNative() {
        when(rule.getAuthenticatorType(any())).thenReturn("NATIVE");
        Authenticator authenticator = new AuthenticatorFactory<>(MySQLAuthenticatorType.class, rule).newInstance(mock(ShardingSphereUser.class));
        assertThat(authenticator, instanceOf(MySQLNativePasswordAuthenticator.class));
        assertThat(authenticator.getAuthenticationMethod().getMethodName(), is("mysql_native_password"));
    }
    
    @Test
    public void assertAuthenticatorTypeWithPassword() {
        when(rule.getAuthenticatorType(any())).thenReturn("CLEAR_TEXT");
        Authenticator authenticator = new AuthenticatorFactory<>(MySQLAuthenticatorType.class, rule).newInstance(mock(ShardingSphereUser.class));
        assertThat(authenticator, instanceOf(MySQLClearPasswordAuthenticator.class));
        assertThat(authenticator.getAuthenticationMethod().getMethodName(), is("mysql_clear_password"));
    }
}
