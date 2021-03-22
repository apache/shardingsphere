package org.apache.shardingsphere.infra.optimize.util;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class ShardingRuleConfigUtil {
    
    public static ShardingRule createMaximumShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        
        ShardingTableRuleConfiguration tOrderTableRuleConfig = createTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${0..2}");
        tOrderTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "increment"));
        ShardingTableRuleConfiguration tOrderItemTableRuleConfig = createTableRuleConfiguration("t_order_item", "ds_${0..1}.t_order_item_${0..2}");
        shardingRuleConfig.getTables().add(tOrderTableRuleConfig);
        shardingRuleConfig.getTables().add(tOrderItemTableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(tOrderTableRuleConfig.getLogicTable() + "," + tOrderItemTableRuleConfig.getLogicTable());
    
        ShardingTableRuleConfiguration tUserableRuleConfig = createTableRuleConfiguration("t_user", "ds_${0..1}.t_user_${0..2}");
        shardingRuleConfig.getTables().add(tUserableRuleConfig);
        
        shardingRuleConfig.getBroadcastTables().add("BROADCAST_TABLE");
        InlineShardingAlgorithm shardingAlgorithmDB = new InlineShardingAlgorithm();
        Properties props = new Properties();
        props.setProperty("algorithm-expression", "ds_%{ds_id % 2}");
        shardingAlgorithmDB.setProps(props);
        shardingRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("ds_id", "standard"));
        InlineShardingAlgorithm shardingAlgorithmTBL = new InlineShardingAlgorithm();
        props = new Properties();
        props.setProperty("algorithm-expression", "table_%{table_id % 2}");
        shardingAlgorithmTBL.setProps(props);
        shardingRuleConfig.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("table_id", "standard"));
        shardingRuleConfig.setDefaultKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "default"));
        Properties properties = new Properties();
        properties.put("sharding-count", "2");
        shardingRuleConfig.getShardingAlgorithms().put("standard", new ShardingSphereAlgorithmConfiguration("HASH_MOD", properties));
        shardingRuleConfig.getKeyGenerators().put("increment", new ShardingSphereAlgorithmConfiguration("SNOWFLAKE", new Properties()));
        shardingRuleConfig.getKeyGenerators().put("default", new ShardingSphereAlgorithmConfiguration("SNOWFLAKE", new Properties()));
        return new ShardingRule(shardingRuleConfig, mock(DatabaseType.class), createDataSourceMap());
    }
    
    private static ShardingTableRuleConfiguration createTableRuleConfiguration(final String logicTableName, final String actualDataNodes) {
        return new ShardingTableRuleConfiguration(logicTableName, actualDataNodes);
    }
    
    private static Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds_0", mock(DataSource.class, RETURNS_DEEP_STUBS));
        result.put("ds_1", mock(DataSource.class, RETURNS_DEEP_STUBS));
        return result;
    }
}
