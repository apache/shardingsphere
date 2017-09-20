package io.shardingjdbc.core.api.algorithm.masterslave;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public final class RandomMasterSlaveLoadBalanceAlgorithmTest {
    
    private final RandomMasterSlaveLoadBalanceAlgorithm randomMasterSlaveLoadBalanceAlgorithm = new RandomMasterSlaveLoadBalanceAlgorithm();
    
    @Test
    public void assertGetDataSource() {
        String masterDataSourceName = "test_ds_master";
        String slaveDataSourceName1 = "test_ds_slave_1";
        String slaveDataSourceName2 = "test_ds_slave_2";
        List<String> slaveDataSourceNames = Arrays.asList(slaveDataSourceName1, slaveDataSourceName2);
        assertTrue(slaveDataSourceNames.contains(randomMasterSlaveLoadBalanceAlgorithm.getDataSource("ds", masterDataSourceName, slaveDataSourceNames)));
        assertTrue(slaveDataSourceNames.contains(randomMasterSlaveLoadBalanceAlgorithm.getDataSource("ds", masterDataSourceName, slaveDataSourceNames)));
        assertTrue(slaveDataSourceNames.contains(randomMasterSlaveLoadBalanceAlgorithm.getDataSource("ds", masterDataSourceName, slaveDataSourceNames)));
    }
}
