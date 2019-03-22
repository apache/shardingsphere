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

package org.apache.shardingsphere.orchestration.yaml.swapper;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.orchestration.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.yaml.config.YamlOrchestrationConfiguration;

/**
 * Orchestration configuration YAML swapper.
 *
 * @author zhangliang
 */
public final class OrchestrationConfigurationYamlSwapper implements YamlSwapper<YamlOrchestrationConfiguration, OrchestrationConfiguration> {
    
    @Override
    public YamlOrchestrationConfiguration swap(final OrchestrationConfiguration data) {
        YamlOrchestrationConfiguration result = new YamlOrchestrationConfiguration();
        result.setName(data.getName());
        result.setRegistry(data.getRegCenterConfig());
        result.setOverwrite(data.isOverwrite());
        return result;
    }
    
    @Override
    public OrchestrationConfiguration swap(final YamlOrchestrationConfiguration yamlConfiguration) {
        Preconditions.checkNotNull(yamlConfiguration.getRegistry(), "Registry center must be required.");
        return new OrchestrationConfiguration(yamlConfiguration.getName(), yamlConfiguration.getRegistry(), yamlConfiguration.isOverwrite());
    }
}
