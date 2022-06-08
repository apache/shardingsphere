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

import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.proxy.frontend.authentication.Authenticator;

/**
 * PostgreSQL authenticator.
 * 
 * @see <a href="https://www.postgresql.org/docs/14/auth-password.html">Password Authentication</a>
 */
public interface PostgreSQLAuthenticator extends Authenticator {
    
    /**
     * Authenticate.
     *
     * @param user ShardingSphere user
     * @param args arguments for user authentication
     * @return authentication success or not
     */
    boolean authenticate(ShardingSphereUser user, Object[] args);
}
