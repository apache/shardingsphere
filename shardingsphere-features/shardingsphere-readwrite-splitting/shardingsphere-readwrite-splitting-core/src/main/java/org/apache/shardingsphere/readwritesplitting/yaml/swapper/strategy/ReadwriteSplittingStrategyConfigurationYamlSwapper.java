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

import org.apache.shardingsphere.infra.yaml.config.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.readwritesplitting.api.strategy.DynamicReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.ReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.strategy.YamlDynamicReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.strategy.YamlReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.strategy.YamlStaticReadwriteSplittingStrategyConfiguration;

/**
 * Readwrite-splitting strategy configuration YAML swapper.
 */
public final class ReadwriteSplittingStrategyConfigurationYamlSwapper implements YamlConfigurationSwapper<YamlReadwriteSplittingStrategyConfiguration, ReadwriteSplittingStrategyConfiguration> {
    
    @Override
    public YamlReadwriteSplittingStrategyConfiguration swapToYamlConfiguration(final ReadwriteSplittingStrategyConfiguration config) {
        YamlReadwriteSplittingStrategyConfiguration result = new YamlReadwriteSplittingStrategyConfiguration();
        if (config instanceof StaticReadwriteSplittingStrategyConfiguration) {
            result.setStaticStrategy(createYamlStaticReadwriteSplittingStrategyConfiguration((StaticReadwriteSplittingStrategyConfiguration) config));
        }
        if (config instanceof DynamicReadwriteSplittingStrategyConfiguration) {
            result.setDynamicStrategy(createYamlDynamicReadwriteSplittingStrategyConfiguration((DynamicReadwriteSplittingStrategyConfiguration) config));
        }
        return result;
    }
    
    @Override
    public ReadwriteSplittingStrategyConfiguration swapToObject(final YamlReadwriteSplittingStrategyConfiguration yamlConfig) {
        ReadwriteSplittingStrategyConfiguration result = null;
        if (null != yamlConfig.getStaticStrategy()) {
            result = createStaticReadwriteSplittingStrategyConfiguration(yamlConfig.getStaticStrategy());
        }
        if (null != yamlConfig.getDynamicStrategy()) {
            result = createDynamicReadwriteSplittingStrategyConfiguration(yamlConfig.getDynamicStrategy());
        }
        return result;
    }
    
    private YamlStaticReadwriteSplittingStrategyConfiguration createYamlStaticReadwriteSplittingStrategyConfiguration(final StaticReadwriteSplittingStrategyConfiguration config) {
        YamlStaticReadwriteSplittingStrategyConfiguration result = new YamlStaticReadwriteSplittingStrategyConfiguration();
        result.setWriteDataSourceName(config.getWriteDataSourceName());
        result.setReadDataSourceNames(config.getReadDataSourceNames());
        return result;
    }
    
    private YamlDynamicReadwriteSplittingStrategyConfiguration createYamlDynamicReadwriteSplittingStrategyConfiguration(final DynamicReadwriteSplittingStrategyConfiguration config) {
        YamlDynamicReadwriteSplittingStrategyConfiguration result = new YamlDynamicReadwriteSplittingStrategyConfiguration();
        result.setAutoAwareDataSourceName(config.getAutoAwareDataSourceName());
        result.setWriteDataSourceQueryEnabled(config.isWriteDataSourceQueryEnabled());
        return result;
    }
    
    private StaticReadwriteSplittingStrategyConfiguration createStaticReadwriteSplittingStrategyConfiguration(final YamlStaticReadwriteSplittingStrategyConfiguration yamlConfig) {
        return new StaticReadwriteSplittingStrategyConfiguration(yamlConfig.getWriteDataSourceName(), yamlConfig.getReadDataSourceNames());
    }
    
    private DynamicReadwriteSplittingStrategyConfiguration createDynamicReadwriteSplittingStrategyConfiguration(final YamlDynamicReadwriteSplittingStrategyConfiguration yamlConfig) {
        return new DynamicReadwriteSplittingStrategyConfiguration(yamlConfig.getAutoAwareDataSourceName(), yamlConfig.isWriteDataSourceQueryEnabled());
    }
}
