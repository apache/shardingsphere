/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.api;

import io.shardingsphere.api.ConfigMapContext;
import io.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.shardingjdbc.fixture.TestDataSource;
import io.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MasterSlaveDataSourceFactoryTest {
    
    @Before
    public void setUp() {
        ConfigMapContext.getInstance().getConfigMap().clear();
    }
    
    @Test
    public void assertCreateDataSourceForSingleSlave() throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("master_ds", new TestDataSource("master_ds"));
        dataSourceMap.put("slave_ds", new TestDataSource("slave_ds"));
        Map<String, Object> configMap = new ConcurrentHashMap<>();
        Properties properties = new Properties();
        properties.setProperty("sql.show", "true");
        configMap.put("key1", "value1");
        assertThat(MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, 
                new MasterSlaveRuleConfiguration("logic_ds", "master_ds", Collections.singletonList("slave_ds"), MasterSlaveLoadBalanceAlgorithmType.ROUND_ROBIN.getAlgorithm()), 
                configMap, properties), instanceOf(MasterSlaveDataSource.class));
        MatcherAssert.assertThat(ConfigMapContext.getInstance().getConfigMap(), is(configMap));
    }
    
    @Test
    public void assertCreateDataSourceForMultipleSlaves() throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(3, 1);
        dataSourceMap.put("master_ds", new TestDataSource("master_ds"));
        dataSourceMap.put("slave_ds_0", new TestDataSource("slave_ds_0"));
        dataSourceMap.put("slave_ds_1", new TestDataSource("slave_ds_1"));
        Properties properties = new Properties();
        properties.setProperty("sql.show", "true");
        Map<String, Object> configMap = new ConcurrentHashMap<>();
        configMap.put("key1", "value1");
        assertThat(MasterSlaveDataSourceFactory.createDataSource(
                dataSourceMap, new MasterSlaveRuleConfiguration("logic_ds", "master_ds", Arrays.asList("slave_ds_0", "slave_ds_1"), MasterSlaveLoadBalanceAlgorithmType.ROUND_ROBIN.getAlgorithm()),
                configMap, properties), instanceOf(MasterSlaveDataSource.class));
        MatcherAssert.assertThat(ConfigMapContext.getInstance().getConfigMap(), is(configMap));
    }
}
