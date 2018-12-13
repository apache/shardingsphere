package io.shardingsphere.core.yaml.sharding;

import io.shardingsphere.api.algorithm.sharding.ShardingValue;
import io.shardingsphere.api.algorithm.sharding.hint.HintShardingAlgorithm;

import java.util.Collection;


public class HintAlgorithm implements HintShardingAlgorithm {


    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final ShardingValue shardingValue) {
        return availableTargetNames;
    }

}
