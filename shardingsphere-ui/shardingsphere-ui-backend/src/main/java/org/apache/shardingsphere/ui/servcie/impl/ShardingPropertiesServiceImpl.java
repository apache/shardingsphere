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

import org.apache.shardingsphere.ui.servcie.ConfigCenterService;
import org.apache.shardingsphere.ui.servcie.ShardingPropertiesService;
import org.apache.shardingsphere.ui.util.ConfigurationYamlConverter;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Implementation of sharding properties service.
 */
@Service
public final class ShardingPropertiesServiceImpl implements ShardingPropertiesService {
    
    @Autowired
    private ConfigCenterService configCenterService;
    
    @Override
    public String loadShardingProperties() {
        return configCenterService.getActivatedConfigCenter().get(configCenterService.getActivateConfigurationNode().getPropsPath());
    }
    
    @Override
    public void updateShardingProperties(final String configData) {
        checkShardingProperties(configData);
        configCenterService.getActivatedConfigCenter().persist(configCenterService.getActivateConfigurationNode().getPropsPath(), configData);
    }
    
    private void checkShardingProperties(final String configData) {
        try {
            Properties properties = ConfigurationYamlConverter.loadProperties(configData);
            new ConfigurationProperties(properties);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new IllegalArgumentException("Sharding properties is invalid.");
        }
    }
}
