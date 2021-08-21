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

package org.apache.shardingsphere.governance.core.registry;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.governance.repository.api.config.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;

/**
 * Registry center repository factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegistryCenterRepositoryFactory {
    
    static {
        ShardingSphereServiceLoader.register(RegistryCenterRepository.class);
    }
    
    /**
     * Create new instance of registry center repository.
     * 
     * @param config registry center configuration
     * @return new instance of registry center repository
     */
    public static RegistryCenterRepository newInstance(final ClusterPersistRepositoryConfiguration config) {
        Preconditions.checkNotNull(config, "Registry center configuration cannot be null.");
        RegistryCenterRepository result = TypedSPIRegistry.getRegisteredService(RegistryCenterRepository.class, config.getType(), config.getProps());
        result.init(config);
        return result;
    }
}
