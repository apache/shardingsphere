/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.api;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.fixture.TestDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

public final class MasterSlaveDataSourceFactoryTest {
    
    @Test
    public void assertCreateDataSourceForSingleSlave() throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("master_ds", new TestDataSource("master_ds"));
        dataSourceMap.put("slave_ds", new TestDataSource("slave_ds"));
        MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig.setName("logic_ds");
        masterSlaveRuleConfig.setMasterDataSourceName("master_ds");
        masterSlaveRuleConfig.setSlaveDataSourceNames(Collections.singletonList("slave_ds"));
        assertThat(MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, masterSlaveRuleConfig), instanceOf(MasterSlaveDataSource.class));
    }
    
    @Test
    public void assertCreateDataSourceForMultipleSlaves() throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(3, 1);
        dataSourceMap.put("master_ds", new TestDataSource("master_ds"));
        dataSourceMap.put("slave_ds_0", new TestDataSource("slave_ds_0"));
        dataSourceMap.put("slave_ds_1", new TestDataSource("slave_ds_1"));
        MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig.setName("logic_ds");
        masterSlaveRuleConfig.setMasterDataSourceName("master_ds");
        masterSlaveRuleConfig.setSlaveDataSourceNames(Arrays.asList("slave_ds_0", "slave_ds_1"));
        assertThat(MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, masterSlaveRuleConfig), instanceOf(MasterSlaveDataSource.class));
    }
}
