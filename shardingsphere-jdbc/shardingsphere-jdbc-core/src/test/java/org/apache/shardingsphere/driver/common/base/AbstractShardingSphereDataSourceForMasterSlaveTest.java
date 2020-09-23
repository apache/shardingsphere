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

package org.apache.shardingsphere.driver.common.base;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class AbstractShardingSphereDataSourceForMasterSlaveTest extends AbstractSQLTest {
    
    private static ShardingSphereDataSource masterSlaveDataSource;
    
    private static final String CONFIG_MASTER_SLAVE = "config-master-slave.yaml";
    
    private static final List<String> MASTER_SLAVE_DB_NAMES = Arrays.asList("test_primary_ds", "test_replica_ds");
    
    @BeforeClass
    public static void initMasterSlaveDataSources() throws SQLException, IOException {
        if (null != masterSlaveDataSource) {
            return;
        }
        masterSlaveDataSource = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(getDataSources(), getFile(CONFIG_MASTER_SLAVE));
    }
    
    private static Map<String, DataSource> getDataSources() {
        return Maps.filterKeys(getDATABASE_TYPE_MAP().values().iterator().next(), MASTER_SLAVE_DB_NAMES::contains);
    }
    
    private static File getFile(final String fileName) {
        return new File(Preconditions.checkNotNull(
                AbstractShardingSphereDataSourceForMasterSlaveTest.class.getClassLoader().getResource(fileName), "file resource `%s` must not be null.", fileName).getFile());
    }
    
    protected final ShardingSphereDataSource getMasterSlaveDataSource() {
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
