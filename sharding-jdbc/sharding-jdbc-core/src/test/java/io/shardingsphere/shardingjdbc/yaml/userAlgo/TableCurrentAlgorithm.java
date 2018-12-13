package io.shardingsphere.shardingjdbc.yaml.userAlgo;

import io.shardingsphere.api.algorithm.sharding.PreciseShardingValue;
import io.shardingsphere.api.algorithm.sharding.standard.PreciseShardingAlgorithm;

import java.util.Collection;
import java.util.Date;


public class TableCurrentAlgorithm implements PreciseShardingAlgorithm<Date> {

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Date> shardingValue) {
        long value = shardingValue.getValue().getTime();
        int databaseSum = 10;
        String tableName = "data_log_current_";
        int hash = HashUtil.consistentHash(value, databaseSum);
        return tableName + hash;
    }
}
