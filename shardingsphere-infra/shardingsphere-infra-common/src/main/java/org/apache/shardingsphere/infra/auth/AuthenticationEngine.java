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

package org.apache.shardingsphere.infra.auth;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.Collection;
import java.util.Optional;

/**
 * Authentication engine.
*/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthenticationEngine {
    
    static {
        ShardingSphereServiceLoader.register(Authentication.class);
    }
    
    /**
     * Find SPI authentication.
     * 
     * @return authentication
     */
    public static Optional<Authentication> findSPIAuthentication() {
        Collection<Authentication> authentications = ShardingSphereServiceLoader.newServiceInstances(Authentication.class);
        return authentications.isEmpty() ? Optional.empty() : Optional.of(authentications.iterator().next());
    }
}
