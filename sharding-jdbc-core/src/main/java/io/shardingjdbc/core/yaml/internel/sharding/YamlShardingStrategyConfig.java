package io.shardingjdbc.core.yaml.internel.sharding;

import com.google.common.base.Preconditions;
import io.shardingjdbc.core.api.config.strategy.ComplexShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.HintShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.ShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.StandardShardingStrategyConfiguration;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for yaml sharding strategy.
 *
 * @author caohao
 */
@Getter
@Setter
public class YamlShardingStrategyConfig {
    
    private ComplexShardingStrategyConfiguration complex;
    
    private HintShardingStrategyConfiguration hint;
    
    private InlineShardingStrategyConfiguration inline;
    
    private NoneShardingStrategyConfiguration none;
    
    private StandardShardingStrategyConfiguration standard;
    
    public ShardingStrategyConfiguration getShardingStrategy() {
        int shardingStrategyConfigCount = 0;
        ShardingStrategyConfiguration shardingStrategy = null;
        if (null != complex) {
            shardingStrategyConfigCount++;
            shardingStrategy = complex;
        }
        if (null != inline) {
            shardingStrategyConfigCount++;
            shardingStrategy = inline;
        }
        if (null != hint) {
            shardingStrategyConfigCount++;
            shardingStrategy = hint;
        }
        if (null != standard) {
            shardingStrategyConfigCount++;
            shardingStrategy = standard;
        }
        if (null != none) {
            shardingStrategyConfigCount++;
            shardingStrategy = none;
        }
        Preconditions.checkArgument(shardingStrategyConfigCount <= 1, "Only allowed 0 or 1 sharding strategy configuration.");
        return shardingStrategy;
    }
}
