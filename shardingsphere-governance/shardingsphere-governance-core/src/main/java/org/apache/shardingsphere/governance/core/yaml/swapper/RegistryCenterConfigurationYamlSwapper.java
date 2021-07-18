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

package org.apache.shardingsphere.governance.core.yaml.swapper;

import org.apache.shardingsphere.governance.core.yaml.pojo.YamlRegistryCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlConfigurationSwapper;

/**
 * Registry center configuration YAML swapper.
 */
public final class RegistryCenterConfigurationYamlSwapper implements YamlConfigurationSwapper<YamlRegistryCenterConfiguration, RegistryCenterConfiguration> {
    
    @Override
    public YamlRegistryCenterConfiguration swapToYamlConfiguration(final RegistryCenterConfiguration config) {
        YamlRegistryCenterConfiguration result = new YamlRegistryCenterConfiguration();
        result.setType(config.getType());
        result.setServerLists(config.getServerLists());
        result.setProps(config.getProps());
        return result;
    }
    
    @Override
    public RegistryCenterConfiguration swapToObject(final YamlRegistryCenterConfiguration yamlConfig) {
        return new RegistryCenterConfiguration(yamlConfig.getType(), yamlConfig.getServerLists(), yamlConfig.getProps());
    }
}
