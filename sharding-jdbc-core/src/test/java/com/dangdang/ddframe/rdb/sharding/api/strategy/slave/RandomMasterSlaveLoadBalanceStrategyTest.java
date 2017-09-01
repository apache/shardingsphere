package com.dangdang.ddframe.rdb.sharding.api.strategy.slave;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public final class RandomMasterSlaveLoadBalanceStrategyTest {
    
    private final RandomMasterSlaveLoadBalanceStrategy randomMasterSlaveLoadBalanceStrategy = new RandomMasterSlaveLoadBalanceStrategy();
    
    @Test
    public void assertGetDataSource() {
        String masterDataSourceName = "test_ds_master";
        String slaveDataSourceName1 = "test_ds_slave_1";
        String slaveDataSourceName2 = "test_ds_slave_2";
        List<String> slaveDataSourceNames = Arrays.asList(slaveDataSourceName1, slaveDataSourceName2);
        assertTrue(slaveDataSourceNames.contains(randomMasterSlaveLoadBalanceStrategy.getDataSource("ds", masterDataSourceName, slaveDataSourceNames)));
        assertTrue(slaveDataSourceNames.contains(randomMasterSlaveLoadBalanceStrategy.getDataSource("ds", masterDataSourceName, slaveDataSourceNames)));
        assertTrue(slaveDataSourceNames.contains(randomMasterSlaveLoadBalanceStrategy.getDataSource("ds", masterDataSourceName, slaveDataSourceNames)));
    }
}
