package com.dangdang.ddframe.rdb.sharding.api;

import com.dangdang.ddframe.rdb.sharding.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.InlineShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.rdb.sharding.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.rdb.sharding.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class OrchestrationShardingDataSourceFactoryTest {
    
    private static final String ZOOKEEPER_CONNECTION_STRING = "localhost:2181";
    
    private static final String NAMESPACE = "demo";
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws IOException, SQLException {
        // CHECKSTYLE:ON
        CoordinatorRegistryCenter regCenter = setUpRegistryCenter();
        OrchestrationShardingDataSourceFactory.createDataSource("demo", regCenter, createDataSourceMap(), crateShardingRuleConfig());
    }
    
    private static CoordinatorRegistryCenter setUpRegistryCenter() {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(ZOOKEEPER_CONNECTION_STRING, NAMESPACE);
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(zkConfig);
        result.init();
        return result;
    }
    
    private static Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2);
        result.put("ds_jdbc_0", createDataSource("ds_jdbc_0"));
        result.put("ds_jdbc_1", createDataSource("ds_jdbc_1"));
        return result;
    }
    
    private static DataSource createDataSource(final String dataSourceName) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl(String.format("jdbc:mysql://localhost:3306/%s", dataSourceName));
        result.setUsername("root");
        result.setPassword("");
        return result;
    }
    
    private static ShardingRuleConfig crateShardingRuleConfig() throws SQLException {
        ShardingRuleConfig result = new ShardingRuleConfig();
        TableRuleConfig orderTableRuleConfig = new TableRuleConfig();
        orderTableRuleConfig.setLogicTable("t_order");
        orderTableRuleConfig.setActualTables("t_order_${[0, 1]}");
        result.getTableRuleConfigs().add(orderTableRuleConfig);
        InlineShardingStrategyConfig databaseShardingStrategyConfig = new InlineShardingStrategyConfig();
        databaseShardingStrategyConfig.setShardingColumn("user_id");
        databaseShardingStrategyConfig.setAlgorithmInlineExpression("ds_jdbc_${user_id % 2}");
        result.setDefaultDatabaseShardingStrategyConfig(databaseShardingStrategyConfig);
        InlineShardingStrategyConfig tableShardingStrategyConfig  = new InlineShardingStrategyConfig();
        tableShardingStrategyConfig.setShardingColumn("order_id");
        tableShardingStrategyConfig.setAlgorithmInlineExpression("t_order_${order_id % 2}");
        result.setDefaultTableShardingStrategyConfig(tableShardingStrategyConfig);
        return result;
    }
}
