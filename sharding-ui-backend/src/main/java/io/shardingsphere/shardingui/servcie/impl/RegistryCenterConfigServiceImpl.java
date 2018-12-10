/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingui.servcie.impl;

import com.google.common.base.Optional;
import io.shardingsphere.shardingui.common.domain.RegistryCenterConfig;
import io.shardingsphere.shardingui.common.domain.RegistryCenterConfigs;
import io.shardingsphere.shardingui.common.exception.ShardingUIException;
import io.shardingsphere.shardingui.repository.RegistryCenterConfigsRepository;
import io.shardingsphere.shardingui.servcie.RegistryCenterConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of Registry center config service.
 *
 * @author chenqingyang
 */
@Service
public final class RegistryCenterConfigServiceImpl implements RegistryCenterConfigService {
    
    @Autowired
    private RegistryCenterConfigsRepository registryCenterConfigsRepository;
    
    @Override
    public RegistryCenterConfig load(final String name) {
        return find(name, loadAll());
    }
    
    @Override
    public Optional<RegistryCenterConfig> loadActivated() {
        return Optional.fromNullable(findActivatedRegistryCenterConfiguration(loadAll()));
    }
    
    @Override
    public void add(final RegistryCenterConfig config) {
        RegistryCenterConfigs configs = loadAll();
        RegistryCenterConfig existedConfig = find(config.getName(), configs);
        if (existedConfig != null) {
            throw new ShardingUIException("Registry center already existed!");
        }
        configs.getRegistryCenterConfigs().add(config);
        registryCenterConfigsRepository.save(configs);
    }
    
    @Override
    public void delete(final String name) {
        RegistryCenterConfigs configs = loadAll();
        RegistryCenterConfig toBeRemovedConfig = find(name, configs);
        if (null != toBeRemovedConfig) {
            configs.getRegistryCenterConfigs().remove(toBeRemovedConfig);
            registryCenterConfigsRepository.save(configs);
        }
    }
    
    @Override
    public void setActivated(final RegistryCenterConfig config) {
        RegistryCenterConfigs configs = loadAll();
        RegistryCenterConfig activatedConfig = findActivatedRegistryCenterConfiguration(configs);
        if (!config.equals(activatedConfig)) {
            if (null != activatedConfig) {
                activatedConfig.setActivated(false);
            }
            config.setActivated(true);
            registryCenterConfigsRepository.save(configs);
        }
    }
    
    @Override
    public RegistryCenterConfigs loadAll() {
        return registryCenterConfigsRepository.load();
    }
    
    private RegistryCenterConfig findActivatedRegistryCenterConfiguration(final RegistryCenterConfigs registryCenterConfigs) {
        for (RegistryCenterConfig each : registryCenterConfigs.getRegistryCenterConfigs()) {
            if (each.isActivated()) {
                return each;
            }
        }
        return null;
    }
    
    private RegistryCenterConfig find(final String name, final RegistryCenterConfigs configs) {
        for (RegistryCenterConfig each : configs.getRegistryCenterConfigs()) {
            if (name.equals(each.getName())) {
                return each;
            }
        }
        return null;
    }
}
