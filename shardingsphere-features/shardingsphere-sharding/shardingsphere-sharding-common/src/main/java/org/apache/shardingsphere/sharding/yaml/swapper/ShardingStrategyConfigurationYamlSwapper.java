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

package org.apache.shardingsphere.sharding.yaml.swapper;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sharding.api.config.strategy.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.YamlComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.YamlHintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.YamlNoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.YamlShardingAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.YamlStandardShardingStrategyConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.infra.spi.type.TypedSPIRegistry;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;

/**
 * Sharding strategy configuration YAML swapper.
 */
public final class ShardingStrategyConfigurationYamlSwapper implements YamlSwapper<YamlShardingStrategyConfiguration, ShardingStrategyConfiguration> {
    
    static {
        ShardingSphereServiceLoader.register(ShardingAlgorithm.class);
    }
    
    @Override
    public YamlShardingStrategyConfiguration swap(final ShardingStrategyConfiguration data) {
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
    public ShardingStrategyConfiguration swap(final YamlShardingStrategyConfiguration yamlConfiguration) {
        int shardingStrategyConfigCount = 0;
        ShardingStrategyConfiguration result = null;
        if (null != yamlConfiguration.getStandard()) {
            shardingStrategyConfigCount++;
            result = createStandardShardingStrategyConfiguration(yamlConfiguration.getStandard());
        }
        if (null != yamlConfiguration.getComplex()) {
            shardingStrategyConfigCount++;
            result = createComplexShardingStrategyConfiguration(yamlConfiguration.getComplex());
        }
        if (null != yamlConfiguration.getHint()) {
            shardingStrategyConfigCount++;
            result = createHintShardingStrategyConfiguration(yamlConfiguration.getHint());
        }
        if (null != yamlConfiguration.getNone()) {
            shardingStrategyConfigCount++;
            result = new NoneShardingStrategyConfiguration();
        }
        Preconditions.checkArgument(shardingStrategyConfigCount <= 1, "Only allowed 0 or 1 sharding strategy configuration.");
        return result;
    }
    
    private YamlStandardShardingStrategyConfiguration createYamlStandardShardingStrategyConfiguration(final StandardShardingStrategyConfiguration data) {
        YamlStandardShardingStrategyConfiguration result = new YamlStandardShardingStrategyConfiguration();
        result.setShardingColumn(data.getShardingColumn());
        result.setShardingAlgorithm(createYamlShardingAlgorithmConfiguration(data.getShardingAlgorithm()));
        return result;
    }
    
    private YamlComplexShardingStrategyConfiguration createYamlComplexShardingStrategyConfiguration(final ComplexShardingStrategyConfiguration data) {
        YamlComplexShardingStrategyConfiguration result = new YamlComplexShardingStrategyConfiguration();
        result.setShardingColumns(data.getShardingColumns());
        result.setShardingAlgorithm(createYamlShardingAlgorithmConfiguration(data.getShardingAlgorithm()));
        return result;
    }
    
    private YamlHintShardingStrategyConfiguration createYamlHintShardingStrategyConfiguration(final HintShardingStrategyConfiguration data) {
        YamlHintShardingStrategyConfiguration result = new YamlHintShardingStrategyConfiguration();
        result.setShardingAlgorithm(createYamlShardingAlgorithmConfiguration(data.getShardingAlgorithm()));
        return result;
    }
    
    private StandardShardingStrategyConfiguration createStandardShardingStrategyConfiguration(final YamlStandardShardingStrategyConfiguration yamlConfiguration) {
        return new StandardShardingStrategyConfiguration(yamlConfiguration.getShardingColumn(),
                createShardingAlgorithm(StandardShardingAlgorithm.class, yamlConfiguration.getShardingAlgorithm()));
    }
    
    private ComplexShardingStrategyConfiguration createComplexShardingStrategyConfiguration(final YamlComplexShardingStrategyConfiguration yamlConfiguration) {
        return new ComplexShardingStrategyConfiguration(yamlConfiguration.getShardingColumns(), 
                createShardingAlgorithm(ComplexKeysShardingAlgorithm.class, yamlConfiguration.getShardingAlgorithm()));
    }
    
    private HintShardingStrategyConfiguration createHintShardingStrategyConfiguration(final YamlHintShardingStrategyConfiguration yamlConfiguration) {
        return new HintShardingStrategyConfiguration(createShardingAlgorithm(HintShardingAlgorithm.class, yamlConfiguration.getShardingAlgorithm()));
    }
    
    private YamlShardingAlgorithmConfiguration createYamlShardingAlgorithmConfiguration(final ShardingAlgorithm shardingAlgorithm) {
        YamlShardingAlgorithmConfiguration result = new YamlShardingAlgorithmConfiguration();
        result.setType(shardingAlgorithm.getType());
        result.setProps(shardingAlgorithm.getProperties());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private <T extends ShardingAlgorithm> T createShardingAlgorithm(final Class<T> shardingAlgorithmClass, final YamlShardingAlgorithmConfiguration shardingAlgorithm) {
        ShardingAlgorithm result = TypedSPIRegistry.getRegisteredService(ShardingAlgorithm.class, shardingAlgorithm.getType(), shardingAlgorithm.getProps());
        if (!shardingAlgorithmClass.isAssignableFrom(result.getClass())) {
            throw new ShardingSphereException("Class %s is not an implementation of %s", result.getClass().getName(), shardingAlgorithmClass.getName());
        }
        return (T) result;
    }
}
