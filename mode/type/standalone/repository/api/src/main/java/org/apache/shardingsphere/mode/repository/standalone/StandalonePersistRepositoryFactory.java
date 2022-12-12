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

package org.apache.shardingsphere.mode.repository.standalone;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPIRegistry;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;

/**
 * Standalone persist repository factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StandalonePersistRepositoryFactory {
    
    static {
        ShardingSphereServiceLoader.register(StandalonePersistRepository.class);
    }
    
    /**
     * Get instance of standalone persist repository.
     * 
     * @param config persist repository configuration
     * @return got instance
     */
    public static StandalonePersistRepository getInstance(final PersistRepositoryConfiguration config) {
        return null == config ? RequiredSPIRegistry.getRegisteredService(StandalonePersistRepository.class)
                : TypedSPIRegistry.getRegisteredService(StandalonePersistRepository.class, config.getType(), config.getProps());
    }
}
