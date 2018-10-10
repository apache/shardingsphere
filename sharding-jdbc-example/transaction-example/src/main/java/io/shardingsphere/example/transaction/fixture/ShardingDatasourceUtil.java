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
import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.api.config.TableRuleConfiguration;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.example.transaction.fixture.algorithm.PreciseModuloDatabaseShardingAlgorithm;
import io.shardingsphere.example.transaction.fixture.algorithm.PreciseModuloTableShardingAlgorithm;
import io.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
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
        orderTableRuleConfig.setActualDataNodes("ds_trans_${0..1}.t_order_${0..1}");
        orderTableRuleConfig.setKeyGeneratorColumnName("order_id");
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
    
        TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
        orderItemTableRuleConfig.setLogicTable("t_order_item");
        orderItemTableRuleConfig.setActualDataNodes("ds_trans_${0..1}.t_order_item_${0..1}");
        orderItemTableRuleConfig.setKeyGeneratorColumnName("order_item_id");
        shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);
    
        shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
    
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("user_id", new PreciseModuloDatabaseShardingAlgorithm()));
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", new PreciseModuloTableShardingAlgorithm()));
    
        Properties properties = new Properties();
        properties.put("sql.show", false);
        return ShardingDataSourceFactory.createDataSource(createDataSourceMap(type), shardingRuleConfig, new HashMap<String, Object>(), properties);
    }
    
    private static Map<String, DataSource> createDataSourceMap(final DatasourceType type) {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds_trans_0", createDataSource("ds_trans_0", type));
        result.put("ds_trans_1", createDataSource("ds_trans_1", type));
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
