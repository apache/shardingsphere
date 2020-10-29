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

package org.apache.shardingsphere.sharding.yaml.swapper.strategy;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlHintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlNoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlStandardShardingStrategyConfiguration;

/**
 * Sharding strategy configuration YAML swapper.
 */
public final class ShardingStrategyConfigurationYamlSwapper implements YamlSwapper<YamlShardingStrategyConfiguration, ShardingStrategyConfiguration> {
    
    @Override
    public YamlShardingStrategyConfiguration swapToYamlConfiguration(final ShardingStrategyConfiguration data) {
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        if (data instanceof StandardShardingStrategyConfiguration) {
            result.setStandard(createYamlStandardShardingStrategyConfiguration((StandardShardingStrategyConfiguration) data));
        }
        if (data instanceof ComplexShardingStrategyConfiguration) {
            result.setComplex(createYamlComplexShardingStrategyConfiguration((ComplexShardingStrategyConfiguration) data));
        }
        if (data instanceof HintShardingStrategyConfiguration) {
            result.setHint(createYamlHintShardingStrategyConfiguration((HintShardingStrategyConfiguration) data));
        }
        if (data instanceof NoneShardingStrategyConfiguration) {
            result.setNone(new YamlNoneShardingStrategyConfiguration());
        }
        return result;
    }
    
    @Override
    public ShardingStrategyConfiguration swapToObject(final YamlShardingStrategyConfiguration yamlConfig) {
        int shardingStrategyConfigCount = 0;
        ShardingStrategyConfiguration result = null;
        if (null != yamlConfig.getStandard()) {
            shardingStrategyConfigCount++;
            result = createStandardShardingStrategyConfiguration(yamlConfig.getStandard());
        }
        if (null != yamlConfig.getComplex()) {
            shardingStrategyConfigCount++;
            result = createComplexShardingStrategyConfiguration(yamlConfig.getComplex());
        }
        if (null != yamlConfig.getHint()) {
            shardingStrategyConfigCount++;
            result = createHintShardingStrategyConfiguration(yamlConfig.getHint());
        }
        if (null != yamlConfig.getNone()) {
            shardingStrategyConfigCount++;
            result = new NoneShardingStrategyConfiguration();
        }
        Preconditions.checkArgument(shardingStrategyConfigCount <= 1, "Only allowed 0 or 1 sharding strategy configuration.");
        return result;
    }
    
    private YamlStandardShardingStrategyConfiguration createYamlStandardShardingStrategyConfiguration(final StandardShardingStrategyConfiguration data) {
        YamlStandardShardingStrategyConfiguration result = new YamlStandardShardingStrategyConfiguration();
        result.setShardingColumn(data.getShardingColumn());
        result.setShardingAlgorithmName(data.getShardingAlgorithmName());
        return result;
    }
    
    private YamlComplexShardingStrategyConfiguration createYamlComplexShardingStrategyConfiguration(final ComplexShardingStrategyConfiguration data) {
        YamlComplexShardingStrategyConfiguration result = new YamlComplexShardingStrategyConfiguration();
        result.setShardingColumns(data.getShardingColumns());
        result.setShardingAlgorithmName(data.getShardingAlgorithmName());
        return result;
    }
    
    private YamlHintShardingStrategyConfiguration createYamlHintShardingStrategyConfiguration(final HintShardingStrategyConfiguration data) {
        YamlHintShardingStrategyConfiguration result = new YamlHintShardingStrategyConfiguration();
        result.setShardingAlgorithmName(data.getShardingAlgorithmName());
        return result;
    }
    
    private StandardShardingStrategyConfiguration createStandardShardingStrategyConfiguration(final YamlStandardShardingStrategyConfiguration yamlConfig) {
        return new StandardShardingStrategyConfiguration(yamlConfig.getShardingColumn(), yamlConfig.getShardingAlgorithmName());
    }
    
    private ComplexShardingStrategyConfiguration createComplexShardingStrategyConfiguration(final YamlComplexShardingStrategyConfiguration yamlConfig) {
        return new ComplexShardingStrategyConfiguration(yamlConfig.getShardingColumns(), yamlConfig.getShardingAlgorithmName());
    }
    
    private HintShardingStrategyConfiguration createHintShardingStrategyConfiguration(final YamlHintShardingStrategyConfiguration yamlConfig) {
        return new HintShardingStrategyConfiguration(yamlConfig.getShardingAlgorithmName());
    }
}
