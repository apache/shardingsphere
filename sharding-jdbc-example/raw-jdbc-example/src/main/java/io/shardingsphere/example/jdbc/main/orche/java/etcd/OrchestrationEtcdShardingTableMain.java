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

import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.api.config.TableRuleConfiguration;
import io.shardingsphere.example.jdbc.fixture.DataRepository;
import io.shardingsphere.example.jdbc.fixture.DataSourceUtil;
import io.shardingsphere.jdbc.orchestration.api.OrchestrationShardingDataSourceFactory;
import io.shardingsphere.jdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingsphere.jdbc.orchestration.api.config.OrchestrationType;
import io.shardingsphere.jdbc.orchestration.reg.api.RegistryCenterConfiguration;
import io.shardingsphere.jdbc.orchestration.reg.etcd.EtcdConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class OrchestrationEtcdShardingTableMain {
    
    private static final String ETCD_CONNECTION_STRING = "http://localhost:2379";
    
    public static void main(final String[] args) throws SQLException {
        //        new DataRepository(getDataSourceByCloudConfig()).demo();
        DataSource dataSource = getDataSourceByLocalConfig();
        new DataRepository(dataSource).demo();
        OrchestrationShardingDataSourceFactory.closeQuietly(dataSource);
    }
    
    private static DataSource getDataSourceByLocalConfig() throws SQLException {
        return OrchestrationShardingDataSourceFactory.createDataSource(
                createDataSourceMap(), createShardingRuleConfig(), new ConcurrentHashMap<String, Object>(), new Properties(), new OrchestrationConfiguration("orchestration-sharding-data-source", getRegistryCenterConfiguration(), true, OrchestrationType.SHARDING));
    }
    
    private static DataSource getDataSourceByCloudConfig() throws SQLException {
        return OrchestrationShardingDataSourceFactory.createDataSource(
                null, null, null, null, new OrchestrationConfiguration("orchestration-sharding-data-source", getRegistryCenterConfiguration(), false, OrchestrationType.SHARDING));
    }
    
    private static RegistryCenterConfiguration getRegistryCenterConfiguration() {
        EtcdConfiguration result = new EtcdConfiguration();
        result.setServerLists(ETCD_CONNECTION_STRING);
        return result;
    }
    
    private static Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(1, 1);
        result.put("demo_ds", DataSourceUtil.createDataSource("demo_ds"));
        return result;
    }
    
    private static ShardingRuleConfiguration createShardingRuleConfig() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
        orderTableRuleConfig.setLogicTable("t_order");
        orderTableRuleConfig.setActualDataNodes("demo_ds.t_order_${0..1}");
        orderTableRuleConfig.setKeyGeneratorColumnName("order_id");
        result.getTableRuleConfigs().add(orderTableRuleConfig);
        TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
        orderItemTableRuleConfig.setLogicTable("t_order_item");
        orderItemTableRuleConfig.setActualDataNodes("demo_ds.t_order_item_${0..1}");
        result.getTableRuleConfigs().add(orderItemTableRuleConfig);
        result.getBindingTableGroups().add("t_order, t_order_item");
        return result;
    }
}
