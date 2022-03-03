package org.apache.shardingsphere.example.extension.spibased.sharding.spring.boot.mybatis.fixture;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;

/**
 * @author susongyan
 **/
public class SPIBasedDatasourceStandardShardingAlgorithmFixture implements StandardShardingAlgorithm<Integer> {

    @Override
    public String doSharding(Collection<String> dataSourceNames, PreciseShardingValue<Integer> shardingValue) {
        for (String each : dataSourceNames) {
            if (each.endsWith(shardingSuffix(shardingValue.getValue()))) {
                return each;
            }
        }
        return null;
    }

    private String shardingSuffix(Integer shardingValue) {
        return "-" + (shardingValue % 2);
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Integer> shardingValue) {
        return availableTargetNames;
    }

    @Override
    public void init() {

    }

    @Override
    public String getType() {
        return "DATASOURCE_SPI_BASED";
    }
}
