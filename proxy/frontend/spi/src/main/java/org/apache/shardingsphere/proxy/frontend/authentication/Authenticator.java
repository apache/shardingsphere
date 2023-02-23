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

package org.apache.shardingsphere.proxy.frontend.authentication;

import org.apache.shardingsphere.db.protocol.constant.AuthenticationMethod;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

/**
 * Authenticator.
 */
public interface Authenticator {
    
    /**
     * Authenticate.
     *
     * @param user ShardingSphere user
     * @param authInfo authentication information
     * @return authentication success or not
     */
    boolean authenticate(ShardingSphereUser user, Object[] authInfo);
    
    /**
     * Get authentication method.
     *
     * @return authentication method
     */
    AuthenticationMethod getAuthenticationMethod();
}
