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

package org.apache.shardingsphere.governance.core.mode;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.governance.repository.api.config.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.mode.ShardingSphereMode;
import org.apache.shardingsphere.infra.mode.builder.ModeBuilder;
import org.apache.shardingsphere.infra.mode.config.ModeConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;

/**
 * Cluster mode builder.
 */
public final class ClusterModeBuilder implements ModeBuilder {
    
    static {
        ShardingSphereServiceLoader.register(RegistryCenterRepository.class);
    }
    
    @Override
    public ShardingSphereMode build(final ModeConfiguration config) {
        ClusterPersistRepositoryConfiguration clusterRepositoryConfig = (ClusterPersistRepositoryConfiguration) config.getRepository();
        return new ClusterMode(createRegistryCenterRepository(clusterRepositoryConfig));
    }
    
    private RegistryCenterRepository createRegistryCenterRepository(final ClusterPersistRepositoryConfiguration clusterRepositoryConfig) {
        Preconditions.checkNotNull(clusterRepositoryConfig, "Registry center configuration cannot be null.");
        RegistryCenterRepository result = TypedSPIRegistry.getRegisteredService(RegistryCenterRepository.class, clusterRepositoryConfig.getType(), clusterRepositoryConfig.getProps());
        result.init(clusterRepositoryConfig);
        return result;
    }
    
    @Override
    public String getType() {
        return "Cluster";
    }
}
