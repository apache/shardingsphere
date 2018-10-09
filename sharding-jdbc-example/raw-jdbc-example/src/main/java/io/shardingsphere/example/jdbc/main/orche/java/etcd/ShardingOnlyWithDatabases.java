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

import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.api.config.TableRuleConfiguration;
import io.shardingsphere.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingsphere.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.orchestration.config.OrchestrationType;
import io.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import io.shardingsphere.orchestration.reg.etcd.EtcdConfiguration;
import io.shardingsphere.shardingjdbc.orchestration.api.OrchestrationShardingDataSourceFactory;
import io.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationShardingDataSource;
import io.shardingsphere.example.repository.jdbc.JDBCRepository;
import io.shardingsphere.example.jdbc.util.DataSourceUtil;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ShardingOnlyWithDatabases {
    
    private static final String ETCD_CONNECTION_STRING = "http://localhost:2379";
    
    private static final boolean LOAD_CONFIG_FROM_REG_CENTER = false;
    
    public static void main(final String[] args) throws SQLException {
        DataSource dataSource = getDataSource();
        new JDBCRepository(dataSource).demo();
        ((OrchestrationShardingDataSource) dataSource).close();
    }
    
    private static DataSource getDataSource() throws SQLException {
        return LOAD_CONFIG_FROM_REG_CENTER ? getDataSourceFromRegCenter() : getDataSourceFromLocalConfiguration();
    }
    
    private static DataSource getDataSourceFromRegCenter() throws SQLException {
        return OrchestrationShardingDataSourceFactory.createDataSource(
                new OrchestrationConfiguration("orchestration-sharding-db-data-source", getRegistryCenterConfiguration(), false, OrchestrationType.SHARDING));
    }
    
    private static DataSource getDataSourceFromLocalConfiguration() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(getOrderTableRuleConfiguration());
        shardingRuleConfig.getTableRuleConfigs().add(getOrderItemTableRuleConfiguration());
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "demo_ds_${user_id % 2}"));
        OrchestrationConfiguration orchestrationConfig = new OrchestrationConfiguration(
                "orchestration-sharding-db-data-source", getRegistryCenterConfiguration(), true, OrchestrationType.SHARDING);
        return OrchestrationShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig, new HashMap<String, Object>(), new Properties(), orchestrationConfig);
    }
    
    private static TableRuleConfiguration getOrderTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("t_order");
        result.setKeyGeneratorColumnName("order_id");
        return result;
    }
    
    private static TableRuleConfiguration getOrderItemTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("t_order_item");
        return result;
    }
    
    private static Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>();
        result.put("demo_ds_0", DataSourceUtil.createDataSource("demo_ds_0"));
        result.put("demo_ds_1", DataSourceUtil.createDataSource("demo_ds_1"));
        return result;
    }
    
    private static RegistryCenterConfiguration getRegistryCenterConfiguration() {
        EtcdConfiguration result = new EtcdConfiguration();
        result.setServerLists(ETCD_CONNECTION_STRING);
        return result;
    }
}
