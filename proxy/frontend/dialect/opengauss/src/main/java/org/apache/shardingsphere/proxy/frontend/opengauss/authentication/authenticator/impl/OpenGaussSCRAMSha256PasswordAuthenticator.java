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

import org.apache.shardingsphere.database.protocol.opengauss.constant.OpenGaussAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.opengauss.packet.authentication.OpenGaussMacCalculator;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.proxy.frontend.opengauss.authentication.authenticator.OpenGaussAuthenticator;

import java.util.Arrays;

/**
 * SCRAM Sha256 password authenticator for openGauss.
 */
public final class OpenGaussSCRAMSha256PasswordAuthenticator implements OpenGaussAuthenticator {
    
    @Override
    public boolean authenticate(final ShardingSphereUser user, final Object[] authInfo) {
        String h3HexString = (String) authInfo[0];
        String salt = (String) authInfo[1];
        String nonce = (String) authInfo[2];
        int serverIteration = (int) authInfo[3];
        byte[] serverStoredKey = OpenGaussMacCalculator.requestClientMac(user.getPassword(), salt, serverIteration);
        byte[] clientCalculatedStoredKey = OpenGaussMacCalculator.calculateClientMac(h3HexString, nonce, serverStoredKey);
        return Arrays.equals(clientCalculatedStoredKey, serverStoredKey);
    }
    
    @Override
    public String getAuthenticationMethodName() {
        return OpenGaussAuthenticationMethod.SCRAM_SHA256.getMethodName();
    }
}
