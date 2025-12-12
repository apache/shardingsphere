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

import com.google.common.base.Strings;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.proxy.frontend.mysql.authentication.authenticator.MySQLAuthenticator;

/**
 * Clear password authenticator for MySQL.
 * 
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_authentication_methods_clear_text_password.html">Clear Text Authentication</a>
 */
public final class MySQLClearPasswordAuthenticator implements MySQLAuthenticator {
    
    @Override
    public boolean authenticate(final ShardingSphereUser user, final Object[] authInfo) {
        byte[] authResponse = (byte[]) authInfo[0];
        byte[] password = new byte[authResponse.length - 1];
        System.arraycopy(authResponse, 0, password, 0, authResponse.length - 1);
        return Strings.isNullOrEmpty(user.getPassword()) || user.getPassword().equals(new String(password));
    }
    
    @Override
    public String getAuthenticationMethodName() {
        return MySQLAuthenticationMethod.CLEAR_TEXT.getMethodName();
    }
}
