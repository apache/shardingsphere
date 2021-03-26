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

package org.apache.shardingsphere.infra.metadata.auth;

import org.apache.shardingsphere.infra.metadata.auth.model.privilege.ShardingSpherePrivilege;
import org.apache.shardingsphere.infra.metadata.auth.model.user.Grantee;
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication.
*/
public interface Authentication {
    
    /**
     * Initialize authentication.
     * 
     * @param initializedUsers initialized users
     * @param loadedPrivileges loaded privileges
     */
    void init(Collection<ShardingSphereUser> initializedUsers, Map<ShardingSphereUser, ShardingSpherePrivilege> loadedPrivileges);
    
    /**
     * Get authentication.
     *
     * @return Authentication
     */
    Map<ShardingSphereUser, ShardingSpherePrivilege> getAuthentication();
    
    /**
     * Get all users.
     *
     * @return all users
     */
    Collection<ShardingSphereUser> getAllUsers();
    
    /**
     * Find user.
     * 
     * @param grantee grantee
     * @return found user
     */
    Optional<ShardingSphereUser> findUser(Grantee grantee);
    
    /**
     * Find Privilege.
     *
     * @param grantee grantee
     * @return found user
     */
    Optional<ShardingSpherePrivilege> findPrivilege(Grantee grantee);
}
