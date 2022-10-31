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

package org.apache.shardingsphere.shadow.yaml.swapper.datasource;

import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.datasource.YamlShadowDataSourceConfiguration;

/**
 * YAML shadow data source configuration swapper.
 */
public final class YamlShadowDataSourceConfigurationSwapper implements YamlConfigurationSwapper<YamlShadowDataSourceConfiguration, ShadowDataSourceConfiguration> {
    
    @Override
    public YamlShadowDataSourceConfiguration swapToYamlConfiguration(final ShadowDataSourceConfiguration data) {
        YamlShadowDataSourceConfiguration result = new YamlShadowDataSourceConfiguration();
        result.setProductionDataSourceName(data.getProductionDataSourceName());
        result.setShadowDataSourceName(data.getShadowDataSourceName());
        return result;
    }
    
    @Override
    public ShadowDataSourceConfiguration swapToObject(final YamlShadowDataSourceConfiguration yamlConfig) {
        return new ShadowDataSourceConfiguration(yamlConfig.getProductionDataSourceName(), yamlConfig.getShadowDataSourceName());
    }
}
