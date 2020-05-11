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

package org.apache.shardingsphere.core.yaml.swapper;

import org.apache.shardingsphere.api.config.sharding.strategy.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.core.shard.fixture.ComplexKeysShardingAlgorithmFixture;
import org.apache.shardingsphere.core.shard.fixture.HintShardingAlgorithmFixture;
import org.apache.shardingsphere.core.shard.fixture.StandardShardingAlgorithmFixture;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.strategy.YamlComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.strategy.YamlHintShardingStrategyConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.strategy.YamlNoneShardingStrategyConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.strategy.YamlShardingAlgorithmConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.strategy.YamlStandardShardingStrategyConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingStrategyConfigurationYamlSwapperTest {
    
    private final ShardingStrategyConfigurationYamlSwapper shardingStrategyConfigurationYamlSwapper = new ShardingStrategyConfigurationYamlSwapper();
    
    @Test
    public void assertSwapToYamlWithStandard() {
        StandardShardingAlgorithm standardShardingAlgorithm = mock(StandardShardingAlgorithm.class);
        YamlShardingStrategyConfiguration actual = shardingStrategyConfigurationYamlSwapper.swap(new StandardShardingStrategyConfiguration("id", standardShardingAlgorithm));
        assertThat(actual.getStandard().getShardingColumn(), is("id"));
        assertThat(actual.getStandard().getShardingAlgorithm(), instanceOf(YamlShardingAlgorithmConfiguration.class));
        assertNull(actual.getComplex());
        assertNull(actual.getHint());
        assertNull(actual.getNone());
    }
    
    @Test
    public void assertSwapToYamlWithComplex() {
        ComplexKeysShardingAlgorithm complexKeysShardingAlgorithm = mock(ComplexKeysShardingAlgorithm.class);
        when(complexKeysShardingAlgorithm.getType()).thenReturn("COMPLEX_TEST");
        YamlShardingStrategyConfiguration actual = shardingStrategyConfigurationYamlSwapper.swap(new ComplexShardingStrategyConfiguration("id, creation_date", complexKeysShardingAlgorithm));
        assertThat(actual.getComplex().getShardingColumns(), is("id, creation_date"));
        assertThat(actual.getComplex().getShardingAlgorithm().getType(), is("COMPLEX_TEST"));
        assertNull(actual.getStandard());
        assertNull(actual.getHint());
        assertNull(actual.getNone());
    }
    
    @Test
    public void assertSwapToYamlWithHint() {
        HintShardingAlgorithm hintShardingAlgorithm = mock(HintShardingAlgorithm.class);
        when(hintShardingAlgorithm.getType()).thenReturn("HINT_TEST");
        YamlShardingStrategyConfiguration actual = shardingStrategyConfigurationYamlSwapper.swap(new HintShardingStrategyConfiguration(hintShardingAlgorithm));
        assertThat(actual.getHint().getShardingAlgorithm().getType(), is("HINT_TEST"));
        assertNull(actual.getStandard());
        assertNull(actual.getComplex());
        assertNull(actual.getNone());
    }
    
    @Test
    public void assertSwapToYamlWithNone() {
        YamlShardingStrategyConfiguration actual = shardingStrategyConfigurationYamlSwapper.swap(new NoneShardingStrategyConfiguration());
        assertNull(actual.getStandard());
        assertNull(actual.getComplex());
        assertNull(actual.getHint());
        assertThat(actual.getNone(), instanceOf(YamlNoneShardingStrategyConfiguration.class));
    }
    
    @Test
    public void assertSwapToObjectWithStandardWithRangeShardingAlgorithm() {
        StandardShardingStrategyConfiguration actual = (StandardShardingStrategyConfiguration) shardingStrategyConfigurationYamlSwapper.swap(createStandardShardingStrategyConfiguration());
        assertThat(actual.getShardingColumn(), is("id"));
        assertThat(actual.getShardingAlgorithm(), instanceOf(StandardShardingAlgorithmFixture.class));
    }
    
    @Test
    public void assertSwapToObjectWithStandardWithoutRangeShardingAlgorithm() {
        StandardShardingStrategyConfiguration actual = (StandardShardingStrategyConfiguration) shardingStrategyConfigurationYamlSwapper.swap(createStandardShardingStrategyConfiguration());
        assertThat(actual.getShardingColumn(), is("id"));
        assertThat(actual.getShardingAlgorithm(), instanceOf(StandardShardingAlgorithmFixture.class));
    }
    
    private YamlShardingStrategyConfiguration createStandardShardingStrategyConfiguration() {
        YamlStandardShardingStrategyConfiguration yamlStandardShardingStrategyConfiguration = new YamlStandardShardingStrategyConfiguration();
        yamlStandardShardingStrategyConfiguration.setShardingColumn("id");
        YamlShardingAlgorithmConfiguration shardingAlgorithmConfiguration = new YamlShardingAlgorithmConfiguration();
        shardingAlgorithmConfiguration.setType("STANDARD_TEST");
        yamlStandardShardingStrategyConfiguration.setShardingAlgorithm(shardingAlgorithmConfiguration);
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        result.setStandard(yamlStandardShardingStrategyConfiguration);
        return result;
    }
    
    @Test
    public void assertSwapToObjectWithComplex() {
        ComplexShardingStrategyConfiguration actual = (ComplexShardingStrategyConfiguration) shardingStrategyConfigurationYamlSwapper.swap(createComplexShardingStrategyConfiguration());
        assertThat(actual.getShardingColumns(), is("id, creation_date"));
        assertThat(actual.getShardingAlgorithm(), instanceOf(ComplexKeysShardingAlgorithmFixture.class));
    }
    
    private YamlShardingStrategyConfiguration createComplexShardingStrategyConfiguration() {
        YamlComplexShardingStrategyConfiguration yamlComplexShardingStrategyConfiguration = new YamlComplexShardingStrategyConfiguration();
        yamlComplexShardingStrategyConfiguration.setShardingColumns("id, creation_date");
        yamlComplexShardingStrategyConfiguration.setShardingAlgorithm(new YamlShardingAlgorithmConfiguration());
        yamlComplexShardingStrategyConfiguration.getShardingAlgorithm().setType("COMPLEX_TEST");
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        result.setComplex(yamlComplexShardingStrategyConfiguration);
        return result;
    }
    
    @Test
    public void assertSwapToObjectWithHint() {
        HintShardingStrategyConfiguration actual = (HintShardingStrategyConfiguration) shardingStrategyConfigurationYamlSwapper.swap(createHintShardingStrategyConfiguration());
        assertThat(actual.getShardingAlgorithm(), instanceOf(HintShardingAlgorithmFixture.class));
    }
    
    private YamlShardingStrategyConfiguration createHintShardingStrategyConfiguration() {
        YamlHintShardingStrategyConfiguration yamlHintShardingStrategyConfiguration = new YamlHintShardingStrategyConfiguration();
        yamlHintShardingStrategyConfiguration.setShardingAlgorithm(new YamlShardingAlgorithmConfiguration());
        yamlHintShardingStrategyConfiguration.getShardingAlgorithm().setType("HINT_TEST");
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        result.setHint(yamlHintShardingStrategyConfiguration);
        return result;
    }
    
    @Test
    public void assertSwapToObjectWithNone() {
        NoneShardingStrategyConfiguration actual = (NoneShardingStrategyConfiguration) shardingStrategyConfigurationYamlSwapper.swap(createNoneShardingStrategyConfiguration());
        assertThat(actual, instanceOf(NoneShardingStrategyConfiguration.class));
    }
    
    private YamlShardingStrategyConfiguration createNoneShardingStrategyConfiguration() {
        YamlNoneShardingStrategyConfiguration noneShardingStrategyConfig = new YamlNoneShardingStrategyConfiguration();
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        result.setNone(noneShardingStrategyConfig);
        return result;
    }
    
    @Test
    public void assertSwapToObjectWithNull() {
        assertNull(shardingStrategyConfigurationYamlSwapper.swap(new YamlShardingStrategyConfiguration()));
    }
}
