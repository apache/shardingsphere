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

package org.apache.shardingsphere.readwritesplitting.yaml.swapper.strategy;

import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.readwritesplitting.api.strategy.DynamicReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.strategy.YamlDynamicReadwriteSplittingStrategyConfiguration;

/**
 * YAML dynamic readwrite-splitting strategy configuration swapper.
 */
public final class YamlDynamicReadwriteSplittingStrategyConfigurationSwapper
        implements
            YamlConfigurationSwapper<YamlDynamicReadwriteSplittingStrategyConfiguration, DynamicReadwriteSplittingStrategyConfiguration> {
    
    @Override
    public YamlDynamicReadwriteSplittingStrategyConfiguration swapToYamlConfiguration(final DynamicReadwriteSplittingStrategyConfiguration config) {
        YamlDynamicReadwriteSplittingStrategyConfiguration result = new YamlDynamicReadwriteSplittingStrategyConfiguration();
        result.setAutoAwareDataSourceName(config.getAutoAwareDataSourceName());
        result.setWriteDataSourceQueryEnabled(config.getWriteDataSourceQueryEnabled());
        return result;
    }
    
    @Override
    public DynamicReadwriteSplittingStrategyConfiguration swapToObject(final YamlDynamicReadwriteSplittingStrategyConfiguration config) {
        DynamicReadwriteSplittingStrategyConfiguration result = null;
        if (null != config) {
            result = new DynamicReadwriteSplittingStrategyConfiguration(config.getAutoAwareDataSourceName(), config.getWriteDataSourceQueryEnabled());
        }
        return result;
    }
}
