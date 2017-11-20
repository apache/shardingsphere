package io.shardingjdbc.orchestration.reg.stub;

import io.shardingjdbc.core.api.algorithm.sharding.PreciseShardingValue;
import io.shardingjdbc.core.api.algorithm.sharding.standard.PreciseShardingAlgorithm;

import java.util.Collection;

public class ModuloTableShardingAlgorithm implements PreciseShardingAlgorithm<Integer> {
    @Override
    public String doSharding(final Collection<String> tableNames, final PreciseShardingValue<Integer> shardingValue) {
        for (String each : tableNames) {
            if (each.endsWith(shardingValue.getValue() % 2 + "")) {
                return each;
            }
        }
        throw new UnsupportedOperationException();
    }
}
