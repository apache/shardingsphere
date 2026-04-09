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

import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.config.keygen.impl.ColumnKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.infra.config.keygen.KeyGenerateStrategiesConfiguration;
import org.apache.shardingsphere.infra.config.keygen.impl.SequenceKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyRuleConfiguration;

/**
 * YAML key generate strategy rule configuration swapper.
 */
public final class YamlKeyGenerateStrategyRuleConfigurationSwapper implements YamlConfigurationSwapper<YamlKeyGenerateStrategyRuleConfiguration, KeyGenerateStrategiesConfiguration> {
    
    @Override
    public YamlKeyGenerateStrategyRuleConfiguration swapToYamlConfiguration(final KeyGenerateStrategiesConfiguration data) {
        YamlKeyGenerateStrategyRuleConfiguration result = new YamlKeyGenerateStrategyRuleConfiguration();
        result.setKeyGenerateType(data.getKeyGenerateType());
        result.setKeyGeneratorName(data.getKeyGeneratorName());
        if (data instanceof ColumnKeyGenerateStrategiesRuleConfiguration) {
            result.setLogicTable(((ColumnKeyGenerateStrategiesRuleConfiguration) data).getLogicTable());
            result.setKeyGenerateColumn(((ColumnKeyGenerateStrategiesRuleConfiguration) data).getKeyGenerateColumn());
        } else {
            result.setKeyGenerateSequence(((SequenceKeyGenerateStrategiesRuleConfiguration) data).getKeyGenerateSequence());
        }
        return result;
    }
    
    @Override
    public KeyGenerateStrategiesConfiguration swapToObject(final YamlKeyGenerateStrategyRuleConfiguration yamlConfig) {
        if ("sequence".equalsIgnoreCase(yamlConfig.getKeyGenerateType())) {
            return new SequenceKeyGenerateStrategiesRuleConfiguration(yamlConfig.getKeyGeneratorName(), yamlConfig.getKeyGenerateSequence());
        }
        return new ColumnKeyGenerateStrategiesRuleConfiguration(yamlConfig.getKeyGeneratorName(), yamlConfig.getLogicTable(), yamlConfig.getKeyGenerateColumn());
    }
}
