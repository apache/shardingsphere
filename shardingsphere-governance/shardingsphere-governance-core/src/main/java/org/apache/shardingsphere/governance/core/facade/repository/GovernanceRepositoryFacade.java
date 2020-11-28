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

package org.apache.shardingsphere.governance.core.facade.repository;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;

/**
 * Governance repository facade.
 */
@Getter
public final class GovernanceRepositoryFacade implements AutoCloseable {
    
    static {
        ShardingSphereServiceLoader.register(RegistryRepository.class);
        ShardingSphereServiceLoader.register(ConfigurationRepository.class);
    }
    
    private final RegistryRepository registryRepository;
    
    private final ConfigurationRepository configurationRepository;
    
    public GovernanceRepositoryFacade(final GovernanceConfiguration config) {
        registryRepository = createRegistryRepository(config);
        configurationRepository = createConfigurationRepository(config);
    }
    
    private RegistryRepository createRegistryRepository(final GovernanceConfiguration config) {
        GovernanceCenterConfiguration registryCenterConfig = config.getRegistryCenterConfiguration();
        Preconditions.checkNotNull(registryCenterConfig, "Registry center configuration cannot be null.");
        RegistryRepository result = TypedSPIRegistry.getRegisteredService(RegistryRepository.class, registryCenterConfig.getType(), registryCenterConfig.getProps());
        result.init(config.getName(), registryCenterConfig);
        return result;
    }
    
    private ConfigurationRepository createConfigurationRepository(final GovernanceConfiguration config) {
        if (config.getAdditionalConfigCenterConfiguration().isPresent()) {
            GovernanceCenterConfiguration additionalConfigCenterConfig = config.getAdditionalConfigCenterConfiguration().get();
            ConfigurationRepository result = TypedSPIRegistry.getRegisteredService(ConfigurationRepository.class, additionalConfigCenterConfig.getType(), additionalConfigCenterConfig.getProps());
            result.init(config.getName(), additionalConfigCenterConfig);
            return result;
        }
        if (registryRepository instanceof ConfigurationRepository) {
            return (ConfigurationRepository) registryRepository;
        }
        throw new IllegalArgumentException("Registry repository is not suitable for config center and no additional config center configuration provided.");
    }
    
    @Override
    public void close() {
        registryRepository.close();
        if (registryRepository != configurationRepository) {
            configurationRepository.close();
        }
    }
}
