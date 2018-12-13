package io.shardingsphere.shardingjdbc.yaml.userAlgo;


import io.shardingsphere.api.algorithm.sharding.PreciseShardingValue;
import io.shardingsphere.api.algorithm.sharding.standard.PreciseShardingAlgorithm;

import java.util.Collection;


public class DatabaseAlgorithm implements PreciseShardingAlgorithm<String> {


    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
        int value = Integer.parseInt(shardingValue.getValue(),16);
        int databaseSum = 10;
        int hash = HashUtil.consistentHash(value, databaseSum);
        String dataBaseName = "safecuit_data_";
        return dataBaseName + hash;
    }

}
