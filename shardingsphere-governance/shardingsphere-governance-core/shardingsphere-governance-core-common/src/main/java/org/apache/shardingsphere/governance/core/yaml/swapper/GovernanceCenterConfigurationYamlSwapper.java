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

import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;

/**
 * Governance center configuration YAML swapper.
 */
public final class GovernanceCenterConfigurationYamlSwapper implements YamlSwapper<YamlGovernanceCenterConfiguration, GovernanceCenterConfiguration> {
    
    @Override
    public YamlGovernanceCenterConfiguration swapToYamlConfiguration(final GovernanceCenterConfiguration config) {
        YamlGovernanceCenterConfiguration result = new YamlGovernanceCenterConfiguration();
        result.setType(config.getType());
        result.setServerLists(config.getServerLists());
        result.setProps(config.getProps());
        return result;
    }
    
    @Override
    public GovernanceCenterConfiguration swapToObject(final YamlGovernanceCenterConfiguration yamlConfig) {
        return new GovernanceCenterConfiguration(yamlConfig.getType(), yamlConfig.getServerLists(), yamlConfig.getProps());
    }
}
