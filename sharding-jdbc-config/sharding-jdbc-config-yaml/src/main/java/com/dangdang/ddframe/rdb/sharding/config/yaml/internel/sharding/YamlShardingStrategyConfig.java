package com.dangdang.ddframe.rdb.sharding.config.yaml.internel.sharding;

import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ComplexShardingStrategyConfiguration;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.HintShardingStrategyConfiguration;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.InlineShardingStrategyConfiguration;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.NoneShardingStrategyConfiguration;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ShardingStrategyConfiguration;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.StandardShardingStrategyConfiguration;
import com.google.common.base.Preconditions;
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
