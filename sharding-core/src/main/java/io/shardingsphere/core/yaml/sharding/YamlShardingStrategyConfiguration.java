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

import com.google.common.base.Preconditions;
import io.shardingsphere.api.algorithm.sharding.complex.ComplexKeysShardingAlgorithm;
import io.shardingsphere.api.algorithm.sharding.hint.HintShardingAlgorithm;
import io.shardingsphere.api.algorithm.sharding.standard.PreciseShardingAlgorithm;
import io.shardingsphere.api.algorithm.sharding.standard.RangeShardingAlgorithm;
import io.shardingsphere.api.config.strategy.ComplexShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.HintShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.ShardingStrategyConfiguration;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.core.routing.strategy.ShardingAlgorithmFactory;
import io.shardingsphere.core.yaml.sharding.strategy.YamlComplexShardingStrategyConfiguration;
import io.shardingsphere.core.yaml.sharding.strategy.YamlHintShardingStrategyConfiguration;
import io.shardingsphere.core.yaml.sharding.strategy.YamlInlineShardingStrategyConfiguration;
import io.shardingsphere.core.yaml.sharding.strategy.YamlNoneShardingStrategyConfiguration;
import io.shardingsphere.core.yaml.sharding.strategy.YamlStandardShardingStrategyConfiguration;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuration for yaml sharding strategy.
 *
 * @author caohao
 * @author panjuan
 */
@NoArgsConstructor
@Getter
@Setter
public class YamlShardingStrategyConfiguration {
    
    private YamlStandardShardingStrategyConfiguration standard;
    
    private YamlComplexShardingStrategyConfiguration complex;
    
    private YamlHintShardingStrategyConfiguration hint;
    
    private YamlInlineShardingStrategyConfiguration inline;
    
    private YamlNoneShardingStrategyConfiguration none;
    
    public YamlShardingStrategyConfiguration(final ShardingStrategyConfiguration shardingStrategyConfiguration) {
        if (shardingStrategyConfiguration instanceof StandardShardingStrategyConfiguration) {
            standard = new YamlStandardShardingStrategyConfiguration();
            StandardShardingStrategyConfiguration config = (StandardShardingStrategyConfiguration) shardingStrategyConfiguration;
            standard.setShardingColumn(config.getShardingColumn());
            standard.setPreciseAlgorithmClassName(config.getPreciseShardingAlgorithm().getClass().getName());
            standard.setRangeAlgorithmClassName(null == config.getRangeShardingAlgorithm()
                    ? null : config.getRangeShardingAlgorithm().getClass().getName());
        }
        if (shardingStrategyConfiguration instanceof ComplexShardingStrategyConfiguration) {
            complex = new YamlComplexShardingStrategyConfiguration();
            ComplexShardingStrategyConfiguration config = (ComplexShardingStrategyConfiguration) shardingStrategyConfiguration;
            complex.setShardingColumns(config.getShardingColumns());
            complex.setAlgorithmClassName(config.getShardingAlgorithm().getClass().getName());
        }
        if (shardingStrategyConfiguration instanceof HintShardingStrategyConfiguration) {
            hint = new YamlHintShardingStrategyConfiguration();
            hint.setAlgorithmClassName(((HintShardingStrategyConfiguration) shardingStrategyConfiguration).getShardingAlgorithm().getClass().getName());
        }
        if (shardingStrategyConfiguration instanceof InlineShardingStrategyConfiguration) {
            inline = new YamlInlineShardingStrategyConfiguration();
            InlineShardingStrategyConfiguration config = (InlineShardingStrategyConfiguration) shardingStrategyConfiguration;
            inline.setShardingColumn(config.getShardingColumn());
            inline.setAlgorithmExpression(config.getAlgorithmExpression());
        }
    }
    
    /**
     * Build sharding strategy configuration.
     * 
     * @return sharding strategy configuration
     */
    public ShardingStrategyConfiguration build() {
        int shardingStrategyConfigCount = 0;
        ShardingStrategyConfiguration result = null;
        if (null != standard) {
            shardingStrategyConfigCount++;
            if (null == standard.getRangeAlgorithmClassName()) {
                result = new StandardShardingStrategyConfiguration(standard.getShardingColumn(),
                        ShardingAlgorithmFactory.newInstance(standard.getPreciseAlgorithmClassName(), PreciseShardingAlgorithm.class));
            } else {
                result = new StandardShardingStrategyConfiguration(standard.getShardingColumn(),
                        ShardingAlgorithmFactory.newInstance(standard.getPreciseAlgorithmClassName(), PreciseShardingAlgorithm.class),
                        ShardingAlgorithmFactory.newInstance(standard.getRangeAlgorithmClassName(), RangeShardingAlgorithm.class));
            }
            
        }
        if (null != complex) {
            shardingStrategyConfigCount++;
            result = new ComplexShardingStrategyConfiguration(complex.getShardingColumns(), ShardingAlgorithmFactory.newInstance(complex.getAlgorithmClassName(), ComplexKeysShardingAlgorithm.class));
        }
        if (null != inline) {
            shardingStrategyConfigCount++;
            result = new InlineShardingStrategyConfiguration(inline.getShardingColumn(), inline.getAlgorithmExpression());
        }
        if (null != hint) {
            shardingStrategyConfigCount++;
            result = new HintShardingStrategyConfiguration(ShardingAlgorithmFactory.newInstance(hint.getAlgorithmClassName(), HintShardingAlgorithm.class));
        }
        if (null != none) {
            shardingStrategyConfigCount++;
            result = new NoneShardingStrategyConfiguration();
        }
        Preconditions.checkArgument(shardingStrategyConfigCount <= 1, "Only allowed 0 or 1 sharding strategy configuration.");
        return result;
    }
}
