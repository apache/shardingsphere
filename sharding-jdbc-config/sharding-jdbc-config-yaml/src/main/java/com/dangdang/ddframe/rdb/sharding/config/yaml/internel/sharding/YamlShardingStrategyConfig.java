package com.dangdang.ddframe.rdb.sharding.config.yaml.internel.sharding;

import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ComplexShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.HintShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.InlineShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.NoneShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.StandardShardingStrategyConfig;
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
    
    private ComplexShardingStrategyConfig complex;
    
    private HintShardingStrategyConfig hint;
    
    private InlineShardingStrategyConfig inline;
    
    private NoneShardingStrategyConfig none;
    
    private StandardShardingStrategyConfig standard;
    
    public ShardingStrategyConfig getShardingStrategy() {
        int shardingStrategyConfigCount = 0;
        ShardingStrategyConfig shardingStrategy = null;
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
