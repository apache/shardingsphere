/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingjdbc.api;

import org.apache.shardingsphere.api.config.masterslave.LoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.shardingjdbc.fixture.TestDataSource;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class MasterSlaveDataSourceFactoryTest {
    
    @Test
    public void assertCreateDataSourceForSingleSlave() throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("master_ds", new TestDataSource("master_ds"));
        dataSourceMap.put("slave_ds", new TestDataSource("slave_ds"));
        Properties properties = new Properties();
        properties.setProperty("sql.show", "true");
        assertThat(MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, new MasterSlaveRuleConfiguration("logic_ds", "master_ds", Collections.singletonList("slave_ds"),
                new LoadBalanceStrategyConfiguration("ROUND_ROBIN")), properties), instanceOf(MasterSlaveDataSource.class));
    }
    
    @Test
    public void assertCreateDataSourceForMultipleSlaves() throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(3, 1);
        dataSourceMap.put("master_ds", new TestDataSource("master_ds"));
        dataSourceMap.put("slave_ds_0", new TestDataSource("slave_ds_0"));
        dataSourceMap.put("slave_ds_1", new TestDataSource("slave_ds_1"));
        Properties properties = new Properties();
        properties.setProperty("sql.show", "true");
        assertThat(MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, new MasterSlaveRuleConfiguration("logic_ds", "master_ds", Arrays.asList("slave_ds_0", "slave_ds_1"),
                new LoadBalanceStrategyConfiguration("ROUND_ROBIN")), properties), instanceOf(MasterSlaveDataSource.class));
    }
}
