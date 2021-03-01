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

package org.apache.shardingsphere.scaling.core.config.yaml;

import org.apache.shardingsphere.governance.core.yaml.swapper.GovernanceConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;

/**
 * Server configuration yaml swapper.
 */
public final class ServerConfigurationYamlSwapper implements YamlConfigurationSwapper<YamlServerConfiguration, ServerConfiguration> {
    
    private final GovernanceConfigurationYamlSwapper governanceConfigurationYamlSwapper = new GovernanceConfigurationYamlSwapper();
    
    @Override
    public YamlServerConfiguration swapToYamlConfiguration(final ServerConfiguration data) {
        YamlServerConfiguration result = new YamlServerConfiguration();
        result.getScaling().setPort(data.getPort());
        result.getScaling().setBlockQueueSize(data.getBlockQueueSize());
        result.getScaling().setWorkerThread(data.getWorkerThread());
        if (null != data.getGovernanceConfig()) {
            result.setGovernance(governanceConfigurationYamlSwapper.swapToYamlConfiguration(data.getGovernanceConfig()));
        }
        return result;
    }
    
    @Override
    public ServerConfiguration swapToObject(final YamlServerConfiguration yamlConfig) {
        ServerConfiguration result = new ServerConfiguration();
        result.setPort(yamlConfig.getScaling().getPort());
        result.setBlockQueueSize(yamlConfig.getScaling().getBlockQueueSize());
        result.setWorkerThread(yamlConfig.getScaling().getWorkerThread());
        if (null != yamlConfig.getGovernance()) {
            result.setGovernanceConfig(governanceConfigurationYamlSwapper.swapToObject(yamlConfig.getGovernance()));
        }
        return result;
    }
}
