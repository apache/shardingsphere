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

import io.shardingsphere.shardingui.servcie.ConfigMapService;
import io.shardingsphere.shardingui.servcie.RegistryCenterService;
import io.shardingsphere.shardingui.util.ConfigurationYamlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of config map service.
 *
 * 4.0.0-RC1 has removed this directory.
 *
 * @author chenqingyang
 */
@Deprecated
@Service
public final class ConfigMapServiceImpl implements ConfigMapService {
    
    @Autowired
    private RegistryCenterService registryCenterService;
    
    @Override
    public Map<String, Object> loadConfigMap() {
//        String configData = registryCenterService.getActivatedRegistryCenter().get(registryCenterService.getActivateConfigurationNode().getConfigMapPath());
//        return ConfigurationYamlConverter.loadConfigMap(configData);
        return new HashMap<>();
    }
    
    @Override
    public void updateConfigMap(final Map<String, Object> configMap) {
        String configData = ConfigurationYamlConverter.dumpConfigMap(configMap);
//        registryCenterService.getActivatedRegistryCenter().persist(registryCenterService.getActivateConfigurationNode().getConfigMapPath(), configData);
    }
}
