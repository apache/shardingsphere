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

package org.apache.shardingsphere.infra.metadata.privilege.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.Collection;
import java.util.Optional;

/**
 * Privilege loader engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrivilegeLoaderEngine {
    
    static {
        ShardingSphereServiceLoader.register(PrivilegeLoader.class);
    }
    
    /**
     * Get privilege loader.
     *
     * @return privilege loader
     */
    public static Optional<PrivilegeLoader> getPrivilegeLoader() {
        Collection<PrivilegeLoader> loaders = ShardingSphereServiceLoader.newServiceInstances(PrivilegeLoader.class);
        if (loaders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(loaders.iterator().next());
    }
}
