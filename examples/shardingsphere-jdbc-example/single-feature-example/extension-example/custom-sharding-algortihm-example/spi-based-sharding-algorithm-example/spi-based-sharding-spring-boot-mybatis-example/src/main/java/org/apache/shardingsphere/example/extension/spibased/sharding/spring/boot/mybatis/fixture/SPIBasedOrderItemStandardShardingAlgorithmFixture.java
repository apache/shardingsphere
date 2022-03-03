package org.apache.shardingsphere.example.extension.spibased.sharding.spring.boot.mybatis.fixture;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;

/**
 * @author susongyan
 **/
public class SPIBasedOrderItemStandardShardingAlgorithmFixture implements StandardShardingAlgorithm<Long> {

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        for (String each : availableTargetNames) {
            if (each.endsWith(shardingSuffix(shardingValue.getValue()))) {
                return each;
            }
        }
        return null;
    }

    private String shardingSuffix(Long shardingValue) {
        return "_" + (shardingValue % 2);
    }

    @Override
    public Collection<String> doSharding
            (Collection<String> availableTargetNames, RangeShardingValue<Long> shardingValue) {
        return availableTargetNames;
    }

    @Override
    public void init() {

    }

    @Override
    public String getType() {
        return "T_ORDER_ITEM_SPI_BASED";
    }
}
