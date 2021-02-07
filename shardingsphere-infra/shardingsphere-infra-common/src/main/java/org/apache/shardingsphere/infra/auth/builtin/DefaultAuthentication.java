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

package org.apache.shardingsphere.infra.auth.builtin;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.user.Grantee;
import org.apache.shardingsphere.infra.auth.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.auth.privilege.ShardingSpherePrivilege;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Default authentication.
*/
@NoArgsConstructor
@Getter
public final class DefaultAuthentication implements Authentication {
    
    private final Map<ShardingSphereUser, ShardingSpherePrivilege> authentication = new LinkedHashMap<>();
    
    public DefaultAuthentication(final Collection<ShardingSphereUser> users) {
        for (ShardingSphereUser each : users) {
            authentication.put(each, createShardingSpherePrivilege());
        }
    }
    
    private ShardingSpherePrivilege createShardingSpherePrivilege() {
        ShardingSpherePrivilege result = new ShardingSpherePrivilege();
        result.setSuper();
        return result;
    }
    
    @Override
    public Optional<ShardingSphereUser> findUser(final Grantee grantee) {
        return authentication.keySet().stream().filter(each -> each.getUsername().equals(grantee.getUsername())
                && (each.getHostname().equals(grantee.getHostname()) || Strings.isNullOrEmpty(each.getHostname()))).findFirst();
    }
    
    @Override
    public Optional<ShardingSpherePrivilege> findPrivilege(final Grantee grantee) {
        return findUser(grantee).map(authentication::get);
    }
}
