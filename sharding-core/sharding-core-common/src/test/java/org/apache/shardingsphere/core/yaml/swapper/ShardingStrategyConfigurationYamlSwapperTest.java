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
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.core.shard.fixture.ComplexKeysShardingAlgorithmFixture;
import org.apache.shardingsphere.core.shard.fixture.HintShardingAlgorithmFixture;
import org.apache.shardingsphere.core.shard.fixture.PreciseShardingAlgorithmFixture;
import org.apache.shardingsphere.core.shard.fixture.RangeShardingAlgorithmFixture;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.strategy.YamlComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.strategy.YamlHintShardingStrategyConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.strategy.YamlInlineShardingStrategyConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.strategy.YamlNoneShardingStrategyConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.strategy.YamlStandardShardingStrategyConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ShardingStrategyConfigurationYamlSwapperTest {
    
    private ShardingStrategyConfigurationYamlSwapper shardingStrategyConfigurationYamlSwapper = new ShardingStrategyConfigurationYamlSwapper();
    
    @Test
    public void assertSwapToYamlWithStandard() {
        PreciseShardingAlgorithm preciseShardingAlgorithm = mock(PreciseShardingAlgorithm.class);
        RangeShardingAlgorithm rangeShardingAlgorithm = mock(RangeShardingAlgorithm.class);
        YamlShardingStrategyConfiguration actual = shardingStrategyConfigurationYamlSwapper.swap(new StandardShardingStrategyConfiguration("id", preciseShardingAlgorithm, rangeShardingAlgorithm));
        assertThat(actual.getStandard().getShardingColumn(), is("id"));
        assertThat(actual.getStandard().getPreciseAlgorithmClassName(), is(preciseShardingAlgorithm.getClass().getName()));
        assertThat(actual.getStandard().getRangeAlgorithmClassName(), is(rangeShardingAlgorithm.getClass().getName()));
        assertNull(actual.getInline());
        assertNull(actual.getComplex());
        assertNull(actual.getHint());
        assertNull(actual.getNone());
    }
    
    @Test
    public void assertSwapToYamlWithInline() {
        YamlShardingStrategyConfiguration actual = shardingStrategyConfigurationYamlSwapper.swap(new InlineShardingStrategyConfiguration("id", "xxx_$->{id % 10}"));
        assertThat(actual.getInline().getShardingColumn(), is("id"));
        assertThat(actual.getInline().getAlgorithmExpression(), is("xxx_$->{id % 10}"));
        assertNull(actual.getStandard());
        assertNull(actual.getComplex());
        assertNull(actual.getHint());
        assertNull(actual.getNone());
    }
    
    @Test
    public void assertSwapToYamlWithComplex() {
        ComplexKeysShardingAlgorithm complexKeysShardingAlgorithm = mock(ComplexKeysShardingAlgorithm.class);
        YamlShardingStrategyConfiguration actual = shardingStrategyConfigurationYamlSwapper.swap(new ComplexShardingStrategyConfiguration("id, creation_date", complexKeysShardingAlgorithm));
        assertThat(actual.getComplex().getShardingColumns(), is("id, creation_date"));
        assertThat(actual.getComplex().getAlgorithmClassName(), is(complexKeysShardingAlgorithm.getClass().getName()));
        assertNull(actual.getStandard());
        assertNull(actual.getInline());
        assertNull(actual.getHint());
        assertNull(actual.getNone());
    }
    
    @Test
    public void assertSwapToYamlWithHint() {
        HintShardingAlgorithm hintShardingAlgorithm = mock(HintShardingAlgorithm.class);
        YamlShardingStrategyConfiguration actual = shardingStrategyConfigurationYamlSwapper.swap(new HintShardingStrategyConfiguration(hintShardingAlgorithm));
        assertThat(actual.getHint().getAlgorithmClassName(), is(hintShardingAlgorithm.getClass().getName()));
        assertNull(actual.getStandard());
        assertNull(actual.getInline());
        assertNull(actual.getComplex());
        assertNull(actual.getNone());
    }
    
    @Test
    public void assertSwapToYamlWithNone() {
        YamlShardingStrategyConfiguration actual = shardingStrategyConfigurationYamlSwapper.swap(new NoneShardingStrategyConfiguration());
        assertNull(actual.getStandard());
        assertNull(actual.getInline());
        assertNull(actual.getComplex());
        assertNull(actual.getHint());
        assertThat(actual.getNone(), instanceOf(YamlNoneShardingStrategyConfiguration.class));
    }
    
    @Test
    public void assertSwapToObjectWithStandardWithRangeShardingAlgorithm() {
        StandardShardingStrategyConfiguration actual = (StandardShardingStrategyConfiguration) shardingStrategyConfigurationYamlSwapper.swap(createStandardShardingStrategyConfiguration(true));
        assertThat(actual.getShardingColumn(), is("id"));
        assertThat(actual.getPreciseShardingAlgorithm(), instanceOf(PreciseShardingAlgorithmFixture.class));
        assertThat(actual.getRangeShardingAlgorithm(), instanceOf(RangeShardingAlgorithmFixture.class));
    }
    
    @Test
    public void assertSwapToObjectWithStandardWithoutRangeShardingAlgorithm() {
        StandardShardingStrategyConfiguration actual = (StandardShardingStrategyConfiguration) shardingStrategyConfigurationYamlSwapper.swap(createStandardShardingStrategyConfiguration(false));
        assertThat(actual.getShardingColumn(), is("id"));
        assertThat(actual.getPreciseShardingAlgorithm(), instanceOf(PreciseShardingAlgorithmFixture.class));
        assertNull(actual.getRangeShardingAlgorithm());
    }
    
    private YamlShardingStrategyConfiguration createStandardShardingStrategyConfiguration(final boolean withRangeAlgorithmClassName) {
        YamlStandardShardingStrategyConfiguration yamlStandardShardingStrategyConfiguration = new YamlStandardShardingStrategyConfiguration();
        yamlStandardShardingStrategyConfiguration.setShardingColumn("id");
        yamlStandardShardingStrategyConfiguration.setPreciseAlgorithmClassName(PreciseShardingAlgorithmFixture.class.getName());
        if (withRangeAlgorithmClassName) {
            yamlStandardShardingStrategyConfiguration.setRangeAlgorithmClassName(RangeShardingAlgorithmFixture.class.getName());
        }
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        result.setStandard(yamlStandardShardingStrategyConfiguration);
        return result;
    }
    
    @Test
    public void assertSwapToObjectWithInline() {
        InlineShardingStrategyConfiguration actual = (InlineShardingStrategyConfiguration) shardingStrategyConfigurationYamlSwapper.swap(createInlineShardingStrategyConfiguration());
        assertThat(actual.getShardingColumn(), is("id"));
        assertThat(actual.getAlgorithmExpression(), is("xxx_$->{id % 10}"));
    }
    
    private YamlShardingStrategyConfiguration createInlineShardingStrategyConfiguration() {
        YamlInlineShardingStrategyConfiguration yamlInlineShardingStrategyConfiguration = new YamlInlineShardingStrategyConfiguration();
        yamlInlineShardingStrategyConfiguration.setShardingColumn("id");
        yamlInlineShardingStrategyConfiguration.setAlgorithmExpression("xxx_$->{id % 10}");
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        result.setInline(yamlInlineShardingStrategyConfiguration);
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
        yamlComplexShardingStrategyConfiguration.setAlgorithmClassName(ComplexKeysShardingAlgorithmFixture.class.getName());
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
        yamlHintShardingStrategyConfiguration.setAlgorithmClassName(HintShardingAlgorithmFixture.class.getName());
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
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSwapToObjectWithMultipleSHardingStrategies() {
        YamlShardingStrategyConfiguration actual = new YamlShardingStrategyConfiguration();
        YamlInlineShardingStrategyConfiguration inlineShardingStrategyConfig = new YamlInlineShardingStrategyConfiguration();
        inlineShardingStrategyConfig.setShardingColumn("order_id");
        inlineShardingStrategyConfig.setAlgorithmExpression("t_order_${order_id % 2}");
        actual.setInline(inlineShardingStrategyConfig);
        actual.setNone(new YamlNoneShardingStrategyConfiguration());
        shardingStrategyConfigurationYamlSwapper.swap(actual);
    }
}
