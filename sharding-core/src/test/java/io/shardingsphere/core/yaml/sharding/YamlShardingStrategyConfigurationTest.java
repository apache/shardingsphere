/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.yaml.sharding;

import io.shardingsphere.api.config.strategy.ComplexShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.HintShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.core.fixture.ComplexOrderShardingAlgorithm;
import io.shardingsphere.core.fixture.OrderDatabaseHintShardingAlgorithm;
import io.shardingsphere.core.fixture.PreciseOrderShardingAlgorithm;
import io.shardingsphere.core.fixture.RangeOrderShardingAlgorithm;
import io.shardingsphere.core.yaml.sharding.strategy.YamlComplexShardingStrategyConfiguration;
import io.shardingsphere.core.yaml.sharding.strategy.YamlHintShardingStrategyConfiguration;
import io.shardingsphere.core.yaml.sharding.strategy.YamlInlineShardingStrategyConfiguration;
import io.shardingsphere.core.yaml.sharding.strategy.YamlNoneShardingStrategyConfiguration;
import io.shardingsphere.core.yaml.sharding.strategy.YamlStandardShardingStrategyConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class YamlShardingStrategyConfigurationTest {
    
    @Test
    public void assertBuildWithStandard() {
        assertStandardShardingStrategyConfig((StandardShardingStrategyConfiguration) createStandardShardingStrategyConfig().build());
    }
    
    private YamlShardingStrategyConfiguration createStandardShardingStrategyConfig() {
        YamlStandardShardingStrategyConfiguration standardShardingStrategyConfig = new YamlStandardShardingStrategyConfiguration();
        standardShardingStrategyConfig.setShardingColumn("order_id");
        standardShardingStrategyConfig.setPreciseAlgorithmClassName(PreciseOrderShardingAlgorithm.class.getName());
        standardShardingStrategyConfig.setRangeAlgorithmClassName(RangeOrderShardingAlgorithm.class.getName());
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        result.setStandard(standardShardingStrategyConfig);
        return result;
    }
    
    private void assertStandardShardingStrategyConfig(final StandardShardingStrategyConfiguration actual) {

        assertThat(actual.getShardingColumn(), is("order_id"));
        assertThat(actual.getPreciseShardingAlgorithm(), instanceOf(PreciseOrderShardingAlgorithm.class));
        assertThat(actual.getRangeShardingAlgorithm(), instanceOf(RangeOrderShardingAlgorithm.class));
    }
    
    @Test
    public void assertBuildWithComplex() {
        assertComplexShardingStrategyConfig((ComplexShardingStrategyConfiguration) createComplexShardingStrategyConfig().build());
    }
    
    private YamlShardingStrategyConfiguration createComplexShardingStrategyConfig() {
        YamlComplexShardingStrategyConfiguration complexShardingStrategyConfig = new YamlComplexShardingStrategyConfiguration();
        complexShardingStrategyConfig.setShardingColumns("user_id, order_id");
        complexShardingStrategyConfig.setAlgorithmClassName(ComplexOrderShardingAlgorithm.class.getName());
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        result.setComplex(complexShardingStrategyConfig);
        return result;
    }
    
    private void assertComplexShardingStrategyConfig(final ComplexShardingStrategyConfiguration actual) {
        assertThat(actual.getShardingColumns(), is("user_id, order_id"));
        assertThat(actual.getShardingAlgorithm(), instanceOf(ComplexOrderShardingAlgorithm.class));
    }
    
    @Test
    public void assertBuildWithHint() {
        assertHintShardingStrategyConfig((HintShardingStrategyConfiguration) createHintShardingStrategyConfig().build());
    }
    
    private YamlShardingStrategyConfiguration createHintShardingStrategyConfig() {
        YamlHintShardingStrategyConfiguration hintShardingStrategyConfig = new YamlHintShardingStrategyConfiguration();
        hintShardingStrategyConfig.setAlgorithmClassName(OrderDatabaseHintShardingAlgorithm.class.getName());
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        result.setHint(hintShardingStrategyConfig);
        return result;
    }
    
    private void assertHintShardingStrategyConfig(final HintShardingStrategyConfiguration actual) {
        assertThat(actual.getShardingAlgorithm(), instanceOf(OrderDatabaseHintShardingAlgorithm.class));
    }
    
    @Test
    public void assertBuildWithInline() {
        assertInlineShardingStrategyConfig((InlineShardingStrategyConfiguration) createInlineShardingStrategyConfig().build());
    }
    
    private YamlShardingStrategyConfiguration createInlineShardingStrategyConfig() {
        YamlInlineShardingStrategyConfiguration inlineShardingStrategyConfig = new YamlInlineShardingStrategyConfiguration();
        inlineShardingStrategyConfig.setShardingColumn("order_id");
        inlineShardingStrategyConfig.setAlgorithmExpression("t_order_${order_id % 2}");
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        result.setInline(inlineShardingStrategyConfig);
        return result;
    }
    
    private void assertInlineShardingStrategyConfig(final InlineShardingStrategyConfiguration actual) {
        assertThat(actual.getShardingColumn(), is("order_id"));
        assertThat(actual.getAlgorithmExpression(), is("t_order_${order_id % 2}"));
    }
    
    @Test
    public void assertBuildWithNone() {
        assertNoneShardingStrategyConfig((NoneShardingStrategyConfiguration) createNoneShardingStrategyConfig().build());
    }
    
    private YamlShardingStrategyConfiguration createNoneShardingStrategyConfig() {
        YamlNoneShardingStrategyConfiguration noneShardingStrategyConfig = new YamlNoneShardingStrategyConfiguration();
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        result.setNone(noneShardingStrategyConfig);
        return result;
    }
    
    private void assertNoneShardingStrategyConfig(final NoneShardingStrategyConfiguration actual) {
        assertThat(actual, instanceOf(NoneShardingStrategyConfiguration.class));
    }
    
    @Test
    public void assertBuildWithNull() {
        assertNull(new YamlShardingStrategyConfiguration().build());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithMultipleSHardingStrategies() {
        YamlShardingStrategyConfiguration actual = new YamlShardingStrategyConfiguration();
        YamlInlineShardingStrategyConfiguration inlineShardingStrategyConfig = new YamlInlineShardingStrategyConfiguration();
        inlineShardingStrategyConfig.setShardingColumn("order_id");
        inlineShardingStrategyConfig.setAlgorithmExpression("t_order_${order_id % 2}");
        actual.setInline(inlineShardingStrategyConfig);
        actual.setNone(new YamlNoneShardingStrategyConfiguration());
        actual.build();
    }
}
