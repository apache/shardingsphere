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
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class YamlShardingStrategyConfigurationSwapperTest {
    
    @Test
    void assertSwapToYamlConfigurationForStandardShardingStrategy() {
        ShardingStrategyConfiguration data = new StandardShardingStrategyConfiguration("order_id", "core_standard_fixture");
        YamlShardingStrategyConfigurationSwapper swapper = new YamlShardingStrategyConfigurationSwapper();
        YamlShardingStrategyConfiguration actual = swapper.swapToYamlConfiguration(data);
        assertThat(actual.getStandard().getShardingColumn(), is("order_id"));
        assertThat(actual.getStandard().getShardingAlgorithmName(), is("core_standard_fixture"));
    }
    
    @Test
    void assertSwapToYamlConfigurationForComplexShardingStrategy() {
        ShardingStrategyConfiguration data = new ComplexShardingStrategyConfiguration("region_id, user_id", "core_complex_fixture");
        YamlShardingStrategyConfigurationSwapper swapper = new YamlShardingStrategyConfigurationSwapper();
        YamlShardingStrategyConfiguration actual = swapper.swapToYamlConfiguration(data);
        assertThat(actual.getComplex().getShardingColumns(), is("region_id, user_id"));
        assertThat(actual.getComplex().getShardingAlgorithmName(), is("core_complex_fixture"));
    }
    
    @Test
    void assertSwapToYamlConfigurationForHintShardingStrategy() {
        ShardingStrategyConfiguration data = new HintShardingStrategyConfiguration("core_hint_fixture");
        YamlShardingStrategyConfigurationSwapper swapper = new YamlShardingStrategyConfigurationSwapper();
        YamlShardingStrategyConfiguration actual = swapper.swapToYamlConfiguration(data);
        assertThat(actual.getHint().getShardingAlgorithmName(), is("core_hint_fixture"));
    }
    
    @Test
    void assertSwapToYamlConfigurationForNoneShardingStrategy() {
        ShardingStrategyConfiguration data = new NoneShardingStrategyConfiguration();
        YamlShardingStrategyConfigurationSwapper swapper = new YamlShardingStrategyConfigurationSwapper();
        YamlShardingStrategyConfiguration actual = swapper.swapToYamlConfiguration(data);
        assertThat(actual.getNone().getClass(), is(YamlNoneShardingStrategyConfiguration.class));
    }
    
    @Test
    void assertSwapToObjectForStandardShardingStrategy() {
        YamlShardingStrategyConfiguration yamlConfig = new YamlShardingStrategyConfiguration();
        yamlConfig.setStandard(createYamlStandardShardingStrategyConfiguration());
        YamlShardingStrategyConfigurationSwapper swapper = new YamlShardingStrategyConfigurationSwapper();
        ShardingStrategyConfiguration actual = swapper.swapToObject(yamlConfig);
        assertThat(actual, instanceOf(StandardShardingStrategyConfiguration.class));
        assertThat(((StandardShardingStrategyConfiguration) actual).getShardingColumn(), is("order_id"));
        assertThat(actual.getShardingAlgorithmName(), is("core_standard_fixture"));
    }
    
    private YamlStandardShardingStrategyConfiguration createYamlStandardShardingStrategyConfiguration() {
        YamlStandardShardingStrategyConfiguration result = new YamlStandardShardingStrategyConfiguration();
        result.setShardingColumn("order_id");
        result.setShardingAlgorithmName("core_standard_fixture");
        return result;
    }
    
    @Test
    void assertSwapToObjectForComplexShardingStrategy() {
        YamlShardingStrategyConfiguration yamlConfig = new YamlShardingStrategyConfiguration();
        yamlConfig.setComplex(createYamlComplexShardingStrategyConfiguration());
        YamlShardingStrategyConfigurationSwapper swapper = new YamlShardingStrategyConfigurationSwapper();
        ShardingStrategyConfiguration actual = swapper.swapToObject(yamlConfig);
        assertThat(actual, instanceOf(ComplexShardingStrategyConfiguration.class));
        assertThat(((ComplexShardingStrategyConfiguration) actual).getShardingColumns(), is("region_id, user_id"));
        assertThat(actual.getShardingAlgorithmName(), is("core_complex_fixture"));
    }
    
    private YamlComplexShardingStrategyConfiguration createYamlComplexShardingStrategyConfiguration() {
        YamlComplexShardingStrategyConfiguration result = new YamlComplexShardingStrategyConfiguration();
        result.setShardingColumns("region_id, user_id");
        result.setShardingAlgorithmName("core_complex_fixture");
        return result;
    }
    
    @Test
    void assertSwapToObjectForHintShardingStrategy() {
        YamlShardingStrategyConfiguration yamlConfig = new YamlShardingStrategyConfiguration();
        yamlConfig.setHint(createYamlHintShardingStrategyConfiguration());
        YamlShardingStrategyConfigurationSwapper swapper = new YamlShardingStrategyConfigurationSwapper();
        ShardingStrategyConfiguration actual = swapper.swapToObject(yamlConfig);
        assertThat(actual, instanceOf(HintShardingStrategyConfiguration.class));
        assertThat(actual.getShardingAlgorithmName(), is("core_hint_fixture"));
    }
    
    private YamlHintShardingStrategyConfiguration createYamlHintShardingStrategyConfiguration() {
        YamlHintShardingStrategyConfiguration result = new YamlHintShardingStrategyConfiguration();
        result.setShardingAlgorithmName("core_hint_fixture");
        return result;
    }
    
    @Test
    void assertSwapToObjectForNoneShardingStrategy() {
        YamlShardingStrategyConfiguration yamlConfig = new YamlShardingStrategyConfiguration();
        yamlConfig.setNone(new YamlNoneShardingStrategyConfiguration());
        YamlShardingStrategyConfigurationSwapper swapper = new YamlShardingStrategyConfigurationSwapper();
        ShardingStrategyConfiguration actual = swapper.swapToObject(yamlConfig);
        assertThat(actual, instanceOf(NoneShardingStrategyConfiguration.class));
    }
}
