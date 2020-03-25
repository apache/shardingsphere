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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.shardingjdbc.api.yaml.YamlMasterSlaveDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.fixture.TestDataSource;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMasterSlaveJDBCDatabaseAndTableTest extends AbstractSQLTest {
    
    private static MasterSlaveDataSource masterSlaveDataSource;
    
    private static final String CONFIG_MASTER_SLAVE = "config-master-slave.yaml";
    
    @BeforeClass
    public static void initMasterSlaveDataSources() throws SQLException, IOException {
        if (null != masterSlaveDataSource) {
            return;
        }
        DataSource masterDataSource = new TestDataSource("test_ds_master");
        DataSource slaveDataSource = new TestDataSource("test_ds_slave");
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("test_ds_master", masterDataSource);
        dataSourceMap.put("test_ds_slave", slaveDataSource);
        masterSlaveDataSource = (MasterSlaveDataSource) YamlMasterSlaveDataSourceFactory.createDataSource(dataSourceMap, getFile(CONFIG_MASTER_SLAVE));
    }
    
    private static File getFile(final String fileName) {
        return new File(Preconditions.checkNotNull(
                AbstractMasterSlaveJDBCDatabaseAndTableTest.class.getClassLoader().getResource(fileName), "file resource `%s` must not be null.", fileName).getFile());
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
