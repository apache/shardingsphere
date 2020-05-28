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

package org.apache.shardingsphere.orchestration.center.yaml.swapper;

import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlCenterRepositoryConfiguration;

/**
 * Orchestration instance configuration YAML swapper.
 */
public final class CenterRepositoryConfigurationYamlSwapper implements YamlSwapper<YamlCenterRepositoryConfiguration, CenterConfiguration> {
    
    @Override
    public YamlCenterRepositoryConfiguration swap(final CenterConfiguration configuration) {
        YamlCenterRepositoryConfiguration result = new YamlCenterRepositoryConfiguration();
        result.setOrchestrationType(configuration.getOrchestrationType());
        result.setInstanceType(configuration.getType());
        result.setServerLists(configuration.getServerLists());
        result.setNamespace(configuration.getNamespace());
        result.setProps(configuration.getProperties());
        return result;
    }
    
    @Override
    public CenterConfiguration swap(final YamlCenterRepositoryConfiguration yamlConfiguration) {
        CenterConfiguration result = new CenterConfiguration(yamlConfiguration.getInstanceType(), yamlConfiguration.getProps());
        result.setOrchestrationType(yamlConfiguration.getOrchestrationType());
        result.setServerLists(yamlConfiguration.getServerLists());
        result.setNamespace(yamlConfiguration.getNamespace());
        return result;
    }
}
