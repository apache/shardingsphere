package io.shardingjdbc.core.yaml.sharding;

import com.google.common.base.Preconditions;
import io.shardingjdbc.core.api.config.strategy.ComplexShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.HintShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.ShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.StandardShardingStrategyConfiguration;
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
            result = new StandardShardingStrategyConfiguration(standard.getShardingColumn(), standard.getPreciseAlgorithmClassName(), standard.getRangeAlgorithmClassName());
        }
        if (null != complex) {
            shardingStrategyConfigCount++;
            result = new ComplexShardingStrategyConfiguration(complex.getShardingColumns(), complex.getAlgorithmClassName());
        }
        if (null != inline) {
            shardingStrategyConfigCount++;
            result = new InlineShardingStrategyConfiguration(inline.getShardingColumn(), inline.getAlgorithmExpression());
        }
        if (null != hint) {
            shardingStrategyConfigCount++;
            result = new HintShardingStrategyConfiguration(hint.getAlgorithmClassName());
        }
        if (null != none) {
            shardingStrategyConfigCount++;
            result = new NoneShardingStrategyConfiguration();
        }
        Preconditions.checkArgument(shardingStrategyConfigCount <= 1, "Only allowed 0 or 1 sharding strategy configuration.");
        return result;
    }
}
