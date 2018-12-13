package io.shardingsphere.shardingjdbc.yaml.userAlgo;

import io.shardingsphere.api.algorithm.sharding.ShardingValue;
import io.shardingsphere.api.algorithm.sharding.hint.HintShardingAlgorithm;

import java.util.Collection;
import java.util.Collections;


public class HintAlgorithm implements HintShardingAlgorithm {


    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final ShardingValue shardingValue) {
        for (String each : availableTargetNames) {
            if (each.endsWith("1")) {
                return Collections.singletonList(each);
            }
        }
        return availableTargetNames;
    }
}
