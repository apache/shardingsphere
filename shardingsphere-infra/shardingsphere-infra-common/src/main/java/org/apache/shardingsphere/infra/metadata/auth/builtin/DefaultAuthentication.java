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

package org.apache.shardingsphere.infra.metadata.auth.builtin;

import lombok.Getter;
import org.apache.shardingsphere.infra.metadata.auth.Authentication;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.ShardingSpherePrivilege;
import org.apache.shardingsphere.infra.metadata.auth.model.user.Grantee;
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default authentication.
*/
@Getter
public final class DefaultAuthentication implements Authentication {
    
    private final Map<ShardingSphereUser, ShardingSpherePrivilege> authentication = new ConcurrentHashMap<>();
    
    @Override
    public void init(final Collection<ShardingSphereUser> initializedUsers, final Map<ShardingSphereUser, ShardingSpherePrivilege> loadedPrivileges) {
        for (ShardingSphereUser each : initializedUsers) {
            authentication.put(each, createShardingSpherePrivilege());
        }
        authentication.putAll(loadedPrivileges);
    }
    
    private ShardingSpherePrivilege createShardingSpherePrivilege() {
        ShardingSpherePrivilege result = new ShardingSpherePrivilege();
        result.setSuperPrivilege();
        return result;
    }
    
    @Override
    public Optional<ShardingSphereUser> findUser(final Grantee grantee) {
        return authentication.keySet().stream().filter(each -> each.getGrantee().equals(grantee)).findFirst();
    }
    
    @Override
    public Optional<ShardingSpherePrivilege> findPrivilege(final Grantee grantee) {
        return findUser(grantee).map(authentication::get);
    }
}
