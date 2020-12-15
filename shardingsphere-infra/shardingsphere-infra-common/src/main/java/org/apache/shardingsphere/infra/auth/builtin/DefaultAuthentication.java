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

import lombok.Getter;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ShardingSphereUser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Default authentication.
*/
@Getter
public final class DefaultAuthentication implements Authentication {
    
    private final Map<String, ShardingSphereUser> users = new LinkedHashMap<>();
    
    @Override
    public Optional<ShardingSphereUser> findUser(final String username) {
        return Optional.ofNullable(users.get(username));
    }
}
