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

package org.apache.shardingsphere.shadow.yaml.swapper.table;

import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;

import java.util.LinkedList;

/**
 * YAML shadow table configuration swapper.
 */
public final class YamlShadowTableConfigurationSwapper implements YamlConfigurationSwapper<YamlShadowTableConfiguration, ShadowTableConfiguration> {
    
    @Override
    public YamlShadowTableConfiguration swapToYamlConfiguration(final ShadowTableConfiguration data) {
        YamlShadowTableConfiguration result = new YamlShadowTableConfiguration();
        result.setDataSourceNames(data.getDataSourceNames());
        result.setShadowAlgorithmNames(data.getShadowAlgorithmNames());
        return result;
    }
    
    @Override
    public ShadowTableConfiguration swapToObject(final YamlShadowTableConfiguration yamlConfig) {
        return new ShadowTableConfiguration(yamlConfig.getDataSourceNames(), new LinkedList<>(yamlConfig.getShadowAlgorithmNames()));
    }
}
