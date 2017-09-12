package com.dangdang.ddframe.rdb.sharding.config.yaml.internel;

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
    
    private ComplexShardingStrategyConfig complexShardingStrategy;
    
    private HintShardingStrategyConfig hintShardingStrategy;
    
    private InlineShardingStrategyConfig inlineShardingStrategy;
    
    private NoneShardingStrategyConfig noneShardingStrategyConfig;
    
    private StandardShardingStrategyConfig standardShardingStrategy;
    
    public ShardingStrategyConfig getShardingStrategy() {
        int shardingStrategyConfigCount = 0;
        ShardingStrategyConfig shardingStrategy = null;
        if (null != complexShardingStrategy) {
            shardingStrategyConfigCount++;
            shardingStrategy = complexShardingStrategy;
        }
        if (null != inlineShardingStrategy) {
            shardingStrategyConfigCount++;
            shardingStrategy = inlineShardingStrategy;
        }
        if (null != hintShardingStrategy) {
            shardingStrategyConfigCount++;
            shardingStrategy = hintShardingStrategy;
        }
        if (null != standardShardingStrategy) {
            shardingStrategyConfigCount++;
            shardingStrategy = standardShardingStrategy;
        }
        if (null != noneShardingStrategyConfig) {
            shardingStrategyConfigCount++;
            shardingStrategy = noneShardingStrategyConfig;
        }
        Preconditions.checkArgument(shardingStrategyConfigCount == 1, "Must only have one sharding strategy.");
        return shardingStrategy;
    }
}
