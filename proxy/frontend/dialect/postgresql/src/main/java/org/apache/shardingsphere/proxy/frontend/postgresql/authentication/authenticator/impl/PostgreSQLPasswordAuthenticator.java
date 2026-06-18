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

package org.apache.shardingsphere.proxy.frontend.postgresql.authentication.authenticator.impl;

import com.google.common.base.Strings;
import org.apache.shardingsphere.database.protocol.postgresql.constant.PostgreSQLAuthenticationMethod;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.authenticator.PostgreSQLAuthenticator;

/**
 * Password authenticator for PostgreSQL.
 */
public final class PostgreSQLPasswordAuthenticator implements PostgreSQLAuthenticator {
    
    @Override
    public boolean authenticate(final ShardingSphereUser user, final Object[] authInfo) {
        String password = (String) authInfo[0];
        return Strings.isNullOrEmpty(user.getPassword()) || user.getPassword().equals(password);
    }
    
    @Override
    public String getAuthenticationMethodName() {
        return PostgreSQLAuthenticationMethod.PASSWORD.getMethodName();
    }
}
