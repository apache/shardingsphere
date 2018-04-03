/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.yaml.sharding;

import com.google.common.base.Preconditions;
import io.shardingjdbc.core.api.algorithm.sharding.complex.ComplexKeysShardingAlgorithm;
import io.shardingjdbc.core.api.algorithm.sharding.hint.HintShardingAlgorithm;
import io.shardingjdbc.core.api.algorithm.sharding.standard.PreciseShardingAlgorithm;
import io.shardingjdbc.core.api.algorithm.sharding.standard.RangeShardingAlgorithm;
import io.shardingjdbc.core.api.config.strategy.ComplexShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.HintShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.ShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingjdbc.core.routing.strategy.ShardingAlgorithmFactory;
import io.shardingjdbc.core.yaml.sharding.strategy.YamlComplexShardingStrategyConfiguration;
import io.shardingjdbc.core.yaml.sharding.strategy.YamlHintShardingStrategyConfiguration;
import io.shardingjdbc.core.yaml.sharding.strategy.YamlInlineShardingStrategyConfiguration;
import io.shardingjdbc.core.yaml.sharding.strategy.YamlNoneShardingStrategyConfiguration;
import io.shardingjdbc.core.yaml.sharding.strategy.YamlStandardShardingStrategyConfiguration;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for yaml sharding strategy.
 *
 * @author caohao
 */
@Getter
@Setter
public class YamlShardingStrategyConfiguration {
    
    private YamlStandardShardingStrategyConfiguration standard;
    
    private YamlComplexShardingStrategyConfiguration complex;
    
    private YamlHintShardingStrategyConfiguration hint;
    
    private YamlInlineShardingStrategyConfiguration inline;
    
    private YamlNoneShardingStrategyConfiguration none;
    
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
