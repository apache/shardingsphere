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

package io.shardingsphere.example.transaction.fixture;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import io.shardingsphere.core.api.ShardingDataSourceFactory;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.api.config.TableRuleConfiguration;
import io.shardingsphere.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.example.transaction.algorithm.ModuloShardingAlgorithm;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ShardingDatasourceUtil {
    
    public static DataSource getShardingDataSource(final DatasourceType type) throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
        orderTableRuleConfig.setLogicTable("t_order");
        orderTableRuleConfig.setActualDataNodes("ds_trans_${0..10}.t_order_${0..10}");
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
    
        TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
        orderItemTableRuleConfig.setLogicTable("t_order_item");
        orderItemTableRuleConfig.setActualDataNodes("ds_trans_${0..10}.t_order_item_${0..10}");
        shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);
    
        shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
    
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("user_id", new ModuloShardingAlgorithm()));
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", new ModuloShardingAlgorithm()));
    
        return ShardingDataSourceFactory.createDataSource(createDataSourceMap(type), shardingRuleConfig, new HashMap<String, Object>(), new Properties());
    }
    
    private static Map<String, DataSource> createDataSourceMap(final DatasourceType type) {
        Map<String, DataSource> result = new HashMap<>(10, 1);
        result.put("ds_trans_0", createDataSource("ds_trans_0", type));
        result.put("ds_trans_1", createDataSource("ds_trans_1", type));
        result.put("ds_trans_2", createDataSource("ds_trans_2", type));
        result.put("ds_trans_3", createDataSource("ds_trans_3", type));
        result.put("ds_trans_4", createDataSource("ds_trans_4", type));
        result.put("ds_trans_5", createDataSource("ds_trans_5", type));
        result.put("ds_trans_6", createDataSource("ds_trans_6", type));
        result.put("ds_trans_7", createDataSource("ds_trans_7", type));
        result.put("ds_trans_8", createDataSource("ds_trans_8", type));
        result.put("ds_trans_9", createDataSource("ds_trans_9", type));
        result.put("ds_trans_10", createDataSource("ds_trans_10", type));
        return result;
    }
    
    private static DataSource createDataSource(final String dataSourceName, final DatasourceType type) {
        switch (type) {
            case XA:
                return createXADatasource(dataSourceName);
            case LOCAL:
                return createNoneXADatasource(dataSourceName);
            default:
                return null;
        }
    }
    
    private static DataSource createXADatasource(final String dataSourceName) {
        AtomikosDataSourceBean result = new AtomikosDataSourceBean();
        result.setUniqueResourceName(dataSourceName);
        result.setXaDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
        result.setMaxPoolSize(65);
        Properties xaProperties = new Properties();
        xaProperties.setProperty("user", "root");
        xaProperties.setProperty("password", "");
        xaProperties.setProperty("URL", String.format("jdbc:mysql://localhost:3306/%s", dataSourceName));
        xaProperties.setProperty("pinGlobalTxToPhysicalConnection", "true");
        result.setXaProperties(xaProperties);
        return result;
    }
    
    private static DataSource createNoneXADatasource(final String dataSourceName) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl(String.format("jdbc:mysql://localhost:3306/%s", dataSourceName));
        result.setUsername("root");
        result.setPassword("");
        return result;
    }
}
