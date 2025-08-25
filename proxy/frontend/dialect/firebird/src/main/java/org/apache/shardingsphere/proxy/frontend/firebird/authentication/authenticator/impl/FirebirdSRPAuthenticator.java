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
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdSRPAuthenticationData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.proxy.frontend.firebird.authentication.authenticator.FirebirdAuthenticator;
import org.firebirdsql.util.ByteArrayHelper;

/**
 * SRP authenticator for Firebird.
 * 
 * @see <a href=https://github.com/FirebirdSQL/jaybird/blob/Branch_5_0/src/main/org/firebirdsql/gds/ng/wire/auth/srp/SrpAuthenticationPlugin.java>Jaybird implementation</a>
 */
public final class FirebirdSRPAuthenticator implements FirebirdAuthenticator {
    
    @Override
    public boolean authenticate(final ShardingSphereUser user, final Object[] authInfo) {
        FirebirdSRPAuthenticationData authData = (FirebirdSRPAuthenticationData) authInfo[1];
        String serverProof = ByteArrayHelper.toHexString(authData.serverProof(user.getGrantee().getUsername()));
        String clientProof = (String) authInfo[2];
        return serverProof.equals(clientProof);
    }
    
    @Override
    public String getAuthenticationMethodName() {
        return FirebirdAuthenticationMethod.SRP.getMethodName();
    }
}
