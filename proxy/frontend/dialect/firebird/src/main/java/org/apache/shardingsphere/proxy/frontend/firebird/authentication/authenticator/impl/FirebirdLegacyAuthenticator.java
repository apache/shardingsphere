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

import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdAuthenticationMethod;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.proxy.frontend.firebird.authentication.authenticator.FirebirdAuthenticator;
import org.firebirdsql.gds.ng.wire.auth.legacy.UnixCrypt;

/**
 * Legacy authenticator for Firebird.
 *
 * @see <a href=https://github.com/FirebirdSQL/jaybird/blob/Branch_5_0/src/main/org/firebirdsql/gds/ng/wire/auth/legacy/LegacyAuthenticationPlugin.java>Jaybird implementation</a>
 */
public final class FirebirdLegacyAuthenticator implements FirebirdAuthenticator {
    
    private static final String SALT = "9z";
    
    @Override
    public boolean authenticate(final ShardingSphereUser user, final Object[] authInfo) {
        // TODO update when version 6 of jaybird comes out
        String password = (String) authInfo[0];
        String expectedPassword = UnixCrypt.crypt(user.getPassword(), SALT).substring(2, 13);
        return expectedPassword.equals(password);
    }
    
    @Override
    public String getAuthenticationMethodName() {
        return FirebirdAuthenticationMethod.LEGACY_AUTH.getMethodName();
    }
}
