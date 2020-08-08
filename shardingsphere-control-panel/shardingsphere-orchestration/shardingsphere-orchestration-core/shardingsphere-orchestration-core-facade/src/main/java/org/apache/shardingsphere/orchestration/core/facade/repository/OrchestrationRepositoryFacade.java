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

package org.apache.shardingsphere.orchestration.core.facade.repository;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.TypedSPIRegistry;
import org.apache.shardingsphere.orchestration.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.orchestration.repository.api.RegistryRepository;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationCenterConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationConfiguration;

/**
 * Orchestration repository facade.
 */
@Getter
public final class OrchestrationRepositoryFacade implements AutoCloseable {
    
    static {
        ShardingSphereServiceLoader.register(RegistryRepository.class);
        ShardingSphereServiceLoader.register(ConfigurationRepository.class);
    }
    
    private final RegistryRepository registryRepository;
    
    private final ConfigurationRepository configurationRepository;
    
    public OrchestrationRepositoryFacade(final OrchestrationConfiguration config) {
        registryRepository = createRegistryRepository(config);
        configurationRepository = createConfigurationRepository(config);
    }
    
    private RegistryRepository createRegistryRepository(final OrchestrationConfiguration config) {
        OrchestrationCenterConfiguration registryCenterConfig = config.getRegistryCenterConfiguration();
        Preconditions.checkNotNull(registryCenterConfig, "Registry center configuration cannot be null.");
        RegistryRepository result = TypedSPIRegistry.getRegisteredService(RegistryRepository.class, registryCenterConfig.getType(), registryCenterConfig.getProps());
        result.init(config.getName(), registryCenterConfig);
        return result;
    }
    
    private ConfigurationRepository createConfigurationRepository(final OrchestrationConfiguration config) {
        if (config.getAdditionalConfigCenterConfiguration().isPresent()) {
            OrchestrationCenterConfiguration additionalConfigCenterConfig = config.getAdditionalConfigCenterConfiguration().get();
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
