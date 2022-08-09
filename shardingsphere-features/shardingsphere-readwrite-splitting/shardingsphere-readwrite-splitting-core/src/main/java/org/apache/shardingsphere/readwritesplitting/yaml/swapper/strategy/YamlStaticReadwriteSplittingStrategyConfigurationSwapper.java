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
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.strategy.YamlStaticReadwriteSplittingStrategyConfiguration;

/**
 * YAML static readwrite-splitting strategy configuration swapper.
 */
public final class YamlStaticReadwriteSplittingStrategyConfigurationSwapper
        implements
            YamlConfigurationSwapper<YamlStaticReadwriteSplittingStrategyConfiguration, StaticReadwriteSplittingStrategyConfiguration> {
    
    @Override
    public YamlStaticReadwriteSplittingStrategyConfiguration swapToYamlConfiguration(final StaticReadwriteSplittingStrategyConfiguration config) {
        YamlStaticReadwriteSplittingStrategyConfiguration result = new YamlStaticReadwriteSplittingStrategyConfiguration();
        result.setWriteDataSourceName(config.getWriteDataSourceName());
        result.setReadDataSourceNames(config.getReadDataSourceNames());
        return result;
    }
    
    @Override
    public StaticReadwriteSplittingStrategyConfiguration swapToObject(final YamlStaticReadwriteSplittingStrategyConfiguration yamlConfig) {
        StaticReadwriteSplittingStrategyConfiguration result = null;
        if (null != yamlConfig) {
            result = new StaticReadwriteSplittingStrategyConfiguration(yamlConfig.getWriteDataSourceName(), yamlConfig.getReadDataSourceNames());
        }
        return result;
    }
}
