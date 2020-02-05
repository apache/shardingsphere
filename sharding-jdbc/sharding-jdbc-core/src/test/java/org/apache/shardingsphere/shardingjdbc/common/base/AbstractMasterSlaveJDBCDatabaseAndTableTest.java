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

package org.apache.shardingsphere.shardingjdbc.common.base;

import org.apache.shardingsphere.api.config.masterslave.LoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.shardingjdbc.fixture.TestDataSource;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractMasterSlaveJDBCDatabaseAndTableTest extends AbstractSQLTest {
    
    private static DataSource masterDataSource;
    
    private static DataSource slaveDataSource;
    
    private static MasterSlaveDataSource masterSlaveDataSource;
    
    @BeforeClass
    public static void initMasterSlaveDataSources() throws SQLException {
        if (null != masterSlaveDataSource) {
            return;
        }
        masterDataSource = new TestDataSource("test_ds_master");
        slaveDataSource = new TestDataSource("test_ds_slave");
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("test_ds_master", masterDataSource);
        dataSourceMap.put("test_ds_slave", slaveDataSource);
        MasterSlaveRule masterSlaveRule = new MasterSlaveRule(
                new MasterSlaveRuleConfiguration("test_ds", "test_ds_master", Collections.singletonList("test_ds_slave"), new LoadBalanceStrategyConfiguration("ROUND_ROBIN")));
        masterSlaveDataSource = new MasterSlaveDataSource(dataSourceMap, masterSlaveRule, new Properties());
    }
    
    protected final MasterSlaveDataSource getMasterSlaveDataSource() {
        return masterSlaveDataSource;
    }
    
    @AfterClass
    public static void clear() throws Exception {
        if (null == masterSlaveDataSource) {
            return;
        }
        masterSlaveDataSource.close();
        masterSlaveDataSource = null;
    }
}
