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

package io.shardingsphere.shardingui.servcie.impl;

import com.google.common.base.Strings;
import io.shardingsphere.shardingui.servcie.ProxyAuthenticationService;
import io.shardingsphere.shardingui.servcie.RegistryCenterService;
import io.shardingsphere.shardingui.util.ConfigurationYamlConverter;
import org.apache.shardingsphere.core.rule.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of sharding proxy authentication service.
 *
 * @author chenqingyang
 */
@Service
public final class ProxyAuthenticationServiceImpl implements ProxyAuthenticationService {
    
    @Autowired
    private RegistryCenterService registryCenterService;
    
    @Override
    public Authentication getAuthentication() {
        String data = registryCenterService.getActivatedRegistryCenter().get(registryCenterService.getActivateConfigurationNode().getAuthenticationPath());
        return Strings.isNullOrEmpty(data) ? null : ConfigurationYamlConverter.loadAuthentication(data);
    }
    
    @Override
    public void updateAuthentication(final Authentication authentication) {
        registryCenterService.getActivatedRegistryCenter()
                .persist(registryCenterService.getActivateConfigurationNode().getAuthenticationPath(), ConfigurationYamlConverter.dumpAuthentication(authentication));
    }
}
