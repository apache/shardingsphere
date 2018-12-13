package io.shardingsphere.shardingjdbc.yaml.userAlgo;

import io.shardingsphere.api.algorithm.sharding.PreciseShardingValue;
import io.shardingsphere.api.algorithm.sharding.standard.PreciseShardingAlgorithm;

import java.util.Collection;
import java.util.Date;

public class TableTemperatureAlgorithm implements PreciseShardingAlgorithm<Date> {

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Date> shardingValue) {
        long value = shardingValue.getValue().getTime();
        int databaseSum = 10;
        int hash = HashUtil.consistentHash(value, databaseSum);
        String tableName = "data_log_temperature_";
        return tableName + hash;
    }
}
