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

package io.shardingsphere.example.jdbc.main.orche.java.etcd;

import io.shardingsphere.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.example.jdbc.fixture.DataRepository;
import io.shardingsphere.example.jdbc.fixture.DataSourceUtil;
import io.shardingsphere.jdbc.orchestration.api.OrchestrationMasterSlaveDataSourceFactory;
import io.shardingsphere.jdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingsphere.jdbc.orchestration.api.config.OrchestrationType;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationMasterSlaveDataSource;
import io.shardingsphere.jdbc.orchestration.reg.api.RegistryCenterConfiguration;
import io.shardingsphere.jdbc.orchestration.reg.etcd.EtcdConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Please make sure master-slave data sync on MySQL is running correctly. Otherwise this example will query empty data from slave.
 */
public class MasterSlaveOnly {
    
    private static final String ETCD_CONNECTION_STRING = "http://localhost:2379";
    
    private static final boolean LOAD_CONFIG_FROM_REG_CENTER = false;
    
    public static void main(final String[] args) throws SQLException {
        DataSource dataSource = getDataSource();
        new DataRepository(dataSource).demo();
        ((OrchestrationMasterSlaveDataSource) dataSource).close();
    }
    
    private static DataSource getDataSource() throws SQLException {
        return LOAD_CONFIG_FROM_REG_CENTER ? getDataSourceFromRegCenter() : getDataSourceFromLocalConfiguration();
    }
    
    private static DataSource getDataSourceFromRegCenter() throws SQLException {
        return OrchestrationMasterSlaveDataSourceFactory.createDataSource(
                new OrchestrationConfiguration("orchestration-master-slave-data-source", getRegistryCenterConfiguration(), false, OrchestrationType.MASTER_SLAVE));
    }
    
    private static DataSource getDataSourceFromLocalConfiguration() throws SQLException {
        MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration("demo_ds_master_slave", "demo_ds_master", Arrays.asList("demo_ds_slave_0", "demo_ds_slave_1"));
        return OrchestrationMasterSlaveDataSourceFactory.createDataSource(createDataSourceMap(), masterSlaveRuleConfig, new ConcurrentHashMap<String, Object>(), new Properties(),
                new OrchestrationConfiguration("orchestration-master-slave-data-source", getRegistryCenterConfiguration(), true, OrchestrationType.MASTER_SLAVE));
    }
    
    private static RegistryCenterConfiguration getRegistryCenterConfiguration() {
        EtcdConfiguration result = new EtcdConfiguration();
        result.setServerLists(ETCD_CONNECTION_STRING);
        return result;
    }
    
    private static Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>();
        result.put("demo_ds_master", DataSourceUtil.createDataSource("demo_ds_master"));
        result.put("demo_ds_slave_0", DataSourceUtil.createDataSource("demo_ds_slave_0"));
        result.put("demo_ds_slave_1", DataSourceUtil.createDataSource("demo_ds_slave_1"));
        return result;
    }
}
