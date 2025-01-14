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

package org.apache.shardingsphere.infra.instance.yaml;

import org.apache.shardingsphere.infra.instance.ComputeNodeData;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

/**
 * YAML compute node data swapper.
 */
public final class YamlComputeNodeDataSwapper implements YamlConfigurationSwapper<YamlComputeNodeData, ComputeNodeData> {
    
    @Override
    public YamlComputeNodeData swapToYamlConfiguration(final ComputeNodeData data) {
        YamlComputeNodeData result = new YamlComputeNodeData();
        result.setDatabaseName(data.getDatabaseName());
        result.setAttribute(data.getAttribute());
        result.setVersion(data.getVersion());
        return result;
    }
    
    @Override
    public ComputeNodeData swapToObject(final YamlComputeNodeData yamlConfig) {
        return new ComputeNodeData(yamlConfig.getDatabaseName(), yamlConfig.getAttribute(), yamlConfig.getVersion());
    }
}
