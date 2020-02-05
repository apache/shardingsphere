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

package org.apache.shardingsphere.ui.servcie.impl;

import com.google.common.base.Optional;
import org.apache.shardingsphere.ui.common.domain.RegistryCenterConfig;
import org.apache.shardingsphere.ui.common.domain.RegistryCenterConfigs;
import org.apache.shardingsphere.ui.common.exception.ShardingSphereUIException;
import org.apache.shardingsphere.ui.repository.RegistryCenterConfigsRepository;
import org.apache.shardingsphere.ui.servcie.RegistryCenterConfigService;
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
        if (null != existedConfig) {
            throw new ShardingSphereUIException(ShardingSphereUIException.SERVER_ERROR, "Registry center already existed!");
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
    public void setActivated(final String name) {
        RegistryCenterConfigs configs = loadAll();
        RegistryCenterConfig config = find(name, configs);
        if (null == config) {
            throw new ShardingSphereUIException(ShardingSphereUIException.SERVER_ERROR, "Registry center not existed!");
        }
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
